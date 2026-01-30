package com.example.telemedicine;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.RatingBar;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public class FindProviderFragment extends Fragment implements ProviderAdapter.OnProviderClickListener {

    private RecyclerView recyclerView;
    private ProviderAdapter adapter;
    private List<Provider> providers;
    private EditText editSearch;
    private Spinner spinnerSpecialty, spinnerLanguage, spinnerSortBy;
    private RatingBar ratingBarFilter;
    private CheckBox checkAvailableNow;
    private Button btnApplyFilters;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_find_provider, container, false);

        initializeViews(view);
        setupRecyclerView();
        setupSpinners();
        loadSampleData();
        setupFilters();
        setupRatingBar();

        return view;
    }

    private void initializeViews(View view) {
        recyclerView = view.findViewById(R.id.recycler_providers);
        editSearch = view.findViewById(R.id.edit_search);
        spinnerSpecialty = view.findViewById(R.id.spinner_specialty);
        spinnerLanguage = view.findViewById(R.id.spinner_language);
        spinnerSortBy = view.findViewById(R.id.spinner_sort_by);
        ratingBarFilter = view.findViewById(R.id.rating_bar_filter);
        checkAvailableNow = view.findViewById(R.id.check_available_now);
        btnApplyFilters = view.findViewById(R.id.btn_apply_filters);
    }

    private void setupRecyclerView() {
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        providers = new ArrayList<>();
        adapter = new ProviderAdapter(providers, this);
        recyclerView.setAdapter(adapter);
    }

    private void setupSpinners() {
        // Specialty spinner
        String[] specialties = {"All", "Cardiologist", "Dermatologist", "Pediatrician", "Ophthalmologist", "Orthopedic Surgeon", "General Practitioner"};
        ArrayAdapter<String> specialtyAdapter = new ArrayAdapter<>(getContext(),
            android.R.layout.simple_spinner_item, specialties);
        specialtyAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerSpecialty.setAdapter(specialtyAdapter);

        // Language spinner
        String[] languages = {"All", "English", "Spanish", "French", "German", "Mandarin", "Hindi"};
        ArrayAdapter<String> languageAdapter = new ArrayAdapter<>(getContext(),
            android.R.layout.simple_spinner_item, languages);
        languageAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerLanguage.setAdapter(languageAdapter);

        // Sort by spinner
        String[] sortByOptions = {"Recommended", "Rating", "Distance", "Availability"};
        ArrayAdapter<String> sortAdapter = new ArrayAdapter<>(getContext(),
            android.R.layout.simple_spinner_item, sortByOptions);
        sortAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerSortBy.setAdapter(sortAdapter);
    }

    private void loadSampleData() {
        providers.add(new Provider("Dr. John Smith", "Cardiologist", "New York, NY", 4.8f, "English", true, ""));
        providers.add(new Provider("Dr. Sarah Johnson", "Dermatologist", "Los Angeles, CA", 4.6f, "English", false, ""));
        providers.add(new Provider("Dr. Michael Chen", "Pediatrician", "Chicago, IL", 4.9f, "English", true, ""));
        providers.add(new Provider("Dr. Maria Rodriguez", "Ophthalmologist", "Miami, FL", 4.7f, "Spanish", true, ""));
        providers.add(new Provider("Dr. James Wilson", "Orthopedic Surgeon", "Houston, TX", 4.5f, "English", false, ""));
        providers.add(new Provider("Dr. Emily Davis", "General Practitioner", "Boston, MA", 4.7f, "English", true, ""));
        providers.add(new Provider("Dr. Robert Brown", "Neurologist", "Seattle, WA", 4.6f, "English", false, ""));
        providers.add(new Provider("Dr. Lisa Taylor", "Psychiatrist", "Denver, CO", 4.9f, "English", true, ""));
        adapter.notifyDataSetChanged();
    }

    private void setupFilters() {
        btnApplyFilters.setOnClickListener(v -> applyFilters());
    }

    private void setupRatingBar() {
        ratingBarFilter.setOnRatingBarChangeListener((ratingBar, rating, fromUser) -> {
            if (fromUser) {
                // Update the text next to the rating bar to show the selected rating
                View parent = (View) ratingBar.getParent();
                if (parent != null) {
                    // Find the text view next to the rating bar
                    for (int i = 0; i < ((ViewGroup) parent).getChildCount(); i++) {
                        View child = ((ViewGroup) parent).getChildAt(i);
                        if (child instanceof android.widget.TextView) {
                            android.widget.TextView textView = (android.widget.TextView) child;
                            if (textView.getText().toString().equals("Any Rating")) {
                                if (rating > 0) {
                                    textView.setText(rating + "+ Rating");
                                } else {
                                    textView.setText("Any Rating");
                                }
                                break;
                            }
                        }
                    }
                }
            }
        });
    }

    private void applyFilters() {
        String searchTerm = editSearch.getText().toString().toLowerCase();
        String specialty = spinnerSpecialty.getSelectedItem() != null ? spinnerSpecialty.getSelectedItem().toString() : "";
        String language = spinnerLanguage.getSelectedItem() != null ? spinnerLanguage.getSelectedItem().toString() : "";
        float minRating = ratingBarFilter.getRating();
        boolean availableNow = checkAvailableNow.isChecked();

        List<Provider> filteredProviders = new ArrayList<>();
        for (Provider provider : providers) {
            boolean matches = true;

            // Apply search term filter
            if (!searchTerm.isEmpty() &&
                !provider.getName().toLowerCase().contains(searchTerm) &&
                !provider.getSpecialty().toLowerCase().contains(searchTerm)) {
                matches = false;
            }

            // Apply specialty filter
            if (!specialty.isEmpty() && !specialty.equals("All") &&
                !provider.getSpecialty().equals(specialty)) {
                matches = false;
            }

            // Apply language filter
            if (!language.isEmpty() && !language.equals("All") &&
                !provider.getLanguage().equals(language)) {
                matches = false;
            }

            // Apply rating filter
            if (minRating > 0 && provider.getRating() < minRating) {
                matches = false;
            }

            // Apply availability filter
            if (availableNow && !provider.isAvailableNow()) {
                matches = false;
            }

            if (matches) {
                filteredProviders.add(provider);
            }
        }

        // Apply sorting
        String sortBy = spinnerSortBy.getSelectedItem() != null ? spinnerSortBy.getSelectedItem().toString() : "Recommended";
        sortProviders(filteredProviders, sortBy);

        adapter.updateProviders(filteredProviders);
        Toast.makeText(getContext(), "Applied filters. Found " + filteredProviders.size() + " providers", Toast.LENGTH_SHORT).show();
    }

    private void sortProviders(List<Provider> providers, String sortBy) {
        switch (sortBy) {
            case "Rating":
                providers.sort((p1, p2) -> Float.compare(p2.getRating(), p1.getRating()));
                break;
            case "Distance":
                // For demo purposes, we'll just reverse the order
                providers.sort((p1, p2) -> p1.getName().compareTo(p2.getName()));
                break;
            case "Availability":
                providers.sort((p1, p2) -> Boolean.compare(p2.isAvailableNow(), p1.isAvailableNow()));
                break;
            case "Recommended":
            default:
                // Default order (no sorting needed)
                break;
        }
    }

    @Override
    public void onProviderClick(Provider provider) {
        // Show provider details
        Toast.makeText(getContext(), "Selected: " + provider.getName(), Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onBookAppointmentClick(Provider provider) {
        // Navigate to appointment booking
        Toast.makeText(getContext(), "Booking appointment with: " + provider.getName(), Toast.LENGTH_SHORT).show();
    }
}