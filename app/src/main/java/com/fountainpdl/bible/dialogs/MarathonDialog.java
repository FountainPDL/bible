package com.fountainpdl.bible.dialogs;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.graphics.drawable.GradientDrawable;
import android.text.InputType;
import android.view.Gravity;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.GridLayout;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.core.widget.NestedScrollView;

import com.fountainpdl.bible.MainActivity;
import com.fountainpdl.bible.models.Marathon;
import com.fountainpdl.bible.utils.AppTheme;
import com.fountainpdl.bible.utils.BooksData;
import com.fountainpdl.bible.utils.PrefsManager;
import com.fountainpdl.bible.utils.ReadingPlans;

import java.util.ArrayList;
import java.util.List;

public class MarathonDialog {

    public interface OnSavedListener { void onSaved(); }

    private final Context context;
    private final AppTheme theme;
    private final OnSavedListener listener;
    private String selectedPlan = "Full Bible (OT then NT)";

    public MarathonDialog(Context context, AppTheme theme, OnSavedListener listener) {
        this.context = context;
        this.theme = theme;
        this.listener = listener;
    }

    public void show() {
        LinearLayout root = new LinearLayout(context);
        root.setOrientation(LinearLayout.VERTICAL);
        int pad = dp(20);
        root.setPadding(pad, dp(4), pad, pad);
        root.setBackgroundColor(theme.surface);

        TextView nameLabel = label("MARATHON NAME");
        root.addView(nameLabel);
        EditText nameInput = new EditText(context);
        nameInput.setHint("e.g. 2025 Bible Read-Through");
        nameInput.setHintTextColor(theme.sub);
        nameInput.setTextColor(theme.text);
        nameInput.setBackground(inputBg());
        nameInput.setPadding(dp(12), dp(10), dp(12), dp(10));
        nameInput.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_CAP_SENTENCES);
        root.addView(nameInput);

        root.addView(label("READING PLAN"));
        GridLayout planGrid = new GridLayout(context);
        planGrid.setColumnCount(2);
        List<String> planNames = ReadingPlans.getPlanNames();
        TextView[] planButtons = new TextView[planNames.size()];
        final TextView[] infoLabelRef = new TextView[1];
        for (int i = 0; i < planNames.size(); i++) {
            String plan = planNames.get(i);
            TextView btn = new TextView(context);
            btn.setText(plan);
            btn.setTextSize(11.5f);
            btn.setGravity(Gravity.CENTER);
            btn.setPadding(dp(8), dp(10), dp(8), dp(10));
            GridLayout.LayoutParams lp = new GridLayout.LayoutParams();
            lp.width = 0;
            lp.height = ViewGroup.LayoutParams.WRAP_CONTENT;
            lp.columnSpec = GridLayout.spec(GridLayout.UNDEFINED, 1f);
            lp.setMargins(dp(3), dp(3), dp(3), dp(3));
            btn.setLayoutParams(lp);
            final int idx = i;
            btn.setOnClickListener(v -> {
                selectedPlan = plan;
                for (int j = 0; j < planButtons.length; j++) stylePlanButton(planButtons[j], planNames.get(j).equals(selectedPlan));
                updatePlanInfo(infoLabelRef[0]);
            });
            planButtons[i] = btn;
            planGrid.addView(btn);
        }
        for (int j = 0; j < planButtons.length; j++) stylePlanButton(planButtons[j], planNames.get(j).equals(selectedPlan));
        root.addView(planGrid);

        TextView infoLabel = new TextView(context);
        infoLabelRef[0] = infoLabel;
        updatePlanInfo(infoLabel);
        infoLabel.setTextSize(11);
        infoLabel.setTextColor(theme.sub);
        infoLabel.setPadding(0, dp(10), 0, 0);
        root.addView(infoLabel);

        NestedScrollView scroll = new NestedScrollView(context);
        scroll.addView(root);

        AlertDialog dialog = new AlertDialog.Builder(context)
            .setTitle("New Reading Marathon")
            .setView(scroll)
            .setPositiveButton("Begin Marathon", null)
            .setNegativeButton("Cancel", (d, w) -> d.dismiss())
            .create();

        dialog.setOnShowListener(d -> {
            dialog.getButton(Dialog.BUTTON_POSITIVE).setOnClickListener(v -> {
                String name = nameInput.getText().toString().trim();
                if (name.isEmpty()) { nameInput.setError("Required"); return; }

                MainActivity activity = context instanceof MainActivity ? (MainActivity) context : null;
                if (activity == null) { dialog.dismiss(); return; }
                PrefsManager prefs = activity.getPrefsManager();

                Marathon m = new Marathon();
                m.name = name;
                m.plan = selectedPlan;
                m.books = ReadingPlans.getBooksForPlan(selectedPlan);

                List<Marathon> list = prefs.getMarathons();
                list.add(m);
                prefs.saveMarathons(list);

                if (listener != null) listener.onSaved();
                dialog.dismiss();
            });
        });

        dialog.show();
    }

    private void updatePlanInfo(TextView infoLabel) {
        List<String> books = ReadingPlans.getBooksForPlan(selectedPlan);
        int totalChapters = 0;
        for (String b : books) totalChapters += BooksData.getChapterCount(b);
        infoLabel.setText(books.size() + " books - " + totalChapters + " chapters");
    }

    private void stylePlanButton(TextView btn, boolean active) {
        GradientDrawable gd = new GradientDrawable();
        gd.setCornerRadius(dp(4));
        if (active) {
            gd.setColor(withAlpha(theme.primary, 0x28));
            gd.setStroke(dp(1), theme.primary);
            btn.setTextColor(theme.primary);
        } else {
            gd.setColor(theme.card);
            gd.setStroke(dp(1), withAlpha(theme.sub, 0x33));
            btn.setTextColor(theme.text);
        }
        btn.setBackground(gd);
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
