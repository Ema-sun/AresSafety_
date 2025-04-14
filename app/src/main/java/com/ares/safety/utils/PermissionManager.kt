package com.ares.safety.utils

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat

object PermissionManager {

    // Grupos de permisos según la funcionalidad
    val LOCATION_PERMISSIONS = arrayOf(
        Manifest.permission.ACCESS_FINE_LOCATION,
        Manifest.permission.ACCESS_COARSE_LOCATION
    )

    val BACKGROUND_LOCATION_PERMISSION = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
        Manifest.permission.ACCESS_BACKGROUND_LOCATION
    } else {
        ""
    }

    val CONTACTS_PERMISSIONS = arrayOf(
        Manifest.permission.READ_CONTACTS
    )

    val SMS_PERMISSIONS = arrayOf(
        Manifest.permission.SEND_SMS
    )

    val MICROPHONE_PERMISSIONS = arrayOf(
        Manifest.permission.RECORD_AUDIO
    )

    val STORAGE_PERMISSIONS = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        arrayOf(
            Manifest.permission.READ_MEDIA_IMAGES
        )
    } else {
        arrayOf(
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
        )
    }

    val CAMERA_PERMISSIONS = arrayOf(
        Manifest.permission.CAMERA
    )

    // Verificar si todos los permisos en un grupo están concedidos
    fun hasPermissions(context: Context, permissions: Array<String>): Boolean {
        return permissions.all {
            ContextCompat.checkSelfPermission(context, it) == PackageManager.PERMISSION_GRANTED
        }
    }

    // Verificar si debemos mostrar explicación para un grupo de permisos
    fun shouldShowRationale(activity: Activity, permissions: Array<String>): Boolean {
        return permissions.any {
            ActivityCompat.shouldShowRequestPermissionRationale(activity, it)
        }
    }

    // Abrir configuración de la aplicación para permisos
    fun openAppSettings(context: Context) {
        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
            data = Uri.fromParts("package", context.packageName, null)
        }
        context.startActivity(intent)
    }
}

// Composable para solicitar permisos con diálogo de explicación
@Composable
fun RequestPermission(
    permissions: Array<String>,
    rationaleMessage: String,
    permanentlyDeniedMessage: String,
    onPermissionResult: (Boolean) -> Unit
) {
    val context = LocalContext.current
    var showRationale by remember { mutableStateOf(false) }
    var permanentlyDenied by remember { mutableStateOf(false) }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissionsResult ->
        val allGranted = permissionsResult.values.all { it }
        if (allGranted) {
            onPermissionResult(true)
        } else {
            val activity = context as? Activity
            if (activity != null && PermissionManager.shouldShowRationale(activity, permissions)) {
                showRationale = true
            } else {
                permanentlyDenied = true
            }
            onPermissionResult(false)
        }
    }

    // Solicitar permisos al lanzar el composable
    LaunchedEffect(key1 = permissions) {
        if (!PermissionManager.hasPermissions(context, permissions)) {
            val activity = context as? Activity
            if (activity != null && PermissionManager.shouldShowRationale(activity, permissions)) {
                showRationale = true
            } else {
                permissionLauncher.launch(permissions)
            }
        } else {
            onPermissionResult(true)
        }
    }

    // Diálogo para explicar por qué se necesitan los permisos
    if (showRationale) {
        AlertDialog(
            onDismissRequest = { showRationale = false },
            title = { Text("Permisos necesarios") },
            text = { Text(rationaleMessage) },
            confirmButton = {
                TextButton(onClick = {
                    showRationale = false
                    permissionLauncher.launch(permissions)
                }) {
                    Text("Solicitar")
                }
            },
            dismissButton = {
                TextButton(onClick = {
                    showRationale = false
                    onPermissionResult(false)
                }) {
                    Text("Cancelar")
                }
            }
        )
    }

    // Diálogo cuando los permisos son permanentemente denegados
    if (permanentlyDenied) {
        AlertDialog(
            onDismissRequest = { permanentlyDenied = false },
            title = { Text("Permisos denegados") },
            text = { Text(permanentlyDeniedMessage) },
            confirmButton = {
                TextButton(onClick = {
                    permanentlyDenied = false
                    PermissionManager.openAppSettings(context)
                }) {
                    Text("Abrir configuración")
                }
            },
            dismissButton = {
                TextButton(onClick = {
                    permanentlyDenied = false
                    onPermissionResult(false)
                }) {
                    Text("Cancelar")
                }
            }
        )
    }
}