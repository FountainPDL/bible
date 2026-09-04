package com.fountainpdl.bible.dialogs;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.graphics.drawable.GradientDrawable;
import android.text.InputType;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.core.widget.NestedScrollView;

import com.fountainpdl.bible.MainActivity;
import com.fountainpdl.bible.models.Note;
import com.fountainpdl.bible.utils.AppTheme;
import com.fountainpdl.bible.utils.PrefsManager;

import java.util.List;

/**
 * Create or edit a Note. Uses a single plain AlertDialog with normal
 * native EditText fields -- this is the fix for two bugs from the old
 * WebView build: the title field losing its text when focus moved
 * elsewhere (caused by React recreating the input component on every
 * render), and paste not working reliably (also a side effect of the
 * same remounting). Neither issue is possible here: the EditTexts are
 * created once and never rebuilt while the dialog is open, so Android's
 * normal text-retention and long-press-paste behavior just works.
 */
public class NoteDialog {

    public interface OnSavedListener { void onSaved(); }

    private final Context context;
    private final AppTheme theme;
    private final String linkedRef;
    private final Note editNote;
    private final OnSavedListener listener;

    public NoteDialog(Context context, AppTheme theme, String linkedRef, Note editNote, OnSavedListener listener) {
        this.context = context;
        this.theme = theme;
        this.linkedRef = linkedRef;
        this.editNote = editNote;
        this.listener = listener;
    }

    public void show() {
        LinearLayout root = new LinearLayout(context);
        root.setOrientation(LinearLayout.VERTICAL);
        int pad = dp(20);
        root.setPadding(pad, dp(4), pad, pad);
        root.setBackgroundColor(theme.surface);

        root.addView(label("TOPIC / TITLE"));
        EditText topicInput = makeInput("Faith, Salvation, The Cross…", false);
        if (editNote != null) topicInput.setText(editNote.topic);
        root.addView(topicInput);

        root.addView(label("NOTE"));
        EditText textInput = makeInput("Your thoughts, observations, revelations…", true);
        if (editNote != null) textInput.setText(editNote.text);
        root.addView(textInput);

        root.addView(label("VERSE REFERENCES"));
        LinearLayout refRow = new LinearLayout(context);
        refRow.setOrientation(LinearLayout.HORIZONTAL);
        EditText refsInput = makeInput("John 3:16; Romans 8:28", false);
        LinearLayout.LayoutParams refLp = new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f);
        refsInput.setLayoutParams(refLp);
        if (editNote != null && editNote.refs != null) refsInput.setText(editNote.refs);
        refRow.addView(refsInput);

        TextView pickBtn = new TextView(context);
        pickBtn.setText("Pick");
        pickBtn.setTextColor(theme.primary);
        pickBtn.setTextSize(12);
        pickBtn.setPadding(dp(12), dp(10), dp(12), dp(10));
        GradientDrawable pickBg = new GradientDrawable();
        pickBg.setColor(withAlpha(theme.primary, 0x20));
        pickBg.setStroke(dp(1), theme.primary);
        pickBg.setCornerRadius(dp(4));
        pickBtn.setBackground(pickBg);
        LinearLayout.LayoutParams pickLp = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        pickLp.setMarginStart(dp(6));
        pickBtn.setLayoutParams(pickLp);
        refRow.addView(pickBtn);
        root.addView(refRow);

        if (linkedRef != null && !linkedRef.isEmpty()) {
            TextView linkedLabel = new TextView(context);
            linkedLabel.setText("Linked: " + linkedRef);
            linkedLabel.setTextColor(theme.primary);
            linkedLabel.setTextSize(11);
            linkedLabel.setPadding(0, dp(8), 0, 0);
            root.addView(linkedLabel);
        }

        pickBtn.setOnClickListener(v -> {
            MainActivity activity = context instanceof MainActivity ? (MainActivity) context : null;
            com.fountainpdl.bible.models.AppSettings s = activity != null ? activity.getPrefsManager().getSettings() : new com.fountainpdl.bible.models.AppSettings();
            new NavigatePickerDialog(context, theme, s, (book, chapter, verse) -> {
                String ref = verse != null ? (book + " " + chapter + ":" + verse) : (book + " " + chapter + ":1");
                String existing = refsInput.getText().toString().trim();
                refsInput.setText(existing.isEmpty() ? ref : existing + "; " + ref);
                refsInput.setSelection(refsInput.getText().length());
            }).show();
        });

        NestedScrollView scroll = new NestedScrollView(context);
        scroll.addView(root);

        AlertDialog dialog = new AlertDialog.Builder(context)
            .setTitle(editNote != null ? "Edit Note" : "New Note")
            .setView(scroll)
            .setPositiveButton(editNote != null ? "Save Changes" : "Save Note", null)
            .setNegativeButton("Cancel", (d, w) -> d.dismiss())
            .create();

        dialog.setOnShowListener(d -> {
            dialog.getButton(Dialog.BUTTON_POSITIVE).setOnClickListener(v -> {
                String topic = topicInput.getText().toString().trim();
                String text = textInput.getText().toString().trim();
                if (topic.isEmpty() || text.isEmpty()) {
                    if (topic.isEmpty()) topicInput.setError("Required");
                    if (text.isEmpty()) textInput.setError("Required");
                    return;
                }
                String refs = refsInput.getText().toString().trim();

                MainActivity activity = context instanceof MainActivity ? (MainActivity) context : null;
                if (activity == null) { dialog.dismiss(); return; }
                PrefsManager prefs = activity.getPrefsManager();
                List<Note> notes = prefs.getNotes();

                if (editNote != null) {
                    for (Note n : notes) {
                        if (n.id.equals(editNote.id)) { n.topic = topic; n.text = text; n.refs = refs; break; }
                    }
                } else {
                    Note n = new Note();
                    n.topic = topic; n.text = text; n.refs = refs;
                    notes.add(n);
                }
                prefs.saveNotes(notes);
                if (listener != null) listener.onSaved();
                dialog.dismiss();
            });
        });

        dialog.show();
    }

    private EditText makeInput(String hint, boolean multiline) {
        EditText et = new EditText(context);
        et.setHint(hint);
        et.setHintTextColor(theme.sub);
        et.setTextColor(theme.text);
        et.setBackground(inputBg());
        et.setPadding(dp(12), dp(10), dp(12), dp(10));
        et.setTextSize(14);
        if (multiline) {
            et.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_MULTI_LINE | InputType.TYPE_TEXT_FLAG_CAP_SENTENCES);
            et.setMinLines(4);
            et.setGravity(android.view.Gravity.TOP);
        } else {
            et.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_CAP_SENTENCES);
            et.setMaxLines(1);
        }
        // Native EditText -- long-press context menu (Cut/Copy/Paste/Select All) works
        // automatically; nothing here disables it.
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        lp.setMargins(0, 0, 0, dp(4));
        et.setLayoutParams(lp);
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
