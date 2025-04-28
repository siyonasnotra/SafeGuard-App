package com.example.navapp.ui.emergency

import android.content.Context
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FastOutLinearInEasing
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.filled.ContactPhone
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material.icons.rounded.Notifications
import androidx.compose.material.icons.rounded.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.navapp.ui.theme.SafetyTheme
import com.example.navapp.viewmodel.AuthViewModel
import com.example.navapp.viewmodel.SOSViewModel
import androidx.lifecycle.viewmodel.compose.viewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SOSScreen(
    onContactsClick: () -> Unit,
    onSafeRoutesClick: () -> Unit,
    onSafeZonesClick: () -> Unit,
    viewModel: SOSViewModel
) {
    val context = LocalContext.current
    val authViewModel: AuthViewModel = viewModel()
    val currentUser by authViewModel.currentUser.collectAsState()
    val sosState by viewModel.sosState.collectAsState()
    val sosContacts by viewModel.sosContacts.collectAsState()
    
    var showCustomMessageDialog by remember { mutableStateOf(false) }
    var customMessage by remember { mutableStateOf("") }
    
    val isActive = sosState is SOSViewModel.SOSState.Active || sosState is SOSViewModel.SOSState.Triggering
    
    // Colors for the SOS button
    val buttonColor by animateColorAsState(
        targetValue = if (isActive) SafetyTheme.colors.sosColor.copy(alpha = 0.8f) else SafetyTheme.colors.sosColor,
        animationSpec = tween(durationMillis = 500), label = "Button Color"
    )
    
    // Pulsating animation for the SOS button when active
    val infiniteTransition = rememberInfiniteTransition(label = "Infinite Transition")
    val scale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = if (isActive) 1.2f else 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = LinearOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ), label = "Scale Animation"
    )
    
    LaunchedEffect(Unit) {
        // Load user's emergency contacts
        currentUser?.let { user ->
            viewModel.loadSOSContacts(user.userId)
            viewModel.checkActiveSOSEvents(user.userId)
        }
    }
    
    if (showCustomMessageDialog) {
        AlertDialog(
            onDismissRequest = { showCustomMessageDialog = false },
            title = { Text("Add Custom Message") },
            text = {
                Column {
                    Text("Add a custom message to send with your SOS alert:")
                    Spacer(modifier = Modifier.height(16.dp))
                    TextField(
                        value = customMessage,
                        onValueChange = { customMessage = it },
                        modifier = Modifier.fillMaxWidth(),
                        placeholder = { Text("E.g. I need help at...") }
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        showCustomMessageDialog = false
                        currentUser?.let { user ->
                            viewModel.triggerSOS(context, user, customMessage)
                        }
                    }
                ) {
                    Text("Send SOS Alert")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        showCustomMessageDialog = false
                        currentUser?.let { user ->
                            viewModel.triggerSOS(context, user)
                        }
                    }
                ) {
                    Text("Skip Message")
                }
            }
        )
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("SafeGuard - Emergency") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        }
    ) { contentPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(contentPadding),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Status card
            StatusCard(sosState, sosContacts.size)
            
            // Main content
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                // SOS Button
                Box(
                    modifier = Modifier.padding(vertical = 24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    // SOS Button with animation
                    Button(
                        onClick = {
                            if (isActive) {
                                // If SOS is active, cancel it
                                val activeEvent = (sosState as? SOSViewModel.SOSState.Active)?.sosEvent
                                activeEvent?.let { event ->
                                    viewModel.cancelSOS(context, event.userId, event.eventId)
                                }
                            } else {
                                // Otherwise show message dialog to trigger SOS
                                showCustomMessageDialog = true
                            }
                        },
                        modifier = Modifier
                            .size(180.dp)
                            .scale(scale),
                        shape = CircleShape,
                        contentPadding = PaddingValues(16.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = buttonColor,
                            contentColor = Color.White
                        )
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            if (isActive) {
                                Icon(
                                    imageVector = Icons.Default.Cancel,
                                    contentDescription = "Cancel SOS",
                                    modifier = Modifier.size(48.dp)
                                )
                                Text(
                                    text = "CANCEL",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 22.sp
                                )
                            } else {
                                Icon(
                                    imageVector = Icons.Rounded.Warning,
                                    contentDescription = "SOS Alert",
                                    modifier = Modifier.size(48.dp)
                                )
                                Text(
                                    text = "SOS",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 26.sp
                                )
                            }
                        }
                    }
                }
                
                Text(
                    text = if (isActive) 
                        "Emergency alert is active\nTap to cancel" 
                    else 
                        "Press the SOS button in case of emergency",
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(vertical = 16.dp),
                    style = MaterialTheme.typography.bodyLarge,
                    color = if (isActive) SafetyTheme.colors.sosColor else MaterialTheme.colorScheme.onSurface
                )
                
                if (sosContacts.isEmpty() && !isActive) {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.7f)
                        )
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.PersonAdd,
                                contentDescription = "Add Contacts",
                                tint = MaterialTheme.colorScheme.error
                            )
                            Spacer(modifier = Modifier.width(16.dp))
                            Column {
                                Text(
                                    "No emergency contacts set up",
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.error
                                )
                                Text(
                                    "Add contacts to notify in case of emergency",
                                    style = MaterialTheme.typography.bodyMedium
                                )
                            }
                        }
                    }
                }
            }
            
            // Bottom navigation buttons
            QuickActionButtons(
                onContactsClick = onContactsClick,
                onSafeRoutesClick = onSafeRoutesClick,
                onSafeZonesClick = onSafeZonesClick
            )
        }
    }
}

@Composable
fun StatusCard(sosState: SOSViewModel.SOSState, contactsCount: Int) {
    val backgroundColor = when(sosState) {
        is SOSViewModel.SOSState.Active -> SafetyTheme.colors.sosColor.copy(alpha = 0.1f)
        is SOSViewModel.SOSState.Error -> MaterialTheme.colorScheme.errorContainer
        else -> MaterialTheme.colorScheme.surfaceVariant
    }
    
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = backgroundColor
        ),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            when(sosState) {
                is SOSViewModel.SOSState.Active -> {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Notifications,
                            contentDescription = "Active Alert",
                            tint = SafetyTheme.colors.sosColor,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Emergency Alert Active",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = SafetyTheme.colors.sosColor
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Your location is being shared with your emergency contacts.",
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
                is SOSViewModel.SOSState.Error -> {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "Error",
                            tint = MaterialTheme.colorScheme.error,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Error: ${sosState.message}",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                }
                else -> {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Verified,
                            contentDescription = "Status OK",
                            tint = SafetyTheme.colors.successColor,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Status: Ready",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = if (contactsCount > 0)
                            "You have $contactsCount emergency contact${if (contactsCount > 1) "s" else ""} set up."
                        else
                            "Add emergency contacts for quick access in case of emergency.",
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        }
    }
}

@Composable
fun QuickActionButtons(
    onContactsClick: () -> Unit,
    onSafeRoutesClick: () -> Unit,
    onSafeZonesClick: () -> Unit
) {
    Surface(
        color = MaterialTheme.colorScheme.surfaceVariant,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            ActionButton(
                icon = Icons.Default.ContactPhone,
                label = "Contacts",
                onClick = onContactsClick
            )
            
            ActionButton(
                icon = Icons.Default.Map,
                label = "Safe Routes",
                onClick = onSafeRoutesClick
            )
            
            ActionButton(
                icon = Icons.Default.LocationOn,
                label = "Safe Zones",
                onClick = onSafeZonesClick
            )
        }
    }
}

@Composable
fun ActionButton(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.clip(RoundedCornerShape(12.dp))
            .padding(8.dp)
    ) {
        Button(
            onClick = onClick,
            shape = CircleShape,
            modifier = Modifier.size(56.dp),
            contentPadding = PaddingValues(0.dp)
        ) {
            Icon(imageVector = icon, contentDescription = label)
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            fontWeight = FontWeight.Medium
        )
    }
} 