package com.example.hikokainotes.activities;

import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;

import com.example.hikokainotes.R;
import com.example.hikokainotes.adapters.NotesAdapter;
import com.example.hikokainotes.adapters.SpinnerAdapter;
import com.example.hikokainotes.database.NotesDatabase;
import com.example.hikokainotes.entities.Note;
import com.example.hikokainotes.listeners.NotesListener;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipDrawable;
import com.google.android.material.chip.ChipGroup;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MainActivity extends AppCompatActivity implements NotesListener {
    public static final int REQUEST_CODE_ADD_NOTE = 1;
    public static final int REQUEST_CODE_UPDATE_NOTE = 2;
    public static final int REQUEST_CODE_SHOW_NOTES = 3;

    private RecyclerView notesRecyclerView;
    private List<Note> noteList;
    private NotesAdapter notesAdapter;

    private int noteClickedPosition = -1;

    private AlertDialog dialogDelete;

    int[] searchMode = {R.drawable.ic_search, R.drawable.ic_tag};
    private ChipGroup chipGroupSearch;
    private EditText inputSearch;

    int searchChosen = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ImageView addButton = findViewById(R.id.imageAddNoteMain);
        addButton.setOnClickListener(v -> {
            if (inputSearch.getText().toString().equals("") && chipGroupSearch.getChildCount() == 0) {
                startActivityForResult(new Intent(getApplicationContext(), NoteActivity.class), REQUEST_CODE_ADD_NOTE);
            } else {
                Toast toast = Toast.makeText(getApplicationContext(), "Cannot add new note while searching", Toast.LENGTH_LONG);
                toast.show();
            }
        });

        notesRecyclerView = findViewById(R.id.notesRecyclerView);
        notesRecyclerView.setLayoutManager(new StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL));

        noteList = new ArrayList<>();
        notesAdapter = new NotesAdapter(noteList, this);
        notesRecyclerView.setAdapter(notesAdapter);

        getNotes(REQUEST_CODE_SHOW_NOTES, false);

        inputSearch = findViewById(R.id.inputSearch);
        chipGroupSearch = findViewById(R.id.chip_group_search);

        // Text Listener search word
        TextWatcher textWatcher = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (searchChosen == 0) {
                    notesAdapter.cancelTimer();
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
                if (noteList.size() != 0 && searchChosen == 0) {
                    notesAdapter.searchNotes(s.toString());
                }
            }
        };

        // Action Listener search tag
        TextView.OnEditorActionListener onEditorActionListener = (v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_DONE && searchChosen == 1) {
                if (chipGroupSearch.getVisibility() == View.GONE) {
                    chipGroupSearch.setVisibility(View.VISIBLE);
                }

                Chip chip = new Chip(MainActivity.this);
                ChipDrawable drawable = ChipDrawable.createFromAttributes(MainActivity.this, null, 0, R.style.Widget_MaterialComponents_Chip_Entry);
                chip.setChipDrawable(drawable);
                chip.setCheckable(false);
                chip.setClickable(false);
                chip.setPadding(60, 10, 60, 10);
                chip.setText(inputSearch.getText().toString());
                chip.setOnCloseIconClickListener(v1 -> {
                    chipGroupSearch.removeView(chip);
                    notesAdapter.searchTags(getCurrentTags());
                });

                chipGroupSearch.addView(chip);
                inputSearch.setText("");

                notesAdapter.searchTags(getCurrentTags());
                return true;
            }
            return false;
        };

        Spinner spin = findViewById(R.id.spinner);
        spin.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                inputSearch.setText("");
                searchChosen = position;
                if (position == 0) {
                    chipGroupSearch.removeAllViews();
                    notesAdapter.searchTags(getCurrentTags());
                    chipGroupSearch.setVisibility(View.GONE);
                    inputSearch.addTextChangedListener(textWatcher);

                } else if (position == 1) {
                    inputSearch.setOnEditorActionListener(onEditorActionListener);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        SpinnerAdapter spinnerAdapter = new SpinnerAdapter(getApplicationContext(), searchMode);
        spin.setAdapter(spinnerAdapter);
    }

    private ArrayList<String> getCurrentTags() {
        ArrayList<String> tagSearch = new ArrayList<>();
        for (int i = 0; i < chipGroupSearch.getChildCount(); i++) {
            Chip c = (Chip) chipGroupSearch.getChildAt(i);
            tagSearch.add(c.getText().toString());
        }
        return tagSearch;
    }

    @Override
    public void onNoteClicked(Note note, int position) {
        noteClickedPosition = position;
        Intent intent = new Intent(getApplicationContext(), NoteActivity.class);
        intent.putExtra("isViewOrUpdate", true);
        intent.putExtra("note", note);
        startActivityForResult(intent, REQUEST_CODE_UPDATE_NOTE);
    }

    @Override
    public void onDeleteClicked(int position) {
        noteClickedPosition = position;
        showDeleteDialog();
    }

    private void showDeleteDialog() {
        if (dialogDelete == null) {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            View view = LayoutInflater.from(this).inflate(R.layout.layout_delete_note, (ViewGroup) findViewById(R.id.layoutDeleteNoteContainer));
            builder.setView(view);
            dialogDelete = builder.create();
            if (dialogDelete.getWindow() != null) {
                dialogDelete.getWindow().setBackgroundDrawable(new ColorDrawable(0));
            }
            view.findViewById(R.id.textConfirm).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    // Delete task
                    ExecutorService executor = Executors.newSingleThreadExecutor();
                    executor.execute(() -> {
                        NotesDatabase.getDatabase(getApplicationContext()).noteDao().deleteNote(noteList.get(noteClickedPosition));

                        new Handler(Looper.getMainLooper()).post(() -> {
                            Intent intent = new Intent();
                            intent.putExtra("isNoteDeleted", true);
                            setResult(RESULT_OK, intent);
                            dialogDelete.dismiss();
                        });
                    });

                    getNotes(REQUEST_CODE_UPDATE_NOTE, true);
                }
            });

            view.findViewById(R.id.textCancel).setOnClickListener(v -> dialogDelete.dismiss());
        }
        dialogDelete.show();
    }


    private void getNotes(final int requestCode, final boolean isNoteDeleted) {
        // Get notes task
        ExecutorService executor = Executors.newSingleThreadExecutor();
        executor.execute(() -> {
            List<Note> notes = NotesDatabase.getDatabase(getApplicationContext()).noteDao().getAllNotes();
            new Handler(Looper.getMainLooper()).post(() -> {
                if (requestCode == REQUEST_CODE_SHOW_NOTES) {
                    noteList.addAll(notes);
                    notesAdapter.notifyDataSetChanged();
                } else if (requestCode == REQUEST_CODE_ADD_NOTE) {
                    noteList.add(0, notes.get(0));
                    notesAdapter.notifyItemInserted(0);
                    notesRecyclerView.smoothScrollToPosition(0);
                } else if (requestCode == REQUEST_CODE_UPDATE_NOTE) {
                    noteList.remove(noteClickedPosition);
                    if (isNoteDeleted) {
                        notesAdapter.notifyItemRemoved(noteClickedPosition);
                    } else {
                        noteList.add(noteClickedPosition, notes.get(noteClickedPosition));
                        notesAdapter.notifyItemChanged(noteClickedPosition);
                    }
                    if (searchChosen == 0) {
                        String temp = inputSearch.getText().toString();
                        inputSearch.setText(temp);
                        inputSearch.setSelection(temp.length());
                    } else if (searchChosen == 1) {
                        notesAdapter.searchTags(getCurrentTags());
                    }
                }
            });
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE_ADD_NOTE && resultCode == RESULT_OK) {
            getNotes(REQUEST_CODE_ADD_NOTE, false);
        } else if (requestCode == REQUEST_CODE_UPDATE_NOTE && resultCode == RESULT_OK) {
            if (data != null) {
                getNotes(REQUEST_CODE_UPDATE_NOTE, data.getBooleanExtra("isNoteDeleted", false));
            }
        }
    }
}