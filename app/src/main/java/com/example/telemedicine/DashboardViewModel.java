package com.example.telemedicine;

import android.app.Application;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class DashboardViewModel extends AndroidViewModel {
    private static final String TAG = "DashboardViewModel";

    private final MutableLiveData<List<Appointment>> upcomingAppointments = new MutableLiveData<>();
    private final MutableLiveData<Integer> unreadMessages = new MutableLiveData<>();
    private final MutableLiveData<Integer> prescriptionsDue = new MutableLiveData<>();

    private final FirebaseAuth mAuth;
    private final FirebaseFirestore db;

    public DashboardViewModel(@NonNull Application application) {
        super(application);
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
    }

    public LiveData<List<Appointment>> getUpcomingAppointments() {
        return upcomingAppointments;
    }

    public LiveData<Integer> getUnreadMessages() {
        return unreadMessages;
    }

    public LiveData<Integer> getPrescriptionsDue() {
        return prescriptionsDue;
    }

    public void loadPatientStats() {
        String userId = mAuth.getCurrentUser().getUid();

        // Load upcoming appointments
        db.collection("appointments")
                .whereEqualTo("patientId", userId)
                .addSnapshotListener((querySnapshot, error) -> {
                    if (error != null) {
                        Log.e(TAG, "Error loading appointments", error);
                        return;
                    }
                    if (querySnapshot != null) {
                        List<Appointment> list = new ArrayList<>();
                        for (QueryDocumentSnapshot doc : querySnapshot) {
                            Appointment appt = doc.toObject(Appointment.class);
                            appt.setId(doc.getId());
                            if (!"completed".equalsIgnoreCase(appt.getStatus()) &&
                                !"cancelled".equalsIgnoreCase(appt.getStatus())) {
                                list.add(appt);
                            }
                        }
                        upcomingAppointments.postValue(list);
                    }
                });

        // Load unread messages (simplified: count messages where read=false and recipient=userId)
        db.collection("messages")
                .whereEqualTo("recipientId", userId)
                .whereEqualTo("read", false)
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    unreadMessages.postValue(querySnapshot.size());
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error loading unread messages", e);
                    unreadMessages.postValue(0);
                });

        // Load prescriptions due (simplified: count prescriptions with status="pending" and patientId=userId)
        db.collection("prescriptions")
                .whereEqualTo("patientId", userId)
                .whereEqualTo("status", "pending")
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    prescriptionsDue.postValue(querySnapshot.size());
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error loading prescriptions", e);
                    prescriptionsDue.postValue(0);
                });
    }

    // For doctor: could override or add separate method
    public void loadDoctorStats() {
        String userId = mAuth.getCurrentUser().getUid();

        // Upcoming appointments for doctor
        db.collection("appointments")
                .whereEqualTo("doctorId", userId)
                .addSnapshotListener((querySnapshot, error) -> {
                    if (error != null) {
                        Log.e(TAG, "Error loading doctor appointments", error);
                        return;
                    }
                    if (querySnapshot != null) {
                        List<Appointment> list = new ArrayList<>();
                        for (QueryDocumentSnapshot doc : querySnapshot) {
                            Appointment appt = doc.toObject(Appointment.class);
                            appt.setId(doc.getId());
                            if (!"completed".equalsIgnoreCase(appt.getStatus()) &&
                                !"cancelled".equalsIgnoreCase(appt.getStatus())) {
                                list.add(appt);
                            }
                        }
                        upcomingAppointments.postValue(list);
                    }
                });

        // Unread messages: same as patient (if doctor is recipient)
        db.collection("messages")
                .whereEqualTo("recipientId", userId)
                .whereEqualTo("read", false)
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    unreadMessages.postValue(querySnapshot.size());
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error loading doctor unread messages", e);
                    unreadMessages.postValue(0);
                });

        // Prescriptions due: doctor may have prescriptions to approve
        db.collection("prescriptions")
                .whereEqualTo("doctorId", userId)
                .whereEqualTo("status", "pending")
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    prescriptionsDue.postValue(querySnapshot.size());
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error loading doctor prescriptions", e);
                    prescriptionsDue.postValue(0);
                });
    }
}