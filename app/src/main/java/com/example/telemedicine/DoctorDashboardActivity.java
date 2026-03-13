package com.example.telemedicine;

import android.os.Bundle;
import android.content.res.ColorStateList;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

public class DoctorDashboardActivity extends AppCompatActivity {

    public static boolean isThemeChange = false;
    private int dockStartId = R.id.nav_appointments;
    private int dockSecondId = R.id.nav_patients;
    private int dockCenterId = R.id.nav_messages;
    private int dockFourthId = R.id.nav_prescriptions;
    private int dockEndId = R.id.nav_settings;

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
        bindDockItem(findViewById(R.id.nav_item_start), findViewById(R.id.nav_icon_start), findViewById(R.id.nav_label_start),
                new DockItem(R.id.nav_appointments, R.drawable.ic_home_ios, "Home"));
        bindDockItem(findViewById(R.id.nav_item_second), findViewById(R.id.nav_icon_second), findViewById(R.id.nav_label_second),
                new DockItem(R.id.nav_patients, R.drawable.ic_people_ios, "Patients"));
        bindDockItem(findViewById(R.id.nav_item_fourth), findViewById(R.id.nav_icon_fourth), findViewById(R.id.nav_label_fourth),
                new DockItem(R.id.nav_prescriptions, R.drawable.ic_prescription_ios, "Rx"));
        bindDockItem(findViewById(R.id.nav_item_end), findViewById(R.id.nav_icon_end), findViewById(R.id.nav_label_end),
                new DockItem(R.id.nav_settings, R.drawable.ic_profile_ios, "Profile"));

        ImageView centerIcon = findViewById(R.id.nav_center_icon);
        if (centerIcon != null) {
            centerIcon.setImageResource(R.drawable.ic_chat_ios);
            centerIcon.setImageTintList(ColorStateList.valueOf(getColor(android.R.color.white)));
        }

        View centerAction = findViewById(R.id.nav_center_action);
        if (centerAction != null) {
            centerAction.setOnClickListener(v -> selectDockItem(R.id.nav_messages));
        }

        Fragment currentFragment = getSupportFragmentManager().findFragmentById(R.id.fragment_container);
        if (currentFragment == null) {
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, new DoctorDashboardFragment())
                    .commit();
            updateDockSelection(R.id.nav_appointments);
        } else {
            syncDockSelection(currentFragment);
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

    private void selectDockItem(int navId) {
        Fragment selectedFragment = null;
        if (navId == R.id.nav_appointments) {
            selectedFragment = new DoctorDashboardFragment();
        } else if (navId == R.id.nav_patients) {
            selectedFragment = new DoctorPatientsFragment();
        } else if (navId == R.id.nav_prescriptions) {
            selectedFragment = new PrescriptionManagerFragment();
        } else if (navId == R.id.nav_messages) {
            selectedFragment = new SecureMessagingHubFragment();
        } else if (navId == R.id.nav_settings) {
            selectedFragment = new ProfileFragment();
        }

        if (selectedFragment != null) {
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, selectedFragment)
                    .commit();
            updateDockSelection(navId);
        }
    }

    private void syncDockSelection(Fragment fragment) {
        int selectedNavId = dockStartId;
        if (fragment instanceof DoctorPatientsFragment) {
            selectedNavId = dockSecondId;
        } else if (fragment instanceof PrescriptionManagerFragment) {
            selectedNavId = dockFourthId;
        } else if (fragment instanceof SecureMessagingHubFragment) {
            selectedNavId = dockCenterId;
        } else if (fragment instanceof ProfileFragment) {
            selectedNavId = dockEndId;
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
        iconView.setImageTintList(ColorStateList.valueOf(getColor(colorRes)));
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
}
