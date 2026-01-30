package com.example.telemedicine;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.fragment.app.Fragment;

import com.google.android.material.switchmaterial.SwitchMaterial;
import com.google.firebase.auth.FirebaseAuth;

public class SettingsFragment extends Fragment {

    private FirebaseAuth mAuth;
    private SwitchMaterial switchDarkMode;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_settings, container, false);

        // Initialize Firebase Auth
        mAuth = FirebaseAuth.getInstance();

        // Initialize views
        Button btnLogout = view.findViewById(R.id.btn_logout);
        switchDarkMode = view.findViewById(R.id.switch_dark_mode);

        // Setup dark mode switch
        setupDarkModeSwitch();

        // Find and set click listener for logout button
        if (btnLogout != null) {
            btnLogout.setOnClickListener(v -> logoutUser());
        }

        return view;
    }

    private void setupDarkModeSwitch() {
        if (switchDarkMode != null) {
            // Set initial state based on current theme
            boolean isDarkMode = ThemeUtils.isDarkMode(requireContext());
            switchDarkMode.setChecked(isDarkMode);

            // Set listener for theme changes
            switchDarkMode.setOnCheckedChangeListener((buttonView, isChecked) -> {
                int themeMode = isChecked ?
                    AppCompatDelegate.MODE_NIGHT_YES :
                    AppCompatDelegate.MODE_NIGHT_NO;

                // Save the selected theme mode
                ThemeUtils.saveThemeMode(requireContext(), themeMode);

                // Apply the theme
                AppCompatDelegate.setDefaultNightMode(themeMode);

                // Show a message to the user about the theme change
                Toast.makeText(requireContext(),
                    isChecked ? "Dark mode enabled" : "Light mode enabled",
                    Toast.LENGTH_SHORT).show();

                // Set the theme change flag before recreating the activity
                if (getActivity() instanceof MainActivity) {
                    MainActivity.isThemeChange = true;
                } else if (getActivity() instanceof DoctorDashboardActivity) {
                    DoctorDashboardActivity.isThemeChange = true;
                } else if (getActivity() instanceof AdminDashboardActivity) {
                    AdminDashboardActivity.isThemeChange = true;
                }

                // Recreate the activity to apply theme changes
                if (getActivity() != null) {
                    getActivity().recreate();
                }
            });
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        // Ensure the switch reflects the current theme when returning to the fragment
        if (switchDarkMode != null) {
            boolean isDarkMode = ThemeUtils.isDarkMode(requireContext());
            switchDarkMode.setChecked(isDarkMode);
        }
    }

    private void logoutUser() {
        mAuth.signOut();

        // Show confirmation
        Toast.makeText(getContext(), "Logged out successfully", Toast.LENGTH_SHORT).show();

        // Navigate back to login screen
        if (getActivity() != null) {
            Intent intent = new Intent(getActivity(), LoginActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            getActivity().finish();
        }
    }
}