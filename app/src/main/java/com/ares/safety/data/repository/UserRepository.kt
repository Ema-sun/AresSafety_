// Mejora para com/ares/safety/data/repository/UserRepository.kt
package com.ares.safety.data.repository

import com.ares.safety.data.model.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

class UserRepository {
    private val auth = FirebaseAuth.getInstance()
    private val firestore = FirebaseFirestore.getInstance()
    private val usersCollection = "users"

    suspend fun getCurrentUserProfile(): Result<User> = withContext(Dispatchers.IO) {
        try {
            val userId = auth.currentUser?.uid ?: return@withContext Result.failure(Exception("Usuario no autenticado"))
            val userEmail = auth.currentUser?.email ?: ""

            val document = firestore.collection(usersCollection)
                .document(userId)
                .get()
                .await()

            if (document.exists()) {
                val user = document.toObject(User::class.java)
                    ?: return@withContext Result.failure(Exception("Error al obtener el perfil"))

                Result.success(user)
            } else {
                // Si el perfil no existe, creamos uno básico
                val newUser = User(
                    id = userId,
                    email = userEmail,
                    createdAt = System.currentTimeMillis(),
                    updatedAt = System.currentTimeMillis()
                )

                // Intentamos crear el perfil
                try {
                    firestore.collection(usersCollection)
                        .document(userId)
                        .set(newUser)
                        .await()

                    Result.success(newUser)
                } catch (e: Exception) {
                    Result.failure(Exception("No se pudo crear el perfil: ${e.message}"))
                }
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun updateUserProfile(user: User): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val userId = auth.currentUser?.uid ?: return@withContext Result.failure(Exception("Usuario no autenticado"))

            // Asegurarse de que el ID coincide con el usuario actual
            val userToUpdate = if (user.id.isEmpty()) {
                user.copy(id = userId)
            } else if (user.id != userId) {
                return@withContext Result.failure(Exception("No puedes actualizar un perfil que no te pertenece"))
            } else {
                user
            }

            // Preparar los campos a actualizar
            val updatedFields = mutableMapOf<String, Any>(
                "fullName" to userToUpdate.fullName,
                "phoneNumber" to userToUpdate.phoneNumber,
                "birthDate" to userToUpdate.birthDate,
                "address" to userToUpdate.address,
                "emergencyMessage" to userToUpdate.emergencyMessage,
                "updatedAt" to System.currentTimeMillis()
            )

            // Añadir foto de perfil solo si existe
            if (userToUpdate.profilePhotoUrl.isNotEmpty()) {
                updatedFields["profilePhotoUrl"] = userToUpdate.profilePhotoUrl
            }

            // Comprobar si el documento existe
            val docRef = firestore.collection(usersCollection).document(userId)
            val docSnapshot = docRef.get().await()

            if (docSnapshot.exists()) {
                // Actualizar documento existente
                docRef.update(updatedFields).await()
            } else {
                // Crear nuevo documento con todos los campos
                val newUser = userToUpdate.copy(
                    createdAt = System.currentTimeMillis(),
                    updatedAt = System.currentTimeMillis()
                )
                docRef.set(newUser).await()
            }

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun updateEmergencyMessage(message: String): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val userId = auth.currentUser?.uid ?: return@withContext Result.failure(Exception("Usuario no autenticado"))

            firestore.collection(usersCollection)
                .document(userId)
                .update("emergencyMessage", message)
                .await()

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}