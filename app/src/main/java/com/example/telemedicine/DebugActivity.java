package com.example.telemedicine;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class DebugActivity extends AppCompatActivity {
    private FirebaseAuth mAuth;
    private TextView statusText;
    private Button testButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        setContentView(R.layout.activity_login); // Use existing layout
        
        mAuth = FirebaseAuth.getInstance();
        statusText = findViewById(R.id.edit_email); // Reuse email field as status display
        testButton = findViewById(R.id.btn_login);

        if (statusText != null) {
            statusText.setText("Debug Mode: Firebase initialized");
        }

        if (testButton != null) {
            testButton.setText("Test Firebase");
            testButton.setOnClickListener(v -> testFirebase());
        }
    }

    private void testFirebase() {
        try {
            FirebaseUser user = mAuth.getCurrentUser();
            if (user == null) {
                Toast.makeText(this, "No user signed in", Toast.LENGTH_SHORT).show();
                statusText.setText("Status: No user signed in");
                
                // Try to sign in with test credentials (replace with your test email/password)
                mAuth.signInWithEmailAndPassword("test@example.com", "password123")
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            Toast.makeText(this, "Firebase test login successful!", Toast.LENGTH_LONG).show();
                            statusText.setText("Status: Firebase working!");
                        } else {
                            String errorMsg = task.getException() != null ? task.getException().getMessage() : "Unknown error";
                            Toast.makeText(this, "Firebase test failed: " + errorMsg, Toast.LENGTH_LONG).show();
                            statusText.setText("Status: Firebase error: " + errorMsg);
                        }
                    });
            } else {
                Toast.makeText(this, "User signed in: " + user.getEmail(), Toast.LENGTH_SHORT).show();
                statusText.setText("Status: User: " + user.getEmail());
            }
        } catch (Exception e) {
            Toast.makeText(this, "Firebase error: " + e.getMessage(), Toast.LENGTH_LONG).show();
            statusText.setText("Status: Error: " + e.getMessage());
        }
    }
}