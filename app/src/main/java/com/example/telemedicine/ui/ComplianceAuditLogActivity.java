package com.example.telemedicine.ui;

import android.os.Bundle;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.telemedicine.R;
import com.example.telemedicine.model.ComplianceAuditLog;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;

public class ComplianceAuditLogActivity extends AppCompatActivity {
    private ListView auditLogsListView;
    private TextView titleTextView;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_compliance_audit_log);

        auditLogsListView = findViewById(R.id.listViewAuditLogs);
        titleTextView = findViewById(R.id.textViewAuditLogTitle);
        db = FirebaseFirestore.getInstance();

        titleTextView.setText("Compliance Audit Logs");

        // Load audit logs
        loadAuditLogs();
    }

    private void loadAuditLogs() {
        db.collection("compliance_audit_logs")
                .addSnapshotListener(new EventListener<QuerySnapshot>() {
                    @Override
                    public void onEvent(@Nullable QuerySnapshot querySnapshot, @Nullable FirebaseFirestoreException e) {
                        if (e != null) {
                            Toast.makeText(ComplianceAuditLogActivity.this, "Error loading audit logs: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                            return;
                        }

                        List<ComplianceAuditLog> logs = new ArrayList<>();
                        if (querySnapshot != null) {
                            for (DocumentSnapshot document : querySnapshot) {
                                ComplianceAuditLog log = document.toObject(ComplianceAuditLog.class);
                                if (log != null) {
                                    logs.add(log);
                                }
                            }
                        }

                        // Update UI with audit logs
                        AuditLogAdapter adapter = new AuditLogAdapter(ComplianceAuditLogActivity.this, logs);
                        auditLogsListView.setAdapter(adapter);
                    }
                });
    }
}