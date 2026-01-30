package com.example.telemedicine;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

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

public class AppointmentsFragment extends Fragment implements AppointmentAdapter.OnAppointmentClickListener {

    private RecyclerView recyclerAppointments;
    private Button btnScheduleNew;
    private AppointmentAdapter adapter;
    private List<Appointment> appointments;

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private com.google.firebase.firestore.ListenerRegistration listenerRegistration;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_appointments, container, false);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        initializeViews(view);
        setupRecyclerView();
        loadAppointments();
        
        btnScheduleNew.setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), ScheduleAppointmentActivity.class);
            startActivity(intent);
        });

        return view;
    }

    private void initializeViews(View view) {
        recyclerAppointments = view.findViewById(R.id.recycler_appointments);
        btnScheduleNew = view.findViewById(R.id.btn_schedule_new);
    }

    private void setupRecyclerView() {
        appointments = new ArrayList<>();
        adapter = new AppointmentAdapter(appointments, this);
        recyclerAppointments.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerAppointments.setAdapter(adapter);
    }

    private void loadAppointments() {
        String userId = mAuth.getCurrentUser().getUid();

        // Remove previous listener if exists
        if (listenerRegistration != null) {
            listenerRegistration.remove();
        }

        listenerRegistration = db.collection("appointments")
                .whereEqualTo("patientId", userId)
                .addSnapshotListener((querySnapshot, error) -> {
                    if (error != null) {
                        android.util.Log.e("AppointmentsFragment", "Error loading appointments", error);
                        if (getContext() != null) {
                            android.widget.Toast.makeText(getContext(),
                                "Error loading appointments: " + error.getMessage(),
                                android.widget.Toast.LENGTH_LONG).show();
                        }
                        return;
                    }

                    if (querySnapshot != null) {
                        appointments.clear();
                        for (QueryDocumentSnapshot document : querySnapshot) {
                            Appointment appointment = document.toObject(Appointment.class);
                            appointment.setId(document.getId()); // Set the document ID
                            appointments.add(appointment);
                        }

                        // Sort appointments by date (closest first)
                        appointments.sort((a, b) -> {
                            if (a.getAppointmentDate() == null || b.getAppointmentDate() == null) {
                                return 0;
                            }
                            return a.getAppointmentDate().compareTo(b.getAppointmentDate());
                        });

                        adapter.updateAppointments(appointments);

                        // Debug: Show how many appointments were loaded
                        android.util.Log.d("AppointmentsFragment", "Loaded " + appointments.size() + " appointments");
                    }
                });
    }

    @Override
    public void onAppointmentClick(Appointment appointment) {
        // Handle appointment click - navigate to appointment details
        if (getActivity() != null) {
            // Create a bundle to pass the appointment data
            Bundle bundle = new Bundle();
            bundle.putString("appointment_id", appointment.getId());
            bundle.putString("patient_id", appointment.getPatientId());
            bundle.putString("doctor_id", appointment.getDoctorId());
            bundle.putString("patient_name", appointment.getPatientName());
            bundle.putString("doctor_name", appointment.getDoctorName());
            bundle.putString("appointment_status", appointment.getStatus());

            // Navigate to appointment details fragment
            AppointmentDetailsFragment detailsFragment = new AppointmentDetailsFragment();
            detailsFragment.setArguments(bundle);

            getActivity().getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.fragment_container, detailsFragment)
                    .addToBackStack(null)
                    .commit();
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        // Clean up the listener to prevent memory leaks
        if (listenerRegistration != null) {
            listenerRegistration.remove();
        }
    }
}