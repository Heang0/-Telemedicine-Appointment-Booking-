package com.example.telemedicine.ui;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.example.telemedicine.R;
import com.example.telemedicine.model.PharmacyLocator;

import java.util.List;

public class PharmacyAdapter extends ArrayAdapter<PharmacyLocator> {
    private Context context;
    private List<PharmacyLocator> pharmacies;

    public PharmacyAdapter(Context context, List<PharmacyLocator> pharmacies) {
        super(context, 0, pharmacies);
        this.context = context;
        this.pharmacies = pharmacies;
    }

    @NonNull
    @Override
    public View getView(int position, View convertView, @NonNull ViewGroup parent) {
        View view = convertView;
        if (view == null) {
            view = LayoutInflater.from(context).inflate(R.layout.item_pharmacy, parent, false);
        }

        PharmacyLocator pharmacy = pharmacies.get(position);
        
        TextView nameTextView = view.findViewById(R.id.textViewPharmacyName);
        TextView addressTextView = view.findViewById(R.id.textViewPharmacyAddress);
        TextView phoneTextView = view.findViewById(R.id.textViewPharmacyPhone);
        TextView ratingTextView = view.findViewById(R.id.textViewPharmacyRating);

        nameTextView.setText(pharmacy.getName());
        addressTextView.setText(pharmacy.getAddress() + ", " + pharmacy.getCity() + ", " + pharmacy.getState());
        phoneTextView.setText(pharmacy.getPhoneNumber());
        ratingTextView.setText("Rating: " + pharmacy.getRating() + " (" + pharmacy.getReviewCount() + " reviews)");

        return view;
    }
}