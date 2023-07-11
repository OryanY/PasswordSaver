package com.example.app;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class Login extends AppCompatActivity {

    private FirebaseAuth mAuth;
    Button login,register;
    ProgressBar bar;
    TextView forgotPass;
    EditText emailtext,password;    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        mAuth = FirebaseAuth.getInstance();
        emailtext= findViewById(R.id.email);
        password= findViewById(R.id.password);
        login= findViewById(R.id.login);
        forgotPass= findViewById(R.id.forgotPass);
        register= findViewById(R.id.register);
        bar= findViewById(R.id.loadingbar);
        register.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(),Register.class);
                startActivity(intent);
            }
        });
        login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(TextUtils.isEmpty(emailtext.getText()))
                {
                    Toast.makeText(Login.this, "Enter Email.", Toast.LENGTH_LONG).show();
                    return;
                }
                if(TextUtils.isEmpty(password.getText()))
                {
                    Toast.makeText(Login.this, "Enter Password.", Toast.LENGTH_LONG).show();
                    return;
                }
                bar.setVisibility(View.VISIBLE);
                mAuth.signInWithEmailAndPassword(emailtext.getText().toString(), password.getText().toString()).addOnCompleteListener( new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(@NonNull Task<AuthResult> task) {
                                if (task.isSuccessful()) {

                                    Toast.makeText(getApplicationContext(), "Login Successful.",Toast.LENGTH_SHORT).show();
                                    Intent intent = new Intent(getApplicationContext(),MainActivity.class);
                                    startActivity(intent);
                                    finish();
                                } else {
                                    Toast.makeText(Login.this, task.getException().getLocalizedMessage()+"",
                                            Toast.LENGTH_SHORT).show();
                                }
                                bar.setVisibility(View.GONE);

                            }
                        });
            }
        });
        forgotPass.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(TextUtils.isEmpty(emailtext.getText()))
                {
                    Toast.makeText(Login.this, "Enter Email To Reset The Password", Toast.LENGTH_LONG).show();
                    return;
                }
                mAuth.sendPasswordResetEmail(emailtext.getText().toString()).addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        Toast.makeText(Login.this, "Reset Link Sent To Email", Toast.LENGTH_LONG).show();
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(Login.this, e.getMessage(), Toast.LENGTH_LONG).show();

                    }
                });
            }
        });
    }
    @Override
    public void onStart() {
        super.onStart();
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if(currentUser != null)
        {
            Intent intent = new Intent(getApplicationContext(),MainActivity.class);
            startActivity(intent);
            finish();
        }
    }
}