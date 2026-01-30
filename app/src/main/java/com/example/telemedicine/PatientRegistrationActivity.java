package com.example.telemedicine;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class PatientRegistrationActivity extends AppCompatActivity {

    private EditText editFirstName, editLastName, editEmail, editPhone;
    private Spinner spinnerMonth, spinnerDay, spinnerYear;
    private RadioGroup radioGroupGender;
    private EditText editMedicalConditions, editAllergies, editCurrentMedications;
    private EditText editInsuranceProvider, editPolicyNumber;
    private Button btnRegister;

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_patient_registration);

        // Initialize Firebase
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        // Initialize views
        initializeViews();

        // Setup spinners
        setupSpinners();

        // Set click listener
        btnRegister = findViewById(R.id.btn_register);
        btnRegister.setOnClickListener(v -> completeRegistration());
    }

    private void initializeViews() {
        editFirstName = findViewById(R.id.edit_first_name);
        editLastName = findViewById(R.id.edit_last_name);
        editEmail = findViewById(R.id.edit_email);
        editPhone = findViewById(R.id.edit_phone);

        spinnerMonth = findViewById(R.id.spinner_month);
        spinnerDay = findViewById(R.id.spinner_day);
        spinnerYear = findViewById(R.id.spinner_year);

        radioGroupGender = findViewById(R.id.radio_group_gender);

        editMedicalConditions = findViewById(R.id.edit_medical_conditions);
        editAllergies = findViewById(R.id.edit_allergies);
        editCurrentMedications = findViewById(R.id.edit_current_medications);

        editInsuranceProvider = findViewById(R.id.edit_insurance_provider);
        editPolicyNumber = findViewById(R.id.edit_policy_number);
    }

    private void setupSpinners() {
        // Months
        String[] months = {"Jan", "Feb", "Mar", "Apr", "May", "Jun",
                          "Jul", "Aug", "Sep", "Oct", "Nov", "Dec"};
        ArrayAdapter<String> monthAdapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, months);
        monthAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerMonth.setAdapter(monthAdapter);

        // Days
        String[] days = new String[31];
        for (int i = 0; i < 31; i++) {
            days[i] = String.valueOf(i + 1);
        }
        ArrayAdapter<String> dayAdapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, days);
        dayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerDay.setAdapter(dayAdapter);

        // Years (last 100 years)
        int currentYear = 2026; // Using current year
        String[] years = new String[100];
        for (int i = 0; i < 100; i++) {
            years[i] = String.valueOf(currentYear - i);
        }
        ArrayAdapter<String> yearAdapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, years);
        yearAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerYear.setAdapter(yearAdapter);
    }

    private void completeRegistration() {
        // Validate required fields
        if (!validateFields()) {
            return;
        }

        // Get selected gender
        String gender = "";
        int selectedGenderId = radioGroupGender.getCheckedRadioButtonId();
        if (selectedGenderId != -1) {
            RadioButton selectedGender = findViewById(selectedGenderId);
            gender = selectedGender.getText().toString();
        }

        // Get selected date of birth
        String dob = spinnerMonth.getSelectedItem().toString() + "/" +
                     spinnerDay.getSelectedItem().toString() + "/" +
                     spinnerYear.getSelectedItem().toString();

        // Create patient data map
        Map<String, Object> patientData = new HashMap<>();
        patientData.put("firstName", editFirstName.getText().toString().trim());
        patientData.put("lastName", editLastName.getText().toString().trim());
        patientData.put("email", editEmail.getText().toString().trim());
        patientData.put("phone", editPhone.getText().toString().trim());
        patientData.put("dateOfBirth", dob);
        patientData.put("gender", gender);
        patientData.put("medicalConditions", editMedicalConditions.getText().toString().trim());
        patientData.put("allergies", editAllergies.getText().toString().trim());
        patientData.put("currentMedications", editCurrentMedications.getText().toString().trim());
        patientData.put("insuranceProvider", editInsuranceProvider.getText().toString().trim());
        patientData.put("policyNumber", editPolicyNumber.getText().toString().trim());
        patientData.put("registeredAt", System.currentTimeMillis());

        // Get current user ID
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null) {
            Toast.makeText(this, "User not authenticated", Toast.LENGTH_SHORT).show();
            return;
        }

        String userId = currentUser.getUid();

        // Save patient data to Firestore
        db.collection("users")
                .document(userId)
                .update(patientData)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(PatientRegistrationActivity.this,
                        "Patient information saved successfully!",
                        Toast.LENGTH_SHORT).show();

                    // Navigate to main dashboard
                    Intent intent = new Intent(PatientRegistrationActivity.this, MainActivity.class);
                    startActivity(intent);
                    finish(); // Close registration activity
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(PatientRegistrationActivity.this,
                        "Failed to save patient information: " + e.getMessage(),
                        Toast.LENGTH_LONG).show();
                });
    }

    private boolean validateFields() {
        if (TextUtils.isEmpty(editFirstName.getText().toString())) {
            editFirstName.setError("First name is required");
            editFirstName.requestFocus();
            return false;
        }

        if (TextUtils.isEmpty(editLastName.getText().toString())) {
            editLastName.setError("Last name is required");
            editLastName.requestFocus();
            return false;
        }

        if (TextUtils.isEmpty(editEmail.getText().toString())) {
            editEmail.setError("Email is required");
            editEmail.requestFocus();
            return false;
        }

        return true;
    }
}