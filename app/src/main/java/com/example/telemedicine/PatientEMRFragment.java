package com.example.telemedicine;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class PatientEMRFragment extends Fragment {

    private RecyclerView recyclerMedicalHistory;
    private MedicalHistoryAdapter historyAdapter;
    private final List<MedicalHistoryItem> historyItems = new ArrayList<>();
    private final List<MedicalHistoryItem> allHistoryItems = new ArrayList<>();
    private TextView textPatientName;
    private TextView textPatientDob;
    private TextView textPatientGender;
    private TextView textPatientMrn;
    private TextView textPrimaryCare;
    private TextView textBloodType;

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private String currentUserId;
    private String currentUserRole = UserRole.PATIENT.getRoleName();
    private String recordFilter = "";

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_patient_emr_ios, container, false);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        currentUserId = mAuth.getCurrentUser() != null ? mAuth.getCurrentUser().getUid() : null;

        if (getArguments() != null) {
            recordFilter = getArguments().getString("record_filter", "");
        }

        initializeViews(view);
        setupRecyclerView();
        loadDynamicData();

        return view;
    }

    private void initializeViews(View view) {
        recyclerMedicalHistory = view.findViewById(R.id.recycler_medical_history);
        textPatientName = view.findViewById(R.id.text_patient_name);
        textPatientDob = view.findViewById(R.id.text_patient_dob);
        textPatientGender = view.findViewById(R.id.text_patient_gender);
        textPatientMrn = view.findViewById(R.id.text_patient_mrn);
        textPrimaryCare = view.findViewById(R.id.text_primary_care);
        textBloodType = view.findViewById(R.id.text_blood_type);
    }

    private void setupRecyclerView() {
        recyclerMedicalHistory.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerMedicalHistory.setNestedScrollingEnabled(false);
        historyAdapter = new MedicalHistoryAdapter(historyItems);
        recyclerMedicalHistory.setAdapter(historyAdapter);
    }

    private void loadDynamicData() {
        if (currentUserId == null) {
            return;
        }

        db.collection("users")
                .document(currentUserId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    User user = documentSnapshot.toObject(User.class);
                    if (user == null) {
                        return;
                    }
                    if (user.getRole() != null && !user.getRole().trim().isEmpty()) {
                        currentUserRole = user.getRole();
                    }
                    populateUserInfo(user);
                    loadHistory(user);
                });
    }

    private void populateUserInfo(User user) {
        boolean isDoctor = UserRole.DOCTOR.getRoleName().equalsIgnoreCase(currentUserRole);
        if (textPatientName != null) {
            textPatientName.setText(user.getFullName() != null ? user.getFullName() : (isDoctor ? "Doctor" : "Patient"));
        }
        if (textPatientDob != null) {
            textPatientDob.setText(user.getDateOfBirth() != null && !user.getDateOfBirth().trim().isEmpty() ? user.getDateOfBirth() : "Not provided");
        }
        if (textPatientGender != null) {
            textPatientGender.setText(user.getGender() != null && !user.getGender().trim().isEmpty() ? user.getGender() : "Not provided");
        }
        if (textPatientMrn != null) {
            String identifier = isDoctor ? "Clinician ID" : "Patient ID";
            textPatientMrn.setText(identifier + ": " + (user.getUserId() != null ? user.getUserId() : currentUserId));
        }
        if (textPrimaryCare != null) {
            if (isDoctor) {
                String specialization = user.getSpecialization() != null && !user.getSpecialization().trim().isEmpty()
                        ? user.getSpecialization()
                        : "General Practice";
                textPrimaryCare.setText(specialization);
            } else {
                textPrimaryCare.setText("Care team synced from appointments");
            }
        }
        if (textBloodType != null) {
            textBloodType.setText("N/A");
        }
    }

    private void loadHistory(User user) {
        allHistoryItems.clear();
        addProfileHistory(user);
        publishHistory();

        String appointmentField = UserRole.DOCTOR.getRoleName().equalsIgnoreCase(currentUserRole) ? "doctorId" : "patientId";
        db.collection("appointments")
                .whereEqualTo(appointmentField, currentUserId)
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    SimpleDateFormat sdf = new SimpleDateFormat("MMM dd, yyyy", Locale.getDefault());
                    for (com.google.firebase.firestore.QueryDocumentSnapshot document : querySnapshot) {
                        Appointment appointment = document.toObject(Appointment.class);
                        String counterpartyName = UserRole.DOCTOR.getRoleName().equalsIgnoreCase(currentUserRole)
                                ? appointment.getPatientName()
                                : appointment.getDoctorName();
                        String summary = UserRole.DOCTOR.getRoleName().equalsIgnoreCase(currentUserRole)
                                ? "Consultation with " + safe(counterpartyName, "patient")
                                : "Visit with " + safe(counterpartyName, "doctor");
                        String dateText = appointment.getAppointmentDate() != null ? sdf.format(appointment.getAppointmentDate()) : "Scheduled";
                        String notes = safe(appointment.getConsultationType(), "consultation") + " • " + safe(appointment.getReason(), "No reason provided");
                        allHistoryItems.add(new MedicalHistoryItem(
                                summary,
                                dateText,
                                safe(counterpartyName, "Care team"),
                                safe(appointment.getStatus(), "scheduled"),
                                notes,
                                "visit"
                        ));
                    }
                    publishHistory();
                });

        String prescriptionField = UserRole.DOCTOR.getRoleName().equalsIgnoreCase(currentUserRole) ? "doctorId" : "patientId";
        db.collection("prescriptions")
                .whereEqualTo(prescriptionField, currentUserId)
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    SimpleDateFormat sdf = new SimpleDateFormat("MMM dd, yyyy", Locale.getDefault());
                    for (com.google.firebase.firestore.QueryDocumentSnapshot document : querySnapshot) {
                        Prescription prescription = document.toObject(Prescription.class);
                        String counterpartyName = UserRole.DOCTOR.getRoleName().equalsIgnoreCase(currentUserRole)
                                ? prescription.getPatientName()
                                : prescription.getDoctorName();
                        String medicationSummary = buildMedicationSummary(prescription);
                        String dateText = prescription.getPrescribedDate() != null
                                ? sdf.format(prescription.getPrescribedDate())
                                : formatTimestamp(prescription.getCreatedAt());
                        allHistoryItems.add(new MedicalHistoryItem(
                                "Prescription: " + medicationSummary,
                                dateText,
                                safe(counterpartyName, "Care team"),
                                safe(prescription.getStatus(), "active"),
                                safe(prescription.getInstructions(), "Medication plan available in prescription details"),
                                "prescription"
                        ));
                    }
                    publishHistory();
                });

        if (!UserRole.DOCTOR.getRoleName().equalsIgnoreCase(currentUserRole)) {
            db.collection("medical_records")
                    .whereEqualTo("patientId", currentUserId)
                    .get()
                    .addOnSuccessListener(querySnapshot -> {
                        for (com.google.firebase.firestore.QueryDocumentSnapshot document : querySnapshot) {
                            MedicalRecordsVault.MedicalRecord record = document.toObject(MedicalRecordsVault.MedicalRecord.class);
                            String sourceType = record.getRecordType() != null ? record.getRecordType() : "record";
                            allHistoryItems.add(new MedicalHistoryItem(
                                    safe(record.getTitle(), "Medical Record"),
                                    record.getCreatedAt() != null ? new SimpleDateFormat("MMM dd, yyyy", Locale.getDefault()).format(record.getCreatedAt()) : "Saved",
                                    "Health Records",
                                    safe(record.getStatus(), "active"),
                                    safe(record.getDescription(), "Secure clinical record"),
                                    sourceType
                            ));
                        }
                        publishHistory();
                    });
        }
    }

    private void addProfileHistory(User user) {
        if (user == null || UserRole.DOCTOR.getRoleName().equalsIgnoreCase(currentUserRole)) {
            return;
        }

        for (String allergy : splitValues(user.getAllergies())) {
            allHistoryItems.add(new MedicalHistoryItem(
                    "Allergy: " + allergy,
                    "Profile",
                    "Profile",
                    "active",
                    "Captured from patient registration",
                    "allergy"
            ));
        }

        for (String condition : splitValues(user.getMedicalConditions())) {
            allHistoryItems.add(new MedicalHistoryItem(
                    "Condition: " + condition,
                    "Profile",
                    "Profile",
                    "active",
                    "Tracked as part of ongoing medical history",
                    "condition"
            ));
        }
    }

    private List<String> splitValues(String rawValue) {
        List<String> values = new ArrayList<>();
        if (rawValue == null || rawValue.trim().isEmpty()) {
            return values;
        }
        String[] parts = rawValue.split("[,\n]");
        for (String part : parts) {
            if (!part.trim().isEmpty()) {
                values.add(part.trim());
            }
        }
        return values;
    }

    private void publishHistory() {
        historyItems.clear();
        for (MedicalHistoryItem item : allHistoryItems) {
            if (matchesFilter(item)) {
                historyItems.add(item);
            }
        }
        historyAdapter.updateHistoryItems(new ArrayList<>(historyItems));
    }

    private boolean matchesFilter(MedicalHistoryItem item) {
        if (recordFilter == null || recordFilter.trim().isEmpty()) {
            return true;
        }

        String filter = recordFilter.trim().toLowerCase(Locale.getDefault());
        switch (filter) {
            case "lab results":
                return "lab_result".equalsIgnoreCase(item.getSourceType());
            case "immunizations":
                return "immunization".equalsIgnoreCase(item.getSourceType());
            case "prescriptions":
                return "prescription".equalsIgnoreCase(item.getSourceType());
            case "visit history":
                return "visit".equalsIgnoreCase(item.getSourceType());
            case "allergies":
                return "allergy".equalsIgnoreCase(item.getSourceType());
            case "conditions":
                return "condition".equalsIgnoreCase(item.getSourceType());
            default:
                return true;
        }
    }

    private String buildMedicationSummary(Prescription prescription) {
        if (prescription.getMedications() == null || prescription.getMedications().isEmpty()) {
            return "Medication plan";
        }
        Prescription.Medication medication = prescription.getMedications().get(0);
        if (medication == null || medication.getName() == null || medication.getName().trim().isEmpty()) {
            return "Medication plan";
        }
        return medication.getName();
    }

    private String formatTimestamp(long value) {
        if (value <= 0) {
            return "Saved";
        }
        return new SimpleDateFormat("MMM dd, yyyy", Locale.getDefault()).format(new Date(value));
    }

    private String safe(String value, String fallback) {
        return value != null && !value.trim().isEmpty() ? value.trim() : fallback;
    }
}

