package com.fountainpdl.bible.utils;

import com.fountainpdl.bible.models.AppSettings;

import java.util.Calendar;

/**
 * A fully-resolved set of colors for the current settings.
 * Recomputed whenever primaryColor, accentColor, or themeMode changes.
 * All fragments read from this rather than static XML colors, which is
 * what makes the theme "reactive" -- changing a color updates every
 * screen immediately.
 *
 * primary / accent are the raw brand colors: use them for fills, strokes and
 * gradients. primaryFg / accentFg are the same hues nudged until they are
 * readable on the surface color: use those whenever the color is TEXT or an
 * icon, otherwise a pale or very dark preset becomes unreadable.
 */
public class AppTheme {
    public int bg, surface, card, border, text, textMuted, sub;
    public int navBg, navBorder, selectedBg, highlightBg;
    public int primary, accent, redWord;
    public int primaryFg, accentFg;
    public int accentText;
    public boolean isDark, isAmoled;

    /** The theme mode actually in force right now (honours Auto Night Mode). */
    public static String effectiveMode(AppSettings s) {
        String mode = s.themeMode == null ? "dark" : s.themeMode;
        if (s.autoNight && "light".equals(mode)) {
            int hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY);
            if (hour >= s.nightHour || hour < 6) return "dark";
        }
        return mode;
    }

    public static boolean isDarkMode(AppSettings s) {
        String mode = effectiveMode(s);
        return "dark".equals(mode) || "amoled".equals(mode);
    }

    public static AppTheme build(AppSettings s) {
        AppTheme t = new AppTheme();
        String mode = effectiveMode(s);
        boolean dark = "dark".equals(mode) || "amoled".equals(mode);
        boolean amoled = "amoled".equals(mode);

        int primary = ColorUtils.parseColor(s.primaryColor, 0xFF7B2FBE);
        int accent  = ColorUtils.parseColor(s.accentColor, 0xFFC0392B);
        float[] pHsl = ColorUtils.toHsl(primary);
        float[] aHsl = ColorUtils.toHsl(accent);

        if (amoled) {
            t.bg = 0xFF000000;
            t.surface = 0xFF080808;
            t.card = 0xFF101010;
            t.navBg = 0xFF040404;
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

        t.border     = ColorUtils.withAlpha(primary, dark ? 0x44 : 0x28);
        t.navBorder  = ColorUtils.withAlpha(primary, dark ? 0x33 : 0x18);
        t.text       = dark ? 0xFFF2EAFF : 0xFF100820;
        t.textMuted  = dark ? 0xFFC4A8E0 : 0xFF3A1870;
        t.sub        = dark ? 0xFF9B72C8 : 0xFF5A3080;
        t.selectedBg = ColorUtils.withAlpha(primary, dark ? 0x38 : 0x22);
        t.highlightBg= dark ? ColorUtils.fromHsl(aHsl[0], Math.min(aHsl[1], 70), 18)
                             : ColorUtils.fromHsl(aHsl[0], Math.min(aHsl[1], 80), 92);

        t.primary   = primary;
        t.accent    = accent;
        // Text / icon variants that are guaranteed readable on the surface color.
        t.primaryFg = ColorUtils.ensureContrast(primary, t.surface, 4.5);
        t.accentFg  = ColorUtils.ensureContrast(accent, t.surface, 4.5);
        // Muted text also has to clear the bar on both surface and card.
        t.sub       = ColorUtils.ensureContrast(t.sub, t.card, 4.5);
        t.redWord   = dark ? 0xFFFF7070 : 0xFFCC1111;
        t.accentText= 0xFFFFFFFF;
        t.isDark    = dark;
        t.isAmoled  = amoled;
        return t;
    }
}
