package com.example.telemedicine;

import java.util.Date;

public class ChatMessage {
    private String id;
    private String message;
    private String sender; // "patient" or "doctor"
    private String appointmentId;
    private Date timestamp;
    private String messageType; // "text", "file", "note", "prescription_link"
    private String fileUrl; // For file attachments
    private String fileName;
    private String fileSize;
    private String fileMimeType;
    private String noteType; // "consultation_note", "follow_up", "summary"
    private String prescriptionId;
    private long createdAt;
    private long updatedAt;

    public ChatMessage(String message, String sender, String appointmentId) {
        this.message = message;
        this.sender = sender;
        this.appointmentId = appointmentId;
        this.messageType = "text";
        this.timestamp = new Date();
        this.createdAt = System.currentTimeMillis();
        this.updatedAt = System.currentTimeMillis();
    }

    // Constructor for file messages
    public ChatMessage(String sender, String appointmentId, String fileUrl, String fileName,
                      String fileSize, String fileMimeType) {
        this.sender = sender;
        this.appointmentId = appointmentId;
        this.messageType = "file";
        this.fileUrl = fileUrl;
        this.fileName = fileName;
        this.fileSize = fileSize;
        this.fileMimeType = fileMimeType;
        this.timestamp = new Date();
        this.createdAt = System.currentTimeMillis();
        this.updatedAt = System.currentTimeMillis();
    }

    // Constructor for note messages
    public ChatMessage(String sender, String appointmentId, String noteType, String message) {
        this.sender = sender;
        this.appointmentId = appointmentId;
        this.messageType = "note";
        this.noteType = noteType;
        this.message = message;
        this.timestamp = new Date();
        this.createdAt = System.currentTimeMillis();
        this.updatedAt = System.currentTimeMillis();
    }

    // Getters and setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }

    public String getSender() { return sender; }
    public void setSender(String sender) { this.sender = sender; }

    public String getAppointmentId() { return appointmentId; }
    public void setAppointmentId(String appointmentId) { this.appointmentId = appointmentId; }

    public Date getTimestamp() { return timestamp; }
    public void setTimestamp(Date timestamp) { this.timestamp = timestamp; }

    public String getMessageType() { return messageType; }
    public void setMessageType(String messageType) { this.messageType = messageType; }

    public String getFileUrl() { return fileUrl; }
    public void setFileUrl(String fileUrl) { this.fileUrl = fileUrl; }

    public String getFileName() { return fileName; }
    public void setFileName(String fileName) { this.fileName = fileName; }

    public String getFileSize() { return fileSize; }
    public void setFileSize(String fileSize) { this.fileSize = fileSize; }

    public String getFileMimeType() { return fileMimeType; }
    public void setFileMimeType(String fileMimeType) { this.fileMimeType = fileMimeType; }

    public String getNoteType() { return noteType; }
    public void setNoteType(String noteType) { this.noteType = noteType; }

    public String getPrescriptionId() { return prescriptionId; }
    public void setPrescriptionId(String prescriptionId) { this.prescriptionId = prescriptionId; }

    public long getCreatedAt() { return createdAt; }
    public void setCreatedAt(long createdAt) { this.createdAt = createdAt; }

    public long getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(long updatedAt) { this.updatedAt = updatedAt; }
}