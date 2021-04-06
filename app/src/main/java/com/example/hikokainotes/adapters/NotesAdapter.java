package com.example.hikokainotes.adapters;

import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.hikokainotes.R;
import com.example.hikokainotes.entities.Note;
import com.example.hikokainotes.listeners.NotesListener;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class NotesAdapter extends RecyclerView.Adapter<NotesAdapter.NoteViewHolder> {
    private List<Note> notes;
    private NotesListener notesListener;
    private Timer timer;
    private List<Note> notesSource;

    public NotesAdapter(List<Note> notes, NotesListener notesListener) {
        this.notes = notes;
        this.notesListener = notesListener;
        notesSource = notes;
    }

    @NonNull
    @Override
    public NoteViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new NoteViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_container_note, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull NoteViewHolder holder, final int position) {
        holder.setNote(notes.get(position));
        holder.layoutNote.setOnClickListener(v -> notesListener.onNoteClicked(notes.get(position), position));
        holder.deleteButton.setOnClickListener(v -> notesListener.onDeleteClicked(position));
    }

    @Override
    public int getItemCount() {
        return notes.size();
    }

    @Override
    public int getItemViewType(int position) {
        return position;
    }

    static class NoteViewHolder extends RecyclerView.ViewHolder {
        TextView textTitle, textUpdateTime, textTag;
        LinearLayout layoutNote;
        ImageView deleteButton;

        NoteViewHolder(@NonNull View itemView) {
            super(itemView);
            textTitle = itemView.findViewById(R.id.textTitle);
            textUpdateTime = itemView.findViewById(R.id.textUpdateTime);
            textTag = itemView.findViewById(R.id.textTag);
            layoutNote = itemView.findViewById(R.id.layoutNote);
            deleteButton = itemView.findViewById(R.id.deleteButton);
        }

        void setNote(Note note) {
            textTitle.setText(note.getTitle());
            textUpdateTime.setText(note.getUpdateTime());

            StringBuilder tempTags = new StringBuilder();
            ArrayList<String> tags = note.getTags();
            for (int i = 0; i < tags.size(); i++) {
                if (i == tags.size() - 1) {
                    tempTags.append(tags.get(i));
                } else {
                    tempTags.append(tags.get(i)).append(", ");
                }

            }
            textTag.setText(tempTags);
        }
    }

    public void searchNotes(final String keyword) {
        timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                if (keyword.trim().isEmpty()) {
                    notes = notesSource;
                } else {
                    ArrayList<Note> temp = new ArrayList<>();
                    for (Note note : notesSource) {
                        if (note.getTitle().toLowerCase().contains(keyword.toLowerCase()) || (note.getNoteText() != null && note.getNoteText().toLowerCase().contains(keyword.toLowerCase()))) {
                            temp.add(note);
                        }
                    }
                    notes = temp;
                }
                new Handler(Looper.getMainLooper()).post(() -> notifyDataSetChanged());
            }
        }, 500);
    }

    public void cancelTimer() {
        if (timer != null) {
            timer.cancel();
        }
    }

    public void searchTags(final List<String> tags) {
        if (tags.size() == 0) {
            notes = notesSource;
        } else {
            List<Note> temp = new ArrayList<>();
            for (Note note : notesSource) {
                List<String> tagsNote = note.getTags();
                List<String> tempList = new ArrayList<>(tagsNote);
                tempList.retainAll(tags);

                if (tempList.size() != 0) {
                    temp.add(note);
                }
            }
            notes = temp;
        }
        new Handler(Looper.getMainLooper()).post(this::notifyDataSetChanged);
    }
}
