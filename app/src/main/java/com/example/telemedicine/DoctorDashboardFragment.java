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
    private AppointmentAdapterIOS appointmentAdapter;
    private List<Appointment> appointments;

    // Stats views - Header
    private TextView textDoctorName, textAppointmentsCount, textPatientsCount, textMessagesCount;
    // Stats views - Summary
    private TextView textTodayPatientsValue, textPendingPrescriptionsValue, textUnreadMessagesValue, textAvailabilityValue;
    // Action buttons
    private View chipNewAppointment, chipPatients, chipMessages, chipPrescriptions, chipLabResults;
    private TextView textSeeAllAppointments;

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private com.google.firebase.firestore.ListenerRegistration appointmentsListenerRegistration;

    private DashboardViewModel viewModel;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_doctor_dashboard_ios, container, false);

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
        // RecyclerView
        recyclerAppointments = view.findViewById(R.id.recycler_appointments);
        
        // Header stats
        textDoctorName = view.findViewById(R.id.text_doctor_name);
        textAppointmentsCount = view.findViewById(R.id.text_appointments_count);
        textPatientsCount = view.findViewById(R.id.text_patients_count);
        textMessagesCount = view.findViewById(R.id.text_messages_count);
        
        // Summary stats
        textTodayPatientsValue = view.findViewById(R.id.text_today_patients_value);
        textPendingPrescriptionsValue = view.findViewById(R.id.text_pending_prescriptions_value);
        textUnreadMessagesValue = view.findViewById(R.id.text_unread_messages_value);
        textAvailabilityValue = view.findViewById(R.id.text_availability_value);
        
        // Action buttons
        chipNewAppointment = view.findViewById(R.id.chip_new_appointment);
        chipPatients = view.findViewById(R.id.chip_patients);
        chipMessages = view.findViewById(R.id.chip_messages);
        chipPrescriptions = view.findViewById(R.id.chip_prescriptions);
        chipLabResults = view.findViewById(R.id.chip_lab_results);
        
        // See all
        textSeeAllAppointments = view.findViewById(R.id.text_see_all_appointments);
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
        appointmentAdapter = new AppointmentAdapterIOS(appointments, appointment -> {
            if (getActivity() != null) {
                Bundle bundle = new Bundle();
                bundle.putString("appointment_id", appointment.getId());
                bundle.putString("patient_id", appointment.getPatientId());
                bundle.putString("doctor_id", appointment.getDoctorId());
                bundle.putString("patient_name", appointment.getPatientName());
                bundle.putString("doctor_name", appointment.getDoctorName());
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
            // Update header appointments count
            if (textAppointmentsCount != null && list != null) {
                textAppointmentsCount.setText(String.valueOf(list.size()));
            }
        });

        viewModel.getUnreadMessages().observe(getViewLifecycleOwner(), count -> {
            if (textUnreadMessagesValue != null) {
                textUnreadMessagesValue.setText(String.valueOf(count));
            }
            if (textMessagesCount != null) {
                textMessagesCount.setText(String.valueOf(count));
            }
        });

        viewModel.getPrescriptionsDue().observe(getViewLifecycleOwner(), count -> {
            if (textPendingPrescriptionsValue != null) {
                textPendingPrescriptionsValue.setText(String.valueOf(count));
            }
        });

        // Today's patients: count appointments for today (client-side filter to avoid composite index)
        if (mAuth.getCurrentUser() == null) {
            if (getContext() != null) {
                android.widget.Toast.makeText(getContext(), "User not authenticated", android.widget.Toast.LENGTH_SHORT).show();
            }
            return;
        }
        String doctorId = mAuth.getCurrentUser().getUid();
        long todayStart = System.currentTimeMillis() - (System.currentTimeMillis() % (24 * 60 * 60 * 1000));
        long todayEnd = todayStart + (24 * 60 * 60 * 1000);
        db.collection("appointments")
                .whereEqualTo("doctorId", doctorId)
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    int count = 0;
                    for (QueryDocumentSnapshot document : querySnapshot) {
                        Appointment appointment = document.toObject(Appointment.class);
                        java.util.Date apptDate = appointment.getAppointmentDate();
                        if (apptDate != null) {
                            long time = apptDate.getTime();
                            if (time >= todayStart && time < todayEnd) {
                                count++;
                            }
                        }
                    }
                    if (textTodayPatientsValue != null) {
                        textTodayPatientsValue.setText(String.valueOf(count));
                    }
                    // Update header patients count (total unique patients)
                    if (textPatientsCount != null) {
                        textPatientsCount.setText(String.valueOf(count + 150)); // Mock total
                    }
                })
                .addOnFailureListener(e -> {
                    if (textTodayPatientsValue != null) {
                        textTodayPatientsValue.setText("0");
                    }
                });

        // Availability: hardcode for now (could be from user profile later)
        if (textAvailabilityValue != null) {
            textAvailabilityValue.setText("Online");
        }
    }

    private void setClickListeners() {
        if (chipNewAppointment != null) {
            chipNewAppointment.setOnClickListener(v -> {
                if (getActivity() != null) {
                    getActivity().getSupportFragmentManager()
                            .beginTransaction()
                            .replace(R.id.fragment_container,
                                    AppointmentSchedulerFragment.newInstance(UserRole.DOCTOR.getRoleName()))
                            .addToBackStack(null)
                            .commit();
                }
            });
        }

        if (chipPatients != null) {
            chipPatients.setOnClickListener(v -> {
                if (getActivity() != null) {
                    getActivity().getSupportFragmentManager()
                            .beginTransaction()
                            .replace(R.id.fragment_container, new DoctorPatientsFragment())
                            .addToBackStack(null)
                            .commit();
                }
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

        if (chipPrescriptions != null) {
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
        if (appointmentsListenerRegistration != null) {
            appointmentsListenerRegistration.remove();
        }
    }
}
