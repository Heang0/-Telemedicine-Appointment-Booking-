package com.example.telemedicine;

public class TimeSlot {
    private String time;
    private boolean available;
    private boolean selected;

    public TimeSlot(String time, boolean available) {
        this.time = time;
        this.available = available;
        this.selected = false;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public boolean isAvailable() {
        return available;
    }

    public void setAvailable(boolean available) {
        this.available = available;
    }

    public boolean isSelected() {
        return selected;
    }

    public void setSelected(boolean selected) {
        this.selected = selected;
    }
}