package com.fountainpdl.bible.adapters;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.fountainpdl.bible.R;
import com.fountainpdl.bible.models.Marathon;
import com.fountainpdl.bible.utils.AppTheme;
import com.fountainpdl.bible.utils.BooksData;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;

import java.util.ArrayList;
import java.util.List;

public class MarathonAdapter extends RecyclerView.Adapter<MarathonAdapter.VH> {

    public interface Listener {
        void onMarkRead(Marathon marathon);
        void onDelete(Marathon marathon);
    }

    private List<Marathon> marathons = new ArrayList<>();
    private AppTheme theme;
    private final Listener listener;

    public MarathonAdapter(Listener listener) { this.listener = listener; }

    public void setTheme(AppTheme theme) { this.theme = theme; }
    public void setMarathons(List<Marathon> marathons) { this.marathons = marathons; notifyDataSetChanged(); }

    @NonNull @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_marathon_card, parent, false);
        return new VH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull VH holder, int position) {
        Marathon m = marathons.get(position);
        holder.name.setText(m.name);

        int totalChapters = 0, doneChapters = 0;
        for (String book : m.books) {
            int chCount = BooksData.getChapterCount(book);
            totalChapters += chCount;
            List<Integer> completed = m.completedChapters.get(book);
            if (completed != null) doneChapters += completed.size();
        }
        int pct = totalChapters > 0 ? (doneChapters * 100 / totalChapters) : 0;

        holder.meta.setText(m.plan + " - " + doneChapters + "/" + totalChapters + " - " + pct + "%");
        holder.progress.setProgress(pct);

        holder.bookChips.removeAllViews();
        for (String book : m.books) {
            int chCount = BooksData.getChapterCount(book);
            List<Integer> completed = m.completedChapters.get(book);
            int bookDone = completed != null ? completed.size() : 0;
            int bookPct = chCount > 0 ? (bookDone * 100 / chCount) : 0;

            Chip chip = new Chip(holder.itemView.getContext());
            chip.setText(shorten(book) + " " + bookPct + "%");
            chip.setTextSize(9.5f);
            chip.setChipMinHeight(dp(holder, 22));
            chip.setClickable(false);
            if (theme != null) {
                if (bookPct == 100) { chip.setChipBackgroundColor(android.content.res.ColorStateList.valueOf(theme.primary)); chip.setTextColor(Color.WHITE); }
                else if (bookPct > 0) { chip.setChipBackgroundColor(android.content.res.ColorStateList.valueOf(withAlpha(theme.primary, 0x44))); chip.setTextColor(theme.text); }
                else { chip.setChipBackgroundColor(android.content.res.ColorStateList.valueOf(theme.card)); chip.setTextColor(theme.sub); }
            }
            holder.bookChips.addView(chip);
        }

        if (theme != null) {
            holder.name.setTextColor(theme.primary);
            holder.meta.setTextColor(theme.sub);
            holder.markRead.setTextColor(theme.text);
            holder.markRead.setBackgroundColor(theme.card);
            holder.delete.setTextColor(theme.sub);
        }

        holder.markRead.setOnClickListener(v -> { if (listener != null) listener.onMarkRead(m); });
        holder.delete.setOnClickListener(v -> { if (listener != null) listener.onDelete(m); });
    }

    private String shorten(String book) { return book.length() > 7 ? book.substring(0, 7) : book; }
    private int withAlpha(int color, int alpha) { return Color.argb(alpha, Color.red(color), Color.green(color), Color.blue(color)); }
    private int dp(VH holder, int v) { return (int) (v * holder.itemView.getResources().getDisplayMetrics().density); }

    @Override public int getItemCount() { return marathons.size(); }

    static class VH extends RecyclerView.ViewHolder {
        TextView name, meta, markRead, delete;
        ProgressBar progress;
        ChipGroup bookChips;
        VH(View itemView) {
            super(itemView);
            name = itemView.findViewById(R.id.marathonName);
            meta = itemView.findViewById(R.id.marathonMeta);
            progress = itemView.findViewById(R.id.marathonProgress);
            markRead = itemView.findViewById(R.id.marathonMarkRead);
            delete = itemView.findViewById(R.id.marathonDelete);
            bookChips = itemView.findViewById(R.id.marathonBookChips);
        }
    }
}
