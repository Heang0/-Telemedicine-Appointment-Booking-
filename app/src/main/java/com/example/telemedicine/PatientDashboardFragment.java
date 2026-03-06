package com.example.telemedicine;

import android.content.Intent;
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
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class PatientDashboardFragment extends Fragment {

    private TextView textUserName, textSeeAllAppointments;
    private RecyclerView recyclerUpcomingAppointments;
    private AppointmentAdapterIOS appointmentAdapter;
    private List<Appointment> upcomingAppointments;

    // Header stats
    private TextView textAppointmentsCount, textPrescriptionsCount, textMessagesCount;
    // Health stats
    private TextView textLastVisitValue, textNextAppointmentValue, textMedicationsValue, textAllergiesValue;
    // Action buttons
    private View chipBookAppointment, chipMessages, chipMedicalRecords, chipPrescriptions, chipLabResults;

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private com.google.firebase.firestore.ListenerRegistration appointmentsListenerRegistration;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_patient_dashboard_ios, container, false);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        initializeViews(view);
        loadUserProfile();
        setupRecyclerView();
        loadUpcomingAppointments();
        loadHealthStats();

        setClickListeners();

        return view;
    }

    private void initializeViews(View view) {
        textUserName = view.findViewById(R.id.text_user_name);
        textSeeAllAppointments = view.findViewById(R.id.text_see_all_appointments);
        recyclerUpcomingAppointments = view.findViewById(R.id.recycler_upcoming_appointments);

        // Header stats
        textAppointmentsCount = view.findViewById(R.id.text_appointments_count);
        textPrescriptionsCount = view.findViewById(R.id.text_prescriptions_count);
        textMessagesCount = view.findViewById(R.id.text_messages_count);

        // Health stats
        textLastVisitValue = view.findViewById(R.id.text_last_visit_value);
        textNextAppointmentValue = view.findViewById(R.id.text_next_appointment_value);
        textMedicationsValue = view.findViewById(R.id.text_medications_value);
        textAllergiesValue = view.findViewById(R.id.text_allergies_value);

        // Action buttons
        chipBookAppointment = view.findViewById(R.id.chip_book_appointment);
        chipMessages = view.findViewById(R.id.chip_messages);
        chipMedicalRecords = view.findViewById(R.id.chip_medical_records);
        chipPrescriptions = view.findViewById(R.id.chip_prescriptions);
        chipLabResults = view.findViewById(R.id.chip_lab_results);
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
                            textUserName.setText(user.getFullName());
                        }
                    }
                });
    }

    private void setupRecyclerView() {
        upcomingAppointments = new ArrayList<>();
        appointmentAdapter = new AppointmentAdapterIOS(upcomingAppointments, appointment -> {
            // Handle appointment click if needed
        });
        recyclerUpcomingAppointments.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerUpcomingAppointments.setAdapter(appointmentAdapter);
    }

    private void loadUpcomingAppointments() {
        if (mAuth.getCurrentUser() == null) {
            if (getContext() != null) {
                android.widget.Toast.makeText(getContext(), "User not authenticated", android.widget.Toast.LENGTH_SHORT).show();
            }
            return;
        }
        String userId = mAuth.getCurrentUser().getUid();

        // Remove previous listener if exists
        if (appointmentsListenerRegistration != null) {
            appointmentsListenerRegistration.remove();
        }

        appointmentsListenerRegistration = db.collection("appointments")
                .whereEqualTo("patientId", userId)
                .addSnapshotListener((querySnapshot, error) -> {
                    if (error != null) {
                        return;
                    }

                    if (querySnapshot != null) {
                        upcomingAppointments.clear();
                        for (QueryDocumentSnapshot document : querySnapshot) {
                            Appointment appointment = document.toObject(Appointment.class);
                            appointment.setId(document.getId());

                            // Only show upcoming appointments (not completed/cancelled)
                            if (!"completed".equalsIgnoreCase(appointment.getStatus()) &&
                                !"cancelled".equalsIgnoreCase(appointment.getStatus())) {
                                upcomingAppointments.add(appointment);
                            }
                        }

                        // Sort by date (closest first)
                        upcomingAppointments.sort((a, b) -> {
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

                        // Update header count
                        if (textAppointmentsCount != null) {
                            textAppointmentsCount.setText(String.valueOf(upcomingAppointments.size()));
                        }

                        // Limit to 3 upcoming appointments
                        if (upcomingAppointments.size() > 3) {
                            upcomingAppointments = upcomingAppointments.subList(0, 3);
                        }

                        appointmentAdapter.updateAppointments(upcomingAppointments);
                    }
                });
    }

    private void loadHealthStats() {
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
                            // Fallback: use createdAt as last visit date
                            String lastVisit = user.getCreatedAt() > 0 ?
                                    android.text.format.DateFormat.format("MMM d, yyyy", new java.util.Date(user.getCreatedAt())).toString() : "—";

                            // Next appointment: for now, show "—"; could be fetched from appointments later
                            String nextAppt = "—";

                            // Active medications count: parse from medications string
                            int medCount = 0;
                            if (user.getMedications() != null && !user.getMedications().trim().isEmpty()) {
                                String[] meds = user.getMedications().split(",");
                                for (String m : meds) {
                                    if (!m.trim().isEmpty()) medCount++;
                                }
                            }

                            String allergies = user.getAllergies() != null && !user.getAllergies().trim().isEmpty() ?
                                    user.getAllergies() : "None";

                            // Update health stats
                            if (textLastVisitValue != null) {
                                textLastVisitValue.setText(lastVisit);
                            }
                            if (textNextAppointmentValue != null) {
                                textNextAppointmentValue.setText(nextAppt);
                            }
                            if (textMedicationsValue != null) {
                                textMedicationsValue.setText(String.valueOf(medCount));
                            }
                            if (textAllergiesValue != null) {
                                textAllergiesValue.setText(allergies);
                            }
                            
                            // Update header prescriptions count
                            if (textPrescriptionsCount != null) {
                                textPrescriptionsCount.setText(String.valueOf(medCount));
                            }
                        }
                    }
                });
    }

    private void setClickListeners() {
        if (chipBookAppointment != null) {
            chipBookAppointment.setOnClickListener(v -> {
                Intent intent = new Intent(getActivity(), ScheduleAppointmentActivity.class);
                startActivity(intent);
            });
        }

        if (chipMessages != null) {
            chipMessages.setOnClickListener(v -> {
                if (getActivity() != null) {
                    getActivity().getSupportFragmentManager()
                            .beginTransaction()
                            .replace(R.id.fragment_container, new SecureMessagingHubFragment())
                            .addToBackStack(null)
                            .commit();
                }
            });
        }

        if (chipMedicalRecords != null) {
            chipMedicalRecords.setOnClickListener(v -> {
                if (getActivity() != null) {
                    getActivity().getSupportFragmentManager()
                            .beginTransaction()
                            .replace(R.id.fragment_container, new PatientEMRFragment())
                            .addToBackStack(null)
                            .commit();
                }
            });
        }

        if (chipPrescriptions != null) {
            chipPrescriptions.setOnClickListener(v -> {
                if (getActivity() != null) {
                    getActivity().getSupportFragmentManager()
                            .beginTransaction()
                            .replace(R.id.fragment_container, new PatientPrescriptionsFragment())
                            .addToBackStack(null)
                            .commit();
                }
            });
        }

        if (chipLabResults != null) {
            chipLabResults.setOnClickListener(v -> {
                if (getActivity() != null) {
                    getActivity().getSupportFragmentManager()
                            .beginTransaction()
                            .replace(R.id.fragment_container, new MedicalRecordsVaultFragment())
                            .addToBackStack(null)
                            .commit();
                }
            });
        }

        if (textSeeAllAppointments != null) {
            textSeeAllAppointments.setOnClickListener(v -> {
                if (getActivity() != null) {
                    getActivity().getSupportFragmentManager()
                            .beginTransaction()
                            .replace(R.id.fragment_container, new AppointmentsFragment())
                            .addToBackStack(null)
                            .commit();
                }
            });
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        // Clean up the listener to prevent memory leaks
        if (appointmentsListenerRegistration != null) {
            appointmentsListenerRegistration.remove();
        }
    }
}
