package com.example.telemedicine;

import android.util.Log;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class FirestoreHelper {
    private static final String TAG = "FirestoreHelper";
    private FirebaseFirestore db;
    private FirebaseAuth auth;

    public FirestoreHelper() {
        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();
    }

    // Get current user ID
    public String getCurrentUserId() {
        FirebaseUser currentUser = auth.getCurrentUser();
        return currentUser != null ? currentUser.getUid() : null;
    }

    // Save user profile data
    public Task<Void> saveUserData(String userId, Map<String, Object> userData) {
        return db.collection("users")
                .document(userId)
                .set(userData);
    }

    // Get user profile data
    public DocumentReference getUserData(String userId) {
        return db.collection("users").document(userId);
    }

    // Update user profile data
    public Task<Void> updateUserData(String userId, Map<String, Object> updates) {
        return db.collection("users")
                .document(userId)
                .update(updates);
    }

    // Save patient medical record
    public Task<DocumentReference> savePatientRecord(String userId, Map<String, Object> recordData) {
        return db.collection("users")
                .document(userId)
                .collection("medical_records")
                .add(recordData);
    }

    // Get patient records
    public Task<QuerySnapshot> getPatientRecords(String userId) {
        return db.collection("users")
                .document(userId)
                .collection("medical_records")
                .get();
    }

    // Save appointment
    public Task<DocumentReference> saveAppointment(Map<String, Object> appointmentData) {
        return db.collection("appointments")
                .add(appointmentData);
    }

    // Save appointment with enhanced data structure
    public Task<DocumentReference> saveAppointment(String patientId, String doctorId, String patientName,
                                                   String doctorName, Date appointmentDate, String status,
                                                   String reason, String consultationType, String location,
                                                   String meetingLink) {
        Map<String, Object> appointmentData = new HashMap<>();
        appointmentData.put("patientId", patientId);
        appointmentData.put("doctorId", doctorId);
        appointmentData.put("patientName", patientName);
        appointmentData.put("doctorName", doctorName);
        appointmentData.put("appointmentDate", appointmentDate);
        appointmentData.put("status", status);
        appointmentData.put("reason", reason);
        appointmentData.put("consultationType", consultationType);
        appointmentData.put("location", location);
        appointmentData.put("meetingLink", meetingLink);
        appointmentData.put("createdAt", System.currentTimeMillis());
        appointmentData.put("updatedAt", System.currentTimeMillis());

        return db.collection("appointments").add(appointmentData);
    }

    // Get appointments for user
    public Task<QuerySnapshot> getUserAppointments(String userId) {
        return db.collection("appointments")
                .whereEqualTo("patientId", userId)
                .get();
    }

    // Save symptom form
    public Task<DocumentReference> saveSymptomForm(Map<String, Object> symptomFormData) {
        return db.collection("symptom_forms")
                .add(symptomFormData);
    }

    // Get symptom forms for appointment
    public Task<QuerySnapshot> getSymptomFormsForAppointment(String appointmentId) {
        return db.collection("symptom_forms")
                .whereEqualTo("appointmentId", appointmentId)
                .get();
    }

    // Save medical record
    public Task<DocumentReference> saveMedicalRecord(Map<String, Object> medicalRecordData) {
        return db.collection("medical_records")
                .add(medicalRecordData);
    }

    // Get medical records for patient
    public Task<QuerySnapshot> getMedicalRecordsForPatient(String patientId) {
        return db.collection("medical_records")
                .whereEqualTo("patientId", patientId)
                .get();
    }

    // Save pharmacy
    public Task<DocumentReference> savePharmacy(Map<String, Object> pharmacyData) {
        return db.collection("pharmacies")
                .add(pharmacyData);
    }

    // Get pharmacies by location
    public Task<QuerySnapshot> getPharmaciesNearLocation(String city, String state) {
        return db.collection("pharmacies")
                .whereEqualTo("city", city)
                .whereEqualTo("state", state)
                .get();
    }

    // Add other data operations as needed
    public FirebaseFirestore getDb() {
        return db;
    }
}