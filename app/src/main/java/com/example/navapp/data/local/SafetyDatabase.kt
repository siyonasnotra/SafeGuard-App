package com.example.navapp.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.example.navapp.data.model.EmergencyContact
import com.example.navapp.data.model.SOSEvent
import com.example.navapp.data.model.SafeZone
import com.example.navapp.data.model.User

@Database(
    entities = [
        User::class,
        EmergencyContact::class,
        SafeZone::class,
        SOSEvent::class
    ],
    version = 1,
    exportSchema = false
)
abstract class SafetyDatabase : RoomDatabase() {
    abstract fun userDao(): UserDao
    abstract fun emergencyContactDao(): EmergencyContactDao
    abstract fun safeZoneDao(): SafeZoneDao
    abstract fun sosEventDao(): SOSEventDao
} 