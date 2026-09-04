package com.fountainpdl.bible.utils;

import android.content.Context;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.speech.tts.UtteranceProgressListener;

import com.fountainpdl.bible.models.Verse;

import java.util.List;
import java.util.Locale;

/**
 * Wraps Android's native TextToSpeech engine directly -- no WebView, no
 * Web Speech API, no async voice-loading race conditions. This is the
 * fix for TTS being stuck on "initializing": native TextToSpeech fires
 * a reliable onInit(SUCCESS/ERROR) callback the moment the engine is
 * ready, instead of depending on a browser API that Android's WebView
 * only partially implements.
 */
public class TTSManager implements TextToSpeech.OnInitListener {

    public interface Listener {
        void onReady();
        void onVerseStart(int verseNum);
        void onDone();
        void onError(String message);
    }

    private TextToSpeech tts;
    private boolean ready = false;
    private Listener listener;
    private List<Verse> queue;
    private int queueIndex = 0;
    private boolean playing = false;

    public TTSManager(Context context, Listener listener) {
        this.listener = listener;
        tts = new TextToSpeech(context.getApplicationContext(), this);
    }

    @Override
    public void onInit(int status) {
        if (status == TextToSpeech.SUCCESS) {
            int result = tts.setLanguage(Locale.US);
            if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                // Fall back to device default language rather than failing outright
                tts.setLanguage(Locale.getDefault());
            }
            tts.setSpeechRate(0.9f);
            tts.setPitch(1.0f);

            tts.setOnUtteranceProgressListener(new UtteranceProgressListener() {
                @Override public void onStart(String utteranceId) {}
                @Override public void onDone(String utteranceId) {
                    android.os.Handler main = new android.os.Handler(android.os.Looper.getMainLooper());
                    main.post(() -> advanceQueue());
                }
                @Override public void onError(String utteranceId) {
                    android.os.Handler main = new android.os.Handler(android.os.Looper.getMainLooper());
                    main.post(() -> advanceQueue());
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

    public void setRate(float rate) { if (tts != null) tts.setSpeechRate(rate); }
    public void setPitch(float pitch) { if (tts != null) tts.setPitch(pitch); }

    /** Begin reading a list of verses in order, starting at index 0. */
    public void readVerses(List<Verse> verses) {
        if (!ready || verses == null || verses.isEmpty()) return;
        this.queue = verses;
        this.queueIndex = 0;
        this.playing = true;
        speakCurrent();
    }

    private void speakCurrent() {
        if (!playing || queue == null || queueIndex >= queue.size()) {
            playing = false;
            if (listener != null) listener.onDone();
            return;
        }
        Verse v = queue.get(queueIndex);
        if (listener != null) listener.onVerseStart(v.verseNum);
        Bundle params = new Bundle();
        String utteranceId = "verse_" + v.verseNum + "_" + System.nanoTime();
        int result = tts.speak(v.text, TextToSpeech.QUEUE_FLUSH, params, utteranceId);
        if (result == TextToSpeech.ERROR) {
            advanceQueue();
        }
    }

    private void advanceQueue() {
        if (!playing) return;
        queueIndex++;
        speakCurrent();
    }

    public void stop() {
        playing = false;
        queue = null;
        queueIndex = 0;
        if (tts != null) tts.stop();
    }

    public boolean isPlaying() { return playing; }

    public void shutdown() {
        stop();
        if (tts != null) {
            tts.shutdown();
            tts = null;
        }
    }
}
