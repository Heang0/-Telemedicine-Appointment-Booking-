package com.example.telemedicine;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.example.telemedicine.ui.ComplianceAuditLogActivity;
import com.example.telemedicine.ui.PlatformAnalyticsActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class AdminDashboardFragment extends Fragment {

    // Header Views
    private TextView textAdminName, textCurrentDate;
    private ImageButton btnNotifications;

    // Stat Views
    private TextView statTotalUsers, statActiveDoctors, statAppointmentsToday, statPendingVerifications;

    // Quick Action Views
    private LinearLayout actionUserManagement, actionAuditLogs, actionAnalytics, actionPartnerPortal;

    // Doctor Form Views
    private TextInputEditText editDoctorName, editDoctorEmail, editDoctorSpecialization, editDoctorLicense, editDoctorPassword;
    private MaterialButton btnAddDoctor, btnLogout;

    // Recent Activity
    private RecyclerView recyclerRecentActivity;

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private RecentActivityAdapter recentActivityAdapter;
    private List<RecentActivityItem> activityItems;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_admin_dashboard_ios, container, false);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        initializeViews(view);
        setClickListeners();
        updateDate();
        loadStatistics();
        setupRecentActivity();
        loadRecentActivity();

        return view;
    }

    private void initializeViews(View view) {
        // Header
        textAdminName = view.findViewById(R.id.text_admin_name);
        textCurrentDate = view.findViewById(R.id.text_current_date);
        btnNotifications = view.findViewById(R.id.btn_notifications);

        // Stats
        statTotalUsers = view.findViewById(R.id.stat_total_users);
        statActiveDoctors = view.findViewById(R.id.stat_active_doctors);
        statAppointmentsToday = view.findViewById(R.id.stat_appointments_today);
        statPendingVerifications = view.findViewById(R.id.stat_pending_verifications);

        // Quick Actions
        actionUserManagement = view.findViewById(R.id.action_user_management);
        actionAuditLogs = view.findViewById(R.id.action_audit_logs);
        actionAnalytics = view.findViewById(R.id.action_analytics);
        actionPartnerPortal = view.findViewById(R.id.action_partner_portal);

        // Doctor Form
        editDoctorName = view.findViewById(R.id.edit_doctor_name);
        editDoctorEmail = view.findViewById(R.id.edit_doctor_email);
        editDoctorSpecialization = view.findViewById(R.id.edit_doctor_specialization);
        editDoctorLicense = view.findViewById(R.id.edit_doctor_license);
        editDoctorPassword = view.findViewById(R.id.edit_doctor_password);

        btnAddDoctor = view.findViewById(R.id.btn_add_doctor);
        btnLogout = view.findViewById(R.id.btn_logout);

        // Recent Activity
        recyclerRecentActivity = view.findViewById(R.id.recycler_recent_activity);
    }

    private void setClickListeners() {
        // Quick Actions
        actionUserManagement.setOnClickListener(v -> navigateToUserManagement());
        actionAuditLogs.setOnClickListener(v -> openAuditLogs());
        actionAnalytics.setOnClickListener(v -> openAnalytics());
        actionPartnerPortal.setOnClickListener(v -> openPartnerPortal());

        // Doctor Form
        btnAddDoctor.setOnClickListener(v -> createDoctorAccount());
        btnLogout.setOnClickListener(v -> logoutUser());
        btnNotifications.setOnClickListener(v -> showNotifications());
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

    private void navigateToUserManagement() {
        if (getContext() != null) {
            Intent intent = new Intent(getContext(), UserManagementActivity.class);
            getContext().startActivity(intent);
        }
    }

    private void openAuditLogs() {
        if (getContext() != null) {
            Intent intent = new Intent(getContext(), ComplianceAuditLogActivity.class);
            getContext().startActivity(intent);
        }
    }

    private void openAnalytics() {
        if (getContext() != null) {
            Intent intent = new Intent(getContext(), PlatformAnalyticsActivity.class);
            getContext().startActivity(intent);
        }
    }

    private void openPartnerPortal() {
        Toast.makeText(getContext(), "Partner Portal - Coming Soon", Toast.LENGTH_SHORT).show();
    }

    private void showNotifications() {
        Toast.makeText(getContext(), "No new notifications", Toast.LENGTH_SHORT).show();
    }

    private void updateDate() {
        SimpleDateFormat sdf = new SimpleDateFormat("EEEE, MMMM d, yyyy", Locale.getDefault());
        String currentDate = sdf.format(Calendar.getInstance().getTime());
        textCurrentDate.setText(currentDate);
    }

    private void setupRecentActivity() {
        activityItems = new ArrayList<>();
        recentActivityAdapter = new RecentActivityAdapter(activityItems);
        recyclerRecentActivity.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerRecentActivity.setAdapter(recentActivityAdapter);
    }

    private void loadRecentActivity() {
        // Load recent activities from Firestore
        db.collection("auditLogs")
                .orderBy("timestamp", com.google.firebase.firestore.Query.Direction.DESCENDING)
                .limit(5)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    activityItems.clear();
                    for (var doc : queryDocumentSnapshots) {
                        String action = doc.getString("action") != null ? doc.getString("action") : "Unknown Action";
                        String user = doc.getString("userName") != null ? doc.getString("userName") : "Unknown User";
                        long timestamp = doc.getLong("timestamp") != null ? doc.getLong("timestamp") : System.currentTimeMillis();
                        activityItems.add(new RecentActivityItem(action, user, timestamp));
                    }
                    if (activityItems.isEmpty()) {
                        // Add sample data for demo
                        activityItems.add(new RecentActivityItem("New user registered", "System", System.currentTimeMillis()));
                        activityItems.add(new RecentActivityItem("Doctor verified", "Admin", System.currentTimeMillis() - 3600000));
                        activityItems.add(new RecentActivityItem("Appointment scheduled", "Patient", System.currentTimeMillis() - 7200000));
                    }
                    recentActivityAdapter.notifyDataSetChanged();
                })
                .addOnFailureListener(e -> {
                    // Add sample data if Firestore fails
                    activityItems.clear();
                    activityItems.add(new RecentActivityItem("New user registered", "System", System.currentTimeMillis()));
                    activityItems.add(new RecentActivityItem("Doctor verified", "Admin", System.currentTimeMillis() - 3600000));
                    activityItems.add(new RecentActivityItem("Appointment scheduled", "Patient", System.currentTimeMillis() - 7200000));
                    recentActivityAdapter.notifyDataSetChanged();
                });
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
        // Count total users (patients + doctors)
        db.collection("users")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    long totalCount = queryDocumentSnapshots.size();
                    statTotalUsers.setText(String.valueOf(totalCount));
                });

        // Count active doctors
        db.collection("users")
                .whereEqualTo("role", UserRole.DOCTOR.getRoleName())
                .whereEqualTo("isVerified", true)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    long doctorCount = queryDocumentSnapshots.size();
                    statActiveDoctors.setText(String.valueOf(doctorCount));
                });

        // Count today's appointments
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        long startOfDay = calendar.getTimeInMillis();
        
        calendar.set(Calendar.HOUR_OF_DAY, 23);
        calendar.set(Calendar.MINUTE, 59);
        calendar.set(Calendar.SECOND, 59);
        long endOfDay = calendar.getTimeInMillis();

        db.collection("appointments")
                .whereGreaterThanOrEqualTo("timestamp", startOfDay)
                .whereLessThanOrEqualTo("timestamp", endOfDay)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    long appointmentCount = queryDocumentSnapshots.size();
                    statAppointmentsToday.setText(String.valueOf(appointmentCount));
                });

        // Count pending verifications (unverified doctors)
        db.collection("users")
                .whereEqualTo("role", UserRole.DOCTOR.getRoleName())
                .whereEqualTo("isVerified", false)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    long pendingCount = queryDocumentSnapshots.size();
                    statPendingVerifications.setText(String.valueOf(pendingCount));
                });
    }

    // Recent Activity Item Model
    public static class RecentActivityItem {
        private String action;
        private String user;
        private long timestamp;

        public RecentActivityItem(String action, String user, long timestamp) {
            this.action = action;
            this.user = user;
            this.timestamp = timestamp;
        }

        public String getAction() { return action; }
        public String getUser() { return user; }
        public long getTimestamp() { return timestamp; }
    }

    // Recent Activity Adapter
    public static class RecentActivityAdapter extends RecyclerView.Adapter<RecentActivityAdapter.ViewHolder> {
        private final List<RecentActivityItem> items;

        public RecentActivityAdapter(List<RecentActivityItem> items) {
            this.items = items;
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(android.R.layout.simple_list_item_2, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            RecentActivityItem item = items.get(position);
            holder.textPrimary.setText(item.getAction());
            holder.textSecondary.setText(item.getUser() + " • " + formatTime(item.getTimestamp()));
        }

        @Override
        public int getItemCount() {
            return items.size();
        }

        private String formatTime(long timestamp) {
            long diff = System.currentTimeMillis() - timestamp;
            if (diff < 60000) return "Just now";
            if (diff < 3600000) return (diff / 60000) + " min ago";
            if (diff < 86400000) return (diff / 3600000) + " hours ago";
            return (diff / 86400000) + " days ago";
        }

        static class ViewHolder extends RecyclerView.ViewHolder {
            TextView textPrimary, textSecondary;

            ViewHolder(View itemView) {
                super(itemView);
                textPrimary = itemView.findViewById(android.R.id.text1);
                textSecondary = itemView.findViewById(android.R.id.text2);
            }
        }
    }
}
