package com.fountainpdl.bible.fragments;

import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.fountainpdl.bible.MainActivity;
import com.fountainpdl.bible.R;
import com.fountainpdl.bible.adapters.MarathonAdapter;
import com.fountainpdl.bible.adapters.NoteAdapter;
import com.fountainpdl.bible.adapters.SermonAdapter;
import com.fountainpdl.bible.adapters.SimpleRowAdapter;
import com.fountainpdl.bible.models.Bookmark;
import com.fountainpdl.bible.models.HistoryEntry;
import com.fountainpdl.bible.models.Highlight;
import com.fountainpdl.bible.models.Marathon;
import com.fountainpdl.bible.models.Note;
import com.fountainpdl.bible.models.Sermon;
import com.fountainpdl.bible.models.SermonBlock;
import com.fountainpdl.bible.utils.AppTheme;
import com.fountainpdl.bible.utils.BooksData;
import com.fountainpdl.bible.utils.PrefsManager;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class LibraryFragment extends Fragment {

    private MainActivity activity;
    private LinearLayout listContainer, sermonDetailContainer, addButtonRow;
    private RecyclerView recycler;
    private TextView emptyState, btnAddItem;
    private TextView tabHistory, tabBookmarks, tabHighlights, tabNotes, tabSermons, tabMarathon;
    private TextView sermonDetailTitle, sermonDetailBack, sermonDetailEdit;
    private LinearLayout sermonDetailBlocks;

    private String currentTab = "bookmarks";
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("MMM d, yyyy · h:mm a", Locale.getDefault());

    private SimpleRowAdapter simpleAdapter;
    private NoteAdapter noteAdapter;
    private SermonAdapter sermonAdapter;
    private MarathonAdapter marathonAdapter;

    @Nullable @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_library, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View v, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(v, savedInstanceState);
        activity = (MainActivity) getActivity();
        if (activity == null) return;

        listContainer = v.findViewById(R.id.libraryListContainer);
        sermonDetailContainer = v.findViewById(R.id.sermonDetailContainer);
        recycler = v.findViewById(R.id.libraryRecycler);
        emptyState = v.findViewById(R.id.libraryEmptyState);
        addButtonRow = v.findViewById(R.id.addButtonRow);
        btnAddItem = v.findViewById(R.id.btnAddItem);

        tabHistory = v.findViewById(R.id.tabHistory);
        tabBookmarks = v.findViewById(R.id.tabBookmarks);
        tabHighlights = v.findViewById(R.id.tabHighlights);
        tabNotes = v.findViewById(R.id.tabNotes);
        tabSermons = v.findViewById(R.id.tabSermons);
        tabMarathon = v.findViewById(R.id.tabMarathon);

        sermonDetailTitle = v.findViewById(R.id.sermonDetailTitle);
        sermonDetailBack = v.findViewById(R.id.sermonDetailBack);
        sermonDetailEdit = v.findViewById(R.id.sermonDetailEdit);
        sermonDetailBlocks = v.findViewById(R.id.sermonDetailBlocks);

        recycler.setLayoutManager(new LinearLayoutManager(getContext()));

        simpleAdapter = new SimpleRowAdapter();
        noteAdapter = new NoteAdapter(new NoteAdapter.Listener() {
            @Override public void onEdit(Note note) { activity.openNoteDialog(null, note); }
            @Override public void onDelete(Note note) {
                PrefsManager prefs = activity.getPrefsManager();
                List<Note> notes = prefs.getNotes();
                notes.removeIf(n -> n.id.equals(note.id));
                prefs.saveNotes(notes);
                refreshNotes();
            }
            @Override public void onRefTap(String refs) { navigateToFirstRef(refs); }
        });
        sermonAdapter = new SermonAdapter(new SermonAdapter.Listener() {
            @Override public void onOpen(Sermon sermon) { openSermonDetail(sermon); }
            @Override public void onEdit(Sermon sermon) { activity.openSermonDialog(sermon); }
            @Override public void onDelete(Sermon sermon) {
                PrefsManager prefs = activity.getPrefsManager();
                List<Sermon> sermons = prefs.getSermons();
                sermons.removeIf(s -> s.id.equals(sermon.id));
                prefs.saveSermons(sermons);
                refreshSermons();
            }
        });
        marathonAdapter = new MarathonAdapter(new MarathonAdapter.Listener() {
            @Override public void onMarkRead(Marathon marathon) { markCurrentChapterRead(marathon); }
            @Override public void onDelete(Marathon marathon) {
                PrefsManager prefs = activity.getPrefsManager();
                List<Marathon> list = prefs.getMarathons();
                list.removeIf(m -> m.id.equals(marathon.id));
                prefs.saveMarathons(list);
                refreshMarathons();
            }
        });

        setupTabClicks();
        setupSermonDetailControls();

        showTab("bookmarks");
        onThemeChanged();
    }

    private void setupTabClicks() {
        tabHistory.setOnClickListener(x -> showTab("history"));
        tabBookmarks.setOnClickListener(x -> showTab("bookmarks"));
        tabHighlights.setOnClickListener(x -> showTab("highlights"));
        tabNotes.setOnClickListener(x -> showTab("notes"));
        tabSermons.setOnClickListener(x -> showTab("sermons"));
        tabMarathon.setOnClickListener(x -> showTab("marathon"));
    }

    private void setupSermonDetailControls() {
        sermonDetailBack.setOnClickListener(x -> closeSermonDetail());
    }

    private void showTab(String tab) {
        currentTab = tab;
        closeSermonDetail();
        styleTabs();

        addButtonRow.setVisibility(View.GONE);
        btnAddItem.setOnClickListener(null);

        switch (tab) {
            case "history": showHistory(); break;
            case "bookmarks": showBookmarks(); break;
            case "highlights": showHighlights(); break;
            case "notes":
                showNotes();
                addButtonRow.setVisibility(View.VISIBLE);
                btnAddItem.setText("+ New Note");
                btnAddItem.setOnClickListener(x -> activity.openNoteDialog(null, null));
                break;
            case "sermons":
                showSermons();
                addButtonRow.setVisibility(View.VISIBLE);
                btnAddItem.setText("+ New Sermon");
                btnAddItem.setOnClickListener(x -> activity.openSermonDialog(null));
                break;
            case "marathon":
                showMarathons();
                addButtonRow.setVisibility(View.VISIBLE);
                btnAddItem.setText("+ New Marathon");
                btnAddItem.setOnClickListener(x -> activity.openMarathonDialog());
                break;
        }
    }

    private void styleTabs() {
        AppTheme t = activity.getAppTheme();
        TextView[] tabs = {tabHistory, tabBookmarks, tabHighlights, tabNotes, tabSermons, tabMarathon};
        String[] ids = {"history", "bookmarks", "highlights", "notes", "sermons", "marathon"};
        for (int i = 0; i < tabs.length; i++) {
            boolean active = ids[i].equals(currentTab);
            GradientDrawable gd = new GradientDrawable();
            gd.setCornerRadius(dp(20));
            if (active) { gd.setColor(withAlpha(t.primary, 0x28)); gd.setStroke(dp(1), t.primary); tabs[i].setTextColor(t.primary); }
            else { gd.setColor(android.graphics.Color.TRANSPARENT); gd.setStroke(dp(1), withAlpha(t.sub, 0x55)); tabs[i].setTextColor(t.sub); }
            tabs[i].setBackground(gd);
        }
    }

    // ── History ───────────────────────────────────────────────────────────────
    private void showHistory() {
        PrefsManager prefs = activity.getPrefsManager();
        List<HistoryEntry> history = prefs.getHistory();
        List<SimpleRowAdapter.RowItem> items = new ArrayList<>();
        for (HistoryEntry h : history) {
            SimpleRowAdapter.RowItem item = new SimpleRowAdapter.RowItem();
            item.title = h.getRef();
            item.subtitle = dateFormat.format(new java.util.Date(h.timestamp));
            item.onClick = () -> activity.navigateToChapter(h.book, h.chapter, null);
            items.add(item);
        }
        simpleAdapter.setTheme(activity.getAppTheme());
        simpleAdapter.setItems(items);
        recycler.setAdapter(simpleAdapter);
        toggleEmpty(items.isEmpty(), getString(R.string.empty_history));
    }

    // ── Bookmarks ─────────────────────────────────────────────────────────────
    private void showBookmarks() {
        PrefsManager prefs = activity.getPrefsManager();
        List<Bookmark> bookmarks = prefs.getBookmarks();
        List<SimpleRowAdapter.RowItem> items = new ArrayList<>();
        for (Bookmark b : bookmarks) {
            SimpleRowAdapter.RowItem item = new SimpleRowAdapter.RowItem();
            item.title = b.label;
            item.subtitle = dateFormat.format(new java.util.Date(b.timestamp));
            item.actionLabel = "Remove";
            item.onClick = () -> activity.navigateToChapter(b.book, b.chapter, b.verse >= 0 ? b.verse : null);
            item.onAction = () -> {
                List<Bookmark> list = prefs.getBookmarks();
                list.removeIf(x -> x.key.equals(b.key));
                prefs.saveBookmarks(list);
                showBookmarks();
            };
            items.add(item);
        }
        simpleAdapter.setTheme(activity.getAppTheme());
        simpleAdapter.setItems(items);
        recycler.setAdapter(simpleAdapter);
        toggleEmpty(items.isEmpty(), getString(R.string.empty_bookmarks));
    }

    // ── Highlights ────────────────────────────────────────────────────────────
    private void showHighlights() {
        PrefsManager prefs = activity.getPrefsManager();
        List<Highlight> highlights = prefs.getHighlights();
        List<SimpleRowAdapter.RowItem> items = new ArrayList<>();
        for (Highlight h : highlights) {
            SimpleRowAdapter.RowItem item = new SimpleRowAdapter.RowItem();
            item.title = ("underline".equals(h.type) ? "_ " : "") + h.book + " " + h.chapter + ":" + h.verse;
            item.subtitle = h.type + " · " + dateFormat.format(new java.util.Date(h.timestamp));
            item.actionLabel = "Remove";
            item.onClick = () -> activity.navigateToChapter(h.book, h.chapter, h.verse);
            item.onAction = () -> {
                List<Highlight> list = prefs.getHighlights();
                list.removeIf(x -> x.key.equals(h.key));
                prefs.saveHighlights(list);
                showHighlights();
            };
            items.add(item);
        }
        simpleAdapter.setTheme(activity.getAppTheme());
        simpleAdapter.setItems(items);
        recycler.setAdapter(simpleAdapter);
        toggleEmpty(items.isEmpty(), getString(R.string.empty_highlights));
    }

    // ── Notes ─────────────────────────────────────────────────────────────────
    private void showNotes() {
        List<Note> notes = activity.getPrefsManager().getNotes();
        noteAdapter.setTheme(activity.getAppTheme());
        noteAdapter.setNotes(notes);
        recycler.setAdapter(noteAdapter);
        toggleEmpty(notes.isEmpty(), getString(R.string.empty_notes));
    }

    public void refreshNotes() { if ("notes".equals(currentTab)) showNotes(); }

    private void navigateToFirstRef(String refs) {
        if (refs == null || refs.trim().isEmpty()) return;
        String first = refs.split(";")[0].trim();
        java.util.regex.Matcher m = java.util.regex.Pattern.compile("^(.+?)\\s+(\\d+):(\\d+)").matcher(first);
        if (m.find()) {
            activity.navigateToChapter(m.group(1).trim(), Integer.parseInt(m.group(2)), Integer.parseInt(m.group(3)));
        }
    }

    // ── Sermons ───────────────────────────────────────────────────────────────
    private void showSermons() {
        List<Sermon> sermons = activity.getPrefsManager().getSermons();
        sermonAdapter.setTheme(activity.getAppTheme());
        sermonAdapter.setSermons(sermons);
        recycler.setAdapter(sermonAdapter);
        toggleEmpty(sermons.isEmpty(), getString(R.string.empty_sermons));
    }

    public void refreshSermons() { if ("sermons".equals(currentTab)) showSermons(); }

    private void openSermonDetail(Sermon sermon) {
        listContainer.setVisibility(View.GONE);
        sermonDetailContainer.setVisibility(View.VISIBLE);
        sermonDetailTitle.setText(sermon.title);
        sermonDetailEdit.setOnClickListener(x -> activity.openSermonDialog(sermon));

        sermonDetailBlocks.removeAllViews();
        String translation = activity.getPrefsManager().getSettings().translation;
        for (SermonBlock bl : sermon.blocks) {
            if ("verse".equals(bl.type)) {
                TextView refView = new TextView(getContext());
                refView.setText(bl.ref);
                refView.setTextColor(activity.getAppTheme().accent);
                refView.setTextSize(11);
                refView.setTypeface(null, android.graphics.Typeface.BOLD);
                refView.setOnClickListener(x -> navigateToFirstRef(bl.ref));
                sermonDetailBlocks.addView(refView, marginLp(0, 16, 0, 4));

                TextView textView = new TextView(getContext());
                textView.setText(activity.getBibleData().resolveRefText(bl.ref, translation));
                textView.setTextColor(activity.getAppTheme().text);
                textView.setTextSize(16);
                textView.setTypeface(null, android.graphics.Typeface.ITALIC);
                textView.setLineSpacing(0, 1.4f);
                android.widget.LinearLayout wrapper = new android.widget.LinearLayout(getContext());
                wrapper.setOrientation(android.widget.LinearLayout.VERTICAL);
                wrapper.setPadding(dp(14), dp(10), dp(14), dp(10));
                GradientDrawable wbg = new GradientDrawable();
                wbg.setColor(activity.getAppTheme().card);
                wbg.setCornerRadius(dp(4));
                wrapper.setBackground(wbg);
                wrapper.addView(textView);
                sermonDetailBlocks.addView(wrapper, marginLp(0, 0, 0, 0));
            } else {
                TextView textView = new TextView(getContext());
                textView.setText(bl.content);
                textView.setTextColor(activity.getAppTheme().text);
                textView.setTextSize(15);
                textView.setLineSpacing(0, 1.5f);
                sermonDetailBlocks.addView(textView, marginLp(0, 16, 0, 4));
            }
        }
    }

    private LinearLayout.LayoutParams marginLp(int l, int t, int r, int b) {
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        lp.setMargins(dp(l), dp(t), dp(r), dp(b));
        return lp;
    }

    private void closeSermonDetail() {
        sermonDetailContainer.setVisibility(View.GONE);
        listContainer.setVisibility(View.VISIBLE);
    }

    // ── Marathon ──────────────────────────────────────────────────────────────
    private void showMarathons() {
        List<Marathon> marathons = activity.getPrefsManager().getMarathons();
        marathonAdapter.setTheme(activity.getAppTheme());
        marathonAdapter.setMarathons(marathons);
        recycler.setAdapter(marathonAdapter);
        toggleEmpty(marathons.isEmpty(), getString(R.string.empty_marathons));
    }

    public void refreshMarathons() { if ("marathon".equals(currentTab)) showMarathons(); }

    private void markCurrentChapterRead(Marathon marathon) {
        PrefsManager prefs = activity.getPrefsManager();
        List<Marathon> list = prefs.getMarathons();
        String book = activity.getCurrentBook();
        int chapter = activity.getCurrentChapter();
        for (Marathon m : list) {
            if (m.id.equals(marathon.id)) {
                List<Integer> completed = m.completedChapters.get(book);
                if (completed == null) { completed = new ArrayList<>(); m.completedChapters.put(book, completed); }
                if (!completed.contains(chapter)) completed.add(chapter);
                m.lastRead = System.currentTimeMillis();
                break;
            }
        }
        prefs.saveMarathons(list);
        showMarathons();
    }

    // ── Theme / translation hooks ─────────────────────────────────────────────
    public void onThemeChanged() {
        if (activity == null || getView() == null) return;
        getView().setBackgroundColor(activity.getAppTheme().surface);
        styleTabs();
        emptyState.setTextColor(activity.getAppTheme().sub);
        sermonDetailTitle.setTextColor(activity.getAppTheme().primary);
        showTab(currentTab);
    }

    public void onTranslationChanged() {
        if ("sermons".equals(currentTab)) showSermons();
    }

    private void toggleEmpty(boolean empty, String message) {
        if (empty) {
            recycler.setVisibility(View.GONE);
            emptyState.setVisibility(View.VISIBLE);
            emptyState.setText(message);
        } else {
            recycler.setVisibility(View.VISIBLE);
            emptyState.setVisibility(View.GONE);
        }
    }

    private int withAlpha(int color, int alpha) {
        return android.graphics.Color.argb(alpha, android.graphics.Color.red(color), android.graphics.Color.green(color), android.graphics.Color.blue(color));
    }

    private int dp(int v) { return (int) (v * getResources().getDisplayMetrics().density); }

    @Override
    public void onResume() {
        super.onResume();
        if (activity != null) showTab(currentTab);
    }
}
