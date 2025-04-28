package com.example.navapp

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextOverflow
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.navapp.screens.AboutScreen
import com.example.navapp.screens.LocationScreen
import com.example.navapp.screens.ProfileScreen
import com.example.navapp.screens.RegisterScreen
import com.example.navapp.screens.SettingsScreen
import kotlinx.coroutines.launch
import androidx.compose.ui.unit.dp
import androidx.compose.material3.MaterialTheme

data class NavigationItem(
    val route: String,
    val title: String,
    val icon: ImageVector
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainApp() {
    val navController = rememberNavController()
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    var userRegistered by remember { mutableStateOf(false) }
    var userData by remember { mutableStateOf(UserData()) }

    val navigationItems = listOf(
        NavigationItem("register", "Register", Icons.Filled.Home),
        NavigationItem("profile", "Profile", Icons.Filled.Person),
        NavigationItem("about", "About", Icons.Filled.AccountCircle),
        NavigationItem("settings", "Settings", Icons.Filled.Settings),
        NavigationItem("location", "Location", Icons.Filled.LocationOn)
    )

    val drawerItems = listOf(
        NavigationItem("register", "Register", Icons.Filled.Home),
        NavigationItem("profile", "Profile", Icons.Filled.Person),
        NavigationItem("about", "About", Icons.Filled.AccountCircle),
        NavigationItem("settings", "Settings", Icons.Filled.Settings),
        NavigationItem("location", "Location", Icons.Filled.LocationOn)
    )

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet {
                Text("NavApp", modifier = Modifier.padding(16.dp))

                drawerItems.forEach { item ->
                    NavigationDrawerItem(
                        label = { Text(item.title) },
                        icon = { Icon(item.icon, contentDescription = null) },
                        selected = false,
                        onClick = {
                            navController.navigate(item.route) {
                                popUpTo(navController.graph.findStartDestination().id) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                            scope.launch {
                                drawerState.close()
                            }
                        }
                    )
                }
            }
        }
    ) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = {
                        Text(
                            "NavApp",
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    },
                    navigationIcon = {
                        IconButton(onClick = { scope.launch { drawerState.open() } }) {
                            Icon(
                                imageVector = Icons.Default.Menu,
                                contentDescription = "Menu"
                            )
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer,
                        titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                    )
                )
            },
            bottomBar = {
                NavigationBar {
                    val navBackStackEntry by navController.currentBackStackEntryAsState()
                    val currentDestination = navBackStackEntry?.destination

                    navigationItems.forEach { screen ->
                        NavigationBarItem(
                            icon = { Icon(screen.icon, contentDescription = null) },
                            label = { Text(screen.title) },
                            selected = currentDestination?.hierarchy?.any { it.route == screen.route } == true,
                            onClick = {
                                navController.navigate(screen.route) {
                                    popUpTo(navController.graph.findStartDestination().id) {
                                        saveState = true
                                    }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            }
                        )
                    }
                }
            }
        ) { innerPadding ->
            NavHost(
                navController = navController,
                startDestination = "register",
                modifier = Modifier.padding(innerPadding)
            ) {
                composable("register") {
                    RegisterScreen(
                        onUserRegistered = { user ->
                            userData = user
                            userRegistered = true
                            navController.navigate("profile")
                        }
                    )
                }
                composable("profile") {
                    ProfileScreen(userData = userData, userRegistered = userRegistered)
                }
                composable("about") {
                    AboutScreen(onNavigateToSettings = {
                        navController.navigate("settings")
                    })
                }
                composable("settings") {
                    SettingsScreen(onNavigateToAbout = {
                        navController.navigate("about")
                    })
                }
                composable("location") {
                    LocationScreen()
                }
            }
        }
    }
}