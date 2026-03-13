package com.example.telemedicine;

import android.content.Intent;
import android.content.res.ColorStateList;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

public class MainActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private com.google.firebase.firestore.ListenerRegistration userRoleListener;
    public static boolean isThemeChange = false;
    private String currentRole = "patient";
    private int dockStartId = View.NO_ID;
    private int dockSecondId = View.NO_ID;
    private int dockCenterId = View.NO_ID;
    private int dockFourthId = View.NO_ID;
    private int dockEndId = View.NO_ID;

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
            currentRole = role == null ? "patient" : role;
            FrameLayout container = findViewById(R.id.fragment_container);
            if (container == null) {
                return;
            }

            if (UserRole.DOCTOR.getRoleName().equalsIgnoreCase(currentRole)) {
                setupDoctorDashboard();
            } else if (UserRole.ADMIN.getRoleName().equalsIgnoreCase(currentRole)) {
                setupAdminDashboard();
            } else {
                setupPatientDashboard();
            }
        } catch (Exception e) {
            Log.e("MainActivity", "Error in setupDashboard", e);
        }
    }

    private void setupPatientDashboard() {
        try {
            configureDock(
                    new DockItem(R.id.nav_patient, R.drawable.ic_home_ios, "Home"),
                    new DockItem(R.id.nav_appointments, R.drawable.ic_calendar_ios, "Visits"),
                    new DockItem(R.id.nav_messages, R.drawable.ic_chat_ios, null),
                    new DockItem(R.id.nav_prescriptions, R.drawable.ic_prescription_ios, "Meds"),
                    new DockItem(R.id.nav_settings, R.drawable.ic_profile_ios, "Profile"));
            ensureDefaultFragment(new PatientDashboardFragment(), R.id.nav_patient);
        } catch (Exception e) {
            Log.e("MainActivity", "Error in setupPatientDashboard", e);
        }
    }

    private void setupDoctorDashboard() {
        try {
            configureDock(
                    new DockItem(R.id.nav_appointments, R.drawable.ic_home_ios, "Home"),
                    new DockItem(R.id.nav_patients, R.drawable.ic_people_ios, "Patients"),
                    new DockItem(R.id.nav_messages, R.drawable.ic_chat_ios, null),
                    new DockItem(R.id.nav_prescriptions, R.drawable.ic_prescription_ios, "Rx"),
                    new DockItem(R.id.nav_settings, R.drawable.ic_profile_ios, "Profile"));
            ensureDefaultFragment(new DoctorDashboardFragment(), R.id.nav_appointments);
        } catch (Exception e) {
            Log.e("MainActivity", "Error in setupDoctorDashboard", e);
        }
    }

    private void setupAdminDashboard() {
        try {
            configureDock(
                    new DockItem(R.id.nav_admin_home, R.drawable.ic_home_ios, "Home"),
                    new DockItem(R.id.nav_admin_users, R.drawable.ic_people_ios, "Users"),
                    new DockItem(R.id.nav_admin_audit, R.drawable.ic_chat_ios, null),
                    new DockItem(R.id.nav_admin_analytics, R.drawable.ic_calendar_ios, "Stats"),
                    new DockItem(R.id.nav_admin_profile, R.drawable.ic_profile_ios, "Profile"));
            ensureDefaultFragment(new AdminDashboardFragment(), R.id.nav_admin_home);
        } catch (Exception e) {
            Log.e("MainActivity", "Error in setupAdminDashboard", e);
        }
    }

    private void configureDock(DockItem start, DockItem second, DockItem center, DockItem fourth, DockItem end) {
        bindDockItem(findViewById(R.id.nav_item_start), findViewById(R.id.nav_icon_start), findViewById(R.id.nav_label_start), start);
        bindDockItem(findViewById(R.id.nav_item_second), findViewById(R.id.nav_icon_second), findViewById(R.id.nav_label_second), second);
        bindDockItem(findViewById(R.id.nav_item_fourth), findViewById(R.id.nav_icon_fourth), findViewById(R.id.nav_label_fourth), fourth);
        bindDockItem(findViewById(R.id.nav_item_end), findViewById(R.id.nav_icon_end), findViewById(R.id.nav_label_end), end);

        View centerAction = findViewById(R.id.nav_center_action);
        ImageView centerIcon = findViewById(R.id.nav_center_icon);
        dockStartId = start.navId;
        dockSecondId = second.navId;
        dockCenterId = center.navId;
        dockFourthId = fourth.navId;
        dockEndId = end.navId;

        if (centerIcon != null) {
            centerIcon.setImageResource(center.iconResId);
            centerIcon.setImageTintList(ColorStateList.valueOf(getColor(android.R.color.white)));
        }
        if (centerAction != null) {
            centerAction.setOnClickListener(v -> selectDockItem(center.navId));
        }
    }

    private void bindDockItem(View itemView, ImageView iconView, TextView labelView, DockItem item) {
        if (itemView == null || iconView == null || labelView == null) {
            return;
        }
        iconView.setImageResource(item.iconResId);
        labelView.setText(item.label);
        itemView.setOnClickListener(v -> selectDockItem(item.navId));
    }

    private void ensureDefaultFragment(Fragment defaultFragment, int defaultNavId) {
        Fragment currentFragment = getSupportFragmentManager().findFragmentById(R.id.fragment_container);
        if (currentFragment == null) {
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, defaultFragment)
                    .commit();
            updateDockSelection(defaultNavId);
        } else {
            syncDockSelectionForFragment(currentFragment, defaultNavId);
        }
    }

    private void selectDockItem(int navId) {
        Fragment selectedFragment = createFragmentForNav(navId);
        if (selectedFragment == null) {
            return;
        }
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, selectedFragment)
                .commit();
        updateDockSelection(navId);
    }

    private Fragment createFragmentForNav(int navId) {
        if (UserRole.DOCTOR.getRoleName().equalsIgnoreCase(currentRole)) {
            if (navId == R.id.nav_appointments) {
                return new DoctorDashboardFragment();
            }
            if (navId == R.id.nav_patients) {
                return new DoctorPatientsFragment();
            }
            if (navId == R.id.nav_messages) {
                return new SecureMessagingHubFragment();
            }
            if (navId == R.id.nav_prescriptions) {
                return new PrescriptionManagerFragment();
            }
            if (navId == R.id.nav_settings) {
                return new ProfileFragment();
            }
            return null;
        }

        if (UserRole.ADMIN.getRoleName().equalsIgnoreCase(currentRole)) {
            if (navId == R.id.nav_admin_home || navId == R.id.nav_admin_audit || navId == R.id.nav_admin_analytics) {
                return new AdminDashboardFragment();
            }
            if (navId == R.id.nav_admin_users) {
                return new AllUsersFragment();
            }
            if (navId == R.id.nav_admin_profile) {
                return new ProfileFragment();
            }
            return null;
        }

        if (navId == R.id.nav_patient) {
            return new PatientDashboardFragment();
        }
        if (navId == R.id.nav_appointments) {
            return new AppointmentsFragment();
        }
        if (navId == R.id.nav_messages) {
            return new SecureMessagingHubFragment();
        }
        if (navId == R.id.nav_prescriptions) {
            return new PatientPrescriptionsFragment();
        }
        if (navId == R.id.nav_settings) {
            return new ProfileFragment();
        }
        return null;
    }

    private void syncDockSelectionForFragment(Fragment fragment, int fallbackNavId) {
        int selectedNavId = fallbackNavId;

        if (fragment instanceof SecureMessagingHubFragment) {
            selectedNavId = dockCenterId;
        } else if (fragment instanceof ProfileFragment) {
            selectedNavId = dockEndId;
        } else if (UserRole.DOCTOR.getRoleName().equalsIgnoreCase(currentRole)) {
            if (fragment instanceof DoctorPatientsFragment) {
                selectedNavId = dockSecondId;
            } else if (fragment instanceof PrescriptionManagerFragment) {
                selectedNavId = dockFourthId;
            } else if (fragment instanceof DoctorDashboardFragment) {
                selectedNavId = dockStartId;
            }
        } else if (UserRole.ADMIN.getRoleName().equalsIgnoreCase(currentRole)) {
            if (fragment instanceof AllUsersFragment) {
                selectedNavId = dockSecondId;
            } else if (fragment instanceof AdminDashboardFragment) {
                selectedNavId = dockStartId;
            }
        } else {
            if (fragment instanceof AppointmentsFragment) {
                selectedNavId = dockSecondId;
            } else if (fragment instanceof PatientPrescriptionsFragment) {
                selectedNavId = dockFourthId;
            } else if (fragment instanceof PatientDashboardFragment) {
                selectedNavId = dockStartId;
            }
        }

        updateDockSelection(selectedNavId);
    }

    private void updateDockSelection(int selectedNavId) {
        applyDockSelection(findViewById(R.id.nav_icon_start), findViewById(R.id.nav_label_start), selectedNavId == dockStartId);
        applyDockSelection(findViewById(R.id.nav_icon_second), findViewById(R.id.nav_label_second), selectedNavId == dockSecondId);
        applyDockSelection(findViewById(R.id.nav_icon_fourth), findViewById(R.id.nav_label_fourth), selectedNavId == dockFourthId);
        applyDockSelection(findViewById(R.id.nav_icon_end), findViewById(R.id.nav_label_end), selectedNavId == dockEndId);
        View centerAction = findViewById(R.id.nav_center_action);
        if (centerAction != null) {
            centerAction.setAlpha(selectedNavId == dockCenterId ? 1f : 0.98f);
        }
    }

    private void applyDockSelection(ImageView iconView, TextView labelView, boolean selected) {
        if (iconView == null || labelView == null) {
            return;
        }
        int colorRes = selected ? R.color.bottom_dock_active : R.color.bottom_dock_inactive;
        ColorStateList tint = ColorStateList.valueOf(getColor(colorRes));
        iconView.setImageTintList(tint);
        labelView.setTextColor(getColor(colorRes));
        labelView.setAlpha(selected ? 1f : 0.82f);
    }

    private static final class DockItem {
        private final int navId;
        private final int iconResId;
        private final String label;

        private DockItem(int navId, int iconResId, String label) {
            this.navId = navId;
            this.iconResId = iconResId;
            this.label = label;
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
