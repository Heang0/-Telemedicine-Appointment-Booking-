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
        if (mAuth.getCurrentUser() == null) {
            Log.e(TAG, "No authenticated user for patient stats");
            upcomingAppointments.postValue(new ArrayList<>());
            unreadMessages.postValue(0);
            prescriptionsDue.postValue(0);
            return;
        }
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

        // Load unread messages (client-side filter to avoid composite index)
        db.collection("messages")
                .whereEqualTo("recipientId", userId)
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    int count = 0;
                    for (QueryDocumentSnapshot doc : querySnapshot) {
                        Boolean read = doc.getBoolean("read");
                        if (read == null || !read) {
                            count++;
                        }
                    }
                    unreadMessages.postValue(count);
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error loading unread messages", e);
                    unreadMessages.postValue(0);
                });

        // Load prescriptions due (client-side filter to avoid composite index)
        db.collection("prescriptions")
                .whereEqualTo("patientId", userId)
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    int count = 0;
                    for (QueryDocumentSnapshot doc : querySnapshot) {
                        String status = doc.getString("status");
                        if ("pending".equalsIgnoreCase(status)) {
                            count++;
                        }
                    }
                    prescriptionsDue.postValue(count);
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error loading prescriptions", e);
                    prescriptionsDue.postValue(0);
                });
    }

    // For doctor: could override or add separate method
    public void loadDoctorStats() {
        if (mAuth.getCurrentUser() == null) {
            Log.e(TAG, "No authenticated user for doctor stats");
            upcomingAppointments.postValue(new ArrayList<>());
            unreadMessages.postValue(0);
            prescriptionsDue.postValue(0);
            return;
        }
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

        // Unread messages: client-side filter to avoid composite index
        db.collection("messages")
                .whereEqualTo("recipientId", userId)
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    int count = 0;
                    for (QueryDocumentSnapshot doc : querySnapshot) {
                        Boolean read = doc.getBoolean("read");
                        if (read == null || !read) {
                            count++;
                        }
                    }
                    unreadMessages.postValue(count);
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error loading doctor unread messages", e);
                    unreadMessages.postValue(0);
                });

        // Prescriptions due: client-side filter to avoid composite index
        db.collection("prescriptions")
                .whereEqualTo("doctorId", userId)
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    int count = 0;
                    for (QueryDocumentSnapshot doc : querySnapshot) {
                        String status = doc.getString("status");
                        if ("pending".equalsIgnoreCase(status)) {
                            count++;
                        }
                    }
                    prescriptionsDue.postValue(count);
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error loading doctor prescriptions", e);
                    prescriptionsDue.postValue(0);
                });
    }
}
