package com.example.navapp.service

import android.app.Notification
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.location.Location
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.example.navapp.MainActivity
import com.example.navapp.R
import com.example.navapp.SafetyApplication
import com.example.navapp.utils.LocationUtils
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

class LocationTrackingService : Service() {
    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private var locationTrackingJob: Job? = null
    
    companion object {
        private const val NOTIFICATION_ID = 1
        private const val LOCATION_UPDATE_INTERVAL = 10000L // 10 seconds
        
        // Intent actions
        const val ACTION_START = "com.example.navapp.TRACKING_START"
        const val ACTION_STOP = "com.example.navapp.TRACKING_STOP"
        
        // Intent extras
        const val EXTRA_ACTIVITY_TYPE = "activity_type"
        const val EXTRA_USER_ID = "user_id"
        
        // Start the service
        fun startService(context: Context, userId: String) {
            val intent = Intent(context, LocationTrackingService::class.java).apply {
                action = ACTION_START
                putExtra(EXTRA_USER_ID, userId)
            }
            context.startForegroundService(intent)
        }
        
        // Stop the service
        fun stopService(context: Context) {
            val intent = Intent(context, LocationTrackingService::class.java).apply {
                action = ACTION_STOP
            }
            context.startService(intent)
        }
    }
    
    override fun onCreate() {
        super.onCreate()
        // This is where you'd initialize any resources that are used throughout the service lifetime
    }
    
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_START -> {
                val userId = intent.getStringExtra(EXTRA_USER_ID) ?: return START_NOT_STICKY
                startLocationTracking(userId)
            }
            ACTION_STOP -> {
                stopLocationTracking()
                stopSelf()
            }
        }
        return START_STICKY
    }
    
    override fun onBind(intent: Intent?): IBinder? = null
    
    private fun startLocationTracking(userId: String) {
        startForeground(NOTIFICATION_ID, createNotification())
        
        locationTrackingJob?.cancel()
        locationTrackingJob = LocationUtils.getLocationUpdates(this, LOCATION_UPDATE_INTERVAL)
            .onEach { location -> 
                // Here you'd update the repository with the new location
                // and potentially share it with emergency contacts if needed
                updateLocationNotification(location)
            }
            .catch { e -> 
                e.printStackTrace()
                stopSelf() 
            }
            .launchIn(serviceScope)
    }
    
    private fun stopLocationTracking() {
        locationTrackingJob?.cancel()
        locationTrackingJob = null
        stopForeground(true)
    }
    
    private fun createNotification(): Notification {
        val notificationIntent = Intent(this, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            this, 0, notificationIntent, 
            PendingIntent.FLAG_IMMUTABLE
        )
        
        return NotificationCompat.Builder(this, SafetyApplication.LOCATION_CHANNEL_ID)
            .setContentTitle("Location Tracking")
            .setContentText("Your location is being tracked for safety")
            .setSmallIcon(R.drawable.ic_launcher_foreground)  // Replace with appropriate icon
            .setContentIntent(pendingIntent)
            .setOngoing(true)
            .build()
    }
    
    private fun updateLocationNotification(location: Location) {
        val notification = NotificationCompat.Builder(this, SafetyApplication.LOCATION_CHANNEL_ID)
            .setContentTitle("Location Tracking")
            .setContentText("Location: ${location.latitude}, ${location.longitude}")
            .setSmallIcon(R.drawable.ic_launcher_foreground)  // Replace with appropriate icon
            .setOngoing(true)
            .build()
            
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(NOTIFICATION_ID, notification)
    }
    
    override fun onDestroy() {
        serviceScope.cancel()
        super.onDestroy()
    }
} 