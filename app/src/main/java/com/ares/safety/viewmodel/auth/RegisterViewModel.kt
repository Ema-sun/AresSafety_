package com.ares.safety.viewmodel.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ares.safety.data.model.User
import com.ares.safety.data.repository.AuthRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.regex.Pattern

class RegisterViewModel : ViewModel() {
    private val repository = AuthRepository()

    // Estado del formulario
    private val _uiState = MutableStateFlow(RegisterUiState())
    val uiState: StateFlow<RegisterUiState> = _uiState.asStateFlow()

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

    // Acciones de UI
    fun onEmailChanged(email: String) {
        _uiState.update { it.copy(email = email) }
        validateForm()
    }

    fun onPasswordChanged(password: String) {
        _uiState.update { it.copy(password = password) }
        validateForm()
    }

    fun onConfirmPasswordChanged(confirmPassword: String) {
        _uiState.update { it.copy(confirmPassword = confirmPassword) }
        validateForm()
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

    fun onTermsCheckedChanged(checked: Boolean) {
        _uiState.update { it.copy(termsAccepted = checked) }
        validateForm()
    }

    fun onRegisterClick() {
        if (!_uiState.value.isFormValid) return

        _uiState.update { it.copy(isLoading = true, errorMessage = null) }

        viewModelScope.launch {
            val userData = User(
                fullName = _uiState.value.fullName,
                phoneNumber = _uiState.value.phoneNumber,
                birthDate = _uiState.value.birthDate,
                address = _uiState.value.address,
                email = _uiState.value.email,
                createdAt = System.currentTimeMillis(),
                updatedAt = System.currentTimeMillis()
            )

            val result = repository.signUp(
                email = _uiState.value.email,
                password = _uiState.value.password,
                userData = userData
            )

            result.fold(
                onSuccess = {
                    _uiState.update { state ->
                        state.copy(
                            isLoading = false,
                            isRegistrationSuccessful = true
                        )
                    }
                },
                onFailure = { exception ->
                    _uiState.update { state ->
                        state.copy(
                            isLoading = false,
                            errorMessage = exception.message
                        )
                    }
                }
            )
        }
    }

    private fun validateForm() {
        val isEmailValid = validateEmail(_uiState.value.email)
        val isPasswordValid = validatePassword(_uiState.value.password)
        val doPasswordsMatch = _uiState.value.password == _uiState.value.confirmPassword
        val isPhoneValid = validatePhone(_uiState.value.phoneNumber)
        val isFullNameValid = _uiState.value.fullName.isNotBlank()

        _uiState.update { state ->
            state.copy(
                isEmailValid = isEmailValid,
                isPasswordValid = isPasswordValid,
                isPhoneValid = isPhoneValid,
                doPasswordsMatch = doPasswordsMatch,
                isFormValid = isEmailValid && isPasswordValid &&
                        doPasswordsMatch && state.termsAccepted &&
                        isFullNameValid && isPhoneValid
            )
        }
    }

    private fun validateEmail(email: String): Boolean {
        return email.isNotBlank() && emailPattern.matcher(email).matches()
    }

    private fun validatePassword(password: String): Boolean {
        // Mínimo 8 caracteres, 1 mayúscula y 1 número
        return password.length >= 8 &&
                password.any { it.isUpperCase() } &&
                password.any { it.isDigit() }
    }

    private fun validatePhone(phone: String): Boolean {
        return phone.length >= 10 && phone.all { it.isDigit() }
    }
}

data class RegisterUiState(
    val email: String = "",
    val password: String = "",
    val confirmPassword: String = "",
    val fullName: String = "",
    val phoneNumber: String = "",
    val birthDate: Long = 0,
    val address: String = "",
    val termsAccepted: Boolean = false,
    val isEmailValid: Boolean = true,
    val isPasswordValid: Boolean = true,
    val isPhoneValid: Boolean = true,
    val doPasswordsMatch: Boolean = true,
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val isRegistrationSuccessful: Boolean = false,
    val isFormValid: Boolean = false
)