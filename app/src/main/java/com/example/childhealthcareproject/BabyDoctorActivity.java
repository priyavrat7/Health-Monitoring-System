package com.example.childhealthcareproject;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.Map;

public class BabyDoctorActivity extends AppCompatActivity {
    private static final String TAG = "BabyDoctorActivity";
    private TextView firstNameTextView,
            lastNameTextView,
            birthdateTextView,
            genderTextView,
            heightTextView,
            weightTextView,
            motherNameTextView,
            fatherNameTextView,
            parentPhoneNumberTextView,
            parentEmailTextView,
            addressTextView;
    private Button viewNotesButton, viewTempDataBtn;
    private String babyID;
    private BabyFileClass babyData;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_baby_doctor);

        firstNameTextView = findViewById(R.id.first_name_text_view);
        lastNameTextView = findViewById(R.id.last_name_text_view);
        birthdateTextView = findViewById(R.id.birthdate_text_view);
        genderTextView = findViewById(R.id.gender_text_view);
        heightTextView = findViewById(R.id.height_text_view);
        weightTextView = findViewById(R.id.weight_text_view);
        motherNameTextView = findViewById(R.id.mother_name_text_view);
        fatherNameTextView = findViewById(R.id.father_name_text_view);
        parentPhoneNumberTextView = findViewById(R.id.parent_phone_number_text_view);
        parentEmailTextView = findViewById(R.id.parent_email_text_view);
        addressTextView = findViewById(R.id.address_text_view);
        viewNotesButton = findViewById(R.id.viewNotesButton);
        viewTempDataBtn = findViewById(R.id.view_temp_btn);

        Intent intent = getIntent();
        babyID = intent.getStringExtra("childDataID");
        Log.d("ChildID in BabyDoctorActivity",String.valueOf(babyID));

        getBabyData();

        viewNotesButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), NoteActivity.class);
                intent.putExtra("babyDocID", babyID);
                intent.putExtra("userType", "Doctor");
                startActivity(intent);
            }
        });

        viewTempDataBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), DoctorTempActivity.class);
                intent.putExtra("babyID", babyID);
                startActivity(intent);
                finish();
            }
        });
    }

    private void getBabyData() {
        DocumentReference documentReference;
        documentReference = FirebaseFirestore.getInstance().collection("Babies").document(babyID);
        Log.d("BabyDoctorActivity", "Fetched baby data of babyID = "+ babyID);
        documentReference.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if (document.exists()) {
                        Log.d(TAG, "DocumentSnapshot data: " + document.getData());
                        Map<String, Object> data = document.getData();
                        babyData = new BabyFileClass(data);

                        firstNameTextView.setText(babyData.getBabyFirstName());
                        lastNameTextView.setText(babyData.getBabyLastName());
                        birthdateTextView.setText(babyData.getDateOfBirth());
                        genderTextView.setText(babyData.getGender());
                        heightTextView.setText(String.valueOf(babyData.getHeight()));
                        weightTextView.setText(String.valueOf(babyData.getWeight()));
                        motherNameTextView.setText(babyData.getMotherName());
                        fatherNameTextView.setText(babyData.getFatherName());
                        parentPhoneNumberTextView.setText(babyData.getPhoneNo());
                        parentEmailTextView.setText(babyData.getEmail());
                        addressTextView.setText(babyData.getAddress());
                    } else {
                        Log.d(TAG, "No such document");
                    }
                } else {
                    Log.d(TAG, "get failed with ", task.getException());
                }
            }
        });
    }
}
