package com.example.navapp.data.repository

import com.example.navapp.SafetyApplication
import com.example.navapp.data.model.SOSEvent
import com.example.navapp.data.model.SOSStatus
import kotlinx.coroutines.flow.Flow

class SOSRepository {
    private val sosEventDao = SafetyApplication.database.sosEventDao()
    
    suspend fun triggerSOS(userId: String, latitude: Double, longitude: Double): Long {
        val event = SOSEvent(
            userId = userId,
            timestamp = System.currentTimeMillis(),
            latitude = latitude,
            longitude = longitude,
            status = SOSStatus.ACTIVE
        )
        return sosEventDao.insertSOSEvent(event)
    }
    
    suspend fun resolveSOS(eventId: Long, notes: String? = null) {
        sosEventDao.updateSOSEventStatus(
            eventId = eventId,
            newStatus = SOSStatus.RESOLVED,
            resolvedTimestamp = System.currentTimeMillis(),
            notes = notes
        )
    }
    
    suspend fun cancelSOS(eventId: Long, notes: String? = null) {
        sosEventDao.updateSOSEventStatus(
            eventId = eventId,
            newStatus = SOSStatus.CANCELLED,
            resolvedTimestamp = System.currentTimeMillis(),
            notes = notes
        )
    }
    
    fun getSOSEventsForUser(userId: String): Flow<List<SOSEvent>> {
        return sosEventDao.getSOSEventsForUser(userId)
    }
    
    fun getSOSEventsByStatus(userId: String, status: SOSStatus): Flow<List<SOSEvent>> {
        return sosEventDao.getSOSEventsByStatus(userId, status)
    }
    
    fun getLatestActiveSOSEvent(userId: String): Flow<SOSEvent?> {
        return sosEventDao.getLatestSOSEvent(userId, SOSStatus.ACTIVE)
    }
    
    fun getSOSEventById(eventId: Long): Flow<SOSEvent?> {
        return sosEventDao.getSOSEventById(eventId)
    }
    
    suspend fun deleteSOSEvent(event: SOSEvent) {
        sosEventDao.deleteSOSEvent(event)
    }
    
    suspend fun deleteAllEventsForUser(userId: String) {
        sosEventDao.deleteAllEventsForUser(userId)
    }
} 