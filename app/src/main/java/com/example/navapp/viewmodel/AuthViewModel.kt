package com.example.navapp.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.navapp.data.model.User
import com.example.navapp.data.repository.UserRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch

class AuthViewModel : ViewModel() {
    private val userRepository = UserRepository()
    
    private val _authState = MutableStateFlow<AuthState>(AuthState.Idle)
    val authState: StateFlow<AuthState> = _authState
    
    private val _currentUser = MutableStateFlow<User?>(null)
    val currentUser: StateFlow<User?> = _currentUser
    
    init {
        checkLoggedInUser()
    }
    
    private fun checkLoggedInUser() {
        viewModelScope.launch {
            _authState.value = AuthState.Loading
            userRepository.getLoggedInUser().collect { user ->
                _currentUser.value = user
                _authState.value = if (user != null) {
                    AuthState.LoggedIn(user)
                } else {
                    AuthState.LoggedOut
                }
            }
        }
    }
    
    fun login(email: String) {
        if (email.isBlank()) {
            _authState.value = AuthState.Error("Email cannot be empty")
            return
        }
        
        _authState.value = AuthState.Loading
        viewModelScope.launch {
            val success = userRepository.loginUser(email)
            if (success) {
                val user = userRepository.getUserByEmail(email).firstOrNull()
                _currentUser.value = user
                _authState.value = if (user != null) {
                    AuthState.LoggedIn(user)
                } else {
                    AuthState.Error("User not found")
                }
            } else {
                _authState.value = AuthState.Error("Login failed")
            }
        }
    }
    
    fun register(email: String, name: String, phone: String) {
        if (email.isBlank() || name.isBlank() || phone.isBlank()) {
            _authState.value = AuthState.Error("All fields are required")
            return
        }
        
        _authState.value = AuthState.Loading
        viewModelScope.launch {
            try {
                val userId = userRepository.registerUser(email, name, phone)
                val user = userRepository.getUserById(userId).firstOrNull()
                _currentUser.value = user
                _authState.value = if (user != null) {
                    AuthState.LoggedIn(user)
                } else {
                    AuthState.Error("Registration failed")
                }
            } catch (e: Exception) {
                _authState.value = AuthState.Error(e.message ?: "Registration failed")
            }
        }
    }
    
    fun logout() {
        _authState.value = AuthState.Loading
        viewModelScope.launch {
            userRepository.logoutCurrentUser()
            _currentUser.value = null
            _authState.value = AuthState.LoggedOut
        }
    }
    
    fun updateUserProfile(user: User) {
        viewModelScope.launch {
            userRepository.updateUserProfile(user)
            _currentUser.value = user
        }
    }
    
    // Auth state definitions
    sealed class AuthState {
        object Idle : AuthState()
        object Loading : AuthState()
        data class LoggedIn(val user: User) : AuthState()
        object LoggedOut : AuthState()
        data class Error(val message: String) : AuthState()
    }
} 