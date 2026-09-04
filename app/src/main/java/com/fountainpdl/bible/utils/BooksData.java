package com.fountainpdl.bible.utils;

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.HashSet;
import java.util.Set;

public class BooksData {

    public static final List<String> OT_BOOKS = Arrays.asList(
        "Genesis", "Exodus", "Leviticus", "Numbers", "Deuteronomy", "Joshua", "Judges", "Ruth", "1 Samuel", "2 Samuel", "1 Kings", "2 Kings", "1 Chronicles", "2 Chronicles", "Ezra", "Nehemiah", "Esther", "Job", "Psalms", "Proverbs", "Ecclesiastes", "Song of Solomon", "Isaiah", "Jeremiah", "Lamentations", "Ezekiel", "Daniel", "Hosea", "Joel", "Amos", "Obadiah", "Jonah", "Micah", "Nahum", "Habakkuk", "Zephaniah", "Haggai", "Zechariah", "Malachi"
    );

    public static final List<String> NT_BOOKS = Arrays.asList(
        "Matthew", "Mark", "Luke", "John", "Acts", "Romans", "1 Corinthians", "2 Corinthians", "Galatians", "Ephesians", "Philippians", "Colossians", "1 Thessalonians", "2 Thessalonians", "1 Timothy", "2 Timothy", "Titus", "Philemon", "Hebrews", "James", "1 Peter", "2 Peter", "1 John", "2 John", "3 John", "Jude", "Revelation"
    );

    public static final Set<String> NT_SET = new HashSet<>(NT_BOOKS);

    public static final List<String> ALL_BOOKS = new java.util.ArrayList<>();
    static { ALL_BOOKS.addAll(OT_BOOKS); ALL_BOOKS.addAll(NT_BOOKS); }

    public static final Map<String, Integer> CHAPTER_COUNTS = new LinkedHashMap<>();
    static {
        CHAPTER_COUNTS.put("Genesis", 50);
        CHAPTER_COUNTS.put("Exodus", 40);
        CHAPTER_COUNTS.put("Leviticus", 27);
        CHAPTER_COUNTS.put("Numbers", 36);
        CHAPTER_COUNTS.put("Deuteronomy", 34);
        CHAPTER_COUNTS.put("Joshua", 24);
        CHAPTER_COUNTS.put("Judges", 21);
        CHAPTER_COUNTS.put("Ruth", 4);
        CHAPTER_COUNTS.put("1 Samuel", 31);
        CHAPTER_COUNTS.put("2 Samuel", 24);
        CHAPTER_COUNTS.put("1 Kings", 22);
        CHAPTER_COUNTS.put("2 Kings", 25);
        CHAPTER_COUNTS.put("1 Chronicles", 29);
        CHAPTER_COUNTS.put("2 Chronicles", 36);
        CHAPTER_COUNTS.put("Ezra", 10);
        CHAPTER_COUNTS.put("Nehemiah", 13);
        CHAPTER_COUNTS.put("Esther", 10);
        CHAPTER_COUNTS.put("Job", 42);
        CHAPTER_COUNTS.put("Psalms", 150);
        CHAPTER_COUNTS.put("Proverbs", 31);
        CHAPTER_COUNTS.put("Ecclesiastes", 12);
        CHAPTER_COUNTS.put("Song of Solomon", 8);
        CHAPTER_COUNTS.put("Isaiah", 66);
        CHAPTER_COUNTS.put("Jeremiah", 52);
        CHAPTER_COUNTS.put("Lamentations", 5);
        CHAPTER_COUNTS.put("Ezekiel", 48);
        CHAPTER_COUNTS.put("Daniel", 12);
        CHAPTER_COUNTS.put("Hosea", 14);
        CHAPTER_COUNTS.put("Joel", 3);
        CHAPTER_COUNTS.put("Amos", 9);
        CHAPTER_COUNTS.put("Obadiah", 1);
        CHAPTER_COUNTS.put("Jonah", 4);
        CHAPTER_COUNTS.put("Micah", 7);
        CHAPTER_COUNTS.put("Nahum", 3);
        CHAPTER_COUNTS.put("Habakkuk", 3);
        CHAPTER_COUNTS.put("Zephaniah", 3);
        CHAPTER_COUNTS.put("Haggai", 2);
        CHAPTER_COUNTS.put("Zechariah", 14);
        CHAPTER_COUNTS.put("Malachi", 4);
        CHAPTER_COUNTS.put("Matthew", 28);
        CHAPTER_COUNTS.put("Mark", 16);
        CHAPTER_COUNTS.put("Luke", 24);
        CHAPTER_COUNTS.put("John", 21);
        CHAPTER_COUNTS.put("Acts", 28);
        CHAPTER_COUNTS.put("Romans", 16);
        CHAPTER_COUNTS.put("1 Corinthians", 16);
        CHAPTER_COUNTS.put("2 Corinthians", 13);
        CHAPTER_COUNTS.put("Galatians", 6);
        CHAPTER_COUNTS.put("Ephesians", 6);
        CHAPTER_COUNTS.put("Philippians", 4);
        CHAPTER_COUNTS.put("Colossians", 4);
        CHAPTER_COUNTS.put("1 Thessalonians", 5);
        CHAPTER_COUNTS.put("2 Thessalonians", 3);
        CHAPTER_COUNTS.put("1 Timothy", 6);
        CHAPTER_COUNTS.put("2 Timothy", 4);
        CHAPTER_COUNTS.put("Titus", 3);
        CHAPTER_COUNTS.put("Philemon", 1);
        CHAPTER_COUNTS.put("Hebrews", 13);
        CHAPTER_COUNTS.put("James", 5);
        CHAPTER_COUNTS.put("1 Peter", 5);
        CHAPTER_COUNTS.put("2 Peter", 3);
        CHAPTER_COUNTS.put("1 John", 5);
        CHAPTER_COUNTS.put("2 John", 1);
        CHAPTER_COUNTS.put("3 John", 1);
        CHAPTER_COUNTS.put("Jude", 1);
        CHAPTER_COUNTS.put("Revelation", 22);
    }

    public static int getChapterCount(String book) {
        Integer c = CHAPTER_COUNTS.get(book);
        return c != null ? c : 1;
    }

    public static boolean isNewTestament(String book) {
        return NT_SET.contains(book);
    }
}