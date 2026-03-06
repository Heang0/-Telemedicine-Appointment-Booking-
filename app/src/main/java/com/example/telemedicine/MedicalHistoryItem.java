package com.example.telemedicine;

import java.util.Date;

public class MedicalHistoryItem {
    private String condition;
    private String diagnosisDate;
    private String treatingDoctor;
    private String status; // active, resolved, chronic
    private String notes;
    private String sourceType;

    public MedicalHistoryItem(String condition, String diagnosisDate, String treatingDoctor, String status, String notes) {
        this(condition, diagnosisDate, treatingDoctor, status, notes, "general");
    }

    public MedicalHistoryItem(String condition, String diagnosisDate, String treatingDoctor, String status, String notes, String sourceType) {
        this.condition = condition;
        this.diagnosisDate = diagnosisDate;
        this.treatingDoctor = treatingDoctor;
        this.status = status;
        this.notes = notes;
        this.sourceType = sourceType;
    }

    // Getters
    public String getCondition() { return condition; }
    public String getDiagnosisDate() { return diagnosisDate; }
    public String getTreatingDoctor() { return treatingDoctor; }
    public String getStatus() { return status; }
    public String getNotes() { return notes; }
    public String getSourceType() { return sourceType; }

    // Setters
    public void setCondition(String condition) { this.condition = condition; }
    public void setDiagnosisDate(String diagnosisDate) { this.diagnosisDate = diagnosisDate; }
    public void setTreatingDoctor(String treatingDoctor) { this.treatingDoctor = treatingDoctor; }
    public void setStatus(String status) { this.status = status; }
    public void setNotes(String notes) { this.notes = notes; }
    public void setSourceType(String sourceType) { this.sourceType = sourceType; }
}
