package com.example.telemedicine;

import java.util.Date;

public class ComplianceAuditLog {
    private String id;
    private String userId;
    private String userName;
    private String userRole; // "patient", "doctor", "admin"
    private String action; // "view_record", "edit_record", "delete_record", "access_prescription"
    private String resourceType; // "medical_record", "prescription", "appointment", "user_profile"
    private String resourceId;
    private String ipAddress;
    private String userAgent;
    private Date timestamp;
    private String status; // "success", "failed", "denied"
    private String notes;
    private long createdAt;

    // Empty constructor required for Firestore
    public ComplianceAuditLog() {}

    public ComplianceAuditLog(String userId, String userName, String userRole,
                             String action, String resourceType, String resourceId,
                             String ipAddress, String userAgent, String status, String notes) {
        this.userId = userId;
        this.userName = userName;
        this.userRole = userRole;
        this.action = action;
        this.resourceType = resourceType;
        this.resourceId = resourceId;
        this.ipAddress = ipAddress;
        this.userAgent = userAgent;
        this.status = status;
        this.notes = notes;
        this.timestamp = new Date();
        this.createdAt = System.currentTimeMillis();
    }

    // Getters and setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    public String getUserName() { return userName; }
    public void setUserName(String userName) { this.userName = userName; }

    public String getUserRole() { return userRole; }
    public void setUserRole(String userRole) { this.userRole = userRole; }

    public String getAction() { return action; }
    public void setAction(String action) { this.action = action; }

    public String getResourceType() { return resourceType; }
    public void setResourceType(String resourceType) { this.resourceType = resourceType; }

    public String getResourceId() { return resourceId; }
    public void setResourceId(String resourceId) { this.resourceId = resourceId; }

    public String getIpAddress() { return ipAddress; }
    public void setIpAddress(String ipAddress) { this.ipAddress = ipAddress; }

    public String getUserAgent() { return userAgent; }
    public void setUserAgent(String userAgent) { this.userAgent = userAgent; }

    public Date getTimestamp() { return timestamp; }
    public void setTimestamp(Date timestamp) { this.timestamp = timestamp; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }

    public long getCreatedAt() { return createdAt; }
    public void setCreatedAt(long createdAt) { this.createdAt = createdAt; }
}