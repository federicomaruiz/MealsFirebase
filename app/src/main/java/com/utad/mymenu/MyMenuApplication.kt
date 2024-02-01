package com.utad.mymenu

import android.app.Application
import android.util.Log
import com.google.firebase.messaging.FirebaseMessaging
import com.google.firebase.messaging.FirebaseMessagingService

class MyMenuApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        val tag = "MyMenuFirebase"
        FirebaseMessaging.getInstance().token.addOnCompleteListener { task ->
            if (task.isSuccessful) {
                val token =
                    task.result  // Guardo el token para saber que usuarios estan activos y cuales no para enviar notificaciones
                Log.i("$tag", "Se ha creado el token $token")
            } else {
                Log.i("$tag", "FALLO ${task.exception}")
            }
        }
    }
}