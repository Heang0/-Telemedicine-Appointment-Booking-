package com.example.telemedicine;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

public class MainActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private com.google.firebase.firestore.ListenerRegistration userRoleListener;
    public static boolean isThemeChange = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // Apply theme before calling super.onCreate
        ThemeUtils.applyTheme(this);

        super.onCreate(savedInstanceState);

        // Initialize Firebase Auth
        mAuth = FirebaseAuth.getInstance();

        // Check if user is signed in
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null) {
            Intent intent = new Intent(MainActivity.this, LoginActivity.class);
            startActivity(intent);
            finish();
            return;
        }

        // Reset flag
        isThemeChange = false;

        // Proceed with role check
        Log.d("MainActivity", "User signed in: " + currentUser.getUid());
        Log.d("MainActivity", "Fresh start → checking user role");
        checkUserRoleAndRedirect(currentUser.getUid());
    }

    private void checkUserRoleAndRedirect(String userId) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        // Store the listener reference to clean up later if needed
        userRoleListener = db.collection("users")
                .document(userId)
                .addSnapshotListener((documentSnapshot, e) -> {
                    if (e != null) {
                        // If there's an error checking role, default to patient dashboard
                        Log.e("MainActivity", "Error checking user role", e);
                        runOnUiThread(() -> {
                            setContentView(R.layout.activity_main);
                            setupPatientDashboard();
                        });
                        return;
                    }

                    if (documentSnapshot != null && documentSnapshot.exists()) {
                        User user = documentSnapshot.toObject(User.class);
                        if (user != null) {
                            String role = user.getRole();

                            // Check if doctor is verified
                            if (UserRole.DOCTOR.getRoleName().equals(role) && !user.isVerified()) {
                                // Doctor not verified, show message or redirect to verification pending
                                runOnUiThread(() -> {
                                    Toast.makeText(MainActivity.this, "Your account is pending verification", Toast.LENGTH_LONG).show();
                                });
                                // For now, we'll still allow access but in a real app you might restrict features
                            }

                            // Redirect based on role
                            Intent intent;
                            if (UserRole.DOCTOR.getRoleName().equalsIgnoreCase(role)) {
                                intent = new Intent(MainActivity.this, DoctorDashboardActivity.class);
                            } else if (UserRole.ADMIN.getRoleName().equalsIgnoreCase(role)) {
                                intent = new Intent(MainActivity.this, AdminDashboardActivity.class);
                            } else {
                                // Default to patient dashboard - load the layout and setup UI
                                runOnUiThread(() -> {
                                    setContentView(R.layout.activity_main);
                                    setupPatientDashboard();
                                });
                                return; // Don't finish, continue with patient dashboard
                            }

                            runOnUiThread(() -> {
                                startActivity(intent);
                                finish(); // Close this activity to prevent back to patient dashboard
                            });
                            return;
                        }
                    }

                    // If no special role or user doesn't exist, continue with patient dashboard
                    runOnUiThread(() -> {
                        setContentView(R.layout.activity_main);
                        setupPatientDashboard();
                    });
                });
    }

    private void setupPatientDashboard() {
        // Ensure the layout is set before finding views
        setContentView(R.layout.activity_main);

        androidx.appcompat.widget.Toolbar toolbar = findViewById(R.id.toolbar);
        if (toolbar == null) {
            Toast.makeText(this, "ERROR: toolbar not found!", Toast.LENGTH_LONG).show();
            Log.e("MainActivity", "toolbar is null");
            return;
        }
        setSupportActionBar(toolbar);

        com.google.android.material.bottomnavigation.BottomNavigationView bottomNav = findViewById(R.id.bottom_navigation);
        if (bottomNav == null) {
            Toast.makeText(this, "ERROR: bottom_navigation not found!", Toast.LENGTH_LONG).show();
            Log.e("MainActivity", "bottom_navigation is null");
            return;
        }

        FrameLayout container = findViewById(R.id.fragment_container);
        if (container == null) {
            // CRITICAL: fragment_container NOT FOUND - this is likely causing "Hello World"
            Log.e("MainActivity", "fragment_container is null — layout mismatch?");

            // Force load the patient dashboard fragment directly
            runOnUiThread(() -> {
                try {
                    setContentView(R.layout.activity_main);

                    // Re-find the views after setting content view
                    Toolbar toolbar2 = findViewById(R.id.toolbar);
                    BottomNavigationView bottomNav2 = findViewById(R.id.bottom_navigation);
                    FrameLayout container2 = findViewById(R.id.fragment_container);

                    if (container2 != null) {
                        // Now load the fragment
                        getSupportFragmentManager().beginTransaction()
                                .replace(R.id.fragment_container, new PatientDashboardFragment())
                                .commit();

                        if (bottomNav2 != null) {
                            bottomNav2.setSelectedItemId(R.id.nav_patient);
                        }
                    } else {
                        // Ultimate fallback: show proper dashboard message
                        TextView textView = new TextView(MainActivity.this);
                        textView.setText("Telemedicine Dashboard");
                        textView.setTextSize(24);
                        textView.setGravity(android.view.Gravity.CENTER);
                        textView.setPadding(40, 40, 40, 40);
                        setContentView(textView);
                    }
                } catch (Exception e) {
                    Log.e("MainActivity", "Error in setupPatientDashboard", e);
                    TextView textView = new TextView(MainActivity.this);
                    textView.setText("Telemedicine App Dashboard");
                    textView.setTextSize(24);
                    textView.setGravity(android.view.Gravity.CENTER);
                    textView.setPadding(40, 40, 40, 40);
                    setContentView(textView);
                }
            });
            return;
        }

        // Load default fragment if container is empty
        if (getSupportFragmentManager().findFragmentById(R.id.fragment_container) == null) {
            Log.d("MainActivity", "Loading PatientDashboardFragment into fragment_container");
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, new PatientDashboardFragment())
                    .commit();

            // Set the default selected item
            bottomNav.setSelectedItemId(R.id.nav_patient);
        }

        bottomNav.setOnItemSelectedListener(item -> {
            Fragment selectedFragment = null;

            int itemId = item.getItemId();
            if (itemId == R.id.nav_patient) {
                selectedFragment = new PatientDashboardFragment();
            } else if (itemId == R.id.nav_appointments) {
                selectedFragment = new AppointmentsFragment();
            } else if (itemId == R.id.nav_prescriptions) {
                selectedFragment = new PatientPrescriptionsFragment();
            } else if (itemId == R.id.nav_profile) {
                selectedFragment = new ProfileFragment();
            } else if (itemId == R.id.nav_settings) {
                selectedFragment = new SettingsFragment();
            }

            if (selectedFragment != null) {
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.fragment_container, selectedFragment)
                        .commit();
                return true;
            }
            return false;
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Check if user is still signed in
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null) {
            // User is not signed in, redirect to login
            Intent intent = new Intent(MainActivity.this, LoginActivity.class);
            startActivity(intent);
            finish();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Clean up the listener to prevent memory leaks
        if (userRoleListener != null) {
            userRoleListener.remove();
        }
    }
}