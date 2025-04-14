package com.ares.safety

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.ares.safety.data.repository.AuthRepository
import com.ares.safety.ui.navigation.AppNavigation
import com.ares.safety.ui.navigation.Screen
import com.ares.safety.ui.theme.AresSafetyTheme
import com.ares.safety.utils.PreferencesManager

class MainActivity : ComponentActivity() {

    private val authRepository = AuthRepository()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val preferencesManager = PreferencesManager(this)

        setContent {
            AresSafetyTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    // Determinar la pantalla inicial
                    val startDestination = when {
                        // Si hay usuario autenticado, ir a Home
                        authRepository.getCurrentUser() != null -> {
                            Screen.Home.route
                        }
                        // Si es primera ejecución, mostrar Onboarding
                        preferencesManager.isFirstLaunch() -> {
                            Screen.Onboarding.route
                        }
                        // En cualquier otro caso, ir a Login
                        else -> {
                            Screen.Login.route
                        }
                    }

                    AppNavigation(startDestination = startDestination)
                }
            }
        }
    }
}