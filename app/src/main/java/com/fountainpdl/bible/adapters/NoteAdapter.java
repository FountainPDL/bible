package com.fountainpdl.bible.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.fountainpdl.bible.R;
import com.fountainpdl.bible.models.Note;
import com.fountainpdl.bible.utils.AppTheme;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class NoteAdapter extends RecyclerView.Adapter<NoteAdapter.VH> {

    public interface Listener {
        void onEdit(Note note);
        void onDelete(Note note);
        void onRefTap(String ref);
    }

    private List<Note> notes = new ArrayList<>();
    private AppTheme theme;
    private final Listener listener;
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("MMM d, yyyy · h:mm a", Locale.getDefault());

    public NoteAdapter(Listener listener) { this.listener = listener; }

    public void setTheme(AppTheme theme) { this.theme = theme; }
    public void setNotes(List<Note> notes) { this.notes = notes; notifyDataSetChanged(); }

    @NonNull @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_note_card, parent, false);
        return new VH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull VH holder, int position) {
        Note n = notes.get(position);
        holder.topic.setText(n.topic);
        holder.text.setText(n.text);
        holder.date.setText(dateFormat.format(new java.util.Date(n.timestamp)));

        if (n.refs != null && !n.refs.trim().isEmpty()) {
            holder.refs.setVisibility(View.VISIBLE);
            holder.refs.setText("Go to: " + n.refs);
            holder.refs.setOnClickListener(v -> { if (listener != null) listener.onRefTap(n.refs); });
        } else {
            holder.refs.setVisibility(View.GONE);
        }

        if (theme != null) {
            holder.topic.setTextColor(theme.primary);
            holder.text.setTextColor(theme.text);
            holder.refs.setTextColor(theme.accent);
            holder.date.setTextColor(theme.sub);
            holder.edit.setTextColor(theme.primary);
            holder.delete.setTextColor(theme.sub);
        }

        holder.edit.setOnClickListener(v -> { if (listener != null) listener.onEdit(n); });
        holder.delete.setOnClickListener(v -> { if (listener != null) listener.onDelete(n); });
    }

    @Override public int getItemCount() { return notes.size(); }

    static class VH extends RecyclerView.ViewHolder {
        TextView topic, text, refs, date, edit, delete;
        VH(View itemView) {
            super(itemView);
            topic = itemView.findViewById(R.id.noteTopic);
            text = itemView.findViewById(R.id.noteText);
            refs = itemView.findViewById(R.id.noteRefs);
            date = itemView.findViewById(R.id.noteDate);
            edit = itemView.findViewById(R.id.noteEdit);
            delete = itemView.findViewById(R.id.noteDelete);
        }
    }
}
