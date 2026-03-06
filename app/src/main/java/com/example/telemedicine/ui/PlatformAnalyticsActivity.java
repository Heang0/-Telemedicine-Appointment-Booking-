package com.example.telemedicine.ui;

import android.os.Bundle;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.example.telemedicine.R;
import com.example.telemedicine.model.PlatformAnalytics;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;

public class PlatformAnalyticsActivity extends AppCompatActivity {
    private ListView analyticsListView;
    private TextView titleTextView;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_platform_analytics);

        analyticsListView = findViewById(R.id.listViewAnalytics);
        titleTextView = findViewById(R.id.textViewAnalyticsTitle);
        db = FirebaseFirestore.getInstance();

        titleTextView.setText("Platform Analytics");

        // Load analytics data
        loadAnalytics();
    }

    private void loadAnalytics() {
        db.collection("platform_analytics")
                .addSnapshotListener((querySnapshot, e) -> {
                    if (e != null) {
                        Toast.makeText(PlatformAnalyticsActivity.this, "Error loading analytics: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        return;
                    }

                    List<PlatformAnalytics> analytics = new ArrayList<>();
                    if (querySnapshot != null) {
                        for (DocumentSnapshot document : querySnapshot) {
                            PlatformAnalytics analytic = document.toObject(PlatformAnalytics.class);
                            if (analytic != null) {
                                analytics.add(analytic);
                            }
                        }
                    }

                    // Update UI with analytics
                    AnalyticsAdapter adapter = new AnalyticsAdapter(PlatformAnalyticsActivity.this, analytics);
                    analyticsListView.setAdapter(adapter);
                });
    }
}