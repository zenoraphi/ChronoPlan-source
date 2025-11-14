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
                "uid" to uid,
                "displayName" to (displayName ?: "Guest"),
                "email" to email,
                "level" to "Newbie",
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

            if (!snapshot.exists()) {
                return Result.failure(Exception("Profil tidak ditemukan"))
            }

            val profile = UserProfileDto(
                uid = snapshot.getString("uid") ?: uid,
                displayName = snapshot.getString("displayName") ?: "Guest",
                email = snapshot.getString("email") ?: "",
                avatarUrl = snapshot.getString("avatarUrl"),
                birthDate = snapshot.getString("birthDate"),
                gender = snapshot.getString("gender"),
                level = snapshot.getString("level") ?: "Newbie",
                createdAt = snapshot.getLong("createdAt") ?: 0L
            )

            Result.success(profile)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun updateProfile(profile: UserProfileDto): Result<Unit> {
        return try {
            val uid = auth.currentUser?.uid ?: return Result.failure(Exception("Belum login"))

            val profileMap = hashMapOf<String, Any>(
                "uid" to profile.uid,
                "displayName" to profile.displayName,
                "email" to profile.email,
                "level" to profile.level,
                "createdAt" to profile.createdAt
            )

            profile.avatarUrl?.let { profileMap["avatarUrl"] = it }
            profile.birthDate?.let { profileMap["birthDate"] = it }
            profile.gender?.let { profileMap["gender"] = it }

            firestore.collection("users").document(uid).set(profileMap).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun uploadAttachment(localUri: Uri, remotePath: String?): Result<String> {
        return try {
            val uid = auth.currentUser?.uid ?: return Result.failure(Exception("Belum login"))

            // ✅ Generate unique filename
            val timestamp = System.currentTimeMillis()
            val fileName = "img_$timestamp.jpg"
            val path = remotePath ?: "user_uploads/$uid/$fileName"

            val ref = storage.reference.child(path)

            // ✅ Upload file dengan await
            val uploadTask = ref.putFile(localUri).await()

            // ✅ Pastikan upload berhasil
            if (uploadTask.task.isSuccessful) {
                // ✅ Get download URL
                val downloadUrl = ref.downloadUrl.await()
                Result.success(downloadUrl.toString())
            } else {
                Result.failure(Exception("Upload gagal"))
            }
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

                val agendas = value?.documents?.mapNotNull { doc ->
                    doc.toObject(AgendaDto::class.java)?.copy(id = doc.id)
                } ?: emptyList()

                trySend(agendas)
            }
        awaitClose { listener.remove() }
    }

    override suspend fun addAgenda(agenda: AgendaDto): Result<String> {
        return try {
            val uid = auth.currentUser?.uid ?: return Result.failure(Exception("Belum login"))

            val agendaMap = hashMapOf<String, Any>(
                "title" to agenda.title,
                "description" to agenda.description,
                "date" to agenda.date,
                "startAt" to agenda.startAt,
                "endAt" to agenda.endAt,
                "status" to agenda.status,
                "isFavorite" to agenda.isFavorite,
                "reminderMinutesBefore" to agenda.reminderMinutesBefore,
                "createdAt" to System.currentTimeMillis(),
                "updatedAt" to System.currentTimeMillis()
            )

            val docRef = firestore.collection("users")
                .document(uid)
                .collection("agendas")
                .add(agendaMap)
                .await()

            Result.success(docRef.id)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun updateAgenda(agenda: AgendaDto): Result<Unit> {
        return try {
            val uid = auth.currentUser?.uid ?: return Result.failure(Exception("Belum login"))

            val agendaMap = hashMapOf<String, Any>(
                "title" to agenda.title,
                "description" to agenda.description,
                "date" to agenda.date,
                "startAt" to agenda.startAt,
                "endAt" to agenda.endAt,
                "status" to agenda.status,
                "isFavorite" to agenda.isFavorite,
                "reminderMinutesBefore" to agenda.reminderMinutesBefore,
                "createdAt" to agenda.createdAt,
                "updatedAt" to System.currentTimeMillis()
            )

            firestore.collection("users")
                .document(uid)
                .collection("agendas")
                .document(agenda.id)
                .set(agendaMap)
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

                val notes = value?.documents?.mapNotNull { doc ->
                    doc.toObject(NoteDto::class.java)?.copy(id = doc.id)
                } ?: emptyList()

                trySend(notes)
            }
        awaitClose { listener.remove() }
    }

    override suspend fun addNote(note: NoteDto): Result<String> {
        return try {
            val uid = auth.currentUser?.uid ?: return Result.failure(Exception("Belum login"))

            val noteMap = hashMapOf<String, Any>(
                "title" to note.title,
                "contentPreview" to note.contentPreview,
                "content" to note.content,
                "labels" to note.labels,
                "attachments" to note.attachments,
                "isFavorite" to note.isFavorite, // ✅ ADD THIS
                "createdAt" to System.currentTimeMillis(),
                "updatedAt" to System.currentTimeMillis()
            )

            val docRef = firestore.collection("users")
                .document(uid)
                .collection("notes")
                .add(noteMap)
                .await()

            Result.success(docRef.id)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // Di fungsi updateNote
    override suspend fun updateNote(note: NoteDto): Result<Unit> {
        return try {
            val uid = auth.currentUser?.uid ?: return Result.failure(Exception("Belum login"))

            val noteMap = hashMapOf<String, Any>(
                "title" to note.title,
                "contentPreview" to note.contentPreview,
                "content" to note.content,
                "labels" to note.labels,
                "attachments" to note.attachments,
                "isFavorite" to note.isFavorite, // ✅ ADD THIS
                "createdAt" to note.createdAt,
                "updatedAt" to System.currentTimeMillis()
            )

            firestore.collection("users")
                .document(uid)
                .collection("notes")
                .document(note.id)
                .set(noteMap)
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