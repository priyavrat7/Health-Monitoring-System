package com.example.childhealthcareproject;

import static android.content.ContentValues.TAG;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.Filter;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

public class DoctorActivity extends AppCompatActivity {
    private Button searchProfileButton, logoutButton;
    private RecyclerView recyclerView;
    private BabyFileAdapter babyFileAdapter;
    private String foundChildID;
    private EditText emailEditText, contactEditText, firstNameEditText, lastNameEditText;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_doctor);

        searchProfileButton = findViewById(R.id.searchProfileButton);
        logoutButton = findViewById(R.id.logoutButton);
        recyclerView = findViewById(R.id.doctor_recycler_view);
        emailEditText = findViewById(R.id.email_edit_text);
        contactEditText = findViewById(R.id.phone_edit_text);
        firstNameEditText = findViewById(R.id.first_name_edit_text);
        lastNameEditText = findViewById(R.id.last_name_edit_text);

        searchProfileButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Action when "Search Baby's Profile" button is clicked
                searchBabyProfile();
            }
        });

        logoutButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FirebaseAuth.getInstance().signOut();
                Intent intent = new Intent(getApplicationContext(), LoginActivity.class);
                startActivity(intent);
                finish();
            }
        });
    }

    // Function to search baby's profile (placeholder logic)
    private void searchBabyProfile() {
        Toast.makeText(this, "Searching for baby's profile...", Toast.LENGTH_SHORT).show();

        FirebaseFirestore db_baby = FirebaseFirestore.getInstance();

        // search for babies with any of these fields
        Query modified_qr = db_baby.collection("Babies").where(Filter.or(
                Filter.equalTo("babyFirstName", String.valueOf(firstNameEditText.getText())),
                Filter.equalTo("babyLastName", String.valueOf(lastNameEditText.getText())),
                Filter.equalTo("email", String.valueOf(emailEditText.getText())),
                Filter.equalTo("phoneNo", String.valueOf(contactEditText.getText()))
        )).orderBy("babyFirstName", Query.Direction.ASCENDING);

        setupRecyclerView(modified_qr);
    }
    private void setupRecyclerView(Query query) {
        FirestoreRecyclerOptions<BabyFileClass> options = new FirestoreRecyclerOptions.Builder<BabyFileClass>()
                .setQuery(query, BabyFileClass.class).build();
        if (recyclerView.getLayoutManager() == null) {
            recyclerView.setLayoutManager(new LinearLayoutManager(this));
        }
        if(babyFileAdapter == null) {
            babyFileAdapter = new BabyFileAdapter(options, this);
            recyclerView.setAdapter(babyFileAdapter);
            babyFileAdapter.startListening();
        } else {
            babyFileAdapter.updateOptions(options);
            babyFileAdapter.notifyDataSetChanged();
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (babyFileAdapter != null) {
            babyFileAdapter.startListening();
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (babyFileAdapter != null) {
            babyFileAdapter.stopListening();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (babyFileAdapter != null) {
            babyFileAdapter.notifyDataSetChanged();
        }
    }
}
