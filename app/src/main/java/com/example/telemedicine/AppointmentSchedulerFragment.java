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

import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class AppointmentSchedulerFragment extends Fragment implements TimeSlotAdapter.OnTimeSlotSelectedListener {
    private static final String ARG_SCHEDULER_MODE = "scheduler_mode";
    private static final String ARG_SELECTED_PROVIDER_ID = "selected_provider_id";
    private static final String ARG_SELECTED_PROVIDER_NAME = "selected_provider_name";

    private CalendarView calendarView;
    private RecyclerView recyclerTimeSlots;
    private TimeSlotAdapter timeSlotAdapter;
    private List<TimeSlot> timeSlots;
    private TextView textSelectedDate;
    private TextView textProviderLabel;
    private Spinner spinnerProvider;
    private RadioGroup radioGroupAppointmentType;
    private Button btnScheduleAppointment;
    private View btnBack;

    private final List<String> providerIds = new ArrayList<>();
    private final List<String> providerNames = new ArrayList<>();
    private final List<String> providerDisplayNames = new ArrayList<>();

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    private String currentUserId = "";
    private String currentUserRole = UserRole.PATIENT.getRoleName();
    private String currentUserName = "";
    private String schedulerMode = "";
    private String selectedDateString = "";
    private TimeSlot selectedTimeSlot = null;
    private String selectedProviderId = "";
    private String selectedProviderName = "";
    private String preselectedProviderId = "";
    private String preselectedProviderName = "";
    private String appointmentType = "";

    public static AppointmentSchedulerFragment newInstance(String schedulerMode) {
        return newInstance(schedulerMode, "", "");
    }

    public static AppointmentSchedulerFragment newInstance(String schedulerMode, String selectedProviderId, String selectedProviderName) {
        AppointmentSchedulerFragment fragment = new AppointmentSchedulerFragment();
        Bundle args = new Bundle();
        args.putString(ARG_SCHEDULER_MODE, schedulerMode);
        args.putString(ARG_SELECTED_PROVIDER_ID, selectedProviderId);
        args.putString(ARG_SELECTED_PROVIDER_NAME, selectedProviderName);
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_appointment_scheduler, container, false);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        initializeViews(view);
        setupCalendar();
        setupTimeSlots();
        setupProviderSpinner();
        setupAppointmentTypeSelection();
        setupScheduleButton();
        readArguments();
        loadCurrentUserProfile();

        return view;
    }

    private void readArguments() {
        Bundle args = getArguments();
        if (args != null) {
            schedulerMode = args.getString(ARG_SCHEDULER_MODE, "");
            preselectedProviderId = args.getString(ARG_SELECTED_PROVIDER_ID, "");
            preselectedProviderName = args.getString(ARG_SELECTED_PROVIDER_NAME, "");
        }
    }

    private void initializeViews(View view) {
        calendarView = view.findViewById(R.id.calendar_view);
        recyclerTimeSlots = view.findViewById(R.id.recycler_time_slots);
        textSelectedDate = view.findViewById(R.id.text_selected_date);
        textProviderLabel = view.findViewById(R.id.text_provider_label);
        spinnerProvider = view.findViewById(R.id.spinner_provider);
        radioGroupAppointmentType = view.findViewById(R.id.radio_group_appointment_type);
        btnScheduleAppointment = view.findViewById(R.id.btn_schedule_appointment);
        btnBack = view.findViewById(R.id.btn_back_schedule);
    }

    private void loadCurrentUserProfile() {
        if (mAuth.getCurrentUser() == null) {
            Toast.makeText(getContext(), "User not authenticated", Toast.LENGTH_SHORT).show();
            return;
        }

        currentUserId = mAuth.getCurrentUser().getUid();
        db.collection("users")
                .document(currentUserId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    User currentUser = documentSnapshot.toObject(User.class);
                    if (currentUser == null) {
                        Toast.makeText(getContext(), "Failed to load user profile", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    currentUserRole = currentUser.getRole() != null ? currentUser.getRole() : UserRole.PATIENT.getRoleName();
                    currentUserName = currentUser.getFullName() != null ? currentUser.getFullName() : "User";

                    if (UserRole.DOCTOR.getRoleName().equalsIgnoreCase(getEffectiveSchedulerMode())) {
                        textProviderLabel.setText("Select Patient");
                        btnScheduleAppointment.setText("Schedule Patient Appointment");
                        loadPatients();
                    } else {
                        textProviderLabel.setText("Select Doctor");
                        btnScheduleAppointment.setText("Schedule Appointment");
                        loadDoctors();
                    }
                })
                .addOnFailureListener(e ->
                        Toast.makeText(getContext(), "Failed to load profile: " + e.getMessage(), Toast.LENGTH_LONG).show());
    }

    private String getEffectiveSchedulerMode() {
        if (schedulerMode != null && !schedulerMode.trim().isEmpty()) {
            return schedulerMode;
        }
        return currentUserRole;
    }

    private void setupCalendar() {
        Calendar today = Calendar.getInstance();
        SimpleDateFormat dateFormat = new SimpleDateFormat("EEEE, MMM dd, yyyy", Locale.getDefault());
        selectedDateString = dateFormat.format(today.getTime());
        textSelectedDate.setText(selectedDateString);

        calendarView.setOnDateChangeListener((view, year, month, dayOfMonth) -> {
            Calendar selectedCalendar = Calendar.getInstance();
            selectedCalendar.set(year, month, dayOfMonth);

            selectedDateString = dateFormat.format(selectedCalendar.getTime());
            textSelectedDate.setText(selectedDateString);
            generateTimeSlotsForDate(selectedCalendar);
        });
    }

    private void setupTimeSlots() {
        recyclerTimeSlots.setLayoutManager(new LinearLayoutManager(getContext()));
        timeSlots = new ArrayList<>();
        timeSlotAdapter = new TimeSlotAdapter(timeSlots, this);
        recyclerTimeSlots.setAdapter(timeSlotAdapter);
        generateTimeSlotsForDate(Calendar.getInstance());
    }

    private void generateTimeSlotsForDate(Calendar selectedDate) {
        timeSlots.clear();

        Calendar now = Calendar.getInstance();
        boolean isToday = isSameDay(selectedDate, now);

        boolean isDoctorScheduler = UserRole.DOCTOR.getRoleName().equalsIgnoreCase(getEffectiveSchedulerMode());
        int startHour = isDoctorScheduler ? 7 : 9;
        int endHour = isDoctorScheduler ? 21 : 17;
        if (isToday) {
            startHour = Math.max(startHour, now.get(Calendar.HOUR_OF_DAY));
            if (startHour >= endHour) {
                Toast.makeText(getContext(), "No more appointments available for today. Please select another date.", Toast.LENGTH_LONG).show();
                timeSlotAdapter.updateTimeSlots(timeSlots);
                return;
            }
        }

        for (int hour = startHour; hour < endHour; hour++) {
            int[] minuteMarks = hour < endHour - 1
                    ? new int[]{0, 15, 30, 45}
                    : new int[]{0, 15, 30};
            for (int minute : minuteMarks) {
                Calendar slotTime = (Calendar) selectedDate.clone();
                slotTime.set(Calendar.HOUR_OF_DAY, hour);
                slotTime.set(Calendar.MINUTE, minute);
                slotTime.set(Calendar.SECOND, 0);
                slotTime.set(Calendar.MILLISECOND, 0);

                if (isToday && slotTime.before(now)) {
                    continue;
                }

                timeSlots.add(new TimeSlot(formatTime(hour, minute), true));
            }
        }

        timeSlotAdapter.updateTimeSlots(timeSlots);
    }

    private boolean isSameDay(Calendar cal1, Calendar cal2) {
        return cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR)
                && cal1.get(Calendar.DAY_OF_YEAR) == cal2.get(Calendar.DAY_OF_YEAR);
    }

    private String formatTime(int hour, int minute) {
        String amPm = hour < 12 ? "AM" : "PM";
        int displayHour = hour % 12;
        if (displayHour == 0) {
            displayHour = 12;
        }
        return String.format(Locale.getDefault(), "%d:%02d %s", displayHour, minute, amPm);
    }

    private void setupProviderSpinner() {
        ArrayAdapter<String> adapter = new ArrayAdapter<>(requireContext(),
                android.R.layout.simple_spinner_item, providerDisplayNames);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerProvider.setAdapter(adapter);

        spinnerProvider.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (position >= 0 && position < providerIds.size()) {
                    selectedProviderId = providerIds.get(position);
                    selectedProviderName = providerNames.get(position);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                selectedProviderId = "";
                selectedProviderName = "";
            }
        });
    }

    private void loadDoctors() {
        db.collection("users")
                .whereEqualTo("role", UserRole.DOCTOR.getRoleName())
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    providerIds.clear();
                    providerNames.clear();
                    providerDisplayNames.clear();

                    for (QueryDocumentSnapshot document : querySnapshot) {
                        User doctor = document.toObject(User.class);
                        if (!doctor.isVerified()) {
                            continue;
                        }
                        if (doctor.getUserId() == null || doctor.getUserId().equals(currentUserId)) {
                            continue;
                        }

                        providerIds.add(doctor.getUserId());
                        String fullName = doctor.getFullName() != null ? doctor.getFullName() : "Doctor";
                        String specialization = doctor.getSpecialization() != null ? doctor.getSpecialization().trim() : "";
                        providerNames.add(fullName);
                        providerDisplayNames.add(specialization.isEmpty()
                                ? fullName
                                : fullName + " (" + specialization + ")");
                    }

                    refreshProviderSpinner(false);
                })
                .addOnFailureListener(e ->
                        Toast.makeText(getContext(), "Failed to load doctors: " + e.getMessage(), Toast.LENGTH_LONG).show());
    }

    private void loadPatients() {
        db.collection("users")
                .whereEqualTo("role", UserRole.PATIENT.getRoleName())
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    providerIds.clear();
                    providerNames.clear();
                    providerDisplayNames.clear();

                    for (QueryDocumentSnapshot document : querySnapshot) {
                        User patient = document.toObject(User.class);
                        if (patient.getUserId() == null || patient.getUserId().equals(currentUserId)) {
                            continue;
                        }

                        providerIds.add(patient.getUserId());
                        String patientName = patient.getFullName() != null ? patient.getFullName() : "Patient";
                        providerNames.add(patientName);
                        providerDisplayNames.add(patientName);
                    }

                    refreshProviderSpinner(true);
                })
                .addOnFailureListener(e ->
                        Toast.makeText(getContext(), "Failed to load patients: " + e.getMessage(), Toast.LENGTH_LONG).show());
    }

    private void refreshProviderSpinner(boolean isPatientPicker) {
        ArrayAdapter<String> adapter = (ArrayAdapter<String>) spinnerProvider.getAdapter();
        if (providerIds.isEmpty()) {
            String placeholder = isPatientPicker ? "No patients available" : "No doctors available";
            providerIds.add("");
            providerNames.add("");
            providerDisplayNames.add(placeholder);
            selectedProviderId = "";
            selectedProviderName = "";
            adapter.notifyDataSetChanged();
        } else {
            adapter.notifyDataSetChanged();
            int selectedIndex = 0;
            if (preselectedProviderId != null && !preselectedProviderId.trim().isEmpty()) {
                int matchedIndex = providerIds.indexOf(preselectedProviderId);
                if (matchedIndex >= 0) {
                    selectedIndex = matchedIndex;
                    selectedProviderId = providerIds.get(matchedIndex);
                    selectedProviderName = providerNames.get(matchedIndex);
                }
            }
            spinnerProvider.setSelection(selectedIndex);
        }
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
        if (btnBack != null) {
            btnBack.setOnClickListener(v -> {
                if (getActivity() != null) {
                    getActivity().getSupportFragmentManager().popBackStack();
                }
            });
        }
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

        if (selectedProviderId == null || selectedProviderId.trim().isEmpty()) {
            Toast.makeText(getContext(), UserRole.DOCTOR.getRoleName().equalsIgnoreCase(currentUserRole)
                    ? "Please select a patient"
                    : "Please select a doctor", Toast.LENGTH_SHORT).show();
            return;
        }

        if (appointmentType.isEmpty()) {
            Toast.makeText(getContext(), "Please select appointment type", Toast.LENGTH_SHORT).show();
            return;
        }

        Date appointmentDate = buildAppointmentDate();
        if (appointmentDate == null) {
            Toast.makeText(getContext(), "Failed to build appointment date", Toast.LENGTH_SHORT).show();
            return;
        }

        if (appointmentDate.before(new Date())) {
            Toast.makeText(getContext(), "Cannot schedule appointment in the past", Toast.LENGTH_SHORT).show();
            return;
        }

        String doctorId;
        String doctorName;
        String patientId;
        String patientName;

        if (UserRole.DOCTOR.getRoleName().equalsIgnoreCase(getEffectiveSchedulerMode())) {
            doctorId = currentUserId;
            doctorName = currentUserName;
            patientId = selectedProviderId;
            patientName = selectedProviderName;
        } else {
            patientId = currentUserId;
            patientName = currentUserName;
            doctorId = selectedProviderId;
            doctorName = selectedProviderName;
        }

        Appointment appointment = new Appointment(
                patientId,
                doctorId,
                patientName,
                doctorName,
                appointmentDate,
                "scheduled",
                "Scheduled via dashboard",
                appointmentType.toLowerCase(Locale.getDefault()).replace(" ", "_")
        );

        db.collection("appointments")
                .add(appointment)
                .addOnSuccessListener(documentReference -> {
                    Map<String, Object> appointmentRef = new HashMap<>();
                    appointmentRef.put("appointmentId", documentReference.getId());
                    appointmentRef.put("status", "scheduled");
                    appointmentRef.put("createdAt", Timestamp.now());

                    db.collection("users")
                            .document(patientId)
                            .collection("appointments")
                            .add(appointmentRef)
                            .addOnCompleteListener(task -> {
                                Toast.makeText(getContext(), "Appointment scheduled successfully", Toast.LENGTH_SHORT).show();
                                if (getActivity() != null) {
                                    getActivity().getSupportFragmentManager().popBackStack();
                                }
                            });
                })
                .addOnFailureListener(e ->
                        Toast.makeText(getContext(), "Failed to schedule appointment: " + e.getMessage(), Toast.LENGTH_LONG).show());
    }

    private Date buildAppointmentDate() {
        try {
            Calendar selectedCalendar = Calendar.getInstance();
            selectedCalendar.setTime(new SimpleDateFormat("EEEE, MMM dd, yyyy", Locale.getDefault()).parse(selectedDateString));

            Date time = new SimpleDateFormat("h:mm a", Locale.getDefault()).parse(selectedTimeSlot.getTime());
            Calendar timeCalendar = Calendar.getInstance();
            timeCalendar.setTime(time);

            selectedCalendar.set(Calendar.HOUR_OF_DAY, timeCalendar.get(Calendar.HOUR_OF_DAY));
            selectedCalendar.set(Calendar.MINUTE, timeCalendar.get(Calendar.MINUTE));
            selectedCalendar.set(Calendar.SECOND, 0);
            selectedCalendar.set(Calendar.MILLISECOND, 0);
            return selectedCalendar.getTime();
        } catch (ParseException e) {
            return null;
        }
    }

    @Override
    public void onTimeSlotSelected(TimeSlot timeSlot) {
        selectedTimeSlot = timeSlot;
        btnScheduleAppointment.setEnabled(true);
        Toast.makeText(getContext(), "Time slot selected: " + timeSlot.getTime(), Toast.LENGTH_SHORT).show();
    }
}
