package com.example.telemedicine;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class TimeSlotAdapter extends RecyclerView.Adapter<TimeSlotAdapter.TimeSlotViewHolder> {
    private List<TimeSlot> timeSlots;
    private OnTimeSlotSelectedListener listener;
    private int selectedPosition = -1; // Track selected position

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
        private CheckBox checkBox;

        public TimeSlotViewHolder(@NonNull View itemView) {
            super(itemView);
            timeText = itemView.findViewById(R.id.text_time_slot);
            checkBox = itemView.findViewById(R.id.check_time_selected);

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
            checkBox.setChecked(isSelected);
            
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