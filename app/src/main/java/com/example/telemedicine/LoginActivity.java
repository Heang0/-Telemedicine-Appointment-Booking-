package com.example.telemedicine;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
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
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.firestore.FirebaseFirestore;

public class LoginActivity extends AppCompatActivity {
    private static final String TAG = "LoginActivity";
    private static final int RC_GOOGLE_SIGN_IN = 9001;

    private TextInputEditText editTextEmail;
    private TextInputEditText editTextPassword;
    private MaterialButton btnLogin;
    private MaterialButton btnGoogleSignIn;
    private TextView textSignUp;
    private TextView textForgotPassword;
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
        setContentView(R.layout.activity_login);
        Log.d(TAG, "LoginActivity created!");

        // Initialize Firebase
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        // Configure Google Sign-In
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();
        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);

        // Initialize views
        editTextEmail = findViewById(R.id.edit_email);
        editTextPassword = findViewById(R.id.edit_password);
        btnLogin = findViewById(R.id.btn_login);
        btnGoogleSignIn = findViewById(R.id.btn_google_sign_in);
        textSignUp = findViewById(R.id.text_sign_up);
        textForgotPassword = findViewById(R.id.text_forgot_password);

        // Set click listeners
        if (btnLogin != null) {
            btnLogin.setOnClickListener(v -> loginUser());
        } else {
            Log.e(TAG, "btn_login NOT FOUND!");
        }

        if (btnGoogleSignIn != null) {
            btnGoogleSignIn.setOnClickListener(v -> signInWithGoogle());
        } else {
            Log.e(TAG, "btn_google_sign_in NOT FOUND!");
        }

        if (textSignUp != null) {
            textSignUp.setOnClickListener(v -> {
                Intent intent = new Intent(LoginActivity.this, RoleSelectionRegistrationActivity.class);
                startActivity(intent);
            });
        }

        if (textForgotPassword != null) {
            textForgotPassword.setOnClickListener(v -> {
                Toast.makeText(this, "Password reset feature coming soon", Toast.LENGTH_SHORT).show();
            });
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        // Check if user is already signed in
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            // User is already signed in, navigate to main activity
            navigateToMain();
        }
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

        // Show loading indicator
        btnLogin.setEnabled(false);
        btnLogin.setText("Logging in...");

        // Sign in with email and password
        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    btnLogin.setEnabled(true);
                    btnLogin.setText("Sign In");

                    if (task.isSuccessful()) {
                        // Sign in success
                        FirebaseUser user = mAuth.getCurrentUser();
                        Log.d(TAG, "Login successful: " + user.getUid());

                        // Check if user exists in Firestore
                        db.collection("users")
                                .document(user.getUid())
                                .get()
                                .addOnCompleteListener(task2 -> {
                                    if (task2.isSuccessful() && task2.getResult() != null && task2.getResult().exists()) {
                                        // User exists in Firestore, proceed to main activity
                                        navigateToMain();
                                    } else {
                                        // User doesn't exist in Firestore, redirect to registration
                                        Log.e(TAG, "User not found in Firestore");
                                        Toast.makeText(LoginActivity.this,
                                                "User account not found. Please register first.",
                                                Toast.LENGTH_LONG).show();

                                        // Redirect to registration
                                        Intent intent = new Intent(LoginActivity.this, RoleSelectionRegistrationActivity.class);
                                        startActivity(intent);
                                        finish();
                                    }
                                });
                    } else {
                        // Sign in failed
                        Exception exception = task.getException();
                        String errorMessage = "Login failed";
                        if (exception != null) {
                            errorMessage += ": " + exception.getMessage();
                        }
                        Log.e(TAG, "Login failed", exception);
                        Toast.makeText(LoginActivity.this,
                                errorMessage,
                                Toast.LENGTH_LONG).show();
                    }
                });
    }

    private void signInWithGoogle() {
        Intent signInIntent = mGoogleSignInClient.getSignInIntent();
        googleSignInLauncher.launch(signInIntent);
    }

    private void handleGoogleSignInResult(Intent data) {
        try {
            GoogleSignInAccount account = GoogleSignIn.getSignedInAccountFromIntent(data)
                    .getResult(ApiException.class);
            Log.d(TAG, "Google sign-in successful: " + account.getEmail());
            firebaseAuthWithGoogle(account.getIdToken());
        } catch (ApiException e) {
            Log.w(TAG, "Google sign-in failed", e);
            Toast.makeText(this, "Google sign-in failed: " + e.getMessage(),
                    Toast.LENGTH_SHORT).show();
        }
    }

    private void firebaseAuthWithGoogle(String idToken) {
        AuthCredential credential = GoogleAuthProvider.getCredential(idToken, null);
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
                                            Log.d(TAG, "Existing user signed in with Google");
                                            navigateToMain();
                                        } else {
                                            // New user, create profile and navigate to registration
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
        // Create user profile in Firestore
        User userProfile = new User(
                user.getUid(),
                user.getDisplayName() != null ? user.getDisplayName() : "Google User",
                user.getEmail() != null ? user.getEmail() : "",
                System.currentTimeMillis()
        );

        db.collection("users")
                .document(user.getUid())
                .set(userProfile)
                .addOnSuccessListener(aVoid -> {
                    Log.d(TAG, "Google user profile created successfully");
                    navigateToMain();
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error creating Google user profile", e);
                    Toast.makeText(this, "Error creating profile: " + e.getMessage(),
                            Toast.LENGTH_SHORT).show();
                });
    }

    private void navigateToMain() {
        Intent intent = new Intent(LoginActivity.this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}
