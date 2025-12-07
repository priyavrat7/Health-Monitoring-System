package com.example.childhealthcareproject;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

public class SplashActivity extends AppCompatActivity {
    private static final String TAG = "SplashActivity";
    private FirebaseAuth mFirebaseAuth;
    private String userType;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        mFirebaseAuth = FirebaseAuth.getInstance();

        new Handler().postDelayed(new Runnable(){
            @Override
            public void run(){
                FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
                if(currentUser == null) {
                    // User is not logged in
                    startActivity(new Intent(SplashActivity.this, LoginActivity.class));
                } else {
                    // User is logged in
                    if((mFirebaseAuth.getCurrentUser().isEmailVerified())){
                        accessUserActivity();

                    } else {
                        Utility.showToast(SplashActivity.this, "Email not verified. Please verify your email");
                        startActivity(new Intent(SplashActivity.this, LoginActivity.class));
                    }
                }
            }
        }, 1000); // Delay 1 second
    }

    private void accessUserActivity() {
        DocumentReference documentReference;
        documentReference = FirebaseFirestore.getInstance().collection("users").document(mFirebaseAuth.getCurrentUser().getUid());
        documentReference.get()
                .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        if (task.isSuccessful()) {
                            Log.d(TAG, "Redirecting to the activity of " + (String) task.getResult().get("userType"));
                            userType = (String) task.getResult().get("userType");
                            if(userType.equals("Doctor")){
                                if(task.getResult().getBoolean("approvalStatus")){
                                    Intent intent = new Intent(getApplicationContext(), DoctorActivity.class);
                                    startActivity(intent);
                                    finish();
                                }
                                else{
                                    Utility.showToast(SplashActivity.this, "You are not verified yet, Please Wait!!!");
                                    Intent intent = new Intent(getApplicationContext(), LoginActivity.class);
                                    startActivity(intent);
                                    finish();
                                }

                            } else if (userType.equals("Parent")){
                                Intent intent = new Intent(getApplicationContext(), ParentActivity.class);
                                startActivity(intent);
                                finish();
                            } else if (userType.equals("Admin")){
                                Intent intent = new Intent(getApplicationContext(), AdminApprovalActivity.class);
                                startActivity(intent);
                                finish();
                            }
                        } else {
                            Log.d(TAG, "Error getting documents: ", task.getException());
                        }
                    }
                });
    }
}