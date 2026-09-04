package com.fountainpdl.bible.adapters;

import android.graphics.Color;
import android.graphics.Paint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.fountainpdl.bible.R;
import com.fountainpdl.bible.models.AppSettings;
import com.fountainpdl.bible.models.Highlight;
import com.fountainpdl.bible.models.Verse;
import com.fountainpdl.bible.utils.AppTheme;
import com.fountainpdl.bible.utils.JesusWords;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class VerseAdapter extends RecyclerView.Adapter<VerseAdapter.VH> {

    public interface Listener {
        void onVerseTap(int verseNum);
        void onVerseLongPress(int verseNum);
        void onWordAnnotationTap(String annotationId);
    }

    private List<Verse> verses = new ArrayList<>();
    private final Set<Integer> selected = new HashSet<>();
    private final Map<Integer, Highlight> highlights = new HashMap<>();
    private final Map<Integer, Highlight> underlines = new HashMap<>();
    private final Set<Integer> bookmarked = new HashSet<>();
    private final Map<Integer, List<WordAnn>> wordAnns = new HashMap<>();
    private int ttsActiveVerse = -1;
    private int flashVerse = -1;

    private AppTheme theme;
    private AppSettings settings;
    private boolean isNewTestament;
    private final Listener listener;

    public static class WordAnn {
        public String id, text, type, color;
    }

    public VerseAdapter(Listener listener) { this.listener = listener; }

    public void setTheme(AppTheme theme) { this.theme = theme; }
    public void setSettings(AppSettings settings) { this.settings = settings; }
    public void setIsNewTestament(boolean nt) { this.isNewTestament = nt; }

    public void setVerses(List<Verse> verses) {
        this.verses = verses;
        selected.clear();
        notifyDataSetChanged();
    }

    public void setHighlights(Map<Integer, Highlight> h) { this.highlights.clear(); this.highlights.putAll(h); notifyDataSetChanged(); }
    public void setUnderlines(Map<Integer, Highlight> u) { this.underlines.clear(); this.underlines.putAll(u); notifyDataSetChanged(); }
    public void setBookmarked(Set<Integer> b) { this.bookmarked.clear(); this.bookmarked.addAll(b); notifyDataSetChanged(); }
    public void setWordAnnotations(Map<Integer, List<WordAnn>> w) { this.wordAnns.clear(); this.wordAnns.putAll(w); notifyDataSetChanged(); }

    public void setTtsActiveVerse(int v) {
        int old = ttsActiveVerse;
        ttsActiveVerse = v;
        if (old >= 0) notifyItemForVerse(old);
        if (v >= 0) notifyItemForVerse(v);
    }

    public void flashVerse(int v) {
        flashVerse = v;
        notifyItemForVerse(v);
    }

    public void clearFlash() {
        int old = flashVerse;
        flashVerse = -1;
        if (old >= 0) notifyItemForVerse(old);
    }

    private void notifyItemForVerse(int verseNum) {
        for (int i = 0; i < verses.size(); i++) {
            if (verses.get(i).verseNum == verseNum) { notifyItemChanged(i); return; }
        }
    }

    public Set<Integer> getSelected() { return selected; }

    public void toggleSelection(int verseNum) {
        if (selected.contains(verseNum)) selected.remove(verseNum);
        else selected.add(verseNum);
        notifyItemForVerse(verseNum);
    }

    public void clearSelection() {
        Set<Integer> old = new HashSet<>(selected);
        selected.clear();
        for (int v : old) notifyItemForVerse(v);
    }

    public int findPositionForVerse(int verseNum) {
        for (int i = 0; i < verses.size(); i++) if (verses.get(i).verseNum == verseNum) return i;
        return -1;
    }

    @NonNull @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_verse, parent, false);
        return new VH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull VH holder, int position) {
        Verse verse = verses.get(position);
        int vn = verse.verseNum;
        boolean isSelected = selected.contains(vn);
        boolean isHighlighted = highlights.containsKey(vn);
        boolean isUnderlined = underlines.containsKey(vn);
        boolean isBookmarked = bookmarked.contains(vn);
        boolean isTtsActive = ttsActiveVerse == vn;
        boolean isFlashing = flashVerse == vn;
        boolean isRed = settings != null && settings.redLetter && isNewTestament
                && JesusWords.isRedLetter(verse.book, verse.chapter, vn);

        String prefix = (settings == null || settings.verseNumbers) ? (vn + " ") : "";
        SpannableBuilder sb = new SpannableBuilder(prefix, verse.text);

        holder.verseText.setText(sb.build());
        holder.verseText.setTextSize(settings != null ? settings.fontSize : 18);
        if (settings != null) holder.verseText.setLineSpacing(0, settings.lineSpacing);

        int textColor = isRed && theme != null ? theme.redWord : (theme != null ? theme.text : Color.WHITE);
        holder.verseText.setTextColor(textColor);

        if (isUnderlined) holder.verseText.setPaintFlags(holder.verseText.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);
        else holder.verseText.setPaintFlags(holder.verseText.getPaintFlags() & ~Paint.UNDERLINE_TEXT_FLAG);

        int bg = Color.TRANSPARENT;
        if (theme != null) {
            if (isFlashing) bg = withAlpha(theme.accent, 0x55);
            else if (isSelected) bg = theme.selectedBg;
            else if (isHighlighted) bg = theme.highlightBg;
            else if (isTtsActive) bg = withAlpha(theme.accent, 0x18);
        }
        holder.verseRoot.setBackgroundColor(bg);

        List<WordAnn> anns = wordAnns.get(vn);
        holder.annotationRow.removeAllViews();
        if (anns != null && !anns.isEmpty()) {
            holder.annotationRow.setVisibility(View.VISIBLE);
            for (WordAnn ann : anns) {
                TextView chip = new TextView(holder.itemView.getContext());
                chip.setText("\"" + trim(ann.text, 20) + "\" ✕");
                chip.setTextSize(10.5f);
                chip.setPadding(dp(holder, 8), dp(holder, 2), dp(holder, 8), dp(holder, 2));
                android.graphics.drawable.GradientDrawable bg2 = new android.graphics.drawable.GradientDrawable();
                int annColor = ann.color != null ? safeColor(ann.color) : (theme != null ? theme.primary : Color.MAGENTA);
                bg2.setColor("highlight".equals(ann.type) ? annColor : withAlpha(theme != null ? theme.primary : Color.GRAY, 0x22));
                bg2.setStroke(dp(holder,1), withAlpha(annColor, 0x88));
                bg2.setCornerRadius(dp(holder, 10));
                chip.setBackground(bg2);
                chip.setTextColor(theme != null && theme.isDark ? Color.WHITE : Color.BLACK);
                LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
                lp.setMarginEnd(dp(holder, 4));
                chip.setLayoutParams(lp);
                chip.setOnClickListener(v -> { if (listener != null) listener.onWordAnnotationTap(ann.id); });
                holder.annotationRow.addView(chip);
            }
        } else {
            holder.annotationRow.setVisibility(View.GONE);
        }

        holder.itemView.setOnClickListener(v -> { if (listener != null) listener.onVerseTap(vn); });
        holder.itemView.setOnLongClickListener(v -> { if (listener != null) listener.onVerseLongPress(vn); return true; });
    }

    private int dp(VH holder, int v) {
        return (int) (v * holder.itemView.getResources().getDisplayMetrics().density);
    }

    private String trim(String s, int max) {
        if (s == null) return "";
        return s.length() > max ? s.substring(0, max) + "…" : s;
    }

    private int safeColor(String hex) {
        try { return Color.parseColor(hex); } catch (Exception e) { return Color.GRAY; }
    }

    private int withAlpha(int color, int alpha) {
        return Color.argb(alpha, Color.red(color), Color.green(color), Color.blue(color));
    }

    @Override public int getItemCount() { return verses.size(); }

    static class VH extends RecyclerView.ViewHolder {
        TextView verseText;
        LinearLayout verseRoot, annotationRow;
        VH(View itemView) {
            super(itemView);
            verseRoot = itemView.findViewById(R.id.verseRoot);
            verseText = itemView.findViewById(R.id.verseText);
            annotationRow = itemView.findViewById(R.id.wordAnnotationRow);
        }
    }

    private static class SpannableBuilder {
        private final String prefix, body;
        SpannableBuilder(String prefix, String body) { this.prefix = prefix; this.body = body; }
        CharSequence build() {
            if (prefix.isEmpty()) return body;
            android.text.SpannableString ss = new android.text.SpannableString(prefix + body);
            ss.setSpan(new android.text.style.RelativeSizeSpan(0.62f), 0, prefix.length(), 0);
            ss.setSpan(new android.text.style.SuperscriptSpan(), 0, prefix.length(), 0);
            return ss;
        }
    }
}
