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

public class AppointmentAdapterIOS extends RecyclerView.Adapter<AppointmentAdapterIOS.AppointmentViewHolder> {
    private List<Appointment> appointments;
    private OnAppointmentClickListener listener;

    public interface OnAppointmentClickListener {
        void onAppointmentClick(Appointment appointment);
    }

    public AppointmentAdapterIOS(List<Appointment> appointments, OnAppointmentClickListener listener) {
        this.appointments = appointments;
        this.listener = listener;
    }

    @NonNull
    @Override
    public AppointmentViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_appointment_ios, parent, false);
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
        private TextView textAppointmentTime;
        private TextView textAppointmentAmpm;
        private TextView textAppointmentType;
        private TextView textAppointmentMode;
        private TextView textAppointmentStatus;

        public AppointmentViewHolder(@NonNull View itemView) {
            super(itemView);
            textPatientName = itemView.findViewById(R.id.text_patient_name);
            textAppointmentTime = itemView.findViewById(R.id.text_appointment_time);
            textAppointmentAmpm = itemView.findViewById(R.id.text_appointment_ampm);
            textAppointmentType = itemView.findViewById(R.id.text_appointment_type);
            textAppointmentMode = itemView.findViewById(R.id.text_appointment_mode);
            textAppointmentStatus = itemView.findViewById(R.id.text_appointment_status);

            itemView.setOnClickListener(v -> {
                int position = getAdapterPosition();
                if (position != RecyclerView.NO_POSITION && listener != null) {
                    listener.onAppointmentClick(appointments.get(position));
                }
            });
        }

        public void bind(Appointment appointment) {
            // Set patient name
            textPatientName.setText(appointment.getPatientName());

            // Set appointment type
            textAppointmentType.setText(appointment.getReason() != null ? appointment.getReason() : "Consultation");

            // Set appointment mode
            String type = appointment.getConsultationType();
            if (type != null) {
                if (type.equals("video")) {
                    textAppointmentMode.setText("Video Call");
                } else if (type.equals("in_person")) {
                    textAppointmentMode.setText("In-Person");
                } else if (type.equals("chat")) {
                    textAppointmentMode.setText("Chat");
                } else {
                    textAppointmentMode.setText(type);
                }
            } else {
                textAppointmentMode.setText("Video Call");
            }

            // Set time
            SimpleDateFormat timeFormat = new SimpleDateFormat("hh:mm", Locale.getDefault());
            SimpleDateFormat ampmFormat = new SimpleDateFormat("a", Locale.getDefault());
            if (appointment.getAppointmentDate() != null) {
                textAppointmentTime.setText(timeFormat.format(appointment.getAppointmentDate()));
                textAppointmentAmpm.setText(ampmFormat.format(appointment.getAppointmentDate()));
            } else {
                textAppointmentTime.setText("--:--");
                textAppointmentAmpm.setText("--");
            }

            // Set status with iOS-style color
            String status = appointment.getStatus();
            textAppointmentStatus.setText(capitalizeFirstLetter(status != null ? status : "Scheduled"));
            
            // Set status background color based on status
            int statusColor;
            if ("completed".equalsIgnoreCase(status)) {
                statusColor = 0xFFE8F8ED; // Green tint
            } else if ("cancelled".equalsIgnoreCase(status)) {
                statusColor = 0xFFFFEBE8; // Red tint
            } else if ("in_progress".equalsIgnoreCase(status)) {
                statusColor = 0xFFFFF4E8; // Orange tint
            } else {
                statusColor = 0xFFE8F4FF; // Blue tint (default)
            }
            itemView.setBackgroundResource(android.R.color.transparent);
            textAppointmentStatus.setTextColor(statusColor == 0xFFE8F4FF ? 
                itemView.getContext().getResources().getColor(R.color.ios_blue) :
                itemView.getContext().getResources().getColor(R.color.ios_orange));
        }

        private String capitalizeFirstLetter(String str) {
            if (str == null || str.isEmpty()) {
                return str;
            }
            return str.substring(0, 1).toUpperCase() + str.substring(1).toLowerCase();
        }
    }
}
