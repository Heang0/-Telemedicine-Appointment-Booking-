package com.example.telemedicine.ui;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.example.telemedicine.R;
import com.example.telemedicine.model.PlatformAnalytics;

import java.util.List;

public class AnalyticsAdapter extends ArrayAdapter<PlatformAnalytics> {
    private Context context;
    private List<PlatformAnalytics> analytics;

    public AnalyticsAdapter(Context context, List<PlatformAnalytics> analytics) {
        super(context, 0, analytics);
        this.context = context;
        this.analytics = analytics;
    }

    @NonNull
    @Override
    public View getView(int position, View convertView, @NonNull ViewGroup parent) {
        View view = convertView;
        if (view == null) {
            view = LayoutInflater.from(context).inflate(R.layout.item_analytics, parent, false);
        }

        PlatformAnalytics analytic = analytics.get(position);
        
        TextView metricTypeTextView = view.findViewById(R.id.textViewMetricType);
        TextView metricValueTextView = view.findViewById(R.id.textViewMetricValue);
        TextView periodTextView = view.findViewById(R.id.textViewPeriod);
        TextView dateTextView = view.findViewById(R.id.textViewDate);

        metricTypeTextView.setText(analytic.getMetricType());
        metricValueTextView.setText(analytic.getMetricValue());
        periodTextView.setText("Period: " + analytic.getPeriod());
        dateTextView.setText("Date: " + analytic.getDate().toString());

        return view;
    }
}