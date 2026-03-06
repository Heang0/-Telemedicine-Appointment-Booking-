package com.example.telemedicine;

import java.util.Date;
import java.util.List;

public class Appointment {
    private String id;
    private String patientId;
    private String doctorId;
    private String patientName;
    private String doctorName;
    private Date appointmentDate;
    private String status; // scheduled, completed, cancelled, missed
    private String reason;
    private String notes;
    private String consultationType; // "in_person", "chat", "follow_up"
    private String location; // For in-person appointments
    private String meetingLink; // For chat consultations (will be empty for in-person)
    private long createdAt;
    private long updatedAt;

    // Empty constructor required for Firestore
    public Appointment() {}

    public Appointment(String patientId, String doctorId, String patientName, String doctorName,
                      Date appointmentDate, String status, String reason, String consultationType) {
        this.patientId = patientId;
        this.doctorId = doctorId;
        this.patientName = patientName;
        this.doctorName = doctorName;
        this.appointmentDate = appointmentDate;
        this.status = status;
        this.reason = reason;
        this.consultationType = consultationType;
        this.createdAt = System.currentTimeMillis();
        this.updatedAt = System.currentTimeMillis();
    }

    // Enhanced constructor with location and meeting link
    public Appointment(String patientId, String doctorId, String patientName, String doctorName,
                      Date appointmentDate, String status, String reason, String consultationType,
                      String location, String meetingLink) {
        this.patientId = patientId;
        this.doctorId = doctorId;
        this.patientName = patientName;
        this.doctorName = doctorName;
        this.appointmentDate = appointmentDate;
        this.status = status;
        this.reason = reason;
        this.consultationType = consultationType;
        this.location = location;
        this.meetingLink = meetingLink;
        this.createdAt = System.currentTimeMillis();
        this.updatedAt = System.currentTimeMillis();
    }

    // Getters and setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getPatientId() {
        return patientId;
    }

    public void setPatientId(String patientId) {
        this.patientId = patientId;
    }

    public String getDoctorId() {
        return doctorId;
    }

    public void setDoctorId(String doctorId) {
        this.doctorId = doctorId;
    }

    public String getPatientName() {
        return patientName;
    }

    public void setPatientName(String patientName) {
        this.patientName = patientName;
    }

    public String getDoctorName() {
        return doctorName;
    }

    public void setDoctorName(String doctorName) {
        this.doctorName = doctorName;
    }

    public Date getAppointmentDate() {
        return appointmentDate;
    }

    public void setAppointmentDate(Date appointmentDate) {
        this.appointmentDate = appointmentDate;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
        this.updatedAt = System.currentTimeMillis();
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
        this.updatedAt = System.currentTimeMillis();
    }

    public long getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(long createdAt) {
        this.createdAt = createdAt;
    }

    public long getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(long updatedAt) {
        this.updatedAt = updatedAt;
    }

    // Getters and setters for new fields
    public String getConsultationType() {
        return consultationType;
    }

    public void setConsultationType(String consultationType) {
        this.consultationType = consultationType;
        this.updatedAt = System.currentTimeMillis();
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
        this.updatedAt = System.currentTimeMillis();
    }

    public String getMeetingLink() {
        return meetingLink;
    }

    public void setMeetingLink(String meetingLink) {
        this.meetingLink = meetingLink;
        this.updatedAt = System.currentTimeMillis();
    }
}