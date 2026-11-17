package com.fountainpdl.bible.utils;

import android.content.Context;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

/**
 * Words of Christ, at phrase level.
 *
 * assets/bible/redletter-niv.json holds the exact character ranges of his words,
 * taken from the NIV 2011 words-of-Jesus markup. assets/bible/redletter-kjv.json
 * holds the matching ranges in the KJV text, derived from the NIV structure and
 * verified against the KJV wording (verses where no confident match was possible
 * fall back to coloring the whole verse). Ranges are [start, end) offsets into the
 * verse text exactly as stored in the Bible assets.
 */
public class JesusWords {

    private static final Map<String, Map<String, Map<String, int[][]>>> DATA = new HashMap<>();

    /** Called from the background loader together with the Bible text. */
    public static synchronized void load(Context context) {
        loadOne(context, "KJV", "bible/redletter-kjv.json");
        loadOne(context, "NIV", "bible/redletter-niv.json");
    }

    private static void loadOne(Context context, String translation, String path) {
        Type type = new TypeToken<Map<String, Map<String, int[][]>>>(){}.getType();
        try (InputStream is = context.getAssets().open(path);
             InputStreamReader r = new InputStreamReader(is, StandardCharsets.UTF_8)) {
            Map<String, Map<String, int[][]>> m = new Gson().fromJson(r, type);
            if (m != null) DATA.put(translation, m);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /** Character ranges of Jesus' words in this verse, or null if the verse has none. */
    public static synchronized int[][] ranges(String translation, String book, int chapter, int verse) {
        Map<String, Map<String, int[][]>> t = DATA.get(translation);
        if (t == null) return null;
        Map<String, int[][]> c = t.get(book + ":" + chapter);
        return c == null ? null : c.get(String.valueOf(verse));
    }

    public static boolean isRedLetter(String translation, String book, int chapter, int verse) {
        return ranges(translation, book, chapter, verse) != null;
    }
}
