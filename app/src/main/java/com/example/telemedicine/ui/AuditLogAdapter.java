package com.example.telemedicine.ui;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.example.telemedicine.R;
import com.example.telemedicine.model.ComplianceAuditLog;

import java.util.List;

public class AuditLogAdapter extends ArrayAdapter<ComplianceAuditLog> {
    private Context context;
    private List<ComplianceAuditLog> logs;

    public AuditLogAdapter(Context context, List<ComplianceAuditLog> logs) {
        super(context, 0, logs);
        this.context = context;
        this.logs = logs;
    }

    @NonNull
    @Override
    public View getView(int position, View convertView, @NonNull ViewGroup parent) {
        View view = convertView;
        if (view == null) {
            view = LayoutInflater.from(context).inflate(R.layout.item_audit_log, parent, false);
        }

        ComplianceAuditLog log = logs.get(position);
        
        TextView userTextView = view.findViewById(R.id.textViewUser);
        TextView actionTextView = view.findViewById(R.id.textViewAction);
        TextView resourceTextView = view.findViewById(R.id.textViewResource);
        TextView statusTextView = view.findViewById(R.id.textViewStatus);
        TextView timeTextView = view.findViewById(R.id.textViewTime);

        userTextView.setText(log.getUserName() + " (" + log.getUserRole() + ")");
        actionTextView.setText(log.getAction());
        resourceTextView.setText(log.getResourceType() + ": " + log.getResourceId());
        statusTextView.setText(log.getStatus());
        timeTextView.setText("Time: " + log.getTimestamp().toString());

        return view;
    }
}