package com.fountainpdl.bible.fragments;

import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.SwitchCompat;
import androidx.fragment.app.Fragment;

import com.fountainpdl.bible.MainActivity;
import com.fountainpdl.bible.R;
import com.fountainpdl.bible.models.AppSettings;
import com.fountainpdl.bible.utils.AppTheme;
import com.fountainpdl.bible.utils.ColorUtils;
import com.fountainpdl.bible.utils.ThemePresets;
import com.fountainpdl.bible.utils.PrefsManager;

import java.util.function.Consumer;

public class MoreFragment extends Fragment {

    private MainActivity activity;
    private LinearLayout hubList, detailContent;
    private View hubScroll, detailContainer;
    private TextView detailTitle;
    private String currentPage = null;

    @Nullable @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_more, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View v, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(v, savedInstanceState);
        activity = (MainActivity) getActivity();
        if (activity == null) return;

        hubScroll = v.findViewById(R.id.moreHubScroll);
        hubList = v.findViewById(R.id.moreHubList);
        detailContainer = v.findViewById(R.id.moreDetailContainer);
        detailContent = v.findViewById(R.id.moreDetailContent);
        detailTitle = v.findViewById(R.id.moreDetailTitle);
        v.findViewById(R.id.moreDetailBack).setOnClickListener(x -> showHub());

        buildHub();
        onThemeChanged();
    }

    private void buildHub() {
        hubList.removeAllViews();
        String[][] items = {
            {"S", "Settings", "Translation and general behavior", "settings"},
            {"A", "Appearance", "Theme, colors, layout", "appearance"},
            {"R", "Reading Preferences", "Mode, typography, verse display", "reading"},
            {"P", "Audio & Text-to-Speech", "Voice, speed, auto-scroll", "audio"},
            {"D", "Data Management", "Clear history, bookmarks, notes", "data"},
            {"i", "About", "FountainPDL's Bible", "about"},
            {"?", "Help & Shortcuts", "Gestures and tips", "help"},
        };
        for (String[] item : items) hubList.addView(buildHubRow(item[0], item[1], item[2], item[3]));
    }

    private View buildHubRow(String iconLetter, String title, String subtitle, String pageId) {
        LinearLayout row = new LinearLayout(getContext());
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setGravity(Gravity.CENTER_VERTICAL);
        row.setPadding(dp(16), dp(14), dp(16), dp(14));
        row.setClickable(true);
        row.setFocusable(true);
        android.util.TypedValue outValue = new android.util.TypedValue();
        requireContext().getTheme().resolveAttribute(android.R.attr.selectableItemBackground, outValue, true);
        if (outValue.resourceId != 0) {
            row.setForeground(androidx.core.content.ContextCompat.getDrawable(requireContext(), outValue.resourceId));
        }

        TextView icon = new TextView(getContext());
        icon.setText(iconLetter);
        icon.setTextSize(15);
        icon.setGravity(Gravity.CENTER);
        icon.setTypeface(null, android.graphics.Typeface.BOLD);
        LinearLayout.LayoutParams iconLp = new LinearLayout.LayoutParams(dp(38), dp(38));
        iconLp.setMarginEnd(dp(12));
        icon.setLayoutParams(iconLp);
        GradientDrawable iconBg = new GradientDrawable();
        iconBg.setCornerRadius(dp(4));
        icon.setBackground(iconBg);
        row.addView(icon);

        LinearLayout textCol = new LinearLayout(getContext());
        textCol.setOrientation(LinearLayout.VERTICAL);
        LinearLayout.LayoutParams textLp = new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f);
        textCol.setLayoutParams(textLp);

        TextView titleView = new TextView(getContext());
        titleView.setText(title);
        titleView.setTextSize(14.5f);
        textCol.addView(titleView);

        TextView subView = new TextView(getContext());
        subView.setText(subtitle);
        subView.setTextSize(11.5f);
        subView.setPadding(0, dp(1), 0, 0);
        textCol.addView(subView);

        row.addView(textCol);

        TextView chevron = new TextView(getContext());
        chevron.setText("\u203A");
        chevron.setTextSize(18);
        row.addView(chevron);

        row.setTag(new View[]{icon, titleView, subView, chevron});
        row.setOnClickListener(x -> showDetail(pageId, title));
        return row;
    }

    private void showDetail(String pageId, String title) {
        currentPage = pageId;
        detailTitle.setText(title);
        hubScroll.setVisibility(View.GONE);
        detailContainer.setVisibility(View.VISIBLE);
        detailContent.removeAllViews();

        switch (pageId) {
            case "settings": buildSettingsPage(); break;
            case "appearance": buildAppearancePage(); break;
            case "reading": buildReadingPage(); break;
            case "audio": buildAudioPage(); break;
            case "data": buildDataPage(); break;
            case "about": buildAboutPage(); break;
            case "help": buildHelpPage(); break;
        }
        applyThemeToDetailPage();
    }

    /** Back inside a settings sub-page returns to the list instead of leaving the tab. */
    public boolean handleBack() {
        if (currentPage != null && detailContainer != null && detailContainer.getVisibility() == View.VISIBLE) {
            showHub();
            return true;
        }
        return false;
    }

    @Override
    public void onHiddenChanged(boolean hidden) {
        super.onHiddenChanged(hidden);
        if (!hidden && getView() != null) {
            onThemeChanged();
            // Engine status may have changed since the Audio page was built.
            if ("audio".equals(currentPage)) showDetail("audio", detailTitle.getText().toString());
        }
    }

    private void showHub() {
        currentPage = null;
        detailContainer.setVisibility(View.GONE);
        hubScroll.setVisibility(View.VISIBLE);
        buildHub();
        onThemeChanged();
    }

    // ── SETTINGS page ─────────────────────────────────────────────────────────
    private void buildSettingsPage() {
        AppSettings s = activity.getPrefsManager().getSettings();

        detailContent.addView(sectionLabel("TRANSLATION"));
        detailContent.addView(pillRow(new String[]{"KJV", "NIV"}, s.translation, val -> {
            change(st -> st.translation = val);
        }));

        detailContent.addView(sectionLabel("BEHAVIOR"));
        detailContent.addView(toggleRow("Confirm Before Delete", "Ask before removing notes, sermons, marathons",
            s.confirmBeforeDelete, val -> { change(st -> st.confirmBeforeDelete = val); }));
        detailContent.addView(toggleRow("Vibrate on Selection", "Haptic feedback when selecting verses",
            s.vibrateOnSelect, val -> { change(st -> st.vibrateOnSelect = val); }));
        detailContent.addView(toggleRow("Keep Screen On", "Prevent screen from sleeping while reading",
            s.keepScreenOn, val -> change(st -> st.keepScreenOn = val)));

        detailContent.addView(sectionLabel("DEFAULT LIBRARY TAB"));
        detailContent.addView(pillRow(new String[]{"bookmarks", "notes", "sermons", "marathon"}, s.defaultLibraryTab,
            val -> { change(st -> st.defaultLibraryTab = val); }));
    }

    // ── APPEARANCE page ───────────────────────────────────────────────────────
    private void buildAppearancePage() {
        AppSettings s = activity.getPrefsManager().getSettings();
        ThemePresets.Preset current = ThemePresets.resolve(s);

        detailContent.addView(sectionLabel("DARK THEMES"));
        addPresetGrid(true, current);
        detailContent.addView(sectionLabel("LIGHT THEMES"));
        addPresetGrid(false, current);

        detailContent.addView(toggleRow("Auto Night Mode", "Light themes switch to their dark partner at night",
            s.autoNight, val -> change(st -> st.autoNight = val)));

        detailContent.addView(sectionLabel("CUSTOM THEME"));
        TextView hint = new TextView(getContext());
        hint.setText("Pick your own colors. Choosing any of these switches to a custom theme" + (current == null ? " (active)." : "."));
        hint.setTextSize(12);
        hint.setTag("hint");
        hint.setPadding(0, 0, 0, dp(8));
        detailContent.addView(hint);

        String mode = current != null ? (current.amoled ? "amoled" : current.dark ? "dark" : "light") : s.themeMode;
        if (current != null && "sepia".equals(current.id)) mode = "sepia";
        detailContent.addView(pillRow(new String[]{"light", "dark", "amoled", "sepia"}, current == null ? s.themeMode : "", val -> {
            final String seedP = hex(current != null ? current.primary : parse(s.primaryColor));
            final String seedA = hex(current != null ? current.accent : parse(s.accentColor));
            change(st -> seedCustom(st, seedP, seedA, val));
        }));

        detailContent.addView(sectionLabel("PRIMARY COLOR (tints backgrounds and highlights)"));
        detailContent.addView(colorRow(ThemePresets.PRIMARY_SWATCHES, current == null ? s.primaryColor : "", hexColor -> {
            final String seedA = hex(current != null ? current.accent : parse(s.accentColor));
            final String m = mode;
            change(st -> { seedCustom(st, hexColor, seedA, current == null ? st.themeMode : m); });
            showDetail("appearance", "Appearance");
        }));

        detailContent.addView(sectionLabel("ACCENT COLOR (buttons, markers, gradient)"));
        detailContent.addView(colorRow(ThemePresets.ACCENT_SWATCHES, current == null ? s.accentColor : "", hexColor -> {
            final String seedP = hex(current != null ? current.primary : parse(s.primaryColor));
            final String m = mode;
            change(st -> { seedCustom(st, seedP, hexColor, current == null ? st.themeMode : m); });
            showDetail("appearance", "Appearance");
        }));

        detailContent.addView(sectionLabel("LAYOUT"));
        detailContent.addView(toggleRow("Compact Mode", "Reduce spacing between verses",
            s.compactMode, val -> { change(st -> st.compactMode = val); }));
        detailContent.addView(toggleRow("Justify Text", "Align text to both margins",
            s.justifyText, val -> { change(st -> st.justifyText = val); }));
        detailContent.addView(toggleRow("Bold Verse Numbers", "Make verse numbers stand out more",
            s.boldVerseNumbers, val -> { change(st -> st.boldVerseNumbers = val); }));
    }

    private static int parse(String hex) { return ColorUtils.parseColor(hex, ThemePresets.find(ThemePresets.DEFAULT_ID).primary); }
    private static String hex(int color) { return String.format("#%06X", 0xFFFFFF & color); }

    /** Switch to a custom theme, keeping the colors the user was looking at as the starting point. */
    private static void seedCustom(AppSettings st, String primaryHex, String accentHex, String mode) {
        st.themePreset = ThemePresets.CUSTOM_ID;
        st.primaryColor = primaryHex;
        st.accentColor = accentHex;
        st.themeMode = (mode == null || mode.isEmpty()) ? "dark" : mode;
    }

    /** Two-column grid of theme tiles. Each tile previews itself in its own colors. */
    private void addPresetGrid(boolean dark, ThemePresets.Preset current) {
        java.util.List<ThemePresets.Preset> list = new java.util.ArrayList<>();
        for (ThemePresets.Preset p : ThemePresets.ALL) if (p.dark == dark) list.add(p);
        AppTheme now = activity.getAppTheme();
        for (int i = 0; i < list.size(); i += 2) {
            LinearLayout row = new LinearLayout(getContext());
            row.setOrientation(LinearLayout.HORIZONTAL);
            for (int j = 0; j < 2; j++) {
                if (i + j < list.size()) row.addView(presetTile(list.get(i + j), current, now, j == 0));
                else { View filler = new View(getContext()); filler.setLayoutParams(new LinearLayout.LayoutParams(0, 1, 1f)); row.addView(filler); }
            }
            detailContent.addView(row);
        }
    }

    private View presetTile(ThemePresets.Preset p, ThemePresets.Preset current, AppTheme now, boolean left) {
        boolean selected = current != null && current.id.equals(p.id);
        LinearLayout tile = new LinearLayout(getContext());
        tile.setOrientation(LinearLayout.VERTICAL);
        tile.setPadding(dp(12), dp(10), dp(12), dp(12));
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f);
        lp.setMargins(left ? 0 : dp(5), 0, left ? dp(5) : 0, dp(10));
        tile.setLayoutParams(lp);
        GradientDrawable bg = new GradientDrawable();
        bg.setColor(p.bg);
        bg.setCornerRadius(dp(14));
        bg.setStroke(selected ? dp(3) : dp(1), selected ? now.primaryFg : ColorUtils.withAlpha(p.primary, 0x66));
        tile.setBackground(bg);

        TextView name = new TextView(getContext());
        name.setText((selected ? "\u2713 " : "") + p.name);
        name.setTextSize(13);
        name.setTypeface(android.graphics.Typeface.create("serif", android.graphics.Typeface.BOLD));
        name.setTextColor(p.text);
        name.setTag("keep");
        tile.addView(name);

        TextView sample = new TextView(getContext());
        sample.setText("In the beginning was the Word");
        sample.setTextSize(11);
        sample.setTextColor(p.sub);
        sample.setTag("keep");
        sample.setPadding(0, dp(2), 0, dp(8));
        tile.addView(sample);

        LinearLayout dots = new LinearLayout(getContext());
        dots.setOrientation(LinearLayout.HORIZONTAL);
        int[] cols = {p.card, p.primary, p.accent, p.red};
        for (int c : cols) {
            View d = new View(getContext());
            LinearLayout.LayoutParams dl = new LinearLayout.LayoutParams(dp(18), dp(18));
            dl.setMarginEnd(dp(6));
            d.setLayoutParams(dl);
            GradientDrawable g = new GradientDrawable();
            g.setShape(GradientDrawable.OVAL);
            g.setColor(c);
            g.setStroke(dp(1), ColorUtils.withAlpha(p.text, 0x44));
            d.setBackground(g);
            dots.addView(d);
        }
        tile.addView(dots);

        tile.setOnClickListener(v -> {
            change(st -> st.themePreset = p.id);
            showDetail("appearance", "Appearance");
        });
        return tile;
    }

    // ── READING page ──────────────────────────────────────────────────────────
    private void buildReadingPage() {
        AppSettings s = activity.getPrefsManager().getSettings();

        detailContent.addView(sectionLabel("READING MODE"));
        detailContent.addView(pillRow(new String[]{"scroll", "focus"}, s.readingMode, val -> { change(st -> st.readingMode = val); }));

        detailContent.addView(sectionLabel("RED LETTER COLOR"));
        detailContent.addView(pillRow(new String[]{"theme", "custom"}, s.redLetterColor.isEmpty() ? "theme" : "custom", val -> {
            if ("theme".equals(val)) change(st -> st.redLetterColor = "");
            else change(st -> { if (st.redLetterColor.isEmpty()) st.redLetterColor = hex(ThemePresets.RED_LETTER_SWATCHES[0]); });
        }));
        if (!s.redLetterColor.isEmpty()) {
            detailContent.addView(colorRow(ThemePresets.RED_LETTER_SWATCHES, s.redLetterColor,
                hexColor -> { change(st -> st.redLetterColor = hexColor); showDetail("reading", "Reading"); }));
        }

        detailContent.addView(sectionLabel("DISPLAY"));
        detailContent.addView(toggleRow("Verse Numbers", "Show the number before each verse",
            s.verseNumbers, val -> { change(st -> st.verseNumbers = val); }));
        detailContent.addView(toggleRow("Red Letter", "Words of Christ shown in red (New Testament)",
            s.redLetter, val -> { change(st -> st.redLetter = val); }));
        detailContent.addView(toggleRow("Show Chapter Header", "Display book/chapter title above verses",
            s.showChapterHeader, val -> { change(st -> st.showChapterHeader = val); }));
        detailContent.addView(toggleRow("Range Verse Selection", "Pick a start and end verse when navigating",
            s.rangeVerseSelect, val -> { change(st -> st.rangeVerseSelect = val); }));

        detailContent.addView(sectionLabel("FONT"));
        detailContent.addView(pillRow(new String[]{"serif", "sans-serif", "monospace"}, s.fontFamily, val -> { change(st -> st.fontFamily = val); }));

        detailContent.addView(sectionLabel("FONT SIZE (" + s.fontSize + "px)"));
        detailContent.addView(sliderRow(12, 30, s.fontSize, val -> { change(st -> st.fontSize = val); }));

        detailContent.addView(sectionLabel("LINE SPACING"));
        detailContent.addView(sliderRow(10, 26, Math.round(s.lineSpacing * 10), val -> { change(st -> st.lineSpacing = val / 10f); }));
    }

    // ── AUDIO page ────────────────────────────────────────────────────────────
    private void buildAudioPage() {
        AppSettings s = activity.getPrefsManager().getSettings();

        TextView info = new TextView(getContext());
        info.setText("Audio reading uses your device's built-in text-to-speech engine and works completely offline.");
        info.setTextSize(12.5f);
        info.setPadding(dp(14), dp(12), dp(14), dp(12));
        GradientDrawable infoBg = new GradientDrawable();
        infoBg.setCornerRadius(dp(4));
        info.setBackground(infoBg);
        info.setTag("infoBox");
        LinearLayout.LayoutParams infoLp = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        infoLp.setMargins(0, 0, 0, dp(16));
        info.setLayoutParams(infoLp);
        detailContent.addView(info);

        boolean ready = activity.getTtsManager() != null && activity.getTtsManager().isReady();
        detailContent.addView(statusRow("Engine Status", ready ? "Ready" : "Initializing...", ready));

        detailContent.addView(sectionLabel("READING SPEED (" + s.ttsRate + "x)"));
        detailContent.addView(sliderRow(5, 20, Math.round(s.ttsRate * 10), val -> { change(st -> st.ttsRate = val / 10f); }));

        detailContent.addView(sectionLabel("VOICE PITCH (" + s.ttsPitch + ")"));
        detailContent.addView(sliderRow(5, 20, Math.round(s.ttsPitch * 10), val -> { change(st -> st.ttsPitch = val / 10f); }));

        detailContent.addView(toggleRow("Auto-Scroll While Reading", "Scroll to the verse currently being read",
            s.ttsAutoScroll, val -> { change(st -> st.ttsAutoScroll = val); }));
    }

    // ── DATA page ─────────────────────────────────────────────────────────────
    private void buildDataPage() {
        PrefsManager prefs = activity.getPrefsManager();
        String[][] rows = {
            {"Clear Reading History", "history"},
            {"Clear All Bookmarks", "bookmarks"},
            {"Clear All Highlights", "highlights"},
            {"Delete All Notes", "notes"},
            {"Delete All Sermons", "sermons"},
            {"Delete All Marathons", "marathons"},
        };
        for (String[] row : rows) {
            detailContent.addView(clearRow(row[0], () -> {
                AppSettings s = prefs.getSettings();
                Runnable action = () -> {
                    switch (row[1]) {
                        case "history": prefs.clearHistory(); break;
                        case "bookmarks": prefs.clearBookmarks(); break;
                        case "highlights": prefs.clearHighlights(); break;
                        case "notes": prefs.clearNotes(); break;
                        case "sermons": prefs.clearSermons(); break;
                        case "marathons": prefs.clearMarathons(); break;
                    }
                };
                if (s.confirmBeforeDelete) {
                    new android.app.AlertDialog.Builder(requireContext())
                        .setTitle("Are you sure?")
                        .setMessage("This cannot be undone.")
                        .setPositiveButton("Delete", (d, w) -> action.run())
                        .setNegativeButton("Cancel", null)
                        .show();
                } else {
                    action.run();
                }
            }));
        }
    }

    // ── ABOUT page ────────────────────────────────────────────────────────────
    private void buildAboutPage() {
        TextView appName = new TextView(getContext());
        appName.setText("FountainPDL's Bible");
        appName.setTextSize(19);
        appName.setTypeface(null, android.graphics.Typeface.BOLD);
        appName.setGravity(Gravity.CENTER);
        detailContent.addView(appName, centerLp());

        TextView tagline = new TextView(getContext());
        tagline.setText("HOLY BIBLE");
        tagline.setTextSize(10.5f);
        tagline.setLetterSpacing(0.2f);
        tagline.setGravity(Gravity.CENTER);
        detailContent.addView(tagline, centerLp());

        TextView version = new TextView(getContext());
        version.setText("Version 3.0 - Native Android Edition");
        version.setTextSize(11.5f);
        version.setGravity(Gravity.CENTER);
        LinearLayout.LayoutParams vLp = centerLp();
        vLp.setMargins(0, dp(6), 0, dp(24));
        detailContent.addView(version, vLp);

        detailContent.addView(bodyText("TRANSLATIONS", true));
        detailContent.addView(bodyText("KJV - King James Version (1769), Public Domain, 31,102 verses.", false));
        detailContent.addView(bodyText("NIV - New International Version, 31,086 verses.", false));

        detailContent.addView(bodyText("FEATURES", true));
        detailContent.addView(bodyText("Complete offline Bible with notes, bookmarks, highlights, sermon builder, reading marathons, audio reading, and full-text search. Built natively for Android.", false));
    }

    // ── HELP page ─────────────────────────────────────────────────────────────
    private void buildHelpPage() {
        String[][] groups = {
            {"GESTURES",
                "Swipe left on Read screen - next chapter",
                "Swipe right on Read screen - previous chapter",
                "Long-press any verse - start reading aloud from there"},
            {"READING",
                "Tap a verse to select it, then use the action bar",
                "Tap the book/chapter chip in the top bar to navigate",
                "Tap the play icon in the top bar to read the whole chapter"},
            {"STUDY",
                "Select verse(s), then tap Highlight, Underline, or Bookmark",
                "Tap Word to annotate a specific phrase within a verse",
                "Tap Note to write a note linked to your selection",
                "In Notes, tap a reference to jump straight to that verse"},
            {"SERMONS",
                "Add scripture by picker or by typing a reference",
                "Add commentary blocks between verses",
                "Reorder blocks with the up/down arrows",
                "Sermon verse text always matches your current translation"},
        };
        for (String[] g : groups) {
            detailContent.addView(sectionLabel(g[0]));
            for (int i = 1; i < g.length; i++) detailContent.addView(helpLine(g[i]));
        }
    }

    // ── Reusable UI builders ──────────────────────────────────────────────────
    private TextView sectionLabel(String text) {
        TextView tv = new TextView(getContext());
        tv.setText(text);
        tv.setTextSize(10.5f);
        tv.setLetterSpacing(0.1f);
        tv.setPadding(0, dp(16), 0, dp(6));
        tv.setTag("section");
        return tv;
    }

    private LinearLayout toggleRow(String title, String subtitle, boolean checked, Consumer<Boolean> onChange) {
        LinearLayout row = new LinearLayout(getContext());
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setGravity(Gravity.CENTER_VERTICAL);
        row.setPadding(0, dp(9), 0, dp(9));

        LinearLayout textCol = new LinearLayout(getContext());
        textCol.setOrientation(LinearLayout.VERTICAL);
        textCol.setLayoutParams(new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f));
        TextView t = new TextView(getContext()); t.setText(title); t.setTextSize(13.5f); textCol.addView(t);
        if (subtitle != null) {
            TextView sub = new TextView(getContext()); sub.setText(subtitle); sub.setTextSize(11); textCol.addView(sub);
        }
        row.addView(textCol);

        SwitchCompat sw = new SwitchCompat(getContext());
        sw.setChecked(checked);
        sw.setOnCheckedChangeListener((btn, isChecked) -> onChange.accept(isChecked));
        row.addView(sw);

        row.setTag(new Object[]{textCol.getChildAt(0), subtitle != null ? textCol.getChildAt(1) : null});
        return row;
    }

    private LinearLayout pillRow(String[] options, String selected, Consumer<String> onSelect) {
        LinearLayout row = new LinearLayout(getContext());
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setPadding(0, dp(2), 0, dp(10));
        TextView[] buttons = new TextView[options.length];
        for (int i = 0; i < options.length; i++) {
            String opt = options[i];
            TextView btn = new TextView(getContext());
            btn.setText(capitalize(opt));
            btn.setTextSize(12);
            btn.setGravity(Gravity.CENTER);
            btn.setPadding(dp(14), dp(8), dp(14), dp(8));
            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f);
            lp.setMarginEnd(i < options.length - 1 ? dp(6) : 0);
            btn.setLayoutParams(lp);
            buttons[i] = btn;
            final String optFinal = opt;
            btn.setOnClickListener(v -> { onSelect.accept(optFinal); showDetail(currentPage, detailTitle.getText().toString()); });
            row.addView(btn);
        }
        row.setTag(new Object[]{buttons, options, selected});
        return row;
    }

    private LinearLayout colorRow(int[] presets, String selectedHex, Consumer<String> onSelect) {
        LinearLayout row = new LinearLayout(getContext());
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setPadding(0, dp(2), 0, dp(14));
        int selectedColor;
        try { selectedColor = Color.parseColor(selectedHex); } catch (Exception e) { selectedColor = 0; }
        for (int color : presets) {
            View dot = new View(getContext());
            int size = dp(30);
            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(size, size);
            lp.setMarginEnd(dp(8));
            dot.setLayoutParams(lp);
            GradientDrawable gd = new GradientDrawable();
            gd.setShape(GradientDrawable.OVAL);
            gd.setColor(color);
            if (color == selectedColor) gd.setStroke(dp(3), activity.getAppTheme().text);
            dot.setBackground(gd);
            dot.setOnClickListener(v -> onSelect.accept(String.format("#%06X", (0xFFFFFF & color))));
            row.addView(dot);
        }
        return row;
    }

    private LinearLayout sliderRow(int min, int max, int value, Consumer<Integer> onChange) {
        LinearLayout row = new LinearLayout(getContext());
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setPadding(0, dp(2), 0, dp(14));
        SeekBar seekBar = new SeekBar(getContext());
        seekBar.setMax(max - min);
        seekBar.setProgress(value - min);
        seekBar.setLayoutParams(new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f));
        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override public void onProgressChanged(SeekBar sb, int progress, boolean fromUser) {
                if (fromUser) onChange.accept(progress + min);
            }
            @Override public void onStartTrackingTouch(SeekBar sb) {}
            @Override public void onStopTrackingTouch(SeekBar sb) { showDetail(currentPage, detailTitle.getText().toString()); }
        });
        row.addView(seekBar);
        return row;
    }

    private LinearLayout statusRow(String title, String status, boolean ok) {
        LinearLayout row = new LinearLayout(getContext());
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setGravity(Gravity.CENTER_VERTICAL);
        row.setPadding(0, dp(9), 0, dp(9));
        TextView t = new TextView(getContext());
        t.setText(title);
        t.setTextSize(13.5f);
        t.setLayoutParams(new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f));
        row.addView(t);

        View dot = new View(getContext());
        LinearLayout.LayoutParams dotLp = new LinearLayout.LayoutParams(dp(8), dp(8));
        dotLp.setMarginEnd(dp(6));
        dot.setLayoutParams(dotLp);
        GradientDrawable dotBg = new GradientDrawable();
        dotBg.setShape(GradientDrawable.OVAL);
        dotBg.setColor(ok ? activity.getAppTheme().success : activity.getAppTheme().sub);
        dot.setBackground(dotBg);
        row.addView(dot);

        TextView statusText = new TextView(getContext());
        statusText.setText(status);
        statusText.setTextSize(12);
        row.addView(statusText);
        return row;
    }

    private LinearLayout clearRow(String label, Runnable onClick) {
        LinearLayout row = new LinearLayout(getContext());
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setGravity(Gravity.CENTER_VERTICAL);
        row.setPadding(0, dp(10), 0, dp(10));
        TextView t = new TextView(getContext());
        t.setText(label);
        t.setTextSize(13.5f);
        t.setLayoutParams(new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f));
        row.addView(t);
        TextView btn = new TextView(getContext());
        btn.setText("Clear");
        btn.setTextSize(11.5f);
        btn.setPadding(dp(14), dp(6), dp(14), dp(6));
        GradientDrawable bg = new GradientDrawable();
        bg.setCornerRadius(dp(16));
        btn.setBackground(bg);
        btn.setTag("clearBtn");
        btn.setOnClickListener(v -> onClick.run());
        row.addView(btn);
        return row;
    }

    private TextView bodyText(String text, boolean isHeader) {
        TextView tv = new TextView(getContext());
        tv.setText(text);
        tv.setTextSize(isHeader ? 11 : 13);
        if (isHeader) { tv.setLetterSpacing(0.08f); tv.setPadding(0, dp(14), 0, dp(4)); }
        else { tv.setLineSpacing(0, 1.5f); tv.setPadding(0, dp(2), 0, dp(4)); }
        return tv;
    }

    private TextView helpLine(String text) {
        TextView tv = new TextView(getContext());
        tv.setText("\u2022  " + text);
        tv.setTextSize(13);
        tv.setLineSpacing(0, 1.3f);
        tv.setPadding(0, dp(5), 0, dp(5));
        return tv;
    }

    private LinearLayout.LayoutParams centerLp() {
        return new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
    }

    /**
     * Applies one setting change to the CURRENT saved settings (not a copy captured
     * when the page was built, which could be stale and would overwrite changes made
     * elsewhere, e.g. the translation chip in the top bar) and pushes it live to
     * every screen at once.
     */
    private void change(Consumer<AppSettings> mutation) {
        PrefsManager prefs = activity.getPrefsManager();
        AppSettings fresh = prefs.getSettings();
        mutation.accept(fresh);
        prefs.saveSettings(fresh);
        activity.onSettingsChanged();
    }

    private String capitalize(String s) { return s.isEmpty() ? s : Character.toUpperCase(s.charAt(0)) + s.substring(1); }

    // ── Theming ───────────────────────────────────────────────────────────────
    public void onThemeChanged() {
        if (activity == null || getView() == null) return;
        AppTheme t = activity.getAppTheme();
        getView().setBackgroundColor(t.surface);
        applyThemeRecursive(hubList, t);
        if (detailContainer.getVisibility() == View.VISIBLE) applyThemeToDetailPage();
        if (detailTitle != null) detailTitle.setTextColor(t.primaryFg);
    }

    private void applyThemeToDetailPage() {
        AppTheme t = activity.getAppTheme();
        applyThemeRecursive(detailContent, t);
    }

    private void applyThemeRecursive(View view, AppTheme t) {
        if (view instanceof TextView && !(view instanceof SwitchCompat)) {
            TextView tv = (TextView) view;
            if ("clearBtn".equals(tv.getTag())) {
                tv.setTextColor(t.onAccent);
                GradientDrawable bg = new GradientDrawable();
                bg.setCornerRadius(dp(16));
                bg.setColor(t.accent);
                tv.setBackground(bg);
            } else if ("infoBox".equals(tv.getTag())) {
                tv.setTextColor(t.sub);
                GradientDrawable bg = new GradientDrawable();
                bg.setCornerRadius(dp(4));
                bg.setColor(t.card);
                tv.setBackground(bg);
            } else if ("keep".equals(tv.getTag())) {
                // painted with its own preview colors
            } else if ("hint".equals(tv.getTag())) {
                tv.setTextColor(t.sub);
            } else if ("section".equals(tv.getTag())) {
                tv.setTextColor(t.sub);
            } else {
                tv.setTextColor(t.text);
            }
        } else if (view instanceof SwitchCompat) {
            SwitchCompat sw = (SwitchCompat) view;
            sw.setThumbTintList(com.fountainpdl.bible.utils.ThemeHelper.stateList(t.primaryFg, t.sub));
            sw.setTrackTintList(com.fountainpdl.bible.utils.ThemeHelper.stateList(withAlpha(t.primary, 0x88), withAlpha(t.sub, 0x55)));
        } else if (view instanceof SeekBar) {
            SeekBar sb = (SeekBar) view;
            android.content.res.ColorStateList c = android.content.res.ColorStateList.valueOf(t.primaryFg);
            sb.setProgressTintList(c);
            sb.setThumbTintList(c);
            sb.setProgressBackgroundTintList(android.content.res.ColorStateList.valueOf(withAlpha(t.sub, 0x66)));
        }
        if (view instanceof ViewGroup) {
            ViewGroup vg = (ViewGroup) view;
            Object tag = vg.getTag();
            if (tag instanceof View[]) {
                View[] parts = (View[]) tag;
                TextView icon = (TextView) parts[0];
                icon.setTextColor(com.fountainpdl.bible.utils.ThemeHelper.onPrimary(t));
                if (icon.getBackground() != null) icon.getBackground().mutate().setTint(t.primary);
                ((TextView) parts[1]).setTextColor(t.text);
                ((TextView) parts[2]).setTextColor(t.sub);
                ((TextView) parts[3]).setTextColor(t.sub);
            }
            if (tag instanceof Object[]) {
                Object[] arr = (Object[]) tag;
                if (arr.length == 3 && arr[0] instanceof TextView[]) {
                    TextView[] buttons = (TextView[]) arr[0];
                    String[] options = (String[]) arr[1];
                    String selected = (String) arr[2];
                    for (int i = 0; i < buttons.length; i++) stylePill(buttons[i], options[i].equals(selected), t);
                }
            }
            for (int i = 0; i < vg.getChildCount(); i++) applyThemeRecursive(vg.getChildAt(i), t);
        }
    }

    private void stylePill(TextView btn, boolean active, AppTheme t) {
        GradientDrawable gd = new GradientDrawable();
        gd.setCornerRadius(dp(20));
        if (active) { gd.setColor(withAlpha(t.primary, 0x28)); gd.setStroke(dp(1), t.primary); btn.setTextColor(t.primaryFg); }
        else { gd.setColor(Color.TRANSPARENT); gd.setStroke(dp(1), withAlpha(t.sub, 0x55)); btn.setTextColor(t.sub); }
        btn.setBackground(gd);
    }

    private int withAlpha(int color, int alpha) { return Color.argb(alpha, Color.red(color), Color.green(color), Color.blue(color)); }
    private int dp(int v) { return (int) (v * getResources().getDisplayMetrics().density); }
}
