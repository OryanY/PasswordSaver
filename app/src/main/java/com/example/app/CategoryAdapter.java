package com.example.app;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.List;

public class CategoryAdapter extends RecyclerView.Adapter<CategoryAdapter.CategoryViewHolder> {
    private final List<String> categories;
    private final Context context;
    public CategoryAdapter(List<String> categories, Context context) {
        this.categories = categories;
        this.context=context;
    }

    @NonNull
    @Override
    public CategoryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.categoryitem, parent, false);
        return new CategoryViewHolder(itemView);
    }
    @Override
    public void onBindViewHolder(@NonNull CategoryViewHolder holder, int position) {
        String category = categories.get(position);
        holder.categoryNameTextView.setText(category);
        //Open the category on click
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(context, category + " Clicked", Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(context, CategoryView.class);
                intent.putExtra("category", category);
                context.startActivity(intent);
            }
        });
        //Set option to delete on long click
        holder.itemView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                AlertDialog.Builder builder = new AlertDialog.Builder(context);
                builder.setTitle("Delete Confirmation");
                builder.setMessage("Are you sure you want to delete this category?");
                builder.setPositiveButton("Delete", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which)
                    {
                        deleteCategory(category);
                        Toast.makeText(context, "Category " + category + " was deleted successfully ", Toast.LENGTH_SHORT).show();
                    }
                });

                builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
                builder.create().show();
                return true;
            }
        });
    }
    private void deleteCategory(String category) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        String authId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        // Create a reference to the category document
        DocumentReference categoryRef = db.collection("users")
                .document(authId)
                .collection("categories")
                .document(category);

        // Retrieve all the documents inside the category
        categoryRef.collection("accounts").get()
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot querySnapshot) {
                        // Delete each document inside the category
                        for (QueryDocumentSnapshot documentSnapshot : querySnapshot) {
                            DocumentReference docRef = documentSnapshot.getReference();
                            docRef.delete();
                        }
                        // Delete the category document itself
                        categoryRef.delete()
                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void aVoid) {
                                        Toast.makeText(context, "Category deleted", Toast.LENGTH_SHORT).show();
                                        categories.remove(category); // Remove the category from the list
                                        notifyDataSetChanged(); // Notify the adapter of the data change
                                    }
                                })
                                .addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        Toast.makeText(context, "Failed to delete Category", Toast.LENGTH_SHORT).show();
                                    }
                                });
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(context, "Failed to retrieve documents", Toast.LENGTH_SHORT).show();
                    }
                });
    }
    @Override
    public int getItemCount() {
        return categories.size();
    }
    public static class CategoryViewHolder extends RecyclerView.ViewHolder {
        public TextView categoryNameTextView;

        public CategoryViewHolder(@NonNull View itemView) {
            super(itemView);
            categoryNameTextView = itemView.findViewById(R.id.categoryNameTextView);
        }
    }
}
