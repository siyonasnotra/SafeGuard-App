package com.example.navapp

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.core.app.ActivityCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.navapp.ui.auth.LoginScreen
import com.example.navapp.ui.auth.RegisterScreen
import com.example.navapp.ui.emergency.ContactsScreen
import com.example.navapp.ui.emergency.SOSScreen
import com.example.navapp.ui.home.HomeScreen
import com.example.navapp.ui.maps.SafeRoutesScreen
import com.example.navapp.ui.maps.SafeZonesScreen
import com.example.navapp.ui.profile.ProfileScreen
import com.example.navapp.ui.theme.NavAppTheme
import com.example.navapp.utils.Permissions
import com.example.navapp.utils.RequestPermissions
import com.example.navapp.viewmodel.AuthViewModel
import androidx.compose.runtime.saveable.rememberSaveable
import com.example.navapp.ui.maps.MapScreen
import com.example.navapp.ui.safety.SafetyTipsScreen
import com.example.navapp.ui.about.AboutScreen
import com.example.navapp.ui.settings.SettingsScreen

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            val authViewModel: AuthViewModel = viewModel()
            val authState by authViewModel.authState.collectAsState()
            val isDarkTheme = isSystemInDarkTheme()
            var darkTheme by rememberSaveable { mutableStateOf(isDarkTheme) }
            var permissionsRequested by remember { mutableStateOf(false) }
            
            // Request critical permissions
            if (!permissionsRequested) {
                RequestPermissions(
                    permissions = Permissions.CRITICAL
                ) { granted ->
                    // Handle permission result
                    permissionsRequested = true
                }
            }
            
            NavAppTheme(darkTheme = darkTheme) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    SafetyNavHost(
                        authViewModel = authViewModel,
                        isDarkTheme = darkTheme,
                        onThemeToggle = { newTheme -> darkTheme = newTheme }
                    )
                }
            }
        }
    }
}

@Composable
fun SafetyNavHost(
    authViewModel: AuthViewModel,
    isDarkTheme: Boolean,
    onThemeToggle: (Boolean) -> Unit
) {
    val navController = rememberNavController()
    val authState by authViewModel.authState.collectAsState()
    
    // Determine start destination based on auth state
    val startDestination = when (authState) {
        is AuthViewModel.AuthState.LoggedIn -> "home"
        else -> "login"
    }
    
    NavHost(
        navController = navController,
        startDestination = startDestination
    ) {
        // Auth routes
        composable("login") {
            LoginScreen(
                onLoginSuccess = { navController.navigate("home") {
                    popUpTo("login") { inclusive = true }
                }},
                onRegisterClick = { navController.navigate("register") },
                viewModel = authViewModel
            )
        }
        
        composable("register") {
            RegisterScreen(
                onRegisterSuccess = { navController.navigate("home") {
                    popUpTo("login") { inclusive = true }
                }},
                onLoginClick = { navController.navigate("login") },
                viewModel = authViewModel
            )
        }
        
        // Main app routes
        composable("home") {
            HomeScreen(
                onSOSClick = { navController.navigate("sos") },
                onContactsClick = { navController.navigate("contacts") },
                onSafeRoutesClick = { navController.navigate("saferoutes") },
                onSafeZonesClick = { navController.navigate("safezones") },
                onProfileClick = { navController.navigate("profile") },
                onSettingsClick = { navController.navigate("settings") },
                onMapClick = { navController.navigate("map") },
                onSafetyTipsClick = { navController.navigate("safetytips") }
            )
        }
        
        composable("sos") {
            SOSScreen(
                onContactsClick = { navController.navigate("contacts") },
                onSafeRoutesClick = { navController.navigate("saferoutes") },
                onSafeZonesClick = { navController.navigate("safezones") },
                viewModel = viewModel()
            )
        }
        
        composable("contacts") {
            ContactsScreen(
                onBackClick = { navController.navigateUp() },
                viewModel = viewModel()
            )
        }
        
        composable("saferoutes") {
            SafeRoutesScreen(
                onBackClick = { navController.navigateUp() },
                viewModel = viewModel()
            )
        }
        
        composable("safezones") {
            SafeZonesScreen(
                onBackClick = { navController.navigateUp() },
                viewModel = viewModel()
            )
        }
        
        composable("profile") {
            ProfileScreen(
                onBackClick = { navController.navigateUp() },
                onLogout = {
                    navController.navigate("login") {
                        popUpTo("home") { inclusive = true }
                    }
                },
                onThemeToggle = onThemeToggle,
                isDarkTheme = isDarkTheme,
                authViewModel = authViewModel
            )
        }
        
        composable("map") {
            MapScreen(
                onBackClick = { navController.navigateUp() },
                isDarkTheme = isDarkTheme
            )
        }
        
        composable("safetytips") {
            SafetyTipsScreen(
                onBackClick = { navController.navigateUp() },
                onThemeToggle = onThemeToggle,
                isDarkTheme = isDarkTheme
            )
        }
        
        composable("about") {
            AboutScreen(
                onBackClick = { navController.navigateUp() }
            )
        }
        
        composable("settings") {
            SettingsScreen(
                onBackClick = { navController.navigateUp() },
                onAboutClick = { navController.navigate("about") },
                onThemeToggle = onThemeToggle,
                isDarkTheme = isDarkTheme
            )
        }
    }
}
