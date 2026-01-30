package com.example.telemedicine;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.FirebaseFirestore;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

public class AppointmentAdapter extends RecyclerView.Adapter<AppointmentAdapter.AppointmentViewHolder> {
    private List<Appointment> appointments;
    private OnAppointmentClickListener listener;

    public interface OnAppointmentClickListener {
        void onAppointmentClick(Appointment appointment);
    }

    public AppointmentAdapter(List<Appointment> appointments, OnAppointmentClickListener listener) {
        this.appointments = appointments;
        this.listener = listener;
    }

    @NonNull
    @Override
    public AppointmentViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_appointment, parent, false);
        return new AppointmentViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull AppointmentViewHolder holder, int position) {
        Appointment appointment = appointments.get(position);
        holder.bind(appointment);
    }

    @Override
    public int getItemCount() {
        return appointments.size();
    }

    public void updateAppointments(List<Appointment> newAppointments) {
        this.appointments = newAppointments;
        notifyDataSetChanged();
    }

    class AppointmentViewHolder extends RecyclerView.ViewHolder {
        private TextView textPatientName;
        private TextView textDate;
        private TextView textStatus;
        private TextView textReason;
        private Button btnViewDetails;
        private Button btnComplete;

        public AppointmentViewHolder(@NonNull View itemView) {
            super(itemView);
            textPatientName = itemView.findViewById(R.id.text_patient_name);
            textDate = itemView.findViewById(R.id.text_date);
            textStatus = itemView.findViewById(R.id.text_status);
            textReason = itemView.findViewById(R.id.text_reason);
            btnViewDetails = itemView.findViewById(R.id.btn_view_details);
            btnComplete = itemView.findViewById(R.id.btn_complete);

            itemView.setOnClickListener(v -> {
                int position = getAdapterPosition();
                if (position != RecyclerView.NO_POSITION && listener != null) {
                    listener.onAppointmentClick(appointments.get(position));
                }
            });
        }

        public void bind(Appointment appointment) {
            // Show patient name instead of doctor name for doctor view
            textPatientName.setText(appointment.getPatientName());

            SimpleDateFormat dateFormat = new SimpleDateFormat("MMM dd, yyyy 'at' hh:mm a", Locale.getDefault());
            textDate.setText(dateFormat.format(appointment.getAppointmentDate()));

            textStatus.setText(capitalizeFirstLetter(appointment.getStatus()));
            textReason.setText("Reason: " + appointment.getReason());

            // Set status color based on status
            if ("completed".equalsIgnoreCase(appointment.getStatus())) {
                textStatus.setTextColor(itemView.getResources().getColor(android.R.color.holo_green_dark));
            } else if ("cancelled".equalsIgnoreCase(appointment.getStatus())) {
                textStatus.setTextColor(itemView.getResources().getColor(android.R.color.holo_red_dark));
            } else {
                textStatus.setTextColor(itemView.getResources().getColor(android.R.color.holo_blue_dark));
            }

            // Set button click listeners
            btnViewDetails.setOnClickListener(v -> {
                int position = getAdapterPosition();
                if (position != RecyclerView.NO_POSITION && listener != null) {
                    listener.onAppointmentClick(appointments.get(position));
                }
            });

            btnComplete.setOnClickListener(v -> {
                int position = getAdapterPosition();
                if (position != RecyclerView.NO_POSITION) {
                    updateAppointmentStatus(appointments.get(position), "completed");
                }
            });
        }

        private void updateAppointmentStatus(Appointment appointment, String newStatus) {
            if (appointment.getId() == null || appointment.getId().isEmpty()) {
                if (itemView.getContext() != null) {
                    android.widget.Toast.makeText(itemView.getContext(),
                        "Appointment ID is missing",
                        android.widget.Toast.LENGTH_SHORT).show();
                }
                return;
            }

            FirebaseFirestore db = FirebaseFirestore.getInstance();
            db.collection("appointments")
                    .document(appointment.getId())
                    .update("status", newStatus)
                    .addOnSuccessListener(aVoid -> {
                        // Update the local appointment object
                        appointment.setStatus(newStatus);
                        // Update the status text and color
                        textStatus.setText(capitalizeFirstLetter(newStatus));
                        if ("completed".equalsIgnoreCase(newStatus)) {
                            textStatus.setTextColor(itemView.getResources().getColor(android.R.color.holo_green_dark));
                        } else if ("cancelled".equalsIgnoreCase(newStatus)) {
                            textStatus.setTextColor(itemView.getResources().getColor(android.R.color.holo_red_dark));
                        } else {
                            textStatus.setTextColor(itemView.getResources().getColor(android.R.color.holo_blue_dark));
                        }
                        // Notify adapter to refresh the item
                        notifyItemChanged(getAdapterPosition());
                    })
                    .addOnFailureListener(e -> {
                        // Handle error - maybe show a toast
                        if (itemView.getContext() != null) {
                            android.widget.Toast.makeText(itemView.getContext(),
                                "Failed to update appointment status: " + e.getMessage(),
                                android.widget.Toast.LENGTH_SHORT).show();
                        }
                    });
        }

        private String capitalizeFirstLetter(String str) {
            if (str == null || str.isEmpty()) {
                return str;
            }
            return str.substring(0, 1).toUpperCase() + str.substring(1);
        }
    }
}