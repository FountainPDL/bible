package com.fountainpdl.bible.dialogs;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.text.InputType;
import android.view.Gravity;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.fountainpdl.bible.utils.AppTheme;
import com.fountainpdl.bible.utils.WordAnnotationStore;

/**
 * Lets the user annotate a specific word or phrase (rather than a whole
 * verse) as a highlight, underline, or note. Uses a plain native
 * AlertDialog with a normal EditText -- text entry, editing, and paste
 * all work exactly as they do everywhere else in Android, since there is
 * no virtual DOM remounting the field on every keystroke the way the old
 * WebView build did.
 */
public class WordAnnotationDialog {

    public interface OnSavedListener { void onSaved(); }

    private final Context context;
    private final AppTheme theme;
    private final String book;
    private final int chapter;
    private final int verse;
    private final String selectedText;
    private final OnSavedListener listener;

    private String currentType = "highlight";
    private String currentColor = "#F1C40F";

    private static final String[] COLORS = {
        "#F1C40F", "#27AE60", "#2980B9", "#E67E22", "#8E44AD", "#16A085", "#E74C3C", "#1ABC9C"
    };

    public WordAnnotationDialog(Context context, AppTheme theme, String book, int chapter, int verse,
                                 String selectedText, OnSavedListener listener) {
        this.context = context;
        this.theme = theme;
        this.book = book;
        this.chapter = chapter;
        this.verse = verse;
        this.selectedText = selectedText;
        this.listener = listener;
    }

    public void show() {
        LinearLayout root = new LinearLayout(context);
        root.setOrientation(LinearLayout.VERTICAL);
        int pad = dp(20);
        root.setPadding(pad, pad, pad, pad);
        root.setBackgroundColor(theme.surface);

        TextView preview = new TextView(context);
        preview.setText(selectedText);
        preview.setTextColor(theme.text);
        preview.setBackgroundColor(theme.card);
        preview.setPadding(dp(12), dp(10), dp(12), dp(10));
        preview.setTextSize(14);
        root.addView(preview);

        TextView refLabel = new TextView(context);
        refLabel.setText("From: " + book + " " + chapter + ":" + verse);
        refLabel.setTextColor(theme.sub);
        refLabel.setTextSize(11);
        refLabel.setPadding(0, dp(6), 0, dp(14));
        root.addView(refLabel);

        TextView phraseLabel = sectionLabel("SPECIFIC WORD OR PHRASE (optional)");
        root.addView(phraseLabel);

        EditText phraseInput = new EditText(context);
        phraseInput.setHint("Leave blank to annotate full selection");
        phraseInput.setInputType(InputType.TYPE_CLASS_TEXT);
        phraseInput.setTextColor(theme.text);
        phraseInput.setHintTextColor(theme.sub);
        phraseInput.setBackground(inputBg());
        phraseInput.setPadding(dp(12), dp(10), dp(12), dp(10));
        root.addView(phraseInput);

        root.addView(sectionLabel("TYPE"));
        LinearLayout typeRow = new LinearLayout(context);
        typeRow.setOrientation(LinearLayout.HORIZONTAL);
        typeRow.setPadding(0, dp(6), 0, dp(14));
        String[] types = {"highlight", "underline", "note"};
        String[] typeLabels = {"Highlight", "Underline", "Note"};
        TextView[] typeButtons = new TextView[types.length];
        for (int i = 0; i < types.length; i++) {
            final String t = types[i];
            TextView btn = new TextView(context);
            btn.setText(typeLabels[i]);
            btn.setTextSize(12);
            btn.setPadding(dp(14), dp(6), dp(14), dp(6));
            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
            lp.setMarginEnd(dp(8));
            btn.setLayoutParams(lp);
            typeButtons[i] = btn;
            btn.setOnClickListener(v -> {
                currentType = t;
                for (int j = 0; j < typeButtons.length; j++) styleTypeButton(typeButtons[j], types[j].equals(currentType));
            });
            typeRow.addView(btn);
        }
        root.addView(typeRow);
        for (int j = 0; j < typeButtons.length; j++) styleTypeButton(typeButtons[j], types[j].equals(currentType));

        LinearLayout colorRow = new LinearLayout(context);
        colorRow.setOrientation(LinearLayout.HORIZONTAL);
        colorRow.setPadding(0, 0, 0, dp(14));
        TextView colorSectionLabel = sectionLabel("COLOR");
        root.addView(colorSectionLabel);
        View[] colorDots = new View[COLORS.length];
        for (int i = 0; i < COLORS.length; i++) {
            View dot = new View(context);
            int size = dp(30);
            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(size, size);
            lp.setMarginEnd(dp(8));
            dot.setLayoutParams(lp);
            GradientDrawable gd = new GradientDrawable();
            gd.setShape(GradientDrawable.OVAL);
            gd.setColor(Color.parseColor(COLORS[i]));
            dot.setBackground(gd);
            final String c = COLORS[i];
            colorDots[i] = dot;
            dot.setOnClickListener(v -> {
                currentColor = c;
                for (int j = 0; j < colorDots.length; j++) colorDots[j].setScaleX(COLORS[j].equals(currentColor) ? 1.25f : 1f);
                for (int j = 0; j < colorDots.length; j++) colorDots[j].setScaleY(COLORS[j].equals(currentColor) ? 1.25f : 1f);
            });
            colorRow.addView(dot);
        }
        root.addView(colorRow);

        TextView noteSectionLabel = sectionLabel("NOTE (if type = Note)");
        root.addView(noteSectionLabel);
        EditText noteInput = new EditText(context);
        noteInput.setHint("Greek meaning, cross-reference, personal insight…");
        noteInput.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_MULTI_LINE);
        noteInput.setMinLines(3);
        noteInput.setTextColor(theme.text);
        noteInput.setHintTextColor(theme.sub);
        noteInput.setBackground(inputBg());
        noteInput.setPadding(dp(12), dp(10), dp(12), dp(10));
        root.addView(noteInput);

        androidx.core.widget.NestedScrollView scroll = new androidx.core.widget.NestedScrollView(context);
        scroll.addView(root);

        AlertDialog dialog = new AlertDialog.Builder(context)
            .setTitle("Annotate Word / Phrase")
            .setView(scroll)
            .setPositiveButton("Apply", null)
            .setNegativeButton("Cancel", (d, w) -> d.dismiss())
            .create();

        dialog.setOnShowListener(d -> {
            dialog.getButton(Dialog.BUTTON_POSITIVE).setOnClickListener(v -> {
                String phrase = phraseInput.getText().toString().trim();
                String finalText = phrase.isEmpty() ? selectedText : phrase;
                WordAnnotationStore store = new WordAnnotationStore(context);
                WordAnnotationStore.WordAnnotation ann = new WordAnnotationStore.WordAnnotation();
                ann.book = book; ann.chapter = chapter; ann.verse = verse;
                ann.text = finalText; ann.type = currentType;
                if ("highlight".equals(currentType)) ann.color = currentColor;
                if ("note".equals(currentType)) ann.note = noteInput.getText().toString().trim();
                store.add(ann);
                if (listener != null) listener.onSaved();
                dialog.dismiss();
            });
        });

        dialog.show();
    }

    private void styleTypeButton(TextView btn, boolean active) {
        GradientDrawable gd = new GradientDrawable();
        gd.setCornerRadius(dp(16));
        if (active) {
            gd.setColor(withAlpha(theme.primary, 0x33));
            gd.setStroke(dp(1), theme.primary);
            btn.setTextColor(theme.primary);
        } else {
            gd.setColor(Color.TRANSPARENT);
            gd.setStroke(dp(1), theme.sub);
            btn.setTextColor(theme.sub);
        }
        btn.setBackground(gd);
    }

    private TextView sectionLabel(String text) {
        TextView tv = new TextView(context);
        tv.setText(text);
        tv.setTextSize(10.5f);
        tv.setTextColor(theme.sub);
        tv.setLetterSpacing(0.1f);
        tv.setPadding(0, dp(8), 0, dp(4));
        return tv;
    }

    private GradientDrawable inputBg() {
        GradientDrawable gd = new GradientDrawable();
        gd.setColor(theme.card);
        gd.setStroke(dp(1), withAlpha(theme.sub, 0x55));
        gd.setCornerRadius(dp(4));
        return gd;
    }

    private int withAlpha(int color, int alpha) {
        return Color.argb(alpha, Color.red(color), Color.green(color), Color.blue(color));
    }

    private int dp(int v) {
        return (int) (v * context.getResources().getDisplayMetrics().density);
    }
}
