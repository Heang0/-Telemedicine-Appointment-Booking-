package com.example.telemedicine;

import android.content.res.ColorStateList;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.card.MaterialCardView;

import java.util.List;

public class TimeSlotAdapter extends RecyclerView.Adapter<TimeSlotAdapter.TimeSlotViewHolder> {
    private List<TimeSlot> timeSlots;
    private OnTimeSlotSelectedListener listener;
    private int selectedPosition = -1;

    public interface OnTimeSlotSelectedListener {
        void onTimeSlotSelected(TimeSlot timeSlot);
    }

    public TimeSlotAdapter(List<TimeSlot> timeSlots, OnTimeSlotSelectedListener listener) {
        this.timeSlots = timeSlots;
        this.listener = listener;
    }

    @NonNull
    @Override
    public TimeSlotViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_time_slot, parent, false);
        return new TimeSlotViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TimeSlotViewHolder holder, int position) {
        TimeSlot timeSlot = timeSlots.get(position);
        holder.bind(timeSlot, position == selectedPosition);
    }

    @Override
    public int getItemCount() {
        return timeSlots.size();
    }

    public void updateTimeSlots(List<TimeSlot> newTimeSlots) {
        this.timeSlots = newTimeSlots;
        selectedPosition = -1;
        notifyDataSetChanged();
    }

    public void setSelectedPosition(int position) {
        int previousPosition = selectedPosition;
        selectedPosition = position;

        if (previousPosition >= 0) notifyItemChanged(previousPosition);
        if (selectedPosition >= 0) notifyItemChanged(selectedPosition);
    }

    class TimeSlotViewHolder extends RecyclerView.ViewHolder {
        private TextView timeText;
        private MaterialCardView cardView;

        public TimeSlotViewHolder(@NonNull View itemView) {
            super(itemView);
            timeText = itemView.findViewById(R.id.text_time_slot);
            cardView = (MaterialCardView) itemView;

            itemView.setOnClickListener(v -> {
                int position = getAdapterPosition();
                if (position != RecyclerView.NO_POSITION) {
                    setSelectedPosition(position);
                    if (listener != null) {
                        listener.onTimeSlotSelected(timeSlots.get(position));
                    }
                }
            });
        }

        public void bind(TimeSlot timeSlot, boolean isSelected) {
            timeText.setText(timeSlot.getTime());

            if (isSelected) {
                cardView.setCardBackgroundColor(ContextCompat.getColor(itemView.getContext(), com.example.telemedicine.R.color.ios_blue));
                timeText.setTextColor(ContextCompat.getColor(itemView.getContext(), com.example.telemedicine.R.color.white));
                cardView.setStrokeWidth(0);
            } else {
                cardView.setCardBackgroundColor(ContextCompat.getColor(itemView.getContext(), com.example.telemedicine.R.color.ios_card_background));
                timeText.setTextColor(ContextCompat.getColor(itemView.getContext(), com.example.telemedicine.R.color.ios_text_primary));
                cardView.setStrokeColor(ContextCompat.getColor(itemView.getContext(), com.example.telemedicine.R.color.ios_divider));
                cardView.setStrokeWidth(1);
            }

            if (timeSlot.isAvailable()) {
                itemView.setEnabled(true);
                timeText.setEnabled(true);
                itemView.setAlpha(1.0f);
            } else {
                itemView.setEnabled(false);
                timeText.setEnabled(false);
                itemView.setAlpha(0.5f);
            }
        }
    }
}