package com.fountainpdl.bible.utils;

import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;

/**
 * Small helpers that paint views from AppTheme. The XML drawables used to hard-code
 * dark-theme colors, which is why light mode and custom colors looked broken;
 * these build the same shapes from the live theme instead.
 */
public final class ThemeHelper {
    private ThemeHelper() {}

    public static int dp(Context c, float v) {
        return Math.round(v * c.getResources().getDisplayMetrics().density);
    }

    public static GradientDrawable shape(Context c, int fill, int stroke, float radiusDp) {
        GradientDrawable d = new GradientDrawable();
        d.setColor(fill);
        if (stroke != Color.TRANSPARENT) d.setStroke(dp(c, 1), stroke);
        d.setCornerRadius(dp(c, radiusDp));
        return d;
    }

    /** Rounded surface with a themed border (cards, bars). */
    public static void card(View v, AppTheme t, float radiusDp) {
        v.setBackground(shape(v.getContext(), t.card, t.border, radiusDp));
    }

    public static void bar(View v, AppTheme t, float radiusDp) {
        v.setBackground(shape(v.getContext(), t.navBg, t.border, radiusDp));
    }

    /** Outlined pill button. */
    public static void pill(TextView v, AppTheme t) {
        v.setBackground(shape(v.getContext(), Color.TRANSPARENT, t.border, 20));
        v.setTextColor(t.primaryFg);
    }

    /** Filled (selected) pill. */
    public static void pillFilled(TextView v, AppTheme t) {
        v.setBackground(shape(v.getContext(), t.primary, Color.TRANSPARENT, 20));
        v.setTextColor(ColorUtils.luminance(t.primary) > 0.45 ? 0xFF000000 : 0xFFFFFFFF);
    }

    public static void input(EditText e, AppTheme t) {
        e.setBackground(shape(e.getContext(), t.card, t.border, 12));
        e.setTextColor(t.text);
        e.setHintTextColor(t.sub);
        e.setBackgroundTintList(null);
    }

    /** Text color for content drawn on top of a solid t.primary fill. */
    public static int onPrimary(AppTheme t) {
        return ColorUtils.luminance(t.primary) > 0.45 ? 0xFF000000 : 0xFFFFFFFF;
    }

    /** Recursively colors every TextView under root with the given color. */
    public static void textColorTree(View root, int color) {
        if (root instanceof TextView && !(root instanceof EditText)) ((TextView) root).setTextColor(color);
        if (root instanceof ViewGroup) {
            ViewGroup g = (ViewGroup) root;
            for (int i = 0; i < g.getChildCount(); i++) textColorTree(g.getChildAt(i), color);
        }
    }

    public static ColorStateList stateList(int selected, int normal) {
        return new ColorStateList(
            new int[][]{ new int[]{android.R.attr.state_checked}, new int[]{} },
            new int[]{ selected, normal });
    }
}
