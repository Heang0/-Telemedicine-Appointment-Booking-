package com.example.telemedicine.ui;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.example.telemedicine.R;
import com.example.telemedicine.model.PharmacyLocator;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;

public class PharmacyLocatorActivity extends AppCompatActivity {
    private EditText cityEditText, stateEditText;
    private Button searchButton;
    private ListView pharmaciesListView;
    private TextView titleTextView;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pharmacy_locator);

        cityEditText = findViewById(R.id.editTextCity);
        stateEditText = findViewById(R.id.editTextState);
        searchButton = findViewById(R.id.buttonSearchPharmacies);
        pharmaciesListView = findViewById(R.id.listViewPharmacies);
        titleTextView = findViewById(R.id.textViewPharmacyLocatorTitle);

        db = FirebaseFirestore.getInstance();

        titleTextView.setText("Pharmacy Locator");

        // Set up search button
        searchButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String city = cityEditText.getText().toString().trim();
                String state = stateEditText.getText().toString().trim();

                if (city.isEmpty() || state.isEmpty()) {
                    Toast.makeText(PharmacyLocatorActivity.this, "Please enter city and state", Toast.LENGTH_SHORT).show();
                    return;
                }

                searchPharmacies(city, state);
            }
        });
    }

    private void searchPharmacies(String city, String state) {
        db.collection("pharmacies")
                .whereEqualTo("city", city)
                .whereEqualTo("state", state)
                .addSnapshotListener((querySnapshot, e) -> {
                    if (e != null) {
                        Toast.makeText(PharmacyLocatorActivity.this, "Error searching pharmacies: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        return;
                    }

                    List<PharmacyLocator> pharmacies = new ArrayList<>();
                    if (querySnapshot != null) {
                        for (DocumentSnapshot document : querySnapshot) {
                            PharmacyLocator pharmacy = document.toObject(PharmacyLocator.class);
                            if (pharmacy != null) {
                                pharmacies.add(pharmacy);
                            }
                        }
                    }

                    // Update UI with pharmacies
                    PharmacyAdapter adapter = new PharmacyAdapter(PharmacyLocatorActivity.this, pharmacies);
                    pharmaciesListView.setAdapter(adapter);

                    if (pharmacies.isEmpty()) {
                        Toast.makeText(PharmacyLocatorActivity.this, "No pharmacies found in " + city + ", " + state, Toast.LENGTH_SHORT).show();
                    }
                });
    }
}