package com.example.childhealthcareproject;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import android.content.Intent;
import android.content.res.Resources;
import android.graphics.PorterDuff;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

public class RegisterActivity extends AppCompatActivity {
    private static final String TAG = "RegisterActivity";
    private EditText emailEditText, passwordEditText, confirmPasswordEditText;
    private Button registerButton;
    private TextView loginBtnTextView, passwordStrengthTextView, confirmPassCheching;
    private ProgressBar progressBarCloud, progressBarPassword;
    private Spinner userTypeSpinner;
    private String email, password, confirmPassword, userType;
    private FirebaseAuth mFirebaseAuth;
    private int red, orange, yellow, green;
    private CharSequence dynamicPass, dynamicConfirmpass;
    private Resources res;
    private PasswordStrength passwordStrength;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        emailEditText = findViewById(R.id.email_edit_text);
        passwordEditText = findViewById(R.id.password_edit_text);
        confirmPasswordEditText = findViewById(R.id.confirm_password_edit_text);
        registerButton = findViewById(R.id.register_button);

        progressBarCloud = findViewById(R.id.progress_bar_cloud);
        progressBarPassword = findViewById(R.id.progress_bar_password);
        progressBarPassword.setVisibility(View.INVISIBLE);

        loginBtnTextView = findViewById(R.id.login_text_view_btn);
        userTypeSpinner = findViewById(R.id.user_type_spinner);
        passwordStrengthTextView = findViewById(R.id.pass_strength);
        confirmPassCheching = findViewById(R.id.confirmPassCheck);
        res = getResources();

        mFirebaseAuth = FirebaseAuth.getInstance();

        loginBtnTextView.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view){
                Intent intent = new Intent(getApplicationContext(), LoginActivity.class);
                startActivity(intent);
                finish();
            }
        });

        registerButton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view){
                email = emailEditText.getText().toString();
                password = passwordEditText.getText().toString();
                confirmPassword = confirmPasswordEditText.getText().toString();

                boolean isValidated = validateData(email, password, confirmPassword, userType);
                if(!isValidated){
                    return;
                }

                createAccountInFirebase(email, password);
            }
        });

        userTypeSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
                String selectedItem = parentView.getItemAtPosition(position).toString();

                if((selectedItem.equals("Parent")) ||(selectedItem.equals("Doctor"))){
                    Toast.makeText(getApplicationContext(), "Selected item: " + selectedItem, Toast.LENGTH_SHORT).show();
                    userType = selectedItem;
                    Log.d("Spinner Item Selected", userType);
                } else {
                    //Toast.makeText(getApplicationContext(), "Need to choose user type! ", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void
            onNothingSelected(AdapterView<?> parentView) {
                //Toast.makeText(getApplicationContext(), "Need to choose user type! ", Toast.LENGTH_SHORT).show();
            }
        });

        passwordEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                calculatePasswordStrength(s.toString());
                dynamicPass = s;
                //Log.d("Dynamic_Password", String.valueOf(dynamicPass));
                if(dynamicPass.length() != 0 && dynamicPass != null && dynamicConfirmpass != null){
                    //if(dynamicPass.equals(dynamicConfirmpass)){       // Throwing False even after the dynamicPass and dynamicConfirmPass are equal
                    if(dynamicPass.toString().equals(dynamicConfirmpass.toString())){         // An alternative way
                        Log.d("PassMatched", "");
                        confirmPassCheching.setText("Password matched");
                        confirmPassCheching.setTextColor(green);
                    }
                    else{
                        Log.d("PassNotMatched", "");
                        confirmPassCheching.setText("Password didn't match");
                        confirmPassCheching.setTextColor(red);
                    }
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });
        confirmPasswordEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                dynamicConfirmpass = s;
                Log.d("Dynamic_Password", String.valueOf(dynamicPass));
                Log.d("Dynamic_Confirm_Password", String.valueOf(dynamicConfirmpass));
                Log.d("PassMatchCondition", String.valueOf(dynamicPass.toString().equals(dynamicConfirmpass.toString())));
                if(dynamicPass.length() != 0 && dynamicPass != null && dynamicConfirmpass != null){
                    //if(dynamicPass.equals(dynamicConfirmpass)){       // Throwing False even after the dynamicPass and dynamicConfirmPass are equal
                    if(dynamicPass.toString().equals(dynamicConfirmpass.toString())){         // An alternative way
                        Log.d("PassMatched", "");
                        confirmPassCheching.setText("Password matched");
                        confirmPassCheching.setTextColor(green);
                    }
                    else{
                        Log.d("PassNotMatched", "");
                        confirmPassCheching.setText("Password didn't match");
                        confirmPassCheching.setTextColor(red);
                    }
                }

            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

    }

    private boolean validateData(String email, String password, String confirmPassword, String usrType) {
        // Validate data input by user

        if(usrType == null){
            Toast.makeText(getApplicationContext(), "Select User Type ", Toast.LENGTH_SHORT).show();
            return false;
        }

        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            emailEditText.setError("Email is invalid");
            return false;
        }

        if (passwordStrength.msg == res.getIdentifier("weak", "string", getPackageName())){
            passwordEditText.setError("Password must have at least 6 characters");
            Toast.makeText(getApplicationContext(), "Password is weak ", Toast.LENGTH_SHORT).show();
            return false;
        }

        if(!password.equals(confirmPassword)){
            confirmPasswordEditText.setError("Password not matched");
            return false;
        }

        return true;
    }

    private void createAccountInFirebase(String email, String password){
        changeInProgress(true);

        mFirebaseAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        changeInProgress(false);
                        if (task.isSuccessful()) {
                            // Creating account is done
                            Utility.showToast(RegisterActivity.this, "Successfully created account, Check email to verify");
                            FirebaseUser user = mFirebaseAuth.getCurrentUser();

                            addUserProfile(email, userType);

                        } else {
                            // Failed to create account
                            Utility.showToast(RegisterActivity.this, task.getException().getLocalizedMessage());
                        }
                    }

                });
    }

    private void changeInProgress(boolean inProgress){
        if(inProgress){
            progressBarCloud.setVisibility(View.VISIBLE);
            registerButton.setVisibility(View.GONE);
        } else {
            progressBarCloud.setVisibility(View.GONE);
            registerButton.setVisibility(View.VISIBLE);
        }
    }

    private void addUserProfile(String inEmail, String inUserType) {

        if(inUserType.equals("Parent")){
            FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
            UserProfileClass userProfileParent = new UserProfileClass(inEmail, inUserType, firebaseUser.getUid());
            DocumentReference documentReference;
            documentReference = FirebaseFirestore.getInstance().collection("users").document(firebaseUser.getUid());

            documentReference.set(userProfileParent).addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    if(task.isSuccessful()){
                        // profile is added
                        Toast.makeText(RegisterActivity.this, "A new user profile is added successfully, Going back to login page", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(RegisterActivity.this, "Profile addition has failed", Toast.LENGTH_SHORT).show();
                    }
                    firebaseUser.sendEmailVerification();

                    mFirebaseAuth.signOut();

                    Intent intent = new Intent(getApplicationContext(), LoginActivity.class);
                    startActivity(intent);
                    finish();
                }
            });
        }
        if(inUserType.equals("Doctor")){
            FirebaseUser firebaseUserDoc = FirebaseAuth.getInstance().getCurrentUser();
            UserDoctorClass userProfileDoctor = new UserDoctorClass(inEmail, true, false, firebaseUserDoc.getUid(), "Doctor");
            DocumentReference documentReferenceDoc;
            documentReferenceDoc = FirebaseFirestore.getInstance().collection("users").document(firebaseUserDoc.getUid());

            documentReferenceDoc.set(userProfileDoctor).addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    if(task.isSuccessful()){
                        // doctor profile request is added to pendingRequest collection
                        Toast.makeText(RegisterActivity.this, "The Doctor's request is added and admin will review it", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(RegisterActivity.this, "The request to add Doctor profile is failed", Toast.LENGTH_SHORT).show();
                    }
                    firebaseUserDoc.sendEmailVerification();

                    mFirebaseAuth.signOut();

                    Intent intent = new Intent(getApplicationContext(), EmailDocActivity.class);
                    startActivity(intent);
                    finish();
                }
            });
        }




    }
    /*private void addDoctorRequest(String docEmail){
        FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        UserProfileClass userProfile = new UserProfileClass(docEmail, "false", "true", firebaseUser.getUid());
        DocumentReference documentReference;
        documentReference = FirebaseFirestore.getInstance().collection("pendingRequests").document(firebaseUser.getUid());
        documentReference.set(userProfile).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if(task.isSuccessful()){
                    // doctor profile request is added to pendingRequest collection
                    Toast.makeText(RegisterActivity.this, "The Doctor's request is added and admin will review it", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(RegisterActivity.this, "The request to add Doctor profile is failed", Toast.LENGTH_SHORT).show();
                }
                firebaseUser.sendEmailVerification();
            }
        });
    }*/

    private void calculatePasswordStrength(String str) {
        // Now, we need to define a PasswordStrength enum
        // with a calculate static method returning the password strength
        passwordStrength = PasswordStrength.calculate(str);

        passwordStrengthTextView.setText(passwordStrength.msg);
        Log.d("ResourceID_From_PasswordStrengthClass", String.valueOf(passwordStrength.msg) );
        //passwordStrength.msg is of int type and it gives the resource id of String resources associated with four enums
        // defined and each enum us linked with resources of strings.xml;

        passwordStrengthTextView.setTextColor(passwordStrength.color);
        progressBarPassword.setVisibility(View.VISIBLE);

        if(passwordStrength.msg == res.getIdentifier("weak", "string", getPackageName())){
            progressBarPassword.setProgress(25);
            red = ContextCompat.getColor(this, R.color.red);
            progressBarPassword.getProgressDrawable().setColorFilter(red, PorterDuff.Mode.SRC_IN);
        }
        if(passwordStrength.msg == res.getIdentifier("medium", "string", getPackageName())){
            progressBarPassword.setProgress(50);
            orange = ContextCompat.getColor(this, R.color.orange);
            progressBarPassword.getProgressDrawable().setColorFilter(yellow, PorterDuff.Mode.SRC_IN);
        }
        if(passwordStrength.msg == res.getIdentifier("strong", "string", getPackageName())){
            progressBarPassword.setProgress(75);
            yellow = ContextCompat.getColor(this, R.color.yellow);
            progressBarPassword.getProgressDrawable().setColorFilter(green, PorterDuff.Mode.SRC_IN);
        }
        if(passwordStrength.msg == res.getIdentifier("very_strong", "string", getPackageName())){
            progressBarPassword.setProgress(100);
            green = ContextCompat.getColor(this, R.color.green);
            progressBarPassword.getProgressDrawable().setColorFilter(green, PorterDuff.Mode.SRC_IN);
        }


    }
}