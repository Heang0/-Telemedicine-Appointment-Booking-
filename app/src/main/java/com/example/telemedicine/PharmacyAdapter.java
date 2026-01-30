package com.example.telemedicine;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class PharmacyAdapter extends RecyclerView.Adapter<PharmacyAdapter.PharmacyViewHolder> {
    private List<Pharmacy> pharmacies;
    private OnPharmacyActionListener listener;

    public interface OnPharmacyActionListener {
        void onDirectionsRequested(Pharmacy pharmacy);
        void onCallPharmacy(Pharmacy pharmacy);
    }

    public PharmacyAdapter(List<Pharmacy> pharmacies, OnPharmacyActionListener listener) {
        this.pharmacies = pharmacies;
        this.listener = listener;
    }

    @NonNull
    @Override
    public PharmacyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_pharmacy, parent, false);
        return new PharmacyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PharmacyViewHolder holder, int position) {
        Pharmacy pharmacy = pharmacies.get(position);
        holder.bind(pharmacy);
    }

    @Override
    public int getItemCount() {
        return pharmacies.size();
    }

    public void updatePharmacies(List<Pharmacy> newPharmacies) {
        this.pharmacies = newPharmacies;
        notifyDataSetChanged();
    }

    class PharmacyViewHolder extends RecyclerView.ViewHolder {
        private TextView nameText;
        private TextView addressText;
        private TextView distanceText;
        private TextView hoursText;
        private TextView phoneText;
        private TextView statusText;
        private Button btnDirections;
        private Button btnCall;

        public PharmacyViewHolder(@NonNull View itemView) {
            super(itemView);
            
            nameText = itemView.findViewById(R.id.pharmacy_name);
            addressText = itemView.findViewById(R.id.pharmacy_address);
            distanceText = itemView.findViewById(R.id.pharmacy_distance);
            hoursText = itemView.findViewById(R.id.pharmacy_hours);
            phoneText = itemView.findViewById(R.id.pharmacy_phone);
            statusText = itemView.findViewById(R.id.pharmacy_status);
            
            btnDirections = itemView.findViewById(R.id.btn_directions);
            btnCall = itemView.findViewById(R.id.btn_call);

            btnDirections.setOnClickListener(v -> {
                int position = getAdapterPosition();
                if (position != RecyclerView.NO_POSITION && listener != null) {
                    listener.onDirectionsRequested(pharmacies.get(position));
                }
            });

            btnCall.setOnClickListener(v -> {
                int position = getAdapterPosition();
                if (position != RecyclerView.NO_POSITION && listener != null) {
                    listener.onCallPharmacy(pharmacies.get(position));
                }
            });
        }

        public void bind(Pharmacy pharmacy) {
            nameText.setText(pharmacy.getName());
            addressText.setText(pharmacy.getAddress());
            distanceText.setText(pharmacy.getDistance());
            hoursText.setText(pharmacy.getHours());
            phoneText.setText(pharmacy.getPhone());
            
            if (pharmacy.isOpen()) {
                statusText.setText("OPEN NOW");
                statusText.setTextColor(itemView.getResources().getColor(android.R.color.holo_green_dark));
            } else {
                statusText.setText("CLOSED");
                statusText.setTextColor(itemView.getResources().getColor(android.R.color.holo_red_dark));
            }
        }
    }
}