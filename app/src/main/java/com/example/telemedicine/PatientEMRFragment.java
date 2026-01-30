package com.example.telemedicine;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public class PatientEMRFragment extends Fragment {

    private RecyclerView recyclerMedicalHistory;
    private MedicalHistoryAdapter historyAdapter;
    private List<MedicalHistoryItem> historyItems;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_patient_emr, container, false);

        initializeViews(view);
        setupRecyclerView();
        loadSampleData();
        populatePatientInfo();

        return view;
    }

    private void initializeViews(View view) {
        recyclerMedicalHistory = view.findViewById(R.id.recycler_medical_history);
    }

    private void setupRecyclerView() {
        recyclerMedicalHistory.setLayoutManager(new LinearLayoutManager(getContext()));
        historyItems = new ArrayList<>();
        historyAdapter = new MedicalHistoryAdapter(historyItems);
        recyclerMedicalHistory.setAdapter(historyAdapter);
    }

    private void loadSampleData() {
        historyItems.add(new MedicalHistoryItem(
            "Hypertension",
            "Jan 15, 2020",
            "Smith",
            "active",
            "Patient has been managing with medication and lifestyle changes."
        ));

        historyItems.add(new MedicalHistoryItem(
            "Type 2 Diabetes",
            "Mar 22, 2019",
            "Johnson",
            "chronic",
            "Diagnosed early stage, managed with diet and metformin."
        ));

        historyItems.add(new MedicalHistoryItem(
            "Seasonal Allergies",
            "Apr 10, 2018",
            "Rodriguez",
            "resolved",
            "Patient responded well to antihistamines."
        ));

        historyItems.add(new MedicalHistoryItem(
            "Anxiety Disorder",
            "Sep 5, 2021",
            "Chen",
            "active",
            "Currently being treated with therapy and medication."
        ));

        historyAdapter.notifyDataSetChanged();
    }

    private void populatePatientInfo() {
        TextView textPatientName = getView().findViewById(R.id.text_patient_name);
        TextView textPatientDob = getView().findViewById(R.id.text_patient_dob);
        TextView textPatientGender = getView().findViewById(R.id.text_patient_gender);
        TextView textPatientMrn = getView().findViewById(R.id.text_patient_mrn);
        TextView textPrimaryCare = getView().findViewById(R.id.text_primary_care);

        if (textPatientName != null) textPatientName.setText("John Doe");
        if (textPatientDob != null) textPatientDob.setText("01/15/1980");
        if (textPatientGender != null) textPatientGender.setText("Male");
        if (textPatientMrn != null) textPatientMrn.setText("MRN123456789");
        if (textPrimaryCare != null) textPrimaryCare.setText("Dr. Sarah Smith");
    }
}