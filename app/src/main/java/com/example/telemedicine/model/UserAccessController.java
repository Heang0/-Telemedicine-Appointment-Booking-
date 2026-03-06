package com.example.telemedicine.model;

import java.util.List;

public class UserAccessController {
    private String id;
    private String userId;
    private String userName;
    private String userRole; // "patient", "doctor", "admin", "pharmacy"
    private String accessLevel; // "read_only", "read_write", "admin"
    private List<String> permissions; // "view_medical_records", "edit_prescriptions", "manage_appointments", etc.
    private String resourceType; // "medical_record", "prescription", "appointment", "user_profile"
    private String resourceId;
    private boolean isActive;
    private long createdAt;
    private long updatedAt;

    // Empty constructor required for Firestore
    public UserAccessController() {}

    public UserAccessController(String userId, String userName, String userRole,
                               String accessLevel, List<String> permissions,
                               String resourceType, String resourceId) {
        this.userId = userId;
        this.userName = userName;
        this.userRole = userRole;
        this.accessLevel = accessLevel;
        this.permissions = permissions;
        this.resourceType = resourceType;
        this.resourceId = resourceId;
        this.isActive = true;
        this.createdAt = System.currentTimeMillis();
        this.updatedAt = System.currentTimeMillis();
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

    public String getAccessLevel() { return accessLevel; }
    public void setAccessLevel(String accessLevel) { this.accessLevel = accessLevel; }

    public List<String> getPermissions() { return permissions; }
    public void setPermissions(List<String> permissions) { this.permissions = permissions; }

    public String getResourceType() { return resourceType; }
    public void setResourceType(String resourceType) { this.resourceType = resourceType; }

    public String getResourceId() { return resourceId; }
    public void setResourceId(String resourceId) { this.resourceId = resourceId; }

    public boolean isActive() { return isActive; }
    public void setActive(boolean active) { this.isActive = active; }

    public long getCreatedAt() { return createdAt; }
    public void setCreatedAt(long createdAt) { this.createdAt = createdAt; }

    public long getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(long updatedAt) { this.updatedAt = updatedAt; }
}