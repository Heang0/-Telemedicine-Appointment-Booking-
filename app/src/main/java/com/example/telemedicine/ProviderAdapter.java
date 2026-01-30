package com.example.telemedicine;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class ProviderAdapter extends RecyclerView.Adapter<ProviderAdapter.ProviderViewHolder> {
    private List<Provider> providers;
    private OnProviderClickListener listener;

    public interface OnProviderClickListener {
        void onProviderClick(Provider provider);
        void onBookAppointmentClick(Provider provider);
    }

    public ProviderAdapter(List<Provider> providers, OnProviderClickListener listener) {
        this.providers = providers;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ProviderViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_provider, parent, false);
        return new ProviderViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ProviderViewHolder holder, int position) {
        Provider provider = providers.get(position);
        holder.bind(provider);
    }

    @Override
    public int getItemCount() {
        return providers.size();
    }

    public void updateProviders(List<Provider> newProviders) {
        this.providers = newProviders;
        notifyDataSetChanged();
    }

    class ProviderViewHolder extends RecyclerView.ViewHolder {
        private ImageView imageView;
        private TextView nameText;
        private TextView specialtyText;
        private TextView locationText;
        private RatingBar ratingBar;
        private TextView languageText;
        private TextView availabilityText;
        private Button bookButton;

        public ProviderViewHolder(@NonNull View itemView) {
            super(itemView);
            
            imageView = itemView.findViewById(R.id.provider_image);
            nameText = itemView.findViewById(R.id.provider_name);
            specialtyText = itemView.findViewById(R.id.provider_specialty);
            locationText = itemView.findViewById(R.id.provider_location);
            ratingBar = itemView.findViewById(R.id.provider_rating);
            languageText = itemView.findViewById(R.id.provider_language);
            availabilityText = itemView.findViewById(R.id.availability_text);
            bookButton = itemView.findViewById(R.id.btn_book_appointment);

            itemView.setOnClickListener(v -> {
                int position = getAdapterPosition();
                if (position != RecyclerView.NO_POSITION && listener != null) {
                    listener.onProviderClick(providers.get(position));
                }
            });

            bookButton.setOnClickListener(v -> {
                int position = getAdapterPosition();
                if (position != RecyclerView.NO_POSITION && listener != null) {
                    listener.onBookAppointmentClick(providers.get(position));
                }
            });
        }

        public void bind(Provider provider) {
            nameText.setText(provider.getName());
            specialtyText.setText(provider.getSpecialty());
            locationText.setText(provider.getLocation());
            ratingBar.setRating(provider.getRating());
            languageText.setText("Language: " + provider.getLanguage());
            
            if (provider.isAvailableNow()) {
                availabilityText.setText("Available Now");
                availabilityText.setTextColor(itemView.getResources().getColor(android.R.color.holo_green_dark));
            } else {
                availabilityText.setText("Not Available");
                availabilityText.setTextColor(itemView.getResources().getColor(android.R.color.holo_red_dark));
            }
        }
    }
}