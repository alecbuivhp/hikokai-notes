package com.example.hikokainotes.listeners;

import com.example.hikokainotes.entities.Note;

public interface NotesListener {
    void onNoteClicked(Note note, int position);
}
