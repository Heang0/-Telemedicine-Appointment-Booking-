package com.example.telemedicine;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

public class AdminDashboardActivity extends AppCompatActivity {

    public static boolean isThemeChange = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // Apply theme before calling super.onCreate
        ThemeUtils.applyTheme(this);

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_dashboard);

        // Check if this is a theme change recreation
        if (isThemeChange) {
            // This is a theme change, just restore the UI
            isThemeChange = false; // Reset the flag
            setupFragments();
        } else {
            // This is a fresh start, set up normally
            setupFragments();
        }
    }

    private void setupFragments() {
        // Load admin dashboard fragments
        if (getSupportFragmentManager().findFragmentById(R.id.fragment_container) == null) {
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, new AdminDashboardFragment())
                    .commit();
        }
    }
}