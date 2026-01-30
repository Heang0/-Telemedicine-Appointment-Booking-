package com.example.telemedicine;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class ScheduleAppointmentActivity extends AppCompatActivity {

    private Spinner spinnerDoctor, spinnerMonth, spinnerDay, spinnerYear, spinnerHour, spinnerMinute;
    private EditText editReason;
    private Button btnSchedule;

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private List<String> doctorIds;
    private List<String> doctorNames;
    private com.google.firebase.firestore.ListenerRegistration doctorsListenerRegistration;

    // Variables for doctor-patient scheduling
    private String selectedPatientId;
    private String selectedPatientName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // Apply theme before calling super.onCreate
        ThemeUtils.applyTheme(this);

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_schedule_appointment);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        // Setup toolbar with back button
        androidx.appcompat.widget.Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // Enable back button in action bar
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }

        initializeViews();
        setupSpinners();

        // Check if this activity was launched from doctor's patient list
        selectedPatientId = getIntent().getStringExtra("selected_patient_id");
        selectedPatientName = getIntent().getStringExtra("selected_patient_name");

        if (selectedPatientId != null && selectedPatientName != null) {
            // This is from doctor's patient list - load only this patient
            setupForDoctorPatientScheduling(selectedPatientId, selectedPatientName);
        } else {
            // Regular scheduling - load all doctors
            loadDoctors();
        }

        btnSchedule = findViewById(R.id.btn_schedule);
        btnSchedule.setOnClickListener(v -> scheduleAppointment());
    }

    private void setupForDoctorPatientScheduling(String patientId, String patientName) {
        // For doctors scheduling with specific patients
        // In this case, the doctor is scheduling with a specific patient
        // So we need to set up the spinners differently

        // Load the current doctor (the one scheduling)
        String doctorId = mAuth.getCurrentUser().getUid();

        // Load doctor's information
        db.collection("users")
                .document(doctorId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        User doctor = documentSnapshot.toObject(User.class);
                        // Set up the UI for doctor-patient scheduling
                        // Disable doctor spinner since it's the current doctor
                        spinnerDoctor.setEnabled(false);

                        // Create a list with just the current doctor
                        List<String> doctorNames = new ArrayList<>();
                        doctorNames.add(doctor.getFullName() + " (" + doctor.getSpecialization() + ")");

                        ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                                android.R.layout.simple_spinner_item, doctorNames);
                        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                        spinnerDoctor.setAdapter(adapter);

                        // Set the spinner to the current doctor
                        spinnerDoctor.setSelection(0);

                        // Store the doctor ID for later use
                        this.doctorIds = new ArrayList<>();
                        this.doctorNames = new ArrayList<>();
                        this.doctorIds.add(doctorId);
                        this.doctorNames.add(doctor.getFullName() + " (" + doctor.getSpecialization() + ")");

                        // Update the title to reflect that this is scheduling with a specific patient
                        androidx.appcompat.widget.Toolbar toolbar = findViewById(R.id.toolbar);
                        if (toolbar != null) {
                            toolbar.setTitle("Schedule with " + patientName);
                        }
                    }
                });
    }

    private void initializeViews() {
        spinnerDoctor = findViewById(R.id.spinner_doctor);
        spinnerMonth = findViewById(R.id.spinner_month);
        spinnerDay = findViewById(R.id.spinner_day);
        spinnerYear = findViewById(R.id.spinner_year);
        spinnerHour = findViewById(R.id.spinner_hour);
        spinnerMinute = findViewById(R.id.spinner_minute);
        editReason = findViewById(R.id.edit_reason);
    }

    private void setupSpinners() {
        // Months
        String[] months = {"Jan", "Feb", "Mar", "Apr", "May", "Jun",
                          "Jul", "Aug", "Sep", "Oct", "Nov", "Dec"};
        ArrayAdapter<String> monthAdapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, months);
        monthAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerMonth.setAdapter(monthAdapter);

        // Days
        String[] days = new String[31];
        for (int i = 0; i < 31; i++) {
            days[i] = String.format(Locale.getDefault(), "%02d", i + 1);
        }
        ArrayAdapter<String> dayAdapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, days);
        dayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerDay.setAdapter(dayAdapter);

        // Years (next 2 years)
        int currentYear = Calendar.getInstance().get(Calendar.YEAR);
        String[] years = new String[2];
        for (int i = 0; i < 2; i++) {
            years[i] = String.valueOf(currentYear + i);
        }
        ArrayAdapter<String> yearAdapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, years);
        yearAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerYear.setAdapter(yearAdapter);

        // Hours
        String[] hours = new String[24];
        for (int i = 0; i < 24; i++) {
            hours[i] = String.format(Locale.getDefault(), "%02d", i);
        }
        ArrayAdapter<String> hourAdapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, hours);
        hourAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerHour.setAdapter(hourAdapter);

        // Minutes
        String[] minutes = new String[60];
        for (int i = 0; i < 60; i++) {
            minutes[i] = String.format(Locale.getDefault(), "%02d", i);
        }
        ArrayAdapter<String> minuteAdapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, minutes);
        minuteAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerMinute.setAdapter(minuteAdapter);
    }

    private void loadDoctors() {
        // Fetch doctors from Firestore
        doctorIds = new ArrayList<>();
        doctorNames = new ArrayList<>();

        // Remove previous listener if exists
        if (doctorsListenerRegistration != null) {
            doctorsListenerRegistration.remove();
        }

        doctorsListenerRegistration = db.collection("users")
                .whereEqualTo("role", UserRole.DOCTOR.getRoleName())
                .addSnapshotListener((querySnapshot, error) -> {
                    if (error != null) {
                        return;
                    }

                    if (querySnapshot != null) {
                        doctorIds.clear();
                        doctorNames.clear();

                        for (com.google.firebase.firestore.QueryDocumentSnapshot document : querySnapshot) {
                            User doctor = document.toObject(User.class);
                            doctorIds.add(doctor.getUserId());
                            doctorNames.add(doctor.getFullName() + " (" + doctor.getSpecialization() + ")");
                        }

                        // If no doctors are found, add a placeholder
                        if (doctorIds.isEmpty()) {
                            doctorIds.add("");
                            doctorNames.add("No doctors available");
                        }

                        ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                                android.R.layout.simple_spinner_item, doctorNames);
                        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                        spinnerDoctor.setAdapter(adapter);
                    }
                });
    }

    private void scheduleAppointment() {
        if (!validateInputs()) {
            return;
        }

        // Check if this is doctor scheduling with a specific patient
        boolean isDoctorScheduling = selectedPatientId != null && selectedPatientName != null;

        String doctorId;
        String doctorName;
        String patientId;
        String patientName;

        if (isDoctorScheduling) {
            // Doctor is scheduling with a specific patient
            // The doctor is the current user
            String currentDoctorId = mAuth.getCurrentUser().getUid();
            // Get doctor's name from the spinner (it should be the current doctor's name)
            String currentDoctorName = spinnerDoctor.getSelectedItem().toString();
            // Use the selected patient
            String currentPatientId = selectedPatientId;
            String currentPatientName = selectedPatientName;

            // Create appointment directly
            createAppointment(currentPatientId, currentPatientName, currentDoctorId, currentDoctorName);
        } else {
            // Regular scheduling (patient scheduling with a doctor)
            String selectedDoctorName = spinnerDoctor.getSelectedItem().toString();
            int selectedDoctorIndex = doctorNames.indexOf(selectedDoctorName);
            String currentDoctorId = doctorIds.get(selectedDoctorIndex);
            String currentDoctorName = selectedDoctorName;

            // Get current user info (patient)
            FirebaseUser currentUser = mAuth.getCurrentUser();
            if (currentUser == null) {
                Toast.makeText(this, "User not authenticated", Toast.LENGTH_SHORT).show();
                return;
            }

            // Get patient profile to get name
            db.collection("users")
                    .document(currentUser.getUid())
                    .get()
                    .addOnSuccessListener(documentSnapshot -> {
                        if (documentSnapshot.exists()) {
                            User user = documentSnapshot.toObject(User.class);
                            String currentPatientId = currentUser.getUid();
                            String currentPatientName = user.getFullName();

                            // Continue with appointment creation
                            createAppointment(currentPatientId, currentPatientName, currentDoctorId, currentDoctorName);
                        } else {
                            Toast.makeText(ScheduleAppointmentActivity.this,
                                "User profile not found",
                                Toast.LENGTH_SHORT).show();
                        }
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(ScheduleAppointmentActivity.this,
                            "Error retrieving user profile: " + e.getMessage(),
                            Toast.LENGTH_LONG).show();
                    });
        }
    }

    private void createAppointment(String patientId, String patientName, String doctorId, String doctorName) {
        String monthStr = spinnerMonth.getSelectedItem().toString();
        String dayStr = spinnerDay.getSelectedItem().toString();
        String yearStr = spinnerYear.getSelectedItem().toString();
        String hourStr = spinnerHour.getSelectedItem().toString();
        String minuteStr = spinnerMinute.getSelectedItem().toString();

        // Parse date and time
        int month = getMonthNumber(monthStr);
        int day = Integer.parseInt(dayStr);
        int year = Integer.parseInt(yearStr);
        int hour = Integer.parseInt(hourStr);
        int minute = Integer.parseInt(minuteStr);

        Calendar calendar = Calendar.getInstance();
        calendar.set(year, month, day, hour, minute, 0);
        Date appointmentDateTime = calendar.getTime();

        // Check if appointment is in the past
        if (appointmentDateTime.before(new Date())) {
            Toast.makeText(this, "Cannot schedule appointment in the past", Toast.LENGTH_SHORT).show();
            return;
        }

        // Create appointment object
        Appointment appointment = new Appointment(
                patientId,
                doctorId,
                patientName,
                doctorName,
                appointmentDateTime,
                "scheduled",
                editReason.getText().toString().trim()
        );

        // Save to Firestore
        db.collection("appointments")
                .add(appointment)
                .addOnSuccessListener(documentReference -> {
                    Toast.makeText(ScheduleAppointmentActivity.this,
                        "Appointment scheduled successfully!",
                        Toast.LENGTH_SHORT).show();

                    // Also add to user's appointments subcollection
                    Map<String, Object> appointmentRef = new HashMap<>();
                    appointmentRef.put("appointmentId", documentReference.getId());
                    appointmentRef.put("status", "scheduled");
                    appointmentRef.put("createdAt", Timestamp.now());

                    // Add to patient's appointments subcollection
                    db.collection("users")
                            .document(patientId)
                            .collection("appointments")
                            .add(appointmentRef)
                            .addOnSuccessListener(aVoid -> {
                                finish(); // Close activity
                            })
                            .addOnFailureListener(e -> {
                                // Even if subcollection fails, appointment is still scheduled
                                finish();
                            });
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(ScheduleAppointmentActivity.this,
                        "Failed to schedule appointment: " + e.getMessage(),
                        Toast.LENGTH_LONG).show();
                });
    }

    private boolean validateInputs() {
        if (TextUtils.isEmpty(editReason.getText().toString())) {
            editReason.setError("Reason is required");
            editReason.requestFocus();
            return false;
        }
        return true;
    }

    private int getMonthNumber(String monthAbbr) {
        String[] months = {"Jan", "Feb", "Mar", "Apr", "May", "Jun",
                          "Jul", "Aug", "Sep", "Oct", "Nov", "Dec"};
        for (int i = 0; i < months.length; i++) {
            if (months[i].equals(monthAbbr)) {
                return i; // Month in Calendar is 0-indexed
            }
        }
        return 0; // Default to January if not found
    }

    @Override
    public boolean onOptionsItemSelected(android.view.MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            // Handle the back button press
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Clean up the listener to prevent memory leaks
        if (doctorsListenerRegistration != null) {
            doctorsListenerRegistration.remove();
        }
    }
}