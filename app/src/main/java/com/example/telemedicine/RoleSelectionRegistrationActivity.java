package com.example.telemedicine;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

public class RoleSelectionRegistrationActivity extends AppCompatActivity {

    private TextInputEditText editTextFullName, editTextEmail, editTextPassword, editTextConfirmPassword;
    private TextInputEditText editSpecialization, editLicenseNumber;
    private RadioGroup radioGroupRole;
    private RadioButton radioPatient, radioDoctor, radioAdmin;
    private LinearLayout layoutDoctorFields;
    private Button buttonRegister;
    private TextView textViewSignIn;
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_role_selection_registration);

        // Initialize Firebase Auth and Firestore
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        // Initialize views
        initializeViews();

        // Set up role selection listener
        setupRoleSelection();

        // Set click listener
        buttonRegister.setOnClickListener(v -> registerUser());
        textViewSignIn.setOnClickListener(v -> navigateToLogin());
    }

    private void initializeViews() {
        editTextFullName = findViewById(R.id.edit_full_name);
        editTextEmail = findViewById(R.id.edit_email);
        editTextPassword = findViewById(R.id.edit_password);
        editTextConfirmPassword = findViewById(R.id.edit_confirm_password);
        editSpecialization = findViewById(R.id.edit_specialization);
        editLicenseNumber = findViewById(R.id.edit_license_number);
        
        radioGroupRole = findViewById(R.id.radio_group_role);
        radioPatient = findViewById(R.id.radio_patient);
        radioDoctor = findViewById(R.id.radio_doctor);
        radioAdmin = findViewById(R.id.radio_admin);
        
        layoutDoctorFields = findViewById(R.id.layout_doctor_fields);
        buttonRegister = findViewById(R.id.btn_register);
        textViewSignIn = findViewById(R.id.text_sign_in);
    }

    private void setupRoleSelection() {
        // Hide role selection and doctor fields since this is only for patient registration
        radioGroupRole.setVisibility(View.GONE);
        layoutDoctorFields.setVisibility(View.GONE);
    }

    private void registerUser() {
        String fullName = editTextFullName.getText().toString().trim();
        String email = editTextEmail.getText().toString().trim();
        String password = editTextPassword.getText().toString().trim();
        String confirmPassword = editTextConfirmPassword.getText().toString().trim();
        String specialization = editSpecialization.getText().toString().trim();
        String licenseNumber = editLicenseNumber.getText().toString().trim();

        // For patient registration only (doctors are created by admin)
        String selectedRole = UserRole.PATIENT.getRoleName(); // Always patient for self-registration

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
                        
                        Toast.makeText(RoleSelectionRegistrationActivity.this, 
                            "Registration successful!", 
                            Toast.LENGTH_SHORT).show();
                        
                        // Navigate to patient dashboard
                        navigateBasedOnRole();
                    } else {
                        // Sign up failed
                        Toast.makeText(RoleSelectionRegistrationActivity.this, 
                            "Registration failed: " + task.getException().getMessage(), 
                            Toast.LENGTH_LONG).show();
                    }
                });
    }

    private void saveUserProfile(String userId, String fullName, String email) {
        // Create a user object to store in Firestore with patient role
        User userProfile = new User(userId, fullName, email, UserRole.PATIENT.getRoleName(), System.currentTimeMillis());

        // Save to Firestore
        db.collection("users")
                .document(userId)
                .set(userProfile)
                .addOnSuccessListener(aVoid -> {
                    // Successfully saved to Firestore
                })
                .addOnFailureListener(e -> {
                    // Handle error
                    Toast.makeText(RoleSelectionRegistrationActivity.this,
                        "Failed to save user profile: " + e.getMessage(),
                        Toast.LENGTH_LONG).show();
                });
    }

    private void navigateBasedOnRole() {
        // Always navigate to patient dashboard for self-registration
        Intent intent = new Intent(RoleSelectionRegistrationActivity.this, MainActivity.class);
        startActivity(intent);
        finish(); // Close registration activity
    }

    private void navigateToLogin() {
        Intent intent = new Intent(this, LoginActivity.class);
        startActivity(intent);
    }
}