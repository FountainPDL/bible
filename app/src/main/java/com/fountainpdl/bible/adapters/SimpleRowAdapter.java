package com.fountainpdl.bible.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.fountainpdl.bible.R;
import com.fountainpdl.bible.utils.AppTheme;

import java.util.ArrayList;
import java.util.List;

public class SimpleRowAdapter extends RecyclerView.Adapter<SimpleRowAdapter.VH> {

    public static class RowItem {
        public String title, subtitle, actionLabel;
        public int accentColor = 0; // 0 = no accent stripe
        public Runnable onClick, onAction;
    }

    private List<RowItem> items = new ArrayList<>();
    private AppTheme theme;

    public void setTheme(AppTheme theme) { this.theme = theme; }
    public void setItems(List<RowItem> items) { this.items = items; notifyDataSetChanged(); }

    @NonNull @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_list_row, parent, false);
        return new VH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull VH holder, int position) {
        RowItem item = items.get(position);
        holder.title.setText(item.title);
        holder.subtitle.setText(item.subtitle);
        holder.subtitle.setVisibility(item.subtitle == null || item.subtitle.isEmpty() ? View.GONE : View.VISIBLE);

        if (item.actionLabel != null) {
            holder.action.setVisibility(View.VISIBLE);
            holder.action.setText(item.actionLabel);
            holder.action.setOnClickListener(v -> { if (item.onAction != null) item.onAction.run(); });
        } else {
            holder.action.setVisibility(View.GONE);
        }

        if (theme != null) {
            holder.title.setTextColor(theme.text);
            holder.subtitle.setTextColor(theme.sub);
            holder.action.setTextColor(theme.sub);
            holder.itemView.setBackgroundColor(item.accentColor != 0 ? item.accentColor : android.graphics.Color.TRANSPARENT);
        }

        holder.itemView.setOnClickListener(v -> { if (item.onClick != null) item.onClick.run(); });
    }

    @Override public int getItemCount() { return items.size(); }

    static class VH extends RecyclerView.ViewHolder {
        TextView title, subtitle, action;
        VH(View itemView) {
            super(itemView);
            title = itemView.findViewById(R.id.rowTitle);
            subtitle = itemView.findViewById(R.id.rowSubtitle);
            action = itemView.findViewById(R.id.rowAction);
        }
    }
}
