package com.ares.safety.ui.screens.auth

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
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.ares.safety.R
import com.ares.safety.ui.components.AresCheckbox
import com.ares.safety.ui.components.AresPrimaryButton
import com.ares.safety.ui.components.AresTextButton
import com.ares.safety.ui.components.EmailTextField
import com.ares.safety.ui.components.LogoPlaceholder
import com.ares.safety.ui.components.PasswordTextField
import com.ares.safety.ui.components.SwitchAuthText
import com.ares.safety.viewmodel.auth.LoginViewModel
import com.ares.safety.viewmodel.auth.LoginViewModelFactory

@Composable
fun LoginScreen(
    navigateToRegister: () -> Unit,
    navigateToHome: () -> Unit
) {
    val context = LocalContext.current
    val viewModel: LoginViewModel = viewModel(factory = LoginViewModelFactory(context))

    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val scrollState = rememberScrollState()

    // Calcular espaciado dinámico basado en la densidad de la pantalla
    val density = LocalDensity.current
    val smallSpacing = with(density) { 8.dp }
    val mediumSpacing = with(density) { 16.dp }
    val largeSpacing = with(density) { 24.dp }

    // Manejar navegación tras login exitoso
    LaunchedEffect(uiState.isLoginSuccessful) {
        if (uiState.isLoginSuccessful) {
            navigateToHome()
        }
    }

    // Mostrar errores
    LaunchedEffect(uiState.errorMessage) {
        uiState.errorMessage?.let {
            snackbarHostState.showSnackbar(it)
        }
    }

    // Confirmación de envío de correo de recuperación
    LaunchedEffect(uiState.passwordResetSent) {
        if (uiState.passwordResetSent) {
            snackbarHostState.showSnackbar("Se ha enviado un correo para restablecer tu contraseña")
        }
    }

    Scaffold(
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
                    .padding(paddingValues)
                    .padding(horizontal = 16.dp)
                    .verticalScroll(scrollState),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Espacio flexible al inicio que se comprime en pantallas pequeñas
                Spacer(modifier = Modifier.weight(0.1f))

                // Logo con tamaño adaptable
                LogoPlaceholder(
                    modifier = Modifier.fillMaxWidth(0.7f)
                )

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = stringResource(R.string.login_title),
                    style = MaterialTheme.typography.headlineMedium,
                    color = MaterialTheme.colorScheme.tertiary,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(24.dp))

                // Campos de formulario
                EmailTextField(
                    value = uiState.email,
                    onValueChange = viewModel::onEmailChanged
                )

                Spacer(modifier = Modifier.height(12.dp))

                PasswordTextField(
                    value = uiState.password,
                    onValueChange = viewModel::onPasswordChanged
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Opciones de recordarme y olvidé mi contraseña
                // En pantallas pequeñas, posiblemente queden en líneas separadas
                Box(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    AresCheckbox(
                        text = stringResource(R.string.remember_me),
                        checked = uiState.rememberMe,
                        onCheckedChange = viewModel::onRememberMeChanged,
                        modifier = Modifier.align(Alignment.CenterStart)
                    )

                    AresTextButton(
                        text = stringResource(R.string.forgot_password),
                        onClick = viewModel::onForgotPasswordClick,
                        modifier = Modifier.align(Alignment.CenterEnd)
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Botón de login
                AresPrimaryButton(
                    text = stringResource(R.string.login_button),
                    onClick = viewModel::onLoginClick,
                    enabled = uiState.isFormValid && !uiState.isLoading
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Opción para registrarse
                SwitchAuthText(
                    questionText = stringResource(R.string.no_account),
                    actionText = stringResource(R.string.register_now),
                    onClick = navigateToRegister
                )

                // Espacio flexible al final que se comprime en pantallas pequeñas
                Spacer(modifier = Modifier.weight(0.1f))
            }
        }

        // Diálogo de recuperación de contraseña
        if (uiState.showPasswordRecovery) {
            ForgotPasswordDialog(
                email = uiState.email,
                onEmailChange = viewModel::onEmailChanged,
                onSendClick = viewModel::onSendPasswordResetClick,
                onDismiss = viewModel::dismissPasswordRecovery,
                errorMessage = uiState.resetPasswordError
            )
        }
    }
}

@Composable
fun ForgotPasswordDialog(
    email: String,
    onEmailChange: (String) -> Unit,
    onSendClick: () -> Unit,
    onDismiss: () -> Unit,
    errorMessage: String?
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Recuperar contraseña") },
        text = {
            Column {
                Text(
                    "Ingresa tu correo electrónico para recibir un enlace de recuperación",
                    style = MaterialTheme.typography.bodyMedium
                )

                Spacer(modifier = Modifier.height(16.dp))

                EmailTextField(
                    value = email,
                    onValueChange = onEmailChange
                )

                if (errorMessage != null) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = errorMessage,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onSendClick) {
                Text("Enviar")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar")
            }
        }
    )
}