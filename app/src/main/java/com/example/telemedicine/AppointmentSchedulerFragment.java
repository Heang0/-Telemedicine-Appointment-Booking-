package com.example.telemedicine;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CalendarView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class AppointmentSchedulerFragment extends Fragment implements TimeSlotAdapter.OnTimeSlotSelectedListener {

    private CalendarView calendarView;
    private RecyclerView recyclerTimeSlots;
    private TimeSlotAdapter timeSlotAdapter;
    private List<TimeSlot> timeSlots;
    private TextView textSelectedDate;
    private Spinner spinnerProvider;
    private RadioGroup radioGroupAppointmentType;
    private Button btnScheduleAppointment;

    private String selectedDateString = "";
    private TimeSlot selectedTimeSlot = null;
    private String selectedProvider = "";
    private String appointmentType = "";

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_appointment_scheduler, container, false);

        initializeViews(view);
        setupCalendar();
        setupTimeSlots();
        setupProviderSpinner();
        setupAppointmentTypeSelection();
        setupScheduleButton();

        return view;
    }

    private void initializeViews(View view) {
        calendarView = view.findViewById(R.id.calendar_view);
        recyclerTimeSlots = view.findViewById(R.id.recycler_time_slots);
        textSelectedDate = view.findViewById(R.id.text_selected_date);
        spinnerProvider = view.findViewById(R.id.spinner_provider);
        radioGroupAppointmentType = view.findViewById(R.id.radio_group_appointment_type);
        btnScheduleAppointment = view.findViewById(R.id.btn_schedule_appointment);
    }

    private void setupCalendar() {
        calendarView.setOnDateChangeListener((view, year, month, dayOfMonth) -> {
            Calendar selectedCalendar = Calendar.getInstance();
            selectedCalendar.set(year, month, dayOfMonth);

            SimpleDateFormat dateFormat = new SimpleDateFormat("EEEE, MMM dd, yyyy", Locale.getDefault());
            selectedDateString = dateFormat.format(selectedCalendar.getTime());
            textSelectedDate.setText(selectedDateString);

            // Generate time slots for the selected date
            generateTimeSlotsForDate(selectedCalendar);
        });
    }

    private void setupTimeSlots() {
        recyclerTimeSlots.setLayoutManager(new LinearLayoutManager(getContext()));
        timeSlots = new ArrayList<>();
        timeSlotAdapter = new TimeSlotAdapter(timeSlots, this);
        recyclerTimeSlots.setAdapter(timeSlotAdapter);

        // Initially show today's time slots
        generateTimeSlotsForDate(Calendar.getInstance());
    }

    private void generateTimeSlotsForDate(Calendar selectedDate) {
        timeSlots.clear();

        // Check if selected date is today - if so, start from current hour
        Calendar now = Calendar.getInstance();
        boolean isToday = isSameDay(selectedDate, now);

        int startHour = 9; // 9 AM
        if (isToday) {
            startHour = now.get(Calendar.HOUR_OF_DAY);
            // If it's past 5 PM, show next available day
            if (startHour >= 17) {
                Toast.makeText(getContext(), "No more appointments available for today. Please select another date.", Toast.LENGTH_LONG).show();
                return;
            }
        }

        // Generate time slots from 9 AM to 5 PM in 30-minute intervals
        for (int hour = startHour; hour < 17; hour++) {
            // Add :00 slot
            String timeString = String.format(Locale.getDefault(), "%02d:00", hour);
            timeSlots.add(new TimeSlot(formatTime(hour, 0), true));

            // Add :30 slot (but not for 5 PM)
            if (hour < 16) {
                timeSlots.add(new TimeSlot(formatTime(hour, 30), true));
            }
        }

        timeSlotAdapter.updateTimeSlots(timeSlots);
    }

    private boolean isSameDay(Calendar cal1, Calendar cal2) {
        return cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR) &&
               cal1.get(Calendar.DAY_OF_YEAR) == cal2.get(Calendar.DAY_OF_YEAR);
    }

    private String formatTime(int hour, int minute) {
        String amPm = hour < 12 ? "AM" : "PM";
        int displayHour = hour % 12;
        if (displayHour == 0) displayHour = 12;
        return String.format(Locale.getDefault(), "%d:%02d %s", displayHour, minute, amPm);
    }

    private void setupProviderSpinner() {
        // Sample providers data
        List<String> providers = new ArrayList<>();
        providers.add("Dr. John Smith - Cardiologist");
        providers.add("Dr. Sarah Johnson - Dermatologist");
        providers.add("Dr. Michael Chen - Pediatrician");
        providers.add("Dr. Maria Rodriguez - Ophthalmologist");
        providers.add("Dr. James Wilson - Orthopedic Surgeon");

        ArrayAdapter<String> adapter = new ArrayAdapter<>(getContext(),
            android.R.layout.simple_spinner_item, providers);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerProvider.setAdapter(adapter);

        spinnerProvider.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                selectedProvider = (String) parent.getItemAtPosition(position);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                selectedProvider = "";
            }
        });
    }

    private void setupAppointmentTypeSelection() {
        radioGroupAppointmentType.setOnCheckedChangeListener((group, checkedId) -> {
            RadioButton selectedRadioButton = group.findViewById(checkedId);
            if (selectedRadioButton != null) {
                appointmentType = selectedRadioButton.getText().toString();
            }
        });
    }

    private void setupScheduleButton() {
        btnScheduleAppointment.setOnClickListener(v -> scheduleAppointment());
    }

    private void scheduleAppointment() {
        if (selectedDateString.isEmpty()) {
            Toast.makeText(getContext(), "Please select a date", Toast.LENGTH_SHORT).show();
            return;
        }

        if (selectedTimeSlot == null) {
            Toast.makeText(getContext(), "Please select a time slot", Toast.LENGTH_SHORT).show();
            return;
        }

        if (selectedProvider.isEmpty()) {
            Toast.makeText(getContext(), "Please select a provider", Toast.LENGTH_SHORT).show();
            return;
        }

        if (appointmentType.isEmpty()) {
            Toast.makeText(getContext(), "Please select appointment type", Toast.LENGTH_SHORT).show();
            return;
        }

        // In a real app, this would connect to a backend service
        String appointmentDetails = String.format(
            "Appointment scheduled!\n\nProvider: %s\nDate: %s\nTime: %s\nType: %s",
            selectedProvider, selectedDateString, selectedTimeSlot.getTime(), appointmentType
        );

        Toast.makeText(getContext(), appointmentDetails, Toast.LENGTH_LONG).show();
    }

    @Override
    public void onTimeSlotSelected(TimeSlot timeSlot) {
        this.selectedTimeSlot = timeSlot;
        btnScheduleAppointment.setEnabled(true);
        Toast.makeText(getContext(), "Time slot selected: " + timeSlot.getTime(), Toast.LENGTH_SHORT).show();
    }
}