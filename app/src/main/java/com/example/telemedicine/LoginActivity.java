package com.example.telemedicine;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

public class LoginActivity extends AppCompatActivity {

    private TextInputEditText editTextEmail, editTextPassword;
    private Button buttonLogin;
    private TextView textViewSignUp, textViewForgotPassword;
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // Apply theme before calling super.onCreate
        ThemeUtils.applyTheme(this);

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // Initialize Firebase Auth and Firestore
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        // Initialize views
        editTextEmail = findViewById(R.id.edit_email);
        editTextPassword = findViewById(R.id.edit_password);
        buttonLogin = findViewById(R.id.btn_login);
        textViewSignUp = findViewById(R.id.text_sign_up);
        textViewForgotPassword = findViewById(R.id.text_forgot_password);

        // Set click listeners
        buttonLogin.setOnClickListener(v -> loginUser());
        textViewSignUp.setOnClickListener(v -> navigateToRegistration());
        textViewForgotPassword.setOnClickListener(v -> forgotPassword());
    }

    private void loginUser() {
        String email = editTextEmail.getText().toString().trim();
        String password = editTextPassword.getText().toString().trim();

        // Validate inputs
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

        // Check if it's the admin account
        if (email.equals("admin@telemedicine.com") && password.equals("admin123456")) {
            // This is the admin account - create it if it doesn't exist
            handleAdminLogin(email, password);
            return;
        }

        // For regular login
        if (password.length() < 6) {
            editTextPassword.setError("Password must be at least 6 characters");
            editTextPassword.requestFocus();
            return;
        }

        // Show loading indicator
        buttonLogin.setEnabled(false);
        buttonLogin.setText("Signing in...");

        // Sign in with Firebase
        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    buttonLogin.setEnabled(true);
                    buttonLogin.setText("Login");

                    if (task.isSuccessful()) {
                        // Sign in success
                        Toast.makeText(LoginActivity.this, "Login successful!", Toast.LENGTH_SHORT).show();

                        // Navigate based on user role
                        navigateBasedOnUserRole();
                    } else {
                        // Sign in failed
                        Exception exception = task.getException();
                        String errorMessage = exception != null ? exception.getMessage() : "Unknown error";
                        Log.e("LoginActivity", "Sign-in failed", exception);

                        // For presentation: show helpful message
                        if (errorMessage.contains("INVALID") || errorMessage.contains("internal")) {
                            Toast.makeText(LoginActivity.this,
                                "Firebase Auth Error: Please check if Firebase Authentication is enabled in console",
                                Toast.LENGTH_LONG).show();
                        } else {
                            Toast.makeText(LoginActivity.this,
                                "Authentication failed: " + errorMessage,
                                Toast.LENGTH_LONG).show();
                        }
                    }
                });
    }

    private void handleAdminLogin(String email, String password) {
        // First, try to sign in with the admin credentials
        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        // Sign in successful, now check if user profile exists in Firestore
                        FirebaseUser user = mAuth.getCurrentUser();
                        if (user != null) {
                            // Check if user profile exists in Firestore
                            db.collection("users")
                                    .document(user.getUid())
                                    .get()
                                    .addOnSuccessListener(documentSnapshot -> {
                                        if (documentSnapshot.exists()) {
                                            // User profile exists, navigate to admin dashboard
                                            Toast.makeText(LoginActivity.this, "Admin login successful!", Toast.LENGTH_SHORT).show();
                                            navigateToAdminDashboard();
                                        } else {
                                            // User profile doesn't exist in Firestore, create it
                                            createAdminProfileInFirestore(user.getUid(), email);
                                        }
                                    })
                                    .addOnFailureListener(e -> {
                                        // Error checking Firestore, try to create profile anyway
                                        Log.e("LoginActivity", "Error checking Firestore for admin", e);
                                        createAdminProfileInFirestore(user.getUid(), email);
                                    });
                        }
                    } else {
                        // Sign in failed, likely because account doesn't exist, so create it
                        Exception exception = task.getException();
                        String errorMessage = exception != null ? exception.getMessage() : "Unknown error";
                        Log.e("LoginActivity", "Admin sign-in failed", exception);

                        // Try to create the admin account
                        createAdminAccount(email, password);
                    }
                });
    }

    private void createAdminProfileInFirestore(String userId, String email) {
        // Create admin user profile in Firestore
        User adminProfile = new User(userId, "System Admin", email, UserRole.ADMIN.getRoleName(), System.currentTimeMillis());
        adminProfile.setVerified(true); // Admin is automatically verified

        db.collection("users")
                .document(userId)
                .set(adminProfile)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(LoginActivity.this, "Admin profile created, logging in...", Toast.LENGTH_SHORT).show();
                    navigateToAdminDashboard();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(LoginActivity.this,
                        "Failed to create admin profile: " + e.getMessage(),
                        Toast.LENGTH_LONG).show();
                    navigateToAdminDashboard(); // Still navigate to admin dashboard
                });
    }

    private void createAdminAccount(String email, String password) {
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        // Admin account created successfully
                        FirebaseUser user = task.getResult().getUser();
                        if (user != null) {
                            // Save admin user profile to Firestore
                            User adminProfile = new User(user.getUid(), "System Admin", email, UserRole.ADMIN.getRoleName(), System.currentTimeMillis());
                            adminProfile.setVerified(true); // Admin is automatically verified

                            db.collection("users")
                                    .document(user.getUid())
                                    .set(adminProfile)
                                    .addOnSuccessListener(aVoid -> {
                                        Toast.makeText(LoginActivity.this, "Admin account created and logged in!", Toast.LENGTH_SHORT).show();
                                        navigateToAdminDashboard();
                                    })
                                    .addOnFailureListener(e -> {
                                        Toast.makeText(LoginActivity.this,
                                            "Admin profile creation failed: " + e.getMessage(),
                                            Toast.LENGTH_LONG).show();
                                        navigateToAdminDashboard(); // Still navigate to admin dashboard
                                    });
                        }
                    } else {
                        // Get the specific error message
                        Exception exception = task.getException();
                        String errorMessage = exception != null ? exception.getMessage() : "Unknown error";
                        Toast.makeText(LoginActivity.this,
                            "Failed to create admin account: " + errorMessage,
                            Toast.LENGTH_LONG).show();
                        Log.e("LoginActivity", "Admin account creation failed", exception);
                    }
                });
    }

    private void navigateToAdminDashboard() {
        Intent intent = new Intent(LoginActivity.this, AdminDashboardActivity.class);
        startActivity(intent);
        finish(); // Close login activity
    }

    private void navigateToRegistration() {
        Intent intent = new Intent(this, RoleSelectionRegistrationActivity.class);
        startActivity(intent);
    }

    private void navigateBasedOnUserRole() {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            String userId = currentUser.getUid();

            // Get user role from Firestore
            db.collection("users")
                    .document(userId)
                    .get()
                    .addOnSuccessListener(documentSnapshot -> {
                        if (documentSnapshot.exists()) {
                            User user = documentSnapshot.toObject(User.class);
                            if (user != null) {
                                String role = user.getRole();

                                Intent intent;
                                if (UserRole.DOCTOR.getRoleName().equalsIgnoreCase(role)) {
                                    intent = new Intent(LoginActivity.this, DoctorDashboardActivity.class);
                                } else if (UserRole.ADMIN.getRoleName().equalsIgnoreCase(role)) {
                                    intent = new Intent(LoginActivity.this, AdminDashboardActivity.class);
                                } else {
                                    // Default to patient dashboard
                                    intent = new Intent(LoginActivity.this, MainActivity.class);
                                }

                                startActivity(intent);
                                finish(); // Close login activity
                            } else {
                                // User object is null, default to patient dashboard with safety
                                safeNavigateToMainActivity();
                            }
                        } else {
                            // If user document doesn't exist, create a basic user profile first
                            createBasicUserProfile(userId, currentUser.getEmail());
                        }
                    })
                    .addOnFailureListener(e -> {
                        // If there's an error getting user data, default to patient dashboard safely
                        Log.e("LoginActivity", "Error getting user data", e);
                        safeNavigateToMainActivity();
                    });
        } else {
            // No current user, go back to login
            Toast.makeText(this, "No user signed in", Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    private void createBasicUserProfile(String userId, String email) {
        // Create basic user profile with default role (patient)
        User basicUser = new User(userId, "New User", email, UserRole.PATIENT.getRoleName(), System.currentTimeMillis());
        basicUser.setVerified(true);

        db.collection("users")
                .document(userId)
                .set(basicUser)
                .addOnSuccessListener(aVoid -> {
                    Log.d("LoginActivity", "Basic user profile created for: " + userId);
                    safeNavigateToMainActivity();
                })
                .addOnFailureListener(e -> {
                    Log.e("LoginActivity", "Failed to create basic user profile", e);
                    safeNavigateToMainActivity();
                });
    }

    private void safeNavigateToMainActivity() {
        try {
            Intent intent = new Intent(LoginActivity.this, MainActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        } catch (Exception e) {
            Log.e("LoginActivity", "Error navigating to MainActivity", e);
            // Ultimate fallback: show simple message
            Toast.makeText(LoginActivity.this, "Welcome to Telemedicine App", Toast.LENGTH_LONG).show();
            finish();
        }
    }

    private void forgotPassword() {
        String email = editTextEmail.getText().toString().trim();

        if (!TextUtils.isEmpty(email)) {
            mAuth.sendPasswordResetEmail(email)
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            Toast.makeText(LoginActivity.this,
                                "Password reset email sent!",
                                Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(LoginActivity.this,
                                "Failed to send reset email: " + task.getException().getMessage(),
                                Toast.LENGTH_LONG).show();
                        }
                    });
        } else {
            editTextEmail.setError("Enter your email address");
            editTextEmail.requestFocus();
        }
    }
}