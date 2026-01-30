package com.example.telemedicine;

import java.util.Date;
import java.util.List;

/**
 * Enhanced Prescription model with real-world medical features for telemedicine
 * Includes safety checks, validation, and comprehensive medical documentation
 */
public class EnhancedPrescription {
    // Basic identification
    private String id;
    private String patientId;
    private String doctorId;
    private String patientName;
    private String doctorName;
    private String doctorSpecialty;
    private String doctorLicenseNumber; // Added for medical authenticity
    
    // Appointment linkage
    private String appointmentId;
    private String consultationType; // video, phone, in-person
    
    // Prescription details
    private List<Medication> medications;
    private String instructions;
    private String notes;
    private String diagnosisCode; // ICD-10 code
    private String diagnosisDescription;
    
    // Medical safety features
    private boolean isControlledSubstance; // Schedule II-V drugs
    private String DEARegistrationNumber; // For controlled substances
    private boolean requiresPriorAuthorization; // Insurance pre-approval needed
    private boolean hasDrugAllergyCheck; // Confirmed no allergies
    private String allergyCheckNotes;
    
    // Timing and validity
    private Date prescribedDate;
    private Date expiryDate;
    private Date refillExpiryDate; // When refills expire
    private int totalRefills;
    private int remainingRefills;
    
    // Status tracking
    private String status; // pending, active, fulfilled, expired, cancelled, rejected
    private String fulfillmentStatus; // not_filled, partially_filled, fully_filled
    private String pharmacyId;
    private String pharmacyName;
    
    // Digital signature and authentication
    private String digitalSignature; // Hash of prescription + timestamp
    private String signatureTimestamp;
    private boolean isDigitallySigned;
    private String signatureMethod; // e.g., "biometric", "PIN", "OTP"
    
    // Audit trail
    private long createdAt;
    private long updatedAt;
    private String createdBy;
    private String updatedBy;
    
    // Empty constructor required for Firestore
    public EnhancedPrescription() {}

    // Constructor with essential fields
    public EnhancedPrescription(String patientId, String doctorId, String patientName, String doctorName,
                               String doctorSpecialty, String doctorLicenseNumber,
                               List<Medication> medications, String instructions, String notes,
                               String diagnosisCode, String diagnosisDescription) {
        this.patientId = patientId;
        this.doctorId = doctorId;
        this.patientName = patientName;
        this.doctorName = doctorName;
        this.doctorSpecialty = doctorSpecialty;
        this.doctorLicenseNumber = doctorLicenseNumber;
        this.medications = medications;
        this.instructions = instructions;
        this.notes = notes;
        this.diagnosisCode = diagnosisCode;
        this.diagnosisDescription = diagnosisDescription;
        
        this.prescribedDate = new Date();
        this.expiryDate = new Date(System.currentTimeMillis() + (90L * 24 * 60 * 60 * 1000)); // 90 days default
        this.refillExpiryDate = new Date(System.currentTimeMillis() + (180L * 24 * 60 * 60 * 1000)); // 180 days for refills
        this.totalRefills = 3;
        this.remainingRefills = 3;
        this.status = "active";
        this.fulfillmentStatus = "not_filled";
        this.createdAt = System.currentTimeMillis();
        this.updatedAt = System.currentTimeMillis();
        this.createdBy = doctorId;
        this.updatedBy = doctorId;
    }

    // Inner class for medication with enhanced medical details
    public static class Medication {
        private String name;
        private String genericName;
        private String dosage;
        private String frequency;
        private String duration;
        private String quantity;
        private String route; // oral, topical, IV, etc.
        private String form; // tablet, capsule, liquid, injection
        private String strength;
        private String manufacturer;
        private String NDCCode; // National Drug Code
        private boolean isControlledSubstance;
        private String schedule; // Schedule II, III, IV, V
        private String therapeuticClass;
        private String indication; // Why this medication is prescribed
        
        public Medication() {}

        public Medication(String name, String genericName, String dosage, String frequency,
                         String duration, String quantity, String route, String form,
                         String strength, String manufacturer, String NDCCode,
                         boolean isControlledSubstance, String schedule,
                         String therapeuticClass, String indication) {
            this.name = name;
            this.genericName = genericName;
            this.dosage = dosage;
            this.frequency = frequency;
            this.duration = duration;
            this.quantity = quantity;
            this.route = route;
            this.form = form;
            this.strength = strength;
            this.manufacturer = manufacturer;
            this.NDCCode = NDCCode;
            this.isControlledSubstance = isControlledSubstance;
            this.schedule = schedule;
            this.therapeuticClass = therapeuticClass;
            this.indication = indication;
        }

        // Getters and setters
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }

        public String getGenericName() { return genericName; }
        public void setGenericName(String genericName) { this.genericName = genericName; }

        public String getDosage() { return dosage; }
        public void setDosage(String dosage) { this.dosage = dosage; }

        public String getFrequency() { return frequency; }
        public void setFrequency(String frequency) { this.frequency = frequency; }

        public String getDuration() { return duration; }
        public void setDuration(String duration) { this.duration = duration; }

        public String getQuantity() { return quantity; }
        public void setQuantity(String quantity) { this.quantity = quantity; }

        public String getRoute() { return route; }
        public void setRoute(String route) { this.route = route; }

        public String getForm() { return form; }
        public void setForm(String form) { this.form = form; }

        public String getStrength() { return strength; }
        public void setStrength(String strength) { this.strength = strength; }

        public String getManufacturer() { return manufacturer; }
        public void setManufacturer(String manufacturer) { this.manufacturer = manufacturer; }

        public String getNDCCode() { return NDCCode; }
        public void setNDCCode(String NDCCode) { this.NDCCode = NDCCode; }

        public boolean isControlledSubstance() { return isControlledSubstance; }
        public void setControlledSubstance(boolean controlledSubstance) { this.isControlledSubstance = controlledSubstance; }

        public String getSchedule() { return schedule; }
        public void setSchedule(String schedule) { this.schedule = schedule; }

        public String getTherapeuticClass() { return therapeuticClass; }
        public void setTherapeuticClass(String therapeuticClass) { this.therapeuticClass = therapeuticClass; }

        public String getIndication() { return indication; }
        public void setIndication(String indication) { this.indication = indication; }
    }

    // Getters and setters for all fields
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

    public String getDoctorSpecialty() { return doctorSpecialty; }
    public void setDoctorSpecialty(String doctorSpecialty) { this.doctorSpecialty = doctorSpecialty; }

    public String getDoctorLicenseNumber() { return doctorLicenseNumber; }
    public void setDoctorLicenseNumber(String doctorLicenseNumber) { this.doctorLicenseNumber = doctorLicenseNumber; }

    public String getAppointmentId() { return appointmentId; }
    public void setAppointmentId(String appointmentId) { this.appointmentId = appointmentId; }

    public String getConsultationType() { return consultationType; }
    public void setConsultationType(String consultationType) { this.consultationType = consultationType; }

    public List<Medication> getMedications() { return medications; }
    public void setMedications(List<Medication> medications) { this.medications = medications; }

    public String getInstructions() { return instructions; }
    public void setInstructions(String instructions) { this.instructions = instructions; }

    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }

    public String getDiagnosisCode() { return diagnosisCode; }
    public void setDiagnosisCode(String diagnosisCode) { this.diagnosisCode = diagnosisCode; }

    public String getDiagnosisDescription() { return diagnosisDescription; }
    public void setDiagnosisDescription(String diagnosisDescription) { this.diagnosisDescription = diagnosisDescription; }

    public boolean isControlledSubstance() { return isControlledSubstance; }
    public void setControlledSubstance(boolean controlledSubstance) { this.isControlledSubstance = controlledSubstance; }

    public String getDEARegistrationNumber() { return DEARegistrationNumber; }
    public void setDEARegistrationNumber(String DEARegistrationNumber) { this.DEARegistrationNumber = DEARegistrationNumber; }

    public boolean requiresPriorAuthorization() { return requiresPriorAuthorization; }
    public void setRequiresPriorAuthorization(boolean requiresPriorAuthorization) { this.requiresPriorAuthorization = requiresPriorAuthorization; }

    public boolean hasDrugAllergyCheck() { return hasDrugAllergyCheck; }
    public void setHasDrugAllergyCheck(boolean hasDrugAllergyCheck) { this.hasDrugAllergyCheck = hasDrugAllergyCheck; }

    public String getAllergyCheckNotes() { return allergyCheckNotes; }
    public void setAllergyCheckNotes(String allergyCheckNotes) { this.allergyCheckNotes = allergyCheckNotes; }

    public Date getPrescribedDate() { return prescribedDate; }
    public void setPrescribedDate(Date prescribedDate) { this.prescribedDate = prescribedDate; }

    public Date getExpiryDate() { return expiryDate; }
    public void setExpiryDate(Date expiryDate) { this.expiryDate = expiryDate; }

    public Date getRefillExpiryDate() { return refillExpiryDate; }
    public void setRefillExpiryDate(Date refillExpiryDate) { this.refillExpiryDate = refillExpiryDate; }

    public int getTotalRefills() { return totalRefills; }
    public void setTotalRefills(int totalRefills) { this.totalRefills = totalRefills; }

    public int getRemainingRefills() { return remainingRefills; }
    public void setRemainingRefills(int remainingRefills) { this.remainingRefills = remainingRefills; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getFulfillmentStatus() { return fulfillmentStatus; }
    public void setFulfillmentStatus(String fulfillmentStatus) { this.fulfillmentStatus = fulfillmentStatus; }

    public String getPharmacyId() { return pharmacyId; }
    public void setPharmacyId(String pharmacyId) { this.pharmacyId = pharmacyId; }

    public String getPharmacyName() { return pharmacyName; }
    public void setPharmacyName(String pharmacyName) { this.pharmacyName = pharmacyName; }

    public String getDigitalSignature() { return digitalSignature; }
    public void setDigitalSignature(String digitalSignature) { this.digitalSignature = digitalSignature; }

    public String getSignatureTimestamp() { return signatureTimestamp; }
    public void setSignatureTimestamp(String signatureTimestamp) { this.signatureTimestamp = signatureTimestamp; }

    public boolean isDigitallySigned() { return isDigitallySigned; }
    public void setIsDigitallySigned(boolean digitallySigned) { this.isDigitallySigned = digitallySigned; }

    public String getSignatureMethod() { return signatureMethod; }
    public void setSignatureMethod(String signatureMethod) { this.signatureMethod = signatureMethod; }

    public long getCreatedAt() { return createdAt; }
    public void setCreatedAt(long createdAt) { this.createdAt = createdAt; }

    public long getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(long updatedAt) { this.updatedAt = updatedAt; }

    public String getCreatedBy() { return createdBy; }
    public void setCreatedBy(String createdBy) { this.createdBy = createdBy; }

    public String getUpdatedBy() { return updatedBy; }
    public void setUpdatedBy(String updatedBy) { this.updatedBy = updatedBy; }

    // Helper methods for medical safety
    public boolean isExpired() {
        if (expiryDate == null) return false;
        return new Date().after(expiryDate);
    }

    public boolean hasRemainingRefills() {
        return remainingRefills > 0;
    }

    public boolean isControlledSubstanceValid() {
        return !isControlledSubstance || (DEARegistrationNumber != null && !DEARegistrationNumber.isEmpty());
    }

    public String getMedicationSummary() {
        if (medications == null || medications.isEmpty()) return "No medications";
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < medications.size(); i++) {
            if (i > 0) sb.append(", ");
            sb.append(medications.get(i).getName()).append(" ").append(medications.get(i).getDosage());
        }
        return sb.toString();
    }
}