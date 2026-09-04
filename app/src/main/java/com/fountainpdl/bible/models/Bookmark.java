package com.fountainpdl.bible.models;

public class Bookmark {
    public String key;
    public String book;
    public int chapter;
    public int verse;
    public String label;
    public long timestamp;

    public Bookmark() {}

    public Bookmark(String book, int chapter, int verse) {
        this.book = book;
        this.chapter = chapter;
        this.verse = verse;
        this.key = verse < 0 ? book + ":" + chapter + ":ALL" : book + ":" + chapter + ":" + verse;
        this.label = verse < 0 ? book + " Chapter " + chapter : book + " " + chapter + ":" + verse;
        this.timestamp = System.currentTimeMillis();
    }
}
