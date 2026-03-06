package com.example.telemedicine;

import android.util.Base64;
import android.util.Log;

import java.security.Key;
import java.security.SecureRandom;
import java.util.Date;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

public class MedicalRecordsVault {
    private static final String TAG = "MedicalRecordsVault";
    private static final String ALGORITHM = "AES";
    private static final String TRANSFORMATION = "AES/ECB/PKCS5Padding";
    
    // For demo purposes, in production use Android Keystore
    private SecretKey secretKey;
    
    public MedicalRecordsVault() {
        try {
            KeyGenerator keyGenerator = KeyGenerator.getInstance(ALGORITHM);
            keyGenerator.init(256); // 256-bit key
            this.secretKey = keyGenerator.generateKey();
        } catch (Exception e) {
            Log.e(TAG, "Error generating encryption key", e);
        }
    }
    
    // Encrypt medical record data
    public String encrypt(String data) {
        if (secretKey == null || data == null) return data;
        
        try {
            Cipher cipher = Cipher.getInstance(TRANSFORMATION);
            cipher.init(Cipher.ENCRYPT_MODE, secretKey);
            byte[] encryptedBytes = cipher.doFinal(data.getBytes());
            return Base64.encodeToString(encryptedBytes, Base64.DEFAULT);
        } catch (Exception e) {
            Log.e(TAG, "Encryption error", e);
            return data; // Return unencrypted data on failure (for demo)
        }
    }
    
    // Decrypt medical record data
    public String decrypt(String encryptedData) {
        if (secretKey == null || encryptedData == null) return encryptedData;
        
        try {
            Cipher cipher = Cipher.getInstance(TRANSFORMATION);
            cipher.init(Cipher.DECRYPT_MODE, secretKey);
            byte[] decodedBytes = Base64.decode(encryptedData, Base64.DEFAULT);
            byte[] decryptedBytes = cipher.doFinal(decodedBytes);
            return new String(decryptedBytes);
        } catch (Exception e) {
            Log.e(TAG, "Decryption error", e);
            return encryptedData; // Return encrypted data on failure (for demo)
        }
    }
    
    // Medical record structure
    public static class MedicalRecord {
        private String id;
        private String patientId;
        private String recordType; // "lab_result", "immunization", "medical_history", "diagnosis"
        private String title;
        private String description;
        private String encryptedData;
        private Date createdAt;
        private String status; // "active", "archived", "deleted"
        private long updatedAt;
        
        public MedicalRecord() {}
        
        public MedicalRecord(String patientId, String recordType, String title,
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
        
        // Getters and setters
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
}