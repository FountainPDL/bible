package com.fountainpdl.bible.fragments;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.fountainpdl.bible.MainActivity;
import com.fountainpdl.bible.R;
import com.fountainpdl.bible.adapters.VerseAdapter;
import com.fountainpdl.bible.models.AppSettings;
import com.fountainpdl.bible.models.Bookmark;
import com.fountainpdl.bible.models.Highlight;
import com.fountainpdl.bible.models.Verse;
import com.fountainpdl.bible.utils.AppTheme;
import com.fountainpdl.bible.utils.BibleDataManager;
import com.fountainpdl.bible.utils.BooksData;
import com.fountainpdl.bible.utils.PrefsManager;
import com.fountainpdl.bible.utils.ThemeHelper;
import com.google.android.material.chip.Chip;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class ReadFragment extends Fragment {

    private MainActivity activity;
    private RecyclerView verseRecycler;
    private VerseAdapter adapter;
    private LinearLayoutManager layoutManager;
    private TextView testamentLabel, bookLabel, chapterLabel, btnBookmarkChapter;
    private View actionBar, ttsStatusBar;
    private TextView selectionInfo, ttsStatusText;

    private String book;
    private int chapter;
    private List<Verse> currentVerses = new ArrayList<>();

    // A chapter load asked for before the Bible text finished loading from assets.
    private boolean dataReady = false;
    private boolean pendingLoad = false;
    private Integer pendingScrollVerse = null;

    // Focus (one verse at a time) reading mode
    private View focusContainer, chapterHeader;
    private TextView focusRef, focusText, focusCounter;
    private int focusIndex = 0;
    private boolean focusMode = false;

    private GestureDetector gestureDetector;

    @Nullable @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_read, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        activity = (MainActivity) getActivity();
        if (activity == null) return;

        bindViews(view);
        setupRecycler();
        setupGestures(view);
        setupHeaderButtons();
        setupActionBar();
        setupTtsBar();

        activity.setTtsUiListener(this::onTtsStateChanged);

        book = activity.getCurrentBook();
        chapter = activity.getCurrentChapter();
        applyTheme();

        // The Bible text loads on a background thread. If it isn't in memory yet,
        // remember what to show and render it the moment loading finishes --
        // previously the page was filled once, empty, and never refreshed.
        pendingLoad = true;
        activity.getBibleData().loadAsync(activity, () -> {
            dataReady = true;
            if (getView() == null || !isAdded()) return;
            if (pendingLoad) {
                pendingLoad = false;
                loadChapter(book, chapter, pendingScrollVerse);
                pendingScrollVerse = null;
            }
        });
    }

    private void onTtsStateChanged(boolean playing, int verse) {
        if (getView() == null) return;
        if (playing) {
            ttsStatusBar.setVisibility(View.VISIBLE);
            ttsStatusText.setText("Reading verse " + verse + "…");
            adapter.setTtsActiveVerse(verse);
            AppSettings s = activity.getPrefsManager().getSettings();
            if (s.ttsAutoScroll) {
                if (focusMode) showFocusVerse(verse); else scrollToVerse(verse, false);
            }
        } else {
            ttsStatusBar.setVisibility(View.GONE);
            adapter.setTtsActiveVerse(-1);
        }
    }

    @Override
    public void onDestroyView() {
        saveScrollPosition();
        if (activity != null) activity.setTtsUiListener(null);
        super.onDestroyView();
    }

    @Override
    public void onPause() {
        saveScrollPosition();
        super.onPause();
    }

    /** Tabs are shown/hidden, not recreated, so onResume never fires on a tab switch. */
    @Override
    public void onHiddenChanged(boolean hidden) {
        super.onHiddenChanged(hidden);
        if (hidden) { saveScrollPosition(); return; }
        syncWithActivity();
    }

    /** Bring the page in line with the current position, settings and theme. */
    private void syncWithActivity() {
        if (activity == null || getView() == null) return;
        applyTheme();
        String b = activity.getCurrentBook();
        int c = activity.getCurrentChapter();
        if (dataReady && (!b.equals(book) || c != chapter || currentVerses.isEmpty())) {
            loadChapter(b, c, null);
        } else {
            onSettingsChanged();
            applyAnnotationsToAdapter();
        }
        syncTtsBar();
    }

    private void syncTtsBar() {
        if (ttsStatusBar == null || activity == null || activity.getTtsManager() == null) return;
        if (!activity.getTtsManager().isPlaying()) {
            ttsStatusBar.setVisibility(View.GONE);
            adapter.setTtsActiveVerse(-1);
        }
    }

    private void bindViews(View v) {
        verseRecycler = v.findViewById(R.id.verseRecycler);
        testamentLabel = v.findViewById(R.id.testamentLabel);
        bookLabel = v.findViewById(R.id.bookLabel);
        chapterLabel = v.findViewById(R.id.chapterLabel);
        btnBookmarkChapter = v.findViewById(R.id.btnBookmarkChapter);
        actionBar = v.findViewById(R.id.actionBar);
        ttsStatusBar = v.findViewById(R.id.ttsStatusBar);
        selectionInfo = v.findViewById(R.id.selectionInfo);
        ttsStatusText = v.findViewById(R.id.ttsStatusText);
        focusContainer = v.findViewById(R.id.focusContainer);
        chapterHeader = v.findViewById(R.id.chapterHeader);
        focusRef = v.findViewById(R.id.focusRef);
        focusText = v.findViewById(R.id.focusText);
        focusCounter = v.findViewById(R.id.focusCounter);
        v.findViewById(R.id.focusPrev).setOnClickListener(x -> stepFocus(-1));
        v.findViewById(R.id.focusNext).setOnClickListener(x -> stepFocus(1));
    }

    private void setupRecycler() {
        layoutManager = new LinearLayoutManager(getContext());
        verseRecycler.setLayoutManager(layoutManager);
        adapter = new VerseAdapter(new VerseAdapter.Listener() {
            @Override public void onVerseTap(int verseNum) {
                adapter.toggleSelection(verseNum);
                updateActionBar();
                if (activity.getPrefsManager().getSettings().vibrateOnSelect) vibrateTick();
            }
            @Override public void onVerseLongPress(int verseNum) {
                startReadingFrom(verseNum);
            }
            @Override public void onWordAnnotationTap(String annotationId) {
                // Word annotation removal handled via Library screen edit; quick-remove here too
                removeWordAnnotationById(annotationId);
            }
        });
        verseRecycler.setAdapter(adapter);

        verseRecycler.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override public void onScrollStateChanged(@NonNull RecyclerView rv, int newState) {
                if (newState == RecyclerView.SCROLL_STATE_IDLE) saveScrollPosition();
            }
        });
    }

    private void setupGestures(View root) {
        gestureDetector = new GestureDetector(getContext(), new GestureDetector.SimpleOnGestureListener() {
            @Override
            public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
                if (e1 == null) return false;
                float dx = e2.getX() - e1.getX();
                float dy = e2.getY() - e1.getY();
                if (Math.abs(dx) > 90 && Math.abs(dx) > Math.abs(dy) * 1.6f && Math.abs(velocityX) > 250) {
                    if (dx < 0) nextChapter(); else prevChapter();
                    return true;
                }
                return false;
            }
        });
        // Attach a transparent touch listener on the RecyclerView that only intercepts
        // fast horizontal flings, letting normal vertical scroll and item taps through.
        verseRecycler.addOnItemTouchListener(new RecyclerView.SimpleOnItemTouchListener() {
            @Override public boolean onInterceptTouchEvent(@NonNull RecyclerView rv, @NonNull MotionEvent e) {
                gestureDetector.onTouchEvent(e);
                return false;
            }
        });
    }

    private void setupHeaderButtons() {
        View v = getView();
        if (v == null) return;
        v.findViewById(R.id.btnPrevChapter).setOnClickListener(x -> prevChapter());
        v.findViewById(R.id.btnNextChapter).setOnClickListener(x -> nextChapter());
        v.findViewById(R.id.btnReadAloud).setOnClickListener(x -> startReadingFromTop());
        btnBookmarkChapter.setOnClickListener(x -> toggleChapterBookmark());
    }

    private void setupActionBar() {
        View v = getView();
        if (v == null) return;
        ((Chip) v.findViewById(R.id.chipHighlight)).setOnClickListener(x -> applyHighlight());
        ((Chip) v.findViewById(R.id.chipUnderline)).setOnClickListener(x -> applyUnderline());
        ((Chip) v.findViewById(R.id.chipBookmark)).setOnClickListener(x -> applyBookmark());
        ((Chip) v.findViewById(R.id.chipRead)).setOnClickListener(x -> {
            Set<Integer> sel = adapter.getSelected();
            if (!sel.isEmpty()) startReadingFrom(java.util.Collections.min(sel));
        });
        ((Chip) v.findViewById(R.id.chipNote)).setOnClickListener(x -> {
            String ref = buildSelectionRef();
            adapter.clearSelection();
            updateActionBar();
            activity.openNoteDialog(ref, null);
        });
        ((Chip) v.findViewById(R.id.chipWord)).setOnClickListener(x -> openWordAnnotationDialog());
        ((Chip) v.findViewById(R.id.chipCopy)).setOnClickListener(x -> copySelection());
        ((Chip) v.findViewById(R.id.chipClear)).setOnClickListener(x -> { adapter.clearSelection(); updateActionBar(); });
    }

    private void setupTtsBar() {
        View v = getView();
        if (v == null) return;
        v.findViewById(R.id.ttsStopBtn).setOnClickListener(x -> activity.getTtsManager().stop());
    }

    // ── Chapter loading ──────────────────────────────────────────────────────
    public void loadChapter(String book, int chapter, Integer scrollToVerse) {
        if (activity == null || getView() == null) return;

        // Remember where we were in the chapter we're leaving
        if (this.book != null && !currentVerses.isEmpty()) saveScrollPosition();

        this.book = book;
        this.chapter = chapter;

        if (!dataReady) {
            pendingLoad = true;
            pendingScrollVerse = scrollToVerse;
            return;
        }

        PrefsManager prefs = activity.getPrefsManager();
        AppSettings settings = prefs.getSettings();
        boolean isNT = BooksData.isNewTestament(book);

        testamentLabel.setText(isNT ? "NEW TESTAMENT" : "OLD TESTAMENT");
        bookLabel.setText(book);
        chapterLabel.setText("CHAPTER " + chapter);

        BibleDataManager data = activity.getBibleData();
        currentVerses = data.getChapterAsVerseList(book, chapter, settings.translation);

        applyTheme();
        adapter.setTheme(activity.getAppTheme());
        adapter.setSettings(settings);
        adapter.setIsNewTestament(isNT);
        adapter.setVerses(currentVerses);
        applyAnnotationsToAdapter();
        updateChapterBookmarkLabel();
        updateActionBar();
        applyReadingMode(settings);

        if (scrollToVerse != null) {
            final int target = scrollToVerse;
            if (focusMode) showFocusVerse(target);
            else {
                scrollToVerse(target, false);
                new Handler(Looper.getMainLooper()).postDelayed(() -> { if (getView() != null) scrollToVerse(target, true); }, 150);
            }
        } else {
            int pos = prefs.getScrollAnchorPosition(book, chapter);
            int off = prefs.getScrollAnchorOffset(book, chapter);
            if (pos < 0 || pos >= currentVerses.size()) { pos = 0; off = 0; }
            // Position-based restore works even while this tab is hidden or not yet
            // laid out; it is applied on the next layout pass.
            layoutManager.scrollToPositionWithOffset(pos, off);
            focusIndex = pos;
            if (focusMode) showFocusVerse(currentVerses.isEmpty() ? 0 : currentVerses.get(pos).verseNum);
        }
        if (currentVerses.isEmpty()) {
            Toast.makeText(getContext(), book + " " + chapter + " is not available in " + settings.translation, Toast.LENGTH_SHORT).show();
        }
    }

    private void scrollToVerse(int verseNum, boolean flash) {
        int pos = adapter.findPositionForVerse(verseNum);
        if (pos >= 0) {
            layoutManager.scrollToPositionWithOffset(pos, 16);
            if (flash) {
                adapter.flashVerse(verseNum);
                new Handler(Looper.getMainLooper()).postDelayed(() -> { if (getView() != null) adapter.clearFlash(); }, 2000);
            }
        }
    }

    private void saveScrollPosition() {
        if (activity == null || book == null || layoutManager == null || currentVerses.isEmpty()) return;
        int pos = layoutManager.findFirstVisibleItemPosition();
        if (focusMode) pos = focusIndex;
        if (pos < 0) return;
        View first = layoutManager.findViewByPosition(pos);
        int offset = first != null ? first.getTop() : 0;
        activity.getPrefsManager().saveScrollAnchor(book, chapter, pos, focusMode ? 0 : offset);
    }

    // ── Theme & settings (called live from MainActivity) ─────────────────────
    public void onThemeChanged() {
        if (activity == null || getView() == null) return;
        applyTheme();
        adapter.setTheme(activity.getAppTheme());
        adapter.notifyDataSetChanged();
    }

    /** Any setting changed: re-read and redraw once. Cheap enough to call on every slider tick. */
    public void onSettingsChanged() {
        if (activity == null || getView() == null) return;
        AppSettings s = activity.getPrefsManager().getSettings();
        applyTheme();
        adapter.setTheme(activity.getAppTheme());
        adapter.setSettings(s);
        adapter.notifyDataSetChanged();
        applyReadingMode(s);
        if (activity.getTtsManager() != null) {
            activity.getTtsManager().setRate(s.ttsRate);
            activity.getTtsManager().setPitch(s.ttsPitch);
        }
    }

    private void applyReadingMode(AppSettings s) {
        boolean wantFocus = "focus".equals(s.readingMode);
        boolean changed = wantFocus != focusMode;
        focusMode = wantFocus;
        verseRecycler.setVisibility(focusMode ? View.GONE : View.VISIBLE);
        focusContainer.setVisibility(focusMode ? View.VISIBLE : View.GONE);
        testamentLabel.setVisibility(s.showChapterHeader ? View.VISIBLE : View.GONE);
        bookLabel.setVisibility(s.showChapterHeader ? View.VISIBLE : View.GONE);
        chapterLabel.setVisibility(s.showChapterHeader ? View.VISIBLE : View.GONE);
        if (focusMode) {
            int first = layoutManager.findFirstVisibleItemPosition();
            if (changed && first >= 0) focusIndex = first;
            renderFocus(s);
        } else if (changed) {
            layoutManager.scrollToPositionWithOffset(Math.max(0, Math.min(focusIndex, Math.max(0, currentVerses.size() - 1))), 0);
        }
    }

    private void stepFocus(int delta) {
        int next = focusIndex + delta;
        if (next < 0) { prevChapter(); return; }
        if (next >= currentVerses.size()) { nextChapter(); return; }
        focusIndex = next;
        renderFocus(activity.getPrefsManager().getSettings());
    }

    private void showFocusVerse(int verseNum) {
        int pos = adapter.findPositionForVerse(verseNum);
        if (pos >= 0) focusIndex = pos;
        renderFocus(activity.getPrefsManager().getSettings());
    }

    private void renderFocus(AppSettings s) {
        if (currentVerses.isEmpty()) { focusText.setText(""); focusRef.setText(""); focusCounter.setText("0 / 0"); return; }
        if (focusIndex < 0) focusIndex = 0;
        if (focusIndex >= currentVerses.size()) focusIndex = currentVerses.size() - 1;
        Verse v = currentVerses.get(focusIndex);
        focusRef.setText(book + " " + chapter + ":" + v.verseNum);
        focusText.setText(v.text);
        focusText.setTextSize(s.fontSize + 2);
        focusText.setTypeface(android.graphics.Typeface.create(
            s.fontFamily != null ? s.fontFamily : "serif", android.graphics.Typeface.ITALIC));
        focusCounter.setText((focusIndex + 1) + " / " + currentVerses.size());
    }

    private void vibrateTick() {
        try {
            android.os.Vibrator vib = (android.os.Vibrator) requireContext().getSystemService(Context.VIBRATOR_SERVICE);
            if (vib != null && vib.hasVibrator()) {
                vib.vibrate(android.os.VibrationEffect.createOneShot(15, android.os.VibrationEffect.DEFAULT_AMPLITUDE));
            }
        } catch (Exception ignored) {}
    }

    /** Paint everything the XML can't: labels, pills, bars and chips follow the live theme. */
    private void applyTheme() {
        View root = getView();
        if (root == null || activity == null) return;
        AppTheme t = activity.getAppTheme();
        root.setBackgroundColor(t.bg);
        testamentLabel.setTextColor(t.primaryFg);
        bookLabel.setTextColor(t.text);
        chapterLabel.setTextColor(t.sub);
        for (int id : new int[]{R.id.btnPrevChapter, R.id.btnNextChapter, R.id.btnReadAloud,
                                R.id.btnBookmarkChapter, R.id.ttsStopBtn, R.id.focusPrev, R.id.focusNext}) {
            ThemeHelper.pill((TextView) root.findViewById(id), t);
        }
        ThemeHelper.bar(actionBar, t, 16);
        ThemeHelper.bar(ttsStatusBar, t, 12);
        selectionInfo.setTextColor(t.sub);
        ttsStatusText.setTextColor(t.text);
        focusRef.setTextColor(t.primaryFg);
        focusText.setTextColor(t.text);
        focusCounter.setTextColor(t.sub);
        for (int id : new int[]{R.id.chipHighlight, R.id.chipUnderline, R.id.chipBookmark, R.id.chipRead,
                                R.id.chipWord, R.id.chipNote, R.id.chipCopy, R.id.chipClear}) {
            Chip c = root.findViewById(id);
            c.setChipBackgroundColor(android.content.res.ColorStateList.valueOf(t.card));
            c.setChipStrokeColor(android.content.res.ColorStateList.valueOf(t.border));
            c.setChipStrokeWidth(ThemeHelper.dp(requireContext(), 1));
            c.setTextColor(t.text);
        }
        updateChapterBookmarkLabel();
    }

    /** Called when translation is switched -- keeps the same top-visible verse. */
    public void onTranslationChanged() {
        if (activity == null || book == null || getView() == null || !dataReady) return;
        int firstVisible = focusMode ? focusIndex : layoutManager.findFirstVisibleItemPosition();
        Integer topVerse = null;
        if (firstVisible >= 0 && firstVisible < currentVerses.size()) {
            topVerse = currentVerses.get(firstVisible).verseNum;
        }
        AppSettings settings = activity.getPrefsManager().getSettings();
        currentVerses = activity.getBibleData().getChapterAsVerseList(book, chapter, settings.translation);
        adapter.setSettings(settings);
        adapter.setVerses(currentVerses);
        applyAnnotationsToAdapter();
        updateActionBar();
        if (topVerse != null) {
            final int tv = topVerse;
            if (focusMode) showFocusVerse(tv);
            else layoutManager.scrollToPositionWithOffset(Math.max(0, adapter.findPositionForVerse(tv)), 0);
        }
        if (focusMode) renderFocus(settings);
    }

    private void prevChapter() {
        if (chapter > 1) { activity.navigateToChapter(book, chapter - 1, null); return; }
        int idx = BooksData.ALL_BOOKS.indexOf(book);
        if (idx > 0) {
            String prevBook = BooksData.ALL_BOOKS.get(idx - 1);
            activity.navigateToChapter(prevBook, BooksData.getChapterCount(prevBook), null);
        }
    }

    private void nextChapter() {
        if (chapter < BooksData.getChapterCount(book)) { activity.navigateToChapter(book, chapter + 1, null); return; }
        int idx = BooksData.ALL_BOOKS.indexOf(book);
        if (idx < BooksData.ALL_BOOKS.size() - 1) {
            activity.navigateToChapter(BooksData.ALL_BOOKS.get(idx + 1), 1, null);
        }
    }

    // ── TTS ───────────────────────────────────────────────────────────────────
    public void startReadingFromTop() {
        if (!currentVerses.isEmpty()) startReadingFrom(currentVerses.get(0).verseNum);
    }

    private void startReadingFrom(int verseNum) {
        if (activity == null || activity.getTtsManager() == null) return;
        if (!activity.getTtsManager().isReady()) {
            Toast.makeText(getContext(), "Text-to-speech is starting up, try again in a moment", Toast.LENGTH_SHORT).show();
            return;
        }
        AppSettings s = activity.getPrefsManager().getSettings();
        activity.getTtsManager().setRate(s.ttsRate);
        activity.getTtsManager().setPitch(s.ttsPitch);
        List<Verse> toRead = new ArrayList<>();
        boolean started = false;
        for (Verse v : currentVerses) {
            if (v.verseNum == verseNum) started = true;
            if (started) toRead.add(v);
        }
        activity.getTtsManager().readVerses(toRead);
    }

    // ── Selection actions ────────────────────────────────────────────────────
    private void updateActionBar() {
        Set<Integer> sel = adapter.getSelected();
        if (sel.isEmpty()) {
            actionBar.setVisibility(View.GONE);
        } else {
            actionBar.setVisibility(View.VISIBLE);
            selectionInfo.setText(sel.size() + (sel.size() == 1 ? " verse — " : " verses — ") + buildSelectionRef());
        }
    }

    private String buildSelectionRef() {
        List<Integer> sorted = new ArrayList<>(adapter.getSelected());
        java.util.Collections.sort(sorted);
        StringBuilder sb = new StringBuilder();
        int i = 0;
        while (i < sorted.size()) {
            int j = i;
            while (j + 1 < sorted.size() && sorted.get(j + 1) == sorted.get(j) + 1) j++;
            if (sb.length() > 0) sb.append(',');
            sb.append(sorted.get(i));
            if (j > i) sb.append('-').append(sorted.get(j));
            i = j + 1;
        }
        return book + " " + chapter + ":" + sb;
    }

    private void applyHighlight() {
        PrefsManager prefs = activity.getPrefsManager();
        List<Highlight> list = prefs.getHighlights();
        boolean allOn = true;
        for (int v : adapter.getSelected()) {
            String key = book + ":" + chapter + ":" + v + ":highlight";
            boolean found = false;
            for (Highlight h : list) if (h.key.equals(key)) { found = true; break; }
            if (!found) { allOn = false; break; }
        }
        for (int v : adapter.getSelected()) {
            String key = book + ":" + chapter + ":" + v + ":highlight";
            if (allOn) list.removeIf(h -> h.key.equals(key));
            else if (list.stream().noneMatch(h -> h.key.equals(key))) list.add(new Highlight(book, chapter, v, "highlight", "#F1C40F"));
        }
        prefs.saveHighlights(list);
        adapter.clearSelection();
        updateActionBar();
        applyAnnotationsToAdapter();
    }

    private void applyUnderline() {
        PrefsManager prefs = activity.getPrefsManager();
        List<Highlight> list = prefs.getHighlights();
        boolean allOn = true;
        for (int v : adapter.getSelected()) {
            String key = book + ":" + chapter + ":" + v + ":underline";
            boolean found = false;
            for (Highlight h : list) if (h.key.equals(key)) { found = true; break; }
            if (!found) { allOn = false; break; }
        }
        for (int v : adapter.getSelected()) {
            String key = book + ":" + chapter + ":" + v + ":underline";
            if (allOn) list.removeIf(h -> h.key.equals(key));
            else if (list.stream().noneMatch(h -> h.key.equals(key))) list.add(new Highlight(book, chapter, v, "underline", null));
        }
        prefs.saveHighlights(list);
        adapter.clearSelection();
        updateActionBar();
        applyAnnotationsToAdapter();
    }

    private void applyBookmark() {
        PrefsManager prefs = activity.getPrefsManager();
        List<Bookmark> list = prefs.getBookmarks();
        for (int v : adapter.getSelected()) {
            String key = book + ":" + chapter + ":" + v;
            boolean exists = list.stream().anyMatch(b -> b.key.equals(key));
            if (exists) list.removeIf(b -> b.key.equals(key));
            else list.add(new Bookmark(book, chapter, v));
        }
        prefs.saveBookmarks(list);
        adapter.clearSelection();
        updateActionBar();
        applyAnnotationsToAdapter();
    }

    private void toggleChapterBookmark() {
        PrefsManager prefs = activity.getPrefsManager();
        List<Bookmark> list = prefs.getBookmarks();
        String key = book + ":" + chapter + ":ALL";
        boolean exists = list.stream().anyMatch(b -> b.key.equals(key));
        if (exists) list.removeIf(b -> b.key.equals(key));
        else list.add(new Bookmark(book, chapter, -1));
        prefs.saveBookmarks(list);
        updateChapterBookmarkLabel();
    }

    private void updateChapterBookmarkLabel() {
        if (activity == null || book == null || btnBookmarkChapter == null) return;
        List<Bookmark> list = activity.getPrefsManager().getBookmarks();
        String key = book + ":" + chapter + ":ALL";
        boolean exists = list.stream().anyMatch(b -> b.key.equals(key));
        btnBookmarkChapter.setText(exists ? "Bookmarked" : "Bookmark");
        AppTheme t = activity.getAppTheme();
        if (exists) ThemeHelper.pillFilled(btnBookmarkChapter, t); else ThemeHelper.pill(btnBookmarkChapter, t);
    }

    private void copySelection() {
        List<Integer> sorted = new ArrayList<>(adapter.getSelected());
        java.util.Collections.sort(sorted);
        StringBuilder sb = new StringBuilder();
        for (int v : sorted) {
            for (Verse verse : currentVerses) if (verse.verseNum == v) sb.append(v).append(' ').append(verse.text).append('\n');
        }
        sb.append("— ").append(book).append(' ').append(chapter);
        ClipboardManager cm = (ClipboardManager) requireContext().getSystemService(Context.CLIPBOARD_SERVICE);
        cm.setPrimaryClip(ClipData.newPlainText("verse", sb.toString()));
        Toast.makeText(getContext(), "Copied", Toast.LENGTH_SHORT).show();
        adapter.clearSelection();
        updateActionBar();
    }

    private void openWordAnnotationDialog() {
        Set<Integer> sel = adapter.getSelected();
        if (sel.isEmpty()) return;
        List<Integer> sorted = new ArrayList<>(sel);
        java.util.Collections.sort(sorted);
        StringBuilder sb = new StringBuilder();
        for (int v : sorted) {
            for (Verse verse : currentVerses) if (verse.verseNum == v) sb.append(v).append(' ').append(verse.text).append(' ');
        }
        int firstVerse = sorted.get(0);
        String selectedText = sb.toString().trim();
        adapter.clearSelection();
        updateActionBar();
        new com.fountainpdl.bible.dialogs.WordAnnotationDialog(requireContext(), activity.getAppTheme(),
            book, chapter, firstVerse, selectedText.length() > 200 ? selectedText.substring(0, 200) : selectedText,
            this::applyAnnotationsToAdapter).show();
    }

    // ── Word annotations (stored inside SharedPreferences via a simple prefix key on Highlight-like model) ──
    private void removeWordAnnotationById(String id) {
        // Word annotations are stored as a dedicated list; see LibraryFragment for full CRUD.
        // Quick tap-to-remove here re-reads/writes the same store.
        com.fountainpdl.bible.utils.WordAnnotationStore store = new com.fountainpdl.bible.utils.WordAnnotationStore(requireContext());
        store.remove(id);
        applyAnnotationsToAdapter();
    }

    private void applyAnnotationsToAdapter() {
        if (activity == null || getView() == null || book == null) return;
        PrefsManager prefs = activity.getPrefsManager();

        Map<Integer, Highlight> hlMap = new HashMap<>();
        Map<Integer, Highlight> ulMap = new HashMap<>();
        for (Highlight h : prefs.getHighlights()) {
            if (!h.book.equals(book) || h.chapter != chapter) continue;
            if ("highlight".equals(h.type)) hlMap.put(h.verse, h);
            else if ("underline".equals(h.type)) ulMap.put(h.verse, h);
        }

        Set<Integer> bm = new HashSet<>();
        for (Bookmark b : prefs.getBookmarks()) {
            if (b.book.equals(book) && b.chapter == chapter && b.verse >= 0) bm.add(b.verse);
        }

        com.fountainpdl.bible.utils.WordAnnotationStore store = new com.fountainpdl.bible.utils.WordAnnotationStore(requireContext());
        Map<Integer, List<VerseAdapter.WordAnn>> wordMap = new HashMap<>();
        for (com.fountainpdl.bible.utils.WordAnnotationStore.WordAnnotation wa : store.getForChapter(book, chapter)) {
            List<VerseAdapter.WordAnn> list = wordMap.computeIfAbsent(wa.verse, k -> new ArrayList<>());
            VerseAdapter.WordAnn a = new VerseAdapter.WordAnn();
            a.id = wa.id; a.text = wa.text; a.type = wa.type; a.color = wa.color;
            list.add(a);
        }
        adapter.setAnnotations(hlMap, ulMap, bm, wordMap);
    }

    @Override
    public void onResume() {
        super.onResume();
        if (book != null && dataReady && !isHidden()) {
            applyAnnotationsToAdapter();
            syncTtsBar();
        }
    }
}
