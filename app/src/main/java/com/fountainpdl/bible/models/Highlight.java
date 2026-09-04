package com.fountainpdl.bible.models;

public class Highlight {
    public String key;
    public String book;
    public int chapter;
    public int verse;
    public String type;
    public String color;
    public long timestamp;

    public Highlight() {}

    public Highlight(String book, int chapter, int verse, String type, String color) {
        this.book = book;
        this.chapter = chapter;
        this.verse = verse;
        this.type = type;
        this.color = color;
        this.key = book + ":" + chapter + ":" + verse + ":" + type;
        this.timestamp = System.currentTimeMillis();
    }
}
