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

        activity.setTtsUiListener((playing, verse) -> {
            if (playing) {
                ttsStatusBar.setVisibility(View.VISIBLE);
                ttsStatusText.setText("Reading verse " + verse + "…");
                adapter.setTtsActiveVerse(verse);
                scrollToVerse(verse, false);
            } else {
                ttsStatusBar.setVisibility(View.GONE);
                adapter.setTtsActiveVerse(-1);
            }
        });

        book = activity.getCurrentBook();
        chapter = activity.getCurrentChapter();
        loadChapter(book, chapter, null);
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
    }

    private void setupRecycler() {
        layoutManager = new LinearLayoutManager(getContext());
        verseRecycler.setLayoutManager(layoutManager);
        adapter = new VerseAdapter(new VerseAdapter.Listener() {
            @Override public void onVerseTap(int verseNum) {
                adapter.toggleSelection(verseNum);
                updateActionBar();
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
            @Override public void onScrolled(@NonNull RecyclerView rv, int dx, int dy) {
                saveScrollPosition();
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
        if (activity == null) return;
        PrefsManager prefs = activity.getPrefsManager();

        // Save scroll of the chapter we're leaving
        if (this.book != null && scrollToVerse == null) saveScrollPosition();

        this.book = book;
        this.chapter = chapter;

        AppSettings settings = prefs.getSettings();
        boolean isNT = BooksData.isNewTestament(book);

        testamentLabel.setText(isNT ? "NEW TESTAMENT" : "OLD TESTAMENT");
        bookLabel.setText(book);
        chapterLabel.setText("CHAPTER " + chapter);

        BibleDataManager data = activity.getBibleData();
        currentVerses = data.getChapterAsVerseList(book, chapter, settings.translation);

        adapter.setTheme(activity.getAppTheme());
        adapter.setSettings(settings);
        adapter.setIsNewTestament(isNT);
        adapter.setVerses(currentVerses);

        applyAnnotationsToAdapter();
        updateChapterBookmarkLabel();

        if (scrollToVerse != null) {
            new Handler(Looper.getMainLooper()).postDelayed(() -> scrollToVerse(scrollToVerse, true), 120);
        } else {
            int savedScroll = prefs.getScrollPosition(book, chapter);
            verseRecycler.scrollToPosition(0);
            if (savedScroll > 0) {
                new Handler(Looper.getMainLooper()).postDelayed(() -> verseRecycler.scrollBy(0, savedScroll), 60);
            }
        }
    }

    private void scrollToVerse(int verseNum, boolean flash) {
        int pos = adapter.findPositionForVerse(verseNum);
        if (pos >= 0) {
            layoutManager.scrollToPositionWithOffset(pos, 16);
            if (flash) {
                adapter.flashVerse(verseNum);
                new Handler(Looper.getMainLooper()).postDelayed(() -> adapter.clearFlash(), 2000);
            }
        }
    }

    private void saveScrollPosition() {
        if (activity == null || book == null) return;
        View firstChild = layoutManager.findViewByPosition(layoutManager.findFirstVisibleItemPosition());
        int offset = firstChild != null ? -firstChild.getTop() : 0;
        int pos = layoutManager.findFirstVisibleItemPosition();
        // Encode as approximate pixel offset from top using computeVerticalScrollOffset
        int scrollY = verseRecycler.computeVerticalScrollOffset();
        activity.getPrefsManager().saveScrollPosition(book, chapter, scrollY);
    }

    public void onThemeChanged() {
        if (activity == null) return;
        adapter.setTheme(activity.getAppTheme());
        adapter.notifyDataSetChanged();
    }

    /** Called when translation is switched -- keeps the same top-visible verse. */
    public void onTranslationChanged() {
        if (activity == null || book == null) return;
        int firstVisible = layoutManager.findFirstVisibleItemPosition();
        Integer topVerse = null;
        if (firstVisible >= 0 && firstVisible < currentVerses.size()) {
            topVerse = currentVerses.get(firstVisible).verseNum;
        }
        AppSettings settings = activity.getPrefsManager().getSettings();
        currentVerses = activity.getBibleData().getChapterAsVerseList(book, chapter, settings.translation);
        adapter.setSettings(settings);
        adapter.setVerses(currentVerses);
        applyAnnotationsToAdapter();
        if (topVerse != null) {
            final int tv = topVerse;
            new Handler(Looper.getMainLooper()).postDelayed(() -> scrollToVerse(tv, false), 80);
        }
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
        for (int i = 0; i < sorted.size(); i++) { if (i > 0) sb.append(','); sb.append(sorted.get(i)); }
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
        List<Bookmark> list = activity.getPrefsManager().getBookmarks();
        String key = book + ":" + chapter + ":ALL";
        boolean exists = list.stream().anyMatch(b -> b.key.equals(key));
        btnBookmarkChapter.setText(exists ? "Bookmarked" : "Bookmark");
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
        if (activity == null) return;
        PrefsManager prefs = activity.getPrefsManager();

        Map<Integer, Highlight> hlMap = new HashMap<>();
        Map<Integer, Highlight> ulMap = new HashMap<>();
        for (Highlight h : prefs.getHighlights()) {
            if (!h.book.equals(book) || h.chapter != chapter) continue;
            if ("highlight".equals(h.type)) hlMap.put(h.verse, h);
            else if ("underline".equals(h.type)) ulMap.put(h.verse, h);
        }
        adapter.setHighlights(hlMap);
        adapter.setUnderlines(ulMap);

        Set<Integer> bm = new HashSet<>();
        for (Bookmark b : prefs.getBookmarks()) {
            if (b.book.equals(book) && b.chapter == chapter && b.verse >= 0) bm.add(b.verse);
        }
        adapter.setBookmarked(bm);

        com.fountainpdl.bible.utils.WordAnnotationStore store = new com.fountainpdl.bible.utils.WordAnnotationStore(requireContext());
        Map<Integer, List<VerseAdapter.WordAnn>> wordMap = new HashMap<>();
        for (com.fountainpdl.bible.utils.WordAnnotationStore.WordAnnotation wa : store.getForChapter(book, chapter)) {
            List<VerseAdapter.WordAnn> list = wordMap.computeIfAbsent(wa.verse, k -> new ArrayList<>());
            VerseAdapter.WordAnn a = new VerseAdapter.WordAnn();
            a.id = wa.id; a.text = wa.text; a.type = wa.type; a.color = wa.color;
            list.add(a);
        }
        adapter.setWordAnnotations(wordMap);
    }

    @Override
    public void onResume() {
        super.onResume();
        if (book != null) applyAnnotationsToAdapter();
    }
}
