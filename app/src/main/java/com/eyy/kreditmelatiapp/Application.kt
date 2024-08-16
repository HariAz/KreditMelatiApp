package com.eyy.kreditmelatiapp

import android.app.Application
import com.google.firebase.FirebaseApp

class MyApp : Application() {
    override fun onCreate() {
        super.onCreate()
        // Inisialisasi Firebase
        FirebaseApp.initializeApp(this)
    }
}