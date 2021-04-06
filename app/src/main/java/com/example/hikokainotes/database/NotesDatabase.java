package com.example.hikokainotes.database;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.room.TypeConverters;

import com.example.hikokainotes.Helpers.Converters;
import com.example.hikokainotes.dao.NoteDao;
import com.example.hikokainotes.entities.Note;

@Database(entities = Note.class, version = 1, exportSchema = false)
@TypeConverters({Converters.class})
public abstract class NotesDatabase extends RoomDatabase {
    private static NotesDatabase notesDatabase;

    public static synchronized NotesDatabase getDatabase(Context context) {
        if (notesDatabase == null) {
            notesDatabase = Room.databaseBuilder(context, NotesDatabase.class, "notes_db").build();
        }
        return notesDatabase;
    }

    public abstract NoteDao noteDao();
}
