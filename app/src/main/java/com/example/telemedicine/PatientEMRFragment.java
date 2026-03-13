package com.example.telemedicine;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
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
        View view = inflater.inflate(R.layout.fragment_patient_emr_2026, container, false);

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
        setupToolbar(view);
        loadDynamicData();

        return view;
    }

    private void setupToolbar(View view) {
        if (getActivity() != null) {
            com.google.android.material.appbar.MaterialToolbar toolbar = view.findViewById(R.id.app_bar);
            if (toolbar != null) {
                toolbar.setNavigationOnClickListener(v -> {
                    // Go back to previous fragment
                    if (getActivity() != null) {
                        getActivity().getOnBackPressedDispatcher().onBackPressed();
                    }
                });
            }
        }
    }

    private void initializeViews(View view) {
        recyclerMedicalHistory = view.findViewById(R.id.recycler_medical_history);
        textPatientName = view.findViewById(R.id.text_patient_name);
        textPatientDob = view.findViewById(R.id.text_age_gender);
        textPatientGender = view.findViewById(R.id.text_age_gender);
        textPatientMrn = view.findViewById(R.id.text_patient_id);
        textPrimaryCare = view.findViewById(R.id.text_blood_type);
        textBloodType = view.findViewById(R.id.text_blood_type);
        
        // New 2026 layout views
        View editButton = view.findViewById(R.id.btn_edit_patient_info);
        if (editButton != null) {
            editButton.setOnClickListener(v -> showEditPatientInfo());
        }
        
        // Setup RecyclerView
        if (recyclerMedicalHistory != null) {
            recyclerMedicalHistory.setLayoutManager(new LinearLayoutManager(getContext()));
            recyclerMedicalHistory.setNestedScrollingEnabled(false);
        }
    }

    private void setupRecyclerView() {
        if (recyclerMedicalHistory == null || getContext() == null) {
            return;
        }
        recyclerMedicalHistory.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerMedicalHistory.setNestedScrollingEnabled(false);
        historyAdapter = new MedicalHistoryAdapter(historyItems);
        recyclerMedicalHistory.setAdapter(historyAdapter);
    }

    private void loadDynamicData() {
        if (getContext() == null || currentUserId == null || targetPatientId == null || targetPatientId.trim().isEmpty()) {
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
                                populateMedicalInfo(patient);
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
        if (textPatientMrn != null) {
            // Show short ID: P-12345 instead of full ID
            String shortId = "P-" + resolvedPatientId.substring(0, Math.min(5, resolvedPatientId.length()));
            textPatientMrn.setText(shortId);
        }
        
        // Update age/gender combined field (new 2026 layout)
        if (textPatientDob != null) {
            String dob = formatProfileValue(user.getDateOfBirth(), "");
            String gender = formatProfileValue(user.getGender(), "");
            String ageStr = calculateAge(dob);
            String ageGenderText = ageStr + " / " + (gender.isEmpty() ? "N/A" : gender);
            textPatientDob.setText(ageGenderText);
        }
        
        // Update blood type (new 2026 layout)
        if (textBloodType != null) {
            String bloodType = formatProfileValue(user.getBloodType(), "N/A");
            textBloodType.setText(bloodType);
        }
    }

    private void populateMedicalInfo(User user) {
        // This method populates the new 2026 layout specific fields
        View view = getView();
        if (view == null) return;

        // Height
        TextView textHeight = view.findViewById(R.id.text_height);
        if (textHeight != null) {
            String height = user.getHeight();
            textHeight.setText((height != null && !height.isEmpty()) ? height + " cm" : "N/A");
        }

        // Weight
        TextView textWeight = view.findViewById(R.id.text_weight);
        if (textWeight != null) {
            String weight = user.getWeight();
            textWeight.setText((weight != null && !weight.isEmpty()) ? weight + " kg" : "N/A");
        }

        // BMI (calculate from height and weight)
        TextView textBmi = view.findViewById(R.id.text_bmi);
        if (textBmi != null) {
            String height = user.getHeight();
            String weight = user.getWeight();
            if (height != null && !height.isEmpty() && weight != null && !weight.isEmpty()) {
                try {
                    double heightM = Double.parseDouble(height) / 100.0;
                    double weightKg = Double.parseDouble(weight);
                    double bmi = weightKg / (heightM * heightM);
                    textBmi.setText(String.format(Locale.getDefault(), "%.1f", bmi));
                } catch (NumberFormatException e) {
                    textBmi.setText("N/A");
                }
            } else {
                textBmi.setText("N/A");
            }
        }

        // Allergies
        TextView textAllergies = view.findViewById(R.id.text_allergies);
        if (textAllergies != null) {
            String allergies = user.getAllergies();
            textAllergies.setText((allergies != null && !allergies.isEmpty()) ? allergies : "None known");
        }

        // Medical Conditions
        TextView textConditions = view.findViewById(R.id.text_conditions);
        if (textConditions != null) {
            String conditions = user.getMedicalConditions();
            textConditions.setText((conditions != null && !conditions.isEmpty()) ? conditions : "None");
        }

        // Email
        TextView textEmail = view.findViewById(R.id.text_email);
        if (textEmail != null) {
            textEmail.setText(formatProfileValue(user.getEmail(), "Not provided"));
        }

        // Phone
        TextView textPhone = view.findViewById(R.id.text_phone);
        if (textPhone != null) {
            textPhone.setText(formatProfileValue(user.getPhoneNumber(), "Not provided"));
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
                        MedicalRecordsVault record = document.toObject(MedicalRecordsVault.class);
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

    private String calculateAge(String dob) {
        if (dob == null || dob.trim().isEmpty()) {
            return "N/A";
        }
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
            Date birthDate = sdf.parse(dob);
            if (birthDate == null) {
                return "N/A";
            }
            Calendar today = Calendar.getInstance();
            Calendar birth = Calendar.getInstance();
            birth.setTime(birthDate);
            
            int age = today.get(Calendar.YEAR) - birth.get(Calendar.YEAR);
            if (today.get(Calendar.DAY_OF_YEAR) < birth.get(Calendar.DAY_OF_YEAR)) {
                age--;
            }
            return String.valueOf(age);
        } catch (Exception e) {
            return "N/A";
        }
    }

    private void showEditPatientInfo() {
        // Only doctors can edit
        if (!"doctor".equalsIgnoreCase(currentUserRole)) {
            Toast.makeText(getContext(), "Only doctors can edit patient information", Toast.LENGTH_SHORT).show();
            return;
        }

        // Load current patient data and show edit dialog
        db.collection("users")
                .document(targetPatientId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    User patient = documentSnapshot.toObject(User.class);
                    if (patient == null) {
                        Toast.makeText(getContext(), "Patient not found", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    EditPatientInfoDialog editDialog = new EditPatientInfoDialog(getContext(), targetPatientId);
                    editDialog.setCurrentData(
                            formatProfileValue(patient.getDateOfBirth(), ""),
                            formatProfileValue(patient.getGender(), ""),
                            formatProfileValue(patient.getBloodType(), ""),
                            formatProfileValue(patient.getHeight(), ""),
                            formatProfileValue(patient.getWeight(), ""),
                            formatProfileValue(patient.getAllergies(), "None"),
                            formatProfileValue(patient.getMedicalConditions(), "None")
                    );
                    editDialog.show();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(getContext(), "Error loading patient data: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }
}
