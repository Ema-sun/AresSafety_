// Actualización de: com/ares/safety/data/model/User.kt
package com.ares.safety.data.model

data class User(
    val id: String = "",
    val email: String = "",
    val fullName: String = "",
    val phoneNumber: String = "",
    val birthDate: Long = 0,
    val address: String = "",
    val emergencyMessage: String = "¡Estoy en peligro! Por favor, ayuda.", // Mensaje predeterminado para alertas
    val profilePhotoUrl: String = "", // URL para foto de perfil
    val createdAt: Long = 0,
    val updatedAt: Long = 0
)