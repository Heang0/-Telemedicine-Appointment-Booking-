package com.example.telemedicine;

public class RecordCategory {
    private String title;
    private String description;
    private int recordCount;
    private int iconResId;

    public RecordCategory(String title, String description, int recordCount, int iconResId) {
        this.title = title;
        this.description = description;
        this.recordCount = recordCount;
        this.iconResId = iconResId;
    }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public int getRecordCount() { return recordCount; }
    public void setRecordCount(int recordCount) { this.recordCount = recordCount; }

    public int getIconResId() { return iconResId; }
    public void setIconResId(int iconResId) { this.iconResId = iconResId; }
}