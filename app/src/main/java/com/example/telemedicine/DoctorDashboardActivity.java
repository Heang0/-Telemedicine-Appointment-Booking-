package com.example.telemedicine;

import android.os.Bundle;
import android.view.MenuItem;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.google.android.material.bottomnavigation.BottomNavigationView;

public class DoctorDashboardActivity extends AppCompatActivity {

    public static boolean isThemeChange = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // Apply theme before calling super.onCreate
        ThemeUtils.applyTheme(this);

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_doctor_dashboard);

        // Check if this is a theme change recreation
        if (isThemeChange) {
            // This is a theme change, just restore the UI
            isThemeChange = false; // Reset the flag
            setupBottomNavigation();
        } else {
            // This is a fresh start, set up normally
            setupBottomNavigation();
        }
    }

    private void setupBottomNavigation() {
        BottomNavigationView bottomNav = findViewById(R.id.bottom_navigation);
        if (bottomNav != null) {
            // Load default fragment if container is empty
            if (getSupportFragmentManager().findFragmentById(R.id.fragment_container) == null) {
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.fragment_container, new DoctorDashboardFragment())
                        .commit();

                // Set the default selected item
                bottomNav.setSelectedItemId(R.id.nav_appointments);
            }

            bottomNav.setOnItemSelectedListener(item -> {
                Fragment selectedFragment = null;

                int itemId = item.getItemId();
                if (itemId == R.id.nav_appointments) {
                    selectedFragment = new DoctorDashboardFragment();
                } else if (itemId == R.id.nav_patients) {
                    selectedFragment = new DoctorPatientsFragment();
                } else if (itemId == R.id.nav_prescriptions) {
                    selectedFragment = new PrescriptionManagerFragment();
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
    }
}