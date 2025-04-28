package com.example.navapp.data.model

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "emergency_contacts",
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
data class EmergencyContact(
    @PrimaryKey(autoGenerate = true)
    val contactId: Long = 0,
    val userId: String,
    val name: String,
    val phone: String,
    val relationship: String,
    val canReceiveLocation: Boolean = true,
    val canReceiveSos: Boolean = true,
    // Priority level for SOS alerts (e.g., 1 = highest priority)
    val priority: Int = 1
) 