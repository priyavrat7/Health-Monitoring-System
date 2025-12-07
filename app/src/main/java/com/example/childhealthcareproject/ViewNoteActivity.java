package com.example.childhealthcareproject;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.google.firebase.firestore.CollectionReference;

public class ViewNoteActivity extends AppCompatActivity {
    private final String TAG = "ViewNoteActivity";
    TextView titleTextView, nameTextView, contentTextView, prescripTextView;
    private String mTitle, mDoctorName, mContent, mPrescription;

    private String babyDocId, noteDocId;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_note);
        titleTextView = findViewById(R.id.note_title_text);
        nameTextView = findViewById(R.id.note_name_text);
        contentTextView = findViewById(R.id.note_content_text);
        prescripTextView = findViewById(R.id.note_prescription_text);


        // Get Note details
        Intent intent = getIntent();
        mTitle = intent.getStringExtra("Note Title");
        mDoctorName = intent.getStringExtra("Note Doctor Name");
        mContent = intent.getStringExtra("Note Content");
        mPrescription = intent.getStringExtra("Note Prescription");
        babyDocId = intent.getStringExtra("BabyDocId");
        noteDocId = intent.getStringExtra("NoteDocId");

        if (babyDocId != null && !babyDocId.isEmpty() && noteDocId != null && !noteDocId.isEmpty()){
            titleTextView.setText(mTitle);
            nameTextView.setText(mDoctorName);
            contentTextView.setText(mContent);
            prescripTextView.setText(mPrescription );
        } else {
            Log.d(TAG, "Can't get Note or Baby doc Ids, return to previous activity");
            finish();
        }
    }
}