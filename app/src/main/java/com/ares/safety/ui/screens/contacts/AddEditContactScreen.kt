// Archivo: com/ares/safety/ui/screens/contacts/AddEditContactScreen.kt

package com.ares.safety.ui.screens.contacts

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ContactPhone
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.ares.safety.ui.components.AresCheckbox
import com.ares.safety.ui.components.AresPrimaryButton
import com.ares.safety.viewmodel.contact.ContactViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditContactScreen(
    contactId: String? = null,
    onNavigateBack: () -> Unit,
    viewModel: ContactViewModel
) {
    val formState by viewModel.formState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val scrollState = rememberScrollState()

    // Si se proporciona un ID de contacto, cargar para editar
    LaunchedEffect(contactId) {
        if (contactId != null) {
            viewModel.loadContactToEdit(contactId)
        } else {
            viewModel.resetForm()
        }
    }

    // Volver después de guardar
    LaunchedEffect(formState.isSaved) {
        if (formState.isSaved) {
            onNavigateBack()
        }
    }

    // Mostrar errores
    LaunchedEffect(formState.errorMessage) {
        formState.errorMessage?.let {
            snackbarHostState.showSnackbar(it)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = if (formState.isEditing) "Editar contacto" else "Nuevo contacto"
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Volver"
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
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
                    .verticalScroll(scrollState),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Nombre
                OutlinedTextField(
                    value = formState.name,
                    onValueChange = viewModel::onNameChanged,
                    label = { Text("Nombre completo") },
                    modifier = Modifier.fillMaxWidth(),
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.tertiary
                        )
                    },
                    isError = formState.name.isBlank() && formState.name != ""
                )

                // Teléfono
                OutlinedTextField(
                    value = formState.phoneNumber,
                    onValueChange = viewModel::onPhoneNumberChanged,
                    label = { Text("Número de teléfono") },
                    modifier = Modifier.fillMaxWidth(),
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Phone,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.tertiary
                        )
                    },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                    isError = formState.phoneNumber.length < 10 && formState.phoneNumber.isNotEmpty()
                )

                // Relación
                RelationshipDropdown(
                    selected = formState.relationship,
                    onSelect = viewModel::onRelationshipChanged
                )

                // Prioridad
                PriorityDropdown(
                    selected = formState.priority,
                    onSelect = viewModel::onPriorityChanged
                )

                // Notificar en emergencia
                AresCheckbox(
                    text = "Notificar en caso de emergencia",
                    checked = formState.notifyOnEmergency,
                    onCheckedChange = viewModel::onNotifyOnEmergencyChanged,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(24.dp))

                // Botón guardar
                AresPrimaryButton(
                    text = if (formState.isEditing) "Actualizar contacto" else "Guardar contacto",
                    onClick = viewModel::saveContact,
                    enabled = formState.isFormValid && !formState.isLoading,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RelationshipDropdown(
    selected: String,
    onSelect: (String) -> Unit
) {
    val relationships = listOf("Familiar", "Amigo/a", "Pareja", "Compañero/a", "Otro")
    var expanded by remember { mutableStateOf(false) }
    val displayText = if (selected.isEmpty()) "Seleccionar relación" else selected

    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = "Relación",
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.padding(start = 4.dp, bottom = 4.dp)
        )

        ExposedDropdownMenuBox(
            expanded = expanded,
            onExpandedChange = { expanded = !expanded }
        ) {
            OutlinedTextField(
                modifier = Modifier
                    .fillMaxWidth()
                    .menuAnchor(),
                readOnly = true,
                value = displayText,
                onValueChange = {},
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.ContactPhone,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.tertiary
                    )
                },
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) }
            )

            ExposedDropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false }
            ) {
                relationships.forEach { option ->
                    DropdownMenuItem(
                        text = { Text(text = option) },
                        onClick = {
                            onSelect(option)
                            expanded = false
                        }
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PriorityDropdown(
    selected: Int,
    onSelect: (Int) -> Unit
) {
    val priorities = listOf(1, 2, 3)
    var expanded by remember { mutableStateOf(false) }

    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = "Prioridad (1: principal, 3: baja)",
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.padding(start = 4.dp, bottom = 4.dp)
        )

        ExposedDropdownMenuBox(
            expanded = expanded,
            onExpandedChange = { expanded = !expanded }
        ) {
            OutlinedTextField(
                modifier = Modifier
                    .fillMaxWidth()
                    .menuAnchor(),
                readOnly = true,
                value = selected.toString(),
                onValueChange = {},
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) }
            )

            ExposedDropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false }
            ) {
                priorities.forEach { option ->
                    DropdownMenuItem(
                        text = { Text(text = option.toString()) },
                        onClick = {
                            onSelect(option)
                            expanded = false
                        }
                    )
                }
            }
        }
    }
}