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
import com.fountainpdl.bible.utils.PrefsManager;

import java.util.function.Consumer;

public class MoreFragment extends Fragment {

    private MainActivity activity;
    private LinearLayout hubList, detailContent;
    private View hubScroll, detailContainer;
    private TextView detailTitle;
    private String currentPage = null;

    private static final int[] PRIMARY_PRESETS = {
        0xFF7B2FBE, 0xFF6D28D9, 0xFF4F46E5, 0xFF2563EB, 0xFF0891B2, 0xFF059669, 0xFFD97706, 0xFFDC2626
    };
    private static final int[] ACCENT_PRESETS = {
        0xFFC0392B, 0xFFE74C3C, 0xFFBE185D, 0xFF7C3AED, 0xFF0EA5E9, 0xFF10B981, 0xFFF59E0B, 0xFFEF4444
    };

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
            s.translation = val;
            activity.getPrefsManager().saveSettings(s);
            activity.switchTranslationSilently();
        }));

        detailContent.addView(sectionLabel("BEHAVIOR"));
        detailContent.addView(toggleRow("Confirm Before Delete", "Ask before removing notes, sermons, marathons",
            s.confirmBeforeDelete, val -> { s.confirmBeforeDelete = val; save(s); }));
        detailContent.addView(toggleRow("Vibrate on Selection", "Haptic feedback when selecting verses",
            s.vibrateOnSelect, val -> { s.vibrateOnSelect = val; save(s); }));
        detailContent.addView(toggleRow("Keep Screen On", "Prevent screen from sleeping while reading",
            s.keepScreenOn, val -> {
                s.keepScreenOn = val; save(s);
                if (activity.getWindow() != null) {
                    if (val) activity.getWindow().addFlags(android.view.WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
                    else activity.getWindow().clearFlags(android.view.WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
                }
            }));

        detailContent.addView(sectionLabel("DEFAULT LIBRARY TAB"));
        detailContent.addView(pillRow(new String[]{"bookmarks", "notes", "sermons", "marathon"}, s.defaultLibraryTab,
            val -> { s.defaultLibraryTab = val; save(s); }));
    }

    // ── APPEARANCE page ───────────────────────────────────────────────────────
    private void buildAppearancePage() {
        AppSettings s = activity.getPrefsManager().getSettings();

        detailContent.addView(sectionLabel("THEME MODE"));
        detailContent.addView(pillRow(new String[]{"light", "dark", "amoled"}, s.themeMode, val -> {
            s.themeMode = val; save(s); activity.applyTheme(); showDetail("appearance", "Appearance");
        }));

        detailContent.addView(toggleRow("Auto Night Mode", "Switch to dark automatically at night hour",
            s.autoNight, val -> { s.autoNight = val; save(s); }));

        detailContent.addView(sectionLabel("PRIMARY COLOR (tints all backgrounds)"));
        detailContent.addView(colorRow(PRIMARY_PRESETS, s.primaryColor, hex -> {
            s.primaryColor = hex; save(s); activity.applyTheme(); showDetail("appearance", "Appearance");
        }));

        detailContent.addView(sectionLabel("ACCENT COLOR (buttons, markers, gradient)"));
        detailContent.addView(colorRow(ACCENT_PRESETS, s.accentColor, hex -> {
            s.accentColor = hex; save(s); activity.applyTheme(); showDetail("appearance", "Appearance");
        }));

        detailContent.addView(sectionLabel("LAYOUT"));
        detailContent.addView(toggleRow("Compact Mode", "Reduce spacing between verses",
            s.compactMode, val -> { s.compactMode = val; save(s); }));
        detailContent.addView(toggleRow("Justify Text", "Align text to both margins",
            s.justifyText, val -> { s.justifyText = val; save(s); }));
        detailContent.addView(toggleRow("Bold Verse Numbers", "Make verse numbers stand out more",
            s.boldVerseNumbers, val -> { s.boldVerseNumbers = val; save(s); }));
    }

    // ── READING page ──────────────────────────────────────────────────────────
    private void buildReadingPage() {
        AppSettings s = activity.getPrefsManager().getSettings();

        detailContent.addView(sectionLabel("READING MODE"));
        detailContent.addView(pillRow(new String[]{"scroll", "focus"}, s.readingMode, val -> { s.readingMode = val; save(s); }));

        detailContent.addView(sectionLabel("DISPLAY"));
        detailContent.addView(toggleRow("Verse Numbers", "Show the number before each verse",
            s.verseNumbers, val -> { s.verseNumbers = val; save(s); }));
        detailContent.addView(toggleRow("Red Letter", "Words of Christ shown in red (New Testament)",
            s.redLetter, val -> { s.redLetter = val; save(s); }));
        detailContent.addView(toggleRow("Show Chapter Header", "Display book/chapter title above verses",
            s.showChapterHeader, val -> { s.showChapterHeader = val; save(s); }));
        detailContent.addView(toggleRow("Range Verse Selection", "Pick a start and end verse when navigating",
            s.rangeVerseSelect, val -> { s.rangeVerseSelect = val; save(s); }));

        detailContent.addView(sectionLabel("FONT SIZE (" + s.fontSize + "px)"));
        detailContent.addView(sliderRow(12, 30, s.fontSize, val -> { s.fontSize = val; save(s); }));

        detailContent.addView(sectionLabel("LINE SPACING"));
        detailContent.addView(sliderRow(10, 26, Math.round(s.lineSpacing * 10), val -> { s.lineSpacing = val / 10f; save(s); }));
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
        detailContent.addView(sliderRow(5, 20, Math.round(s.ttsRate * 10), val -> { s.ttsRate = val / 10f; save(s); }));

        detailContent.addView(sectionLabel("VOICE PITCH (" + s.ttsPitch + ")"));
        detailContent.addView(sliderRow(5, 20, Math.round(s.ttsPitch * 10), val -> { s.ttsPitch = val / 10f; save(s); }));

        detailContent.addView(toggleRow("Auto-Scroll While Reading", "Scroll to the verse currently being read",
            s.ttsAutoScroll, val -> { s.ttsAutoScroll = val; save(s); }));
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
        try { selectedColor = Color.parseColor(selectedHex); } catch (Exception e) { selectedColor = presets[0]; }
        for (int color : presets) {
            View dot = new View(getContext());
            int size = dp(30);
            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(size, size);
            lp.setMarginEnd(dp(8));
            dot.setLayoutParams(lp);
            GradientDrawable gd = new GradientDrawable();
            gd.setShape(GradientDrawable.OVAL);
            gd.setColor(color);
            if (color == selectedColor) gd.setStroke(dp(3), Color.WHITE);
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
        dotBg.setColor(ok ? Color.parseColor("#27AE60") : Color.parseColor("#888888"));
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

    private void save(AppSettings s) { activity.getPrefsManager().saveSettings(s); }

    private String capitalize(String s) { return s.isEmpty() ? s : Character.toUpperCase(s.charAt(0)) + s.substring(1); }

    // ── Theming ───────────────────────────────────────────────────────────────
    public void onThemeChanged() {
        if (activity == null || getView() == null) return;
        AppTheme t = activity.getAppTheme();
        getView().setBackgroundColor(t.surface);
        applyThemeRecursive(hubList, t);
        if (detailContainer.getVisibility() == View.VISIBLE) applyThemeToDetailPage();
        if (detailTitle != null) detailTitle.setTextColor(t.primary);
    }

    private void applyThemeToDetailPage() {
        AppTheme t = activity.getAppTheme();
        applyThemeRecursive(detailContent, t);
    }

    private void applyThemeRecursive(View view, AppTheme t) {
        if (view instanceof TextView && !(view instanceof SwitchCompat)) {
            TextView tv = (TextView) view;
            if ("clearBtn".equals(tv.getTag())) {
                tv.setTextColor(Color.WHITE);
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
            } else {
                tv.setTextColor(t.text);
            }
        }
        if (view instanceof ViewGroup) {
            ViewGroup vg = (ViewGroup) view;
            Object tag = vg.getTag();
            if (tag instanceof View[]) {
                View[] parts = (View[]) tag;
                TextView icon = (TextView) parts[0];
                icon.setTextColor(Color.WHITE);
                if (icon.getBackground() != null) icon.getBackground().setTint(t.primary);
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
        if (active) { gd.setColor(withAlpha(t.primary, 0x28)); gd.setStroke(dp(1), t.primary); btn.setTextColor(t.primary); }
        else { gd.setColor(Color.TRANSPARENT); gd.setStroke(dp(1), withAlpha(t.sub, 0x55)); btn.setTextColor(t.sub); }
        btn.setBackground(gd);
    }

    private int withAlpha(int color, int alpha) { return Color.argb(alpha, Color.red(color), Color.green(color), Color.blue(color)); }
    private int dp(int v) { return (int) (v * getResources().getDisplayMetrics().density); }
}
