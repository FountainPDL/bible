package com.fountainpdl.bible.dialogs;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.graphics.drawable.GradientDrawable;
import android.text.InputType;
import android.view.Gravity;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.core.widget.NestedScrollView;

import com.fountainpdl.bible.MainActivity;
import com.fountainpdl.bible.models.AppSettings;
import com.fountainpdl.bible.models.Sermon;
import com.fountainpdl.bible.models.SermonBlock;
import com.fountainpdl.bible.utils.AppTheme;
import com.fountainpdl.bible.utils.BibleDataManager;
import com.fountainpdl.bible.utils.PrefsManager;

import java.util.ArrayList;
import java.util.List;

/**
 * Create or edit a Sermon/Study. Each verse block stores only its
 * reference string (e.g. "John 3:16-18") -- never the resolved text.
 * Display text is fetched live from BibleDataManager using whatever
 * translation is currently active, both while editing and when viewing
 * a saved sermon later. This is the fix for sermons being "locked" to
 * KJV: the old build baked the verse text in at save time, so a later
 * translation switch had nothing to update. Now there's nothing baked
 * in to go stale.
 */
public class SermonDialog {

    public interface OnSavedListener { void onSaved(); }

    private final Context context;
    private final AppTheme theme;
    private final BibleDataManager bibleData;
    private final String translation;
    private final Sermon editSermon;
    private final OnSavedListener listener;

    private final List<SermonBlock> blocks = new ArrayList<>();
    private LinearLayout blockListContainer;

    public SermonDialog(Context context, AppTheme theme, BibleDataManager bibleData, String translation,
                         Sermon editSermon, OnSavedListener listener) {
        this.context = context;
        this.theme = theme;
        this.bibleData = bibleData;
        this.translation = translation;
        this.editSermon = editSermon;
        this.listener = listener;
        if (editSermon != null) blocks.addAll(editSermon.blocks);
    }

    public void show() {
        LinearLayout root = new LinearLayout(context);
        root.setOrientation(LinearLayout.VERTICAL);
        int pad = dp(20);
        root.setPadding(pad, dp(4), pad, pad);
        root.setBackgroundColor(theme.surface);

        root.addView(label("TITLE / TOPIC"));
        EditText titleInput = makeInput("Sermon title…", false);
        if (editSermon != null) titleInput.setText(editSermon.title);
        root.addView(titleInput);

        root.addView(label("ADD SCRIPTURE"));
        TextView pickerBtn = new TextView(context);
        pickerBtn.setText("Pick Verse Reference");
        pickerBtn.setTextColor(theme.primary);
        pickerBtn.setGravity(Gravity.CENTER);
        pickerBtn.setPadding(dp(12), dp(11), dp(12), dp(11));
        GradientDrawable pBg = new GradientDrawable();
        pBg.setColor(withAlpha(theme.primary, 0x1A));
        pBg.setStroke(dp(1), theme.primary);
        pBg.setCornerRadius(dp(4));
        pickerBtn.setBackground(pBg);
        root.addView(pickerBtn);

        LinearLayout manualRow = new LinearLayout(context);
        manualRow.setOrientation(LinearLayout.HORIZONTAL);
        manualRow.setPadding(0, dp(8), 0, 0);
        EditText manualRef = makeInput("Or type: John 3:16-18", false);
        LinearLayout.LayoutParams manualLp = new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f);
        manualRef.setLayoutParams(manualLp);
        manualRow.addView(manualRef);
        TextView addRefBtn = smallBtn("+ Add");
        manualRow.addView(addRefBtn);
        root.addView(manualRow);

        root.addView(label("ADD COMMENTARY"));
        LinearLayout textRow = new LinearLayout(context);
        textRow.setOrientation(LinearLayout.HORIZONTAL);
        EditText commentaryInput = makeInput("Your commentary, notes, application…", true);
        LinearLayout.LayoutParams cLp = new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f);
        commentaryInput.setLayoutParams(cLp);
        textRow.addView(commentaryInput);
        TextView addTextBtn = smallBtn("+ Add");
        addTextBtn.setPadding(dp(12), dp(11), dp(12), dp(11));
        LinearLayout.LayoutParams addTextLp = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        addTextLp.gravity = Gravity.TOP;
        addTextLp.setMarginStart(dp(6));
        addTextBtn.setLayoutParams(addTextLp);
        textRow.addView(addTextBtn);
        root.addView(textRow);

        root.addView(label("DOCUMENT"));
        blockListContainer = new LinearLayout(context);
        blockListContainer.setOrientation(LinearLayout.VERTICAL);
        root.addView(blockListContainer);
        renderBlocks();

        pickerBtn.setOnClickListener(v -> {
            MainActivity activity = context instanceof MainActivity ? (MainActivity) context : null;
            AppSettings s = activity != null ? activity.getPrefsManager().getSettings() : new AppSettings();
            new NavigatePickerDialog(context, theme, s, (book, chapter, verse) -> {
                String ref = verse != null ? (book + " " + chapter + ":" + verse) : (book + " " + chapter + ":1");
                blocks.add(SermonBlock.verse(ref));
                renderBlocks();
            }).show();
        });

        addRefBtn.setOnClickListener(v -> {
            String ref = manualRef.getText().toString().trim();
            if (!ref.isEmpty()) {
                blocks.add(SermonBlock.verse(ref));
                manualRef.setText("");
                renderBlocks();
            }
        });

        addTextBtn.setOnClickListener(v -> {
            String txt = commentaryInput.getText().toString().trim();
            if (!txt.isEmpty()) {
                blocks.add(SermonBlock.text(txt));
                commentaryInput.setText("");
                renderBlocks();
            }
        });

        NestedScrollView scroll = new NestedScrollView(context);
        scroll.addView(root);

        AlertDialog dialog = new AlertDialog.Builder(context)
            .setTitle(editSermon != null ? "Edit Sermon" : "Sermon / Study Builder")
            .setView(scroll)
            .setPositiveButton(editSermon != null ? "Save Changes" : "Save Study", null)
            .setNegativeButton("Cancel", (d, w) -> d.dismiss())
            .create();

        dialog.setOnShowListener(d -> {
            dialog.getButton(Dialog.BUTTON_POSITIVE).setOnClickListener(v -> {
                String title = titleInput.getText().toString().trim();
                if (title.isEmpty()) { titleInput.setError("Required"); return; }

                MainActivity activity = context instanceof MainActivity ? (MainActivity) context : null;
                if (activity == null) { dialog.dismiss(); return; }
                PrefsManager prefs = activity.getPrefsManager();
                List<Sermon> sermons = prefs.getSermons();

                if (editSermon != null) {
                    for (Sermon s : sermons) {
                        if (s.id.equals(editSermon.id)) { s.title = title; s.blocks = new ArrayList<>(blocks); break; }
                    }
                } else {
                    Sermon s = new Sermon();
                    s.title = title;
                    s.blocks = new ArrayList<>(blocks);
                    sermons.add(s);
                }
                prefs.saveSermons(sermons);
                if (listener != null) listener.onSaved();
                dialog.dismiss();
            });
        });

        dialog.show();
    }

    private void renderBlocks() {
        blockListContainer.removeAllViews();
        for (int i = 0; i < blocks.size(); i++) {
            SermonBlock bl = blocks.get(i);
            final int idx = i;

            LinearLayout item = new LinearLayout(context);
            item.setOrientation(LinearLayout.VERTICAL);
            item.setPadding(dp(10), dp(8), dp(10), dp(8));
            GradientDrawable itemBg = new GradientDrawable();
            itemBg.setColor(theme.card);
            itemBg.setCornerRadius(dp(4));
            int borderColor = "verse".equals(bl.type) ? theme.accent : theme.primary;
            // left accent bar simulated with padding + colored corner not directly supported;
            // use a thin colored top strip instead for simplicity
            itemBg.setStroke(dp(1), withAlpha(borderColor, 0x55));
            item.setBackground(itemBg);
            LinearLayout.LayoutParams itemLp = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            itemLp.setMargins(0, dp(4), 0, dp(4));
            item.setLayoutParams(itemLp);

            if ("verse".equals(bl.type)) {
                TextView refLabel = new TextView(context);
                refLabel.setText(bl.ref);
                refLabel.setTextColor(theme.accent);
                refLabel.setTextSize(11);
                refLabel.setPadding(0, 0, 0, dp(4));
                item.addView(refLabel);

                TextView textView = new TextView(context);
                String resolved = bibleData.resolveRefText(bl.ref, translation);
                textView.setText(resolved);
                textView.setTextColor(theme.text);
                textView.setTextSize(13);
                textView.setTypeface(null, android.graphics.Typeface.ITALIC);
                item.addView(textView);
            } else {
                TextView textView = new TextView(context);
                textView.setText(bl.content);
                textView.setTextColor(theme.text);
                textView.setTextSize(13.5f);
                item.addView(textView);
            }

            LinearLayout controls = new LinearLayout(context);
            controls.setOrientation(LinearLayout.HORIZONTAL);
            controls.setPadding(0, dp(6), 0, 0);
            if (idx > 0) controls.addView(ctrlBtn("↑", () -> { java.util.Collections.swap(blocks, idx, idx - 1); renderBlocks(); }));
            if (idx < blocks.size() - 1) controls.addView(ctrlBtn("↓", () -> { java.util.Collections.swap(blocks, idx, idx + 1); renderBlocks(); }));
            controls.addView(ctrlBtn("Remove", () -> { blocks.remove(idx); renderBlocks(); }));
            item.addView(controls);

            blockListContainer.addView(item);
        }
    }

    private TextView ctrlBtn(String text, Runnable onClick) {
        TextView tv = new TextView(context);
        tv.setText(text);
        tv.setTextSize(10.5f);
        tv.setTextColor(theme.sub);
        tv.setPadding(dp(8), dp(3), dp(8), dp(3));
        GradientDrawable bg = new GradientDrawable();
        bg.setStroke(dp(1), withAlpha(theme.sub, 0x55));
        bg.setCornerRadius(dp(10));
        tv.setBackground(bg);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        lp.setMarginEnd(dp(6));
        tv.setLayoutParams(lp);
        tv.setOnClickListener(v -> onClick.run());
        return tv;
    }

    private TextView smallBtn(String text) {
        TextView tv = new TextView(context);
        tv.setText(text);
        tv.setTextColor(theme.primary);
        tv.setTextSize(12);
        tv.setPadding(dp(12), dp(8), dp(12), dp(8));
        tv.setGravity(Gravity.CENTER);
        GradientDrawable bg = new GradientDrawable();
        bg.setStroke(dp(1), theme.primary);
        bg.setCornerRadius(dp(4));
        tv.setBackground(bg);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        lp.setMarginStart(dp(6));
        tv.setLayoutParams(lp);
        return tv;
    }

    private EditText makeInput(String hint, boolean multiline) {
        EditText et = new EditText(context);
        et.setHint(hint);
        et.setHintTextColor(theme.sub);
        et.setTextColor(theme.text);
        et.setBackground(inputBg());
        et.setPadding(dp(12), dp(10), dp(12), dp(10));
        et.setTextSize(13.5f);
        if (multiline) {
            et.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_MULTI_LINE | InputType.TYPE_TEXT_FLAG_CAP_SENTENCES);
            et.setMinLines(3);
            et.setGravity(Gravity.TOP);
        } else {
            et.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_CAP_SENTENCES);
            et.setMaxLines(1);
        }
        return et;
    }

    private TextView label(String text) {
        TextView tv = new TextView(context);
        tv.setText(text);
        tv.setTextSize(10.5f);
        tv.setTextColor(theme.sub);
        tv.setLetterSpacing(0.1f);
        tv.setPadding(0, dp(10), 0, dp(4));
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
        return android.graphics.Color.argb(alpha, android.graphics.Color.red(color), android.graphics.Color.green(color), android.graphics.Color.blue(color));
    }

    private int dp(int v) { return (int) (v * context.getResources().getDisplayMetrics().density); }
}
