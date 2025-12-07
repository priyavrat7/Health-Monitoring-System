package com.example.childhealthcareproject;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.io.Serializable;
import java.util.Map;

public class BabyParentActivity extends AppCompatActivity {
    private final String TAG = "BabyParentActivity";
    private Button editBabyProfileBtn;
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
    private BabyFileClass babyData;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_baby_parent);

        editBabyProfileBtn = findViewById(R.id.editBabyProfile);
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

        // Fetch baby's data from ParentActivity
        Intent intent = getIntent();
        babyData = intent.getParcelableExtra("babyData");
        getBabyData();

        editBabyProfileBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), BabyProfile.class);
                intent.putExtra("babyData", babyData);
                intent.putExtra("sourceActivity", "BabyParentActivity");
                startActivity(intent);
                finish();
            }
        });


    }

    private void getBabyData(){
        Log.d(TAG, "getBabyData: start");
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
    }
}
