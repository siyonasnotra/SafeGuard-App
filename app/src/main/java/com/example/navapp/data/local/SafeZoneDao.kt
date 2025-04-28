package com.example.navapp.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.navapp.data.model.SafeZone
import com.example.navapp.data.model.ZoneType
import kotlinx.coroutines.flow.Flow

@Dao
interface SafeZoneDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSafeZone(safeZone: SafeZone): Long
    
    @Update
    suspend fun updateSafeZone(safeZone: SafeZone)
    
    @Delete
    suspend fun deleteSafeZone(safeZone: SafeZone)
    
    @Query("SELECT * FROM safe_zones WHERE zoneId = :zoneId")
    fun getSafeZoneById(zoneId: Long): Flow<SafeZone?>
    
    @Query("SELECT * FROM safe_zones WHERE userId = :userId")
    fun getSafeZonesForUser(userId: String): Flow<List<SafeZone>>
    
    @Query("SELECT * FROM safe_zones WHERE userId = :userId AND zoneType = :zoneType")
    fun getSafeZonesByType(userId: String, zoneType: ZoneType): Flow<List<SafeZone>>
    
    @Query("""
        SELECT * FROM safe_zones 
        WHERE userId = :userId 
        AND ((:latitude - latitude) * (:latitude - latitude) + 
             (:longitude - longitude) * (:longitude - longitude)) <= (radius * radius / 111111.0 / 111111.0)
    """)
    fun getNearbyZones(userId: String, latitude: Double, longitude: Double): Flow<List<SafeZone>>
    
    @Query("DELETE FROM safe_zones WHERE userId = :userId")
    suspend fun deleteAllZonesForUser(userId: String)
} 