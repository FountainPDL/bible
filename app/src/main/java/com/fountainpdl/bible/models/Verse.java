package com.fountainpdl.bible.models;

public class Verse {
    public final String book;
    public final int chapter;
    public final int verseNum;
    public final String text;

    public Verse(String book, int chapter, int verseNum, String text) {
        this.book = book;
        this.chapter = chapter;
        this.verseNum = verseNum;
        this.text = text;
    }

    public String getKey() { return book + ":" + chapter + ":" + verseNum; }
    public String getRef() { return book + " " + chapter + ":" + verseNum; }
}
