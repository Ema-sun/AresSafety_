package com.ares.safety.ui.screens.auth

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.ares.safety.R
import com.ares.safety.ui.components.AddressTextField
import com.ares.safety.ui.components.AresCheckbox
import com.ares.safety.ui.components.AresPrimaryButton
import com.ares.safety.ui.components.DatePickerField
import com.ares.safety.ui.components.EmailTextField
import com.ares.safety.ui.components.FullNameTextField
import com.ares.safety.ui.components.LogoPlaceholder
import com.ares.safety.ui.components.PasswordStrengthIndicator
import com.ares.safety.ui.components.PasswordTextField
import com.ares.safety.ui.components.PhoneTextField
import com.ares.safety.ui.components.SwitchAuthText
import com.ares.safety.viewmodel.auth.RegisterViewModel

@Composable
fun RegisterScreen(
    navigateToLogin: () -> Unit,
    navigateToHome: () -> Unit,
    viewModel: RegisterViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val scrollState = rememberScrollState()

    // Manejar navegación tras registro exitoso
    LaunchedEffect(uiState.isRegistrationSuccessful) {
        if (uiState.isRegistrationSuccessful) {
            navigateToHome()
        }
    }

    // Mostrar errores
    LaunchedEffect(uiState.errorMessage) {
        uiState.errorMessage?.let {
            snackbarHostState.showSnackbar(it)
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 24.dp)
                .verticalScroll(scrollState),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(24.dp))

            LogoPlaceholder()

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = stringResource(R.string.register_title),
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.tertiary,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(32.dp))

            // Nombre completo (nuevo campo)
            FullNameTextField(
                value = uiState.fullName,
                onValueChange = viewModel::onFullNameChanged,
                isError = uiState.fullName.isBlank() && uiState.fullName != "",
                errorMessage = if (uiState.fullName.isBlank() && uiState.fullName != "")
                    stringResource(R.string.fullname_required) else null
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Email
            EmailTextField(
                value = uiState.email,
                onValueChange = viewModel::onEmailChanged,
                isError = !uiState.isEmailValid && uiState.email.isNotEmpty(),
                errorMessage = if (!uiState.isEmailValid && uiState.email.isNotEmpty())
                    stringResource(R.string.email_invalid) else null
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Teléfono (nuevo campo)
            PhoneTextField(
                value = uiState.phoneNumber,
                onValueChange = viewModel::onPhoneNumberChanged,
                isError = !uiState.isPhoneValid && uiState.phoneNumber.isNotEmpty(),
                errorMessage = if (!uiState.isPhoneValid && uiState.phoneNumber.isNotEmpty())
                    stringResource(R.string.phone_invalid) else null
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Fecha de nacimiento (nuevo campo)
            DatePickerField(
                value = uiState.birthDate,
                onValueChange = viewModel::onBirthDateChanged
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Dirección (nuevo campo)
            AddressTextField(
                value = uiState.address,
                onValueChange = viewModel::onAddressChanged
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Contraseña
            PasswordTextField(
                value = uiState.password,
                onValueChange = viewModel::onPasswordChanged,
                isError = !uiState.isPasswordValid && uiState.password.isNotEmpty(),
                errorMessage = if (!uiState.isPasswordValid && uiState.password.isNotEmpty())
                    stringResource(R.string.password_invalid) else null
            )

            if (uiState.password.isNotEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                PasswordStrengthIndicator(password = uiState.password)
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Confirmar contraseña
            PasswordTextField(
                value = uiState.confirmPassword,
                onValueChange = viewModel::onConfirmPasswordChanged,
                label = stringResource(R.string.confirm_password_label),
                isError = !uiState.doPasswordsMatch && uiState.confirmPassword.isNotEmpty(),
                errorMessage = if (!uiState.doPasswordsMatch && uiState.confirmPassword.isNotEmpty())
                    stringResource(R.string.passwords_not_match) else null
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Términos y condiciones
            AresCheckbox(
                text = stringResource(R.string.terms_conditions),
                checked = uiState.termsAccepted,
                onCheckedChange = viewModel::onTermsCheckedChanged
            )

            Spacer(modifier = Modifier.height(32.dp))

            // Botón de registro
            AresPrimaryButton(
                text = stringResource(R.string.register_button),
                onClick = viewModel::onRegisterClick,
                enabled = uiState.isFormValid && !uiState.isLoading,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Link para ir a login
            SwitchAuthText(
                questionText = stringResource(R.string.already_account),
                actionText = stringResource(R.string.login_now),
                onClick = navigateToLogin
            )

            // Espacio adicional para scroll
            Spacer(modifier = Modifier.height(50.dp))
        }
    }
}