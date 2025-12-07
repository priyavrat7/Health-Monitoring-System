package com.example.childhealthcareproject;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;


public class LoginActivity extends AppCompatActivity {

    private static final String TAG = "LoginActivity";
    private EditText emailEditText, passwordEditText;
    private Button loginButton;
    private TextView registerBtnTextView;
    private TextView resetPasswordTextView;
    private ProgressBar progressBar;
    private String userType;
    private FirebaseAuth mFirebaseAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        emailEditText = findViewById(R.id.email_edit_text);
        passwordEditText = findViewById(R.id.password_edit_text);
        loginButton = findViewById(R.id.login_button);
        registerBtnTextView = findViewById(R.id.register_text_view_btn);
        resetPasswordTextView = findViewById(R.id.reset_password);
        progressBar = findViewById(R.id.progress_bar);
        mFirebaseAuth = FirebaseAuth.getInstance();


        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String email = emailEditText.getText().toString();
                String password = passwordEditText.getText().toString();

                boolean isValidated = validateData(email, password);
                if(!isValidated){
                    return;
                }

                loginAccountInFirebase(email, password);
            }
        });

        registerBtnTextView.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view){
                Intent intent = new Intent(getApplicationContext(), RegisterActivity.class);
                startActivity(intent);
//                finish();
            }
        });
        resetPasswordTextView.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view){
                Intent intent = new Intent(getApplicationContext(), EmailActivity.class);
                startActivity(intent);
//                finish();
            }
        });

    }

    private void loginAccountInFirebase(String email, String password){
        mFirebaseAuth.signInWithEmailAndPassword(email, password).addOnCompleteListener(new OnCompleteListener<AuthResult>(){
            @Override
            public void onComplete(@NonNull Task<AuthResult> task){
                changeInProgress(false);
                if(task.isSuccessful()){
                    // Login successfully
                    if((mFirebaseAuth.getCurrentUser().isEmailVerified())){
                        accessUserActivity();

                    } else {
                        Utility.showToast(LoginActivity.this, "Email not verified. Please verify your email");
                    }
                }
            }
        });
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
                                    Utility.showToast(LoginActivity.this, "You are not verified yet, Please Wait!!!");
                                }

                            } else if (userType.equals("Parent")){
                                Intent intent = new Intent(getApplicationContext(), ParentActivity.class);
                                startActivity(intent);
                                finish();
                            } else if(userType.equals("Admin")){
                                //Go to admin approval activity for Doctors
                                Intent intent = new Intent(getApplicationContext(), AdminApprovalActivity.class);
                                startActivity(intent);
                            }
                        } else {
                            Log.d(TAG, "Error getting documents: ", task.getException());
                        }
                    }
                });
    }

    private void changeInProgress(boolean inProgress){
        if(inProgress){
            progressBar.setVisibility(View.VISIBLE);
            loginButton.setVisibility(View.GONE);
        } else {
            progressBar.setVisibility(View.GONE);
            loginButton.setVisibility(View.VISIBLE);
        }
    }

    private boolean validateData(String email, String password) {
        // Validate data input by user

        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            emailEditText.setError("Email is invalid");
            return false;
        }

        if (password.length() < 6){
            passwordEditText.setError("Password must have at least 6 characters");
            return false;
        }
        return true;
    }
}
