package com.example.telemedicine;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

public class ProfileFragment extends Fragment {

    private TextInputEditText editName, editEmail, editPhone;
    private Button btnSaveProfile, btnViewHealthProfile, btnLogout;
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_profile, container, false);

        // Initialize Firebase
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        // Initialize views
        editName = view.findViewById(R.id.edit_name);
        editEmail = view.findViewById(R.id.edit_email);
        editPhone = view.findViewById(R.id.edit_phone);
        btnSaveProfile = view.findViewById(R.id.btn_save_profile);
        btnViewHealthProfile = view.findViewById(R.id.btn_view_health_profile);
        btnLogout = view.findViewById(R.id.btn_logout);

        // Load user profile data
        loadUserProfile();

        // Set click listeners
        if (btnViewHealthProfile != null) {
            btnViewHealthProfile.setOnClickListener(v -> {
                // Navigate to Health Profile
                HealthProfileFragment healthProfileFragment = new HealthProfileFragment();
                if (getActivity() != null) {
                    getActivity().getSupportFragmentManager()
                            .beginTransaction()
                            .replace(R.id.fragment_container, healthProfileFragment)
                            .addToBackStack(null)
                            .commit();
                }
            });
        }

        if (btnSaveProfile != null) {
            btnSaveProfile.setOnClickListener(v -> saveProfile());
        }

        if (btnLogout != null) {
            btnLogout.setOnClickListener(v -> {
                mAuth.signOut();
                // Navigate back to login screen
                if (getActivity() != null) {
                    Intent intent = new Intent(getActivity(), LoginActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(intent);
                    getActivity().finish();
                }
            });
        }

        return view;
    }

    private void loadUserProfile() {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            String userId = currentUser.getUid();

            db.collection("users")
                    .document(userId)
                    .get()
                    .addOnSuccessListener(documentSnapshot -> {
                        if (documentSnapshot.exists()) {
                            User user = documentSnapshot.toObject(User.class);
                            if (user != null) {
                                // Populate the fields with user data
                                if (editName != null) {
                                    editName.setText(user.getFullName());
                                }
                                if (editEmail != null) {
                                    editEmail.setText(user.getEmail());
                                }
                                if (editPhone != null && user.getPhoneNumber() != null) {
                                    editPhone.setText(user.getPhoneNumber());
                                } else if (editPhone != null) {
                                    editPhone.setText(""); // Set to empty if null
                                }
                            }
                        }
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(getContext(), "Failed to load profile: " + e.getMessage(),
                                Toast.LENGTH_SHORT).show();
                    });
        }
    }

    private void saveProfile() {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null) {
            Toast.makeText(getContext(), "User not authenticated", Toast.LENGTH_SHORT).show();
            return;
        }

        // Get input values
        String name = editName != null ? editName.getText().toString().trim() : "";
        String email = editEmail != null ? editEmail.getText().toString().trim() : "";
        String phone = editPhone != null ? editPhone.getText().toString().trim() : "";

        // Validate inputs
        if (TextUtils.isEmpty(name)) {
            editName.setError("Name is required");
            editName.requestFocus();
            return;
        }

        // Update user profile in Firestore
        String userId = currentUser.getUid();
        db.collection("users")
                .document(userId)
                .update(
                        "fullName", name,
                        "email", email,
                        "phoneNumber", phone.isEmpty() ? null : phone
                )
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(getContext(), "Profile updated successfully",
                            Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(getContext(), "Failed to update profile: " + e.getMessage(),
                            Toast.LENGTH_SHORT).show();
                });
    }
}