package com.example.telemedicine;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.Timestamp;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Arrays; // ← Added for Arrays.asList

public class DigitalPrescriptionPadFragment extends Fragment {

    private Spinner spinnerPatient;
    private EditText editMedicationName, editDosage, editFrequency, editDuration, editQuantity, editInstructions, editNotes;
    private Button btnAddMedication, btnCreatePrescription;
    private Spinner spinnerMedicationsList;

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    private List<Prescription.Medication> medicationsList;
    private List<String> patientIds;
    private List<String> patientNames;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_digital_prescription_pad, container, false);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        initializeViews(view);
        setupMedicationsSpinner();
        loadPatients();

        btnAddMedication.setOnClickListener(v -> addMedicationToList());
        btnCreatePrescription.setOnClickListener(v -> createPrescription());

        return view;
    }

    private void initializeViews(View view) {
        spinnerPatient = view.findViewById(R.id.spinner_patient);
        editMedicationName = view.findViewById(R.id.edit_medication_name);
        editDosage = view.findViewById(R.id.edit_dosage);
        editFrequency = view.findViewById(R.id.edit_frequency);
        editDuration = view.findViewById(R.id.edit_duration);
        editQuantity = view.findViewById(R.id.edit_quantity);
        editInstructions = view.findViewById(R.id.edit_instructions);
        editNotes = view.findViewById(R.id.edit_notes);
        btnAddMedication = view.findViewById(R.id.btn_add_medication);
        btnCreatePrescription = view.findViewById(R.id.btn_create_prescription);
        spinnerMedicationsList = view.findViewById(R.id.spinner_medications_list);

        medicationsList = new ArrayList<>();
        patientIds = new ArrayList<>();
        patientNames = new ArrayList<>();
    }

    private void setupMedicationsSpinner() {
        // Common medications list
        String[] commonMeds = {
            "Paracetamol", "Ibuprofen", "Amoxicillin", "Azithromycin",
            "Lisinopril", "Metformin", "Atorvastatin", "Omeprazole",
            "Albuterol", "Levothyroxine", "Metoprolol", "Losartan"
        };

        ArrayAdapter<String> adapter = new ArrayAdapter<>(getContext(),
                android.R.layout.simple_spinner_item, commonMeds);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerMedicationsList.setAdapter(adapter);

        // Set up selection listener to auto-fill medication name
        spinnerMedicationsList.setOnItemSelectedListener(new android.widget.AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(android.widget.AdapterView<?> parent, View view, int position, long id) {
                String selectedMed = commonMeds[position];
                editMedicationName.setText(selectedMed);
            }

            @Override
            public void onNothingSelected(android.widget.AdapterView<?> parent) {
                // Do nothing
            }
        });
    }

    private void loadPatients() {
        String doctorId = mAuth.getCurrentUser().getUid();

        // Show loading indicator
        spinnerPatient.setEnabled(false);

        // Get appointments for this doctor to find associated patients
        db.collection("appointments")
                .whereEqualTo("doctorId", doctorId)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && task.getResult() != null) {
                        // Extract unique patient IDs from appointments
                        List<String> uniquePatientIds = new ArrayList<>();
                        for (com.google.firebase.firestore.QueryDocumentSnapshot document : task.getResult()) {
                            String patientId = document.getString("patientId");
                            if (patientId != null && !uniquePatientIds.contains(patientId)) {
                                uniquePatientIds.add(patientId);
                            }
                        }

                        // Fetch patient details
                        if (!uniquePatientIds.isEmpty()) {
                            db.collection("users")
                                    .whereIn("userId", uniquePatientIds)
                                    .get()
                                    .addOnCompleteListener(patientsTask -> {
                                        spinnerPatient.setEnabled(true);

                                        if (patientsTask.isSuccessful() && patientsTask.getResult() != null) {
                                            patientIds.clear();
                                            patientNames.clear();

                                            for (com.google.firebase.firestore.QueryDocumentSnapshot patientDoc : patientsTask.getResult()) {
                                                User patient = patientDoc.toObject(User.class);
                                                patientIds.add(patient.getUserId());
                                                patientNames.add(patient.getFullName());
                                            }

                                            if (patientNames.isEmpty()) {
                                                // No patients found
                                                ArrayAdapter<String> emptyAdapter = new ArrayAdapter<>(getContext(),
                                                        android.R.layout.simple_spinner_item, new String[]{"No patients found"});
                                                spinnerPatient.setAdapter(emptyAdapter);
                                            } else {
                                                ArrayAdapter<String> patientAdapter = new ArrayAdapter<>(getContext(),
                                                        android.R.layout.simple_spinner_item, patientNames);
                                                patientAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                                                spinnerPatient.setAdapter(patientAdapter);
                                            }
                                        } else {
                                            spinnerPatient.setEnabled(true);
                                            Toast.makeText(getContext(), "Error loading patients: " +
                                                    (patientsTask.getException() != null ? patientsTask.getException().getMessage() : "Unknown error"),
                                                    Toast.LENGTH_LONG).show();
                                        }
                                    });
                        } else {
                            spinnerPatient.setEnabled(true);
                            // No appointments found
                            ArrayAdapter<String> emptyAdapter = new ArrayAdapter<>(getContext(),
                                    android.R.layout.simple_spinner_item, new String[]{"No patients found"});
                            spinnerPatient.setAdapter(emptyAdapter);
                        }
                    } else {
                        spinnerPatient.setEnabled(true);
                        Toast.makeText(getContext(), "Error loading appointments: " +
                                (task.getException() != null ? task.getException().getMessage() : "Unknown error"),
                                Toast.LENGTH_LONG).show();
                    }
                });
    }

    private void addMedicationToList() {
        String name = editMedicationName.getText().toString().trim();
        String dosage = editDosage.getText().toString().trim();
        String frequency = editFrequency.getText().toString().trim();
        String duration = editDuration.getText().toString().trim();
        String quantity = editQuantity.getText().toString().trim();

        if (TextUtils.isEmpty(name)) {
            editMedicationName.setError("Medication name is required");
            editMedicationName.requestFocus();
            return;
        }

        if (TextUtils.isEmpty(dosage)) {
            editDosage.setError("Dosage is required");
            editDosage.requestFocus();
            return;
        }

        Prescription.Medication medication = new Prescription.Medication(name, dosage, frequency, duration, quantity);
        medicationsList.add(medication);

        // Clear the input fields
        editMedicationName.setText("");
        editDosage.setText("");
        editFrequency.setText("");
        editDuration.setText("");
        editQuantity.setText("");

        Toast.makeText(getContext(), "Medication added to list", Toast.LENGTH_SHORT).show();
    }

    private void createPrescription() {
        // Get selected patient
        int selectedPatientIndex = spinnerPatient.getSelectedItemPosition();
        if (selectedPatientIndex == -1 || patientIds.isEmpty() || selectedPatientIndex >= patientIds.size()) {
            Toast.makeText(getContext(), "Please select a valid patient", Toast.LENGTH_SHORT).show();
            return;
        }

        String patientId = patientIds.get(selectedPatientIndex);
        String patientName = patientNames.get(selectedPatientIndex);

        // Get doctor info — with null safety
        com.google.firebase.auth.FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null) {
            Toast.makeText(getContext(), "Error: Not signed in. Please sign in again.", Toast.LENGTH_LONG).show();
            android.util.Log.e("PrescriptionSave", "CurrentUser is null in createPrescription()");
            return;
        }
        String doctorId = currentUser.getUid();

        User doctor = TelemedicineApplication.getInstance().getCurrentUserProfile();
        String doctorName = doctor != null ? doctor.getFullName() : "Unknown Doctor";
        String doctorSpecialty = doctor != null ? doctor.getSpecialization() : "General";

        String instructions = editInstructions.getText().toString().trim();
        String notes = editNotes.getText().toString().trim();

        if (medicationsList.isEmpty()) {
            Toast.makeText(getContext(), "Please add at least one medication", Toast.LENGTH_SHORT).show();
            return;
        }

        // Convert medications list to Map-compatible format
        List<Map<String, Object>> medsMap = new ArrayList<>();
        for (Prescription.Medication med : medicationsList) {
            Map<String, Object> medMap = new HashMap<>();
            medMap.put("name", med.getName());
            medMap.put("dosage", med.getDosage());
            medMap.put("frequency", med.getFrequency());
            medMap.put("duration", med.getDuration());
            medMap.put("quantity", med.getQuantity());
            medsMap.add(medMap);
        }

        // Calculate expiry date (30 days from now) → convert to Timestamp
        Date expiryDate = new Date(System.currentTimeMillis() + (30L * 24 * 60 * 60 * 1000));
        Timestamp expiryTimestamp = new Timestamp(expiryDate);

        // Build prescription data map (Firestore-safe)
        Map<String, Object> prescriptionData = new HashMap<>();
        prescriptionData.put("doctorId", doctorId);
        prescriptionData.put("patientId", patientId);
        prescriptionData.put("patientIds", Arrays.asList(patientId));
        prescriptionData.put("patientName", patientName);
        prescriptionData.put("doctorName", doctorName);
        prescriptionData.put("doctorSpecialty", doctorSpecialty);
        prescriptionData.put("medications", medsMap);
        prescriptionData.put("instructions", instructions);
        prescriptionData.put("notes", notes);
        prescriptionData.put("status", "active");
        prescriptionData.put("createdAt", Timestamp.now());
        prescriptionData.put("updatedAt", Timestamp.now());
        prescriptionData.put("expiryDate", expiryTimestamp);
        prescriptionData.put("appointmentId", null);

        // Save to Firestore
        db.collection("prescriptions")
                .add(prescriptionData)
                .addOnSuccessListener(documentReference -> {
                    // Update appointment status to indicate prescription was issued
                    updateAppointmentWithPrescription(patientId, documentReference.getId());

                    Toast.makeText(getContext(), "Prescription created successfully!", Toast.LENGTH_SHORT).show();

                    // Navigate back to prescription manager
                    if (getActivity() != null) {
                        getActivity().getSupportFragmentManager()
                                .popBackStack();
                    }
                })
                .addOnFailureListener(e -> {
                    String errorMsg = e.getMessage() != null ? e.getMessage() : "Unknown error";
                    Toast.makeText(getContext(), "Failed to create prescription: " + errorMsg, Toast.LENGTH_LONG).show();
                    android.util.Log.e("PrescriptionSave", "Firestore write failed", e);
                });
    }

    private void updateAppointmentWithPrescription(String patientId, String prescriptionId) {
        // Find the most recent appointment with this patient to update with prescription reference
        String doctorId = mAuth.getCurrentUser().getUid();

        db.collection("appointments")
                .whereEqualTo("doctorId", doctorId)
                .whereEqualTo("patientId", patientId)
                .orderBy("scheduledTime", com.google.firebase.firestore.Query.Direction.DESCENDING) // Most recent first
                .limit(1)
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    if (!querySnapshot.isEmpty()) {
                        // Get the most recent appointment
                        com.google.firebase.firestore.DocumentSnapshot doc = querySnapshot.getDocuments().get(0);
                        String appointmentId = doc.getId();

                        // Update the appointment with prescription reference
                        db.collection("appointments").document(appointmentId)
                                .update("prescriptionId", prescriptionId)
                                .addOnSuccessListener(aVoid -> {
                                    // Also update the prescription with the appointment ID
                                    updatePrescriptionWithAppointmentId(prescriptionId, appointmentId);
                                })
                                .addOnFailureListener(e -> {
                                    // Log the error but don't show to user since prescription was created
                                    android.util.Log.e("DigitalPrescriptionPad", "Failed to update appointment with prescription ID", e);
                                });
                    }
                })
                .addOnFailureListener(e -> {
                    // Log the error but don't show to user since prescription was created
                    android.util.Log.e("DigitalPrescriptionPad", "Failed to find appointment to update with prescription", e);
                });
    }

    private void updatePrescriptionWithAppointmentId(String prescriptionId, String appointmentId) {
        // Update the prescription document with the appointment ID for better tracking
        db.collection("prescriptions").document(prescriptionId)
                .update("appointmentId", appointmentId)
                .addOnSuccessListener(aVoid -> {
                    // Successfully updated prescription with appointment ID
                    android.util.Log.d("DigitalPrescriptionPad", "Updated prescription with appointment ID: " + appointmentId);
                })
                .addOnFailureListener(e -> {
                    // Log the error but don't show to user
                    android.util.Log.e("DigitalPrescriptionPad", "Failed to update prescription with appointment ID", e);
                });
    }
}