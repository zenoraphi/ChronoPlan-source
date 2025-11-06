package com.chronoplan.data.repository

import android.net.Uri
import com.chronoplan.data.model.AgendaDto
import com.chronoplan.data.model.NoteDto
import com.chronoplan.data.model.UserProfileDto
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

class FirestoreChronoRepository(
    private val firestore: FirebaseFirestore,
    private val storage: FirebaseStorage,
    private val auth: FirebaseAuth
) : ChronoRepository {

    // 🔹 AUTH --------------------------------------------------------
    override suspend fun signInWithEmail(email: String, password: String): Result<Unit> {
        return try {
            auth.signInWithEmailAndPassword(email, password).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun signUpWithEmail(
        email: String,
        password: String,
        displayName: String?
    ): Result<Unit> {
        return try {
            val userResult = auth.createUserWithEmailAndPassword(email, password).await()
            val uid = userResult.user?.uid ?: return Result.failure(Exception("UID null"))

            val profile = hashMapOf(
                "displayName" to (displayName ?: ""),
                "email" to email,
                "createdAt" to System.currentTimeMillis()
            )

            firestore.collection("users").document(uid).set(profile).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun registerUser(email: String, password: String, displayName: String): Result<Unit> {
        return signUpWithEmail(email, password, displayName)
    }

    override fun signOut() {
        auth.signOut()
    }

    override fun currentUserId(): String? = auth.currentUser?.uid


    // 🔹 PROFILE -----------------------------------------------------
    override suspend fun getProfile(): Result<UserProfileDto> {
        return try {
            val uid = auth.currentUser?.uid ?: return Result.failure(Exception("Belum login"))
            val snapshot = firestore.collection("users").document(uid).get().await()
            val profile = snapshot.toObject(UserProfileDto::class.java)
                ?: return Result.failure(Exception("Profil tidak ditemukan"))
            Result.success(profile)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun updateProfile(profile: UserProfileDto): Result<Unit> {
        return try {
            val uid = auth.currentUser?.uid ?: return Result.failure(Exception("Belum login"))
            firestore.collection("users").document(uid).set(profile).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun uploadAttachment(localUri: Uri, remotePath: String?): Result<String> {
        return try {
            val uid = auth.currentUser?.uid ?: return Result.failure(Exception("Belum login"))
            val path = remotePath ?: "uploads/$uid/${System.currentTimeMillis()}"
            val ref = storage.reference.child(path)
            ref.putFile(localUri).await()
            val url = ref.downloadUrl.await().toString()
            Result.success(url)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // 🔹 AGENDA ------------------------------------------------------
    override fun observeAgendas(): Flow<List<AgendaDto>> = callbackFlow {
        val uid = auth.currentUser?.uid
        if (uid == null) {
            trySend(emptyList())
            close()
            return@callbackFlow
        }
        val listener = firestore.collection("users")
            .document(uid)
            .collection("agendas")
            .addSnapshotListener { value, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                val agendas = value?.toObjects(AgendaDto::class.java) ?: emptyList()
                trySend(agendas)
            }
        awaitClose { listener.remove() }
    }

    override suspend fun addAgenda(agenda: AgendaDto): Result<String> {
        return try {
            val uid = auth.currentUser?.uid ?: return Result.failure(Exception("Belum login"))
            val docRef = firestore.collection("users")
                .document(uid)
                .collection("agendas")
                .add(agenda)
                .await()
            Result.success(docRef.id)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun updateAgenda(agenda: AgendaDto): Result<Unit> {
        return try {
            val uid = auth.currentUser?.uid ?: return Result.failure(Exception("Belum login"))
            firestore.collection("users")
                .document(uid)
                .collection("agendas")
                .document(agenda.id)
                .set(agenda)
                .await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun deleteAgenda(agendaId: String): Result<Unit> {
        return try {
            val uid = auth.currentUser?.uid ?: return Result.failure(Exception("Belum login"))
            firestore.collection("users")
                .document(uid)
                .collection("agendas")
                .document(agendaId)
                .delete()
                .await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // 🔹 NOTES -------------------------------------------------------
    override fun observeNotes(): Flow<List<NoteDto>> = callbackFlow {
        val uid = auth.currentUser?.uid
        if (uid == null) {
            trySend(emptyList())
            close()
            return@callbackFlow
        }
        val listener = firestore.collection("users")
            .document(uid)
            .collection("notes")
            .addSnapshotListener { value, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                val notes = value?.toObjects(NoteDto::class.java) ?: emptyList()
                trySend(notes)
            }
        awaitClose { listener.remove() }
    }

    override suspend fun addNote(note: NoteDto): Result<String> {
        return try {
            val uid = auth.currentUser?.uid ?: return Result.failure(Exception("Belum login"))
            val docRef = firestore.collection("users")
                .document(uid)
                .collection("notes")
                .add(note)
                .await()
            Result.success(docRef.id)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun updateNote(note: NoteDto): Result<Unit> {
        return try {
            val uid = auth.currentUser?.uid ?: return Result.failure(Exception("Belum login"))
            firestore.collection("users")
                .document(uid)
                .collection("notes")
                .document(note.id)
                .set(note)
                .await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun deleteNote(noteId: String): Result<Unit> {
        return try {
            val uid = auth.currentUser?.uid ?: return Result.failure(Exception("Belum login"))
            firestore.collection("users")
                .document(uid)
                .collection("notes")
                .document(noteId)
                .delete()
                .await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
