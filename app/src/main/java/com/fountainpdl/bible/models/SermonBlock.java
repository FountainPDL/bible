package com.fountainpdl.bible.models;

public class SermonBlock {
    public String id;
    public String type;
    public String ref;
    public String content;

    public SermonBlock() { this.id = String.valueOf(System.nanoTime()); }

    public static SermonBlock verse(String ref) {
        SermonBlock b = new SermonBlock();
        b.type = "verse";
        b.ref = ref;
        return b;
    }

    public static SermonBlock text(String content) {
        SermonBlock b = new SermonBlock();
        b.type = "text";
        b.content = content;
        return b;
    }
}
