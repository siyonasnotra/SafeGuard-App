package com.example.navapp.data.repository

import com.example.navapp.SafetyApplication
import com.example.navapp.data.model.User
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import java.util.UUID

class UserRepository {
    private val userDao = SafetyApplication.database.userDao()
    
    // Local user operations
    suspend fun registerUser(email: String, name: String, phone: String): String {
        val userId = UUID.randomUUID().toString()
        val newUser = User(
            userId = userId,
            email = email,
            name = name,
            phone = phone,
            isLoggedIn = true,
            lastLoginTimestamp = System.currentTimeMillis()
        )
        userDao.insertUser(newUser)
        return userId
    }
    
    suspend fun loginUser(email: String): Boolean {
        val userFlow = userDao.getUserByEmail(email)
        val user = userFlow.firstOrNull()
        
        return if (user != null) {
            userDao.logoutAllUsers() // Ensure only one user is logged in
            userDao.loginUser(user.userId, System.currentTimeMillis())
            true
        } else {
            false
        }
    }
    
    suspend fun logoutCurrentUser() {
        userDao.logoutAllUsers()
    }
    
    suspend fun updateUserProfile(user: User) {
        userDao.updateUser(user)
    }
    
    fun getLoggedInUser(): Flow<User?> {
        return userDao.getLoggedInUser()
    }
    
    fun getUserById(userId: String): Flow<User?> {
        return userDao.getUserById(userId)
    }

    fun getUserByEmail(email: String): Flow<User?> {
        return userDao.getUserByEmail(email)
    }
} 