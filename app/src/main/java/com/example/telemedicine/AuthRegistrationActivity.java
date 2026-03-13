package com.example.telemedicine;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.checkbox.MaterialCheckBox;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.firestore.FirebaseFirestore;

public class AuthRegistrationActivity extends AppCompatActivity {
    private static final String TAG = "AuthRegistration";

    private TextInputEditText editTextFullName, editTextEmail, editTextPassword, editTextConfirmPassword;
    private MaterialCheckBox checkboxTerms;
    private MaterialButton buttonRegister;
    private MaterialButton btnGoogleSignUp;
    private TextView textViewSignIn;
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private GoogleSignInClient mGoogleSignInClient;

    // Google Sign-In launcher
    private final ActivityResultLauncher<Intent> googleSignInLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK) {
                    Intent data = result.getData();
                    if (data != null) {
                        handleGoogleSignInResult(data);
                    }
                } else {
                    Log.w(TAG, "Google sign-in cancelled");
                    Toast.makeText(this, "Sign-in cancelled", Toast.LENGTH_SHORT).show();
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_auth_registration);

        // Initialize Firebase Auth and Firestore
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        // Configure Google Sign-In
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();
        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);

        // Initialize views
        editTextFullName = findViewById(R.id.edit_full_name);
        editTextEmail = findViewById(R.id.edit_email);
        editTextPassword = findViewById(R.id.edit_password);
        editTextConfirmPassword = findViewById(R.id.edit_confirm_password);
        checkboxTerms = findViewById(R.id.checkbox_terms);
        buttonRegister = findViewById(R.id.btn_register);
        btnGoogleSignUp = findViewById(R.id.btn_google_sign_up);
        textViewSignIn = findViewById(R.id.text_login);

        // Set click listeners
        buttonRegister.setOnClickListener(v -> registerUser());
        btnGoogleSignUp.setOnClickListener(v -> signUpWithGoogle());
        textViewSignIn.setOnClickListener(v -> navigateToLogin());
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
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

        if (!checkboxTerms.isChecked()) {
            Toast.makeText(this, "Please agree to the Terms of Service and Privacy Policy",
                    Toast.LENGTH_SHORT).show();
            return;
        }

        // Show loading indicator
        buttonRegister.setEnabled(false);
        buttonRegister.setText("Registering...");

        // Create user with email and password
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    buttonRegister.setEnabled(true);
                    buttonRegister.setText("Create Account");

                    if (task.isSuccessful()) {
                        // Sign up success
                        FirebaseUser user = mAuth.getCurrentUser();

                        if (user == null) {
                            Toast.makeText(AuthRegistrationActivity.this,
                                "Registration failed: User is null",
                                Toast.LENGTH_LONG).show();
                            return;
                        }

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
                        Exception exception = task.getException();
                        String errorMessage = "Registration failed";
                        if (exception != null) {
                            errorMessage += ": " + exception.getMessage();
                        }
                        Toast.makeText(AuthRegistrationActivity.this,
                            errorMessage,
                            Toast.LENGTH_LONG).show();
                    }
                });
    }

    private void signUpWithGoogle() {
        Intent signInIntent = mGoogleSignInClient.getSignInIntent();
        googleSignInLauncher.launch(signInIntent);
    }

    private void handleGoogleSignInResult(Intent data) {
        try {
            GoogleSignInAccount account = GoogleSignIn.getSignedInAccountFromIntent(data)
                    .getResult(ApiException.class);
            Log.d(TAG, "Google sign-in successful: " + account.getEmail());
            firebaseAuthWithGoogle(account);
        } catch (ApiException e) {
            Log.w(TAG, "Google sign-in failed", e);
            Toast.makeText(this, "Google sign-in failed: " + e.getMessage(),
                    Toast.LENGTH_SHORT).show();
        }
    }

    private void firebaseAuthWithGoogle(GoogleSignInAccount account) {
        AuthCredential credential = GoogleAuthProvider.getCredential(account.getIdToken(), null);
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        // Sign in success, update UI with the signed-in user's information
                        FirebaseUser user = mAuth.getCurrentUser();
                        Log.d(TAG, "Firebase auth with Google successful: " + user.getUid());

                        // Check if user exists in Firestore
                        db.collection("users")
                                .document(user.getUid())
                                .get()
                                .addOnCompleteListener(task2 -> {
                                    if (task2.isSuccessful() && task2.getResult() != null) {
                                        if (task2.getResult().exists()) {
                                            // User exists, navigate to main
                                            Log.d(TAG, "Existing Google user signed in");
                                            navigateToMain();
                                        } else {
                                            // New user, create profile
                                            Log.d(TAG, "New Google user, creating profile");
                                            createGoogleUserProfile(user);
                                        }
                                    } else {
                                        // Error checking Firestore
                                        Log.e(TAG, "Error checking Firestore", task2.getException());
                                        Toast.makeText(this, "Error verifying user",
                                                Toast.LENGTH_SHORT).show();
                                    }
                                });
                    } else {
                        // If sign in fails, display a message to the user
                        Log.w(TAG, "Firebase auth with Google failed", task.getException());
                        Toast.makeText(this, "Authentication failed: " + task.getException().getMessage(),
                                Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void createGoogleUserProfile(FirebaseUser user) {
        String fullName = user.getDisplayName() != null ? user.getDisplayName() : "Google User";
        String email = user.getEmail() != null ? user.getEmail() : "";

        // Create user profile in Firestore
        User userProfile = new User(user.getUid(), fullName, email, System.currentTimeMillis());

        db.collection("users")
                .document(user.getUid())
                .set(userProfile)
                .addOnSuccessListener(aVoid -> {
                    Log.d(TAG, "Google user profile created successfully");
                    Toast.makeText(this, "Account created successfully!", Toast.LENGTH_SHORT).show();
                    navigateToMain();
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error creating Google user profile", e);
                    Toast.makeText(this, "Error creating profile: " + e.getMessage(),
                            Toast.LENGTH_SHORT).show();
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
        finish();
    }

    private void navigateToMain() {
        Intent intent = new Intent(this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}
