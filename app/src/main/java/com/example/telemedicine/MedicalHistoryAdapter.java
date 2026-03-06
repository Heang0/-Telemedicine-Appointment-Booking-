package com.example.telemedicine;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class MedicalHistoryAdapter extends RecyclerView.Adapter<MedicalHistoryAdapter.MedicalHistoryViewHolder> {
    private List<MedicalHistoryItem> historyItems;

    public MedicalHistoryAdapter(List<MedicalHistoryItem> historyItems) {
        this.historyItems = historyItems;
    }

    @NonNull
    @Override
    public MedicalHistoryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_medical_history, parent, false);
        return new MedicalHistoryViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MedicalHistoryViewHolder holder, int position) {
        MedicalHistoryItem item = historyItems.get(position);
        holder.bind(item);
    }

    @Override
    public int getItemCount() {
        return historyItems.size();
    }

    public void updateHistoryItems(List<MedicalHistoryItem> newHistoryItems) {
        this.historyItems = newHistoryItems;
        notifyDataSetChanged();
    }

    class MedicalHistoryViewHolder extends RecyclerView.ViewHolder {
        private TextView conditionText;
        private TextView dateText;
        private TextView doctorText;
        private TextView statusText;
        private TextView notesText;

        public MedicalHistoryViewHolder(@NonNull View itemView) {
            super(itemView);
            
            conditionText = itemView.findViewById(R.id.history_condition);
            dateText = itemView.findViewById(R.id.history_date);
            doctorText = itemView.findViewById(R.id.history_doctor);
            statusText = itemView.findViewById(R.id.history_status);
            notesText = itemView.findViewById(R.id.history_notes);
        }

        public void bind(MedicalHistoryItem item) {
            conditionText.setText(item.getCondition());
            dateText.setText("Diagnosed: " + item.getDiagnosisDate());
            String clinician = item.getTreatingDoctor() != null ? item.getTreatingDoctor().trim() : "";
            if (clinician.isEmpty()) {
                doctorText.setText("Care team");
            } else if (clinician.startsWith("Dr.") || "Profile".equalsIgnoreCase(clinician) || "Health Records".equalsIgnoreCase(clinician)) {
                doctorText.setText(clinician);
            } else {
                doctorText.setText("Dr. " + clinician);
            }
            String status = item.getStatus() != null ? item.getStatus() : "active";
            statusText.setText(status.toUpperCase());
            notesText.setText(item.getNotes() != null ? item.getNotes() : "No notes available");
            
            // Color code based on status
            switch (status.toLowerCase()) {
                case "active":
                case "scheduled":
                case "pending":
                    statusText.setTextColor(itemView.getResources().getColor(android.R.color.holo_red_dark));
                    break;
                case "resolved":
                case "fulfilled":
                case "completed":
                    statusText.setTextColor(itemView.getResources().getColor(android.R.color.holo_green_dark));
                    break;
                case "chronic":
                case "cancelled":
                case "expired":
                    statusText.setTextColor(itemView.getResources().getColor(android.R.color.holo_orange_dark));
                    break;
                default:
                    statusText.setTextColor(itemView.getResources().getColor(android.R.color.holo_blue_dark));
                    break;
            }
        }
    }
}
