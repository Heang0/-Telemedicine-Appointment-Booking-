package com.example.telemedicine.repository;

import android.util.Log;

import androidx.annotation.NonNull;

import com.example.telemedicine.model.Prescription;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.HashMap;
import java.util.Map;

public class PrescriptionRepository {
    private static final String TAG = "PrescriptionRepository";
    private FirebaseFirestore db;
    private CollectionReference prescriptionsCollection;

    public PrescriptionRepository() {
        this.db = FirebaseFirestore.getInstance();
        this.prescriptionsCollection = db.collection("prescriptions");
    }

    // Save prescription
    public Task<DocumentReference> savePrescription(Prescription prescription) {
        Map<String, Object> prescriptionData = new HashMap<>();
        prescriptionData.put("patientId", prescription.getPatientId());
        prescriptionData.put("doctorId", prescription.getDoctorId());
        prescriptionData.put("patientName", prescription.getPatientName());
        prescriptionData.put("doctorName", prescription.getDoctorName());
        prescriptionData.put("doctorSpecialty", prescription.getDoctorSpecialty());
        prescriptionData.put("appointmentId", prescription.getAppointmentId());
        prescriptionData.put("medications", prescription.getMedications());
        prescriptionData.put("instructions", prescription.getInstructions());
        prescriptionData.put("prescribedDate", prescription.getPrescribedDate());
        prescriptionData.put("expiryDate", prescription.getExpiryDate());
        prescriptionData.put("status", prescription.getStatus());
        prescriptionData.put("notes", prescription.getNotes());
        prescriptionData.put("createdAt", prescription.getCreatedAt());
        prescriptionData.put("updatedAt", prescription.getUpdatedAt());

        return prescriptionsCollection.add(prescriptionData);
    }

    // Get prescriptions for patient
    public Task<QuerySnapshot> getPatientPrescriptions(String patientId) {
        return prescriptionsCollection
                .whereEqualTo("patientId", patientId)
                .get();
    }

    // Get prescriptions by doctor
    public Task<QuerySnapshot> getDoctorPrescriptions(String doctorId) {
        return prescriptionsCollection
                .whereEqualTo("doctorId", doctorId)
                .get();
    }

    // Update prescription status
    public Task<Void> updatePrescriptionStatus(String prescriptionId, String status) {
        return prescriptionsCollection.document(prescriptionId).update("status", status, "updatedAt", System.currentTimeMillis());
    }

    // Get prescription by ID
    public DocumentReference getPrescription(String prescriptionId) {
        return prescriptionsCollection.document(prescriptionId);
    }
}