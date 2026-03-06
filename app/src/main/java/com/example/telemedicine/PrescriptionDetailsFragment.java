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
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class PrescriptionDetailsFragment extends Fragment {

    private TextView textPatientName, textDoctorName, textDate, textStatus, textMedications, textInstructions, textNotes;
    private Button btnBack, btnEdit, btnDelete, btnMarkFilled;

    private String prescriptionId;
    private FirebaseFirestore db;
    private FirebaseAuth mAuth;
    private Prescription loadedPrescription;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_prescription_details, container, false);

        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();

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
        btnEdit = view.findViewById(R.id.btn_edit);
        btnDelete = view.findViewById(R.id.btn_delete);
        btnMarkFilled = view.findViewById(R.id.btn_mark_filled);

        if (btnBack != null) {
            btnBack.setOnClickListener(v -> {
                if (getActivity() != null) {
                    getActivity().getSupportFragmentManager().popBackStack();
                }
            });
        }
        if (btnEdit != null) {
            btnEdit.setOnClickListener(v -> openEditPrescription());
        }
        if (btnDelete != null) {
            btnDelete.setOnClickListener(v -> deletePrescription());
        }
        if (btnMarkFilled != null) {
            btnMarkFilled.setOnClickListener(v -> markAsFilled());
        }
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
                        if (prescription == null) {
                            Toast.makeText(getContext(), "Prescription data is invalid", Toast.LENGTH_SHORT).show();
                            return;
                        }
                        loadedPrescription = prescription;

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

                        if (textDate != null) {
                            SimpleDateFormat dateFormat = new SimpleDateFormat("MMM dd, yyyy", Locale.getDefault());
                            if (prescription.getPrescribedDate() != null) {
                                textDate.setText(dateFormat.format(prescription.getPrescribedDate()));
                            } else if (prescription.getCreatedAt() > 0) {
                                textDate.setText(dateFormat.format(new Date(prescription.getCreatedAt())));
                            }
                        }

                        if (textStatus != null) {
                            String status = prescription.getStatus() != null ? prescription.getStatus() : "active";
                            textStatus.setText(capitalizeFirstLetter(status));

                            // Set status color based on status
                            if ("fulfilled".equalsIgnoreCase(status)) {
                                textStatus.setTextColor(getResources().getColor(android.R.color.holo_green_dark));
                            } else if ("expired".equalsIgnoreCase(status)) {
                                textStatus.setTextColor(getResources().getColor(android.R.color.holo_red_dark));
                            } else {
                                textStatus.setTextColor(getResources().getColor(android.R.color.holo_blue_dark));
                            }
                        }

                        if (textMedications != null && prescription.getMedications() != null) {
                            StringBuilder medText = new StringBuilder();
                            for (int i = 0; i < prescription.getMedications().size(); i++) {
                                Prescription.Medication med = prescription.getMedications().get(i);
                                if (med == null) {
                                    continue;
                                }
                                if (i > 0) medText.append("\n");
                                medText.append("- ").append(safe(med.getName(), "Medication")).append(": ")
                                      .append(safe(med.getDosage(), "dosage pending")).append(" ")
                                      .append(safe(med.getFrequency(), "frequency pending")).append(" for ")
                                      .append(safe(med.getDuration(), "duration pending")).append(" (Qty: ")
                                      .append(safe(med.getQuantity(), "N/A")).append(")");
                            }
                            textMedications.setText(medText.length() > 0 ? medText.toString() : "No medications listed");
                        }

                        if (textInstructions != null && prescription.getInstructions() != null) {
                            textInstructions.setText(prescription.getInstructions());
                        }

                        if (textNotes != null && prescription.getNotes() != null) {
                            textNotes.setText(prescription.getNotes());
                        }

                        updateActionButtons(prescription);
                    } else {
                        Toast.makeText(getContext(), "Prescription not found", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(getContext(), "Failed to load prescription: " + e.getMessage(), 
                            Toast.LENGTH_SHORT).show();
                });
    }

    private void updateActionButtons(Prescription prescription) {
        if (mAuth.getCurrentUser() == null || prescription == null) {
            return;
        }
        String currentUserId = mAuth.getCurrentUser().getUid();
        boolean isDoctor = currentUserId.equals(prescription.getDoctorId());
        boolean isPatient = currentUserId.equals(prescription.getPatientId());

        if (btnEdit != null) {
            btnEdit.setVisibility(isDoctor ? View.VISIBLE : View.GONE);
        }
        if (btnDelete != null) {
            btnDelete.setVisibility(isDoctor ? View.VISIBLE : View.GONE);
        }
        if (btnMarkFilled != null) {
            btnMarkFilled.setVisibility(isPatient ? View.VISIBLE : View.GONE);
        }
    }

    private void openEditPrescription() {
        if (getActivity() == null || prescriptionId == null) {
            return;
        }
        DigitalPrescriptionPadFragment editFragment = new DigitalPrescriptionPadFragment();
        Bundle bundle = new Bundle();
        bundle.putString("prescription_id", prescriptionId);
        editFragment.setArguments(bundle);

        getActivity().getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.fragment_container, editFragment)
                .addToBackStack(null)
                .commit();
    }

    private void deletePrescription() {
        if (prescriptionId == null) {
            Toast.makeText(getContext(), "Prescription ID is missing", Toast.LENGTH_SHORT).show();
            return;
        }
        db.collection("prescriptions")
                .document(prescriptionId)
                .delete()
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(getContext(), "Prescription deleted", Toast.LENGTH_SHORT).show();
                    if (getActivity() != null) {
                        getActivity().getSupportFragmentManager().popBackStack();
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(getContext(), "Failed to delete: " + e.getMessage(), Toast.LENGTH_LONG).show();
                });
    }

    private void markAsFilled() {
        if (prescriptionId == null) {
            Toast.makeText(getContext(), "Prescription ID is missing", Toast.LENGTH_SHORT).show();
            return;
        }
        db.collection("prescriptions")
                .document(prescriptionId)
                .update("status", "fulfilled", "updatedAt", System.currentTimeMillis())
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(getContext(), "Marked as filled", Toast.LENGTH_SHORT).show();
                    loadPrescriptionDetails();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(getContext(), "Failed to update status: " + e.getMessage(), Toast.LENGTH_LONG).show();
                });
    }

    private String capitalizeFirstLetter(String str) {
        if (str == null || str.isEmpty()) {
            return "Active";
        }
        return str.substring(0, 1).toUpperCase() + str.substring(1);
    }

    private String safe(String value, String fallback) {
        return value != null && !value.trim().isEmpty() ? value.trim() : fallback;
    }
}
