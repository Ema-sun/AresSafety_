package com.ares.safety

import android.app.Application
import com.google.firebase.FirebaseApp

class AresApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        // Inicializar Firebase
        FirebaseApp.initializeApp(this)
    }
}