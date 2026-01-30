package com.example.telemedicine;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class AppointmentDetailsFragment extends Fragment {

    private TextView textPatientName, textDoctorName, textAppointmentDate, textStatus, textReason;
    private Button btnCompleteAppointment, btnCancelAppointment, btnBack;

    private String appointmentId;
    private String patientId;
    private String doctorId;
    private String patientName;
    private String doctorName;
    private String appointmentStatus;
    private String reason;

    private FirebaseFirestore db;
    private FirebaseAuth mAuth;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_appointment_details, container, false);

        // Get arguments
        Bundle args = getArguments();
        if (args != null) {
            appointmentId = args.getString("appointment_id");
            patientId = args.getString("patient_id");
            doctorId = args.getString("doctor_id");
            patientName = args.getString("patient_name");
            doctorName = args.getString("doctor_name");
            appointmentStatus = args.getString("appointment_status", "scheduled");
        }

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        initializeViews(view);
        loadFullAppointmentDetails(); // Load the complete appointment details from Firestore
        setupButtonListeners();

        return view;
    }

    private void initializeViews(View view) {
        textPatientName = view.findViewById(R.id.text_patient_name);
        textDoctorName = view.findViewById(R.id.text_doctor_name);
        textAppointmentDate = view.findViewById(R.id.text_appointment_date);
        textStatus = view.findViewById(R.id.text_status);
        textReason = view.findViewById(R.id.text_reason);
        btnCompleteAppointment = view.findViewById(R.id.btn_complete_appointment);
        btnCancelAppointment = view.findViewById(R.id.btn_cancel_appointment);
        btnBack = view.findViewById(R.id.btn_back);
    }

    private void loadFullAppointmentDetails() {
        if (appointmentId == null) {
            Toast.makeText(getContext(), "Appointment ID is missing", Toast.LENGTH_SHORT).show();
            return;
        }

        db.collection("appointments")
                .document(appointmentId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        Appointment appointment = documentSnapshot.toObject(Appointment.class);

                        // Update the local variables with actual data
                        patientName = appointment.getPatientName();
                        doctorName = appointment.getDoctorName();
                        appointmentStatus = appointment.getStatus();
                        reason = appointment.getReason();

                        // Format the date properly
                        SimpleDateFormat dateFormat = new SimpleDateFormat("MMM dd, yyyy 'at' hh:mm a", Locale.getDefault());
                        String formattedDate = dateFormat.format(appointment.getAppointmentDate());

                        // Populate the UI with actual data
                        if (textPatientName != null && patientName != null) {
                            textPatientName.setText(patientName);
                        }

                        if (textDoctorName != null && doctorName != null) {
                            textDoctorName.setText(doctorName);
                        }

                        if (textAppointmentDate != null) {
                            textAppointmentDate.setText(formattedDate);
                        }

                        if (textStatus != null) {
                            textStatus.setText(capitalizeFirstLetter(appointmentStatus));

                            // Set status color based on status
                            if ("completed".equalsIgnoreCase(appointmentStatus)) {
                                textStatus.setTextColor(getResources().getColor(android.R.color.holo_green_dark));
                            } else if ("cancelled".equalsIgnoreCase(appointmentStatus)) {
                                textStatus.setTextColor(getResources().getColor(android.R.color.holo_red_dark));
                            } else {
                                textStatus.setTextColor(getResources().getColor(android.R.color.holo_blue_dark));
                            }
                        }

                        if (textReason != null && reason != null) {
                            textReason.setText(reason);
                        }

                        // Determine if current user is doctor or patient and adjust UI accordingly
                        String currentUserId = mAuth.getCurrentUser().getUid();
                        boolean isDoctor = currentUserId.equals(doctorId);

                        // Show/hide buttons based on user role
                        if (btnCompleteAppointment != null) {
                            btnCompleteAppointment.setVisibility(isDoctor ? View.VISIBLE : View.GONE);
                        }

                        if (btnCancelAppointment != null) {
                            // Patients can cancel, doctors can't cancel (they complete or reschedule)
                            btnCancelAppointment.setVisibility(!isDoctor ? View.VISIBLE : View.GONE);
                        }
                    } else {
                        Toast.makeText(getContext(), "Appointment not found", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(getContext(), "Failed to load appointment details: " + e.getMessage(),
                            Toast.LENGTH_SHORT).show();
                });
    }

    private void setupButtonListeners() {
        if (btnCompleteAppointment != null) {
            btnCompleteAppointment.setOnClickListener(v -> updateAppointmentStatus("completed"));
        }

        if (btnCancelAppointment != null) {
            btnCancelAppointment.setOnClickListener(v -> updateAppointmentStatus("cancelled"));
        }

        if (btnBack != null) {
            btnBack.setOnClickListener(v -> {
                if (getActivity() != null) {
                    getActivity().getSupportFragmentManager().popBackStack();
                }
            });
        }
    }

    private void updateAppointmentStatus(String newStatus) {
        if (appointmentId == null) {
            Toast.makeText(getContext(), "Appointment ID is missing", Toast.LENGTH_SHORT).show();
            return;
        }

        // Check if the current user has permission to update the status
        String currentUserId = mAuth.getCurrentUser().getUid();
        boolean isDoctor = currentUserId.equals(doctorId);
        boolean isPatient = currentUserId.equals(patientId);

        // Doctors can mark as completed, patients can cancel
        boolean canUpdate = (isDoctor && newStatus.equals("completed")) ||
                           (isPatient && newStatus.equals("cancelled"));

        if (!canUpdate) {
            Toast.makeText(getContext(), "You don't have permission to update this appointment",
                    Toast.LENGTH_SHORT).show();
            return;
        }

        db.collection("appointments")
                .document(appointmentId)
                .update("status", newStatus)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(getContext(), "Appointment status updated to " + newStatus,
                            Toast.LENGTH_SHORT).show();

                    // Update the local status
                    appointmentStatus = newStatus;

                    // Update UI
                    if (textStatus != null) {
                        textStatus.setText(capitalizeFirstLetter(newStatus));

                        // Update color based on new status
                        if ("completed".equalsIgnoreCase(newStatus)) {
                            textStatus.setTextColor(getResources().getColor(android.R.color.holo_green_dark));
                        } else if ("cancelled".equalsIgnoreCase(newStatus)) {
                            textStatus.setTextColor(getResources().getColor(android.R.color.holo_red_dark));
                        } else {
                            textStatus.setTextColor(getResources().getColor(android.R.color.holo_blue_dark));
                        }
                    }

                    // Hide the button after status change
                    if (newStatus.equals("completed") && btnCompleteAppointment != null) {
                        btnCompleteAppointment.setVisibility(View.GONE);
                    } else if (newStatus.equals("cancelled") && btnCancelAppointment != null) {
                        btnCancelAppointment.setVisibility(View.GONE);
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(getContext(), "Failed to update appointment status: " + e.getMessage(),
                            Toast.LENGTH_SHORT).show();
                });
    }

    private String capitalizeFirstLetter(String str) {
        if (str == null || str.isEmpty()) {
            return str;
        }
        return str.substring(0, 1).toUpperCase() + str.substring(1);
    }
}