package com.example.navapp.utils

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.core.content.ContextCompat

// All required permissions for the app
object Permissions {
    val LOCATION = arrayOf(
        Manifest.permission.ACCESS_FINE_LOCATION,
        Manifest.permission.ACCESS_COARSE_LOCATION
    )
    
    val BACKGROUND_LOCATION = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
        arrayOf(Manifest.permission.ACCESS_BACKGROUND_LOCATION)
    } else {
        emptyArray()
    }
    
    val SMS = arrayOf(Manifest.permission.SEND_SMS)
    
    val CALL = arrayOf(Manifest.permission.CALL_PHONE)
    
    val NOTIFICATIONS = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        arrayOf(Manifest.permission.POST_NOTIFICATIONS)
    } else {
        emptyArray()
    }
    
    // All critical permissions needed for the app to function
    val CRITICAL = LOCATION + SMS
}

// Check if a permission is granted
fun Context.isPermissionGranted(permission: String): Boolean {
    return ContextCompat.checkSelfPermission(this, permission) == PackageManager.PERMISSION_GRANTED
}

// Check if all permissions in an array are granted
fun Context.arePermissionsGranted(permissions: Array<String>): Boolean {
    return permissions.all { isPermissionGranted(it) }
}

// Open the app settings page
fun Context.openAppSettings() {
    Intent(
        Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
        Uri.fromParts("package", packageName, null)
    ).also { intent ->
        startActivity(intent)
    }
}

// Composable to request permissions
@Composable
fun RequestPermissions(
    permissions: Array<String>,
    onPermissionsResult: (Boolean) -> Unit
) {
    var permissionsRequested by remember { mutableStateOf(false) }
    
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissionsMap ->
        val allGranted = permissionsMap.values.all { it }
        onPermissionsResult(allGranted)
    }
    
    LaunchedEffect(permissions) {
        if (!permissionsRequested) {
            permissionLauncher.launch(permissions)
            permissionsRequested = true
        }
    }
} 