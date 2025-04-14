// Mejora para com/ares/safety/ui/screens/profile/ProfileScreen.kt
package com.ares.safety.ui.screens.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.ares.safety.ui.components.AresPrimaryButton
import com.ares.safety.ui.components.DatePickerField
import com.ares.safety.viewmodel.profile.ProfileViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    onNavigateBack: () -> Unit,
    viewModel: ProfileViewModel
) {
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val scrollState = rememberScrollState()
    val context = LocalContext.current

    // Mostrar errores
    LaunchedEffect(uiState.errorMessage) {
        uiState.errorMessage?.let {
            snackbarHostState.showSnackbar(it)
        }
    }

    // Mostrar mensaje de éxito
    LaunchedEffect(uiState.updateSuccess) {
        if (uiState.updateSuccess) {
            snackbarHostState.showSnackbar("Perfil actualizado correctamente")
            viewModel.resetUpdateSuccess()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Mi Perfil") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Volver"
                        )
                    }
                },
                actions = {
                    IconButton(onClick = { viewModel.toggleEditMode() }) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = if (uiState.isEditing) "Cancelar edición" else "Editar perfil"
                        )
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            if (uiState.isLoading) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            } else if (uiState.errorMessage != null) {
                // Pantalla de error con opción para reintentar
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Error,
                        contentDescription = "Error",
                        modifier = Modifier.size(64.dp),
                        tint = MaterialTheme.colorScheme.error
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = "No se pudo cargar el perfil",
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.error
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = uiState.errorMessage ?: "Error desconocido",
                        style = MaterialTheme.typography.bodyMedium
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    Button(onClick = { viewModel.retryLoading() }) {
                        Text("Reintentar")
                    }
                }
            } else {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp)
                        .verticalScroll(scrollState),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Sección de avatar
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 16.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        // Placeholder para avatar
                        Box(
                            modifier = Modifier
                                .size(100.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.primaryContainer),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Person,
                                contentDescription = "Avatar",
                                modifier = Modifier.size(60.dp),
                                tint = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }
                    }

                    // Mostrar mensaje de bienvenida para nuevos usuarios
                    if (uiState.isNewUser) {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.primaryContainer
                            )
                        ) {
                            Column(
                                modifier = Modifier.padding(16.dp)
                            ) {
                                Text(
                                    text = "¡Bienvenida a Ares Safety!",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )

                                Spacer(modifier = Modifier.height(8.dp))

                                Text(
                                    text = "Por favor, completa tu perfil para mejorar tu experiencia y poder asistirte en situaciones de emergencia.",
                                    style = MaterialTheme.typography.bodyMedium
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))
                    }

                    // Sección Información personal
                    Text(
                        text = "Información personal",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )

                    Divider()

                    // Email (solo lectura)
                    OutlinedTextField(
                        value = uiState.email,
                        onValueChange = { /* No editable */ },
                        label = { Text("Correo electrónico") },
                        modifier = Modifier.fillMaxWidth(),
                        readOnly = true,
                        enabled = false
                    )

                    // Nombre completo
                    OutlinedTextField(
                        value = uiState.fullName,
                        onValueChange = { if (uiState.isEditing) viewModel.onFullNameChanged(it) },
                        label = { Text("Nombre completo") },
                        modifier = Modifier.fillMaxWidth(),
                        readOnly = !uiState.isEditing,
                        isError = uiState.fullName.isBlank() && uiState.isEditing,
                        supportingText = if (uiState.fullName.isBlank() && uiState.isEditing) {
                            { Text("El nombre es obligatorio") }
                        } else null
                    )

                    // Teléfono
                    OutlinedTextField(
                        value = uiState.phoneNumber,
                        onValueChange = { if (uiState.isEditing) viewModel.onPhoneNumberChanged(it) },
                        label = { Text("Número de teléfono") },
                        modifier = Modifier.fillMaxWidth(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                        readOnly = !uiState.isEditing,
                        isError = !uiState.isPhoneValid && uiState.isEditing,
                        supportingText = if (!uiState.isPhoneValid && uiState.isEditing) {
                            { Text("Ingresa un número válido de al menos 10 dígitos") }
                        } else null
                    )

                    // Fecha de nacimiento
                    if (uiState.isEditing) {
                        DatePickerField(
                            value = uiState.birthDate,
                            onValueChange = { viewModel.onBirthDateChanged(it) },
                            label = "Fecha de nacimiento"
                        )
                    } else {
                        val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
                        val birthDateText = if (uiState.birthDate > 0) {
                            dateFormat.format(Date(uiState.birthDate))
                        } else {
                            "No especificada"
                        }

                        OutlinedTextField(
                            value = birthDateText,
                            onValueChange = { },
                            label = { Text("Fecha de nacimiento") },
                            modifier = Modifier.fillMaxWidth(),
                            readOnly = true
                        )
                    }

                    // Dirección
                    OutlinedTextField(
                        value = uiState.address,
                        onValueChange = { if (uiState.isEditing) viewModel.onAddressChanged(it) },
                        label = { Text("Dirección") },
                        modifier = Modifier.fillMaxWidth(),
                        readOnly = !uiState.isEditing
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Sección Configuración de emergencia
                    Text(
                        text = "Configuración de emergencia",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )

                    Divider()

                    // Mensaje de emergencia
                    OutlinedTextField(
                        value = uiState.emergencyMessage,
                        onValueChange = { if (uiState.isEditing) viewModel.onEmergencyMessageChanged(it) },
                        label = { Text("Mensaje de emergencia") },
                        modifier = Modifier.fillMaxWidth(),
                        readOnly = !uiState.isEditing,
                        isError = uiState.emergencyMessage.isBlank() && uiState.isEditing,
                        supportingText = if (uiState.isEditing) {
                            { Text("Este mensaje se enviará a tus contactos de emergencia") }
                        } else null
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    // Botón guardar (solo visible en modo edición)
                    if (uiState.isEditing) {
                        AresPrimaryButton(
                            text = "Guardar cambios",
                            onClick = { viewModel.saveProfile() },
                            enabled = uiState.isFormValid && !uiState.isLoading,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }
        }
    }
}