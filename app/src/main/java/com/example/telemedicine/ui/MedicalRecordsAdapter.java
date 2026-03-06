package com.example.telemedicine.ui;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.example.telemedicine.R;
import com.example.telemedicine.model.MedicalRecordsVault;

import java.util.List;

public class MedicalRecordsAdapter extends ArrayAdapter<MedicalRecordsVault> {
    private Context context;
    private List<MedicalRecordsVault> records;

    public MedicalRecordsAdapter(Context context, List<MedicalRecordsVault> records) {
        super(context, 0, records);
        this.context = context;
        this.records = records;
    }

    @NonNull
    @Override
    public View getView(int position, View convertView, @NonNull ViewGroup parent) {
        View view = convertView;
        if (view == null) {
            view = LayoutInflater.from(context).inflate(R.layout.item_medical_record, parent, false);
        }

        MedicalRecordsVault record = records.get(position);
        
        TextView titleTextView = view.findViewById(R.id.textViewRecordTitle);
        TextView typeTextView = view.findViewById(R.id.textViewRecordType);
        TextView dateTextView = view.findViewById(R.id.textViewRecordDate);
        TextView statusTextView = view.findViewById(R.id.textViewRecordStatus);

        titleTextView.setText(record.getTitle());
        typeTextView.setText(record.getRecordType());
        dateTextView.setText("Created: " + record.getCreatedAt());
        statusTextView.setText(record.getStatus());

        return view;
    }
}