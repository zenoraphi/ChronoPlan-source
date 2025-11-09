package com.chronoplan.ui.note

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.chronoplan.data.model.NoteDto
import com.chronoplan.domain.usecase.ChronoUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class NoteUiState(
    val notes: List<NoteDto> = emptyList(),
    val favoriteNotes: List<NoteDto> = emptyList(),
    val isLoading: Boolean = true,
    val errorMessage: String? = null,
    val showDetailDialog: Boolean = false,
    val selectedNote: NoteDto? = null,
    val navigateToEditor: Boolean = false,
    val noteToEdit: NoteDto? = null
)

class NoteViewModel(private val useCase: ChronoUseCase) : ViewModel() {

    private val _state = MutableStateFlow(NoteUiState())
    val state: StateFlow<NoteUiState> = _state.asStateFlow()

    init {
        observeNotes()
    }

    private fun observeNotes() {
        viewModelScope.launch {
            useCase.observeNotes().collect { list ->
                _state.value = _state.value.copy(
                    notes = list.sortedByDescending { it.updatedAt },
                    favoriteNotes = list.filter { it.isFavorite },
                    isLoading = false
                )
            }
        }
    }

    fun addNote(note: NoteDto) {
        viewModelScope.launch {
            useCase.addNote(note)
        }
    }

    fun updateNote(note: NoteDto) {
        viewModelScope.launch {
            useCase.updateNote(note)
        }
    }

    fun deleteNote(noteId: String) {
        viewModelScope.launch {
            useCase.deleteNote(noteId)
        }
    }

    fun toggleFavorite(noteId: String) {
        viewModelScope.launch {
            val note = _state.value.notes.find { it.id == noteId } ?: return@launch
            val updated = note.copy(
                isFavorite = !note.isFavorite,
                updatedAt = System.currentTimeMillis()
            )
            useCase.updateNote(updated)
        }
    }

    fun uploadAttachment(uri: Uri) {
        viewModelScope.launch {
            val result = useCase.uploadAttachment(uri)
            if (result.isSuccess) {
                // Handle uploaded URL
                val url = result.getOrNull()
                // You can add this to the note being edited
            }
        }
    }

    fun showAddDialog() {
        _state.value = _state.value.copy(showAddDialog = true, isEditMode = false, selectedNote = null)
    }

    fun showEditDialog(note: NoteDto) {
        _state.value = _state.value.copy(showAddDialog = true, isEditMode = true, selectedNote = note)
    }

    fun hideAddDialog() {
        _state.value = _state.value.copy(showAddDialog = false, isEditMode = false, selectedNote = null)
    }

    fun showDetailDialog(note: NoteDto) {
        _state.value = _state.value.copy(showDetailDialog = true, selectedNote = note)
    }

    fun hideDetailDialog() {
        _state.value = _state.value.copy(showDetailDialog = false, selectedNote = null)
    }
}