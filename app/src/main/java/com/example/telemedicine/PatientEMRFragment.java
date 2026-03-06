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

    private static final String ARG_PATIENT_ID = "patient_id";
    private static final String ARG_PATIENT_NAME = "patient_name";
    private static final String ARG_RECORD_FILTER = "record_filter";

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
    private String targetPatientId;
    private String targetPatientName;
    private String recordFilter = "";

    public static PatientEMRFragment newInstance(String patientId, String patientName) {
        PatientEMRFragment fragment = new PatientEMRFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PATIENT_ID, patientId);
        args.putString(ARG_PATIENT_NAME, patientName);
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_patient_emr_ios, container, false);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        currentUserId = mAuth.getCurrentUser() != null ? mAuth.getCurrentUser().getUid() : null;

        if (getArguments() != null) {
            recordFilter = getArguments().getString(ARG_RECORD_FILTER, "");
            targetPatientId = getArguments().getString(ARG_PATIENT_ID);
            targetPatientName = getArguments().getString(ARG_PATIENT_NAME);
        }
        if (targetPatientId == null || targetPatientId.trim().isEmpty()) {
            targetPatientId = currentUserId;
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
        if (currentUserId == null || targetPatientId == null || targetPatientId.trim().isEmpty()) {
            return;
        }

        db.collection("users")
                .document(currentUserId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    User currentUser = documentSnapshot.toObject(User.class);
                    if (currentUser != null && currentUser.getRole() != null && !currentUser.getRole().trim().isEmpty()) {
                        currentUserRole = currentUser.getRole().trim();
                    }

                    db.collection("users")
                            .document(targetPatientId)
                            .get()
                            .addOnSuccessListener(patientSnapshot -> {
                                User patient = patientSnapshot.toObject(User.class);
                                if (patient == null) {
                                    return;
                                }
                                if ((patient.getUserId() == null || patient.getUserId().trim().isEmpty()) && patientSnapshot.getId() != null) {
                                    patient.setUserId(patientSnapshot.getId());
                                }
                                populateUserInfo(patient);
                                loadHistory(patient);
                            });
                });
    }

    private void populateUserInfo(User user) {
        String resolvedPatientName = user.getFullName() != null && !user.getFullName().trim().isEmpty()
                ? user.getFullName().trim()
                : safe(targetPatientName, "Patient");
        String resolvedPatientId = user.getUserId() != null && !user.getUserId().trim().isEmpty()
                ? user.getUserId().trim()
                : targetPatientId;

        if (textPatientName != null) {
            textPatientName.setText(resolvedPatientName);
        }
        if (textPatientDob != null) {
            textPatientDob.setText(formatProfileValue(user.getDateOfBirth(), "Not provided"));
        }
        if (textPatientGender != null) {
            textPatientGender.setText(formatProfileValue(user.getGender(), "Not provided"));
        }
        if (textPatientMrn != null) {
            textPatientMrn.setText("Patient ID: " + safe(resolvedPatientId, "Unavailable"));
        }
        if (textPrimaryCare != null) {
            textPrimaryCare.setText(formatProfileValue(user.getPrimaryCareProvider(), "Loading..."));
        }
        if (textBloodType != null) {
            textBloodType.setText(formatProfileValue(user.getBloodType(), "Not recorded"));
        }
    }

    private void loadHistory(User user) {
        allHistoryItems.clear();
        addProfileHistory(user);
        publishHistory();

        db.collection("appointments")
                .whereEqualTo("patientId", targetPatientId)
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    SimpleDateFormat sdf = new SimpleDateFormat("MMM dd, yyyy", Locale.getDefault());
                    Appointment latestAppointment = null;
                    for (com.google.firebase.firestore.QueryDocumentSnapshot document : querySnapshot) {
                        Appointment appointment = document.toObject(Appointment.class);
                        String doctorName = safe(appointment.getDoctorName(), "Care team");
                        String dateText = appointment.getAppointmentDate() != null ? sdf.format(appointment.getAppointmentDate()) : "Scheduled";
                        String notes = safe(appointment.getConsultationType(), "consultation") + " - " + safe(appointment.getReason(), "No reason provided");
                        allHistoryItems.add(new MedicalHistoryItem(
                                "Consultation with " + doctorName,
                                dateText,
                                doctorName,
                                safe(appointment.getStatus(), "scheduled"),
                                notes,
                                "visit"
                        ));
                        if (latestAppointment == null || compareAppointments(appointment, latestAppointment) > 0) {
                            latestAppointment = appointment;
                        }
                    }
                    applyPrimaryCareFallback(user, latestAppointment);
                    publishHistory();
                });

        db.collection("prescriptions")
                .whereEqualTo("patientId", targetPatientId)
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    SimpleDateFormat sdf = new SimpleDateFormat("MMM dd, yyyy", Locale.getDefault());
                    for (com.google.firebase.firestore.QueryDocumentSnapshot document : querySnapshot) {
                        Prescription prescription = document.toObject(Prescription.class);
                        String dateText = prescription.getPrescribedDate() != null
                                ? sdf.format(prescription.getPrescribedDate())
                                : formatTimestamp(prescription.getCreatedAt());
                        allHistoryItems.add(new MedicalHistoryItem(
                                "Prescription: " + buildMedicationSummary(prescription),
                                dateText,
                                safe(prescription.getDoctorName(), "Care team"),
                                safe(prescription.getStatus(), "active"),
                                safe(prescription.getInstructions(), "Medication plan available in prescription details"),
                                "prescription"
                        ));
                    }
                    publishHistory();
                });

        db.collection("medical_records")
                .whereEqualTo("patientId", targetPatientId)
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    for (com.google.firebase.firestore.QueryDocumentSnapshot document : querySnapshot) {
                        MedicalRecordsVault.MedicalRecord record = document.toObject(MedicalRecordsVault.MedicalRecord.class);
                        String sourceType = record.getRecordType() != null ? record.getRecordType() : "record";
                        allHistoryItems.add(new MedicalHistoryItem(
                                safe(record.getTitle(), "Medical Record"),
                                record.getCreatedAt() != null
                                        ? new SimpleDateFormat("MMM dd, yyyy", Locale.getDefault()).format(record.getCreatedAt())
                                        : "Saved",
                                "Health Records",
                                safe(record.getStatus(), "active"),
                                safe(record.getDescription(), "Secure clinical record"),
                                sourceType
                        ));
                    }
                    publishHistory();
                });
    }

    private void addProfileHistory(User user) {
        if (user == null || UserRole.DOCTOR.getRoleName().equalsIgnoreCase(user.getRole())) {
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

    private void applyPrimaryCareFallback(User user, Appointment latestAppointment) {
        if (textPrimaryCare == null) {
            return;
        }
        if (user.getPrimaryCareProvider() != null && !user.getPrimaryCareProvider().trim().isEmpty()) {
            textPrimaryCare.setText(user.getPrimaryCareProvider().trim());
            return;
        }
        if (latestAppointment != null && latestAppointment.getDoctorName() != null && !latestAppointment.getDoctorName().trim().isEmpty()) {
            textPrimaryCare.setText(latestAppointment.getDoctorName().trim());
            return;
        }
        textPrimaryCare.setText("Not assigned");
    }

    private int compareAppointments(Appointment left, Appointment right) {
        Date leftDate = left != null ? left.getAppointmentDate() : null;
        Date rightDate = right != null ? right.getAppointmentDate() : null;
        if (leftDate == null && rightDate == null) {
            return 0;
        }
        if (leftDate == null) {
            return -1;
        }
        if (rightDate == null) {
            return 1;
        }
        return leftDate.compareTo(rightDate);
    }

    private String formatProfileValue(String value, String fallback) {
        return value != null && !value.trim().isEmpty() ? value.trim() : fallback;
    }

    private String safe(String value, String fallback) {
        return value != null && !value.trim().isEmpty() ? value.trim() : fallback;
    }
}
