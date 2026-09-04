package com.fountainpdl.bible.models;

public class Note {
    public String id;
    public String topic;
    public String text;
    public String refs;
    public long timestamp;

    public Note() {
        this.id = String.valueOf(System.currentTimeMillis());
        this.timestamp = System.currentTimeMillis();
    }
}
