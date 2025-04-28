package com.example.navapp.ui.emergency

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Snackbar
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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.navapp.data.model.EmergencyContact
import com.example.navapp.ui.theme.SafetyTheme
import com.example.navapp.viewmodel.AuthViewModel
import com.example.navapp.viewmodel.EmergencyContactViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ContactsScreen(
    onBackClick: () -> Unit,
    viewModel: EmergencyContactViewModel
) {
    val authViewModel: AuthViewModel = viewModel()
    val currentUser by authViewModel.currentUser.collectAsState()
    val contactsState by viewModel.contactsState.collectAsState()
    val contactOperationState by viewModel.contactOperationState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    
    var showAddContactDialog by remember { mutableStateOf(false) }
    var selectedContact by remember { mutableStateOf<EmergencyContact?>(null) }
    var showDeleteConfirmDialog by remember { mutableStateOf(false) }
    
    // Load contacts when screen is displayed
    LaunchedEffect(currentUser) {
        currentUser?.let { user ->
            viewModel.loadContactsForUser(user.userId)
        }
    }
    
    // Show snackbar for operation states
    LaunchedEffect(contactOperationState) {
        when (contactOperationState) {
            is EmergencyContactViewModel.ContactOperationState.Success -> {
                scope.launch {
                    snackbarHostState.showSnackbar(
                        (contactOperationState as EmergencyContactViewModel.ContactOperationState.Success).message
                    )
                }
            }
            is EmergencyContactViewModel.ContactOperationState.Error -> {
                scope.launch {
                    snackbarHostState.showSnackbar(
                        (contactOperationState as EmergencyContactViewModel.ContactOperationState.Error).message
                    )
                }
            }
            else -> { /* Do nothing */ }
        }
    }
    
    if (showAddContactDialog) {
        AddEditContactDialog(
            contact = selectedContact,
            onDismiss = {
                showAddContactDialog = false
                selectedContact = null
            },
            onSave = { name, phone, relationship, canSendSOS, canShareLocation, priority ->
                currentUser?.let { user ->
                    if (selectedContact != null) {
                        // Update existing contact
                        val updatedContact = selectedContact!!.copy(
                            name = name,
                            phone = phone,
                            relationship = relationship,
                            canReceiveSos = canSendSOS,
                            canReceiveLocation = canShareLocation,
                            priority = priority
                        )
                        viewModel.updateContact(updatedContact)
                    } else {
                        // Add new contact
                        viewModel.addContact(
                            userId = user.userId,
                            name = name,
                            phone = phone,
                            relationship = relationship,
                            canReceiveLocation = canShareLocation,
                            canReceiveSos = canSendSOS,
                            priority = priority
                        )
                    }
                }
                showAddContactDialog = false
                selectedContact = null
            }
        )
    }
    
    if (showDeleteConfirmDialog && selectedContact != null) {
        AlertDialog(
            onDismissRequest = {
                showDeleteConfirmDialog = false
                selectedContact = null
            },
            title = { Text("Delete Contact") },
            text = { Text("Are you sure you want to delete ${selectedContact?.name} from your emergency contacts?") },
            confirmButton = {
                Button(
                    onClick = {
                        selectedContact?.let { contact ->
                            viewModel.deleteContact(contact)
                        }
                        showDeleteConfirmDialog = false
                        selectedContact = null
                    }
                ) {
                    Text("Delete")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        showDeleteConfirmDialog = false
                        selectedContact = null
                    }
                ) {
                    Text("Cancel")
                }
            }
        )
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Emergency Contacts") },
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
                    selectedContact = null
                    showAddContactDialog = true
                },
                icon = { Icon(Icons.Default.Add, contentDescription = "Add Contact") },
                text = { Text("Add Contact") },
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
            when (contactsState) {
                is EmergencyContactViewModel.ContactsState.Loading -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                }
                
                is EmergencyContactViewModel.ContactsState.Success -> {
                    val contacts = (contactsState as EmergencyContactViewModel.ContactsState.Success).contacts
                    
                    if (contacts.isEmpty()) {
                        EmptyContactsView()
                    } else {
                        ContactsList(
                            contacts = contacts,
                            onEditClick = { contact ->
                                selectedContact = contact
                                showAddContactDialog = true
                            },
                            onDeleteClick = { contact ->
                                selectedContact = contact
                                showDeleteConfirmDialog = true
                            }
                        )
                    }
                }
                
                is EmergencyContactViewModel.ContactsState.Error -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = (contactsState as EmergencyContactViewModel.ContactsState.Error).message,
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun EmptyContactsView() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Default.Person,
            contentDescription = null,
            modifier = Modifier.size(80.dp),
            tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.6f)
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Text(
            text = "No Emergency Contacts",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        Text(
            text = "Add emergency contacts who will be notified in case of emergency",
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
fun ContactsList(
    contacts: List<EmergencyContact>,
    onEditClick: (EmergencyContact) -> Unit,
    onDeleteClick: (EmergencyContact) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(8.dp),
        contentPadding = androidx.compose.foundation.layout.PaddingValues(16.dp)
    ) {
        item {
            Text(
                text = "Your emergency contacts will be notified during an SOS alert",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(bottom = 16.dp)
            )
        }
        
        items(contacts) { contact ->
            ContactCard(
                contact = contact,
                onEditClick = { onEditClick(contact) },
                onDeleteClick = { onDeleteClick(contact) }
            )
        }
        
        item {
            Spacer(modifier = Modifier.height(80.dp))
        }
    }
}

@Composable
fun ContactCard(
    contact: EmergencyContact,
    onEditClick: () -> Unit,
    onDeleteClick: () -> Unit
) {
    val context = LocalContext.current
    
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 2.dp
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Contact avatar
                Box(
                    modifier = Modifier
                        .size(50.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primaryContainer),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = contact.name.first().toString(),
                        style = MaterialTheme.typography.headlineMedium,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
                
                Spacer(modifier = Modifier.width(16.dp))
                
                // Contact details
                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = contact.name,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    
                    Text(
                        text = contact.relationship,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    
                    Text(
                        text = contact.phone,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
                
                // Actions
                IconButton(onClick = onEditClick) {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = "Edit",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
                
                IconButton(onClick = onDeleteClick) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Delete",
                        tint = MaterialTheme.colorScheme.error
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            Divider()
            Spacer(modifier = Modifier.height(8.dp))
            
            // Features
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                FeatureChip(
                    label = "SOS Alerts",
                    enabled = contact.canReceiveSos,
                    icon = Icons.Default.Call
                )
                
                FeatureChip(
                    label = "Location Sharing",
                    enabled = contact.canReceiveLocation,
                    icon = Icons.Default.LocationOn
                )
                
                Text(
                    text = "Priority: ${contact.priority}",
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier
                        .clip(RoundedCornerShape(16.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                )
            }
        }
    }
}

@Composable
fun FeatureChip(
    label: String,
    enabled: Boolean,
    icon: ImageVector
) {
    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(16.dp))
            .background(
                if (enabled) MaterialTheme.colorScheme.primaryContainer
                else MaterialTheme.colorScheme.surfaceVariant
            )
            .padding(horizontal = 8.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = if (enabled) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(16.dp)
        )
        
        Spacer(modifier = Modifier.width(4.dp))
        
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = if (enabled) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
fun AddEditContactDialog(
    contact: EmergencyContact?,
    onDismiss: () -> Unit,
    onSave: (name: String, phone: String, relationship: String, canSendSOS: Boolean, canShareLocation: Boolean, priority: Int) -> Unit
) {
    val isEditing = contact != null
    
    var name by remember { mutableStateOf(contact?.name ?: "") }
    var phone by remember { mutableStateOf(contact?.phone ?: "") }
    var relationship by remember { mutableStateOf(contact?.relationship ?: "") }
    var canSendSOS by remember { mutableStateOf(contact?.canReceiveSos ?: true) }
    var canShareLocation by remember { mutableStateOf(contact?.canReceiveLocation ?: true) }
    var priority by remember { mutableStateOf(contact?.priority ?: 1) }
    
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
                        text = if (isEditing) "Edit Contact" else "Add New Contact",
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
                
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Full Name") },
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(
                        imeAction = ImeAction.Next
                    )
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                OutlinedTextField(
                    value = phone,
                    onValueChange = { phone = it },
                    label = { Text("Phone Number") },
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Phone,
                        imeAction = ImeAction.Next
                    )
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                OutlinedTextField(
                    value = relationship,
                    onValueChange = { relationship = it },
                    label = { Text("Relationship (e.g., Family, Friend)") },
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(
                        imeAction = ImeAction.Done
                    )
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                Text(
                    text = "Features:",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Checkbox(
                        checked = canSendSOS,
                        onCheckedChange = { canSendSOS = it }
                    )
                    Text("Send SOS Alerts")
                }
                
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Checkbox(
                        checked = canShareLocation,
                        onCheckedChange = { canShareLocation = it }
                    )
                    Text("Share Location")
                }
                
                Spacer(modifier = Modifier.height(8.dp))
                
                Text(
                    text = "Priority Level: $priority",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                
                Text(
                    text = "Higher priority contacts will be contacted first during emergencies",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = { if (priority > 1) priority-- },
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("-")
                    }
                    
                    Button(
                        onClick = { if (priority < 10) priority++ },
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("+")
                    }
                }
                
                Spacer(modifier = Modifier.height(24.dp))
                
                Button(
                    onClick = {
                        onSave(name, phone, relationship, canSendSOS, canShareLocation, priority)
                    },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = name.isNotBlank() && phone.isNotBlank() && relationship.isNotBlank()
                ) {
                    Text(if (isEditing) "Update Contact" else "Save Contact")
                }
            }
        }
    }
}