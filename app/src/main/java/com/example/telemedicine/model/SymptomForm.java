package com.example.telemedicine.model;

import java.util.Date;
import java.util.List;

public class SymptomForm {
    private String id;
    private String patientId;
    private String appointmentId;
    private String patientName;
    private Date submittedDate;
    private String chiefComplaint;
    private String duration;
    private String severity;
    private String location;
    private String associatedSymptoms;
    private String aggravatingFactors;
    private String relievingFactors;
    private String pastMedicalHistory;
    private String medications;
    private String allergies;
    private String familyHistory;
    private String socialHistory;
    private String reviewOfSystems;
    private String vitalSigns;
    private String notes;
    private String status; // "draft", "submitted", "reviewed"
    private long createdAt;
    private long updatedAt;

    // Empty constructor required for Firestore
    public SymptomForm() {}

    public SymptomForm(String patientId, String appointmentId, String patientName,
                      String chiefComplaint, String duration, String severity,
                      String location, String associatedSymptoms, String aggravatingFactors,
                      String relievingFactors, String pastMedicalHistory, String medications,
                      String allergies, String familyHistory, String socialHistory,
                      String reviewOfSystems, String vitalSigns, String notes) {
        this.patientId = patientId;
        this.appointmentId = appointmentId;
        this.patientName = patientName;
        this.chiefComplaint = chiefComplaint;
        this.duration = duration;
        this.severity = severity;
        this.location = location;
        this.associatedSymptoms = associatedSymptoms;
        this.aggravatingFactors = aggravatingFactors;
        this.relievingFactors = relievingFactors;
        this.pastMedicalHistory = pastMedicalHistory;
        this.medications = medications;
        this.allergies = allergies;
        this.familyHistory = familyHistory;
        this.socialHistory = socialHistory;
        this.reviewOfSystems = reviewOfSystems;
        this.vitalSigns = vitalSigns;
        this.notes = notes;
        this.status = "submitted";
        this.submittedDate = new Date();
        this.createdAt = System.currentTimeMillis();
        this.updatedAt = System.currentTimeMillis();
    }

    // Getters and setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getPatientId() { return patientId; }
    public void setPatientId(String patientId) { this.patientId = patientId; }

    public String getAppointmentId() { return appointmentId; }
    public void setAppointmentId(String appointmentId) { this.appointmentId = appointmentId; }

    public String getPatientName() { return patientName; }
    public void setPatientName(String patientName) { this.patientName = patientName; }

    public Date getSubmittedDate() { return submittedDate; }
    public void setSubmittedDate(Date submittedDate) { this.submittedDate = submittedDate; }

    public String getChiefComplaint() { return chiefComplaint; }
    public void setChiefComplaint(String chiefComplaint) { this.chiefComplaint = chiefComplaint; }

    public String getDuration() { return duration; }
    public void setDuration(String duration) { this.duration = duration; }

    public String getSeverity() { return severity; }
    public void setSeverity(String severity) { this.severity = severity; }

    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }

    public String getAssociatedSymptoms() { return associatedSymptoms; }
    public void setAssociatedSymptoms(String associatedSymptoms) { this.associatedSymptoms = associatedSymptoms; }

    public String getAggravatingFactors() { return aggravatingFactors; }
    public void setAggravatingFactors(String aggravatingFactors) { this.aggravatingFactors = aggravatingFactors; }

    public String getRelievingFactors() { return relievingFactors; }
    public void setRelievingFactors(String relievingFactors) { this.relievingFactors = relievingFactors; }

    public String getPastMedicalHistory() { return pastMedicalHistory; }
    public void setPastMedicalHistory(String pastMedicalHistory) { this.pastMedicalHistory = pastMedicalHistory; }

    public String getMedications() { return medications; }
    public void setMedications(String medications) { this.medications = medications; }

    public String getAllergies() { return allergies; }
    public void setAllergies(String allergies) { this.allergies = allergies; }

    public String getFamilyHistory() { return familyHistory; }
    public void setFamilyHistory(String familyHistory) { this.familyHistory = familyHistory; }

    public String getSocialHistory() { return socialHistory; }
    public void setSocialHistory(String socialHistory) { this.socialHistory = socialHistory; }

    public String getReviewOfSystems() { return reviewOfSystems; }
    public void setReviewOfSystems(String reviewOfSystems) { this.reviewOfSystems = reviewOfSystems; }

    public String getVitalSigns() { return vitalSigns; }
    public void setVitalSigns(String vitalSigns) { this.vitalSigns = vitalSigns; }

    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public long getCreatedAt() { return createdAt; }
    public void setCreatedAt(long createdAt) { this.createdAt = createdAt; }

    public long getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(long updatedAt) { this.updatedAt = updatedAt; }
}