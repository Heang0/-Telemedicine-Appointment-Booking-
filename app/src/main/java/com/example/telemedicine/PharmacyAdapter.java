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

            nameText = itemView.findViewById(R.id.textViewPharmacyName);
            addressText = itemView.findViewById(R.id.textViewPharmacyAddress);
            phoneText = itemView.findViewById(R.id.textViewPharmacyPhone);
            // Optional: initialize others only if layout is updated later
            distanceText = itemView.findViewById(R.id.textViewPharmacyDistance); // placeholder — will be null if not in layout
            hoursText = itemView.findViewById(R.id.textViewPharmacyHours);
            statusText = itemView.findViewById(R.id.textViewPharmacyStatus);
            btnDirections = itemView.findViewById(R.id.btnDirections);
            btnCall = itemView.findViewById(R.id.btnCall);

            // Set click listeners only if buttons exist
            if (btnDirections != null) {
                btnDirections.setOnClickListener(v -> {
                    int position = getAdapterPosition();
                    if (position != RecyclerView.NO_POSITION && listener != null) {
                        listener.onDirectionsRequested(pharmacies.get(position));
                    }
                });
            }
            if (btnCall != null) {
                btnCall.setOnClickListener(v -> {
                    int position = getAdapterPosition();
                    if (position != RecyclerView.NO_POSITION && listener != null) {
                        listener.onCallPharmacy(pharmacies.get(position));
                    }
                });
            }
        }

        public void bind(Pharmacy pharmacy) {
            nameText.setText(pharmacy.getName());
            addressText.setText(pharmacy.getAddress());
            phoneText.setText(pharmacy.getPhone());

            if (distanceText != null) {
                distanceText.setText(pharmacy.getDistance());
            }
            if (hoursText != null) {
                hoursText.setText(pharmacy.getHours());
            }
            if (statusText != null) {
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
}