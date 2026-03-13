package com.example.telemedicine;

import java.util.Date;

/**
 * Medical Record data model
 * Encryption is handled by EncryptionUtil class
 */
public class MedicalRecordsVault {
    private String id;
    private String patientId;
    private String recordType;
    private String title;
    private String description;
    private String encryptedData;
    private Date createdAt;
    private String status;
    private long updatedAt;

    public MedicalRecordsVault() {}

    public MedicalRecordsVault(String patientId, String recordType, String title,
                               String description, String encryptedData) {
        this.patientId = patientId;
        this.recordType = recordType;
        this.title = title;
        this.description = description;
        this.encryptedData = encryptedData;
        this.createdAt = new Date();
        this.status = "active";
        this.updatedAt = System.currentTimeMillis();
    }

    // Getters and Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getPatientId() { return patientId; }
    public void setPatientId(String patientId) { this.patientId = patientId; }

    public String getRecordType() { return recordType; }
    public void setRecordType(String recordType) { this.recordType = recordType; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getEncryptedData() { return encryptedData; }
    public void setEncryptedData(String encryptedData) { this.encryptedData = encryptedData; }

    public Date getCreatedAt() { return createdAt; }
    public void setCreatedAt(Date createdAt) { this.createdAt = createdAt; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public long getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(long updatedAt) { this.updatedAt = updatedAt; }
}
