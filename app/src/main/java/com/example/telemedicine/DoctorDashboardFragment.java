package com.example.telemedicine;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class DoctorDashboardFragment extends Fragment {

    private RecyclerView recyclerAppointments;
    private AppointmentAdapter appointmentAdapter;
    private List<Appointment> appointments;

    // New stats views
    private TextView textDoctorName;
    private TextView textTodayPatientsValue, textPendingPrescriptionsValue, textUnreadMessagesValue, textAvailabilityValue;
    // Chips
    private View chipNewConsult, chipMessages, chipPrescriptions;

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private com.google.firebase.firestore.ListenerRegistration appointmentsListenerRegistration;

    private DashboardViewModel viewModel;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_doctor_dashboard_modern, container, false);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        viewModel = new ViewModelProvider(this).get(DashboardViewModel.class);

        initializeViews(view);
        loadUserProfile();
        setupRecyclerView();
        loadAppointments();
        loadStats();

        setClickListeners();

        return view;
    }

    private void initializeViews(View view) {
        recyclerAppointments = view.findViewById(R.id.recycler_appointments);
        textDoctorName = view.findViewById(R.id.text_doctor_name);
        textTodayPatientsValue = view.findViewById(R.id.text_today_patients_value);
        textPendingPrescriptionsValue = view.findViewById(R.id.text_pending_prescriptions_value);
        textUnreadMessagesValue = view.findViewById(R.id.text_unread_messages_value);
        textAvailabilityValue = view.findViewById(R.id.text_availability_value);

        chipNewConsult = view.findViewById(R.id.chip_new_consult);
        chipMessages = view.findViewById(R.id.chip_messages);
        chipPrescriptions = view.findViewById(R.id.chip_prescriptions);
    }

    private void loadUserProfile() {
        if (mAuth.getCurrentUser() == null) {
            if (getContext() != null) {
                android.widget.Toast.makeText(getContext(), "User not authenticated", android.widget.Toast.LENGTH_SHORT).show();
            }
            return;
        }
        String userId = mAuth.getCurrentUser().getUid();

        db.collection("users")
                .document(userId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        User user = documentSnapshot.toObject(User.class);
                        if (user != null) {
                            textDoctorName.setText(user.getFullName());
                        }
                    }
                });
    }

    private void setupRecyclerView() {
        appointments = new ArrayList<>();
        appointmentAdapter = new AppointmentAdapter(appointments, appointment -> {
            if (getActivity() != null) {
                Bundle bundle = new Bundle();
                bundle.putString("appointment_id", appointment.getId());
                bundle.putString("patient_id", appointment.getPatientId());
                bundle.putString("patient_name", appointment.getPatientName());
                bundle.putString("appointment_status", appointment.getStatus());

                AppointmentDetailsFragment detailsFragment = new AppointmentDetailsFragment();
                detailsFragment.setArguments(bundle);

                getActivity().getSupportFragmentManager()
                        .beginTransaction()
                        .replace(R.id.fragment_container, detailsFragment)
                        .addToBackStack(null)
                        .commit();
            }
        });
        recyclerAppointments.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerAppointments.setAdapter(appointmentAdapter);
    }

    private void loadAppointments() {
        if (mAuth.getCurrentUser() == null) {
            if (getContext() != null) {
                android.widget.Toast.makeText(getContext(), "User not authenticated", android.widget.Toast.LENGTH_SHORT).show();
            }
            return;
        }
        String doctorId = mAuth.getCurrentUser().getUid();

        if (appointmentsListenerRegistration != null) {
            appointmentsListenerRegistration.remove();
        }

        appointmentsListenerRegistration = db.collection("appointments")
                .whereEqualTo("doctorId", doctorId)
                .addSnapshotListener((querySnapshot, error) -> {
                    if (error != null) {
                        android.util.Log.e("DoctorDashboard", "Error loading appointments", error);
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
                            appointment.setId(document.getId());
                            appointments.add(appointment);
                        }

                        appointments.sort((a, b) -> {
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

                        appointmentAdapter.updateAppointments(appointments);
                    }
                });
    }

    private void loadStats() {
        viewModel.loadDoctorStats();

        viewModel.getUpcomingAppointments().observe(getViewLifecycleOwner(), list -> {
            // Not used here — appointments are loaded directly
        });

        viewModel.getUnreadMessages().observe(getViewLifecycleOwner(), count -> {
            textUnreadMessagesValue.setText(String.valueOf(count));
        });

        viewModel.getPrescriptionsDue().observe(getViewLifecycleOwner(), count -> {
            textPendingPrescriptionsValue.setText(String.valueOf(count));
        });

        // Today's patients: count appointments for today (simplified)
        if (mAuth.getCurrentUser() == null) {
            if (getContext() != null) {
                android.widget.Toast.makeText(getContext(), "User not authenticated", android.widget.Toast.LENGTH_SHORT).show();
            }
            return;
        }
        String doctorId = mAuth.getCurrentUser().getUid();
        long todayStart = System.currentTimeMillis() - (System.currentTimeMillis() % (24 * 60 * 60 * 1000));
        db.collection("appointments")
                .whereEqualTo("doctorId", doctorId)
                .whereGreaterThanOrEqualTo("appointmentDate", new java.util.Date(todayStart))
                .whereLessThan("appointmentDate", new java.util.Date(todayStart + 24 * 60 * 60 * 1000))
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    textTodayPatientsValue.setText(String.valueOf(querySnapshot.size()));
                })
                .addOnFailureListener(e -> {
                    textTodayPatientsValue.setText("0");
                });

        // Availability: hardcode for now (could be from user profile later)
        textAvailabilityValue.setText("Online");
    }

    private void setClickListeners() {
        chipNewConsult.setOnClickListener(v -> {
            // Launch video consult or appointment creation
            if (getActivity() != null) {
                getActivity().getSupportFragmentManager()
                        .beginTransaction()
                        .replace(R.id.fragment_container, new AppointmentSchedulerFragment())
                        .addToBackStack(null)
                        .commit();
            }
        });

        chipMessages.setOnClickListener(v -> {
            if (getActivity() != null) {
                getActivity().getSupportFragmentManager()
                        .beginTransaction()
                        .replace(R.id.fragment_container, new SecureMessagingHubFragment())
                        .addToBackStack(null)
                        .commit();
            }
        });

        chipPrescriptions.setOnClickListener(v -> {
            if (getActivity() != null) {
                getActivity().getSupportFragmentManager()
                        .beginTransaction()
                        .replace(R.id.fragment_container, new PrescriptionManagerFragment())
                        .addToBackStack(null)
                        .commit();
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
