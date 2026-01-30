package com.example.telemedicine;

public enum UserRole {
    PATIENT("patient"),
    DOCTOR("doctor"),
    ADMIN("admin");

    private final String roleName;

    UserRole(String roleName) {
        this.roleName = roleName;
    }

    public String getRoleName() {
        return roleName;
    }

    public static UserRole fromString(String role) {
        for (UserRole userRole : UserRole.values()) {
            if (userRole.roleName.equalsIgnoreCase(role)) {
                return userRole;
            }
        }
        return PATIENT; // Default role
    }
}