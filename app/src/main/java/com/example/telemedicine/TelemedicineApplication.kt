package com.example.telemedicine

import android.app.Application
import com.google.firebase.FirebaseApp

class TelemedicineApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        // Initialize Firebase (required for auth/firestore)
        FirebaseApp.initializeApp(this)
    }
}