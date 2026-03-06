package com.example.telemedicine.ui;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.example.telemedicine.R;
import com.example.telemedicine.model.MedicalRecordsVault;
import com.example.telemedicine.security.EncryptionUtil;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;

public class MedicalRecordsVaultActivity extends AppCompatActivity {
    private ListView recordsListView;
    private Button addRecordButton;
    private TextView vaultTitleTextView;
    private FirebaseFirestore db;
    private String userId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_medical_records_vault);

        recordsListView = findViewById(R.id.listViewMedicalRecords);
        addRecordButton = findViewById(R.id.buttonAddMedicalRecord);
        vaultTitleTextView = findViewById(R.id.textViewVaultTitle);

        db = FirebaseFirestore.getInstance();
        userId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        vaultTitleTextView.setText("Medical Records Vault - " + userId);

        // Load medical records
        loadMedicalRecords();

        // Set up add record button
        addRecordButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // In a real app, this would open a form to add new records
                Toast.makeText(MedicalRecordsVaultActivity.this, "Add record functionality will be implemented", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void loadMedicalRecords() {
        db.collection("medical_records")
                .whereEqualTo("patientId", userId)
                .addSnapshotListener((querySnapshot, e) -> {
                    if (e != null) {
                        Toast.makeText(MedicalRecordsVaultActivity.this, "Error loading records: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        return;
                    }

                    List<MedicalRecordsVault> records = new ArrayList<>();
                    if (querySnapshot != null) {
                        for (DocumentSnapshot document : querySnapshot) {
                            MedicalRecordsVault record = document.toObject(MedicalRecordsVault.class);
                            if (record != null) {
                                // Decrypt sensitive data (in production, use proper decryption)
                                try {
                                    String decryptedDescription = EncryptionUtil.decrypt(record.getDescription());
                                    String decryptedData = EncryptionUtil.decrypt(record.getEncryptedData());

                                    record.setDescription(decryptedDescription);
                                    record.setEncryptedData(decryptedData);
                                } catch (Exception ex) {
                                    // Handle decryption errors
                                    record.setDescription("Encrypted data - decryption failed");
                                }
                                records.add(record);
                            }
                        }
                    }

                    // Update UI with records
                    MedicalRecordsAdapter adapter = new MedicalRecordsAdapter(MedicalRecordsVaultActivity.this, records);
                    recordsListView.setAdapter(adapter);
                });
    }
}