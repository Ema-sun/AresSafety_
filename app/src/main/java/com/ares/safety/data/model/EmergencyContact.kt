// Archivo: com/ares/safety/data/model/EmergencyContact.kt

package com.ares.safety.data.model

import com.google.firebase.firestore.DocumentId

data class EmergencyContact(
    @DocumentId val id: String = "",
    val userId: String = "",  // ID del usuario al que pertenece este contacto
    val name: String = "",
    val phoneNumber: String = "",
    val relationship: String = "",  // Ejemplo: "Familiar", "Amigo", "Pareja"
    val priority: Int = 0,  // Para ordenar los contactos (1: primario, 2: secundario, etc.)
    val notifyOnEmergency: Boolean = true,  // Si se debe notificar a este contacto en caso de emergencia
    val createdAt: Long = 0,
    val updatedAt: Long = 0
)