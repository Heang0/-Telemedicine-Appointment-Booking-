package com.example.telemedicine;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class AdminDashboardFragment extends Fragment {

    private TextInputEditText editDoctorName, editDoctorEmail, editDoctorSpecialization, editDoctorLicense, editDoctorPassword;
    private Button btnAddDoctor, btnManageUsers, btnLogout;
    private TextView textTotalPatients, textTotalDoctors, textTotalAppointments, textPendingVerifications;

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_admin_dashboard_updated, container, false);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        initializeViews(view);
        setClickListeners();
        loadStatistics();

        return view;
    }

    private void initializeViews(View view) {
        editDoctorName = view.findViewById(R.id.edit_doctor_name);
        editDoctorEmail = view.findViewById(R.id.edit_doctor_email);
        editDoctorSpecialization = view.findViewById(R.id.edit_doctor_specialization);
        editDoctorLicense = view.findViewById(R.id.edit_doctor_license);
        editDoctorPassword = view.findViewById(R.id.edit_doctor_password);

        btnAddDoctor = view.findViewById(R.id.btn_add_doctor);
        btnManageUsers = view.findViewById(R.id.btn_manage_users);
        btnLogout = view.findViewById(R.id.btn_logout);

        textTotalPatients = view.findViewById(R.id.text_total_patients);
        textTotalDoctors = view.findViewById(R.id.text_total_doctors);
        textTotalAppointments = view.findViewById(R.id.text_total_appointments);
        textPendingVerifications = view.findViewById(R.id.text_pending_verifications);
    }

    private void setClickListeners() {
        btnAddDoctor.setOnClickListener(v -> createDoctorAccount());
        btnManageUsers.setOnClickListener(v -> {
            // Navigate to user management activity
            if (getContext() != null) {
                Intent intent = new Intent(getContext(), UserManagementActivity.class);
                getContext().startActivity(intent);
            }
        });
        btnLogout.setOnClickListener(v -> logoutUser());
    }

    private void logoutUser() {
        mAuth.signOut();
        if (getActivity() != null) {
            Intent intent = new Intent(getActivity(), LoginActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            getActivity().finish();
        }
    }

    private void createDoctorAccount() {
        String name = editDoctorName.getText().toString().trim();
        String email = editDoctorEmail.getText().toString().trim();
        String specialization = editDoctorSpecialization.getText().toString().trim();
        String license = editDoctorLicense.getText().toString().trim();
        String password = editDoctorPassword.getText().toString().trim();

        // Validate inputs
        if (TextUtils.isEmpty(name)) {
            editDoctorName.setError("Doctor name is required");
            editDoctorName.requestFocus();
            return;
        }

        if (TextUtils.isEmpty(email)) {
            editDoctorEmail.setError("Email is required");
            editDoctorEmail.requestFocus();
            return;
        }

        if (TextUtils.isEmpty(specialization)) {
            editDoctorSpecialization.setError("Specialization is required");
            editDoctorSpecialization.requestFocus();
            return;
        }

        if (TextUtils.isEmpty(license)) {
            editDoctorLicense.setError("License number is required");
            editDoctorLicense.requestFocus();
            return;
        }

        if (TextUtils.isEmpty(password)) {
            editDoctorPassword.setError("Password is required");
            editDoctorPassword.requestFocus();
            return;
        }

        if (password.length() < 6) {
            editDoctorPassword.setError("Password must be at least 6 characters");
            editDoctorPassword.requestFocus();
            return;
        }

        // Create the doctor account in Firebase Auth with admin-set password
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        // Save doctor details to Firestore
                        String userId = task.getResult().getUser().getUid();
                        saveDoctorToFirestore(userId, name, email, specialization, license);

                        Toast.makeText(getContext(),
                            "Doctor account created successfully!\n" +
                            "Email: " + email + "\n" +
                            "Password: " + password + "\n" +
                            "(Share these credentials with the doctor)",
                            Toast.LENGTH_LONG).show();

                        // Clear the input fields
                        clearDoctorForm();
                    } else {
                        Toast.makeText(getContext(),
                            "Failed to create doctor account: " + task.getException().getMessage(),
                            Toast.LENGTH_LONG).show();
                    }
                });
    }

    private String generateTempPassword() {
        // Generate a temporary password (in a real app, you might want more secure generation)
        return "DocPass123!";
    }

    private void saveDoctorToFirestore(String userId, String name, String email, String specialization, String license) {
        // Create a user object for the doctor
        User doctor = new User(userId, name, email, UserRole.DOCTOR.getRoleName(), System.currentTimeMillis());
        doctor.setSpecialization(specialization);
        doctor.setLicenseNumber(license);
        doctor.setVerified(true); // Admin-created doctors are automatically verified

        // Save to Firestore
        db.collection("users")
                .document(userId)
                .set(doctor)
                .addOnSuccessListener(aVoid -> {
                    // Successfully saved to Firestore
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(getContext(),
                        "Failed to save doctor details: " + e.getMessage(),
                        Toast.LENGTH_LONG).show();
                });
    }

    private void clearDoctorForm() {
        editDoctorName.setText("");
        editDoctorEmail.setText("");
        editDoctorSpecialization.setText("");
        editDoctorLicense.setText("");
        editDoctorPassword.setText("");
    }

    private void loadStatistics() {
        // Count total patients
        db.collection("users")
                .whereEqualTo("role", UserRole.PATIENT.getRoleName())
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    long patientCount = queryDocumentSnapshots.size();
                    textTotalPatients.setText("Patients: " + patientCount);
                });

        // Count total doctors
        db.collection("users")
                .whereEqualTo("role", UserRole.DOCTOR.getRoleName())
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    long doctorCount = queryDocumentSnapshots.size();
                    textTotalDoctors.setText("Doctors: " + doctorCount);
                });

        // Count total appointments
        db.collection("appointments")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    long appointmentCount = queryDocumentSnapshots.size();
                    textTotalAppointments.setText("Appointments: " + appointmentCount);
                });

        // Count pending verifications (unverified doctors)
        db.collection("users")
                .whereEqualTo("role", UserRole.DOCTOR.getRoleName())
                .whereEqualTo("isVerified", false)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    long pendingCount = queryDocumentSnapshots.size();
                    textPendingVerifications.setText("Pending: " + pendingCount);
                });
    }
}