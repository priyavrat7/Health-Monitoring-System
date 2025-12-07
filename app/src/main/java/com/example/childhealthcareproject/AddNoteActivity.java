package com.example.childhealthcareproject;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.Firebase;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

public class AddNoteActivity extends AppCompatActivity {

    EditText titleEditText, nameEditText, contentEditText, prescripEditText;
    Button saveNoteButton;
    private String babyDocId;

    private CollectionReference noteCollection;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_note);
        titleEditText = findViewById(R.id.note_title_text);
        nameEditText = findViewById(R.id.note_name_text);
        contentEditText = findViewById(R.id.note_content_text);
        prescripEditText = findViewById(R.id.note_prescription_text);
        saveNoteButton = findViewById(R.id.save_note_button);

        // Get babyID
        Intent intent = getIntent();
        babyDocId = intent.getStringExtra("babyDocID");
        noteCollection = Utility.getNoteCollectionReference(babyDocId);

        saveNoteButton.setOnClickListener((v) -> saveNote());
    }

    private void saveNote() {
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
        documentReference = noteCollection.document();
        documentReference.set(note).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if(task.isSuccessful()){
                    // profile is added
                    Toast.makeText(AddNoteActivity.this, "Note is added successfully, Going back to notes page", Toast.LENGTH_SHORT).show();
                    finish();
                } else {
                    Toast.makeText(AddNoteActivity.this, "Note addition has failed", Toast.LENGTH_SHORT).show();
                }
            }
        });


    }
}