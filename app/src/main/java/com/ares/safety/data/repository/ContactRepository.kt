// Archivo: com/ares/safety/data/repository/ContactRepository.kt

package com.ares.safety.data.repository

import com.ares.safety.data.model.EmergencyContact
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

class ContactRepository {
    private val auth = FirebaseAuth.getInstance()
    private val firestore = FirebaseFirestore.getInstance()
    private val contactsCollection = "emergencyContacts"

    // Obtener todos los contactos de emergencia del usuario actual
    suspend fun getEmergencyContacts(): Result<List<EmergencyContact>> = withContext(Dispatchers.IO) {
        try {
            val userId = auth.currentUser?.uid ?: return@withContext Result.failure(Exception("No hay usuario autenticado"))

            val snapshot = firestore.collection(contactsCollection)
                .whereEqualTo("userId", userId)
                .orderBy("priority")
                .get()
                .await()

            val contacts = snapshot.toObjects(EmergencyContact::class.java)
            Result.success(contacts)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // Agregar un nuevo contacto de emergencia
    suspend fun addEmergencyContact(contact: EmergencyContact): Result<EmergencyContact> = withContext(Dispatchers.IO) {
        try {
            val userId = auth.currentUser?.uid ?: return@withContext Result.failure(Exception("No hay usuario autenticado"))

            // Asignar el ID del usuario actual
            val newContact = contact.copy(
                userId = userId,
                createdAt = System.currentTimeMillis(),
                updatedAt = System.currentTimeMillis()
            )

            // Crear un nuevo documento con ID generado automáticamente
            val docRef = firestore.collection(contactsCollection).document()
            val contactWithId = newContact.copy(id = docRef.id)

            docRef.set(contactWithId).await()

            Result.success(contactWithId)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // Actualizar un contacto existente
    suspend fun updateEmergencyContact(contact: EmergencyContact): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val updatedContact = contact.copy(
                updatedAt = System.currentTimeMillis()
            )

            firestore.collection(contactsCollection)
                .document(contact.id)
                .set(updatedContact)
                .await()

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // Eliminar un contacto
    suspend fun deleteEmergencyContact(contactId: String): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            firestore.collection(contactsCollection)
                .document(contactId)
                .delete()
                .await()

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getContactsOrderedByPriority(): Result<List<EmergencyContact>> {
        val userId = auth.currentUser?.uid ?: return Result.failure(Exception("Usuario no autenticado"))

        return try {
            val snapshot = firestore.collection("users")
                .document(userId)
                .collection("contacts")
                .orderBy("priority")
                .get()
                .await()

            val contacts = snapshot.toObjects(EmergencyContact::class.java)
            Result.success(contacts)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}