package com.fountainpdl.bible.models;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Marathon {
    public String id;
    public String name;
    public String plan;
    public List<String> books = new ArrayList<>();
    public Map<String, List<Integer>> completedChapters = new HashMap<>();
    public long startDate;
    public long lastRead;

    public Marathon() {
        this.id = String.valueOf(System.currentTimeMillis());
        this.startDate = System.currentTimeMillis();
    }
}
