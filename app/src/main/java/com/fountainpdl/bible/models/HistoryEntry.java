package com.fountainpdl.bible.models;

public class HistoryEntry {
    public String book;
    public int chapter;
    public long timestamp;

    public HistoryEntry() {}
    public HistoryEntry(String book, int chapter) {
        this.book = book;
        this.chapter = chapter;
        this.timestamp = System.currentTimeMillis();
    }
    public String getRef() { return book + " " + chapter; }
}
