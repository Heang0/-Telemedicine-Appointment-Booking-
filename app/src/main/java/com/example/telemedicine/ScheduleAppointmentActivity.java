package com.example.telemedicine;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
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

    private Spinner spinnerDoctor, spinnerAppointmentType;
    private EditText editReason;
    private Button btnSchedule;
    private TextView textSelectedDate, textSelectedTime;

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
        spinnerAppointmentType = findViewById(R.id.spinner_appointment_type);
        textSelectedDate = findViewById(R.id.text_selected_date);
        textSelectedTime = findViewById(R.id.text_selected_time);
        editReason = findViewById(R.id.edit_reason);
        btnSchedule = findViewById(R.id.btn_schedule);
    }

    private void setupSpinners() {
        // Setup doctor spinner adapter
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, new ArrayList<>());
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerDoctor.setAdapter(adapter);

        // Setup appointment type spinner
        String[] appointmentTypes = {"In-Person Visit", "Video Consultation", "Follow-up", "Chat Consultation"};
        ArrayAdapter<String> typeAdapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, appointmentTypes);
        typeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerAppointmentType.setAdapter(typeAdapter);

        // Setup date picker
        textSelectedDate.setOnClickListener(v -> showDatePicker());

        // Setup time picker
        textSelectedTime.setOnClickListener(v -> showTimePicker());
        
        // Setup schedule button
        btnSchedule.setOnClickListener(v -> scheduleAppointment());
    }

    private void showDatePicker() {
        Calendar calendar = Calendar.getInstance();
        DatePickerDialog datePickerDialog = new DatePickerDialog(
                this,
                (view, year, month, dayOfMonth) -> {
                    String date = String.format(Locale.getDefault(), "%d/%02d/%02d", year, month + 1, dayOfMonth);
                    textSelectedDate.setText(date);
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
        );
        datePickerDialog.show();
    }

    private void showTimePicker() {
        Calendar calendar = Calendar.getInstance();
        TimePickerDialog timePickerDialog = new TimePickerDialog(
                this,
                (view, hourOfDay, minute) -> {
                    String time = String.format(Locale.getDefault(), "%02d:%02d", hourOfDay, minute);
                    textSelectedTime.setText(time);
                },
                calendar.get(Calendar.HOUR_OF_DAY),
                calendar.get(Calendar.MINUTE),
                true
        );
        timePickerDialog.show();
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
            if (mAuth.getCurrentUser() == null) {
                Toast.makeText(this, "User not authenticated", Toast.LENGTH_SHORT).show();
                return;
            }
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
            if (selectedDoctorIndex < 0 || doctorIds == null || selectedDoctorIndex >= doctorIds.size()) {
                Toast.makeText(this, "Please select a valid doctor", Toast.LENGTH_SHORT).show();
                return;
            }
            String currentDoctorId = doctorIds.get(selectedDoctorIndex);
            if (currentDoctorId == null || currentDoctorId.trim().isEmpty()) {
                Toast.makeText(this, "No doctors available", Toast.LENGTH_SHORT).show();
                return;
            }
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
        // Get date from text view
        String dateStr = textSelectedDate.getText().toString();
        String timeStr = textSelectedTime.getText().toString();

        if (dateStr.equals("Select Date") || timeStr.equals("Select Time")) {
            Toast.makeText(this, "Please select date and time", Toast.LENGTH_SHORT).show();
            return;
        }

        // Parse date (format: yyyy/MM/dd)
        String[] dateParts = dateStr.split("/");
        int year = Integer.parseInt(dateParts[0]);
        int month = Integer.parseInt(dateParts[1]) - 1; // Calendar months are 0-based
        int day = Integer.parseInt(dateParts[2]);

        // Parse time (format: HH:mm)
        String[] timeParts = timeStr.split(":");
        int hour = Integer.parseInt(timeParts[0]);
        int minute = Integer.parseInt(timeParts[1]);

        Calendar calendar = Calendar.getInstance();
        calendar.set(year, month, day, hour, minute, 0);
        Date appointmentDateTime = calendar.getTime();

        // Check if appointment is in the past
        if (appointmentDateTime.before(new Date())) {
            Toast.makeText(this, "Cannot schedule appointment in the past", Toast.LENGTH_SHORT).show();
            return;
        }

        // Get appointment type
        String appointmentType = spinnerAppointmentType.getSelectedItem().toString().toLowerCase().replace(" ", "_");

        // Create appointment object
        Appointment appointment = new Appointment(
                patientId,
                doctorId,
                patientName,
                doctorName,
                appointmentDateTime,
                "scheduled",
                editReason.getText().toString().trim(),
                appointmentType
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
