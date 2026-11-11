package com.fountainpdl.bible.utils;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.speech.tts.TextToSpeech;
import android.speech.tts.UtteranceProgressListener;

import com.fountainpdl.bible.models.Verse;

import java.util.List;
import java.util.Locale;

/**
 * Wraps Android's native TextToSpeech engine.
 *
 * All Listener callbacks are delivered on the main thread.
 *
 * Stop handling: tts.stop() makes the engine fire onStop() (not onDone()) for the
 * utterance in flight, and the old code never told the UI that playback had
 * ended -- so the "now reading" banner stayed up forever. stop() now reports
 * onDone() itself, and every engine callback is checked against the id of the
 * utterance we expect, so a late callback from a stopped run can't restart or
 * advance the queue.
 */
public class TTSManager implements TextToSpeech.OnInitListener {

    public interface Listener {
        void onReady();
        void onVerseStart(int verseNum);
        /** Playback ended for any reason: finished, stopped by the user, or failed. */
        void onDone();
        void onError(String message);
    }

    private final Handler main = new Handler(Looper.getMainLooper());
    private TextToSpeech tts;
    private boolean ready = false;
    private Listener listener;
    private List<Verse> queue;
    private int queueIndex = 0;
    private boolean playing = false;
    private String currentUtteranceId = null;
    private float rate = 0.9f;
    private float pitch = 1.0f;

    public TTSManager(Context context, Listener listener) {
        this.listener = listener;
        tts = new TextToSpeech(context.getApplicationContext(), this);
    }

    @Override
    public void onInit(int status) {
        main.post(() -> handleInit(status));
    }

    private void handleInit(int status) {
        if (tts == null) return; // shut down before the engine came up
        if (status == TextToSpeech.SUCCESS) {
            int result = tts.setLanguage(Locale.US);
            if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                tts.setLanguage(Locale.getDefault());
            }
            tts.setSpeechRate(rate);
            tts.setPitch(pitch);

            tts.setOnUtteranceProgressListener(new UtteranceProgressListener() {
                @Override public void onStart(String utteranceId) {}
                @Override public void onDone(String utteranceId) {
                    main.post(() -> advanceQueue(utteranceId));
                }
                @Override public void onStop(String utteranceId, boolean interrupted) {
                    // Deliberately ignored: stop() reports the end of playback itself.
                }
                @Override public void onError(String utteranceId) {
                    main.post(() -> advanceQueue(utteranceId));
                }
            });

            ready = true;
            if (listener != null) listener.onReady();
        } else {
            ready = false;
            if (listener != null) listener.onError("Text-to-speech engine unavailable on this device");
        }
    }

    public boolean isReady() { return ready; }

    public void setRate(float r) {
        rate = r;
        if (tts != null && ready) tts.setSpeechRate(r);
    }

    public void setPitch(float p) {
        pitch = p;
        if (tts != null && ready) tts.setPitch(p);
    }

    /** Begin reading a list of verses in order, starting at index 0. */
    public void readVerses(List<Verse> verses) {
        if (!ready || verses == null || verses.isEmpty()) return;
        if (playing) silence();
        this.queue = verses;
        this.queueIndex = 0;
        this.playing = true;
        speakCurrent();
    }

    private void speakCurrent() {
        if (!playing || queue == null || queueIndex >= queue.size()) {
            finish();
            return;
        }
        Verse v = queue.get(queueIndex);
        if (listener != null) listener.onVerseStart(v.verseNum);
        Bundle params = new Bundle();
        currentUtteranceId = "verse_" + v.verseNum + "_" + System.nanoTime();
        int result = tts.speak(v.text, TextToSpeech.QUEUE_FLUSH, params, currentUtteranceId);
        if (result == TextToSpeech.ERROR) {
            queueIndex++;
            speakCurrent();
        }
    }

    private void advanceQueue(String utteranceId) {
        if (!playing) return;
        if (utteranceId == null || !utteranceId.equals(currentUtteranceId)) return; // stale
        queueIndex++;
        speakCurrent();
    }

    /** Playback reached its natural end (or ran out of verses). */
    private void finish() {
        boolean wasPlaying = playing;
        reset();
        if (wasPlaying && listener != null) listener.onDone();
    }

    private void reset() {
        playing = false;
        queue = null;
        queueIndex = 0;
        currentUtteranceId = null;
    }

    private void silence() {
        reset();
        if (tts != null && ready) tts.stop();
    }

    /** Stop immediately and tell the UI, so the banner goes away. Safe to call when idle. */
    public void stop() {
        boolean wasPlaying = playing;
        silence();
        if (wasPlaying && listener != null) listener.onDone();
    }

    public boolean isPlaying() { return playing; }

    public void shutdown() {
        listener = null; // Activity is going away; nothing to update
        silence();
        if (tts != null) {
            tts.shutdown();
            tts = null;
        }
        ready = false;
    }
}
