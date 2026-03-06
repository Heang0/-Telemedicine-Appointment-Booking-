package com.example.telemedicine;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

public class PatientAdapter extends RecyclerView.Adapter<PatientAdapter.PatientViewHolder> {
    private List<User> patients;
    private OnPatientClickListener listener;

    public interface OnPatientClickListener {
        void onPatientClick(User patient);
    }

    public PatientAdapter(List<User> patients, OnPatientClickListener listener) {
        this.patients = patients;
        this.listener = listener;
    }

    @NonNull
    @Override
    public PatientViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_patient, parent, false);
        return new PatientViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PatientViewHolder holder, int position) {
        User patient = patients.get(position);
        holder.bind(patient);
    }

    @Override
    public int getItemCount() {
        return patients.size();
    }

    public void updatePatients(List<User> newPatients) {
        this.patients = newPatients;
        notifyDataSetChanged();
    }

    class PatientViewHolder extends RecyclerView.ViewHolder {
        private TextView textPatientName, textPatientInfo, textLastVisit;
        private TextView textHistory1, textHistory2;
        private Button btnViewProfile;

        public PatientViewHolder(@NonNull View itemView) {
            super(itemView);
            textPatientName = itemView.findViewById(R.id.text_patient_name);
            textPatientInfo = itemView.findViewById(R.id.text_patient_info);
            textLastVisit = itemView.findViewById(R.id.text_last_visit);
            textHistory1 = itemView.findViewById(R.id.text_history_1);
            textHistory2 = itemView.findViewById(R.id.text_history_2);
            btnViewProfile = itemView.findViewById(R.id.btn_view_profile);

            itemView.setOnClickListener(v -> {
                int position = getAdapterPosition();
                if (position != RecyclerView.NO_POSITION && listener != null) {
                    listener.onPatientClick(patients.get(position));
                }
            });

            btnViewProfile.setOnClickListener(v -> {
                int position = getAdapterPosition();
                if (position != RecyclerView.NO_POSITION && listener != null) {
                    listener.onPatientClick(patients.get(position));
                }
            });
        }

        public void bind(User patient) {
            textPatientName.setText(patient.getFullName());

            // Format age and gender
            String age = calculateAge(patient.getDateOfBirth());
            String gender = patient.getGender() != null ? patient.getGender() : "N/A";
            textPatientInfo.setText(age + " years • " + gender);

            // Last visit - use createdAt as fallback
            if (patient.getCreatedAt() > 0) {
                SimpleDateFormat sdf = new SimpleDateFormat("MMM dd, yyyy", Locale.getDefault());
                textLastVisit.setText(sdf.format(new java.util.Date(patient.getCreatedAt())));
            } else {
                textLastVisit.setText("N/A");
            }

            // Medical history - parse from allergies/medications
            String allergies = patient.getAllergies();
            String medications = patient.getMedications();

            if (allergies != null && !allergies.trim().isEmpty()) {
                textHistory1.setText(allergies.length() > 15 ? allergies.substring(0, 15) + "..." : allergies);
                textHistory1.setVisibility(View.VISIBLE);
            } else {
                textHistory1.setVisibility(View.GONE);
            }

            if (medications != null && !medications.trim().isEmpty()) {
                textHistory2.setText(medications.length() > 15 ? medications.substring(0, 15) + "..." : medications);
                textHistory2.setVisibility(View.VISIBLE);
            } else {
                textHistory2.setVisibility(View.GONE);
            }
        }

        private String calculateAge(String dob) {
            if (dob == null || dob.isEmpty()) {
                return "?";
            }
            try {
                String[] parts = dob.split("/");
                if (parts.length == 3) {
                    int year = Integer.parseInt(parts[2]);
                    int currentYear = java.util.Calendar.getInstance().get(java.util.Calendar.YEAR);
                    return String.valueOf(currentYear - year);
                }
            } catch (Exception e) {
                // Ignore parsing errors
            }
            return "?";
        }
    }
}
