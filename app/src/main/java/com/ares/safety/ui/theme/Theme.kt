package com.ares.safety.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

// Esquema de colores oscuro (el actual)
private val DarkColorScheme = darkColorScheme(
    primary = Primary,
    primaryContainer = PrimaryVariant,
    secondary = Secondary,
    secondaryContainer = SecondaryVariant,
    tertiary = Tertiary,
    tertiaryContainer = TertiaryVariant,
    background = Background,
    surface = Surface,
    onPrimary = OnPrimary,
    onSecondary = OnSecondary,
    onBackground = OnBackground,
    onSurface = OnSurface,
    onTertiary = OnTertiary,
    error = Error,
    onError = OnError
)

// Esquema de colores claro (nuevo)
private val LightColorScheme = lightColorScheme(
    primary = Primary,
    primaryContainer = PrimaryVariant,
    secondary = Secondary,
    secondaryContainer = SecondaryVariant,
    tertiary = Tertiary,
    tertiaryContainer = TertiaryVariant,
    // Para el tema claro, usamos colores más claros para el fondo
    background = Color(0xFFF5F0F8),  // Lavanda muy claro
    surface = Color(0xFFFAF5FF),     // Blanco ligeramente lavanda
    onPrimary = OnPrimary,
    onSecondary = OnSecondary,
    // Para el tema claro, usamos textos oscuros
    onBackground = Color(0xFF1B0A2B), // Púrpura muy oscuro
    onSurface = Color(0xFF1B0A2B),    // Púrpura muy oscuro
    onTertiary = Color(0xFF1B0A2B),   // Púrpura muy oscuro
    error = Error,
    onError = OnError
)

@Composable
fun AresSafetyTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            // Configura el color de la barra de estado
            window.statusBarColor = colorScheme.primary.toArgb()
            // Configura el color de la barra de navegación
            window.navigationBarColor = colorScheme.primary.toArgb()

            // Configura el modo claro/oscuro para ambas barras
            val insetsController = WindowCompat.getInsetsController(window, view)
            insetsController.isAppearanceLightStatusBars = !darkTheme
            insetsController.isAppearanceLightNavigationBars = !darkTheme
        }
    }
    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}