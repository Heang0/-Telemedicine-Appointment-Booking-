package com.example.telemedicine.ui;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.example.telemedicine.R;
import com.example.telemedicine.model.UserAccessController;

import java.util.List;

public class AccessControlAdapter extends ArrayAdapter<UserAccessController> {
    private Context context;
    private List<UserAccessController> controls;

    public AccessControlAdapter(Context context, List<UserAccessController> controls) {
        super(context, 0, controls);
        this.context = context;
        this.controls = controls;
    }

    @NonNull
    @Override
    public View getView(int position, View convertView, @NonNull ViewGroup parent) {
        View view = convertView;
        if (view == null) {
            view = LayoutInflater.from(context).inflate(R.layout.item_access_control, parent, false);
        }

        UserAccessController control = controls.get(position);
        
        TextView userTextView = view.findViewById(R.id.textViewUser);
        TextView roleTextView = view.findViewById(R.id.textViewRole);
        TextView accessLevelTextView = view.findViewById(R.id.textViewAccessLevel);
        TextView resourceTextView = view.findViewById(R.id.textViewResource);
        TextView statusTextView = view.findViewById(R.id.textViewStatus);

        userTextView.setText(control.getUserName());
        roleTextView.setText("Role: " + control.getUserRole());
        accessLevelTextView.setText("Access: " + control.getAccessLevel());
        resourceTextView.setText("Resource: " + control.getResourceType() + " - " + control.getResourceId());
        statusTextView.setText(control.isActive() ? "Active" : "Inactive");

        return view;
    }
}