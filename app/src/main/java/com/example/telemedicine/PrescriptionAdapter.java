package com.example.telemedicine;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

public class PrescriptionAdapter extends RecyclerView.Adapter<PrescriptionAdapter.PrescriptionViewHolder> {
    private List<Prescription> prescriptions;
    private OnPrescriptionClickListener listener;

    public interface OnPrescriptionClickListener {
        void onPrescriptionClick(Prescription prescription);
    }

    public PrescriptionAdapter(List<Prescription> prescriptions, OnPrescriptionClickListener listener) {
        this.prescriptions = prescriptions;
        this.listener = listener;
    }

    @NonNull
    @Override
    public PrescriptionViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_prescription_ios, parent, false);
        return new PrescriptionViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PrescriptionViewHolder holder, int position) {
        Prescription prescription = prescriptions.get(position);
        holder.bind(prescription);
    }

    @Override
    public int getItemCount() {
        return prescriptions.size();
    }

    public void updatePrescriptions(List<Prescription> newPrescriptions) {
        this.prescriptions = newPrescriptions;
        notifyDataSetChanged();
    }

    class PrescriptionViewHolder extends RecyclerView.ViewHolder {
        private TextView textMedicationName;
        private TextView textPatientName;
        private TextView textStatus;
        private TextView textDosage;
        private TextView textDuration;
        private TextView textPrescribedBy;

        public PrescriptionViewHolder(@NonNull View itemView) {
            super(itemView);
            textMedicationName = itemView.findViewById(R.id.text_medication_name);
            textPatientName = itemView.findViewById(R.id.text_patient_name);
            textStatus = itemView.findViewById(R.id.text_status);
            textDosage = itemView.findViewById(R.id.text_dosage);
            textDuration = itemView.findViewById(R.id.text_duration);
            textPrescribedBy = itemView.findViewById(R.id.text_prescribed_by);

            itemView.setOnClickListener(v -> {
                int position = getAdapterPosition();
                if (position != RecyclerView.NO_POSITION && listener != null) {
                    listener.onPrescriptionClick(prescriptions.get(position));
                }
            });
        }

        public void bind(Prescription prescription) {
            Prescription.Medication primaryMedication = null;
            if (prescription.getMedications() != null && !prescription.getMedications().isEmpty()) {
                primaryMedication = prescription.getMedications().get(0);
            }

            textMedicationName.setText(primaryMedication != null && primaryMedication.getName() != null
                    ? primaryMedication.getName()
                    : "Prescription");
            textPatientName.setText(prescription.getPatientName() != null
                    ? "Patient: " + prescription.getPatientName()
                    : "Patient information pending");

            String status = prescription.getStatus();
            textStatus.setText(capitalizeFirstLetter(status));

            if (primaryMedication != null) {
                String dosage = primaryMedication.getDosage() != null ? primaryMedication.getDosage() : "Dosage pending";
                String frequency = primaryMedication.getFrequency() != null ? primaryMedication.getFrequency() : "";
                String duration = primaryMedication.getDuration() != null ? primaryMedication.getDuration() : "";
                textDosage.setText(frequency.isEmpty() ? dosage : dosage + " • " + frequency);
                textDuration.setText(duration.isEmpty() ? buildIssuedDate(prescription) : duration);
            } else {
                textDosage.setText("Medication details pending");
                textDuration.setText(buildIssuedDate(prescription));
            }
            textPrescribedBy.setText(prescription.getDoctorName() != null ? prescription.getDoctorName() : "Doctor pending");

            if ("fulfilled".equalsIgnoreCase(status)) {
                textStatus.setTextColor(itemView.getResources().getColor(android.R.color.holo_green_dark));
            } else if ("expired".equalsIgnoreCase(status)) {
                textStatus.setTextColor(itemView.getResources().getColor(android.R.color.holo_red_dark));
            } else {
                textStatus.setTextColor(itemView.getResources().getColor(android.R.color.holo_blue_dark));
            }
        }

        private String buildIssuedDate(Prescription prescription) {
            SimpleDateFormat dateFormat = new SimpleDateFormat("MMM dd, yyyy", Locale.getDefault());
            if (prescription.getPrescribedDate() != null) {
                return dateFormat.format(prescription.getPrescribedDate());
            }
            if (prescription.getCreatedAt() > 0) {
                return dateFormat.format(new java.util.Date(prescription.getCreatedAt()));
            }
            return "TBD";
        }

        private String capitalizeFirstLetter(String str) {
            if (str == null || str.isEmpty()) {
                return "Active";
            }
            return str.substring(0, 1).toUpperCase() + str.substring(1);
        }
    }
}
