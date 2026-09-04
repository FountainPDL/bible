package com.fountainpdl.bible.models;

public class SearchResult {
    public String book;
    public int chapter;
    public int verse;
    public String text;
    public int matchStart;
    public int matchLen;

    public String getRef() { return book + " " + chapter + ":" + verse; }
}
