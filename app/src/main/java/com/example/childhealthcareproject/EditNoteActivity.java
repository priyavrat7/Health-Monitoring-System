package com.example.childhealthcareproject;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

public class EditNoteActivity extends AppCompatActivity {

    private final String TAG = "EditNoteActivity";
    EditText titleEditText, nameEditText, contentEditText, prescripEditText;
    Button updateNoteBtn, deleteNoteBtn;
    private String babyDocId;
    private String updTitle, updDoctorName, updContent, updPrescription, noteDocId;

    private CollectionReference noteCollection;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_note);
        titleEditText = findViewById(R.id.note_title_text);
        nameEditText = findViewById(R.id.note_name_text);
        contentEditText = findViewById(R.id.note_content_text);
        prescripEditText = findViewById(R.id.note_prescription_text);
        updateNoteBtn = findViewById(R.id.update_note_button);
        deleteNoteBtn = findViewById(R.id.delete_note_button);

        // Get Note details
        Intent intent = getIntent();
        updTitle = intent.getStringExtra("Note Title");
        updDoctorName = intent.getStringExtra("Note Doctor Name");
        updContent = intent.getStringExtra("Note Content");
        updPrescription = intent.getStringExtra("Note Prescription");
        babyDocId = intent.getStringExtra("BabyDocId");
        noteDocId = intent.getStringExtra("NoteDocId");

        if (babyDocId != null && !babyDocId.isEmpty() && noteDocId != null && !noteDocId.isEmpty()){
            titleEditText.setText(updTitle);
            nameEditText.setText(updDoctorName);
            contentEditText.setText(updContent);
            prescripEditText.setText(updPrescription);
        } else {
            Log.d(TAG, "Can't get Note or Baby doc Ids, return to previous activity");
            finish();
        }


        noteCollection = Utility.getNoteCollectionReference(babyDocId);

        updateNoteBtn.setOnClickListener((v) -> updateNote());
        deleteNoteBtn.setOnClickListener((v) -> deleteNote());
    }

    private void updateNote() {
        String noteTitle = titleEditText.getText().toString();
        String noteDoctorName = nameEditText.getText().toString();
        String noteContent = contentEditText.getText().toString();
        String prescription = prescripEditText.getText().toString();

        if(noteTitle == null || noteTitle.isEmpty()){
            titleEditText.setError("Title is required");
            return;
        }
        if(noteDoctorName == null || noteDoctorName.isEmpty()){
            titleEditText.setError("Doctor Name is required");
            return;
        }

        Note note = new Note();
        note.setTitle(noteTitle);
        note.setDoctorName(noteDoctorName);
        note.setContent(noteContent);
        note.setPrescription(prescription);
        note.setTimestamp(Timestamp.now());

        DocumentReference documentReference;
        documentReference = noteCollection.document(noteDocId);
        documentReference.set(note).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if(task.isSuccessful()){
                    // note is updated
                    Toast.makeText(EditNoteActivity.this, "Note is updated successfully, Going back to notes page", Toast.LENGTH_SHORT).show();
                    finish();
                } else {
                    Toast.makeText(EditNoteActivity.this, "Note update has failed", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void deleteNote() {

        DocumentReference documentReference;
        documentReference = noteCollection.document(noteDocId);
        documentReference.delete().addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()) {
                    // note is deleted
                    Toast.makeText(EditNoteActivity.this, "Note is deleted successfully, Going back to notes page", Toast.LENGTH_SHORT).show();
                    finish();
                } else {
                    Toast.makeText(EditNoteActivity.this, "Note deletion has failed", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
}