package com.fountainpdl.bible.utils;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.Arrays;

/**
 * Verses where the words of Christ make up a substantial part of the verse
 * (30% or more of its text), generated from the NIV 2011 words-of-Jesus markup.
 * Covers the Gospels plus Acts, 1-2 Corinthians and Revelation. The Read screen
 * colors whole verses, so verses that only quote a few words of his are left plain.
 */
public class JesusWords {

    private static final Map<String, Set<Integer>> DATA = new HashMap<>();
    static {
        DATA.put("Matthew:3", new HashSet<>(Arrays.asList(15)));
        DATA.put("Matthew:4", new HashSet<>(Arrays.asList(4,7,10,17,19)));
        DATA.put("Matthew:5", new HashSet<>(Arrays.asList(3,4,5,6,7,8,9,10,11,12,13,14,15,16,17,18,19,20,21,22,23,24,25,26,27,28,29,30,31,32,33,34,35,36,37,38,39,40,41,42,43,44,45,46,47,48)));
        DATA.put("Matthew:6", new HashSet<>(Arrays.asList(1,2,3,4,5,6,7,8,9,10,11,12,13,14,15,16,17,18,19,20,21,22,23,24,25,26,27,28,29,30,31,32,33,34)));
        DATA.put("Matthew:7", new HashSet<>(Arrays.asList(1,2,3,4,5,6,7,8,9,10,11,12,13,14,15,16,17,18,19,20,21,22,23,24,25,26,27)));
        DATA.put("Matthew:8", new HashSet<>(Arrays.asList(4,7,10,11,12,13,20,22,26)));
        DATA.put("Matthew:9", new HashSet<>(Arrays.asList(2,4,5,6,12,13,15,16,17,22,24,29,30,37,38)));
        DATA.put("Matthew:10", new HashSet<>(Arrays.asList(5,6,7,8,9,10,11,12,13,14,15,16,17,18,19,20,21,22,23,24,25,26,27,28,29,30,31,32,33,34,35,36,37,38,39,40,41,42)));
        DATA.put("Matthew:11", new HashSet<>(Arrays.asList(4,5,6,7,8,9,10,11,12,13,14,15,16,17,18,19,21,22,23,24,25,26,27,28,29,30)));
        DATA.put("Matthew:12", new HashSet<>(Arrays.asList(3,4,5,6,7,8,11,12,25,26,27,28,29,30,31,32,33,34,35,36,37,39,40,41,42,43,44,45,48,49,50)));
        DATA.put("Matthew:13", new HashSet<>(Arrays.asList(3,4,5,6,7,8,9,11,12,13,14,15,16,17,18,19,20,21,22,23,24,25,26,27,28,29,30,31,32,33,37,38,39,40,41,42,43,44,45,46,47,48,49,50,51,52,57)));
        DATA.put("Matthew:14", new HashSet<>(Arrays.asList(16,18,27,31)));
        DATA.put("Matthew:15", new HashSet<>(Arrays.asList(3,4,5,6,7,8,9,10,11,13,14,16,17,18,19,20,24,26,28,32,34)));
        DATA.put("Matthew:16", new HashSet<>(Arrays.asList(2,3,4,6,8,9,10,11,13,15,17,18,19,23,24,25,26,27,28)));
        DATA.put("Matthew:17", new HashSet<>(Arrays.asList(7,9,11,12,17,20,22,23,25,26,27)));
        DATA.put("Matthew:18", new HashSet<>(Arrays.asList(3,4,5,6,7,8,9,10,12,13,14,15,16,17,18,19,20,22,23,24,25,26,27,28,29,30,31,32,33,34,35)));
        DATA.put("Matthew:19", new HashSet<>(Arrays.asList(4,5,6,8,9,11,12,14,17,18,19,21,23,24,26,28,29,30)));
        DATA.put("Matthew:20", new HashSet<>(Arrays.asList(1,2,3,4,5,6,7,8,9,10,11,12,13,14,15,16,18,19,22,23,25,26,27,28,32)));
        DATA.put("Matthew:21", new HashSet<>(Arrays.asList(2,3,13,16,21,22,24,25,27,28,29,30,31,32,33,34,35,36,37,38,39,40,42,43,44)));
        DATA.put("Matthew:22", new HashSet<>(Arrays.asList(2,3,4,5,6,7,8,9,10,11,12,13,14,18,19,20,21,29,30,31,32,37,38,39,40,42,43,44,45)));
        DATA.put("Matthew:23", new HashSet<>(Arrays.asList(2,3,4,5,6,7,8,9,10,11,12,13,15,16,17,18,19,20,21,22,23,24,25,26,27,28,29,30,31,32,33,34,35,36,37,38,39)));
        DATA.put("Matthew:24", new HashSet<>(Arrays.asList(2,4,5,6,7,8,9,10,11,12,13,14,15,16,17,18,19,20,21,22,23,24,25,26,27,28,29,30,31,32,33,34,35,36,37,38,39,40,41,42,43,44,45,46,47,48,49,50,51)));
        DATA.put("Matthew:25", new HashSet<>(Arrays.asList(1,2,3,4,5,6,7,8,9,10,11,12,13,14,15,16,17,18,19,20,21,22,23,24,25,26,27,28,29,30,31,32,33,34,35,36,37,38,39,40,41,42,43,44,45,46)));
        DATA.put("Matthew:26", new HashSet<>(Arrays.asList(2,10,11,12,13,18,21,23,24,28,29,31,32,34,36,38,39,40,41,42,45,46,52,53,54,55,56,64,75)));
        DATA.put("Matthew:27", new HashSet<>(Arrays.asList(46)));
        DATA.put("Matthew:28", new HashSet<>(Arrays.asList(10,18,19,20)));
        DATA.put("Mark:1", new HashSet<>(Arrays.asList(15,17,25,38,44)));
        DATA.put("Mark:2", new HashSet<>(Arrays.asList(5,9,10,11,17,19,20,21,22,25,26,27,28)));
        DATA.put("Mark:3", new HashSet<>(Arrays.asList(3,4,23,24,25,26,27,28,29,33,34,35)));
        DATA.put("Mark:4", new HashSet<>(Arrays.asList(3,4,5,6,7,8,9,11,12,13,14,15,16,17,18,19,20,21,22,23,24,25,26,27,28,29,30,31,32,35,40)));
        DATA.put("Mark:5", new HashSet<>(Arrays.asList(8,19,34,36,39,41)));
        DATA.put("Mark:6", new HashSet<>(Arrays.asList(4,8,9,10,11,31,38,50)));
        DATA.put("Mark:7", new HashSet<>(Arrays.asList(6,7,8,9,10,11,12,13,14,15,18,19,20,21,22,23,27,29)));
        DATA.put("Mark:8", new HashSet<>(Arrays.asList(2,3,5,12,15,17,18,19,20,21,26,29,33,34,35,36,37,38)));
        DATA.put("Mark:9", new HashSet<>(Arrays.asList(1,12,13,16,19,21,23,25,29,31,33,35,37,39,40,41,42,43,45,47,48,49,50)));
        DATA.put("Mark:10", new HashSet<>(Arrays.asList(3,5,6,7,8,9,11,12,14,15,18,19,21,23,24,25,27,29,30,31,33,34,36,38,39,40,42,43,44,45,51)));
        DATA.put("Mark:11", new HashSet<>(Arrays.asList(2,3,14,17,22,23,24,25,29,30,33)));
        DATA.put("Mark:12", new HashSet<>(Arrays.asList(1,2,3,4,5,6,7,8,9,10,11,15,16,17,24,25,26,27,29,30,31,35,36,37,38,39,40,43,44)));
        DATA.put("Mark:13", new HashSet<>(Arrays.asList(2,5,6,7,8,9,10,11,12,13,14,15,16,17,18,19,20,21,22,23,24,25,26,27,28,29,30,31,32,33,34,35,36,37)));
        DATA.put("Mark:14", new HashSet<>(Arrays.asList(6,7,8,9,13,14,15,18,20,21,24,25,27,28,30,34,36,37,38,41,42,48,49,62,72)));
        DATA.put("Mark:15", new HashSet<>(Arrays.asList(34)));
        DATA.put("Mark:16", new HashSet<>(Arrays.asList(15,16,17,18)));
        DATA.put("Luke:2", new HashSet<>(Arrays.asList(49)));
        DATA.put("Luke:4", new HashSet<>(Arrays.asList(4,8,12,18,19,21,23,24,25,26,27,43)));
        DATA.put("Luke:5", new HashSet<>(Arrays.asList(4,10,14,20,22,23,24,31,32,34,35,36,37,38,39)));
        DATA.put("Luke:6", new HashSet<>(Arrays.asList(3,4,5,9,20,21,22,23,24,25,26,27,28,29,30,31,32,33,34,35,36,37,38,39,40,41,42,43,44,45,46,47,48,49)));
        DATA.put("Luke:7", new HashSet<>(Arrays.asList(9,22,23,24,25,26,27,28,31,32,33,34,35,40,41,42,44,45,46,47,48,50)));
        DATA.put("Luke:8", new HashSet<>(Arrays.asList(5,6,7,8,10,11,12,13,14,15,16,17,18,21,22,39,46,48,50,52,54)));
        DATA.put("Luke:9", new HashSet<>(Arrays.asList(3,4,5,14,20,22,23,24,25,26,27,41,44,48,50,58,60,62)));
        DATA.put("Luke:10", new HashSet<>(Arrays.asList(2,3,4,5,6,7,8,9,10,11,12,13,14,15,16,18,19,20,21,22,23,24,26,28,30,31,32,33,34,35,36,41,42)));
        DATA.put("Luke:11", new HashSet<>(Arrays.asList(2,3,4,5,6,7,8,9,10,11,12,13,17,18,19,20,21,22,23,24,25,26,28,29,30,31,32,33,34,35,36,39,40,41,42,43,44,46,47,48,49,50,51,52)));
        DATA.put("Luke:12", new HashSet<>(Arrays.asList(1,2,3,4,5,6,7,8,9,10,11,12,14,15,16,17,18,19,20,21,22,23,24,25,26,27,28,29,30,31,32,33,34,35,36,37,38,39,40,42,43,44,45,46,47,48,49,50,51,52,53,54,55,56,57,58,59)));
        DATA.put("Luke:13", new HashSet<>(Arrays.asList(2,3,4,5,6,7,8,9,12,15,16,18,19,20,21,24,25,26,27,28,29,30,32,33,34,35)));
        DATA.put("Luke:14", new HashSet<>(Arrays.asList(3,5,8,9,10,11,12,13,14,16,17,18,19,20,21,22,23,24,26,27,28,29,30,31,32,33,34,35)));
        DATA.put("Luke:15", new HashSet<>(Arrays.asList(4,5,6,7,8,9,10,11,12,13,14,15,16,17,18,19,20,21,22,23,24,25,26,27,28,29,30,31,32)));
        DATA.put("Luke:16", new HashSet<>(Arrays.asList(1,2,3,4,5,6,7,8,9,10,11,12,13,15,16,17,18,19,20,21,22,23,24,25,26,27,28,29,30,31)));
        DATA.put("Luke:17", new HashSet<>(Arrays.asList(1,2,3,4,6,7,8,9,10,14,17,18,19,20,21,22,23,24,25,26,27,28,29,30,31,32,33,34,35,37)));
        DATA.put("Luke:18", new HashSet<>(Arrays.asList(2,3,4,5,6,7,8,10,11,12,13,14,16,17,19,20,22,24,25,27,29,30,31,32,33,41,42)));
        DATA.put("Luke:19", new HashSet<>(Arrays.asList(5,9,10,12,13,14,15,16,17,18,19,20,21,22,23,24,25,26,27,30,31,40,42,43,44,46)));
        DATA.put("Luke:20", new HashSet<>(Arrays.asList(3,4,8,9,10,11,12,13,14,15,16,17,18,24,25,34,35,36,37,38,41,42,43,44,46,47)));
        DATA.put("Luke:21", new HashSet<>(Arrays.asList(3,4,6,8,9,10,11,12,13,14,15,16,17,18,19,20,21,22,23,24,25,26,27,28,29,30,31,32,33,34,35,36)));
        DATA.put("Luke:22", new HashSet<>(Arrays.asList(8,10,11,12,15,16,17,18,19,20,21,22,25,26,27,28,29,30,31,32,34,35,36,37,40,42,46,48,52,53,61,67,68,69)));
        DATA.put("Luke:23", new HashSet<>(Arrays.asList(28,29,30,31,34,43,46)));
        DATA.put("Luke:24", new HashSet<>(Arrays.asList(7,17,25,26,38,39,44,46,47,48,49)));
        DATA.put("John:1", new HashSet<>(Arrays.asList(42,47,48,50,51)));
        DATA.put("John:2", new HashSet<>(Arrays.asList(4,7,8,16,19)));
        DATA.put("John:3", new HashSet<>(Arrays.asList(3,5,6,7,8,10,11,12,13,14,15)));
        DATA.put("John:4", new HashSet<>(Arrays.asList(7,10,13,14,16,17,18,21,22,23,24,26,32,34,35,36,37,38,48,50)));
        DATA.put("John:5", new HashSet<>(Arrays.asList(8,11,14,17,19,20,21,22,23,24,25,26,27,28,29,30,31,32,33,34,35,36,37,38,39,40,41,42,43,44,45,46,47)));
        DATA.put("John:6", new HashSet<>(Arrays.asList(5,12,20,26,27,29,32,33,35,36,37,38,39,40,41,43,44,45,46,47,48,49,50,51,53,54,55,56,57,58,62,63,64,65,67,70)));
        DATA.put("John:7", new HashSet<>(Arrays.asList(6,7,8,16,17,18,19,21,22,23,24,28,29,33,34,36,37,38)));
        DATA.put("John:8", new HashSet<>(Arrays.asList(12,14,15,16,17,18,19,21,22,23,24,25,26,28,29,31,32,34,35,36,37,38,39,40,41,42,43,44,45,46,47,49,50,51,54,55,56,58)));
        DATA.put("John:9", new HashSet<>(Arrays.asList(3,4,5,35,37,39,41)));
        DATA.put("John:10", new HashSet<>(Arrays.asList(1,2,3,4,5,7,8,9,10,11,12,13,14,15,16,17,18,25,26,27,28,29,30,32,34,35,36,37,38)));
        DATA.put("John:11", new HashSet<>(Arrays.asList(4,7,9,10,11,14,15,23,25,26,34,40,41,42,43)));
        DATA.put("John:12", new HashSet<>(Arrays.asList(7,8,23,24,25,26,27,30,31,32,35,36,44,45,46,47,48,49,50)));
        DATA.put("John:13", new HashSet<>(Arrays.asList(7,8,10,12,13,14,15,16,17,18,19,20,21,26,27,31,32,33,34,35,36,38)));
        DATA.put("John:14", new HashSet<>(Arrays.asList(1,2,3,4,6,7,9,10,11,12,13,14,15,16,17,18,19,20,21,23,24,25,26,27,28,29,30,31)));
        DATA.put("John:15", new HashSet<>(Arrays.asList(1,2,3,4,5,6,7,8,9,10,11,12,13,14,15,16,17,18,19,20,21,22,23,24,25,26,27)));
        DATA.put("John:16", new HashSet<>(Arrays.asList(1,2,3,4,5,6,7,8,9,10,11,12,13,14,15,16,17,19,20,21,22,23,24,25,26,27,28,31,32,33)));
        DATA.put("John:17", new HashSet<>(Arrays.asList(1,2,3,4,5,6,7,8,9,10,11,12,13,14,15,16,17,18,19,20,21,22,23,24,25,26)));
        DATA.put("John:18", new HashSet<>(Arrays.asList(8,9,11,20,21,23,34,36,37)));
        DATA.put("John:19", new HashSet<>(Arrays.asList(11)));
        DATA.put("John:20", new HashSet<>(Arrays.asList(17,21,22,23,27,29)));
        DATA.put("John:21", new HashSet<>(Arrays.asList(5,6,10,15,16,17,18,22,23)));
        DATA.put("Acts:1", new HashSet<>(Arrays.asList(4,5,7,8)));
        DATA.put("Acts:9", new HashSet<>(Arrays.asList(4,5,6,11,12,15,16)));
        DATA.put("Acts:10", new HashSet<>(Arrays.asList(13,15)));
        DATA.put("Acts:11", new HashSet<>(Arrays.asList(7,9,16)));
        DATA.put("Acts:18", new HashSet<>(Arrays.asList(9,10)));
        DATA.put("Acts:22", new HashSet<>(Arrays.asList(7,8,10,18,21)));
        DATA.put("Acts:23", new HashSet<>(Arrays.asList(11)));
        DATA.put("Acts:26", new HashSet<>(Arrays.asList(14,15,16,17,18)));
        DATA.put("1 Corinthians:11", new HashSet<>(Arrays.asList(24,25)));
        DATA.put("2 Corinthians:12", new HashSet<>(Arrays.asList(9)));
        DATA.put("Revelation:1", new HashSet<>(Arrays.asList(11,17,18,19,20)));
        DATA.put("Revelation:2", new HashSet<>(Arrays.asList(1,2,3,4,5,6,7,8,9,10,11,12,13,14,15,16,17,18,19,20,21,22,23,24,25,26,27,28,29)));
        DATA.put("Revelation:3", new HashSet<>(Arrays.asList(1,2,3,4,5,6,7,8,9,10,11,12,13,14,15,16,17,18,19,20,21,22)));
        DATA.put("Revelation:4", new HashSet<>(Arrays.asList(1)));
        DATA.put("Revelation:16", new HashSet<>(Arrays.asList(15)));
        DATA.put("Revelation:22", new HashSet<>(Arrays.asList(7,12,13,14,15,16)));
    }

    public static boolean isRedLetter(String book, int chapter, int verse) {
        Set<Integer> set = DATA.get(book + ":" + chapter);
        return set != null && set.contains(verse);
    }
}
