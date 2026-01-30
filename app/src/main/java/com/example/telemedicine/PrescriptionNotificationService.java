package com.example.telemedicine;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.util.Log;

import androidx.core.app.NotificationCompat;

import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.DocumentChange;

public class PrescriptionNotificationService {

    private static final String CHANNEL_ID = "prescription_channel";
    private static final int NOTIFICATION_ID = 1001;
    private static final String TAG = "PrescriptionNotification";

    private Context context;
    private NotificationManager notificationManager;
    private FirebaseFirestore db;
    private com.google.firebase.firestore.ListenerRegistration prescriptionListener;

    public PrescriptionNotificationService(Context context) {
        this.context = context;
        this.notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        this.db = FirebaseFirestore.getInstance();
        
        createNotificationChannel();
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = "Prescription Notifications";
            String description = "Notifications for new prescriptions";
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, name, importance);
            channel.setDescription(description);
            
            NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
            notificationManager.createNotificationChannel(channel);
        }
    }

    public void notifyPatientOfNewPrescription(String prescriptionId, String patientId, String doctorName) {
        // Create intent to open the prescription details when notification is clicked
        Intent intent = new Intent(context, MainActivity.class); // Change to appropriate activity
        intent.putExtra("notification_type", "prescription_update");
        intent.putExtra("prescription_id", prescriptionId);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        
        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent, 
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_prescription_notification) // You'll need to add this drawable
                .setContentTitle("New Prescription Available")
                .setContentText("Dr. " + doctorName + " has issued a new prescription for you.")
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true);

        notificationManager.notify(NOTIFICATION_ID, builder.build());
    }

    public void setupPrescriptionChangeListener(String patientId) {
        // Remove previous listener if exists
        if (prescriptionListener != null) {
            prescriptionListener.remove();
        }

        prescriptionListener = db.collection("prescriptions")
                .whereArrayContains("patientIds", patientId)
                .addSnapshotListener((querySnapshot, error) -> {
                    if (error != null) {
                        Log.w(TAG, "Listen failed.", error);
                        return;
                    }

                    if (querySnapshot != null) {
                        for (DocumentChange dc : querySnapshot.getDocumentChanges()) {
                            switch (dc.getType()) {
                                case ADDED:
                                    Prescription prescription = dc.getDocument().toObject(Prescription.class);
                                    notifyPatientOfNewPrescription(dc.getDocument().getId(), patientId, prescription.getDoctorName());
                                    break;
                                case MODIFIED:
                                    // Check if status changed and notify accordingly
                                    break;
                                case REMOVED:
                                    break;
                            }
                        }
                    }
                });
    }

    public void cleanup() {
        // Clean up the listener to prevent memory leaks
        if (prescriptionListener != null) {
            prescriptionListener.remove();
            prescriptionListener = null;
        }
    }
}
