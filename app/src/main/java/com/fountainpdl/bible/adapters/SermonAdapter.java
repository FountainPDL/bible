package com.fountainpdl.bible.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.fountainpdl.bible.R;
import com.fountainpdl.bible.models.Sermon;
import com.fountainpdl.bible.utils.AppTheme;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class SermonAdapter extends RecyclerView.Adapter<SermonAdapter.VH> {

    public interface Listener {
        void onOpen(Sermon sermon);
        void onEdit(Sermon sermon);
        void onDelete(Sermon sermon);
    }

    private List<Sermon> sermons = new ArrayList<>();
    private AppTheme theme;
    private final Listener listener;
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("MMM d, yyyy", Locale.getDefault());

    public SermonAdapter(Listener listener) { this.listener = listener; }

    public void setTheme(AppTheme theme) { this.theme = theme; }
    public void setSermons(List<Sermon> sermons) { this.sermons = sermons; notifyDataSetChanged(); }

    @NonNull @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_sermon_row, parent, false);
        return new VH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull VH holder, int position) {
        Sermon s = sermons.get(position);
        holder.title.setText(s.title);
        holder.meta.setText(s.blocks.size() + " block" + (s.blocks.size() != 1 ? "s" : "") + " · " + dateFormat.format(new java.util.Date(s.timestamp)));

        if (theme != null) {
            holder.title.setTextColor(theme.primary);
            holder.meta.setTextColor(theme.sub);
            holder.edit.setTextColor(theme.primary);
            holder.delete.setTextColor(theme.sub);
        }

        holder.itemView.setOnClickListener(v -> { if (listener != null) listener.onOpen(s); });
        holder.edit.setOnClickListener(v -> { if (listener != null) listener.onEdit(s); });
        holder.delete.setOnClickListener(v -> { if (listener != null) listener.onDelete(s); });
    }

    @Override public int getItemCount() { return sermons.size(); }

    static class VH extends RecyclerView.ViewHolder {
        TextView title, meta, edit, delete;
        VH(View itemView) {
            super(itemView);
            title = itemView.findViewById(R.id.sermonTitle);
            meta = itemView.findViewById(R.id.sermonMeta);
            edit = itemView.findViewById(R.id.sermonEdit);
            delete = itemView.findViewById(R.id.sermonDelete);
        }
    }
}
