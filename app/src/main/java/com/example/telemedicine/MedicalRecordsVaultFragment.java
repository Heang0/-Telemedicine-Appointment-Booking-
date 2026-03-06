package com.example.telemedicine;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;

public class MedicalRecordsVaultFragment extends Fragment implements RecordCategoryAdapter.OnCategoryClickListener {

    private RecyclerView recyclerRecordCategories;
    private RecordCategoryAdapter categoryAdapter;
    private List<RecordCategory> categories;
    private View btnUnlockRecords;
    private View btnShareRecords;

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private String currentUserId;
    private String currentUserRole = UserRole.PATIENT.getRoleName();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_medical_records_vault_ios, container, false);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        currentUserId = mAuth.getCurrentUser() != null ? mAuth.getCurrentUser().getUid() : null;

        initializeViews(view);
        setupRecyclerView();
        setupButtons();
        loadDynamicCategories();

        return view;
    }

    private void initializeViews(View view) {
        recyclerRecordCategories = view.findViewById(R.id.recycler_record_categories);
        btnUnlockRecords = view.findViewById(R.id.btn_unlock_records);
        btnShareRecords = view.findViewById(R.id.btn_share_records);
    }

    private void setupRecyclerView() {
        recyclerRecordCategories.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerRecordCategories.setNestedScrollingEnabled(false);
        categories = new ArrayList<>();
        categoryAdapter = new RecordCategoryAdapter(categories, this);
        recyclerRecordCategories.setAdapter(categoryAdapter);
    }

    private void loadDynamicCategories() {
        if (currentUserId == null) {
            Toast.makeText(getContext(), "User not authenticated", Toast.LENGTH_SHORT).show();
            return;
        }

        db.collection("users")
                .document(currentUserId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    User user = documentSnapshot.toObject(User.class);
                    if (user != null && user.getRole() != null && !user.getRole().trim().isEmpty()) {
                        currentUserRole = user.getRole();
                    }
                    seedCategories(user);
                    loadFirestoreCounts();
                })
                .addOnFailureListener(e -> {
                    seedCategories(null);
                    loadFirestoreCounts();
                });
    }

    private void seedCategories(User user) {
        categories.clear();
        categories.add(new RecordCategory("Lab Results", "Results synced from secure records", 0, R.drawable.ic_document));
        categories.add(new RecordCategory("Immunizations", "Vaccination and immunity status", 0, R.drawable.ic_document));
        categories.add(new RecordCategory("Prescriptions", "Current and previous medication plans", 0, R.drawable.ic_document));
        categories.add(new RecordCategory("Visit History", "Completed, upcoming, and cancelled visits", 0, R.drawable.ic_document));
        categories.add(new RecordCategory("Allergies", "Known allergies and sensitivities", splitCount(user != null ? user.getAllergies() : null), R.drawable.ic_document));
        categories.add(new RecordCategory("Conditions", "Diagnosed and monitored conditions", splitCount(user != null ? user.getMedicalConditions() : null), R.drawable.ic_document));
        categoryAdapter.updateCategories(new ArrayList<>(categories));
    }

    private void loadFirestoreCounts() {
        String userField = UserRole.DOCTOR.getRoleName().equalsIgnoreCase(currentUserRole) ? "doctorId" : "patientId";

        db.collection("appointments")
                .whereEqualTo(userField, currentUserId)
                .get()
                .addOnSuccessListener(querySnapshot -> updateCategoryCount("Visit History", querySnapshot.size()));

        db.collection("prescriptions")
                .whereEqualTo(userField, currentUserId)
                .get()
                .addOnSuccessListener(querySnapshot -> updateCategoryCount("Prescriptions", querySnapshot.size()));

        if (!UserRole.DOCTOR.getRoleName().equalsIgnoreCase(currentUserRole)) {
            db.collection("medical_records")
                    .whereEqualTo("patientId", currentUserId)
                    .whereEqualTo("recordType", "lab_result")
                    .get()
                    .addOnSuccessListener(querySnapshot -> updateCategoryCount("Lab Results", querySnapshot.size()));

            db.collection("medical_records")
                    .whereEqualTo("patientId", currentUserId)
                    .whereEqualTo("recordType", "immunization")
                    .get()
                    .addOnSuccessListener(querySnapshot -> updateCategoryCount("Immunizations", querySnapshot.size()));
        }
    }

    private void updateCategoryCount(String title, int count) {
        for (RecordCategory category : categories) {
            if (title.equalsIgnoreCase(category.getTitle())) {
                category.setRecordCount(count);
                break;
            }
        }
        categoryAdapter.updateCategories(new ArrayList<>(categories));
    }

    private int splitCount(String rawValue) {
        if (rawValue == null || rawValue.trim().isEmpty()) {
            return 0;
        }
        String[] parts = rawValue.split("[,\n]");
        int count = 0;
        for (String part : parts) {
            if (!part.trim().isEmpty()) {
                count++;
            }
        }
        return count;
    }

    private void setupButtons() {
        if (btnUnlockRecords != null) {
            btnUnlockRecords.setOnClickListener(v -> {
                Toast.makeText(getContext(), "Secure records synced successfully", Toast.LENGTH_SHORT).show();
            });
        }

        if (btnShareRecords != null) {
            btnShareRecords.setOnClickListener(v -> {
                Toast.makeText(getContext(), "Sharing tools will be available in the next update", Toast.LENGTH_SHORT).show();
            });
        }
    }

    @Override
    public void onCategoryClick(RecordCategory category) {
        if (getActivity() == null) {
            return;
        }

        Bundle bundle = new Bundle();
        bundle.putString("record_filter", category.getTitle());

        PatientEMRFragment fragment = new PatientEMRFragment();
        fragment.setArguments(bundle);

        getActivity().getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.fragment_container, fragment)
                .addToBackStack(null)
                .commit();
    }
}
