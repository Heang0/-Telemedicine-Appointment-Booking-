package com.example.telemedicine;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

public class DoctorPatientsFragment extends Fragment {

    private static final String ARG_MODE = "mode";
    public static final String MODE_SCHEDULE = "schedule";
    public static final String MODE_MEDICAL_RECORDS = "medical_records";

    private RecyclerView recyclerPatients;
    private PatientAdapter patientAdapter;
    private List<User> patients;
    private List<User> allPatients;
    private android.widget.TextView textPatientsCount;
    private EditText editSearchPatients;
    private View fabAddPatient;
    private View btnScanQr;

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private com.google.firebase.firestore.ListenerRegistration appointmentsListenerRegistration;
    private String mode = MODE_SCHEDULE;

    public static DoctorPatientsFragment newInstance(String mode) {
        DoctorPatientsFragment fragment = new DoctorPatientsFragment();
        Bundle args = new Bundle();
        args.putString(ARG_MODE, mode);
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_doctor_patients_ios, container, false);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        if (getArguments() != null) {
            mode = getArguments().getString(ARG_MODE, MODE_SCHEDULE);
        }

        initializeViews(view);
        setupRecyclerView();
        loadPatients();

        return view;
    }

    private void initializeViews(View view) {
        recyclerPatients = view.findViewById(R.id.recycler_patients);
        textPatientsCount = view.findViewById(R.id.text_patients_count);
        editSearchPatients = view.findViewById(R.id.edit_search_patients);
        fabAddPatient = view.findViewById(R.id.fab_add_patient);
        btnScanQr = view.findViewById(R.id.btn_scan_qr);
    }

    private void setupRecyclerView() {
        patients = new ArrayList<>();
        allPatients = new ArrayList<>();
        patientAdapter = new PatientAdapter(patients, patient -> {
            if (getActivity() != null) {
                if (MODE_MEDICAL_RECORDS.equals(mode)) {
                    getActivity().getSupportFragmentManager()
                            .beginTransaction()
                            .replace(
                                    R.id.fragment_container,
                                    PatientEMRFragment.newInstance(patient.getUserId(), patient.getFullName())
                            )
                            .addToBackStack(null)
                            .commit();
                } else {
                    getActivity().getSupportFragmentManager()
                            .beginTransaction()
                            .replace(
                                    R.id.fragment_container,
                                    AppointmentSchedulerFragment.newInstance(
                                            UserRole.DOCTOR.getRoleName(),
                                            patient.getUserId(),
                                            patient.getFullName()
                                    )
                            )
                            .addToBackStack(null)
                            .commit();
                }
            }
        });
        recyclerPatients.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerPatients.setAdapter(patientAdapter);
        if (fabAddPatient != null) {
            fabAddPatient.setVisibility(View.GONE);
        }
        if (btnScanQr != null) {
            btnScanQr.setVisibility(View.GONE);
        }
        if (editSearchPatients != null) {
            editSearchPatients.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                }

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    applyPatientFilter(s != null ? s.toString() : "");
                }

                @Override
                public void afterTextChanged(Editable s) {
                }
            });
        }
    }

    private void loadPatients() {
        if (mAuth.getCurrentUser() == null) {
            return;
        }
        String doctorId = mAuth.getCurrentUser().getUid();

        // Remove previous listener if exists
        if (appointmentsListenerRegistration != null) {
            appointmentsListenerRegistration.remove();
        }

        // Get appointments for this doctor to find associated patients
        appointmentsListenerRegistration = db.collection("appointments")
                .whereEqualTo("doctorId", doctorId)
                .addSnapshotListener((querySnapshot, error) -> {
                    if (error != null) {
                        return;
                    }

                    if (querySnapshot != null) {
                        Map<String, String> patientMap = new HashMap<>();
                        for (QueryDocumentSnapshot document : querySnapshot) {
                            Appointment appointment = document.toObject(Appointment.class);
                            if (appointment.getPatientId() != null && !appointment.getPatientId().trim().isEmpty()) {
                                patientMap.put(appointment.getPatientId(), appointment.getPatientName());
                            }
                        }

                        if (!patientMap.isEmpty()) {
                            List<String> patientIds = new ArrayList<>(patientMap.keySet());
                            fetchPatients(patientIds, patientMap);
                        } else {
                            allPatients.clear();
                            patients.clear();
                            updatePatientsCount(0);
                            patientAdapter.updatePatients(patients);
                        }
                    }
                });
    }

    private void fetchPatients(List<String> patientIds, Map<String, String> patientNames) {
        Map<String, User> loadedPatients = new HashMap<>();
        AtomicInteger pendingRequests = new AtomicInteger(patientIds.size());

        for (String patientId : patientIds) {
            db.collection("users")
                    .document(patientId)
                    .get()
                    .addOnSuccessListener(documentSnapshot -> {
                        User patient = documentSnapshot.toObject(User.class);
                        if (patient == null) {
                            patient = new User();
                        }
                        if (patient.getUserId() == null || patient.getUserId().trim().isEmpty()) {
                            patient.setUserId(patientId);
                        }
                        if (patient.getFullName() == null || patient.getFullName().trim().isEmpty()) {
                            patient.setFullName(patientNames.get(patientId));
                        }
                        loadedPatients.put(patientId, patient);
                    })
                    .addOnCompleteListener(task -> {
                        if (pendingRequests.decrementAndGet() == 0) {
                            allPatients.clear();
                            allPatients.addAll(loadedPatients.values());
                            Collections.sort(allPatients, Comparator.comparing(
                                    patient -> patient.getFullName() != null ? patient.getFullName() : "",
                                    String.CASE_INSENSITIVE_ORDER
                            ));
                            applyPatientFilter(editSearchPatients != null ? editSearchPatients.getText().toString() : "");
                        }
                    });
        }
    }

    private void applyPatientFilter(String query) {
        String normalizedQuery = query != null ? query.trim().toLowerCase() : "";
        patients.clear();

        for (User patient : allPatients) {
            String name = patient.getFullName() != null ? patient.getFullName().toLowerCase() : "";
            String allergies = patient.getAllergies() != null ? patient.getAllergies().toLowerCase() : "";
            String conditions = patient.getMedicalConditions() != null ? patient.getMedicalConditions().toLowerCase() : "";
            String medications = patient.getMedications() != null ? patient.getMedications().toLowerCase() : "";

            if (normalizedQuery.isEmpty()
                    || name.contains(normalizedQuery)
                    || allergies.contains(normalizedQuery)
                    || conditions.contains(normalizedQuery)
                    || medications.contains(normalizedQuery)) {
                patients.add(patient);
            }
        }

        updatePatientsCount(patients.size());
        patientAdapter.updatePatients(new ArrayList<>(patients));
    }

    private void updatePatientsCount(int count) {
        if (textPatientsCount != null) {
            textPatientsCount.setText(String.valueOf(count));
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (appointmentsListenerRegistration != null) {
            appointmentsListenerRegistration.remove();
        }
    }
}
