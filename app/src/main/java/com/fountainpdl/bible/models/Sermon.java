package com.fountainpdl.bible.models;

import java.util.ArrayList;
import java.util.List;

public class Sermon {
    public String id;
    public String title;
    public List<SermonBlock> blocks = new ArrayList<>();
    public long timestamp;

    public Sermon() {
        this.id = String.valueOf(System.currentTimeMillis());
        this.timestamp = System.currentTimeMillis();
    }
}
