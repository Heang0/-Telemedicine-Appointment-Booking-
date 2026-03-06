package com.example.telemedicine;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

public class LoginActivity extends AppCompatActivity {
    private static final String TAG = "LoginActivity";

    private TextInputEditText editTextEmail;
    private TextInputEditText editTextPassword;
    private MaterialButton btnLogin;
    private TextView textSignUp;
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        Log.d(TAG, "LoginActivity created!");

        // Initialize Firebase
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        // Initialize views
        editTextEmail = findViewById(R.id.edit_email);
        editTextPassword = findViewById(R.id.edit_password);
        btnLogin = findViewById(R.id.btn_login);
        textSignUp = findViewById(R.id.text_sign_up);

        // Set click listeners
        if (btnLogin != null) {
            btnLogin.setOnClickListener(v -> loginUser());
        } else {
            Log.e(TAG, "btn_login NOT FOUND!");
        }

        if (textSignUp != null) {
            textSignUp.setOnClickListener(v -> {
                Intent intent = new Intent(LoginActivity.this, RoleSelectionRegistrationActivity.class);
                startActivity(intent);
            });
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
                    btnLogin.setText("Login");

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
                                        Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                                        startActivity(intent);
                                        finish();
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
}
