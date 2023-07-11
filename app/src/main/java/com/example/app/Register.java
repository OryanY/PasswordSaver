package com.example.app;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class Register extends AppCompatActivity {

    private FirebaseAuth mAuth;
    Button register;
    ProgressBar bar;
    private FirebaseFirestore db;
    ImageButton fab;
    EditText emailtext,password;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.register);
        mAuth = FirebaseAuth.getInstance();
        emailtext= findViewById(R.id.email);
        password= findViewById(R.id.password);
        register= findViewById(R.id.register);
        bar= findViewById(R.id.loadingbar);
        db = FirebaseFirestore.getInstance();
        fab=findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(),MainActivity.class);
                startActivity(intent);
                finish();
            }
        });
        register.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                bar.setVisibility(View.VISIBLE);
                if(TextUtils.isEmpty(emailtext.getText()))
                {
                    Toast.makeText(Register.this, "Enter Email.", Toast.LENGTH_LONG).show();
                    return;
                }
                if(TextUtils.isEmpty(password.getText()))
                {
                    Toast.makeText(Register.this, "Enter Password.", Toast.LENGTH_LONG).show();
                    return;
                }
                Task<AuthResult> authResultTask = mAuth.createUserWithEmailAndPassword(emailtext.getText().toString(), password.getText().toString()).addOnCompleteListener(new OnCompleteListener<AuthResult>() {

                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        bar.setVisibility(View.GONE);

                        if (task.isSuccessful()) {
                            // Account created successfully
                            Toast.makeText(Register.this, "Account created.", Toast.LENGTH_LONG).show();

                            // Get the authenticated user
                            FirebaseUser user = mAuth.getCurrentUser();
                            // Start the login activity
                            Intent intent = new Intent(Register.this, Login.class);
                            startActivity(intent);
                            finish();
                        }
                        else {
                            // Account creation failed
                            Exception exception = task.getException();
                            String errorMessage = exception.getMessage();
                            Toast.makeText(Register.this, "" + errorMessage, Toast.LENGTH_LONG).show();
                        }
                    }
                });

            }
        });

    }

}