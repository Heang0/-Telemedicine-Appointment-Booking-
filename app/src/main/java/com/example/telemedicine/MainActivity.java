package com.example.telemedicine;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
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
        try {
            ThemeUtils.applyTheme(this);
        } catch (Exception e) {
            Log.e("MainActivity", "Error applying theme", e);
            Toast.makeText(this, "Theme error: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }

        super.onCreate(savedInstanceState);

        // Handle back button press
        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                if (getSupportFragmentManager().getBackStackEntryCount() > 0) {
                    getSupportFragmentManager().popBackStack();
                } else {
                    finish();
                }
            }
        });

        try {
            // Initialize Firebase Auth
            mAuth = FirebaseAuth.getInstance();

            // Check if Firebase is properly initialized
            if (mAuth == null) {
                Log.e("MainActivity", "FirebaseAuth instance is null!");
                Toast.makeText(this, "Firebase initialization failed", Toast.LENGTH_LONG).show();
                finish();
                return;
            }

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
        } catch (Exception e) {
            Log.e("MainActivity", "Critical error in onCreate", e);
            Toast.makeText(this, "App startup failed: " + e.getMessage(), Toast.LENGTH_LONG).show();
            finish();
        }
    }

    private void checkUserRoleAndRedirect(String userId) {
        try {
            FirebaseFirestore db = FirebaseFirestore.getInstance();

            // Store the listener reference to clean up later if needed
            userRoleListener = db.collection("users")
                    .document(userId)
                    .addSnapshotListener((documentSnapshot, e) -> {
                        try {
                            if (e != null) {
                                // If there's an error checking role, default to patient dashboard
                                Log.e("MainActivity", "Error checking user role", e);
                                runOnUiThread(() -> {
                                    Toast.makeText(MainActivity.this, "Error loading user data: " + e.getMessage(), Toast.LENGTH_LONG).show();
                                    setContentView(R.layout.activity_main);
                                    checkUserRoleAndRedirectFallback();
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

                                    // Redirect based on role - ALL use the same MainActivity with different menus
                                    runOnUiThread(() -> {
                                        setContentView(R.layout.activity_main);
                                        setupDashboard(role);
                                    });
                                    return;
                                }
                            }

                            // If no special role or user doesn't exist, continue with patient dashboard
                            runOnUiThread(() -> {
                                setContentView(R.layout.activity_main);
                                checkUserRoleAndRedirectFallback();
                            });
                        } catch (Exception ex) {
                            Log.e("MainActivity", "Exception in snapshot listener", ex);
                            runOnUiThread(() -> {
                                Toast.makeText(MainActivity.this, "Critical error: " + ex.getMessage(), Toast.LENGTH_LONG).show();
                                setContentView(R.layout.activity_main);
                                checkUserRoleAndRedirectFallback();
                            });
                        }
                    });
        } catch (Exception e) {
            Log.e("MainActivity", "Error in checkUserRoleAndRedirect", e);
            runOnUiThread(() -> {
                Toast.makeText(MainActivity.this, "Role check failed: " + e.getMessage(), Toast.LENGTH_LONG).show();
                setContentView(R.layout.activity_main);
                checkUserRoleAndRedirectFallback();
            });
        }
    }

    private void checkUserRoleAndRedirectFallback() {
        // Fallback method - default to patient dashboard
        String userId = mAuth.getCurrentUser() != null ? mAuth.getCurrentUser().getUid() : "";
        setupDashboard("patient");
    }

    private void setupDashboard(String role) {
        try {
            com.google.android.material.bottomnavigation.BottomNavigationView bottomNav = findViewById(R.id.bottom_navigation);
            if (bottomNav == null) {
                return;
            }

            bottomNav.setItemIconTintList(getResources().getColorStateList(R.color.bottom_nav_icon_color_ios, getTheme()));
            bottomNav.setItemBackgroundResource(R.drawable.bg_bottom_nav_item_ios);
            bottomNav.setItemTextColor(getResources().getColorStateList(R.color.bottom_nav_text_color_ios, getTheme()));
            bottomNav.setLabelVisibilityMode(com.google.android.material.bottomnavigation.LabelVisibilityMode.LABEL_VISIBILITY_SELECTED);

            // Set menu based on role
            if (UserRole.DOCTOR.getRoleName().equalsIgnoreCase(role)) {
                bottomNav.inflateMenu(R.menu.doctor_bottom_nav_menu);
                setupDoctorDashboard(bottomNav);
            } else if (UserRole.ADMIN.getRoleName().equalsIgnoreCase(role)) {
                bottomNav.inflateMenu(R.menu.admin_bottom_nav_menu);
                setupAdminDashboard(bottomNav);
            } else {
                bottomNav.inflateMenu(R.menu.bottom_nav_menu);
                setupPatientDashboard(bottomNav);
            }
        } catch (Exception e) {
            Log.e("MainActivity", "Error in setupDashboard", e);
        }
    }

    private void setupPatientDashboard(com.google.android.material.bottomnavigation.BottomNavigationView bottomNav) {
        try {
            FrameLayout container = findViewById(R.id.fragment_container);
            if (container == null) {
                return;
            }

            // Apply iOS-style appearance
            bottomNav.setItemIconTintList(getResources().getColorStateList(R.color.bottom_nav_icon_color_ios, getTheme()));
            bottomNav.setItemTextColor(getResources().getColorStateList(R.color.bottom_nav_text_color_ios, getTheme()));
            bottomNav.setLabelVisibilityMode(com.google.android.material.bottomnavigation.LabelVisibilityMode.LABEL_VISIBILITY_SELECTED);

            // Set up navigation listener FIRST
            bottomNav.setOnItemSelectedListener(item -> {
                Fragment selectedFragment = null;
                int itemId = item.getItemId();
                if (itemId == R.id.nav_patient) {
                    selectedFragment = new PatientDashboardFragment();
                } else if (itemId == R.id.nav_appointments) {
                    selectedFragment = new AppointmentsFragment();
                } else if (itemId == R.id.nav_prescriptions) {
                    selectedFragment = new PatientPrescriptionsFragment();
                } else if (itemId == R.id.nav_messages) {
                    selectedFragment = new SecureMessagingHubFragment();
                } else if (itemId == R.id.nav_settings) {
                    selectedFragment = new ProfileFragment();
                }

                if (selectedFragment != null) {
                    getSupportFragmentManager().beginTransaction()
                            .replace(R.id.fragment_container, selectedFragment)
                            .commit();
                    return true;
                }
                return false;
            });

            // Load default fragment
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, new PatientDashboardFragment())
                    .commit();

            // Set selected item AFTER listener is set up
            bottomNav.setSelectedItemId(R.id.nav_patient);
        } catch (Exception e) {
            Log.e("MainActivity", "Error in setupPatientDashboard", e);
        }
    }

    private void setupDoctorDashboard(com.google.android.material.bottomnavigation.BottomNavigationView bottomNav) {
        try {
            FrameLayout container = findViewById(R.id.fragment_container);
            if (container == null) {
                return;
            }

            // Apply iOS-style appearance
            bottomNav.setItemIconTintList(getResources().getColorStateList(R.color.bottom_nav_icon_color_ios, getTheme()));
            bottomNav.setItemTextColor(getResources().getColorStateList(R.color.bottom_nav_text_color_ios, getTheme()));
            bottomNav.setLabelVisibilityMode(com.google.android.material.bottomnavigation.LabelVisibilityMode.LABEL_VISIBILITY_SELECTED);

            // Set up navigation listener FIRST
            bottomNav.setOnItemSelectedListener(item -> {
                Fragment selectedFragment = null;
                int itemId = item.getItemId();
                if (itemId == R.id.nav_appointments) {
                    selectedFragment = new DoctorDashboardFragment();
                } else if (itemId == R.id.nav_patients) {
                    selectedFragment = new DoctorPatientsFragment();
                } else if (itemId == R.id.nav_prescriptions) {
                    selectedFragment = new PrescriptionManagerFragment();
                } else if (itemId == R.id.nav_messages) {
                    selectedFragment = new SecureMessagingHubFragment();
                } else if (itemId == R.id.nav_settings) {
                    selectedFragment = new ProfileFragment();
                }

                if (selectedFragment != null) {
                    getSupportFragmentManager().beginTransaction()
                            .replace(R.id.fragment_container, selectedFragment)
                            .commit();
                    return true;
                }
                return false;
            });

            // Load default fragment
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, new DoctorDashboardFragment())
                    .commit();

            // Set selected item AFTER listener is set up
            bottomNav.setSelectedItemId(R.id.nav_appointments);
        } catch (Exception e) {
            Log.e("MainActivity", "Error in setupDoctorDashboard", e);
        }
    }

    private void setupAdminDashboard(com.google.android.material.bottomnavigation.BottomNavigationView bottomNav) {
        try {
            FrameLayout container = findViewById(R.id.fragment_container);
            if (container == null) {
                return;
            }

            // Apply iOS-style appearance
            bottomNav.setItemIconTintList(getResources().getColorStateList(R.color.bottom_nav_icon_color_ios, getTheme()));
            bottomNav.setItemTextColor(getResources().getColorStateList(R.color.bottom_nav_text_color_ios, getTheme()));
            bottomNav.setLabelVisibilityMode(com.google.android.material.bottomnavigation.LabelVisibilityMode.LABEL_VISIBILITY_SELECTED);

            // Set up navigation listener FIRST
            bottomNav.setOnItemSelectedListener(item -> {
                Fragment selectedFragment = null;
                int itemId = item.getItemId();
                if (itemId == R.id.nav_admin_home) {
                    selectedFragment = new AdminDashboardFragment();
                } else if (itemId == R.id.nav_admin_users) {
                    selectedFragment = new AllUsersFragment();
                } else if (itemId == R.id.nav_admin_audit) {
                    selectedFragment = new AdminDashboardFragment();
                } else if (itemId == R.id.nav_admin_analytics) {
                    selectedFragment = new AdminDashboardFragment();
                } else if (itemId == R.id.nav_admin_profile) {
                    selectedFragment = new ProfileFragment();
                }

                if (selectedFragment != null) {
                    getSupportFragmentManager().beginTransaction()
                            .replace(R.id.fragment_container, selectedFragment)
                            .commit();
                    return true;
                }
                return false;
            });

            // Load default fragment
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, new AdminDashboardFragment())
                    .commit();

            // Set selected item AFTER listener is set up
            bottomNav.setSelectedItemId(R.id.nav_admin_home);
        } catch (Exception e) {
            Log.e("MainActivity", "Error in setupAdminDashboard", e);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        try {
            // Check if user is still signed in
            FirebaseUser currentUser = mAuth.getCurrentUser();
            if (currentUser == null) {
                // User is not signed in, redirect to login
                Intent intent = new Intent(MainActivity.this, LoginActivity.class);
                startActivity(intent);
                finish();
            }
        } catch (Exception e) {
            Log.e("MainActivity", "Error in onResume", e);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Clean up the listener to prevent memory leaks
        try {
            if (userRoleListener != null) {
                userRoleListener.remove();
            }
        } catch (Exception e) {
            Log.e("MainActivity", "Error cleaning up listener", e);
        }
    }
}
