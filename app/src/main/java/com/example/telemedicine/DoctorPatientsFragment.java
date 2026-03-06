package com.example.telemedicine;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DoctorPatientsFragment extends Fragment {

    private RecyclerView recyclerPatients;
    private PatientAdapter patientAdapter;
    private List<User> patients;

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private com.google.firebase.firestore.ListenerRegistration appointmentsListenerRegistration;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_doctor_patients_ios, container, false);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        initializeViews(view);
        setupRecyclerView();
        loadPatients();

        return view;
    }

    private void initializeViews(View view) {
        recyclerPatients = view.findViewById(R.id.recycler_patients);
    }

    private void setupRecyclerView() {
        patients = new ArrayList<>();
        patientAdapter = new PatientAdapter(patients, patient -> {
            if (getActivity() != null) {
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
        });
        recyclerPatients.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerPatients.setAdapter(patientAdapter);
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
                            patientMap.put(appointment.getPatientId(), appointment.getPatientName());
                        }

                        // Now fetch the patient details
                        if (!patientMap.isEmpty()) {
                            List<String> patientIds = new ArrayList<>(patientMap.keySet());
                            db.collection("users")
                                    .whereIn("userId", patientIds.subList(0, Math.min(10, patientIds.size())))
                                    .get()
                                    .addOnSuccessListener(userSnapshot -> {
                                        patients.clear();
                                        for (QueryDocumentSnapshot userDoc : userSnapshot) {
                                            User patient = userDoc.toObject(User.class);
                                            patients.add(patient);
                                        }
                                        patientAdapter.updatePatients(patients);
                                    });
                        } else {
                            patients.clear();
                            patientAdapter.updatePatients(patients);
                        }
                    }
                });
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (appointmentsListenerRegistration != null) {
            appointmentsListenerRegistration.remove();
        }
    }
}
