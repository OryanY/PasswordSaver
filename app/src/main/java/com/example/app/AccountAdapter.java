package com.example.app;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;

public class AccountAdapter extends RecyclerView.Adapter<AccountAdapter.AccountViewHolder> {

    private List<Account> accounts;
    private Context context;
    private String category;
    String authId = FirebaseAuth.getInstance().getCurrentUser().getUid();

    public AccountAdapter(List<Account> accounts, Context context,String category)
    {
        this.accounts = accounts;
        this.context=context;
        this.category=category;
    }
    @NonNull
    @Override
    public AccountViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.accountitem, parent, false);
        return new AccountViewHolder(itemView);
    }
    @Override
    public void onBindViewHolder(@NonNull AccountViewHolder holder, int position) {
        Account account = accounts.get(holder.getAdapterPosition());
        holder.accountNameTextView.setText("Account: " + account.getAccountName());
        holder.usernameTextView.setText("Username: " + account.getUsername());
        try {
            String encryptionKey = PasswordManager.generateEncryptionKey(authId);
            holder.passwordTextView.setText("Password: " +PasswordManager.decryptPassword(account.getPassword().toString(),encryptionKey));
        }
        catch (Exception e)
        {
            holder.passwordTextView.setText("Password: " +e.getMessage());
        }

        //Set update account on click
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showUpdateDialog(accounts.get(holder.getAdapterPosition()).getDocumentId());
            }
        });
        //Set delete account on long click
        holder.itemView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                showDeleteConfirmationDialog(holder.getAdapterPosition());
                return true;
            }
        });
    }
    private void showUpdateDialog(String documentId) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("Update Account");

        // Inflate the custom layout for the dialog
        View dialogView = LayoutInflater.from(context).inflate(R.layout.update_account, null);
        builder.setView(dialogView);

        // Find the views within the custom layout
        EditText accountNameEditText = dialogView.findViewById(R.id.addName);
        EditText usernameEditText = dialogView.findViewById(R.id.addUsername);
        EditText passwordEditText = dialogView.findViewById(R.id.addPassword);

        // Fetch the current account details from the list
        Account account = accounts.get(getAccountPosition(documentId));
        accountNameEditText.setText(account.getAccountName());
        usernameEditText.setText(account.getUsername());

        // Set the decrypted password to the password EditText field
        try {
            String encryptionKey = PasswordManager.generateEncryptionKey(authId);
            String decryptedPassword = PasswordManager.decryptPassword(account.getPassword(), encryptionKey);
            passwordEditText.setText(decryptedPassword);
        }
        catch (Exception e) {
            // Handle decryption error
            passwordEditText.setText("");
        }

        builder.setPositiveButton("Update", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String updatedAccountName = accountNameEditText.getText().toString();
                String updatedUsername = usernameEditText.getText().toString();
                String updatedPassword = passwordEditText.getText().toString();
                FirebaseFirestore db = FirebaseFirestore.getInstance();
                String authId = FirebaseAuth.getInstance().getCurrentUser().getUid();
                DocumentReference accountRef = db.collection("users")
                        .document(authId)
                        .collection("categories")
                        .document(category)
                        .collection("accounts")
                        .document(documentId);
                try {
                    // Encrypt the updated password before saving
                    String encryptionKey = PasswordManager.generateEncryptionKey(authId);
                    String encryptedPassword = PasswordManager.encryptPassword(updatedPassword,encryptionKey);
                    accountRef.set(new Account(documentId, updatedAccountName, updatedUsername, encryptedPassword));
                } catch (Exception e) {
                    // Handle encryption error
                }
                dialog.dismiss();
            }
        });

        // Add cancel button and handle its click event
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });

        // Create and show the dialog
        AlertDialog dialog = builder.create();
        dialog.show();
    }
    private void showDeleteConfirmationDialog(int position) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("Delete Account");
        builder.setMessage("Are you sure you want to delete this account?");
        builder.setPositiveButton("Delete", (dialog, which) -> deleteAccount(accounts.get(position).getDocumentId()));
        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss());
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    @Override public int getItemCount() { return accounts.size(); }
    private void deleteAccount(String documentId) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        String authId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        DocumentReference accountRef = db.collection("users")
                .document(authId)
                .collection("categories")
                .document(category)
                .collection("accounts")
                .document(documentId);

        accountRef.delete().addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        int position = getAccountPosition(documentId);
                        if (position != -1) {
                            accounts.remove(position);
                            notifyItemRemoved(position);
                            notifyItemRangeChanged(position, accounts.size());
                        }
                    }
                })
                .addOnFailureListener(e -> Toast.makeText(context, "Failed to delete the account.", Toast.LENGTH_LONG).show());
    }
    private int getAccountPosition(String documentId) {
        for (int i = 0; i < accounts.size(); i++) {
            Account account = accounts.get(i);
            if (account.getDocumentId().equals(documentId)) {
                return i;
            }
        }
        return -1;
    }
    public static class AccountViewHolder extends RecyclerView.ViewHolder {
        public TextView accountNameTextView;
        public TextView usernameTextView;
        public TextView passwordTextView;

        public AccountViewHolder(@NonNull View itemView) {
            super(itemView);
            accountNameTextView = itemView.findViewById(R.id.accountNameTextView);
            usernameTextView = itemView.findViewById(R.id.usernameTextView);
            passwordTextView = itemView.findViewById(R.id.passwordTextView);
        }
    }


    }
