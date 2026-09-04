package com.fountainpdl.bible.utils;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class ReadingPlans {

    public static final Map<String, List<String>> PLANS = new LinkedHashMap<>();

    static {
        List<String> fullBible = new ArrayList<>();
        fullBible.addAll(BooksData.OT_BOOKS);
        fullBible.addAll(BooksData.NT_BOOKS);
        PLANS.put("Full Bible (OT then NT)", fullBible);

        List<String> ntFirst = new ArrayList<>();
        ntFirst.addAll(BooksData.NT_BOOKS);
        ntFirst.addAll(BooksData.OT_BOOKS);
        PLANS.put("New Testament First", ntFirst);

        List<String> chronological = new ArrayList<>();
        String[] chrono = {
            "Genesis","Job","Exodus","Leviticus","Numbers","Deuteronomy",
            "Joshua","Judges","Ruth","1 Samuel","2 Samuel","Psalms","1 Kings","2 Kings",
            "Proverbs","Ecclesiastes","Song of Solomon","Isaiah","Jeremiah","Lamentations",
            "Ezekiel","Daniel","Hosea","Joel","Amos","Obadiah","Jonah","Micah","Nahum",
            "Habakkuk","Zephaniah","Haggai","Zechariah","Malachi",
            "Matthew","Mark","Luke","John","Acts","Romans","1 Corinthians","2 Corinthians",
            "Galatians","Ephesians","Philippians","Colossians","1 Thessalonians","2 Thessalonians",
            "1 Timothy","2 Timothy","Titus","Philemon","Hebrews","James","1 Peter","2 Peter",
            "1 John","2 John","3 John","Jude","Revelation"
        };
        for (String b : chrono) chronological.add(b);
        PLANS.put("Chronological", chronological);

        List<String> gospels = new ArrayList<>();
        gospels.add("Matthew"); gospels.add("Mark"); gospels.add("Luke"); gospels.add("John");
        PLANS.put("Gospels Only", gospels);

        List<String> pauline = new ArrayList<>();
        String[] paulineArr = {"Romans","1 Corinthians","2 Corinthians","Galatians","Ephesians",
            "Philippians","Colossians","1 Thessalonians","2 Thessalonians",
            "1 Timothy","2 Timothy","Titus","Philemon"};
        for (String b : paulineArr) pauline.add(b);
        PLANS.put("Pauline Epistles", pauline);

        List<String> wisdom = new ArrayList<>();
        wisdom.add("Psalms"); wisdom.add("Proverbs"); wisdom.add("Ecclesiastes"); wisdom.add("Job");
        PLANS.put("Wisdom Literature", wisdom);

        PLANS.put("New Testament", new ArrayList<>(BooksData.NT_BOOKS));
        PLANS.put("Old Testament", new ArrayList<>(BooksData.OT_BOOKS));
    }

    public static List<String> getPlanNames() {
        return new ArrayList<>(PLANS.keySet());
    }

    public static List<String> getBooksForPlan(String planName) {
        List<String> books = PLANS.get(planName);
        if (books != null) return books;
        List<String> fallback = new ArrayList<>();
        fallback.addAll(BooksData.OT_BOOKS);
        fallback.addAll(BooksData.NT_BOOKS);
        return fallback;
    }
}
