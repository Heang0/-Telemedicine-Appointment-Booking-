package com.example.telemedicine.ui;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.telemedicine.R;
import com.example.telemedicine.model.Prescription;
import com.example.telemedicine.security.EncryptionUtil;
import com.example.telemedicine.DigitalPrescriptionPad;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

public class DigitalPrescriptionPadActivity extends AppCompatActivity {
    private EditText medicationNameEditText, dosageEditText, frequencyEditText, durationEditText, quantityEditText;
    private EditText instructionsEditText, notesEditText;
    private Spinner medicationSpinner;
    private Button addMedicationButton, generatePrescriptionButton, savePrescriptionButton;
    private ImageView qrCodeImageView;
    private TextView prescriptionInfoTextView;
    private FirebaseFirestore db;
    private String appointmentId;
    private String patientId;
    private String doctorId;
    private String patientName;
    private String doctorName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_digital_prescription_pad);

        // Initialize views
        medicationNameEditText = findViewById(R.id.editTextMedicationName);
        dosageEditText = findViewById(R.id.editTextDosage);
        frequencyEditText = findViewById(R.id.editTextFrequency);
        durationEditText = findViewById(R.id.editTextDuration);
        quantityEditText = findViewById(R.id.editTextQuantity);
        instructionsEditText = findViewById(R.id.editTextInstructions);
        notesEditText = findViewById(R.id.editTextNotes);
        medicationSpinner = findViewById(R.id.spinnerMedications);
        addMedicationButton = findViewById(R.id.buttonAddMedication);
        generatePrescriptionButton = findViewById(R.id.buttonGeneratePrescription);
        savePrescriptionButton = findViewById(R.id.buttonSavePrescription);
        qrCodeImageView = findViewById(R.id.imageViewQRCode);
        prescriptionInfoTextView = findViewById(R.id.textViewPrescriptionInfo);

        db = FirebaseFirestore.getInstance();
        
        // Get appointment info from intent
        appointmentId = getIntent().getStringExtra("appointment_id");
        patientId = getIntent().getStringExtra("patient_id");
        doctorId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        patientName = getIntent().getStringExtra("patient_name");
        doctorName = getIntent().getStringExtra("doctor_name");

        if (appointmentId != null && patientName != null) {
            prescriptionInfoTextView.setText(String.format("Prescription for: %s", patientName));
        }

        // Set up add medication button
        addMedicationButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addMedication();
            }
        });

        // Set up generate prescription button
        generatePrescriptionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                generatePrescription();
            }
        });

        // Set up save prescription button
        savePrescriptionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                savePrescription();
            }
        });
    }

    private void addMedication() {
        String medicationName = medicationNameEditText.getText().toString().trim();
        String dosage = dosageEditText.getText().toString().trim();
        String frequency = frequencyEditText.getText().toString().trim();
        String duration = durationEditText.getText().toString().trim();
        String quantity = quantityEditText.getText().toString().trim();

        if (medicationName.isEmpty()) {
            Toast.makeText(this, "Please enter medication name", Toast.LENGTH_SHORT).show();
            return;
        }

        // In a real app, this would add to a list of medications
        Toast.makeText(this, "Medication added: " + medicationName, Toast.LENGTH_SHORT).show();
    }

    private void generatePrescription() {
        String medicationName = medicationNameEditText.getText().toString().trim();
        String dosage = dosageEditText.getText().toString().trim();
        String frequency = frequencyEditText.getText().toString().trim();

        if (medicationName.isEmpty() || dosage.isEmpty() || frequency.isEmpty()) {
            Toast.makeText(this, "Please fill in medication name, dosage, and frequency", Toast.LENGTH_SHORT).show();
            return;
        }

        // Generate QR code
        Bitmap qrCodeBitmap = DigitalPrescriptionPad.generatePrescriptionQRCode(
                "PRESCRIPTION_" + System.currentTimeMillis(),
                patientId,
                doctorId,
                medicationName,
                dosage,
                frequency
        );

        if (qrCodeBitmap != null) {
            qrCodeImageView.setImageBitmap(qrCodeBitmap);
            Toast.makeText(this, "QR code generated successfully!", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, "Error generating QR code", Toast.LENGTH_SHORT).show();
        }
    }

    private void savePrescription() {
        String medicationName = medicationNameEditText.getText().toString().trim();
        String dosage = dosageEditText.getText().toString().trim();
        String frequency = frequencyEditText.getText().toString().trim();
        String duration = durationEditText.getText().toString().trim();
        String quantity = quantityEditText.getText().toString().trim();
        String instructions = instructionsEditText.getText().toString().trim();
        String notes = notesEditText.getText().toString().trim();

        if (medicationName.isEmpty() || dosage.isEmpty() || frequency.isEmpty()) {
            Toast.makeText(this, "Please fill in required fields", Toast.LENGTH_SHORT).show();
            return;
        }

        // Create medication object using Prescription.Medication inner class
        Prescription.Medication med = new Prescription.Medication(medicationName, dosage, frequency, duration, quantity);
        List<Prescription.Medication> meds = Arrays.asList(med);
        Date expiryDate = new Date(System.currentTimeMillis() + 30L * 24 * 60 * 60 * 1000); // 30 days from now

        // Create prescription
        Prescription prescription = new Prescription(
                patientId,
                doctorId,
                patientName,
                doctorName,
                meds,
                instructions,
                expiryDate,
                notes
        );

        // Save to Firestore
        db.collection("prescriptions")
                .add(prescription)
                .addOnSuccessListener(documentReference -> {
                    Toast.makeText(this, "Prescription saved successfully!", Toast.LENGTH_SHORT).show();
                    finish(); // Close activity
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Error saving prescription: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }
}