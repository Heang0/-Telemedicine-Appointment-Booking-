package com.example.telemedicine;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.SwitchCompat;
import androidx.fragment.app.Fragment;

import com.google.android.material.button.MaterialButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

public class ProfileFragment extends Fragment {

    private TextView textUserName, textUserEmail, textUserRole, textAppVersion;
    private MaterialButton btnLogout;
    private SwitchCompat switchNotifications;
    private View layoutEditProfile, layoutNotifications, layoutPrivacy, layoutHelp, layoutContact;
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_profile_ios, container, false);

        // Initialize Firebase
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        // Initialize views
        textUserName = view.findViewById(R.id.text_user_name);
        textUserEmail = view.findViewById(R.id.text_user_email);
        textUserRole = view.findViewById(R.id.text_user_role);
        textAppVersion = view.findViewById(R.id.text_app_version);
        btnLogout = view.findViewById(R.id.btn_logout);
        switchNotifications = view.findViewById(R.id.switch_notifications);
        layoutEditProfile = view.findViewById(R.id.layout_edit_profile);
        layoutNotifications = view.findViewById(R.id.layout_notifications);
        layoutPrivacy = view.findViewById(R.id.layout_privacy);
        layoutHelp = view.findViewById(R.id.layout_help);
        layoutContact = view.findViewById(R.id.layout_contact);

        // Load user profile data
        loadUserProfile();

        // Set click listeners
        setupClickListeners();

        return view;
    }

    private void loadUserProfile() {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null) {
            Toast.makeText(getContext(), "User not authenticated", Toast.LENGTH_SHORT).show();
            return;
        }

        String userId = currentUser.getUid();
        db.collection("users")
                .document(userId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        User user = documentSnapshot.toObject(User.class);
                        if (user != null) {
                            textUserName.setText(user.getFullName());
                            textUserEmail.setText(currentUser.getEmail());
                            textUserRole.setText(user.getRole());
                        }
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(getContext(), "Failed to load profile: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private void setupClickListeners() {
        // Logout button
        if (btnLogout != null) {
            btnLogout.setOnClickListener(v -> {
                new androidx.appcompat.app.AlertDialog.Builder(getContext())
                        .setTitle("Log Out")
                        .setMessage("Are you sure you want to log out?")
                        .setPositiveButton("Log Out", (dialog, which) -> {
                            mAuth.signOut();
                            if (getActivity() != null) {
                                Intent intent = new Intent(getActivity(), LoginActivity.class);
                                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                                startActivity(intent);
                                getActivity().finish();
                            }
                        })
                        .setNegativeButton("Cancel", null)
                        .show();
            });
        }

        // Edit Profile
        if (layoutEditProfile != null) {
            layoutEditProfile.setOnClickListener(v -> {
                Toast.makeText(getContext(), "Edit Profile - Coming soon", Toast.LENGTH_SHORT).show();
            });
        }

        // Notifications
        if (layoutNotifications != null && switchNotifications != null) {
            layoutNotifications.setOnClickListener(v -> {
                switchNotifications.setChecked(!switchNotifications.isChecked());
                Toast.makeText(getContext(), "Notifications " + (switchNotifications.isChecked() ? "enabled" : "disabled"), Toast.LENGTH_SHORT).show();
            });
        }

        // Privacy
        if (layoutPrivacy != null) {
            layoutPrivacy.setOnClickListener(v -> {
                Toast.makeText(getContext(), "Privacy Settings - Coming soon", Toast.LENGTH_SHORT).show();
            });
        }

        // Help Center
        if (layoutHelp != null) {
            layoutHelp.setOnClickListener(v -> {
                Toast.makeText(getContext(), "Help Center - Coming soon", Toast.LENGTH_SHORT).show();
            });
        }

        // Contact Us
        if (layoutContact != null) {
            layoutContact.setOnClickListener(v -> {
                Toast.makeText(getContext(), "Contact Us - Coming soon", Toast.LENGTH_SHORT).show();
            });
        }
    }
}
