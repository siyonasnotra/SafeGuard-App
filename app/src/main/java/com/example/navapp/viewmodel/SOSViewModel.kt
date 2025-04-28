package com.example.navapp.viewmodel

import android.content.Context
import android.location.Location
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.navapp.data.model.EmergencyContact
import com.example.navapp.data.model.SOSEvent
import com.example.navapp.data.model.SOSStatus
import com.example.navapp.data.model.User
import com.example.navapp.data.repository.EmergencyContactRepository
import com.example.navapp.data.repository.SOSRepository
import com.example.navapp.service.LocationTrackingService
import com.example.navapp.utils.LocationUtils
import com.example.navapp.utils.SOSUtils
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch

class SOSViewModel : ViewModel() {
    private val sosRepository = SOSRepository()
    private val contactRepository = EmergencyContactRepository()
    
    private val _sosState = MutableStateFlow<SOSState>(SOSState.Inactive)
    val sosState: StateFlow<SOSState> = _sosState
    
    private val _sosEvents = MutableStateFlow<List<SOSEvent>>(emptyList())
    val sosEvents: StateFlow<List<SOSEvent>> = _sosEvents
    
    private val _sosContacts = MutableStateFlow<List<EmergencyContact>>(emptyList())
    val sosContacts: StateFlow<List<EmergencyContact>> = _sosContacts
    
    // Check if there's an active SOS event
    fun checkActiveSOSEvents(userId: String) {
        viewModelScope.launch {
            sosRepository.getLatestActiveSOSEvent(userId)
                .catch { e ->
                    _sosState.value = SOSState.Error(e.message ?: "Failed to check active SOS events")
                }
                .collect { event ->
                    _sosState.value = if (event != null) {
                        SOSState.Active(event)
                    } else {
                        SOSState.Inactive
                    }
                }
        }
    }
    
    // Get SOS contacts for a user
    fun loadSOSContacts(userId: String) {
        viewModelScope.launch {
            contactRepository.getSosContactsForUser(userId)
                .catch { e ->
                    _sosContacts.value = emptyList()
                }
                .collect { contacts ->
                    _sosContacts.value = contacts
                }
        }
    }
    
    // Trigger SOS alert
    fun triggerSOS(context: Context, user: User, customMessage: String? = null) {
        viewModelScope.launch {
            _sosState.value = SOSState.Triggering
            
            try {
                // Get current location
                val location = LocationUtils.getCurrentLocation(context)
                
                if (location != null) {
                    // Create SOS event
                    val eventId = sosRepository.triggerSOS(
                        userId = user.userId,
                        latitude = location.latitude,
                        longitude = location.longitude
                    )
                    
                    // Get emergency contacts
                    val contacts = contactRepository.getSosContactsForUser(user.userId).firstOrNull() ?: emptyList()
                    
                    // Send SOS messages
                    if (contacts.isNotEmpty()) {
                        val message = SOSUtils.generateSOSMessage(user.name, customMessage)
                        val locationUrl = LocationUtils.formatLocationForSharing(location.latitude, location.longitude)
                        
                        SOSUtils.sendSOSMessage(context, contacts, message, locationUrl)
                    }
                    
                    // Start location tracking service
                    LocationTrackingService.startService(context, user.userId)
                    
                    // Start alarm
                    SOSUtils.startAlarm(context)
                    
                    // Update state
                    checkActiveSOSEvents(user.userId)
                } else {
                    _sosState.value = SOSState.Error("Unable to get current location")
                }
            } catch (e: Exception) {
                _sosState.value = SOSState.Error(e.message ?: "Failed to trigger SOS alert")
            }
        }
    }
    
    // Cancel active SOS
    fun cancelSOS(context: Context, userId: String, eventId: Long, notes: String? = null) {
        viewModelScope.launch {
            _sosState.value = SOSState.Updating
            
            try {
                // Cancel SOS event
                sosRepository.cancelSOS(eventId, notes)
                
                // Stop alarm
                SOSUtils.stopAlarm()
                SOSUtils.stopVibration(context)
                
                // Stop location tracking
                LocationTrackingService.stopService(context)
                
                // Update state
                _sosState.value = SOSState.Inactive
                
                // Reload events
                loadSOSEvents(userId)
            } catch (e: Exception) {
                _sosState.value = SOSState.Error(e.message ?: "Failed to cancel SOS alert")
            }
        }
    }
    
    // Resolve active SOS
    fun resolveSOS(context: Context, userId: String, eventId: Long, notes: String? = null) {
        viewModelScope.launch {
            _sosState.value = SOSState.Updating
            
            try {
                // Resolve SOS event
                sosRepository.resolveSOS(eventId, notes)
                
                // Stop alarm
                SOSUtils.stopAlarm()
                SOSUtils.stopVibration(context)
                
                // Stop location tracking
                LocationTrackingService.stopService(context)
                
                // Update state
                _sosState.value = SOSState.Inactive
                
                // Reload events
                loadSOSEvents(userId)
            } catch (e: Exception) {
                _sosState.value = SOSState.Error(e.message ?: "Failed to resolve SOS alert")
            }
        }
    }
    
    // Load SOS event history for a user
    fun loadSOSEvents(userId: String) {
        viewModelScope.launch {
            sosRepository.getSOSEventsForUser(userId)
                .catch { e ->
                    _sosEvents.value = emptyList()
                }
                .collect { events ->
                    _sosEvents.value = events
                }
        }
    }
    
    // State definitions
    sealed class SOSState {
        object Inactive : SOSState()
        object Triggering : SOSState()
        object Updating : SOSState()
        data class Active(val sosEvent: SOSEvent) : SOSState()
        data class Error(val message: String) : SOSState()
    }
} 