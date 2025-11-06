package com.chronoplan.domain.usecase

import android.net.Uri
import com.chronoplan.data.model.AgendaDto
import com.chronoplan.data.model.NoteDto
import com.chronoplan.data.model.UserProfileDto
import com.chronoplan.data.repository.ChronoRepository
import kotlinx.coroutines.flow.Flow

class ChronoUseCase(private val repo: ChronoRepository) {

    // 🔹 AUTH
    suspend fun signInWithEmail(email: String, password: String): Result<Unit> =
        repo.signInWithEmail(email, password)

    // 🔹 REGISTER
    suspend fun registerUser(email: String, password: String, displayName: String): Result<Unit> =
        repo.registerUser(email, password, displayName)


    suspend fun signUpWithEmail(email: String, password: String, displayName: String? = null): Result<Unit> =
        repo.signUpWithEmail(email, password, displayName)

    fun signOut() = repo.signOut()

    // 🔹 USER
    suspend fun getProfile(): Result<UserProfileDto> = repo.getProfile()
    suspend fun updateProfile(profile: UserProfileDto): Result<Unit> = repo.updateProfile(profile)
    suspend fun uploadAttachment(localUri: Uri, remotePath: String? = null): Result<String> =
        repo.uploadAttachment(localUri, remotePath)

    // 🔹 AGENDA
    fun observeAgendas(): Flow<List<AgendaDto>> = repo.observeAgendas()
    suspend fun addAgenda(agenda: AgendaDto): Result<String> = repo.addAgenda(agenda)
    suspend fun updateAgenda(agenda: AgendaDto): Result<Unit> = repo.updateAgenda(agenda)
    suspend fun deleteAgenda(id: String): Result<Unit> = repo.deleteAgenda(id)

    // 🔹 NOTES
    fun observeNotes(): Flow<List<NoteDto>> = repo.observeNotes()
    suspend fun addNote(note: NoteDto): Result<String> = repo.addNote(note)
    suspend fun updateNote(note: NoteDto): Result<Unit> = repo.updateNote(note)
    suspend fun deleteNote(id: String): Result<Unit> = repo.deleteNote(id)
}
