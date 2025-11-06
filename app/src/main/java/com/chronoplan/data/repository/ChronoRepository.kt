package com.chronoplan.data.repository

import android.net.Uri
import com.chronoplan.data.model.AgendaDto
import com.chronoplan.data.model.NoteDto
import com.chronoplan.data.model.UserProfileDto
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.tasks.await

interface ChronoRepository {

    // 🔹 AUTH
    suspend fun signInWithEmail(email: String, password: String): Result<Unit>
    suspend fun signUpWithEmail(email: String, password: String, displayName: String? = null): Result<Unit>
    fun signOut()

    suspend fun registerUser(email: String, password: String, displayName: String): Result<Unit> {
        return try {
            val auth = FirebaseAuth.getInstance()
            val userResult = auth.createUserWithEmailAndPassword(email, password).await()
            val userId = userResult.user?.uid ?: return Result.failure(Exception("UID null"))

            val profile = hashMapOf(
                "displayName" to displayName,
                "email" to email,
                "createdAt" to System.currentTimeMillis()
            )

            FirebaseFirestore.getInstance()
                .collection("users")
                .document(userId)
                .set(profile)
                .await()

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }


    // 🔹 PROFILE
    suspend fun getProfile(): Result<UserProfileDto>
    suspend fun updateProfile(profile: UserProfileDto): Result<Unit>
    suspend fun uploadAttachment(localUri: Uri, remotePath: String? = null): Result<String>
    fun currentUserId(): String?

    // 🔹 AGENDA
    fun observeAgendas(): Flow<List<AgendaDto>>
    suspend fun addAgenda(agenda: AgendaDto): Result<String>
    suspend fun updateAgenda(agenda: AgendaDto): Result<Unit>
    suspend fun deleteAgenda(agendaId: String): Result<Unit>

    // 🔹 NOTE
    fun observeNotes(): Flow<List<NoteDto>>
    suspend fun addNote(note: NoteDto): Result<String>
    suspend fun updateNote(note: NoteDto): Result<Unit>
    suspend fun deleteNote(noteId: String): Result<Unit>
}
