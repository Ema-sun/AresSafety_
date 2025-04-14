package com.ares.safety.viewmodel.auth

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.ares.safety.data.repository.AuthRepository
import com.ares.safety.utils.PreferencesManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class LoginViewModel(private val preferencesManager: PreferencesManager) : ViewModel() {
    private val repository = AuthRepository()

    // Estado del formulario
    private val _uiState = MutableStateFlow(LoginUiState())
    val uiState: StateFlow<LoginUiState> = _uiState.asStateFlow()

    // Contador de intentos fallidos
    private var failedAttempts = 0

    init {
        // Cargar credenciales guardadas si está habilitada la opción
        if (preferencesManager.shouldRememberCredentials()) {
            val savedEmail = preferencesManager.getSavedEmail()
            val savedPassword = preferencesManager.getSavedPassword()

            if (savedEmail.isNotEmpty() && savedPassword.isNotEmpty()) {
                _uiState.update {
                    it.copy(
                        email = savedEmail,
                        password = savedPassword,
                        rememberMe = true
                    )
                }
                validateForm()
            }
        }
    }

    // Acciones de UI
    fun onEmailChanged(email: String) {
        _uiState.update { it.copy(email = email) }
        validateForm()
    }

    fun onPasswordChanged(password: String) {
        _uiState.update { it.copy(password = password) }
        validateForm()
    }

    fun onRememberMeChanged(rememberMe: Boolean) {
        _uiState.update { it.copy(rememberMe = rememberMe) }

        if (!rememberMe) {
            preferencesManager.setRememberCredentials(false)
            preferencesManager.clearCredentials()
        }
    }

    fun onLoginClick() {
        if (!_uiState.value.isFormValid) return

        _uiState.update { it.copy(isLoading = true, errorMessage = null) }

        viewModelScope.launch {
            val result = repository.signIn(
                _uiState.value.email,
                _uiState.value.password
            )

            result.fold(
                onSuccess = {
                    failedAttempts = 0

                    // Guardar credenciales si "Recordarme" está activado
                    if (_uiState.value.rememberMe) {
                        preferencesManager.setRememberCredentials(true)
                        preferencesManager.saveCredentials(
                            _uiState.value.email,
                            _uiState.value.password
                        )
                    } else {
                        preferencesManager.setRememberCredentials(false)
                        preferencesManager.clearCredentials()
                    }

                    _uiState.update { state ->
                        state.copy(
                            isLoading = false,
                            isLoginSuccessful = true
                        )
                    }
                },
                onFailure = { exception ->
                    failedAttempts++

                    val errorMessage = when {
                        failedAttempts >= 3 -> "Demasiados intentos fallidos. Intenta en 5 minutos."
                        else -> exception.message ?: "Error al iniciar sesión"
                    }

                    _uiState.update { state ->
                        state.copy(
                            isLoading = false,
                            errorMessage = errorMessage,
                            isTemporarilyBlocked = failedAttempts >= 3
                        )
                    }
                }
            )
        }
    }

    fun onForgotPasswordClick() {
        _uiState.update { it.copy(showPasswordRecovery = true) }
    }

    fun onSendPasswordResetClick() {
        if (_uiState.value.email.isBlank()) {
            _uiState.update { it.copy(resetPasswordError = "Ingresa tu correo electrónico") }
            return
        }

        _uiState.update { it.copy(isLoading = true, resetPasswordError = null) }

        viewModelScope.launch {
            val result = repository.resetPassword(_uiState.value.email)

            result.fold(
                onSuccess = {
                    _uiState.update { state ->
                        state.copy(
                            isLoading = false,
                            passwordResetSent = true,
                            showPasswordRecovery = false
                        )
                    }
                },
                onFailure = { exception ->
                    _uiState.update { state ->
                        state.copy(
                            isLoading = false,
                            resetPasswordError = exception.message
                        )
                    }
                }
            )
        }
    }

    fun dismissPasswordRecovery() {
        _uiState.update { it.copy(showPasswordRecovery = false, resetPasswordError = null) }
    }

    private fun validateForm() {
        val isEmailValid = _uiState.value.email.isNotBlank()
        val isPasswordValid = _uiState.value.password.isNotBlank()

        _uiState.update { state ->
            state.copy(
                isFormValid = isEmailValid && isPasswordValid && !state.isTemporarilyBlocked
            )
        }
    }
}

data class LoginUiState(
    val email: String = "",
    val password: String = "",
    val rememberMe: Boolean = false,
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val isLoginSuccessful: Boolean = false,
    val isFormValid: Boolean = false,
    val isTemporarilyBlocked: Boolean = false,
    val showPasswordRecovery: Boolean = false,
    val resetPasswordError: String? = null,
    val passwordResetSent: Boolean = false
)

// Factory para proporcionar contexto al ViewModel
class LoginViewModelFactory(private val context: Context) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(LoginViewModel::class.java)) {
            return LoginViewModel(PreferencesManager(context)) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}