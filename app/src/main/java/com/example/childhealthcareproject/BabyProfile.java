package com.example.childhealthcareproject;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;
import android.widget.Toolbar;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import java.util.Arrays;

public class BabyProfile extends AppCompatActivity {
    private static final String TAG = "BabyProfile";
    private Spinner genderSpinner;

    private String genderOption;

    private EditText firstNameEditText, lastNameEditText, birthdateEditText, heightEditText,
            weightEditText, motherNameEditText, fatherNameEditText, phoneNoEditText, addressEditText, emailEditText;

    private Button editBabyProfileButton;
    private BabyFileClass babyData;
    private String[] genders;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_baby_profile);

        genderSpinner = findViewById(R.id.genderSpinner);
        firstNameEditText = findViewById(R.id.babyFirstNameEditText);
        lastNameEditText = findViewById(R.id.babyLastNameEditText);
        birthdateEditText = findViewById(R.id.dateOfBirthEditText);
        heightEditText = findViewById(R.id.heightEditText);
        weightEditText = findViewById(R.id.weightEditText);
        motherNameEditText = findViewById(R.id.motherNameEditText);
        fatherNameEditText = findViewById(R.id.fatherNameEditText);
        phoneNoEditText = findViewById(R.id.phoneNoEditText);
        addressEditText = findViewById(R.id.addressEditText);
        emailEditText = findViewById(R.id.emailAddEditText);
        editBabyProfileButton = findViewById(R.id.editBabyProfileButton);
        genders = getResources().getStringArray(R.array.genders);

        // Check what is the previous Activity
        Intent intent = getIntent();
        String sourceActivity = intent.getStringExtra("sourceActivity");
        if(sourceActivity.equals("BabyParentActivity")){ // Already have babyData
            // Get babyData from BabyParentActivity
            babyData = intent.getParcelableExtra("babyData");
            updateUIWithData(babyData);

        } else if (sourceActivity.equals("ParentActivity")){ // Create BabyProfile first time
            // Do nothing
        }

        genderSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
                String selectedItem = parentView.getItemAtPosition(position).toString();

                if((selectedItem.equals("Male")) ||(selectedItem.equals("Female")) ||(selectedItem.equals("Other"))){
                    Toast.makeText(getApplicationContext(), "Selected item: " + selectedItem, Toast.LENGTH_SHORT).show();
                    genderOption = selectedItem;
                    Log.d(TAG, "onItemSelected: genderOption = " + genderOption);
                } else {
                    Toast.makeText(getApplicationContext(), "Need to choose baby's gender!", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void
            onNothingSelected(AdapterView<?> parentView) {
                Toast.makeText(getApplicationContext(), "Need to choose baby's gender!", Toast.LENGTH_SHORT).show();
            }
        });

        editBabyProfileButton.setOnClickListener((v) -> {
            //Get all the values
            String babyFirstName = firstNameEditText.getText().toString();
            String babyLastName = lastNameEditText.getText().toString();

            String dateOfBirth = Utility.formatDateString(birthdateEditText.getText().toString());

            float height = Float.valueOf(heightEditText.getText().toString());
            float weight = Float.valueOf(weightEditText.getText().toString());

            String motherName = motherNameEditText.getText().toString();
            String fatherName = fatherNameEditText.getText().toString();
            String phonenum = phoneNoEditText.getText().toString();
            String address = addressEditText.getText().toString();
            String email = emailEditText.getText().toString();

            babyData = new BabyFileClass(babyFirstName,
                    babyLastName,
                    motherName,
                    fatherName,
                    dateOfBirth,
                    genderOption,
                    phonenum,
                    address,
                    email,
                    weight,
                    height);

            DocumentReference documentReference;
            // update profile
            documentReference = FirebaseFirestore.getInstance().collection("Babies").document(FirebaseAuth.getInstance().getCurrentUser().getUid());

            documentReference.set(babyData).addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    if(task.isSuccessful()){
                        // profile is added
                        Toast.makeText(BabyProfile.this, "Baby profile is updated successfully", Toast.LENGTH_SHORT).show();
                        Intent intent = new Intent(getApplicationContext(), BabyParentActivity.class);
                        intent.putExtra("babyData", babyData); // Send back updated BabyProfile
                        startActivity(intent);
                        finish();
                    } else {
                        Toast.makeText(BabyProfile.this, "Baby profile update has failed", Toast.LENGTH_SHORT).show();
                    }
                }
            });
        });
    }

    private void updateUIWithData(BabyFileClass data){
        firstNameEditText.setText(babyData.getBabyFirstName());
        lastNameEditText.setText(babyData.getBabyLastName());
        birthdateEditText.setText(babyData.getDateOfBirth());

        // Find the position of the targetString in the array
        int positionGender = Arrays.asList(genders).indexOf(babyData.getGender());
        genderSpinner.setSelection(positionGender);

        heightEditText.setText(String.valueOf(babyData.getHeight()));
        weightEditText.setText(String.valueOf(babyData.getWeight()));
        motherNameEditText.setText(babyData.getMotherName());
        fatherNameEditText.setText(babyData.getFatherName());
        phoneNoEditText.setText(babyData.getPhoneNo());
        emailEditText.setText(babyData.getEmail());
        addressEditText.setText(babyData.getAddress());
    }
}

