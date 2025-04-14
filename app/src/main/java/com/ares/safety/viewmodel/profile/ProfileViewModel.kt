// Mejora para com/ares/safety/viewmodel/profile/ProfileViewModel.kt
package com.ares.safety.viewmodel.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ares.safety.data.model.User
import com.ares.safety.data.repository.UserRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.regex.Pattern

class ProfileViewModel : ViewModel() {
    private val repository = UserRepository()

    private val _uiState = MutableStateFlow(ProfileUiState())
    val uiState: StateFlow<ProfileUiState> = _uiState.asStateFlow()

    // Validación de email
    private val emailPattern = Pattern.compile(
        "[a-zA-Z0-9+._%\\-]{1,256}" +
                "@" +
                "[a-zA-Z0-9][a-zA-Z0-9\\-]{0,64}" +
                "(" +
                "\\." +
                "[a-zA-Z0-9][a-zA-Z0-9\\-]{0,25}" +
                ")+"
    )

    init {
        loadUserProfile()
    }

    private fun loadUserProfile() {
        _uiState.update { it.copy(isLoading = true, errorMessage = null) }

        viewModelScope.launch {
            repository.getCurrentUserProfile().fold(
                onSuccess = { user ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            userId = user.id,
                            email = user.email,
                            fullName = user.fullName,
                            phoneNumber = user.phoneNumber,
                            birthDate = user.birthDate,
                            address = user.address,
                            emergencyMessage = user.emergencyMessage,
                            profilePhotoUrl = user.profilePhotoUrl,
                            isEditing = false,
                            isNewUser = user.fullName.isBlank() // Si no tiene nombre, consideramos que es nuevo
                        )
                    }
                    validateForm()

                    // Si es un usuario nuevo, activar modo edición automáticamente
                    if (user.fullName.isBlank()) {
                        _uiState.update { it.copy(isEditing = true) }
                    }
                },
                onFailure = { exception ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = exception.message ?: "Error al cargar el perfil"
                        )
                    }
                }
            )
        }
    }

    fun toggleEditMode() {
        _uiState.update { it.copy(isEditing = !it.isEditing) }
    }

    fun onFullNameChanged(fullName: String) {
        _uiState.update { it.copy(fullName = fullName) }
        validateForm()
    }

    fun onPhoneNumberChanged(phoneNumber: String) {
        _uiState.update { it.copy(phoneNumber = phoneNumber) }
        validateForm()
    }

    fun onBirthDateChanged(birthDate: Long) {
        _uiState.update { it.copy(birthDate = birthDate) }
        validateForm()
    }

    fun onAddressChanged(address: String) {
        _uiState.update { it.copy(address = address) }
        validateForm()
    }

    fun onEmergencyMessageChanged(message: String) {
        _uiState.update { it.copy(emergencyMessage = message) }
        validateForm()
    }

    fun onProfilePhotoUrlChanged(url: String) {
        _uiState.update { it.copy(profilePhotoUrl = url) }
    }

    fun saveProfile() {
        if (!_uiState.value.isFormValid) return

        _uiState.update { it.copy(isLoading = true, errorMessage = null) }

        viewModelScope.launch {
            val state = _uiState.value
            val user = User(
                id = state.userId,
                email = state.email,
                fullName = state.fullName,
                phoneNumber = state.phoneNumber,
                birthDate = state.birthDate,
                address = state.address,
                emergencyMessage = state.emergencyMessage,
                profilePhotoUrl = state.profilePhotoUrl,
                createdAt = 0, // Se mantendrá el valor original en el servidor
                updatedAt = System.currentTimeMillis()
            )

            repository.updateUserProfile(user).fold(
                onSuccess = {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            isEditing = false,
                            updateSuccess = true,
                            isNewUser = false
                        )
                    }
                },
                onFailure = { exception ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = exception.message ?: "Error al actualizar el perfil"
                        )
                    }
                }
            )
        }
    }

    fun resetUpdateSuccess() {
        _uiState.update { it.copy(updateSuccess = false) }
    }

    fun retryLoading() {
        loadUserProfile()
    }

    private fun validateForm() {
        val isFullNameValid = _uiState.value.fullName.isNotBlank()
        val isPhoneValid = validatePhone(_uiState.value.phoneNumber)
        val isEmergencyMessageValid = _uiState.value.emergencyMessage.isNotBlank()

        _uiState.update { state ->
            state.copy(
                isPhoneValid = isPhoneValid,
                isFormValid = isFullNameValid && isPhoneValid && isEmergencyMessageValid
            )
        }
    }

    private fun validatePhone(phone: String): Boolean {
        return phone.length >= 10 && phone.all { it.isDigit() }
    }
}

data class ProfileUiState(
    val userId: String = "",
    val email: String = "",
    val fullName: String = "",
    val phoneNumber: String = "",
    val birthDate: Long = 0,
    val address: String = "",
    val emergencyMessage: String = "¡Estoy en peligro! Por favor, ayuda.",
    val profilePhotoUrl: String = "",
    val isEditing: Boolean = false,
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val isFormValid: Boolean = false,
    val isPhoneValid: Boolean = true,
    val updateSuccess: Boolean = false,
    val isNewUser: Boolean = false
)