package com.example.telemedicine;

import java.util.Date;
import java.util.Map;

public class PlatformAnalytics {
    private String id;
    private Date date;
    private String metricType; // "consultation_volume", "user_growth", "prescription_count", "pharmacy_usage"
    private String metricValue;
    private Map<String, Object> breakdown; // Detailed breakdown data
    private String period; // "daily", "weekly", "monthly", "yearly"
    private String status; // "active", "archived"
    private long createdAt;
    private long updatedAt;

    // Empty constructor required for Firestore
    public PlatformAnalytics() {}

    public PlatformAnalytics(String metricType, String metricValue, Map<String, Object> breakdown,
                            String period) {
        this.metricType = metricType;
        this.metricValue = metricValue;
        this.breakdown = breakdown;
        this.period = period;
        this.date = new Date();
        this.status = "active";
        this.createdAt = System.currentTimeMillis();
        this.updatedAt = System.currentTimeMillis();
    }

    // Getters and setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public Date getDate() { return date; }
    public void setDate(Date date) { this.date = date; }

    public String getMetricType() { return metricType; }
    public void setMetricType(String metricType) { this.metricType = metricType; }

    public String getMetricValue() { return metricValue; }
    public void setMetricValue(String metricValue) { this.metricValue = metricValue; }

    public Map<String, Object> getBreakdown() { return breakdown; }
    public void setBreakdown(Map<String, Object> breakdown) { this.breakdown = breakdown; }

    public String getPeriod() { return period; }
    public void setPeriod(String period) { this.period = period; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public long getCreatedAt() { return createdAt; }
    public void setCreatedAt(long createdAt) { this.createdAt = createdAt; }

    public long getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(long updatedAt) { this.updatedAt = updatedAt; }
}