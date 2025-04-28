package com.example.navapp.ui.maps

import android.content.Context
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.navapp.data.model.SafeZone
import com.example.navapp.data.model.ZoneType
import com.example.navapp.ui.theme.SafetyTheme
import com.example.navapp.utils.LocationUtils
import com.example.navapp.viewmodel.AuthViewModel
import com.example.navapp.viewmodel.SafeZoneViewModel
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.Circle
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MapProperties
import com.google.maps.android.compose.MapType
import com.google.maps.android.compose.MapUiSettings
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.MarkerState
import com.google.maps.android.compose.rememberCameraPositionState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SafeZonesScreen(
    onBackClick: () -> Unit,
    viewModel: SafeZoneViewModel
) {
    val context = LocalContext.current
    val authViewModel: AuthViewModel = viewModel()
    val currentUser by authViewModel.currentUser.collectAsState()
    val safeZonesState by viewModel.safeZonesState.collectAsState()
    val zoneOperationState by viewModel.zoneOperationState.collectAsState()
    
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    
    var currentLocation by remember { mutableStateOf<LatLng?>(null) }
    var mapReady by remember { mutableStateOf(false) }
    var showAddZoneDialog by remember { mutableStateOf(false) }
    var selectedLocation by remember { mutableStateOf<LatLng?>(null) }
    
    // Load safe zones when screen is displayed
    LaunchedEffect(currentUser) {
        currentUser?.let { user ->
            viewModel.loadSafeZonesForUser(user.userId)
        }
    }
    
    // Get the current location
    LaunchedEffect(Unit) {
        fetchCurrentLocation(context) { lat, lng ->
            currentLocation = LatLng(lat, lng)
        }
    }
    
    // Camera position for the map
    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(
            currentLocation ?: LatLng(0.0, 0.0), 
            14f
        )
    }
    
    // Update camera position when location changes
    LaunchedEffect(currentLocation) {
        currentLocation?.let {
            cameraPositionState.position = CameraPosition.fromLatLngZoom(it, 14f)
        }
    }
    
    // Map properties
    val mapProperties by remember {
        mutableStateOf(
            MapProperties(
                mapType = MapType.NORMAL,
                isMyLocationEnabled = true
            )
        )
    }
    
    // Map UI settings
    val mapUiSettings by remember {
        mutableStateOf(
            MapUiSettings(
                zoomControlsEnabled = true,
                compassEnabled = true,
                mapToolbarEnabled = true
            )
        )
    }
    
    // Show snackbar for operation states
    LaunchedEffect(zoneOperationState) {
        when (zoneOperationState) {
            is SafeZoneViewModel.ZoneOperationState.Success -> {
                scope.launch {
                    snackbarHostState.showSnackbar(
                        (zoneOperationState as SafeZoneViewModel.ZoneOperationState.Success).message
                    )
                }
            }
            is SafeZoneViewModel.ZoneOperationState.Error -> {
                scope.launch {
                    snackbarHostState.showSnackbar(
                        (zoneOperationState as SafeZoneViewModel.ZoneOperationState.Error).message
                    )
                }
            }
            else -> { /* Do nothing */ }
        }
    }
    
    // Add zone dialog
    if (showAddZoneDialog && selectedLocation != null) {
        AddSafeZoneDialog(
            location = selectedLocation!!,
            onDismiss = {
                showAddZoneDialog = false
                selectedLocation = null
            },
            onSave = { name, radius, zoneType ->
                currentUser?.let { user ->
                    viewModel.addSafeZone(
                        userId = user.userId,
                        name = name,
                        latitude = selectedLocation!!.latitude,
                        longitude = selectedLocation!!.longitude,
                        radius = radius,
                        zoneType = zoneType
                    )
                }
                showAddZoneDialog = false
                selectedLocation = null
            }
        )
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Safe Zones") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = {
                    currentLocation?.let {
                        selectedLocation = it
                        showAddZoneDialog = true
                    }
                },
                icon = { Icon(Icons.Default.Add, contentDescription = "Add Zone") },
                text = { Text("Add Safe Zone") },
                expanded = true
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { contentPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(contentPadding)
        ) {
            when (safeZonesState) {
                is SafeZoneViewModel.SafeZonesState.Loading -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                }
                
                is SafeZoneViewModel.SafeZonesState.Success -> {
                    val zones = (safeZonesState as SafeZoneViewModel.SafeZonesState.Success).zones
                    
                    Box(
                        modifier = Modifier.fillMaxSize()
                    ) {
                        // Google Map
                        GoogleMap(
                            modifier = Modifier.fillMaxSize(),
                            cameraPositionState = cameraPositionState,
                            properties = mapProperties,
                            uiSettings = mapUiSettings,
                            onMapLoaded = {
                                mapReady = true
                            },
                            onMapLongClick = { latLng ->
                                selectedLocation = latLng
                                showAddZoneDialog = true
                            }
                        ) {
                            // Show current location marker
                            currentLocation?.let { location ->
                                Marker(
                                    state = MarkerState(position = location),
                                    title = "Your Location"
                                )
                            }
                            
                            // Show all safe zones
                            zones.forEach { zone ->
                                val zonePosition = LatLng(zone.latitude, zone.longitude)
                                val zoneColor = getColorForZoneType(zone.zoneType)
                                
                                // Zone circle
                                Circle(
                                    center = zonePosition,
                                    radius = zone.radius.toDouble(),
                                    fillColor = zoneColor.copy(alpha = 0.2f),
                                    strokeColor = zoneColor.copy(alpha = 0.8f),
                                    strokeWidth = 2f
                                )
                                
                                // Zone marker
                                Marker(
                                    state = MarkerState(position = zonePosition),
                                    title = zone.name,
                                    snippet = "Radius: ${zone.radius}m"
                                )
                            }
                        }
                        
                        // Info card at the bottom if no zones
                        AnimatedVisibility(
                            visible = zones.isEmpty(),
                            enter = fadeIn() + expandVertically(),
                            exit = fadeOut() + shrinkVertically()
                        ) {
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp)
                                    .align(Alignment.BottomCenter),
                                elevation = CardDefaults.cardElevation(
                                    defaultElevation = 6.dp
                                )
                            ) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(16.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Text(
                                        text = "No Safe Zones Added",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold
                                    )
                                    
                                    Text(
                                        text = "Long-press on the map to add a safe zone. These are areas you consider safe and will be used for emergency navigation.",
                                        style = MaterialTheme.typography.bodyMedium,
                                        textAlign = TextAlign.Center,
                                        modifier = Modifier.padding(top = 8.dp)
                                    )
                                }
                            }
                        }
                    }
                }
                
                is SafeZoneViewModel.SafeZonesState.Error -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = (safeZonesState as SafeZoneViewModel.SafeZonesState.Error).message,
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                }
            }
            
            // My Location button
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(16.dp)
            ) {
                FloatingActionButton(
                    onClick = {
                        fetchCurrentLocation(context) { lat, lng ->
                            currentLocation = LatLng(lat, lng)
                        }
                    },
                    modifier = Modifier.size(48.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.MyLocation,
                        contentDescription = "My Location"
                    )
                }
            }
        }
    }
}

@Composable
fun AddSafeZoneDialog(
    location: LatLng,
    onDismiss: () -> Unit,
    onSave: (name: String, radius: Float, zoneType: ZoneType) -> Unit
) {
    var zoneName by remember { mutableStateOf("") }
    var radius by remember { mutableFloatStateOf(100f) }
    var selectedZoneType by remember { mutableStateOf(ZoneType.CUSTOM) }
    
    val scrollState = rememberScrollState()
    
    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = MaterialTheme.colorScheme.surface
        ) {
            Column(
                modifier = Modifier
                    .padding(20.dp)
                    .verticalScroll(scrollState)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Add Safe Zone",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold
                    )
                    
                    IconButton(onClick = onDismiss) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close"
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                Text(
                    text = "Location: ${location.latitude.format(6)}, ${location.longitude.format(6)}",
                    style = MaterialTheme.typography.bodyMedium
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                OutlinedTextField(
                    value = zoneName,
                    onValueChange = { zoneName = it },
                    label = { Text("Zone Name") },
                    modifier = Modifier.fillMaxWidth()
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                Text(
                    text = "Radius: ${radius.toInt()} meters",
                    style = MaterialTheme.typography.bodyMedium
                )
                
                Slider(
                    value = radius,
                    onValueChange = { radius = it },
                    valueRange = 50f..500f,
                    steps = 9,
                    modifier = Modifier.fillMaxWidth()
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                Text(
                    text = "Zone Type:",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                ZoneTypeSelector(
                    selectedType = selectedZoneType,
                    onTypeSelected = { selectedZoneType = it }
                )
                
                Spacer(modifier = Modifier.height(24.dp))
                
                Button(
                    onClick = {
                        onSave(zoneName, radius, selectedZoneType)
                    },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = zoneName.isNotBlank()
                ) {
                    Text("Save Zone")
                }
            }
        }
    }
}

@Composable
fun ZoneTypeSelector(
    selectedType: ZoneType,
    onTypeSelected: (ZoneType) -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth()
    ) {
        ZoneType.values().forEach { zoneType ->
            val isSelected = selectedType == zoneType
            val color = getColorForZoneType(zoneType)
            
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(
                        if (isSelected) color.copy(alpha = 0.2f)
                        else MaterialTheme.colorScheme.surface
                    )
                    .padding(8.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(
                        if (isSelected) color.copy(alpha = 0.1f)
                        else MaterialTheme.colorScheme.surface
                    )
                    .padding(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(24.dp)
                        .clip(CircleShape)
                        .background(color.copy(alpha = 0.8f))
                        .padding(4.dp),
                    contentAlignment = Alignment.Center
                ) {
                    if (isSelected) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
                
                Spacer(modifier = Modifier.width(12.dp))
                
                Text(
                    text = zoneType.name.replace("_", " ").lowercase().capitalize(),
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                )
                
                Spacer(modifier = Modifier.weight(1f))
                
                IconButton(
                    onClick = { onTypeSelected(zoneType) },
                    modifier = Modifier.size(40.dp)
                ) {
                    Icon(
                        imageVector = if (isSelected) Icons.Default.Check else Icons.Default.Check,
                        contentDescription = null,
                        tint = if (isSelected) color else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                    )
                }
            }
        }
    }
}

// Helper function to fetch the current location
private fun fetchCurrentLocation(
    context: Context,
    onLocationFetched: (lat: Double, lng: Double) -> Unit
) {
    CoroutineScope(Dispatchers.Main).launch {
        LocationUtils.getCurrentLocation(context)?.let { location ->
            onLocationFetched(location.latitude, location.longitude)
        }
    }
}

// Helper function to format doubles
private fun Double.format(digits: Int) = "%.${digits}f".format(this)

// Get color for zone type
private fun getColorForZoneType(zoneType: ZoneType): Color {
    return when (zoneType) {
        ZoneType.HOME -> Color(0xFF4285F4)
        ZoneType.WORK -> Color(0xFF34A853)
        ZoneType.SCHOOL -> Color(0xFFFBBC05)
        ZoneType.HOSPITAL -> Color(0xFFEA4335)
        ZoneType.POLICE_STATION -> Color(0xFF673AB7)
        ZoneType.CUSTOM -> Color(0xFF9E9E9E)
    }
}

// Extension function to capitalize first letter
private fun String.capitalize(): String {
    return this.replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() }
}