package com.example.app;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.SetOptions;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CategoryView extends AppCompatActivity {
    private FirebaseAuth mAuth;
    private FirebaseUser user;
    private String category;
    private FloatingActionButton fab;
    private RecyclerView recyclerView;
    private AccountAdapter adapter;
    TextView Text;
    private final List<Account> accounts = new ArrayList<>();
    ImageButton ImageButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_category_view);
        Intent intent = getIntent();
        Text = findViewById(R.id.text);
        category = intent.getStringExtra("category");
        Text.setText(category+ " Accounts");

        fab=findViewById(R.id.fab);
        fab.setOnClickListener(v -> showAddAccountPopup());
        ImageButton back=findViewById(R.id.logout);
        back.setOnClickListener(v -> finish());
        recyclerView=findViewById(R.id.recyclerView);
        adapter = new AccountAdapter(accounts,CategoryView.this,category);

        //SpacingItem spacingItem = new SpacingItem(200);
        //recyclerView.addItemDecoration(spacingItem);

        recyclerView = findViewById(R.id.recyclerView);
        adapter = new AccountAdapter(accounts, CategoryView.this, category);
        recyclerView.setAdapter(adapter);
        getAccounts();




    }

    private void addAccountDocument(String accName, String username, String password) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        String authId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        DocumentReference categoryRef = db.collection("users")
                .document(authId)
                .collection("categories")
                .document(category);

        categoryRef.collection("accounts").get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {

                        int accountCount = queryDocumentSnapshots.size();
                        String accountNumber = "account" + (accountCount + 1);

                        DocumentReference accountRef = categoryRef.collection("accounts").document(accountNumber);
                        try {
                            String encryptionKey = PasswordManager.generateEncryptionKey(authId);
                            String encryptedPass = PasswordManager.encryptPassword(password, encryptionKey);
                            Map<String, Object> accountData = new HashMap<>();
                            accountData.put("accountName", accName);
                            accountData.put("username", username);
                            accountData.put("password",encryptedPass );

                            accountRef.set(accountData)
                                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                                        @Override
                                        public void onSuccess(Void aVoid) {
                                            Toast.makeText(CategoryView.this, "Account added to " +category + " Category", Toast.LENGTH_SHORT).show();
                                            adapter.notifyDataSetChanged();
                                            getAccounts();
                                        }
                                    })
                                    .addOnFailureListener(new OnFailureListener() {
                                        @Override
                                        public void onFailure(@NonNull Exception e) {
                                            Toast.makeText(CategoryView.this, "Failed to add Account", Toast.LENGTH_SHORT).show();
                                        }
                                    });
                        }
                        catch (Exception e)
                        {
                            Toast.makeText(CategoryView.this, "Failed to Encrypt Account"+ e.getMessage(), Toast.LENGTH_SHORT).show();
                        }

                    }
                });
    }
    private void showAddAccountPopup() {
        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this);
        LayoutInflater inflater = getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.addaccount, null);
        dialogBuilder.setView(dialogView);
        Button cancel,add;
        EditText accountName,username,password;

        cancel = dialogView.findViewById(R.id.cancelButton);
        add = dialogView.findViewById(R.id.addButton);
        accountName = dialogView.findViewById(R.id.addName);
        username= dialogView.findViewById(R.id.addUsername);
        password= dialogView.findViewById(R.id.addPassword);


        AlertDialog alertDialog = dialogBuilder.create();
        alertDialog.show();
        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                alertDialog.dismiss();
            }
        });
        add.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(TextUtils.isEmpty(accountName.getText()))
                {
                    Toast.makeText(CategoryView.this, "Account Name Cannot Be Empty.", Toast.LENGTH_LONG).show();
                    return;
                }
                if(TextUtils.isEmpty(username.getText()))
                {
                    Toast.makeText(CategoryView.this, "Username Cannot Be Empty.", Toast.LENGTH_LONG).show();
                    return;
                }
                if(TextUtils.isEmpty(password.getText()))
                {
                    Toast.makeText(CategoryView.this, "Password Cannot Be Empty.", Toast.LENGTH_LONG).show();
                    return;
                }
                addAccountDocument(accountName.getText().toString(),username.getText().toString(),password.getText().toString());
                alertDialog.dismiss();
            }
        });
    }
    public void getAccounts() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        String authId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        db.collection("users")
                .document(authId)
                .collection("categories")
                .document(category)
                .collection("accounts")
                .get()
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                        accounts.clear(); // Clear the existing account list
                        for (QueryDocumentSnapshot documentSnapshot : queryDocumentSnapshots) {
                            String accountName = documentSnapshot.getString("accountName");
                            String username = documentSnapshot.getString("username");
                            String password = documentSnapshot.getString("password");
                            String documentId = documentSnapshot.getId();
                            accounts.add(new Account(documentId,accountName, username, password));
                        }
                        adapter.notifyDataSetChanged(); // Notify the adapter that the data has changed
                    }
                });
    }


}