package com.example.telemedicine;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public class PharmacyLocatorFragment extends Fragment implements PharmacyAdapter.OnPharmacyActionListener {

    private RecyclerView recyclerPharmacies;
    private PharmacyAdapter pharmacyAdapter;
    private List<Pharmacy> pharmacies;
    private EditText editSearchPharmacy;
    private Button btnUseMyLocation;
    private Button btnSearch;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_pharmacy_locator, container, false);

        initializeViews(view);
        setupRecyclerView();
        loadSampleData();
        setupEventListeners();

        return view;
    }

    private void initializeViews(View view) {
        recyclerPharmacies = view.findViewById(R.id.recycler_pharmacies);
        editSearchPharmacy = view.findViewById(R.id.edit_search_pharmacy);
        btnUseMyLocation = view.findViewById(R.id.btn_use_my_location);
        btnSearch = view.findViewById(R.id.btn_search);
    }

    private void setupRecyclerView() {
        recyclerPharmacies.setLayoutManager(new LinearLayoutManager(getContext()));
        pharmacies = new ArrayList<>();
        pharmacyAdapter = new PharmacyAdapter(pharmacies, this);
        recyclerPharmacies.setAdapter(pharmacyAdapter);
    }

    private void loadSampleData() {
        // Sample pharmacy data
        pharmacies.add(new Pharmacy(
            "CVS Pharmacy",
            "123 Main St, New York, NY 10001",
            "0.5 mi",
            "Open until 10:00 PM",
            "(555) 123-4567",
            40.7128,
            -74.0060,
            true
        ));

        pharmacies.add(new Pharmacy(
            "Walgreens",
            "456 Broadway, New York, NY 10002",
            "0.8 mi",
            "Open until 11:00 PM",
            "(555) 234-5678",
            40.7215,
            -74.0050,
            true
        ));

        pharmacies.add(new Pharmacy(
            "Rite Aid",
            "789 Park Ave, New York, NY 10003",
            "1.2 mi",
            "Open until 9:00 PM",
            "(555) 345-6789",
            40.7325,
            -73.9950,
            false
        ));

        pharmacies.add(new Pharmacy(
            "Duane Reade",
            "321 5th Ave, New York, NY 10004",
            "1.5 mi",
            "Open until 12:00 AM",
            "(555) 456-7890",
            40.7415,
            -73.9850,
            true
        ));

        pharmacyAdapter.notifyDataSetChanged();
    }

    private void setupEventListeners() {
        btnUseMyLocation.setOnClickListener(v -> {
            Toast.makeText(getContext(), "Using current location to find nearby pharmacies...", Toast.LENGTH_SHORT).show();
            // In a real app, this would get the user's current location
        });

        btnSearch.setOnClickListener(v -> {
            String searchQuery = editSearchPharmacy.getText().toString().trim();
            if (!searchQuery.isEmpty()) {
                Toast.makeText(getContext(), "Searching for: " + searchQuery, Toast.LENGTH_SHORT).show();
                // In a real app, this would perform a search
            } else {
                Toast.makeText(getContext(), "Please enter a location or pharmacy name", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onDirectionsRequested(Pharmacy pharmacy) {
        Toast.makeText(getContext(), "Getting directions to: " + pharmacy.getName(), Toast.LENGTH_SHORT).show();
        // In a real app, this would open a map with directions
    }

    @Override
    public void onCallPharmacy(Pharmacy pharmacy) {
        Toast.makeText(getContext(), "Calling: " + pharmacy.getPhone(), Toast.LENGTH_SHORT).show();
        // In a real app, this would initiate a phone call
    }
}