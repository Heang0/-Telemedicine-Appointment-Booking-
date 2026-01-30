package com.example.telemedicine;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

public class HealthProfileFragment extends Fragment {

    private TextView textNameValue, textEmailValue, textPhoneValue, textDobValue, textGenderValue;
    private TextView textConditionsValue, textAllergiesValue, textMedicationsValue;
    private TextView textInsuranceProviderValue, textPolicyNumberValue;
    private Button btnEditProfile, btnUpdateHealthInfo;

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_health_profile, container, false);

        // Initialize Firebase
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        // Initialize views
        textNameValue = view.findViewById(R.id.text_name_value);
        textEmailValue = view.findViewById(R.id.text_email_value);
        textPhoneValue = view.findViewById(R.id.text_phone_value);
        textDobValue = view.findViewById(R.id.text_dob_value);
        textGenderValue = view.findViewById(R.id.text_gender_value);
        textConditionsValue = view.findViewById(R.id.text_conditions_value);
        textAllergiesValue = view.findViewById(R.id.text_allergies_value);
        textMedicationsValue = view.findViewById(R.id.text_medications_value);
        textInsuranceProviderValue = view.findViewById(R.id.text_insurance_provider_value);
        textPolicyNumberValue = view.findViewById(R.id.text_policy_number_value);
        btnEditProfile = view.findViewById(R.id.btn_edit_profile);
        btnUpdateHealthInfo = view.findViewById(R.id.btn_update_health_info);

        // Load user health profile data
        loadHealthProfile();

        // Set button click listeners if needed
        if (btnEditProfile != null) {
            btnEditProfile.setOnClickListener(v -> {
                // Navigate back to profile edit
                if (getActivity() != null) {
                    getActivity().getSupportFragmentManager()
                            .beginTransaction()
                            .replace(R.id.fragment_container, new ProfileFragment())
                            .addToBackStack(null)
                            .commit();
                }
            });
        }

        if (btnUpdateHealthInfo != null) {
            btnUpdateHealthInfo.setOnClickListener(v -> {
                // Navigate to update health info screen (could be a new fragment)
                Toast.makeText(getContext(), "Update health information feature coming soon", Toast.LENGTH_SHORT).show();
            });
        }

        return view;
    }

    private void loadHealthProfile() {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            String userId = currentUser.getUid();

            db.collection("users")
                    .document(userId)
                    .get()
                    .addOnSuccessListener(documentSnapshot -> {
                        if (documentSnapshot.exists()) {
                            User user = documentSnapshot.toObject(User.class);
                            if (user != null) {
                                // Populate the health profile fields with user data
                                if (textNameValue != null) {
                                    textNameValue.setText(user.getFullName());
                                }
                                if (textEmailValue != null) {
                                    textEmailValue.setText(user.getEmail());
                                }
                                if (textPhoneValue != null && user.getPhoneNumber() != null) {
                                    textPhoneValue.setText(user.getPhoneNumber());
                                } else if (textPhoneValue != null) {
                                    textPhoneValue.setText("Not provided");
                                }

                                // Populate health-related fields from the User object
                                if (textDobValue != null) {
                                    if (user.getDateOfBirth() != null && !user.getDateOfBirth().isEmpty()) {
                                        textDobValue.setText(user.getDateOfBirth());
                                    } else {
                                        textDobValue.setText("Not provided");
                                    }
                                }
                                if (textGenderValue != null) {
                                    if (user.getGender() != null && !user.getGender().isEmpty()) {
                                        textGenderValue.setText(user.getGender());
                                    } else {
                                        textGenderValue.setText("Not provided");
                                    }
                                }

                                // Populate medical history fields
                                if (textConditionsValue != null) {
                                    if (user.getMedicalConditions() != null && !user.getMedicalConditions().isEmpty()) {
                                        textConditionsValue.setText(user.getMedicalConditions());
                                    } else {
                                        textConditionsValue.setText("No data available");
                                    }
                                }
                                if (textAllergiesValue != null) {
                                    if (user.getAllergies() != null && !user.getAllergies().isEmpty()) {
                                        textAllergiesValue.setText(user.getAllergies());
                                    } else {
                                        textAllergiesValue.setText("No data available");
                                    }
                                }
                                if (textMedicationsValue != null) {
                                    if (user.getMedications() != null && !user.getMedications().isEmpty()) {
                                        textMedicationsValue.setText(user.getMedications());
                                    } else {
                                        textMedicationsValue.setText("No data available");
                                    }
                                }
                                if (textInsuranceProviderValue != null) {
                                    if (user.getInsuranceProvider() != null && !user.getInsuranceProvider().isEmpty()) {
                                        textInsuranceProviderValue.setText(user.getInsuranceProvider());
                                    } else {
                                        textInsuranceProviderValue.setText("No data available");
                                    }
                                }
                                if (textPolicyNumberValue != null) {
                                    if (user.getPolicyNumber() != null && !user.getPolicyNumber().isEmpty()) {
                                        textPolicyNumberValue.setText(user.getPolicyNumber());
                                    } else {
                                        textPolicyNumberValue.setText("No data available");
                                    }
                                }
                            }
                        }
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(getContext(), "Failed to load health profile: " + e.getMessage(),
                                Toast.LENGTH_SHORT).show();
                    });
        }
    }
}