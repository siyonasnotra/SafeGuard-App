package com.example.navapp.data.repository

import com.example.navapp.SafetyApplication
import com.example.navapp.data.model.EmergencyContact
import kotlinx.coroutines.flow.Flow

class EmergencyContactRepository {
    private val contactDao = SafetyApplication.database.emergencyContactDao()
    
    suspend fun addContact(
        userId: String,
        name: String,
        phone: String,
        relationship: String,
        canReceiveLocation: Boolean = true,
        canReceiveSos: Boolean = true,
        priority: Int = 1
    ): Long {
        val contact = EmergencyContact(
            userId = userId,
            name = name,
            phone = phone,
            relationship = relationship,
            canReceiveLocation = canReceiveLocation,
            canReceiveSos = canReceiveSos,
            priority = priority
        )
        return contactDao.insertContact(contact)
    }
    
    suspend fun updateContact(contact: EmergencyContact) {
        contactDao.updateContact(contact)
    }
    
    suspend fun deleteContact(contact: EmergencyContact) {
        contactDao.deleteContact(contact)
    }
    
    fun getContactsForUser(userId: String): Flow<List<EmergencyContact>> {
        return contactDao.getContactsForUser(userId)
    }
    
    fun getSosContactsForUser(userId: String): Flow<List<EmergencyContact>> {
        return contactDao.getSosContactsForUser(userId)
    }
    
    fun getLocationSharingContactsForUser(userId: String): Flow<List<EmergencyContact>> {
        return contactDao.getLocationSharingContactsForUser(userId)
    }
    
    fun getContactById(contactId: Long): Flow<EmergencyContact?> {
        return contactDao.getContactById(contactId)
    }
    
    suspend fun deleteAllContactsForUser(userId: String) {
        contactDao.deleteAllContactsForUser(userId)
    }
} 