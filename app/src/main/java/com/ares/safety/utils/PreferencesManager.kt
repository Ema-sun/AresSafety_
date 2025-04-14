package com.ares.safety.utils

import android.content.Context
import android.content.SharedPreferences
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey

class PreferencesManager(context: Context) {

    // Crear clave maestra para el cifrado
    private val masterKey = MasterKey.Builder(context)
        .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
        .build()

    // SharedPreferences normales para configuraciones no sensibles
    private val preferences: SharedPreferences = context.getSharedPreferences(
        PREFERENCES_NAME, Context.MODE_PRIVATE
    )

    // SharedPreferences cifradas para datos sensibles (credenciales)
    private val securePreferences: SharedPreferences = EncryptedSharedPreferences.create(
        context,
        SECURE_PREFERENCES_NAME,
        masterKey,
        EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
        EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
    )

    // Primera vez que se abre la app
    fun isFirstLaunch(): Boolean {
        return preferences.getBoolean(KEY_FIRST_LAUNCH, true)
    }

    fun setFirstLaunch(isFirstLaunch: Boolean) {
        preferences.edit().putBoolean(KEY_FIRST_LAUNCH, isFirstLaunch).apply()
    }

    // Recordar credenciales
    fun setRememberCredentials(remember: Boolean) {
        preferences.edit().putBoolean(KEY_REMEMBER_CREDENTIALS, remember).apply()
    }

    fun shouldRememberCredentials(): Boolean {
        return preferences.getBoolean(KEY_REMEMBER_CREDENTIALS, false)
    }

    // Guardar credenciales de forma segura
    fun saveCredentials(email: String, password: String) {
        securePreferences.edit()
            .putString(KEY_EMAIL, email)
            .putString(KEY_PASSWORD, password)
            .apply()
    }

    fun getSavedEmail(): String {
        return securePreferences.getString(KEY_EMAIL, "") ?: ""
    }

    fun getSavedPassword(): String {
        return securePreferences.getString(KEY_PASSWORD, "") ?: ""
    }

    fun clearCredentials() {
        securePreferences.edit()
            .remove(KEY_EMAIL)
            .remove(KEY_PASSWORD)
            .apply()
    }

    companion object {
        private const val PREFERENCES_NAME = "ares_preferences"
        private const val SECURE_PREFERENCES_NAME = "ares_secure_preferences"
        private const val KEY_FIRST_LAUNCH = "first_launch"
        private const val KEY_REMEMBER_CREDENTIALS = "remember_credentials"
        private const val KEY_EMAIL = "email"
        private const val KEY_PASSWORD = "password"
    }
}