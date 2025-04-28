package com.example.navapp.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import com.example.navapp.UserData

@Composable
fun RegisterScreen(onUserRegistered: (UserData) -> Unit) {
    var name by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var address by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }

    // Validation states
    var nameError by remember { mutableStateOf<String?>(null) }
    var emailError by remember { mutableStateOf<String?>(null) }
    var phoneError by remember { mutableStateOf<String?>(null) }
    var passwordError by remember { mutableStateOf<String?>(null) }
    var confirmPasswordError by remember { mutableStateOf<String?>(null) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "Register",
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.primary
        )

        Card(
            modifier = Modifier.fillMaxWidth(),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedTextField(
                    value = name,
                    onValueChange = {
                        name = it
                        nameError = if (it.isEmpty()) "Name is required" else null
                    },
                    label = { Text("Full Name") },
                    leadingIcon = { Icon(Icons.Default.Person, contentDescription = null) },
                    isError = nameError != null,
                    supportingText = { nameError?.let { Text(it) } },
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = email,
                    onValueChange = {
                        email = it
                        emailError = when {
                            it.isEmpty() -> "Email is required"
                            !android.util.Patterns.EMAIL_ADDRESS.matcher(it).matches() -> "Invalid email format"
                            else -> null
                        }
                    },
                    label = { Text("Email") },
                    leadingIcon = { Icon(Icons.Default.Email, contentDescription = null) },
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Email,
                        imeAction = ImeAction.Next
                    ),
                    isError = emailError != null,
                    supportingText = { emailError?.let { Text(it) } },
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = phone,
                    onValueChange = {
                        phone = it
                        phoneError = when {
                            it.isEmpty() -> "Phone number is required"
                            !it.all { char -> char.isDigit() } -> "Phone must contain only digits"
                            it.length < 10 -> "Phone number too short"
                            else -> null
                        }
                    },
                    label = { Text("Phone") },
                    leadingIcon = { Icon(Icons.Default.Phone, contentDescription = null) },
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Phone,
                        imeAction = ImeAction.Next
                    ),
                    isError = phoneError != null,
                    supportingText = { phoneError?.let { Text(it) } },
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = address,
                    onValueChange = { address = it },
                    label = { Text("Address") },
                    leadingIcon = { Icon(Icons.Default.Home, contentDescription = null) },
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Text,
                        imeAction = ImeAction.Next
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = password,
                    onValueChange = {
                        password = it
                        passwordError = when {
                            it.isEmpty() -> "Password is required"
                            it.length < 6 -> "Password must be at least 6 characters"
                            !it.any { char -> char.isDigit() } -> "Password must contain at least one number"
                            !it.any { char -> char.isLetter() } -> "Password must contain at least one letter"
                            else -> null
                        }
                    },
                    label = { Text("Password") },
                    leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null) },
                    visualTransformation = PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Password,
                        imeAction = ImeAction.Next
                    ),
                    isError = passwordError != null,
                    supportingText = { passwordError?.let { Text(it) } },
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = confirmPassword,
                    onValueChange = {
                        confirmPassword = it
                        confirmPasswordError = when {
                            it.isEmpty() -> "Please confirm your password"
                            it != password -> "Passwords don't match"
                            else -> null
                        }
                    },
                    label = { Text("Confirm Password") },
                    leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null) },
                    visualTransformation = PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Password,
                        imeAction = ImeAction.Done
                    ),
                    isError = confirmPasswordError != null,
                    supportingText = { confirmPasswordError?.let { Text(it) } },
                    modifier = Modifier.fillMaxWidth()
                )

                Button(
                    onClick = {
                        // Validate all fields
                        nameError = if (name.isEmpty()) "Name is required" else null
                        emailError = when {
                            email.isEmpty() -> "Email is required"
                            !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches() -> "Invalid email format"
                            else -> null
                        }
                        phoneError = when {
                            phone.isEmpty() -> "Phone number is required"
                            !phone.all { char -> char.isDigit() } -> "Phone must contain only digits"
                            phone.length < 10 -> "Phone number too short"
                            else -> null
                        }
                        passwordError = when {
                            password.isEmpty() -> "Password is required"
                            password.length < 6 -> "Password must be at least 6 characters"
                            !password.any { char -> char.isDigit() } -> "Password must contain at least one number"
                            !password.any { char -> char.isLetter() } -> "Password must contain at least one letter"
                            else -> null
                        }
                        confirmPasswordError = when {
                            confirmPassword.isEmpty() -> "Please confirm your password"
                            confirmPassword != password -> "Passwords don't match"
                            else -> null
                        }

                        // If all validations pass
                        if (nameError == null && emailError == null && phoneError == null &&
                            passwordError == null && confirmPasswordError == null) {
                            onUserRegistered(UserData(name, email, phone, address, password))
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 16.dp)
                ) {
                    Text("Register")
                }
            }
        }
    }
}