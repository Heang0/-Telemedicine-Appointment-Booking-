package com.example.telemedicine;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

public class AuthRegistrationActivity extends AppCompatActivity {

    private TextInputEditText editTextFullName, editTextEmail, editTextPassword, editTextConfirmPassword;
    private Button buttonRegister;
    private TextView textViewSignIn;
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_auth_registration);

        // Initialize Firebase Auth and Firestore
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        // Initialize views
        editTextFullName = findViewById(R.id.edit_full_name);
        editTextEmail = findViewById(R.id.edit_email);
        editTextPassword = findViewById(R.id.edit_password);
        editTextConfirmPassword = findViewById(R.id.edit_confirm_password);
        buttonRegister = findViewById(R.id.btn_register);
        textViewSignIn = findViewById(R.id.text_sign_in);

        // Set click listener
        buttonRegister.setOnClickListener(v -> registerUser());
        textViewSignIn.setOnClickListener(v -> navigateToLogin());
    }

    @Override
    public void onBackPressed() {
        // Redirect to login instead of allowing back to main activity
        navigateToLogin();
    }

    private void registerUser() {
        String fullName = editTextFullName.getText().toString().trim();
        String email = editTextEmail.getText().toString().trim();
        String password = editTextPassword.getText().toString().trim();
        String confirmPassword = editTextConfirmPassword.getText().toString().trim();

        // Validate inputs
        if (TextUtils.isEmpty(fullName)) {
            editTextFullName.setError("Full name is required");
            editTextFullName.requestFocus();
            return;
        }

        if (TextUtils.isEmpty(email)) {
            editTextEmail.setError("Email is required");
            editTextEmail.requestFocus();
            return;
        }

        if (TextUtils.isEmpty(password)) {
            editTextPassword.setError("Password is required");
            editTextPassword.requestFocus();
            return;
        }

        if (password.length() < 6) {
            editTextPassword.setError("Password must be at least 6 characters");
            editTextPassword.requestFocus();
            return;
        }

        if (!password.equals(confirmPassword)) {
            editTextConfirmPassword.setError("Passwords do not match");
            editTextConfirmPassword.requestFocus();
            return;
        }

        // Show loading indicator
        buttonRegister.setEnabled(false);
        buttonRegister.setText("Registering...");

        // Create user with email and password
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    buttonRegister.setEnabled(true);
                    buttonRegister.setText("Register");

                    if (task.isSuccessful()) {
                        // Sign up success
                        FirebaseUser user = mAuth.getCurrentUser();
                        
                        // Save user profile to Firestore
                        saveUserProfile(user.getUid(), fullName, email);
                        
                        Toast.makeText(AuthRegistrationActivity.this, 
                            "Registration successful!", 
                            Toast.LENGTH_SHORT).show();
                        
                        // Navigate to main dashboard
                        Intent intent = new Intent(AuthRegistrationActivity.this, MainActivity.class);
                        startActivity(intent);
                        finish(); // Close registration activity
                    } else {
                        // Sign up failed
                        Toast.makeText(AuthRegistrationActivity.this, 
                            "Registration failed: " + task.getException().getMessage(), 
                            Toast.LENGTH_LONG).show();
                    }
                });
    }

    private void saveUserProfile(String userId, String fullName, String email) {
        // Create a user object to store in Firestore
        User userProfile = new User(userId, fullName, email, System.currentTimeMillis());

        // Save to Firestore
        db.collection("users")
                .document(userId)
                .set(userProfile)
                .addOnSuccessListener(aVoid -> {
                    // Successfully saved to Firestore
                })
                .addOnFailureListener(e -> {
                    // Handle error
                    Toast.makeText(AuthRegistrationActivity.this,
                        "Failed to save user profile: " + e.getMessage(),
                        Toast.LENGTH_LONG).show();
                });
    }

    private void navigateToLogin() {
        Intent intent = new Intent(this, LoginActivity.class);
        startActivity(intent);
    }
}