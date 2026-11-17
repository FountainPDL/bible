package com.fountainpdl.bible.models;

public class AppSettings {
    public String translation = "KJV";
    // Theme: a preset id from ThemePresets, or "custom" to use the colors below.
    // null on settings saved by older versions (see ThemePresets.resolve).
    public String themePreset = null;
    public String themeMode = "dark";      // only used by "custom": light | dark | amoled | sepia
    public String primaryColor = "";       // only used by "custom"
    public String accentColor = "";        // only used by "custom"
    public String redLetterColor = "";     // "" = the theme's own red-letter color
    public boolean autoNight = false;
    public int nightHour = 22;
    public int fontSize = 18;
    public float lineSpacing = 1.5f;
    public boolean verseNumbers = true;
    public boolean redLetter = true;
    public boolean justifyText = false;
    public boolean compactMode = false;
    public String readingMode = "scroll";
    public boolean rangeVerseSelect = false;
    public float ttsRate = 0.9f;
    public float ttsPitch = 1.0f;
    public boolean ttsAutoScroll = true;
    public boolean keepScreenOn = false;
    public boolean showChapterHeader = true;
    public boolean boldVerseNumbers = false;
    public String fontFamily = "serif";
    public boolean vibrateOnSelect = true;
    public boolean confirmBeforeDelete = true;
    public String defaultLibraryTab = "bookmarks";
}
