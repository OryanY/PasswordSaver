package com.example.app;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.res.FontResourcesParserCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.navigation.NavigationBarView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private FirebaseUser user;
    private FloatingActionButton fab;
    private RecyclerView recyclerView;
    private CategoryAdapter adapter;
    private List<String> categories = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        //Get current user detail
        mAuth=FirebaseAuth.getInstance();
        user=mAuth.getCurrentUser();

        ImageButton logout = findViewById(R.id.logout);
        //Set logout button
        logout.setOnClickListener(v -> {
            mAuth.signOut();
            Intent intent = new Intent(getApplicationContext(),Login.class);
            startActivity(intent);
            finish();

        });

        if(user==null)
        {
            Intent intent = new Intent(getApplicationContext(),Login.class);
            startActivity(intent);
            finish();
        }

        recyclerView = findViewById(R.id.recyclerView);
        adapter = new CategoryAdapter(categories,MainActivity.this);
        SpacingItem spacingItem = new SpacingItem(200);
        recyclerView.addItemDecoration(spacingItem);
        recyclerView.setAdapter(adapter);
        fab=findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
            showAddCategoryPopup();

            }
        });

        fetchCategories();

    }
    private void fetchCategories() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        String authId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        db.collection("users").document(authId).collection("categories").get()
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                        categories.clear();
                        for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                            String category = document.getId();
                            categories.add(category);
                        }
                        adapter.notifyDataSetChanged();
                    }
                });
    }
    private void showAddCategoryPopup() {
        Button cancel,add;
        EditText name;

        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this);
        LayoutInflater inflater = getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.addcategory, null);
        dialogBuilder.setView(dialogView);

        cancel = dialogView.findViewById(R.id.cancelButton);
        add = dialogView.findViewById(R.id.addButton);
        name = dialogView.findViewById(R.id.addName);

        AlertDialog alertDialog = dialogBuilder.create();
        alertDialog.show();
        cancel.setOnClickListener(v -> alertDialog.dismiss());
        add.setOnClickListener(v -> {
            if(TextUtils.isEmpty(name.getText()))
            {
                Toast.makeText(MainActivity.this, "Name Cannot Be Empty.", Toast.LENGTH_LONG).show();
                return;
            }
            addCategoryDocument(name.getText().toString());
            alertDialog.dismiss();
        });
    }
    private void addCategoryDocument(String documentName) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        String authId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        // Create a reference to the category document
        DocumentReference categoryRef = db.collection("users")
                .document(authId)
                .collection("categories")
                .document(documentName);

        // Create the category document
        categoryRef.set(new HashMap<>()).addOnSuccessListener(aVoid -> {
                    Toast.makeText(MainActivity.this, "Category added to collection", Toast.LENGTH_SHORT).show();
                    adapter.notifyDataSetChanged();
                    fetchCategories();
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(MainActivity.this, "Failed to add Category", Toast.LENGTH_SHORT).show();
                    }
                });
    }

}