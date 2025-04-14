// Archivo: com/ares/safety/viewmodel/contact/ContactViewModel.kt

package com.ares.safety.viewmodel.contact

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ares.safety.data.model.EmergencyContact
import com.ares.safety.data.repository.ContactRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class ContactViewModel : ViewModel() {
    private val repository = ContactRepository()

    private val _uiState = MutableStateFlow(ContactsUiState())
    val uiState: StateFlow<ContactsUiState> = _uiState.asStateFlow()

    // Para el formulario de añadir/editar contacto
    private val _formState = MutableStateFlow(ContactFormState())
    val formState: StateFlow<ContactFormState> = _formState.asStateFlow()

    // Cargar contactos al iniciar
    init {
        loadContacts()
    }

    fun loadContacts() {
        _uiState.update { it.copy(isLoading = true, errorMessage = null) }

        viewModelScope.launch {
            repository.getEmergencyContacts().fold(
                onSuccess = { contacts ->
                    _uiState.update {
                        it.copy(
                            contacts = contacts,
                            isLoading = false
                        )
                    }
                },
                onFailure = { exception ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = exception.message ?: "Error al cargar contactos"
                        )
                    }
                }
            )
        }
    }

    // Acciones para el formulario
    fun resetForm() {
        _formState.update { ContactFormState() }
    }

    fun loadContactToEdit(contactId: String) {
        val contact = _uiState.value.contacts.find { it.id == contactId }
        contact?.let {
            _formState.update {
                ContactFormState(
                    contactId = contact.id,
                    name = contact.name,
                    phoneNumber = contact.phoneNumber,
                    relationship = contact.relationship,
                    priority = contact.priority,
                    notifyOnEmergency = contact.notifyOnEmergency,
                    isEditing = true
                )
            }
        }
    }

    fun onNameChanged(name: String) {
        _formState.update { it.copy(name = name) }
        validateForm()
    }

    fun onPhoneNumberChanged(phoneNumber: String) {
        _formState.update { it.copy(phoneNumber = phoneNumber) }
        validateForm()
    }

    fun onRelationshipChanged(relationship: String) {
        _formState.update { it.copy(relationship = relationship) }
        validateForm()
    }

    fun onPriorityChanged(priority: Int) {
        _formState.update { it.copy(priority = priority) }
        validateForm()
    }

    fun onNotifyOnEmergencyChanged(notify: Boolean) {
        _formState.update { it.copy(notifyOnEmergency = notify) }
    }

    private fun validateForm() {
        val isNameValid = _formState.value.name.isNotBlank()
        val isPhoneValid = _formState.value.phoneNumber.length >= 10

        _formState.update {
            it.copy(isFormValid = isNameValid && isPhoneValid)
        }
    }

    // Guardar contacto (nuevo o editado)
    fun saveContact() {
        if (!_formState.value.isFormValid) return

        _formState.update { it.copy(isLoading = true, errorMessage = null) }

        viewModelScope.launch {
            val formState = _formState.value

            val contact = EmergencyContact(
                id = formState.contactId,
                name = formState.name,
                phoneNumber = formState.phoneNumber,
                relationship = formState.relationship,
                priority = formState.priority,
                notifyOnEmergency = formState.notifyOnEmergency
            )

            val result = if (formState.isEditing) {
                repository.updateEmergencyContact(contact)
                    .map { contact }  // Convertir Result<Unit> a Result<EmergencyContact>
            } else {
                repository.addEmergencyContact(contact)
            }

            result.fold(
                onSuccess = { savedContact ->
                    _formState.update {
                        it.copy(
                            isLoading = false,
                            isSaved = true
                        )
                    }
                    // Recargar la lista de contactos
                    loadContacts()
                },
                onFailure = { exception ->
                    _formState.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = exception.message ?: "Error al guardar el contacto"
                        )
                    }
                }
            )
        }
    }

    fun deleteContact(contactId: String) {
        _uiState.update { it.copy(isLoading = true, errorMessage = null) }

        viewModelScope.launch {
            repository.deleteEmergencyContact(contactId).fold(
                onSuccess = {
                    // Recargar la lista sin el contacto eliminado
                    loadContacts()
                },
                onFailure = { exception ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = exception.message ?: "Error al eliminar el contacto"
                        )
                    }
                }
            )
        }
    }
}

data class ContactsUiState(
    val contacts: List<EmergencyContact> = emptyList(),
    val isLoading: Boolean = false,
    val errorMessage: String? = null
)

data class ContactFormState(
    val contactId: String = "",
    val name: String = "",
    val phoneNumber: String = "",
    val relationship: String = "",
    val priority: Int = 1,
    val notifyOnEmergency: Boolean = true,
    val isEditing: Boolean = false,
    val isFormValid: Boolean = false,
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val isSaved: Boolean = false
)