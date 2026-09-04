package com.fountainpdl.bible.utils;

import android.graphics.Color;

public class ColorUtils {

    /** Convert hex color string to Android color int. Safe on malformed input. */
    public static int parseColor(String hex, int fallback) {
        try {
            return Color.parseColor(hex);
        } catch (Exception e) {
            return fallback;
        }
    }

    /** Returns [h, s, l] where h is 0-360, s and l are 0-100 */
    public static float[] toHsl(int color) {
        float r = Color.red(color) / 255f;
        float g = Color.green(color) / 255f;
        float b = Color.blue(color) / 255f;
        float max = Math.max(r, Math.max(g, b));
        float min = Math.min(r, Math.min(g, b));
        float h = 0, s, l = (max + min) / 2f;

        if (max == min) {
            s = 0;
        } else {
            float d = max - min;
            s = l > 0.5f ? d / (2f - max - min) : d / (max + min);
            if (max == r) h = (g - b) / d + (g < b ? 6 : 0);
            else if (max == g) h = (b - r) / d + 2;
            else h = (r - g) / d + 4;
            h /= 6f;
        }
        return new float[]{h * 360f, s * 100f, l * 100f};
    }

    /** Build a color from h (0-360), s (0-100), l (0-100) */
    public static int fromHsl(float h, float s, float l) {
        s /= 100f; l /= 100f;
        float c = (1 - Math.abs(2 * l - 1)) * s;
        float x = c * (1 - Math.abs((h / 60f) % 2 - 1));
        float m = l - c / 2f;
        float r, g, b;
        if (h < 60)       { r = c; g = x; b = 0; }
        else if (h < 120) { r = x; g = c; b = 0; }
        else if (h < 180) { r = 0; g = c; b = x; }
        else if (h < 240) { r = 0; g = x; b = c; }
        else if (h < 300) { r = x; g = 0; b = c; }
        else              { r = c; g = 0; b = x; }
        int ri = Math.round((r + m) * 255);
        int gi = Math.round((g + m) * 255);
        int bi = Math.round((b + m) * 255);
        return Color.rgb(
            Math.max(0, Math.min(255, ri)),
            Math.max(0, Math.min(255, gi)),
            Math.max(0, Math.min(255, bi))
        );
    }

    /** Tint a base color by mixing toward a primary hue at a given lightness. Used for backgrounds. */
    public static int tintFromPrimary(int primaryColor, float targetLightness, float maxSaturation) {
        float[] hsl = toHsl(primaryColor);
        float s = Math.min(hsl[1], maxSaturation);
        return fromHsl(hsl[0], s, targetLightness);
    }

    /** Apply alpha (0-255) to a color */
    public static int withAlpha(int color, int alpha) {
        return Color.argb(alpha, Color.red(color), Color.green(color), Color.blue(color));
    }

    /** Apply alpha as a hex-style percentage string like "44" (0x44/255) */
    public static int withAlphaHex(int color, String alphaHex) {
        int alpha = Integer.parseInt(alphaHex, 16);
        return withAlpha(color, alpha);
    }

    public static int mix(int color1, int color2, float ratio) {
        float ir = 1f - ratio;
        int r = (int) (Color.red(color1) * ir + Color.red(color2) * ratio);
        int g = (int) (Color.green(color1) * ir + Color.green(color2) * ratio);
        int b = (int) (Color.blue(color1) * ir + Color.blue(color2) * ratio);
        return Color.rgb(r, g, b);
    }

    public static boolean isDark(int backgroundColor) {
        double luminance = (0.299 * Color.red(backgroundColor) +
                             0.587 * Color.green(backgroundColor) +
                             0.114 * Color.blue(backgroundColor)) / 255;
        return luminance < 0.5;
    }
}
