package com.example.navapp.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "users")
data class User(
    @PrimaryKey
    val userId: String,
    val email: String,
    val name: String,
    val phone: String,
    val profilePictureUrl: String? = null,
    val isLoggedIn: Boolean = false,
    val lastLoginTimestamp: Long = 0
) 