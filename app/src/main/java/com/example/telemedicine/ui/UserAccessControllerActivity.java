package com.example.telemedicine.ui;

import android.os.Bundle;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.example.telemedicine.R;
import com.example.telemedicine.model.UserAccessController;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;

public class UserAccessControllerActivity extends AppCompatActivity {
    private ListView accessControlsListView;
    private TextView titleTextView;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_access_controller);

        accessControlsListView = findViewById(R.id.listViewAccessControls);
        titleTextView = findViewById(R.id.textViewAccessControllerTitle);
        db = FirebaseFirestore.getInstance();

        titleTextView.setText("User Access Control");

        // Load access controls
        loadAccessControls();
    }

    private void loadAccessControls() {
        db.collection("user_access_controls")
                .addSnapshotListener((querySnapshot, e) -> {
                    if (e != null) {
                        Toast.makeText(UserAccessControllerActivity.this, "Error loading access controls: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        return;
                    }

                    List<UserAccessController> controls = new ArrayList<>();
                    if (querySnapshot != null) {
                        for (DocumentSnapshot document : querySnapshot) {
                            UserAccessController control = document.toObject(UserAccessController.class);
                            if (control != null) {
                                controls.add(control);
                            }
                        }
                    }

                    // Update UI with access controls
                    AccessControlAdapter adapter = new AccessControlAdapter(UserAccessControllerActivity.this, controls);
                    accessControlsListView.setAdapter(adapter);
                });
    }
}