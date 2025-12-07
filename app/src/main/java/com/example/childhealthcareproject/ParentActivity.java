package com.example.childhealthcareproject;

import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;


import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.WriteBatch;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class ParentActivity extends AppCompatActivity {
    private static final String TAG = "ParentActivity";
    private Button scanConnectButton, accessProfileButton, logoutButton, viewNotesBtn;
    private BluetoothDevice bleDevice;
    private boolean isRecording;
    private int color_green, color_red;

    private boolean isBabyProfileCreated = false;
    private BabyFileClass babyData;
    private String babyID;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_parent);

        // Initialize color for buttons
        color_green = ContextCompat.getColor(this, R.color.green);
        color_red = ContextCompat.getColor(this, R.color.red);

        // Get babyID
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        babyID = currentUser.getUid();

        // Initialize Buttons
        scanConnectButton = findViewById(R.id.scanConnectButton);
        accessProfileButton = findViewById(R.id.accessProfileButton);
        logoutButton = findViewById(R.id.logoutButton);
        viewNotesBtn = findViewById(R.id.viewNotesButton);

        isRecording = false;

        // Check if baby profile is created
        isBabyProfileDefined();

        // Get BLE device
        Intent intent = getIntent();
        if(intent.hasExtra("BleDevice")){
            bleDevice = intent.getParcelableExtra("BleDevice");
            Log.d(TAG, "getBLEdevice: done");
        }

        viewNotesBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), NoteActivity.class);
                intent.putExtra("babyDocID", babyID);
                intent.putExtra("userType", "Parent");
                startActivity(intent);
            }
        });

        scanConnectButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                Intent intent = new Intent(getApplicationContext(), BLEScanActivity.class);
                Intent intent = new Intent(getApplicationContext(), ParentTempActivity.class);
                startActivity(intent);
//                finish();
            }
        });

        accessProfileButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isBabyProfileCreated) {
                    // Action when "Access Baby's Profile" button is clicked
                    Toast.makeText(ParentActivity.this, "Access Baby's Profile Clicked", Toast.LENGTH_SHORT).show();
                    // Add your logic here for accessing the baby's profile
                    Intent intent = new Intent(getApplicationContext(), BabyParentActivity.class);
                    intent.putExtra("babyData", babyData);
                    startActivity(intent);
//                    finish();
                } else {
                    // Action when "Access Baby's Profile" button is clicked
                    Toast.makeText(ParentActivity.this, "Create Baby's Profile Clicked", Toast.LENGTH_SHORT).show();
                    // Add your logic here for accessing the baby's profile
                    Intent intent = new Intent(getApplicationContext(), BabyProfile.class);
                    intent.putExtra("sourceActivity", "ParentActivity");
                    startActivity(intent);
//                    finish();
                }
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

    private void isBabyProfileDefined() {
        // Fetch Baby profile data
        DocumentReference documentReference;
        FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();

        // Get reference to baby profile document on Firebase
        documentReference = FirebaseFirestore.getInstance().collection("Babies").document(firebaseUser.getUid());
        Log.d("Parent Activity, Baby ID", String.valueOf(documentReference));
        documentReference.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if (document.exists()) {
                        Log.d(TAG, "DocumentSnapshot data: " + document.getData());

                        scanConnectButton.setVisibility(View.VISIBLE);
                        viewNotesBtn.setVisibility(View.VISIBLE);
                        accessProfileButton.setText("Access Baby's Profile");
                        isBabyProfileCreated = true;

                        Map<String, Object> data = document.getData();
                        babyData = new BabyFileClass(data);
                    } else {
                        Log.d(TAG, "No such document");
                        scanConnectButton.setVisibility(View.GONE);
                        viewNotesBtn.setVisibility(View.GONE);
                        accessProfileButton.setText("Create Baby's Profile");
                        isBabyProfileCreated = false;
                    }
                } else {
                    Log.d(TAG, "get failed with ", task.getException());
                }
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        isBabyProfileDefined();
    }
}
