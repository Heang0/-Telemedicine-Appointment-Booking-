package com.example.telemedicine;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.google.android.material.bottomnavigation.BottomNavigationView;

public class AdminDashboardActivity extends AppCompatActivity {

    public static boolean isThemeChange = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        ThemeUtils.applyTheme(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_dashboard);

        if (isThemeChange) {
            isThemeChange = false;
            setupFragments();
        } else {
            setupFragments();
        }
    }

    private void setupFragments() {
        BottomNavigationView bottomNav = findViewById(R.id.bottom_navigation);
        if (bottomNav != null) {
            bottomNav.setItemIconTintList(getResources().getColorStateList(R.color.bottom_nav_icon_color_ios, getTheme()));
            bottomNav.setItemBackgroundResource(R.drawable.bg_bottom_nav_item_ios);
            bottomNav.setLabelVisibilityMode(com.google.android.material.bottomnavigation.LabelVisibilityMode.LABEL_VISIBILITY_UNLABELED);

            if (getSupportFragmentManager().findFragmentById(R.id.fragment_container) == null) {
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.fragment_container, new AdminDashboardFragment())
                        .commit();
                bottomNav.setSelectedItemId(R.id.nav_admin_home);
            }

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
        }
    }
}
