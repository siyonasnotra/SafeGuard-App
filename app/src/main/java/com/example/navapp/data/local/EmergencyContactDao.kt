package com.example.navapp.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.navapp.data.model.EmergencyContact
import kotlinx.coroutines.flow.Flow

@Dao
interface EmergencyContactDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertContact(contact: EmergencyContact): Long
    
    @Update
    suspend fun updateContact(contact: EmergencyContact)
    
    @Delete
    suspend fun deleteContact(contact: EmergencyContact)
    
    @Query("SELECT * FROM emergency_contacts WHERE contactId = :contactId")
    fun getContactById(contactId: Long): Flow<EmergencyContact?>
    
    @Query("SELECT * FROM emergency_contacts WHERE userId = :userId")
    fun getContactsForUser(userId: String): Flow<List<EmergencyContact>>
    
    @Query("SELECT * FROM emergency_contacts WHERE userId = :userId AND canReceiveSos = 1 ORDER BY priority")
    fun getSosContactsForUser(userId: String): Flow<List<EmergencyContact>>
    
    @Query("SELECT * FROM emergency_contacts WHERE userId = :userId AND canReceiveLocation = 1")
    fun getLocationSharingContactsForUser(userId: String): Flow<List<EmergencyContact>>
    
    @Query("DELETE FROM emergency_contacts WHERE userId = :userId")
    suspend fun deleteAllContactsForUser(userId: String)
} 