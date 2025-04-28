package com.example.navapp.data.model

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "sos_events",
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
data class SOSEvent(
    @PrimaryKey(autoGenerate = true)
    val eventId: Long = 0,
    val userId: String,
    val timestamp: Long,
    val latitude: Double,
    val longitude: Double,
    val status: SOSStatus = SOSStatus.ACTIVE,
    val resolvedTimestamp: Long? = null,
    val notes: String? = null
)

enum class SOSStatus {
    ACTIVE, RESOLVED, CANCELLED
} 