package com.example.navapp.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.navapp.data.model.EmergencyContact
import com.example.navapp.data.repository.EmergencyContactRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch

class EmergencyContactViewModel : ViewModel() {
    private val contactRepository = EmergencyContactRepository()
    
    private val _contactsState = MutableStateFlow<ContactsState>(ContactsState.Loading)
    val contactsState: StateFlow<ContactsState> = _contactsState
    
    private val _contactOperationState = MutableStateFlow<ContactOperationState>(ContactOperationState.Idle)
    val contactOperationState: StateFlow<ContactOperationState> = _contactOperationState
    
    fun loadContactsForUser(userId: String) {
        viewModelScope.launch {
            _contactsState.value = ContactsState.Loading
            contactRepository.getContactsForUser(userId)
                .catch { e ->
                    _contactsState.value = ContactsState.Error(e.message ?: "Failed to load contacts")
                }
                .collect { contacts ->
                    _contactsState.value = ContactsState.Success(contacts)
                }
        }
    }
    
    fun loadSosContactsForUser(userId: String) {
        viewModelScope.launch {
            _contactsState.value = ContactsState.Loading
            contactRepository.getSosContactsForUser(userId)
                .catch { e ->
                    _contactsState.value = ContactsState.Error(e.message ?: "Failed to load SOS contacts")
                }
                .collect { contacts ->
                    _contactsState.value = ContactsState.Success(contacts)
                }
        }
    }
    
    fun loadLocationSharingContactsForUser(userId: String) {
        viewModelScope.launch {
            _contactsState.value = ContactsState.Loading
            contactRepository.getLocationSharingContactsForUser(userId)
                .catch { e ->
                    _contactsState.value = ContactsState.Error(e.message ?: "Failed to load location sharing contacts")
                }
                .collect { contacts ->
                    _contactsState.value = ContactsState.Success(contacts)
                }
        }
    }
    
    fun addContact(
        userId: String,
        name: String,
        phone: String,
        relationship: String,
        canReceiveLocation: Boolean = true,
        canReceiveSos: Boolean = true,
        priority: Int = 1
    ) {
        if (name.isBlank() || phone.isBlank()) {
            _contactOperationState.value = ContactOperationState.Error("Name and phone number are required")
            return
        }
        
        viewModelScope.launch {
            _contactOperationState.value = ContactOperationState.InProgress
            try {
                val contactId = contactRepository.addContact(
                    userId, name, phone, relationship, 
                    canReceiveLocation, canReceiveSos, priority
                )
                _contactOperationState.value = ContactOperationState.Success("Contact added successfully")
                loadContactsForUser(userId)
            } catch (e: Exception) {
                _contactOperationState.value = ContactOperationState.Error(e.message ?: "Failed to add contact")
            }
        }
    }
    
    fun updateContact(contact: EmergencyContact) {
        viewModelScope.launch {
            _contactOperationState.value = ContactOperationState.InProgress
            try {
                contactRepository.updateContact(contact)
                _contactOperationState.value = ContactOperationState.Success("Contact updated successfully")
                loadContactsForUser(contact.userId)
            } catch (e: Exception) {
                _contactOperationState.value = ContactOperationState.Error(e.message ?: "Failed to update contact")
            }
        }
    }
    
    fun deleteContact(contact: EmergencyContact) {
        viewModelScope.launch {
            _contactOperationState.value = ContactOperationState.InProgress
            try {
                contactRepository.deleteContact(contact)
                _contactOperationState.value = ContactOperationState.Success("Contact deleted successfully")
                loadContactsForUser(contact.userId)
            } catch (e: Exception) {
                _contactOperationState.value = ContactOperationState.Error(e.message ?: "Failed to delete contact")
            }
        }
    }
    
    // State definitions
    sealed class ContactsState {
        object Loading : ContactsState()
        data class Success(val contacts: List<EmergencyContact>) : ContactsState()
        data class Error(val message: String) : ContactsState()
    }
    
    sealed class ContactOperationState {
        object Idle : ContactOperationState()
        object InProgress : ContactOperationState()
        data class Success(val message: String) : ContactOperationState()
        data class Error(val message: String) : ContactOperationState()
    }
} 