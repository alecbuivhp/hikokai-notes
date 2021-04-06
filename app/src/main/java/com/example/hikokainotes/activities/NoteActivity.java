package com.example.hikokainotes.activities;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.example.hikokainotes.R;
import com.example.hikokainotes.database.NotesDatabase;
import com.example.hikokainotes.entities.Note;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipDrawable;
import com.google.android.material.chip.ChipGroup;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

import jp.wasabeef.richeditor.RichEditor;

public class NoteActivity extends AppCompatActivity {
    private EditText inputNoteTitle, inputTag;
    private TextView textUpdateTime;
    private ChipGroup chipGroup;

    private RichEditor editor;
    private Note availableNote;

    private AlertDialog dialogDelete;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_note_activity);

        editor = findViewById(R.id.editor);
        editor.setEditorHeight(200);
        editor.setEditorFontSize(20);
        editor.setEditorFontColor(Color.WHITE);

        findViewById(R.id.action_undo).setOnClickListener(v -> editor.undo());

        findViewById(R.id.action_redo).setOnClickListener(v -> editor.redo());

        findViewById(R.id.action_bold).setOnClickListener(v -> editor.setBold());

        findViewById(R.id.action_italic).setOnClickListener(v -> editor.setItalic());

        findViewById(R.id.action_strikethrough).setOnClickListener(v -> editor.setStrikeThrough());

        findViewById(R.id.action_underline).setOnClickListener(v -> editor.setUnderline());

        findViewById(R.id.action_indent).setOnClickListener(v -> editor.setIndent());

        findViewById(R.id.action_outdent).setOnClickListener(v -> editor.setOutdent());

        findViewById(R.id.action_align_left).setOnClickListener(v -> editor.setAlignLeft());

        findViewById(R.id.action_align_center).setOnClickListener(v -> editor.setAlignCenter());

        findViewById(R.id.action_align_right).setOnClickListener(v -> editor.setAlignRight());

        findViewById(R.id.action_insert_bullets).setOnClickListener(v -> editor.setBullets());

        findViewById(R.id.action_insert_numbers).setOnClickListener(v -> editor.setNumbers());

        ImageView backButton = findViewById(R.id.imageBack);
        backButton.setOnClickListener(v -> onBackPressed());

        inputNoteTitle = findViewById(R.id.inputNoteTitle);
        textUpdateTime = findViewById(R.id.textUpdateTime);

        chipGroup = findViewById(R.id.chip_group);

        inputTag = findViewById(R.id.inputTag);
        inputTag.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                Chip chip = new Chip(NoteActivity.this);
                ChipDrawable drawable = ChipDrawable.createFromAttributes(NoteActivity.this, null, 0, R.style.Widget_MaterialComponents_Chip_Entry);
                chip.setChipDrawable(drawable);
                chip.setCheckable(false);
                chip.setClickable(false);
                chip.setPadding(60, 10, 60, 10);
                chip.setText(inputTag.getText().toString());
                chip.setOnCloseIconClickListener(v1 -> chipGroup.removeView(chip));
                chipGroup.addView(chip);
                inputTag.setText("");
                return true;
            }
            return false;
        });

        textUpdateTime.setText(new SimpleDateFormat("dd MM yyyy HH:mm:ss a", Locale.getDefault()).format(new Date()));

        ImageView saveButton = findViewById(R.id.imageDone);
        saveButton.setOnClickListener(v -> saveNote());

        if (getIntent().getBooleanExtra("isViewOrUpdate", false)) {
            availableNote = (Note) getIntent().getSerializableExtra("note");
            setViewOrUpdateNote();
        }

        initMiscellaneous();
    }

    private void setViewOrUpdateNote() {
        inputNoteTitle.setText(availableNote.getTitle());
        editor.setHtml(availableNote.getNoteText());
        textUpdateTime.setText(availableNote.getUpdateTime());
        ArrayList<String> tags = availableNote.getTags();

        if (tags.size() != 0) {
            for (String t : tags) {
                Chip chip = new Chip(NoteActivity.this);
                ChipDrawable drawable = ChipDrawable.createFromAttributes(NoteActivity.this, null, 0, R.style.Widget_MaterialComponents_Chip_Entry);
                chip.setChipDrawable(drawable);
                chip.setCheckable(false);
                chip.setClickable(false);
                chip.setPadding(60, 10, 60, 10);
                chip.setText(t);
                chip.setOnCloseIconClickListener(v -> chipGroup.removeView(chip));
                chipGroup.addView(chip);
            }
        }
    }

    private void saveNote() {
        if (inputNoteTitle.getText().toString().trim().isEmpty() && editor.getHtml().isEmpty()) {
            finish();
        } else {
            final Note note = new Note();
            note.setTitle(inputNoteTitle.getText().toString());
            note.setNoteText(editor.getHtml());
            note.setUpdateTime(textUpdateTime.getText().toString());

            ArrayList<String> tempTags = new ArrayList<>();
            for (int i = 0; i < chipGroup.getChildCount(); i++) {
                Chip chip = (Chip) chipGroup.getChildAt(i);
                tempTags.add(chip.getText().toString());
            }

            note.setTags(tempTags);

            if (availableNote != null) {
                note.setId(availableNote.getId());
                note.setUpdateTime(new SimpleDateFormat("dd MM yyyy HH:mm:ss a", Locale.getDefault()).format(new Date()));
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

    private void initMiscellaneous() {
        final LinearLayout layoutMiscellaneous = findViewById(R.id.layoutMiscellaneous);
        final BottomSheetBehavior<LinearLayout> bottomSheetBehavior = BottomSheetBehavior.from(layoutMiscellaneous);
        layoutMiscellaneous.findViewById(R.id.textMiscellaneous).setOnClickListener(v -> {
            if (bottomSheetBehavior.getState() != BottomSheetBehavior.STATE_EXPANDED) {
                bottomSheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);
            } else {
                bottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
            }
        });

        if (availableNote != null) {
            layoutMiscellaneous.findViewById(R.id.layoutDeleteNote).setVisibility(View.VISIBLE);
            layoutMiscellaneous.findViewById(R.id.textMiscellaneous).setVisibility(View.VISIBLE);
            layoutMiscellaneous.findViewById(R.id.layoutDeleteNote).setOnClickListener(v -> {
                bottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
                showDeleteDialog();
            });
        }
    }

    private void showDeleteDialog() {
        if (dialogDelete == null) {
            AlertDialog.Builder builder = new AlertDialog.Builder(NoteActivity.this);
            View view = LayoutInflater.from(this).inflate(R.layout.layout_delete_note, (ViewGroup) findViewById(R.id.layoutDeleteNoteContainer));
            builder.setView(view);
            dialogDelete = builder.create();
            if (dialogDelete.getWindow() != null) {
                dialogDelete.getWindow().setBackgroundDrawable(new ColorDrawable(0));
            }
            view.findViewById(R.id.textConfirm).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    class DeleteNoteTask extends AsyncTask<Void, Void, Void> {

                        @Override
                        protected Void doInBackground(Void... voids) {
                            NotesDatabase.getDatabase(getApplicationContext()).noteDao().deleteNote(availableNote);
                            return null;
                        }

                        @Override
                        protected void onPostExecute(Void aVoid) {
                            super.onPostExecute(aVoid);
                            Intent intent = new Intent();
                            intent.putExtra("isNoteDeleted", true);
                            setResult(RESULT_OK, intent);
                            dialogDelete.dismiss();
                            finish();
                        }
                    }
                    new DeleteNoteTask().execute();
                }
            });

            view.findViewById(R.id.textCancel).setOnClickListener(v -> dialogDelete.dismiss());
        }

        dialogDelete.show();
    }
}