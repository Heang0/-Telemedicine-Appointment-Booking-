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
import java.util.List;

public class DoctorPatientsFragment extends Fragment {

    private RecyclerView recyclerPatients;
    private UserAdapter userAdapter;
    private List<User> patients;

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private com.google.firebase.firestore.ListenerRegistration appointmentsListenerRegistration;
    private com.google.firebase.firestore.ListenerRegistration patientsListenerRegistration;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_doctor_patients, container, false);

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
        userAdapter = new UserAdapter(patients, user -> {
            // Handle patient click - navigate to schedule appointment with this patient
            if (getActivity() != null) {
                // Create intent to schedule appointment with this patient
                android.content.Intent intent = new android.content.Intent(getActivity(), ScheduleAppointmentActivity.class);
                intent.putExtra("selected_patient_id", user.getUserId());
                intent.putExtra("selected_patient_name", user.getFullName());
                getActivity().startActivity(intent);
            }
        });
        recyclerPatients.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerPatients.setAdapter(userAdapter);
    }

    private void loadPatients() {
        String doctorId = mAuth.getCurrentUser().getUid();

        // Remove previous listeners if they exist
        if (appointmentsListenerRegistration != null) {
            appointmentsListenerRegistration.remove();
        }
        if (patientsListenerRegistration != null) {
            patientsListenerRegistration.remove();
        }

        // Get appointments for this doctor to find associated patients
        appointmentsListenerRegistration = db.collection("appointments")
                .whereEqualTo("doctorId", doctorId)
                .addSnapshotListener((querySnapshot, error) -> {
                    if (error != null) {
                        return;
                    }

                    if (querySnapshot != null) {
                        // Extract unique patient IDs from appointments
                        List<String> patientIds = new ArrayList<>();
                        for (QueryDocumentSnapshot document : querySnapshot) {
                            String patientId = document.getString("patientId");
                            if (patientId != null && !patientIds.contains(patientId)) {
                                patientIds.add(patientId);
                            }
                        }

                        // Fetch patient details
                        if (!patientIds.isEmpty()) {
                            patientsListenerRegistration = db.collection("users")
                                    .whereIn("userId", patientIds)
                                    .addSnapshotListener((patientsSnapshot, patientsError) -> {
                                        if (patientsError != null) {
                                            return;
                                        }

                                        if (patientsSnapshot != null) {
                                            patients.clear();
                                            for (QueryDocumentSnapshot patientDoc : patientsSnapshot) {
                                                User patient = patientDoc.toObject(User.class);
                                                patients.add(patient);
                                            }
                                            userAdapter.updateUsers(patients);
                                        }
                                    });
                        }
                    }
                });
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        // Clean up the listeners to prevent memory leaks
        if (appointmentsListenerRegistration != null) {
            appointmentsListenerRegistration.remove();
        }
        if (patientsListenerRegistration != null) {
            patientsListenerRegistration.remove();
        }
    }
}