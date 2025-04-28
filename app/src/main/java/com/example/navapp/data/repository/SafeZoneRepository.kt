package com.example.navapp.data.repository

import com.example.navapp.SafetyApplication
import com.example.navapp.data.model.SafeZone
import com.example.navapp.data.model.ZoneType
import kotlinx.coroutines.flow.Flow

class SafeZoneRepository {
    private val safeZoneDao = SafetyApplication.database.safeZoneDao()
    
    suspend fun addSafeZone(
        userId: String,
        name: String,
        latitude: Double,
        longitude: Double,
        radius: Float = 100f,
        address: String? = null,
        zoneType: ZoneType = ZoneType.CUSTOM,
        notes: String? = null
    ): Long {
        val zone = SafeZone(
            userId = userId,
            name = name,
            latitude = latitude,
            longitude = longitude,
            radius = radius,
            address = address,
            zoneType = zoneType,
            notes = notes
        )
        return safeZoneDao.insertSafeZone(zone)
    }
    
    suspend fun updateSafeZone(zone: SafeZone) {
        safeZoneDao.updateSafeZone(zone)
    }
    
    suspend fun deleteSafeZone(zone: SafeZone) {
        safeZoneDao.deleteSafeZone(zone)
    }
    
    fun getSafeZonesForUser(userId: String): Flow<List<SafeZone>> {
        return safeZoneDao.getSafeZonesForUser(userId)
    }
    
    fun getSafeZonesByType(userId: String, zoneType: ZoneType): Flow<List<SafeZone>> {
        return safeZoneDao.getSafeZonesByType(userId, zoneType)
    }
    
    fun getNearbyZones(userId: String, latitude: Double, longitude: Double): Flow<List<SafeZone>> {
        return safeZoneDao.getNearbyZones(userId, latitude, longitude)
    }
    
    fun getSafeZoneById(zoneId: Long): Flow<SafeZone?> {
        return safeZoneDao.getSafeZoneById(zoneId)
    }
    
    suspend fun deleteAllZonesForUser(userId: String) {
        safeZoneDao.deleteAllZonesForUser(userId)
    }
} 