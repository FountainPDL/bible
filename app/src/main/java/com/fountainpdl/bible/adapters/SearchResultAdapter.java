package com.fountainpdl.bible.adapters;

import android.graphics.Color;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.BackgroundColorSpan;
import android.text.style.StyleSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.fountainpdl.bible.R;
import com.fountainpdl.bible.models.SearchResult;
import com.fountainpdl.bible.utils.AppTheme;

import java.util.ArrayList;
import java.util.List;

public class SearchResultAdapter extends RecyclerView.Adapter<SearchResultAdapter.VH> {

    public interface Listener { void onResultTap(SearchResult result); }

    private List<SearchResult> results = new ArrayList<>();
    private AppTheme theme;
    private final Listener listener;

    public SearchResultAdapter(Listener listener) { this.listener = listener; }

    public void setTheme(AppTheme theme) { this.theme = theme; }

    public void setResults(List<SearchResult> results) {
        this.results = results;
        notifyDataSetChanged();
    }

    @NonNull @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_search_result, parent, false);
        return new VH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull VH holder, int position) {
        SearchResult r = results.get(position);
        holder.ref.setText(r.getRef());

        SpannableString spannable = new SpannableString(r.text);
        if (r.matchStart >= 0 && r.matchStart + r.matchLen <= r.text.length()) {
            int accent = theme != null ? theme.accent : Color.RED;
            spannable.setSpan(new BackgroundColorSpan(withAlpha(accent, 0x55)),
                r.matchStart, r.matchStart + r.matchLen, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            spannable.setSpan(new StyleSpan(android.graphics.Typeface.BOLD),
                r.matchStart, r.matchStart + r.matchLen, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        }
        holder.text.setText(spannable);

        if (theme != null) {
            holder.ref.setTextColor(theme.primary);
            holder.text.setTextColor(theme.text);
        }

        holder.itemView.setOnClickListener(v -> { if (listener != null) listener.onResultTap(r); });
    }

    private int withAlpha(int color, int alpha) {
        return Color.argb(alpha, Color.red(color), Color.green(color), Color.blue(color));
    }

    @Override public int getItemCount() { return results.size(); }

    static class VH extends RecyclerView.ViewHolder {
        TextView ref, text;
        VH(View itemView) {
            super(itemView);
            ref = itemView.findViewById(R.id.resultRef);
            text = itemView.findViewById(R.id.resultText);
        }
    }
}
