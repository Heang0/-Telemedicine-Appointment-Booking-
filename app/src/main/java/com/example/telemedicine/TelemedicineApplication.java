package com.example.telemedicine;

import android.app.Application;
import android.os.Environment;
import android.util.Log;

import androidx.annotation.NonNull;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.DocumentSnapshot;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class TelemedicineApplication extends Application {
    private static final String TAG = "TelemedicineApp";
    private static TelemedicineApplication instance;
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private User currentUserProfile;

    @Override
    public void onCreate() {
        super.onCreate();
        instance = this;

        // Install global crash reporter
        Thread.setDefaultUncaughtExceptionHandler((thread, throwable) -> {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", java.util.Locale.getDefault());
            String timestamp = sdf.format(new Date());
            String crashReport = String.format("CRASH at %s\nThread: %s\nException: %s\nStackTrace:\n%s",
                    timestamp, thread.getName(), throwable.toString(), Log.getStackTraceString(throwable));

            // Write to Downloads folder (accessible via file manager)
            try {
                File downloadDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
                File crashFile = new File(downloadDir, "crash_telemedicine.txt");
                FileWriter writer = new FileWriter(crashFile, false); // overwrite
                writer.write(crashReport);
                writer.close();
                Log.e(TAG, "Crash saved to: " + crashFile.getAbsolutePath());
            } catch (IOException e) {
                Log.e(TAG, "Failed to save crash report", e);
            }

            // Terminate process
            android.os.Process.killProcess(android.os.Process.myPid());
        });

        // Apply theme based on saved preference
        ThemeUtils.applyTheme(this);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        // Listen for auth state changes
        mAuth.addAuthStateListener(auth -> {
            FirebaseUser user = auth.getCurrentUser();
            if (user != null) {
                loadUserProfile(user.getUid());
            } else {
                currentUserProfile = null;
            }
        });
    }

    public static TelemedicineApplication getInstance() {
        return instance;
    }

    public FirebaseAuth getAuth() {
        return mAuth;
    }

    public FirebaseFirestore getFirestore() {
        return db;
    }

    public User getCurrentUserProfile() {
        return currentUserProfile;
    }

    private void loadUserProfile(String userId) {
        db.collection("users")
                .document(userId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        currentUserProfile = documentSnapshot.toObject(User.class);
                        Log.d(TAG, "User profile loaded: " + currentUserProfile.getFullName());
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error loading user profile", e);
                });
    }

    public boolean isLoggedIn() {
        return mAuth.getCurrentUser() != null;
    }

    public String getCurrentUserId() {
        FirebaseUser user = mAuth.getCurrentUser();
        return user != null ? user.getUid() : null;
    }
}