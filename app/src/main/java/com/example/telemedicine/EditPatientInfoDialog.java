package com.example.telemedicine;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class EditPatientInfoDialog {

    private final Context context;
    private final String patientId;
    private final FirebaseFirestore db;
    
    // Current patient data
    private String currentDob;
    private String currentGender;
    private String currentBloodType;
    private String currentHeight;
    private String currentWeight;
    private String currentAllergies;
    private String currentConditions;

    public EditPatientInfoDialog(@NonNull Context context, String patientId) {
        this.context = context;
        this.patientId = patientId;
        this.db = FirebaseFirestore.getInstance();
    }

    public void setCurrentData(String dob, String gender, String bloodType, 
                               String height, String weight, String allergies, String conditions) {
        this.currentDob = dob;
        this.currentGender = gender;
        this.currentBloodType = bloodType;
        this.currentHeight = height;
        this.currentWeight = weight;
        this.currentAllergies = allergies;
        this.currentConditions = conditions;
    }

    public void show() {
        // Create the dialog view
        View dialogView = LayoutInflater.from(context).inflate(R.layout.dialog_edit_patient_info, null);
        
        // Initialize views
        EditText editDob = dialogView.findViewById(R.id.edit_dob);
        EditText editGender = dialogView.findViewById(R.id.edit_gender);
        Spinner spinnerBloodType = dialogView.findViewById(R.id.spinner_blood_type);
        EditText editHeight = dialogView.findViewById(R.id.edit_height);
        EditText editWeight = dialogView.findViewById(R.id.edit_weight);
        EditText editAllergies = dialogView.findViewById(R.id.edit_allergies);
        EditText editConditions = dialogView.findViewById(R.id.edit_conditions);
        Button btnPickDob = dialogView.findViewById(R.id.btn_pick_dob);

        // Set current values
        editDob.setText(currentDob != null ? currentDob : "");
        editGender.setText(currentGender != null ? currentGender : "");
        editHeight.setText(currentHeight != null ? currentHeight : "");
        editWeight.setText(currentWeight != null ? currentWeight : "");
        editAllergies.setText(currentAllergies != null ? currentAllergies : "None");
        editConditions.setText(currentConditions != null ? currentConditions : "None");

        // Setup blood type spinner
        List<String> bloodTypes = new ArrayList<>();
        bloodTypes.add("A+");
        bloodTypes.add("A-");
        bloodTypes.add("B+");
        bloodTypes.add("B-");
        bloodTypes.add("AB+");
        bloodTypes.add("AB-");
        bloodTypes.add("O+");
        bloodTypes.add("O-");
        bloodTypes.add("Unknown");

        ArrayAdapter<String> adapter = new ArrayAdapter<>(context, 
                android.R.layout.simple_spinner_item, bloodTypes);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerBloodType.setAdapter(adapter);

        // Set selected blood type
        if (currentBloodType != null) {
            int position = bloodTypes.indexOf(currentBloodType);
            if (position >= 0) {
                spinnerBloodType.setSelection(position);
            }
        }

        // Date picker for DOB
        btnPickDob.setOnClickListener(v -> {
            Calendar calendar = Calendar.getInstance();
            DatePickerDialog datePickerDialog = new DatePickerDialog(
                    context,
                    (view, year, month, dayOfMonth) -> {
                        String date = String.format("%02d/%02d/%04d", dayOfMonth, month + 1, year);
                        editDob.setText(date);
                    },
                    calendar.get(Calendar.YEAR),
                    calendar.get(Calendar.MONTH),
                    calendar.get(Calendar.DAY_OF_MONTH)
            );
            datePickerDialog.show();
        });

        // Create and show dialog
        new MaterialAlertDialogBuilder(context)
                .setTitle("Edit Patient Information")
                .setView(dialogView)
                .setPositiveButton("Save", (dialog, which) -> {
                    // Get values
                    String dob = editDob.getText().toString().trim();
                    String gender = editGender.getText().toString().trim();
                    String bloodType = spinnerBloodType.getSelectedItem().toString();
                    String height = editHeight.getText().toString().trim();
                    String weight = editWeight.getText().toString().trim();
                    String allergies = editAllergies.getText().toString().trim();
                    String conditions = editConditions.getText().toString().trim();

                    // Validate
                    if (dob.isEmpty()) {
                        Toast.makeText(context, "Please enter date of birth", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    if (gender.isEmpty()) {
                        Toast.makeText(context, "Please enter gender", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    // Save to Firestore
                    savePatientInfo(dob, gender, bloodType, height, weight, allergies, conditions);
                })
                .setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss())
                .show();
    }

    private void savePatientInfo(String dob, String gender, String bloodType, 
                                 String height, String weight, String allergies, String conditions) {
        Map<String, Object> updates = new HashMap<>();
        updates.put("dateOfBirth", dob);
        updates.put("gender", gender);
        updates.put("bloodType", bloodType);
        updates.put("height", height);
        updates.put("weight", weight);
        updates.put("allergies", allergies);
        updates.put("medicalConditions", conditions);
        updates.put("updatedAt", System.currentTimeMillis());

        db.collection("users")
                .document(patientId)
                .update(updates)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(context, "Patient information updated successfully!", Toast.LENGTH_SHORT).show();
                    
                    // Also update patientProfile collection if it exists
                    db.collection("patientProfile")
                            .document(patientId)
                            .update(updates)
                            .addOnSuccessListener(aVoid2 -> {
                                // Success
                            })
                            .addOnFailureListener(e -> {
                                // Patient profile might not exist, that's ok
                            });
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(context, "Failed to update: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }
}
