package com.example.navapp.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.navapp.data.model.SOSEvent
import com.example.navapp.data.model.SOSStatus
import kotlinx.coroutines.flow.Flow

@Dao
interface SOSEventDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSOSEvent(sosEvent: SOSEvent): Long
    
    @Update
    suspend fun updateSOSEvent(sosEvent: SOSEvent)
    
    @Delete
    suspend fun deleteSOSEvent(sosEvent: SOSEvent)
    
    @Query("SELECT * FROM sos_events WHERE eventId = :eventId")
    fun getSOSEventById(eventId: Long): Flow<SOSEvent?>
    
    @Query("SELECT * FROM sos_events WHERE userId = :userId ORDER BY timestamp DESC")
    fun getSOSEventsForUser(userId: String): Flow<List<SOSEvent>>
    
    @Query("SELECT * FROM sos_events WHERE userId = :userId AND status = :status ORDER BY timestamp DESC")
    fun getSOSEventsByStatus(userId: String, status: SOSStatus): Flow<List<SOSEvent>>
    
    @Query("""
        UPDATE sos_events 
        SET status = :newStatus, resolvedTimestamp = :resolvedTimestamp, notes = :notes 
        WHERE eventId = :eventId
    """)
    suspend fun updateSOSEventStatus(eventId: Long, newStatus: SOSStatus, resolvedTimestamp: Long, notes: String?)
    
    @Query("SELECT * FROM sos_events WHERE userId = :userId AND status = :status ORDER BY timestamp DESC LIMIT 1")
    fun getLatestSOSEvent(userId: String, status: SOSStatus): Flow<SOSEvent?>
    
    @Query("DELETE FROM sos_events WHERE userId = :userId")
    suspend fun deleteAllEventsForUser(userId: String)
} 