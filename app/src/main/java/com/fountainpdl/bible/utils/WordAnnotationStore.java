package com.fountainpdl.bible.utils;

import android.content.Context;
import android.content.SharedPreferences;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

/**
 * Word/phrase level annotations -- e.g. highlighting or underlining just
 * "born again" inside a verse, or attaching a note to a specific phrase,
 * rather than the whole verse. Stored separately from full-verse
 * Highlight objects so the two systems don't collide.
 */
public class WordAnnotationStore {

    public static class WordAnnotation {
        public String id;
        public String book;
        public int chapter;
        public int verse;
        public String text;   // the phrase itself
        public String type;   // "highlight" | "underline" | "note"
        public String color;  // hex, for highlight type
        public String note;   // note body, for note type
        public long timestamp;

        public WordAnnotation() {
            this.id = String.valueOf(System.nanoTime());
            this.timestamp = System.currentTimeMillis();
        }
    }

    private static final String PREFS_NAME = "fountainpdl_word_annotations";
    private static final String KEY_LIST = "annotations";

    private final SharedPreferences prefs;
    private final Gson gson = new Gson();

    public WordAnnotationStore(Context context) {
        prefs = context.getApplicationContext().getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
    }

    public List<WordAnnotation> getAll() {
        String json = prefs.getString(KEY_LIST, null);
        if (json == null) return new ArrayList<>();
        Type type = new TypeToken<List<WordAnnotation>>(){}.getType();
        try {
            List<WordAnnotation> list = gson.fromJson(json, type);
            return list != null ? list : new ArrayList<>();
        } catch (Exception e) { return new ArrayList<>(); }
    }

    public List<WordAnnotation> getForChapter(String book, int chapter) {
        List<WordAnnotation> out = new ArrayList<>();
        for (WordAnnotation a : getAll()) if (a.book.equals(book) && a.chapter == chapter) out.add(a);
        return out;
    }

    public void add(WordAnnotation ann) {
        List<WordAnnotation> list = getAll();
        list.add(ann);
        save(list);
    }

    public void remove(String id) {
        List<WordAnnotation> list = getAll();
        list.removeIf(a -> a.id.equals(id));
        save(list);
    }

    public void clear() { save(new ArrayList<>()); }

    private void save(List<WordAnnotation> list) {
        prefs.edit().putString(KEY_LIST, gson.toJson(list)).apply();
    }
}
