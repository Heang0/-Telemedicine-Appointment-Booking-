package com.example.telemedicine;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.chip.Chip;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class AppointmentsFragment extends Fragment implements AppointmentAdapter.OnAppointmentClickListener {

    private RecyclerView recyclerAppointments;
    private View scheduleActionView;
    private Chip chipAll;
    private Chip chipUpcoming;
    private Chip chipCompleted;
    private Chip chipCancelled;
    private AppointmentAdapter adapter;
    private List<Appointment> appointments;
    private List<Appointment> allAppointments;
    private String activeFilter = "all";

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private com.google.firebase.firestore.ListenerRegistration listenerRegistration;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_appointments_ios, container, false);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        initializeViews(view);
        setupRecyclerView();
        setupFilters();
        loadAppointments();

        if (scheduleActionView != null) {
            scheduleActionView.setOnClickListener(v -> {
                Intent intent = new Intent(getActivity(), ScheduleAppointmentActivity.class);
                startActivity(intent);
            });
        }

        return view;
    }

    private void initializeViews(View view) {
        recyclerAppointments = view.findViewById(R.id.recycler_appointments);
        scheduleActionView = view.findViewById(R.id.fab_schedule);
        chipAll = view.findViewById(R.id.chip_all);
        chipUpcoming = view.findViewById(R.id.chip_upcoming);
        chipCompleted = view.findViewById(R.id.chip_completed);
        chipCancelled = view.findViewById(R.id.chip_cancelled);
        if (scheduleActionView == null) {
            scheduleActionView = view.findViewById(R.id.btn_schedule_new);
        }
    }

    private void setupRecyclerView() {
        appointments = new ArrayList<>();
        allAppointments = new ArrayList<>();
        adapter = new AppointmentAdapter(appointments, this);
        recyclerAppointments.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerAppointments.setAdapter(adapter);
    }

    private void setupFilters() {
        if (chipAll != null) {
            chipAll.setOnClickListener(v -> setActiveFilter("all"));
        }
        if (chipUpcoming != null) {
            chipUpcoming.setOnClickListener(v -> setActiveFilter("upcoming"));
        }
        if (chipCompleted != null) {
            chipCompleted.setOnClickListener(v -> setActiveFilter("completed"));
        }
        if (chipCancelled != null) {
            chipCancelled.setOnClickListener(v -> setActiveFilter("cancelled"));
        }
        updateFilterState();
    }

    private void setActiveFilter(String filter) {
        activeFilter = filter;
        updateFilterState();
        applyFilter();
    }

    private void updateFilterState() {
        if (chipAll != null) {
            chipAll.setChecked("all".equals(activeFilter));
        }
        if (chipUpcoming != null) {
            chipUpcoming.setChecked("upcoming".equals(activeFilter));
        }
        if (chipCompleted != null) {
            chipCompleted.setChecked("completed".equals(activeFilter));
        }
        if (chipCancelled != null) {
            chipCancelled.setChecked("cancelled".equals(activeFilter));
        }
    }

    private void loadAppointments() {
        if (mAuth.getCurrentUser() == null) {
            if (getContext() != null) {
                android.widget.Toast.makeText(getContext(), "User not authenticated", android.widget.Toast.LENGTH_SHORT).show();
            }
            return;
        }
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
                        allAppointments.clear();
                        for (QueryDocumentSnapshot document : querySnapshot) {
                            Appointment appointment = document.toObject(Appointment.class);
                            appointment.setId(document.getId()); // Set the document ID
                            allAppointments.add(appointment);
                        }

                        // Sort appointments by date (closest first)
                        allAppointments.sort((a, b) -> {
                            if (a.getAppointmentDate() == null && b.getAppointmentDate() == null) {
                                return 0;
                            }
                            if (a.getAppointmentDate() == null) {
                                return 1;
                            }
                            if (b.getAppointmentDate() == null) {
                                return -1;
                            }
                            return a.getAppointmentDate().compareTo(b.getAppointmentDate());
                        });

                        applyFilter();

                        // Debug: Show how many appointments were loaded
                        android.util.Log.d("AppointmentsFragment", "Loaded " + allAppointments.size() + " appointments");
                    }
                });
    }

    private void applyFilter() {
        appointments.clear();
        Date now = new Date();

        for (Appointment appointment : allAppointments) {
            String status = appointment.getStatus() != null ? appointment.getStatus().toLowerCase() : "";
            boolean include = false;

            switch (activeFilter) {
                case "upcoming":
                    include = !"completed".equals(status)
                            && !"cancelled".equals(status)
                            && appointment.getAppointmentDate() != null
                            && !appointment.getAppointmentDate().before(now);
                    break;
                case "completed":
                    include = "completed".equals(status);
                    break;
                case "cancelled":
                    include = "cancelled".equals(status);
                    break;
                default:
                    include = true;
                    break;
            }

            if (include) {
                appointments.add(appointment);
            }
        }

        adapter.updateAppointments(new ArrayList<>(appointments));
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
