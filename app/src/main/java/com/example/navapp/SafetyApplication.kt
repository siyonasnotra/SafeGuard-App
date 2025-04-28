package com.example.navapp

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.room.Room
import com.example.navapp.data.local.SafetyDatabase

class SafetyApplication : Application() {
    
    companion object {
        // Database instance that will be initialized once and shared across the app
        lateinit var database: SafetyDatabase
            private set
        
        // Notification channel IDs
        const val LOCATION_CHANNEL_ID = "location_tracking_channel"
        const val SOS_CHANNEL_ID = "sos_alert_channel"
    }
    
    override fun onCreate() {
        super.onCreate()
        
        // Initialize Room database
        database = Room.databaseBuilder(
            applicationContext,
            SafetyDatabase::class.java,
            "safety_database"
        ).build()
        
        // Create notification channels
        createNotificationChannels()
    }
    
    private fun createNotificationChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            // Location tracking channel
            val locationChannel = NotificationChannel(
                LOCATION_CHANNEL_ID,
                "Location Tracking",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Used for tracking your location in the background"
                enableVibration(true)
            }
            
            // SOS alert channel
            val sosChannel = NotificationChannel(
                SOS_CHANNEL_ID,
                "SOS Alerts",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Used for emergency SOS alerts"
                enableVibration(true)
            }
            
            // Register the channels with the system
            val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(locationChannel)
            notificationManager.createNotificationChannel(sosChannel)
        }
    }
} 