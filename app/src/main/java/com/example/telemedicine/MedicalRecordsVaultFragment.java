package com.example.telemedicine;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public class MedicalRecordsVaultFragment extends Fragment implements RecordCategoryAdapter.OnCategoryClickListener {

    private RecyclerView recyclerRecordCategories;
    private RecordCategoryAdapter categoryAdapter;
    private List<RecordCategory> categories;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_medical_records_vault, container, false);

        initializeViews(view);
        setupRecyclerView();
        loadSampleData();
        setupButtons();

        return view;
    }

    private void initializeViews(View view) {
        recyclerRecordCategories = view.findViewById(R.id.recycler_record_categories);

        Button btnUnlockRecords = view.findViewById(R.id.btn_unlock_records);
        Button btnShareRecords = view.findViewById(R.id.btn_share_records);
    }

    private void setupRecyclerView() {
        recyclerRecordCategories.setLayoutManager(new LinearLayoutManager(getContext()));
        categories = new ArrayList<>();
        categoryAdapter = new RecordCategoryAdapter(categories, this);
        recyclerRecordCategories.setAdapter(categoryAdapter);
    }

    private void loadSampleData() {
        categories.add(new RecordCategory("Lab Results", "Blood tests, imaging, etc.", 12, R.drawable.ic_document));
        categories.add(new RecordCategory("Immunizations", "Vaccination records", 5, R.drawable.ic_document));
        categories.add(new RecordCategory("Prescriptions", "Current and past medications", 8, R.drawable.ic_document));
        categories.add(new RecordCategory("Visit History", "Past appointments and consultations", 15, R.drawable.ic_document));
        categories.add(new RecordCategory("Allergies", "Known allergies and reactions", 3, R.drawable.ic_document));
        categories.add(new RecordCategory("Conditions", "Diagnosed conditions", 4, R.drawable.ic_document));

        categoryAdapter.notifyDataSetChanged();
    }

    private void setupButtons() {
        Button btnUnlockRecords = getView().findViewById(R.id.btn_unlock_records);
        Button btnShareRecords = getView().findViewById(R.id.btn_share_records);

        if (btnUnlockRecords != null) {
            btnUnlockRecords.setOnClickListener(v -> {
                Toast.makeText(getContext(), "Accessing secure medical records...", Toast.LENGTH_SHORT).show();
            });
        }

        if (btnShareRecords != null) {
            btnShareRecords.setOnClickListener(v -> {
                Toast.makeText(getContext(), "Sharing records with healthcare provider...", Toast.LENGTH_SHORT).show();
            });
        }
    }

    @Override
    public void onCategoryClick(RecordCategory category) {
        Toast.makeText(getContext(), "Viewing: " + category.getTitle(), Toast.LENGTH_SHORT).show();
    }
}