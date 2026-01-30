package com.example.telemedicine;

import android.util.Log;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

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

    // Get appointments for user
    public Task<QuerySnapshot> getUserAppointments(String userId) {
        return db.collection("appointments")
                .whereEqualTo("patientId", userId)
                .get();
    }

    // Add other data operations as needed
    public FirebaseFirestore getDb() {
        return db;
    }
}