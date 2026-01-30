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

import com.google.firebase.firestore.FirebaseFirestore;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class PrescriptionDetailsFragment extends Fragment {

    private TextView textPatientName, textDoctorName, textDate, textStatus, textMedications, textInstructions, textNotes;
    private Button btnBack;

    private String prescriptionId;
    private FirebaseFirestore db;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_prescription_details, container, false);

        db = FirebaseFirestore.getInstance();

        // Get arguments
        Bundle args = getArguments();
        if (args != null) {
            prescriptionId = args.getString("prescription_id");
        }

        initializeViews(view);
        loadPrescriptionDetails();

        return view;
    }

    private void initializeViews(View view) {
        textPatientName = view.findViewById(R.id.text_patient_name);
        textDoctorName = view.findViewById(R.id.text_doctor_name);
        textDate = view.findViewById(R.id.text_date);
        textStatus = view.findViewById(R.id.text_status);
        textMedications = view.findViewById(R.id.text_medications);
        textInstructions = view.findViewById(R.id.text_instructions);
        textNotes = view.findViewById(R.id.text_notes);
        btnBack = view.findViewById(R.id.btn_back);

        btnBack.setOnClickListener(v -> {
            if (getActivity() != null) {
                getActivity().getSupportFragmentManager().popBackStack();
            }
        });
    }

    private void loadPrescriptionDetails() {
        if (prescriptionId == null) {
            Toast.makeText(getContext(), "Prescription ID is missing", Toast.LENGTH_SHORT).show();
            return;
        }

        db.collection("prescriptions")
                .document(prescriptionId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        Prescription prescription = documentSnapshot.toObject(Prescription.class);

                        // Populate the UI with prescription data
                        if (textPatientName != null && prescription.getPatientName() != null) {
                            textPatientName.setText(prescription.getPatientName());
                        }

                        if (textDoctorName != null && prescription.getDoctorName() != null) {
                            String doctorInfo = prescription.getDoctorName();
                            if (prescription.getDoctorSpecialty() != null && !prescription.getDoctorSpecialty().isEmpty()) {
                                doctorInfo += " (" + prescription.getDoctorSpecialty() + ")";
                            }
                            textDoctorName.setText(doctorInfo);
                        }

                        if (textDate != null && prescription.getPrescribedDate() != null) {
                            SimpleDateFormat dateFormat = new SimpleDateFormat("MMM dd, yyyy", Locale.getDefault());
                            textDate.setText(dateFormat.format(prescription.getPrescribedDate()));
                        }

                        if (textStatus != null && prescription.getStatus() != null) {
                            textStatus.setText(capitalizeFirstLetter(prescription.getStatus()));

                            // Set status color based on status
                            if ("fulfilled".equalsIgnoreCase(prescription.getStatus())) {
                                textStatus.setTextColor(getResources().getColor(android.R.color.holo_green_dark));
                            } else if ("expired".equalsIgnoreCase(prescription.getStatus())) {
                                textStatus.setTextColor(getResources().getColor(android.R.color.holo_red_dark));
                            } else {
                                textStatus.setTextColor(getResources().getColor(android.R.color.holo_blue_dark));
                            }
                        }

                        if (textMedications != null && prescription.getMedications() != null) {
                            StringBuilder medText = new StringBuilder();
                            for (int i = 0; i < prescription.getMedications().size(); i++) {
                                Prescription.Medication med = prescription.getMedications().get(i);
                                if (i > 0) medText.append("\n");
                                medText.append("- ").append(med.getName()).append(": ")
                                      .append(med.getDosage()).append(" ")
                                      .append(med.getFrequency()).append(" for ")
                                      .append(med.getDuration()).append(" (Qty: ").append(med.getQuantity()).append(")");
                            }
                            textMedications.setText(medText.toString());
                        }

                        if (textInstructions != null && prescription.getInstructions() != null) {
                            textInstructions.setText(prescription.getInstructions());
                        }

                        if (textNotes != null && prescription.getNotes() != null) {
                            textNotes.setText(prescription.getNotes());
                        }
                    } else {
                        Toast.makeText(getContext(), "Prescription not found", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(getContext(), "Failed to load prescription: " + e.getMessage(), 
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