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
                .inflate(R.layout.item_prescription, parent, false);
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
        private TextView textPatientName;
        private TextView textDoctorName;
        private TextView textDate;
        private TextView textStatus;
        private TextView textMedications;

        public PrescriptionViewHolder(@NonNull View itemView) {
            super(itemView);
            textPatientName = itemView.findViewById(R.id.text_patient_name);
            textDoctorName = itemView.findViewById(R.id.text_doctor_name);
            textDate = itemView.findViewById(R.id.text_date);
            textStatus = itemView.findViewById(R.id.text_status);
            textMedications = itemView.findViewById(R.id.text_medications);

            itemView.setOnClickListener(v -> {
                int position = getAdapterPosition();
                if (position != RecyclerView.NO_POSITION && listener != null) {
                    listener.onPrescriptionClick(prescriptions.get(position));
                }
            });
        }

        public void bind(Prescription prescription) {
            textPatientName.setText(prescription.getPatientName());
            textDoctorName.setText(prescription.getDoctorName());

            SimpleDateFormat dateFormat = new SimpleDateFormat("MMM dd, yyyy", Locale.getDefault());
            textDate.setText(dateFormat.format(prescription.getPrescribedDate()));

            textStatus.setText(capitalizeFirstLetter(prescription.getStatus()));

            // Display medications
            StringBuilder medText = new StringBuilder();
            if (prescription.getMedications() != null) {
                for (int i = 0; i < prescription.getMedications().size(); i++) {
                    Prescription.Medication med = prescription.getMedications().get(i);
                    if (i > 0) medText.append("\n");
                    medText.append("- ").append(med.getName()).append(" (").append(med.getDosage()).append(")");
                }
            }
            textMedications.setText(medText.toString());

            // Set status color based on status
            if ("fulfilled".equalsIgnoreCase(prescription.getStatus())) {
                textStatus.setTextColor(itemView.getResources().getColor(android.R.color.holo_green_dark));
            } else if ("expired".equalsIgnoreCase(prescription.getStatus())) {
                textStatus.setTextColor(itemView.getResources().getColor(android.R.color.holo_red_dark));
            } else {
                textStatus.setTextColor(itemView.getResources().getColor(android.R.color.holo_blue_dark));
            }
        }

        private String capitalizeFirstLetter(String str) {
            if (str == null || str.isEmpty()) {
                return str;
            }
            return str.substring(0, 1).toUpperCase() + str.substring(1);
        }
    }
}