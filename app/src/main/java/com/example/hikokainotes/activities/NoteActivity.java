package com.example.hikokainotes.activities;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.hikokainotes.R;
import com.example.hikokainotes.database.NotesDatabase;
import com.example.hikokainotes.entities.Note;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class NoteActivity extends AppCompatActivity {
    private EditText inputNoteTitle, inputNoteText, inputTag;
    private TextView textUpdateTime;

    private Note availableNote;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_note_activity);

        ImageView backButton = findViewById(R.id.imageBack);
        backButton.setOnClickListener(v -> onBackPressed());

        inputNoteTitle = findViewById(R.id.inputNoteTitle);
        inputNoteText = findViewById(R.id.inputNote);
        textUpdateTime = findViewById(R.id.textUpdateTime);
        inputTag = findViewById(R.id.inputTag);

        textUpdateTime.setText(new SimpleDateFormat("dd MM yyyy HH:mm:ss a", Locale.getDefault()).format(new Date()));

        ImageView saveButton = findViewById(R.id.imageDone);
        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveNote();
            }
        });

        if (getIntent().getBooleanExtra("isViewOrUpdate", false)) {
            availableNote = (Note) getIntent().getSerializableExtra("note");
            setViewOrUpdateNote();
        }
    }

    private void setViewOrUpdateNote() {
        inputNoteTitle.setText(availableNote.getTitle());
        inputTag.setText(availableNote.getTag());
        inputNoteText.setText(availableNote.getNoteText());
        textUpdateTime.setText(availableNote.getUpdateTime());
    }

    private void saveNote() {
        if (inputNoteTitle.getText().toString().trim().isEmpty() && inputNoteText.getText().toString().trim().isEmpty() && inputTag.getText().toString().trim().isEmpty()) {
            finish();
        } else {
            final Note note = new Note();
            note.setTitle(inputNoteTitle.getText().toString());
            note.setNoteText(inputNoteText.getText().toString());
            note.setUpdateTime(textUpdateTime.getText().toString());
            note.setTag(inputTag.getText().toString());

            if (availableNote != null) {
                note.setId(availableNote.getId());
            }

            @SuppressLint("StaticFieldLeak")
            class SaveNoteTask extends AsyncTask<Void, Void, Void> {

                @Override
                protected Void doInBackground(Void... voids) {
                    NotesDatabase.getDatabase(getApplicationContext()).noteDao().insertNote(note);
                    return null;
                }

                @Override
                protected void onPostExecute(Void aVoid) {
                    super.onPostExecute(aVoid);
                    Intent intent = new Intent();
                    setResult(RESULT_OK, intent);
                    finish();
                }
            }
            new SaveNoteTask().execute();
        }
    }
}