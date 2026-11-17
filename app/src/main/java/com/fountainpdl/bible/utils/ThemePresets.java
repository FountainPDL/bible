package com.fountainpdl.bible.utils;

import com.fountainpdl.bible.models.AppSettings;

import java.util.ArrayList;
import java.util.List;

/**
 * THE one place raw colors live (besides the neutral fallbacks in res/values/colors.xml).
 * Every screen, dialog and widget gets its colors from AppTheme, which is built from
 * one of these palettes or from the user's custom colors. Add a new theme by adding
 * one line to the list below; nothing else in the app needs to change.
 */
public final class ThemePresets {
    private ThemePresets() {}

    public static final String DEFAULT_ID = "royal";
    public static final String CUSTOM_ID = "custom";

    // Neutral building blocks used for contrast decisions and status indicators.
    public static final int INK = 0xFF000000;
    public static final int PAPER = 0xFFFFFFFF;
    public static final int SUCCESS = 0xFF2E9E5B;

    /** Colors offered for highlighting a verse or words (stored on the highlight itself). */
    public static final String[] HIGHLIGHT_COLORS = {
        "#F1C40F", "#27AE60", "#2980B9", "#E67E22", "#8E44AD", "#16A085", "#E74C3C", "#1ABC9C"
    };
    public static String defaultHighlight() { return HIGHLIGHT_COLORS[0]; }

    /** Swatches offered under "custom" for the primary / accent / red-letter color. */
    public static final int[] PRIMARY_SWATCHES = {
        0xFF7B2FBE, 0xFF6D28D9, 0xFF4F46E5, 0xFF2563EB, 0xFF0891B2, 0xFF059669, 0xFFD97706, 0xFFDC2626,
        0xFFBE185D, 0xFF64748B
    };
    public static final int[] ACCENT_SWATCHES = {
        0xFFC0392B, 0xFFE74C3C, 0xFFBE185D, 0xFF7C3AED, 0xFF0EA5E9, 0xFF10B981, 0xFFF59E0B, 0xFFEF4444,
        0xFF94A3B8, 0xFFFACC15
    };
    public static final int[] RED_LETTER_SWATCHES = {
        0xFFE53935, 0xFFC62828, 0xFFFF7043, 0xFFD81B60, 0xFF8E24AA, 0xFFFB8C00, 0xFFF9A825, 0xFF43A047
    };

    public static final class Preset {
        public final String id, name, nightId;
        public final boolean dark, amoled;
        public final int bg, surface, card, nav, text, muted, sub, primary, accent, red;
        Preset(String id, String name, boolean dark, boolean amoled, String nightId,
               int bg, int surface, int card, int nav, int text, int muted, int sub,
               int primary, int accent, int red) {
            this.id = id; this.name = name; this.dark = dark; this.amoled = amoled; this.nightId = nightId;
            this.bg = 0xFF000000 | bg; this.surface = 0xFF000000 | surface; this.card = 0xFF000000 | card;
            this.nav = 0xFF000000 | nav; this.text = 0xFF000000 | text; this.muted = 0xFF000000 | muted;
            this.sub = 0xFF000000 | sub; this.primary = 0xFF000000 | primary; this.accent = 0xFF000000 | accent;
            this.red = 0xFF000000 | red;
        }
    }

    public static final List<Preset> ALL = new ArrayList<>();

    private static void p(String id, String name, boolean dark, boolean amoled, String nightId,
                          int bg, int surface, int card, int nav, int text, int muted, int sub,
                          int primary, int accent, int red) {
        ALL.add(new Preset(id, name, dark, amoled, nightId, bg, surface, card, nav, text, muted, sub, primary, accent, red));
    }

    static {
        p("royal", "Royal Purple", true, false, null,
          0x0F0A1A, 0x150E24, 0x1C1232, 0x0B0714, 0xF2EAFF, 0xC4A8E0, 0x9B72C8, 0x7B2FBE, 0xC0392B, 0xFF7070);
        p("midnight", "Midnight Blue", true, false, null,
          0x0A1020, 0x0F182C, 0x16223C, 0x070C18, 0xE8F0FF, 0xA9C1E8, 0x7F9CCB, 0x3B82F6, 0x38BDF8, 0xFF7A7A);
        p("ocean", "Ocean Teal", true, false, null,
          0x06191C, 0x0A2327, 0x10303A, 0x041214, 0xE3F8FA, 0x9FD3DA, 0x74B4BD, 0x14B8C4, 0x34D399, 0xFF7B7B);
        p("forest", "Forest", true, false, null,
          0x0B1710, 0x112018, 0x182C20, 0x08110B, 0xE8F5EC, 0xA8CDB3, 0x7FAE8D, 0x22A06B, 0xE0A030, 0xFF7B6B);
        p("crimson", "Crimson", true, false, null,
          0x1A0A0C, 0x241014, 0x30171B, 0x120708, 0xFBEAEA, 0xE3AEB1, 0xC98588, 0xD93A3A, 0xF2A33A, 0xFF8A80);
        p("gold", "Royal Gold", true, false, null,
          0x14110A, 0x1C1810, 0x272015, 0x0E0C07, 0xF7F0DC, 0xD9C79A, 0xB9A56F, 0xD4A62A, 0xC0562B, 0xFF7A66);
        p("rose", "Rose", true, false, null,
          0x1C0B14, 0x261120, 0x32172A, 0x14070E, 0xFCEAF3, 0xE5AFC9, 0xCC85A9, 0xE0508C, 0xB47CF0, 0xFF7C8A);
        p("slate", "Slate", true, false, null,
          0x111418, 0x181C22, 0x20252C, 0x0C0F12, 0xE8ECF1, 0xAEB8C4, 0x8794A3, 0x60A5FA, 0x94A3B8, 0xFF7B7B);
        p("amoled", "AMOLED Black", true, true, null,
          0x000000, 0x070707, 0x101010, 0x000000, 0xF5F5F5, 0xBDBDBD, 0x9E9E9E, 0xA855F7, 0x22D3EE, 0xFF6B6B);
        p("sepia_night", "Sepia Night", true, false, null,
          0x1A130E, 0x221913, 0x2C2118, 0x130E0A, 0xEBDDC3, 0xCDB896, 0xA8916C, 0xD2A56B, 0xC77D4E, 0xFF8A65);
        p("hc_dark", "High Contrast Dark", true, false, null,
          0x000000, 0x000000, 0x0D0D0D, 0x000000, 0xFFFFFF, 0xFFFFFF, 0xE0E0E0, 0xFFD600, 0x00E5FF, 0xFF5252);
        p("lavender", "Lavender", false, false, "royal",
          0xF7F2FC, 0xFCFAFE, 0xF0E8F9, 0xFAF7FD, 0x1A0F2A, 0x4A2D78, 0x5E3F8A, 0x7B2FBE, 0xC0392B, 0xC0151A);
        p("paper", "Paper", false, false, "slate",
          0xF5F5F2, 0xFFFFFF, 0xEEEEEA, 0xFAFAF8, 0x1B1B1B, 0x404040, 0x565656, 0x2563EB, 0xB91C1C, 0xB91C1C);
        p("sunrise", "Sunrise", false, false, "gold",
          0xFFF6EC, 0xFFFBF5, 0xFDEBD6, 0xFFF9F1, 0x2B1706, 0x5A3815, 0x6F4A22, 0xD9730D, 0xC62828, 0xB71C1C);
        p("meadow", "Meadow", false, false, "forest",
          0xF1F8F2, 0xFAFDFA, 0xE4F1E6, 0xF6FBF7, 0x0E2214, 0x2A4A33, 0x3D6149, 0x15803D, 0x1D4ED8, 0xB91C1C);
        p("blush", "Blush", false, false, "rose",
          0xFDF1F6, 0xFFF9FC, 0xFAE3EE, 0xFEF5F9, 0x2A0E1B, 0x5B2742, 0x6E3A55, 0xBE185D, 0x6D28D9, 0xB91C1C);
        p("sepia", "Sepia", false, false, "sepia_night",
          0xF4ECD8, 0xF9F3E3, 0xEDE0C4, 0xF7F0DD, 0x3B2A1A, 0x5C4630, 0x6B5236, 0x8B5A2B, 0xA0432B, 0x9C2A12);
        p("sky", "Sky", false, false, "midnight",
          0xEFF6FD, 0xF8FBFE, 0xE0EDFA, 0xF3F8FD, 0x0B1C33, 0x27456B, 0x395A85, 0x1D5FD0, 0x0E7490, 0xB91C1C);
        p("hc_light", "High Contrast Light", false, false, "hc_dark",
          0xFFFFFF, 0xFFFFFF, 0xF2F2F2, 0xFFFFFF, 0x000000, 0x000000, 0x262626, 0x0033CC, 0xB00020, 0xC40000);
    }

    public static Preset find(String id) {
        if (id == null) return null;
        for (Preset p : ALL) if (p.id.equals(id)) return p;
        return null;
    }

    /**
     * Which preset (if any) these settings use. Returns null for a fully custom theme.
     * Settings saved by older versions have no preset id: they map to the preset
     * whose colors they were using, or to custom if the user had picked their own.
     */
    public static Preset resolve(AppSettings s) {
        String id = s.themePreset;
        if (CUSTOM_ID.equals(id)) return null;
        if (id != null) {
            Preset p = find(id);
            return p != null ? p : find(DEFAULT_ID);
        }
        boolean noColors = s.primaryColor == null || s.primaryColor.isEmpty()
                        || s.accentColor == null || s.accentColor.isEmpty();
        Preset royal = find(DEFAULT_ID);
        if (noColors) return modePreset(s.themeMode);
        int pc = ColorUtils.parseColor(s.primaryColor, 0);
        int ac = ColorUtils.parseColor(s.accentColor, 0);
        if (pc == royal.primary && ac == royal.accent) return modePreset(s.themeMode);
        return null; // user had chosen their own colors
    }

    private static Preset modePreset(String mode) {
        if ("light".equals(mode)) return find("lavender");
        if ("amoled".equals(mode)) return find("amoled");
        if ("sepia".equals(mode)) return find("sepia");
        return find(DEFAULT_ID);
    }
}
