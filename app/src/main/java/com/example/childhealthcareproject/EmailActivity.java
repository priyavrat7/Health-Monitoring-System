package com.example.childhealthcareproject;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;

import java.util.Properties;
import java.util.Random;

import javax.mail.Authenticator;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

public class EmailActivity extends AppCompatActivity {
    private Button buttonEmail, generatePass;
    private EditText editText, otp;

    private int onetimepassword;


    private String subject, content, receiverEmail, senderEmail, senderPassword, gmailHost;

    private FirebaseAuth mFirebaseAuth;
    private ProgressBar progressBar;

    @SuppressLint("MissingInflatedId")
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.email_activity);
        buttonEmail = findViewById(R.id.emailButton);
        editText = findViewById(R.id.editTextEmail);
        //otp = findViewById(R.id.onetimepassword);
        //generatePass = findViewById(R.id.newPassButton);
        progressBar = findViewById(R.id.progressBar);

        mFirebaseAuth = FirebaseAuth.getInstance();
        buttonEmail.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                receiverEmail = editText.getText().toString();

                if (TextUtils.isEmpty(receiverEmail)) {
                    Toast.makeText(getApplication(), "Enter your registered email id", Toast.LENGTH_SHORT).show();
                    return;
                }

                progressBar.setVisibility(View.VISIBLE);

                mFirebaseAuth.sendPasswordResetEmail(receiverEmail)
                        .addOnCompleteListener((OnCompleteListener<Void>) task -> {
                            if (task.isSuccessful()) {
                                Toast.makeText(EmailActivity.this, "We have sent you instructions to reset your password!", Toast.LENGTH_SHORT).show();
                            } else {
                                Toast.makeText(EmailActivity.this, task.getException().getLocalizedMessage(), Toast.LENGTH_SHORT).show();
                            }

                            progressBar.setVisibility(View.GONE);
                            startActivity(new Intent(EmailActivity.this, LoginActivity.class));
                        });

            }
        });
        /*
        generatePass.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view){
                if(Integer.toString(onetimepassword).equals(otp.getText().toString())){
                    Toast.makeText(EmailActivity.this, "Password reset succesfully", Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(getApplicationContext(), LoginActivity.class);
                    startActivity(intent);
                    finish();

                }
                else{
                    Toast.makeText(EmailActivity.this, "Incorrect otp, Try again!!!", Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(getApplicationContext(), LoginActivity.class);
                    startActivity(intent);
                    finish();
                }

            }
        });
        */
    }


/*
    private int passGenerator(){
        onetimepassword = new Random().nextInt(999999);
        return onetimepassword;
    }
*/
    protected void sendEmail(String subject, String content, String email){

        senderEmail = "babycareapp2023@gmail.com";
        //senderPassword = "BabyCare@2023"; //This will no longer work
        senderPassword = "usroyiskdpgcpahm";// Enable 2FA in google account, click on manage my google account then Security.
        //Click on 2FA(make sure it is enabled), scroll down and go to App passwords and create a new one and use here in senderPassword.

        gmailHost = "smtp.gmail.com";
        Properties properties = System.getProperties();
        properties.put("mail.smtp.host", gmailHost);
        properties.put("mail.smtp.port", "465");
        properties.put("mail.smtp.ssl.enable", "true");
        properties.put("mail.smtp.auth", "true");

        javax.mail.Session session = Session.getInstance(properties, new Authenticator(){
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(senderEmail, senderPassword);
            }
        });
        MimeMessage mimeMessage = new MimeMessage(session);
        try {
            mimeMessage.addRecipient(Message.RecipientType.TO, new InternetAddress(email));

            mimeMessage.setSubject(subject);

            mimeMessage.setText((content));


            //Thread to send the email
            Thread thread = new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        Transport.send(mimeMessage);    // This is a blocking call, so it needs to be run in a thread
                    } catch (MessagingException e) {
                        throw new RuntimeException(e);
                    }
                }
            });
            thread.start();
            //Thread to send the email



        } catch (AddressException e) {
            throw new RuntimeException(e);
        } catch (MessagingException e) {
            throw new RuntimeException(e);
        }

    }

}
/*
If you are encountering issues with resolving the symbol javax.mail.Authenticator, it could be due to various reasons, such as:

Missing JavaMail library: Ensure that you have added the JavaMail library correctly to your project's dependencies in the build.gradle file.

gradle
Copy code
implementation 'com.sun.mail:android-mail:1.6.7'
implementation 'com.sun.mail:android-activation:1.6.7'
After adding these dependencies, sync Gradle to download the necessary files.

Potential IDE indexing or caching issue: Sometimes, Android Studio might have difficulties resolving symbols due to indexing or caching problems. You can try the following:

Go to File -> Invalidate Caches / Restart and select Invalidate and Restart. This action clears the cache and restarts Android Studio, which might resolve the issue.
Conflict with other libraries or dependencies: There might be conflicts between different libraries in your project. Check for any conflicts between the JavaMail library and other dependencies. Ensure that there are no duplicate or conflicting dependencies causing issues.

Incorrect import statement or package name: Double-check the import statement in your Java or Kotlin file to ensure it matches the correct package and class name for the Authenticator class.

java
Copy code
import javax.mail.Authenticator;
Check for typos or mistakes in your code: Sometimes, simple typos or errors in the code can lead to symbols not being resolved.

If you've checked these points and the issue persists, try the following:

Restart Android Studio.
Clean and rebuild your project by going to Build -> Clean Project and then Build -> Rebuild Project.
If none of these solutions resolve the problem, consider providing more specific details or code snippets related to how you're using the javax.mail.Authenticator. This information might help in diagnosing the issue more accurately.
*/
