package com.example.navapp.utils

import android.content.Context
import android.content.Intent
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.media.RingtoneManager
import android.net.Uri
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.telephony.SmsManager
import androidx.core.content.ContextCompat
import com.example.navapp.data.model.EmergencyContact
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

object SOSUtils {
    private var mediaPlayer: MediaPlayer? = null
    private var isAlarmPlaying = false
    
    // Send SOS SMS to all emergency contacts
    @Suppress("DEPRECATION")
    suspend fun sendSOSMessage(
        context: Context,
        contacts: List<EmergencyContact>,
        message: String,
        locationUrl: String? = null
    ): Boolean = withContext(Dispatchers.IO) {
        try {
            val fullMessage = if (locationUrl != null) {
                "$message\nMy current location: $locationUrl"
            } else {
                message
            }
            
            contacts.forEach { contact ->
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                    val smsManager = context.getSystemService(SmsManager::class.java)
                    smsManager.sendTextMessage(
                        contact.phone,
                        null,
                        fullMessage,
                        null,
                        null
                    )
                } else {
                    val smsManager = SmsManager.getDefault()
                    smsManager.sendTextMessage(
                        contact.phone,
                        null,
                        fullMessage,
                        null,
                        null
                    )
                }
            }
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }
    
    // Make an emergency call
    fun makeEmergencyCall(context: Context, phoneNumber: String) {
        val intent = Intent(Intent.ACTION_CALL)
        intent.data = Uri.parse("tel:$phoneNumber")
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
        context.startActivity(intent)
    }
    
    // Start playing a loud alarm sound
    fun startAlarm(context: Context) {
        if (isAlarmPlaying) return
        
        try {
            val alarmSound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)
            
            mediaPlayer = MediaPlayer().apply {
                setAudioAttributes(
                    AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_ALARM)
                        .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                        .build()
                )
                setDataSource(context, alarmSound)
                isLooping = true
                prepare()
                start()
            }
            
            isAlarmPlaying = true
            startVibration(context)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
    
    // Stop the alarm sound
    fun stopAlarm() {
        mediaPlayer?.apply {
            if (isPlaying) {
                stop()
            }
            release()
        }
        mediaPlayer = null
        isAlarmPlaying = false
    }
    
    // Start device vibration
    private fun startVibration(context: Context) {
        val vibrationPattern = longArrayOf(0, 1000, 500, 1000, 500, 1000)
        
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val vibratorManager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager
            val vibrator = vibratorManager.defaultVibrator
            
            val vibrationEffect = VibrationEffect.createWaveform(
                vibrationPattern,
                0
            )
            vibrator.vibrate(vibrationEffect)
        } else {
            @Suppress("DEPRECATION")
            val vibrator = context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
            
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val vibrationEffect = VibrationEffect.createWaveform(
                    vibrationPattern,
                    0
                )
                vibrator.vibrate(vibrationEffect)
            } else {
                @Suppress("DEPRECATION")
                vibrator.vibrate(vibrationPattern, 0)
            }
        }
    }
    
    // Stop vibration
    @Suppress("DEPRECATION")
    fun stopVibration(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val vibratorManager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager
            val vibrator = vibratorManager.defaultVibrator
            vibrator.cancel()
        } else {
            val vibrator = context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
            vibrator.cancel()
        }
    }
    
    // Generate SOS message text
    fun generateSOSMessage(userName: String, customMessage: String? = null): String {
        return if (customMessage.isNullOrBlank()) {
            "EMERGENCY ALERT: $userName needs urgent help! This is an automated SOS message."
        } else {
            "EMERGENCY ALERT: $userName needs urgent help! $customMessage"
        }
    }
} 