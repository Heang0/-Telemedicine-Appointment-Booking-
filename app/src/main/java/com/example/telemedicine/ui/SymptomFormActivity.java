package com.example.telemedicine.ui;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.telemedicine.R;
import com.example.telemedicine.model.SymptomForm;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

public class SymptomFormActivity extends AppCompatActivity {
    private EditText chiefComplaintEditText, durationEditText, severityEditText, locationEditText;
    private EditText associatedSymptomsEditText, aggravatingFactorsEditText, relievingFactorsEditText;
    private EditText pastMedicalHistoryEditText, medicationsEditText, allergiesEditText;
    private EditText familyHistoryEditText, socialHistoryEditText, reviewOfSystemsEditText;
    private EditText vitalSignsEditText, notesEditText;
    private Button submitButton;
    private TextView appointmentInfoTextView;

    private FirebaseFirestore db;
    private String appointmentId;
    private String patientId;
    private String patientName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_symptom_form);

        // Initialize views
        chiefComplaintEditText = findViewById(R.id.editTextChiefComplaint);
        durationEditText = findViewById(R.id.editTextDuration);
        severityEditText = findViewById(R.id.editTextSeverity);
        locationEditText = findViewById(R.id.editTextLocation);
        associatedSymptomsEditText = findViewById(R.id.editTextAssociatedSymptoms);
        aggravatingFactorsEditText = findViewById(R.id.editTextAggravatingFactors);
        relievingFactorsEditText = findViewById(R.id.editTextRelievingFactors);
        pastMedicalHistoryEditText = findViewById(R.id.editTextPastMedicalHistory);
        medicationsEditText = findViewById(R.id.editTextMedications);
        allergiesEditText = findViewById(R.id.editTextAllergies);
        familyHistoryEditText = findViewById(R.id.editTextFamilyHistory);
        socialHistoryEditText = findViewById(R.id.editTextSocialHistory);
        reviewOfSystemsEditText = findViewById(R.id.editTextReviewOfSystems);
        vitalSignsEditText = findViewById(R.id.editTextVitalSigns);
        notesEditText = findViewById(R.id.editTextNotes);
        submitButton = findViewById(R.id.buttonSubmitSymptomForm);
        appointmentInfoTextView = findViewById(R.id.textViewAppointmentInfo);

        // Get appointment ID from intent
        appointmentId = getIntent().getStringExtra("appointment_id");
        patientId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        patientName = getIntent().getStringExtra("patient_name");

        if (appointmentId != null && patientName != null) {
            appointmentInfoTextView.setText(String.format("Appointment: %s - %s", appointmentId, patientName));
        }

        db = FirebaseFirestore.getInstance();

        // Set up submit button
        submitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                submitSymptomForm();
            }
        });
    }

    private void submitSymptomForm() {
        String chiefComplaint = chiefComplaintEditText.getText().toString().trim();
        String duration = durationEditText.getText().toString().trim();
        String severity = severityEditText.getText().toString().trim();
        String location = locationEditText.getText().toString().trim();
        String associatedSymptoms = associatedSymptomsEditText.getText().toString().trim();
        String aggravatingFactors = aggravatingFactorsEditText.getText().toString().trim();
        String relievingFactors = relievingFactorsEditText.getText().toString().trim();
        String pastMedicalHistory = pastMedicalHistoryEditText.getText().toString().trim();
        String medications = medicationsEditText.getText().toString().trim();
        String allergies = allergiesEditText.getText().toString().trim();
        String familyHistory = familyHistoryEditText.getText().toString().trim();
        String socialHistory = socialHistoryEditText.getText().toString().trim();
        String reviewOfSystems = reviewOfSystemsEditText.getText().toString().trim();
        String vitalSigns = vitalSignsEditText.getText().toString().trim();
        String notes = notesEditText.getText().toString().trim();

        // Basic validation
        if (chiefComplaint.isEmpty()) {
            Toast.makeText(this, "Please enter chief complaint", Toast.LENGTH_SHORT).show();
            return;
        }

        // Create symptom form
        SymptomForm symptomForm = new SymptomForm(
                patientId,
                appointmentId,
                patientName,
                chiefComplaint,
                duration,
                severity,
                location,
                associatedSymptoms,
                aggravatingFactors,
                relievingFactors,
                pastMedicalHistory,
                medications,
                allergies,
                familyHistory,
                socialHistory,
                reviewOfSystems,
                vitalSigns,
                notes
        );

        // Save to Firestore
        db.collection("symptom_forms")
                .add(symptomForm)
                .addOnSuccessListener(documentReference -> {
                    Toast.makeText(this, "Symptom form submitted successfully!", Toast.LENGTH_SHORT).show();
                    finish(); // Close activity
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Error submitting symptom form: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }
}