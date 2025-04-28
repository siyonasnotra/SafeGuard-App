package com.example.navapp.data.model

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "safe_zones",
    foreignKeys = [
        ForeignKey(
            entity = User::class,
            parentColumns = ["userId"],
            childColumns = ["userId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("userId")]
)
data class SafeZone(
    @PrimaryKey(autoGenerate = true)
    val zoneId: Long = 0,
    val userId: String,
    val name: String,
    val latitude: Double,
    val longitude: Double,
    // Radius in meters
    val radius: Float = 100f,
    val address: String? = null,
    val zoneType: ZoneType = ZoneType.CUSTOM,
    val notes: String? = null
)

enum class ZoneType {
    HOME, WORK, SCHOOL, HOSPITAL, POLICE_STATION, CUSTOM
} 