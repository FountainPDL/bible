package com.fountainpdl.bible.utils;

import com.fountainpdl.bible.models.AppSettings;

import java.util.Calendar;

/**
 * A fully-resolved set of colors for the current settings.
 * Rebuilt whenever a theme-related setting changes; every screen paints itself
 * from this object, which is what makes theme changes instant and consistent.
 *
 * primary / accent are the raw brand colors: use them for fills, strokes and
 * gradients. primaryFg / accentFg are the same hues nudged until they are
 * readable on the surface color: use those whenever the color is TEXT or an
 * icon. onPrimary / onAccent / onColor(bg) pick readable text for a solid fill.
 */
public class AppTheme {
    public int bg, surface, card, border, text, textMuted, sub;
    public int navBg, navBorder, selectedBg, highlightBg;
    public int primary, accent, redWord;
    public int primaryFg, accentFg;
    public int onPrimary, onAccent;
    public int ink, paper, success, danger;
    public boolean isDark, isAmoled;
    public String presetId;

    private static boolean isNight(AppSettings s) {
        if (!s.autoNight) return false;
        int hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY);
        return hour >= s.nightHour || hour < 6;
    }

    /** The preset in force right now, after Auto Night Mode; null means custom colors. */
    public static ThemePresets.Preset activePreset(AppSettings s) {
        ThemePresets.Preset p = ThemePresets.resolve(s);
        if (p != null && !p.dark && isNight(s) && p.nightId != null) {
            ThemePresets.Preset n = ThemePresets.find(p.nightId);
            if (n != null) return n;
        }
        return p;
    }

    /** A string that changes exactly when the visible theme changes (used to detect Auto Night flips). */
    public static String effectiveMode(AppSettings s) {
        ThemePresets.Preset p = activePreset(s);
        if (p != null) return p.id;
        String mode = s.themeMode == null ? "dark" : s.themeMode;
        if ("light".equals(mode) && isNight(s)) mode = "dark";
        return ThemePresets.CUSTOM_ID + ":" + mode;
    }

    public static boolean isDarkMode(AppSettings s) {
        return build(s).isDark;
    }

    public static AppTheme build(AppSettings s) {
        AppTheme t = new AppTheme();
        ThemePresets.Preset p = activePreset(s);

        int primary, accent, redBase;
        if (p != null) {
            t.presetId = p.id;
            t.isDark = p.dark; t.isAmoled = p.amoled;
            t.bg = p.bg; t.surface = p.surface; t.card = p.card; t.navBg = p.nav;
            t.text = p.text; t.textMuted = p.muted; t.sub = p.sub;
            primary = p.primary; accent = p.accent; redBase = p.red;
        } else {
            t.presetId = ThemePresets.CUSTOM_ID;
            String mode = effectiveMode(s).substring(ThemePresets.CUSTOM_ID.length() + 1);
            boolean amoled = "amoled".equals(mode);
            boolean dark = "dark".equals(mode) || amoled;
            ThemePresets.Preset fallback = ThemePresets.find(ThemePresets.DEFAULT_ID);
            primary = ColorUtils.parseColor(s.primaryColor, fallback.primary);
            accent = ColorUtils.parseColor(s.accentColor, fallback.accent);
            float[] pHsl = ColorUtils.toHsl(primary);
            ThemePresets.Preset base = ThemePresets.find(
                "sepia".equals(mode) ? "sepia" : amoled ? "amoled" : dark ? ThemePresets.DEFAULT_ID : "lavender");
            t.isDark = dark; t.isAmoled = amoled;
            if (amoled || "sepia".equals(mode)) {
                t.bg = base.bg; t.surface = base.surface; t.card = base.card; t.navBg = base.nav;
            } else if (dark) {
                t.bg      = ColorUtils.fromHsl(pHsl[0], Math.min(pHsl[1], 60), 7);
                t.surface = ColorUtils.fromHsl(pHsl[0], Math.min(pHsl[1], 55), 10);
                t.card    = ColorUtils.fromHsl(pHsl[0], Math.min(pHsl[1], 50), 14);
                t.navBg   = ColorUtils.fromHsl(pHsl[0], Math.min(pHsl[1], 60), 5);
            } else {
                t.bg      = ColorUtils.fromHsl(pHsl[0], Math.min(pHsl[1], 22), 97);
                t.surface = ColorUtils.fromHsl(pHsl[0], Math.min(pHsl[1], 16), 99);
                t.card    = ColorUtils.fromHsl(pHsl[0], Math.min(pHsl[1], 20), 95);
                t.navBg   = ColorUtils.fromHsl(pHsl[0], Math.min(pHsl[1], 16), 98);
            }
            t.text = base.text; t.textMuted = base.muted; t.sub = base.sub;
            redBase = base.red;
        }

        t.ink = ThemePresets.INK;
        t.paper = ThemePresets.PAPER;
        t.success = ThemePresets.SUCCESS;

        t.border     = ColorUtils.withAlpha(primary, t.isDark ? 0x44 : 0x28);
        t.navBorder  = ColorUtils.withAlpha(primary, t.isDark ? 0x33 : 0x18);
        t.selectedBg = ColorUtils.withAlpha(primary, t.isDark ? 0x38 : 0x22);
        float[] aHsl = ColorUtils.toHsl(accent);
        t.highlightBg = t.isDark ? ColorUtils.fromHsl(aHsl[0], Math.min(aHsl[1], 70), 18)
                                 : ColorUtils.fromHsl(aHsl[0], Math.min(aHsl[1], 80), 92);

        t.primary   = primary;
        t.accent    = accent;
        t.primaryFg = ColorUtils.ensureContrast(primary, t.surface, 4.5);
        t.accentFg  = ColorUtils.ensureContrast(accent, t.surface, 4.5);
        t.sub       = ColorUtils.ensureContrast(t.sub, t.card, 4.5);
        t.onPrimary = t.onColor(primary);
        t.onAccent  = t.onColor(accent);

        int red = (s.redLetterColor != null && !s.redLetterColor.isEmpty())
            ? ColorUtils.parseColor(s.redLetterColor, redBase) : redBase;
        t.redWord = ColorUtils.ensureContrast(red, t.bg, 4.5);
        t.danger  = t.redWord;
        return t;
    }

    /** Readable text color (ink or paper) for content drawn on a solid fill of the given color. */
    public int onColor(int fill) {
        return ColorUtils.contrast(ink, fill) >= ColorUtils.contrast(paper, fill) ? ink : paper;
    }

    /** Same, for a gradient between two colors. */
    public int onGradient(int a, int b) {
        return onColor(ColorUtils.mix(a, b, 0.5f));
    }
}
