package com.example.navapp.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.navapp.data.model.SafeZone
import com.example.navapp.data.model.ZoneType
import com.example.navapp.data.repository.SafeZoneRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch

class SafeZoneViewModel : ViewModel() {
    private val safeZoneRepository = SafeZoneRepository()
    
    private val _safeZonesState = MutableStateFlow<SafeZonesState>(SafeZonesState.Loading)
    val safeZonesState: StateFlow<SafeZonesState> = _safeZonesState
    
    private val _nearbyZonesState = MutableStateFlow<NearbyZonesState>(NearbyZonesState.Loading)
    val nearbyZonesState: StateFlow<NearbyZonesState> = _nearbyZonesState
    
    private val _zoneOperationState = MutableStateFlow<ZoneOperationState>(ZoneOperationState.Idle)
    val zoneOperationState: StateFlow<ZoneOperationState> = _zoneOperationState
    
    // Load all safe zones for a user
    fun loadSafeZonesForUser(userId: String) {
        viewModelScope.launch {
            _safeZonesState.value = SafeZonesState.Loading
            safeZoneRepository.getSafeZonesForUser(userId)
                .catch { e ->
                    _safeZonesState.value = SafeZonesState.Error(e.message ?: "Failed to load safe zones")
                }
                .collect { zones ->
                    _safeZonesState.value = SafeZonesState.Success(zones)
                }
        }
    }
    
    // Load safe zones by type
    fun loadSafeZonesByType(userId: String, zoneType: ZoneType) {
        viewModelScope.launch {
            _safeZonesState.value = SafeZonesState.Loading
            safeZoneRepository.getSafeZonesByType(userId, zoneType)
                .catch { e ->
                    _safeZonesState.value = SafeZonesState.Error(e.message ?: "Failed to load safe zones")
                }
                .collect { zones ->
                    _safeZonesState.value = SafeZonesState.Success(zones)
                }
        }
    }
    
    // Check for nearby safe zones
    fun checkNearbyZones(userId: String, latitude: Double, longitude: Double) {
        viewModelScope.launch {
            _nearbyZonesState.value = NearbyZonesState.Loading
            safeZoneRepository.getNearbyZones(userId, latitude, longitude)
                .catch { e ->
                    _nearbyZonesState.value = NearbyZonesState.Error(e.message ?: "Failed to check nearby zones")
                }
                .collect { zones ->
                    _nearbyZonesState.value = if (zones.isNotEmpty()) {
                        NearbyZonesState.Found(zones)
                    } else {
                        NearbyZonesState.NotFound
                    }
                }
        }
    }
    
    // Add a new safe zone
    fun addSafeZone(
        userId: String,
        name: String,
        latitude: Double,
        longitude: Double,
        radius: Float = 100f,
        address: String? = null,
        zoneType: ZoneType = ZoneType.CUSTOM,
        notes: String? = null
    ) {
        if (name.isBlank()) {
            _zoneOperationState.value = ZoneOperationState.Error("Zone name is required")
            return
        }
        
        viewModelScope.launch {
            _zoneOperationState.value = ZoneOperationState.InProgress
            try {
                val zoneId = safeZoneRepository.addSafeZone(
                    userId, name, latitude, longitude, radius, address, zoneType, notes
                )
                _zoneOperationState.value = ZoneOperationState.Success("Safe zone added successfully")
                loadSafeZonesForUser(userId)
            } catch (e: Exception) {
                _zoneOperationState.value = ZoneOperationState.Error(e.message ?: "Failed to add safe zone")
            }
        }
    }
    
    // Update an existing safe zone
    fun updateSafeZone(zone: SafeZone) {
        viewModelScope.launch {
            _zoneOperationState.value = ZoneOperationState.InProgress
            try {
                safeZoneRepository.updateSafeZone(zone)
                _zoneOperationState.value = ZoneOperationState.Success("Safe zone updated successfully")
                loadSafeZonesForUser(zone.userId)
            } catch (e: Exception) {
                _zoneOperationState.value = ZoneOperationState.Error(e.message ?: "Failed to update safe zone")
            }
        }
    }
    
    // Delete a safe zone
    fun deleteSafeZone(zone: SafeZone) {
        viewModelScope.launch {
            _zoneOperationState.value = ZoneOperationState.InProgress
            try {
                safeZoneRepository.deleteSafeZone(zone)
                _zoneOperationState.value = ZoneOperationState.Success("Safe zone deleted successfully")
                loadSafeZonesForUser(zone.userId)
            } catch (e: Exception) {
                _zoneOperationState.value = ZoneOperationState.Error(e.message ?: "Failed to delete safe zone")
            }
        }
    }
    
    // State definitions
    sealed class SafeZonesState {
        object Loading : SafeZonesState()
        data class Success(val zones: List<SafeZone>) : SafeZonesState()
        data class Error(val message: String) : SafeZonesState()
    }
    
    sealed class NearbyZonesState {
        object Loading : NearbyZonesState()
        object NotFound : NearbyZonesState()
        data class Found(val zones: List<SafeZone>) : NearbyZonesState()
        data class Error(val message: String) : NearbyZonesState()
    }
    
    sealed class ZoneOperationState {
        object Idle : ZoneOperationState()
        object InProgress : ZoneOperationState()
        data class Success(val message: String) : ZoneOperationState()
        data class Error(val message: String) : ZoneOperationState()
    }
} 