package com.example.telemedicine;

import java.util.Date;
import java.util.List;

public class Prescription {
    private String id;
    private String patientId;
    private String doctorId;
    private String patientName;
    private String doctorName;
    private String doctorSpecialty; // Added to show doctor's specialty
    private String appointmentId; // Added to link to specific appointment
    private List<Medication> medications;
    private String instructions;
    private Date prescribedDate;
    private Date expiryDate;
    private String status; // pending, active, fulfilled, expired
    private String notes;
    private long createdAt;
    private long updatedAt;

    // Empty constructor required for Firestore
    public Prescription() {}

    public Prescription(String patientId, String doctorId, String patientName, String doctorName,
                       List<Medication> medications, String instructions, Date expiryDate, String notes) {
        this.patientId = patientId;
        this.doctorId = doctorId;
        this.patientName = patientName;
        this.doctorName = doctorName;
        this.medications = medications;
        this.instructions = instructions;
        this.expiryDate = expiryDate;
        this.notes = notes;
        this.prescribedDate = new Date();
        this.status = "active";
        this.createdAt = System.currentTimeMillis();
        this.updatedAt = System.currentTimeMillis();
    }

    // Enhanced constructor with additional fields
    public Prescription(String patientId, String doctorId, String patientName, String doctorName,
                       String doctorSpecialty, String appointmentId, List<Medication> medications,
                       String instructions, Date expiryDate, String notes) {
        this.patientId = patientId;
        this.doctorId = doctorId;
        this.patientName = patientName;
        this.doctorName = doctorName;
        this.doctorSpecialty = doctorSpecialty;
        this.appointmentId = appointmentId;
        this.medications = medications;
        this.instructions = instructions;
        this.expiryDate = expiryDate;
        this.notes = notes;
        this.prescribedDate = new Date();
        this.status = "active";
        this.createdAt = System.currentTimeMillis();
        this.updatedAt = System.currentTimeMillis();
    }

    // Inner class for medication
    public static class Medication {
        private String name;
        private String dosage;
        private String frequency;
        private String duration;
        private String quantity;

        public Medication() {}

        public Medication(String name, String dosage, String frequency, String duration, String quantity) {
            this.name = name;
            this.dosage = dosage;
            this.frequency = frequency;
            this.duration = duration;
            this.quantity = quantity;
        }

        // Getters and setters
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }

        public String getDosage() { return dosage; }
        public void setDosage(String dosage) { this.dosage = dosage; }

        public String getFrequency() { return frequency; }
        public void setFrequency(String frequency) { this.frequency = frequency; }

        public String getDuration() { return duration; }
        public void setDuration(String duration) { this.duration = duration; }

        public String getQuantity() { return quantity; }
        public void setQuantity(String quantity) { this.quantity = quantity; }
    }

    // Getters and setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getPatientId() { return patientId; }
    public void setPatientId(String patientId) { this.patientId = patientId; }

    public String getDoctorId() { return doctorId; }
    public void setDoctorId(String doctorId) { this.doctorId = doctorId; }

    public String getPatientName() { return patientName; }
    public void setPatientName(String patientName) { this.patientName = patientName; }

    public String getDoctorName() { return doctorName; }
    public void setDoctorName(String doctorName) { this.doctorName = doctorName; }

    public List<Medication> getMedications() { return medications; }
    public void setMedications(List<Medication> medications) { this.medications = medications; }

    public String getInstructions() { return instructions; }
    public void setInstructions(String instructions) { this.instructions = instructions; }

    public Date getPrescribedDate() { return prescribedDate; }
    public void setPrescribedDate(Date prescribedDate) { this.prescribedDate = prescribedDate; }

    public Date getExpiryDate() { return expiryDate; }
    public void setExpiryDate(Date expiryDate) { this.expiryDate = expiryDate; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }

    public long getCreatedAt() { return createdAt; }
    public void setCreatedAt(long createdAt) { this.createdAt = createdAt; }

    public long getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(long updatedAt) { this.updatedAt = updatedAt; }

    // Getters and setters for new fields
    public String getDoctorSpecialty() { return doctorSpecialty; }
    public void setDoctorSpecialty(String doctorSpecialty) { this.doctorSpecialty = doctorSpecialty; }

    public String getAppointmentId() { return appointmentId; }
    public void setAppointmentId(String appointmentId) { this.appointmentId = appointmentId; }
}