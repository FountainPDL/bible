package com.fountainpdl.bible.utils;

import android.content.Context;
import android.content.SharedPreferences;

import com.fountainpdl.bible.models.AppSettings;
import com.fountainpdl.bible.models.Bookmark;
import com.fountainpdl.bible.models.HistoryEntry;
import com.fountainpdl.bible.models.Highlight;
import com.fountainpdl.bible.models.Marathon;
import com.fountainpdl.bible.models.Note;
import com.fountainpdl.bible.models.Sermon;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

/**
 * All persistent app state lives here: user settings, current reading
 * position, and every user-generated collection (bookmarks, highlights,
 * notes, sermons, history, marathons). Backed by SharedPreferences with
 * Gson serialization -- simple, synchronous, and reliable for data of
 * this size.
 */
public class PrefsManager {

    private static final String PREFS_NAME = "fountainpdl_bible_prefs";
    private static final String KEY_SETTINGS = "settings";
    private static final String KEY_BOOK = "current_book";
    private static final String KEY_CHAPTER = "current_chapter";
    private static final String KEY_BOOKMARKS = "bookmarks";
    private static final String KEY_HIGHLIGHTS = "highlights";
    private static final String KEY_NOTES = "notes";
    private static final String KEY_SERMONS = "sermons";
    private static final String KEY_HISTORY = "history";
    private static final String KEY_MARATHONS = "marathons";
    private static final String KEY_SCROLL_PREFIX = "scroll_";

    private final SharedPreferences prefs;
    private final Gson gson = new Gson();

    public PrefsManager(Context context) {
        prefs = context.getApplicationContext().getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
    }

    // ── Settings ──────────────────────────────────────────────────────────────
    public AppSettings getSettings() {
        String json = prefs.getString(KEY_SETTINGS, null);
        if (json == null) return new AppSettings();
        try {
            AppSettings s = gson.fromJson(json, AppSettings.class);
            return s != null ? s : new AppSettings();
        } catch (Exception e) { return new AppSettings(); }
    }

    public void saveSettings(AppSettings settings) {
        prefs.edit().putString(KEY_SETTINGS, gson.toJson(settings)).apply();
    }

    // ── Current position ──────────────────────────────────────────────────────
    public String getCurrentBook() { return prefs.getString(KEY_BOOK, "John"); }
    public int getCurrentChapter() { return prefs.getInt(KEY_CHAPTER, 3); }
    public void setCurrentPosition(String book, int chapter) {
        prefs.edit().putString(KEY_BOOK, book).putInt(KEY_CHAPTER, chapter).apply();
    }

    // ── Per-chapter scroll memory ────────────────────────────────────────────
    public void saveScrollPosition(String book, int chapter, int scrollY) {
        prefs.edit().putInt(KEY_SCROLL_PREFIX + book + "_" + chapter, scrollY).apply();
    }
    public int getScrollPosition(String book, int chapter) {
        return prefs.getInt(KEY_SCROLL_PREFIX + book + "_" + chapter, 0);
    }

    // ── Generic list helpers ─────────────────────────────────────────────────
    private <T> List<T> loadList(String key, Type type) {
        String json = prefs.getString(key, null);
        if (json == null) return new ArrayList<>();
        try {
            List<T> list = gson.fromJson(json, type);
            return list != null ? list : new ArrayList<>();
        } catch (Exception e) { return new ArrayList<>(); }
    }
    private void saveList(String key, Object list) {
        prefs.edit().putString(key, gson.toJson(list)).apply();
    }

    // ── Bookmarks ─────────────────────────────────────────────────────────────
    public List<Bookmark> getBookmarks() {
        return loadList(KEY_BOOKMARKS, new TypeToken<List<Bookmark>>(){}.getType());
    }
    public void saveBookmarks(List<Bookmark> list) { saveList(KEY_BOOKMARKS, list); }

    // ── Highlights ────────────────────────────────────────────────────────────
    public List<Highlight> getHighlights() {
        return loadList(KEY_HIGHLIGHTS, new TypeToken<List<Highlight>>(){}.getType());
    }
    public void saveHighlights(List<Highlight> list) { saveList(KEY_HIGHLIGHTS, list); }

    // ── Notes ─────────────────────────────────────────────────────────────────
    public List<Note> getNotes() {
        return loadList(KEY_NOTES, new TypeToken<List<Note>>(){}.getType());
    }
    public void saveNotes(List<Note> list) { saveList(KEY_NOTES, list); }

    // ── Sermons ───────────────────────────────────────────────────────────────
    public List<Sermon> getSermons() {
        return loadList(KEY_SERMONS, new TypeToken<List<Sermon>>(){}.getType());
    }
    public void saveSermons(List<Sermon> list) { saveList(KEY_SERMONS, list); }

    // ── History ───────────────────────────────────────────────────────────────
    public List<HistoryEntry> getHistory() {
        return loadList(KEY_HISTORY, new TypeToken<List<HistoryEntry>>(){}.getType());
    }
    public void saveHistory(List<HistoryEntry> list) { saveList(KEY_HISTORY, list); }
    public void addHistoryEntry(String book, int chapter) {
        List<HistoryEntry> list = getHistory();
        String ref = book + " " + chapter;
        list.removeIf(h -> h.getRef().equals(ref));
        list.add(0, new HistoryEntry(book, chapter));
        if (list.size() > 300) list = new ArrayList<>(list.subList(0, 300));
        saveHistory(list);
    }

    // ── Marathons ─────────────────────────────────────────────────────────────
    public List<Marathon> getMarathons() {
        return loadList(KEY_MARATHONS, new TypeToken<List<Marathon>>(){}.getType());
    }
    public void saveMarathons(List<Marathon> list) { saveList(KEY_MARATHONS, list); }

    // ── Clear operations ──────────────────────────────────────────────────────
    public void clearBookmarks() { saveBookmarks(new ArrayList<>()); }
    public void clearHighlights() { saveHighlights(new ArrayList<>()); }
    public void clearNotes() { saveNotes(new ArrayList<>()); }
    public void clearSermons() { saveSermons(new ArrayList<>()); }
    public void clearHistory() { saveHistory(new ArrayList<>()); }
    public void clearMarathons() { saveMarathons(new ArrayList<>()); }
}
