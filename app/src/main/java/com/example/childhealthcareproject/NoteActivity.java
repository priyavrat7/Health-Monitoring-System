package com.example.childhealthcareproject;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

public class NoteActivity extends AppCompatActivity {


    NoteAdapter noteAdapter;
    RecyclerView noteRecyclerView;

    private FloatingActionButton addNoteButton;

    private String babyDocId, userType;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_note);

        addNoteButton = findViewById(R.id.addNoteButton);
        noteRecyclerView = findViewById(R.id.note_recycler_view);

        babyDocId = getIntent().getStringExtra("babyDocID");
        userType = getIntent().getStringExtra("userType");

        if (userType.equals("Parent")) {
            addNoteButton.setVisibility(View.GONE);
        }

        setupRecyclerView();
        addNoteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), AddNoteActivity.class);
                intent.putExtra("babyDocID", babyDocId);
                startActivity(intent);
            }
        });
    }

    private void setupRecyclerView() {
        CollectionReference collectionReference = FirebaseFirestore.getInstance().collection("Babies")
                .document(babyDocId).collection("Notes");
        Query query = collectionReference.orderBy("timestamp", Query.Direction.DESCENDING);
        FirestoreRecyclerOptions<Note> options = new FirestoreRecyclerOptions.Builder<Note>()
                .setQuery(query, Note.class).build();
        noteRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        noteAdapter = new NoteAdapter(options, this, babyDocId, userType);
        noteRecyclerView.setAdapter(noteAdapter);
    }

    @Override
    protected void onStart() {
        super.onStart();
        noteAdapter.startListening();
    }

    @Override
    protected void onStop() {
        super.onStop();
        noteAdapter.stopListening();
    }

    @Override
    protected void onResume() {
        super.onResume();
        noteAdapter.notifyDataSetChanged();
    }
}
