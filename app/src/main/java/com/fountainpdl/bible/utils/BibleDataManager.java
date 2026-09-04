package com.fountainpdl.bible.utils;

import android.content.Context;
import android.content.res.AssetManager;

import com.fountainpdl.bible.models.SearchResult;
import com.fountainpdl.bible.models.Verse;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Singleton that owns all Bible text data. Loads the four JSON shards
 * (kjv-ot, kjv-nt, niv-ot, niv-nt) from assets on a background thread at
 * startup so the whole Bible is in memory before the user needs it --
 * this is what makes chapter switches and search instantaneous with no
 * per-request I/O.
 */
public class BibleDataManager {

    private static BibleDataManager instance;

    // translation -> "Book:Chapter" -> verseNum -> text
    private final Map<String, Map<String, Map<String, String>>> data = new ConcurrentHashMap<>();
    private volatile boolean loaded = false;
    private final ExecutorService executor = Executors.newSingleThreadExecutor();

    public interface LoadCallback { void onLoaded(); }

    private BibleDataManager() {}

    public static synchronized BibleDataManager getInstance() {
        if (instance == null) instance = new BibleDataManager();
        return instance;
    }

    public boolean isLoaded() { return loaded; }

    public void loadAsync(Context context, LoadCallback callback) {
        if (loaded) { if (callback != null) callback.onLoaded(); return; }
        Context appCtx = context.getApplicationContext();
        executor.execute(() -> {
            loadTranslation(appCtx, "KJV", new String[]{"bible/kjv-ot.json", "bible/kjv-nt.json"});
            loadTranslation(appCtx, "NIV", new String[]{"bible/niv-ot.json", "bible/niv-nt.json"});
            loaded = true;
            if (callback != null) {
                android.os.Handler main = new android.os.Handler(android.os.Looper.getMainLooper());
                main.post(callback::onLoaded);
            }
        });
    }

    private void loadTranslation(Context context, String translation, String[] assetPaths) {
        Map<String, Map<String, String>> merged = new LinkedHashMap<>();
        Gson gson = new Gson();
        Type type = new TypeToken<Map<String, Map<String, String>>>(){}.getType();
        AssetManager am = context.getAssets();
        for (String path : assetPaths) {
            try (InputStream is = am.open(path);
                 InputStreamReader reader = new InputStreamReader(is, StandardCharsets.UTF_8)) {
                Map<String, Map<String, String>> chunk = gson.fromJson(reader, type);
                if (chunk != null) merged.putAll(chunk);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        data.put(translation, merged);
    }

    /** Returns verseNum(String) -&gt; text for a chapter, or empty map if not found. */
    public Map<String, String> getChapter(String book, int chapter, String translation) {
        Map<String, Map<String, String>> t = data.get(translation);
        if (t == null) return new LinkedHashMap<>();
        Map<String, String> chap = t.get(book + ":" + chapter);
        if (chap == null) return new LinkedHashMap<>();
        TreeMap<Integer, String> sorted = new TreeMap<>();
        for (Map.Entry<String, String> e : chap.entrySet()) {
            try { sorted.put(Integer.parseInt(e.getKey()), e.getValue()); } catch (NumberFormatException ignored) {}
        }
        Map<String, String> out = new LinkedHashMap<>();
        for (Map.Entry<Integer, String> e : sorted.entrySet()) out.put(String.valueOf(e.getKey()), e.getValue());
        return out;
    }

    public List<Verse> getChapterAsVerseList(String book, int chapter, String translation) {
        Map<String, String> chap = getChapter(book, chapter, translation);
        List<Verse> list = new ArrayList<>();
        for (Map.Entry<String, String> e : chap.entrySet()) {
            try {
                int vn = Integer.parseInt(e.getKey());
                list.add(new Verse(book, chapter, vn, e.getValue()));
            } catch (NumberFormatException ignored) {}
        }
        list.sort((a, b) -> Integer.compare(a.verseNum, b.verseNum));
        return list;
    }

    public String getVerseText(String book, int chapter, int verse, String translation) {
        Map<String, String> chap = getChapter(book, chapter, translation);
        String t = chap.get(String.valueOf(verse));
        return t != null ? t : "";
    }

    private static final Pattern REF_PATTERN = Pattern.compile("^(.+?)\\s+(\\d+):(\\d+)(?:[-\u2013](\\d+))?$");

    /** Resolve a reference like "John 3:16" or "Romans 8:28-39" into display text. */
    public String resolveRefText(String ref, String translation) {
        if (ref == null) return "";
        Matcher m = REF_PATTERN.matcher(ref.trim());
        if (!m.matches()) return "[Invalid reference: " + ref + "]";
        String book = m.group(1).trim();
        int chapter = Integer.parseInt(m.group(2));
        int start = Integer.parseInt(m.group(3));
        int end = m.group(4) != null ? Integer.parseInt(m.group(4)) : start;
        Map<String, String> chap = getChapter(book, chapter, translation);
        StringBuilder sb = new StringBuilder();
        for (int i = start; i <= end; i++) {
            String v = chap.get(String.valueOf(i));
            if (v != null) {
                if (sb.length() > 0) sb.append(' ');
                sb.append(i).append(' ').append(v);
            }
        }
        return sb.length() > 0 ? sb.toString() : "[No text found for " + ref + "]";
    }

    /** Full text search across the current translation. Case-insensitive substring match. */
    public List<SearchResult> search(String query, String translation, int maxResults) {
        List<SearchResult> results = new ArrayList<>();
        if (query == null || query.trim().length() < 2) return results;
        String q = query.trim().toLowerCase();
        Map<String, Map<String, String>> t = data.get(translation);
        if (t == null) return results;

        for (Map.Entry<String, Map<String, String>> chapEntry : t.entrySet()) {
            String[] parts = chapEntry.getKey().split(":");
            if (parts.length != 2) continue;
            String book = parts[0];
            int chapter;
            try { chapter = Integer.parseInt(parts[1]); } catch (NumberFormatException e) { continue; }

            for (Map.Entry<String, String> verseEntry : chapEntry.getValue().entrySet()) {
                String text = verseEntry.getValue();
                String lower = text.toLowerCase();
                int idx = lower.indexOf(q);
                if (idx >= 0) {
                    SearchResult r = new SearchResult();
                    r.book = book;
                    r.chapter = chapter;
                    try { r.verse = Integer.parseInt(verseEntry.getKey()); } catch (NumberFormatException e) { continue; }
                    r.text = text;
                    r.matchStart = idx;
                    r.matchLen = q.length();
                    results.add(r);
                    if (results.size() >= maxResults) return results;
                }
            }
        }
        return results;
    }
}
