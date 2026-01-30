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
            doctorText.setText("Dr. " + item.getTreatingDoctor());
            statusText.setText(item.getStatus().toUpperCase());
            notesText.setText(item.getNotes());
            
            // Color code based on status
            switch (item.getStatus().toLowerCase()) {
                case "active":
                    statusText.setTextColor(itemView.getResources().getColor(android.R.color.holo_red_dark));
                    break;
                case "resolved":
                    statusText.setTextColor(itemView.getResources().getColor(android.R.color.holo_green_dark));
                    break;
                case "chronic":
                    statusText.setTextColor(itemView.getResources().getColor(android.R.color.holo_orange_dark));
                    break;
            }
        }
    }
}