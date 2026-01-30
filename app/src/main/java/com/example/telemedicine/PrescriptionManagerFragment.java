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

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class PrescriptionManagerFragment extends Fragment implements PrescriptionAdapter.OnPrescriptionClickListener {

    private RecyclerView recyclerPrescriptions;
    private Button btnNewPrescription;
    private PrescriptionAdapter adapter;
    private List<Prescription> prescriptions;

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private com.google.firebase.firestore.ListenerRegistration prescriptionsListenerRegistration;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_prescription_manager, container, false);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        initializeViews(view);
        setupRecyclerView();
        loadPrescriptions();

        btnNewPrescription.setOnClickListener(v -> {
            // Navigate to create new prescription
            if (getActivity() != null) {
                getActivity().getSupportFragmentManager()
                        .beginTransaction()
                        .replace(R.id.fragment_container, new DigitalPrescriptionPadFragment())
                        .addToBackStack(null)
                        .commit();
            }
        });

        return view;
    }

    private void initializeViews(View view) {
        recyclerPrescriptions = view.findViewById(R.id.recycler_prescriptions);
        btnNewPrescription = view.findViewById(R.id.btn_new_prescription);
    }

    private void setupRecyclerView() {
        prescriptions = new ArrayList<>();
        adapter = new PrescriptionAdapter(prescriptions, this);
        recyclerPrescriptions.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerPrescriptions.setAdapter(adapter);
    }

    private void loadPrescriptions() {
        String doctorId = mAuth.getCurrentUser().getUid();

        // Remove previous listener if exists
        if (prescriptionsListenerRegistration != null) {
            prescriptionsListenerRegistration.remove();
        }

        prescriptionsListenerRegistration = db.collection("prescriptions")
                .whereEqualTo("doctorId", doctorId)
                .orderBy("createdAt", com.google.firebase.firestore.Query.Direction.DESCENDING)
                .addSnapshotListener((querySnapshot, error) -> {
                    if (error != null) {
                        android.util.Log.e("PrescriptionManager", "Error loading prescriptions", error);
                        Toast.makeText(getContext(), "Error loading prescriptions: " + error.getMessage(),
                                Toast.LENGTH_LONG).show();
                        return;
                    }

                    if (querySnapshot != null) {
                        prescriptions.clear();
                        for (QueryDocumentSnapshot document : querySnapshot) {
                            Prescription prescription = document.toObject(Prescription.class);
                            prescription.setId(document.getId());

                            // Update the adapter with the prescription data
                            prescriptions.add(prescription);
                        }

                        adapter.updatePrescriptions(prescriptions);
                    }
                });
    }

    @Override
    public void onPrescriptionClick(Prescription prescription) {
        // Handle prescription click - show details
        if (getActivity() != null) {
            Bundle bundle = new Bundle();
            bundle.putString("prescription_id", prescription.getId());

            PrescriptionDetailsFragment detailsFragment = new PrescriptionDetailsFragment();
            detailsFragment.setArguments(bundle);

            getActivity().getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.fragment_container, detailsFragment)
                    .addToBackStack(null)
                    .commit();
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        // Clean up the listener to prevent memory leaks
        if (prescriptionsListenerRegistration != null) {
            prescriptionsListenerRegistration.remove();
        }
    }
}