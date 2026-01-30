package com.example.telemedicine;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

public class AllUsersFragment extends Fragment {

    private RecyclerView recyclerView;
    private UserAdapter userAdapter;
    private java.util.List<User> userList;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_users_list, container, false);

        recyclerView = view.findViewById(R.id.recycler_view);
        userList = new java.util.ArrayList<>();
        userAdapter = new UserAdapter(userList);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setAdapter(userAdapter);

        // Load all users
        loadAllUsers();

        return view;
    }

    private void loadAllUsers() {
        com.google.firebase.firestore.FirebaseFirestore db = com.google.firebase.firestore.FirebaseFirestore.getInstance();

        db.collection("users")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    userList.clear();
                    for (com.google.firebase.firestore.QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        User user = document.toObject(User.class);
                        userList.add(user);
                    }

                    // Set the action listener for edit/delete
                    userAdapter.setOnUserActionListener(new UserAdapter.OnUserActionListener() {
                        @Override
                        public void onEditUser(User user) {
                            // Handle edit user
                            editUser(user);
                        }

                        @Override
                        public void onDeleteUser(User user) {
                            // Handle delete user
                            deleteUser(user);
                        }
                    });

                    userAdapter.notifyDataSetChanged();
                })
                .addOnFailureListener(e -> {
                    // Handle error
                });
    }

    private void editUser(User user) {
        // For now, just show a toast - in a real app you would open an edit dialog
        if (getContext() != null) {
            android.widget.Toast.makeText(getContext(), "Edit user: " + user.getFullName(), android.widget.Toast.LENGTH_SHORT).show();
        }
    }

    private void deleteUser(User user) {
        // Confirm deletion and then delete
        if (getContext() != null) {
            android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(getContext());
            builder.setTitle("Delete User");
            builder.setMessage("Are you sure you want to delete " + user.getFullName() + "?");
            builder.setPositiveButton("Delete", (dialog, which) -> {
                // Delete user from Firestore
                com.google.firebase.firestore.FirebaseFirestore db = com.google.firebase.firestore.FirebaseFirestore.getInstance();

                // Delete user document
                db.collection("users").document(user.getUserId())
                        .delete()
                        .addOnSuccessListener(aVoid -> {
                            android.widget.Toast.makeText(getContext(), "User deleted successfully", android.widget.Toast.LENGTH_SHORT).show();

                            // Reload the user list
                            loadAllUsers();
                        })
                        .addOnFailureListener(e -> {
                            android.widget.Toast.makeText(getContext(), "Failed to delete user: " + e.getMessage(), android.widget.Toast.LENGTH_LONG).show();
                        });
            });
            builder.setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss());
            builder.show();
        }
    }
}