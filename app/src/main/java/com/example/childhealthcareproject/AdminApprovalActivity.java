package com.example.childhealthcareproject;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AdminApprovalActivity extends AppCompatActivity {

    Spinner spinner;
    private Button approveUserButton, logoutButton;
    //private TextView textViewApproveUser;
    private String selectedDoctorEmail;
    private String pendingDrRequestID;
    private List<String> pendingDoctorArrayList;
    private ArrayAdapter<String> arrayAdopterSpinner;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_approval);

        // Initialize views
        //textViewApproveUser = findViewById(R.id.textViewApproveUser);
        spinner = findViewById(R.id.spinnerXml);
        approveUserButton = findViewById(R.id.buttonApproveUser);
        logoutButton = findViewById(R.id.btlogout);
        pendingDoctorArrayList = new ArrayList<>();

        //pendingDoctorArrayList.add("Pending Emails");// I had to add this line in order for spinner to work

        getPendingDoctorEmails();// This function will update pendingDoctorArrayList in global variable

        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                Log.d("onItemSelected is called by Spinner", "");
                // Get the selected email when an item is selected
                // Getting the Document ID where the email is selectedDoctorEmail
                selectedDoctorEmail = parent.getItemAtPosition(position).toString();
                //Log.d("2DoctorEmailSelected", selectedDoctorEmail);
                //Toast.makeText(getApplicationContext(), "Selected: " + selectedDoctorEmail, Toast.LENGTH_SHORT).show();
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // Handle no selection if needed
            }
        });


        approveUserButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getDoctorIDandApprove(selectedDoctorEmail);
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

    private void initializeSpinner(){
        // Create an ArrayAdapter using the string array and a default spinner layout
        arrayAdopterSpinner = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, pendingDoctorArrayList);
        arrayAdopterSpinner.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(arrayAdopterSpinner);
    }

    private void getDoctorIDandApprove(String inEmail){

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        Log.d("getDoctorIDisCalled", "");

        Query qr = db.collection("users")
                .whereEqualTo("emailAddress", String.valueOf(inEmail))
                .whereEqualTo("requestStatus", true);
        Log.d("QueryFromgetDoctorID to get id of this email", inEmail);
        if(pendingDrRequestID == null){
            Log.d("pendingDrRequestID is null", "");
        }
        qr.get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        Log.d("OnComplete","called in getDoctorID");
                        if (task.isSuccessful()) {
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                pendingDrRequestID = document.getId();

                                approveDoctor(pendingDrRequestID);//The reason of calling this function here only is that
                                //approveDoctor modifies the database and which required data from getDoctorID and hence
                                //approveDoctor function must be called after getting pendingDrRequestID

                                // Handle the retrieved data or call a method passing the data here
                                handleDoctorID(pendingDrRequestID);
                            }
                        } else {
                            Log.d("QueryErrorAdminApproval", "Error: " + task.getException());
                        }
                    }
                    // Method to handle the retrieved doctor ID
                    private void handleDoctorID(String doctorID) {
                        // Perform actions with the doctorID retrieved from the query
                        Log.d("DoctorID", doctorID);
                        // Call other methods or perform actions using the doctorID here
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.d("DeatilsIncorrect", "Error: " + e.getMessage());
                    }
                });
    }
    private void approveDoctor(String doctorID){

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        // Create a map with the new data
        Map<String, Object> updates = new HashMap<>();

        DocumentReference docRef = db.collection("users")
                .document(doctorID);

        updates.put("approvalStatus", true); // Update the field with a new value
        // Update the specific field in the document within the sub-collection
        Log.d("2Debug", doctorID + " approved successfully!!!");
        docRef.update(updates)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        // Handle successful update
                        Log.d("ID Approved", doctorID + " approved successfully!!!");
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        // Handle any errors
                        Log.e("TAG", "Error updating field", e);
                    }
                });
        //Update pending list everytime approve user button is clicked, so when the spinner will be called, the approved user will
        //not be there in the dropdown list.
        pendingDoctorArrayList.remove(selectedDoctorEmail);
        Log.d("Approval confirmation email will be sent to: ", selectedDoctorEmail);
        Toast.makeText(AdminApprovalActivity.this, "User: " + selectedDoctorEmail + " is approved!!!", Toast.LENGTH_SHORT).show();
        EmailActivity emailActivity = new EmailActivity();
        emailActivity.sendEmail("BabyCareApp Approval","Congratulations!!! \n Your profile is approved, you can now login if you have verified your account from the activation link, which has been already sent. ", selectedDoctorEmail);


    }
    private void getPendingDoctorEmails(){
        Log.d("get_PendingDoctorEmails is called", "");
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        Log.d("get_PendingDoctorEmails", "");

        Query qr = db.collection("users")
                .whereEqualTo("approvalStatus", false)
                .whereEqualTo("userType", "Doctor")
                .whereEqualTo("requestStatus", true);

        qr.get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {

                        if(task.isSuccessful()){
                            QuerySnapshot document1 = task.getResult();
                            if(String.valueOf(document1.isEmpty()) == "true"){
                                Log.d("DocIsNull","");
                                Toast.makeText(AdminApprovalActivity.this, "Document is empty", Toast.LENGTH_SHORT).show();
                                //Log.d("TestingLog, ", String.valueOf(document1.isEmpty()));
                            }
                            else if(String.valueOf(document1.isEmpty()) == "false"){
                                Log.d("DocIsNotNull","");
                                for (QueryDocumentSnapshot document : task.getResult()){

                                    /*
                                    pendingDoctorArrayList.add(document.getString("emailAddress"));// The email which is already added, will be added again in
                                    //the next iteration, hence it will end up in redundancy
                                    */
                                    if(pendingDoctorArrayList!=null){
                                        if(!pendingDoctorArrayList.contains(document.getString("emailAddress") ) ){
                                            pendingDoctorArrayList.add(String.valueOf(document.getString("emailAddress")));
                                            Log.d("Debug", String.valueOf(pendingDoctorArrayList));
                                        }
                                    }
                                    Log.d("Debug1", String.valueOf(document.getId() + "=>" +String.valueOf(document.getString("emailAddress")) ));
                                    Log.d("AllPendingApprovals", String.valueOf(pendingDoctorArrayList));

                                }

                            }
                            initializeSpinner();
                        }
                        else{
                            Log.d("QueryErrorAdminApproval", "Error: " + task.getException());
                        }
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.d("DeatilsIncorrect", "Error: " + e.getMessage());
                    }
                });
    }
}
