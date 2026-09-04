package com.fountainpdl.bible.dialogs;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.view.Gravity;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.GridLayout;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.core.widget.NestedScrollView;

import com.fountainpdl.bible.models.AppSettings;
import com.fountainpdl.bible.utils.AppTheme;
import com.fountainpdl.bible.utils.BooksData;
import com.fountainpdl.bible.utils.VerseCounts;

import java.util.List;

/**
 * 3-step Book -> Chapter -> Verse picker. This is the single Navigate
 * entry point in the app now -- reachable from the book/chapter chip in
 * the toolbar. The redundant bottom-nav "Navigate" tab was removed and
 * replaced with Home; this dialog is the only navigator, so there is no
 * more duplication between the two.
 */
public class NavigatePickerDialog {

    public interface OnPickListener { void onPick(String book, int chapter, Integer verse); }

    private final Context context;
    private final AppTheme theme;
    private final AppSettings settings;
    private final OnPickListener listener;

    private Dialog dialog;
    private LinearLayout contentContainer;
    private int step = 1;
    private String pickedBook;
    private int pickedChapter;

    public NavigatePickerDialog(Context context, AppTheme theme, AppSettings settings, OnPickListener listener) {
        this.context = context;
        this.theme = theme;
        this.settings = settings;
        this.listener = listener;
    }

    public void show() {
        dialog = new Dialog(context);
        dialog.requestWindowFeature(android.view.Window.FEATURE_NO_TITLE);

        LinearLayout root = new LinearLayout(context);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setBackgroundColor(theme.surface);
        int pad = dp(16);
        root.setPadding(pad, pad, pad, pad);

        TextView title = new TextView(context);
        title.setText("Navigate to Scripture");
        title.setTextSize(16);
        title.setTextColor(theme.primary);
        title.setPadding(0, 0, 0, dp(14));
        root.addView(title);

        contentContainer = new LinearLayout(context);
        contentContainer.setOrientation(LinearLayout.VERTICAL);
        NestedScrollView scroll = new NestedScrollView(context);
        scroll.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, dp(420)));
        scroll.addView(contentContainer);
        root.addView(scroll);

        dialog.setContentView(root);
        dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);

        showBookStep();
        dialog.show();
    }

    private void showBookStep() {
        step = 1;
        contentContainer.removeAllViews();

        EditText search = new EditText(context);
        search.setHint("Search books...");
        search.setTextColor(theme.text);
        search.setHintTextColor(theme.sub);
        search.setBackground(inputBg());
        search.setPadding(dp(12), dp(8), dp(12), dp(8));
        contentContainer.addView(search);

        LinearLayout grids = new LinearLayout(context);
        grids.setOrientation(LinearLayout.VERTICAL);
        contentContainer.addView(grids);

        renderBookGrid(grids, "");

        search.addTextChangedListener(new android.text.TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int a, int b, int c) {}
            @Override public void onTextChanged(CharSequence s, int a, int b, int c) { renderBookGrid(grids, s.toString()); }
            @Override public void afterTextChanged(android.text.Editable s) {}
        });
    }

    private void renderBookGrid(LinearLayout container, String filter) {
        container.removeAllViews();
        String f = filter.toLowerCase();

        TextView otLabel = new TextView(context);
        otLabel.setText("OLD TESTAMENT");
        otLabel.setTextSize(10);
        otLabel.setTextColor(theme.sub);
        otLabel.setPadding(0, dp(8), 0, dp(4));
        container.addView(otLabel);
        GridLayout otGrid = makeBookGrid(BooksData.OT_BOOKS, f);
        container.addView(otGrid);

        TextView ntLabel = new TextView(context);
        ntLabel.setText("NEW TESTAMENT");
        ntLabel.setTextSize(10);
        ntLabel.setTextColor(theme.sub);
        ntLabel.setPadding(0, dp(8), 0, dp(4));
        container.addView(ntLabel);
        GridLayout ntGrid = makeBookGrid(BooksData.NT_BOOKS, f);
        container.addView(ntGrid);
    }

    private GridLayout makeBookGrid(List<String> books, String filter) {
        GridLayout grid = new GridLayout(context);
        grid.setColumnCount(3);
        for (String book : books) {
            if (!filter.isEmpty() && !book.toLowerCase().contains(filter)) continue;
            TextView cell = new TextView(context);
            cell.setText(book);
            cell.setTextSize(11.5f);
            cell.setTextColor(theme.text);
            cell.setGravity(Gravity.CENTER);
            cell.setPadding(dp(6), dp(8), dp(6), dp(8));
            cell.setBackground(cellBg());
            GridLayout.LayoutParams lp = new GridLayout.LayoutParams();
            lp.width = 0;
            lp.height = ViewGroup.LayoutParams.WRAP_CONTENT;
            lp.columnSpec = GridLayout.spec(GridLayout.UNDEFINED, 1f);
            lp.setMargins(dp(2), dp(2), dp(2), dp(2));
            cell.setLayoutParams(lp);
            cell.setOnClickListener(v -> { pickedBook = book; showChapterStep(); });
            grid.addView(cell);
        }
        return grid;
    }

    private void showChapterStep() {
        step = 2;
        contentContainer.removeAllViews();

        TextView back = new TextView(context);
        back.setText("< " + pickedBook);
        back.setTextColor(theme.sub);
        back.setPadding(dp(8), dp(4), dp(8), dp(4));
        back.setOnClickListener(v -> showBookStep());
        contentContainer.addView(back);

        int count = BooksData.getChapterCount(pickedBook);
        GridLayout grid = new GridLayout(context);
        grid.setColumnCount(6);
        for (int c = 1; c <= count; c++) {
            final int chap = c;
            TextView cell = new TextView(context);
            cell.setText(String.valueOf(c));
            cell.setTextSize(13);
            cell.setTextColor(theme.text);
            cell.setGravity(Gravity.CENTER);
            cell.setPadding(dp(6), dp(10), dp(6), dp(10));
            cell.setBackground(cellBg());
            GridLayout.LayoutParams lp = new GridLayout.LayoutParams();
            lp.width = 0;
            lp.height = ViewGroup.LayoutParams.WRAP_CONTENT;
            lp.columnSpec = GridLayout.spec(GridLayout.UNDEFINED, 1f);
            lp.setMargins(dp(2), dp(2), dp(2), dp(2));
            cell.setLayoutParams(lp);
            cell.setOnClickListener(v -> { pickedChapter = chap; showVerseStep(); });
            grid.addView(cell);
        }
        contentContainer.addView(grid);
    }

    private void showVerseStep() {
        step = 3;
        contentContainer.removeAllViews();

        TextView back = new TextView(context);
        back.setText("< " + pickedBook + " " + pickedChapter);
        back.setTextColor(theme.sub);
        back.setPadding(dp(8), dp(4), dp(8), dp(4));
        back.setOnClickListener(v -> showChapterStep());
        contentContainer.addView(back);

        TextView openChapter = new TextView(context);
        openChapter.setText("Open Full Chapter");
        openChapter.setTextColor(Color.WHITE);
        openChapter.setGravity(Gravity.CENTER);
        openChapter.setPadding(dp(14), dp(12), dp(14), dp(12));
        GradientDrawable grad = new GradientDrawable();
        grad.setOrientation(GradientDrawable.Orientation.TL_BR);
        grad.setColors(new int[]{theme.primary, theme.accent});
        grad.setCornerRadius(dp(4));
        openChapter.setBackground(grad);
        openChapter.setOnClickListener(v -> { finish(pickedBook, pickedChapter, null); });
        LinearLayout.LayoutParams lp0 = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        lp0.setMargins(0, dp(8), 0, dp(12));
        openChapter.setLayoutParams(lp0);
        contentContainer.addView(openChapter);

        TextView orLabel = new TextView(context);
        orLabel.setText("or jump to a specific verse");
        orLabel.setTextColor(theme.sub);
        orLabel.setTextSize(10.5f);
        orLabel.setGravity(Gravity.CENTER);
        orLabel.setPadding(0, 0, 0, dp(8));
        contentContainer.addView(orLabel);

        int maxV = VerseCounts.getVerseCount(pickedBook, pickedChapter);
        GridLayout grid = new GridLayout(context);
        grid.setColumnCount(6);
        for (int vv = 1; vv <= maxV; vv++) {
            final int verseNum = vv;
            TextView cell = new TextView(context);
            cell.setText(String.valueOf(vv));
            cell.setTextSize(12);
            cell.setTextColor(theme.text);
            cell.setGravity(Gravity.CENTER);
            cell.setPadding(dp(6), dp(9), dp(6), dp(9));
            cell.setBackground(cellBg());
            GridLayout.LayoutParams lp = new GridLayout.LayoutParams();
            lp.width = 0;
            lp.height = ViewGroup.LayoutParams.WRAP_CONTENT;
            lp.columnSpec = GridLayout.spec(GridLayout.UNDEFINED, 1f);
            lp.setMargins(dp(2), dp(2), dp(2), dp(2));
            cell.setLayoutParams(lp);
            cell.setOnClickListener(v -> finish(pickedBook, pickedChapter, verseNum));
            grid.addView(cell);
        }
        contentContainer.addView(grid);
    }

    private void finish(String book, int chapter, Integer verse) {
        if (listener != null) listener.onPick(book, chapter, verse);
        if (dialog != null) dialog.dismiss();
    }

    private GradientDrawable cellBg() {
        GradientDrawable gd = new GradientDrawable();
        gd.setColor(theme.card);
        gd.setStroke(dp(1), withAlpha(theme.sub, 0x33));
        gd.setCornerRadius(dp(4));
        return gd;
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

    private int dp(int v) { return (int) (v * context.getResources().getDisplayMetrics().density); }
}
