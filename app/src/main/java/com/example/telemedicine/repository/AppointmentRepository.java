package com.example.telemedicine.repository;

import android.util.Log;

import androidx.annotation.NonNull;

import com.example.telemedicine.model.Appointment;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.HashMap;
import java.util.Map;

public class AppointmentRepository {
    private static final String TAG = "AppointmentRepository";
    private FirebaseFirestore db;
    private CollectionReference appointmentsCollection;

    public AppointmentRepository() {
        this.db = FirebaseFirestore.getInstance();
        this.appointmentsCollection = db.collection("appointments");
    }

    // Save appointment
    public Task<DocumentReference> saveAppointment(Appointment appointment) {
        Map<String, Object> appointmentData = new HashMap<>();
        appointmentData.put("patientId", appointment.getPatientId());
        appointmentData.put("doctorId", appointment.getDoctorId());
        appointmentData.put("patientName", appointment.getPatientName());
        appointmentData.put("doctorName", appointment.getDoctorName());
        appointmentData.put("appointmentDate", appointment.getAppointmentDate());
        appointmentData.put("status", appointment.getStatus());
        appointmentData.put("reason", appointment.getReason());
        appointmentData.put("consultationType", appointment.getConsultationType());
        appointmentData.put("location", appointment.getLocation());
        appointmentData.put("meetingLink", appointment.getMeetingLink());
        appointmentData.put("createdAt", appointment.getCreatedAt());
        appointmentData.put("updatedAt", appointment.getUpdatedAt());

        return appointmentsCollection.add(appointmentData);
    }

    // Get appointments for user
    public Task<QuerySnapshot> getUserAppointments(String userId) {
        return appointmentsCollection
                .whereEqualTo("patientId", userId)
                .get();
    }

    // Get appointments by doctor
    public Task<QuerySnapshot> getDoctorAppointments(String doctorId) {
        return appointmentsCollection
                .whereEqualTo("doctorId", doctorId)
                .get();
    }

    // Update appointment status
    public Task<Void> updateAppointmentStatus(String appointmentId, String status) {
        return appointmentsCollection.document(appointmentId).update("status", status, "updatedAt", System.currentTimeMillis());
    }

    // Get appointment by ID
    public DocumentReference getAppointment(String appointmentId) {
        return appointmentsCollection.document(appointmentId);
    }
}