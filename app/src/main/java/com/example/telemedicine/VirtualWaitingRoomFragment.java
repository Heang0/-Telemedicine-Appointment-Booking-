package com.example.telemedicine;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ToggleButton;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

public class VirtualWaitingRoomFragment extends Fragment {

    private ToggleButton toggleCamera;
    private ToggleButton toggleMicrophone;
    private Button btnTestEquipment;
    private Button btnReviewSymptoms;
    private Button btnEnterConsultation;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_virtual_waiting_room, container, false);

        initializeViews(view);
        setupEventListeners();

        return view;
    }

    private void initializeViews(View view) {
        toggleCamera = view.findViewById(R.id.toggle_camera);
        toggleMicrophone = view.findViewById(R.id.toggle_microphone);
        btnTestEquipment = view.findViewById(R.id.btn_test_equipment);
        btnReviewSymptoms = view.findViewById(R.id.btn_review_symptoms);
        btnEnterConsultation = view.findViewById(R.id.btn_enter_consultation);
    }

    private void setupEventListeners() {
        toggleCamera.setOnCheckedChangeListener((buttonView, isChecked) -> {
            String status = isChecked ? "Camera enabled" : "Camera disabled";
            Toast.makeText(getContext(), status, Toast.LENGTH_SHORT).show();
        });

        toggleMicrophone.setOnCheckedChangeListener((buttonView, isChecked) -> {
            String status = isChecked ? "Microphone enabled" : "Microphone disabled";
            Toast.makeText(getContext(), status, Toast.LENGTH_SHORT).show();
        });

        btnTestEquipment.setOnClickListener(v -> {
            // Simulate equipment testing
            Toast.makeText(getContext(), "Testing camera and microphone...", Toast.LENGTH_SHORT).show();

            // Enable the enter consultation button after testing
            btnEnterConsultation.setEnabled(true);
        });

        btnReviewSymptoms.setOnClickListener(v -> {
            Toast.makeText(getContext(), "Opening symptom review...", Toast.LENGTH_SHORT).show();
        });

        btnEnterConsultation.setOnClickListener(v -> {
            Toast.makeText(getContext(), "Entering consultation with doctor...", Toast.LENGTH_SHORT).show();
        });
    }
}