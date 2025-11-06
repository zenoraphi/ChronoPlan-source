package com.chronoplan.ui.note

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.chronoplan.data.model.NoteDto
import com.chronoplan.domain.usecase.ChronoUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * NoteViewModel - mengatur semua catatan user dari Firestore.
 */
data class NoteUiState(
    val notes: List<NoteDto> = emptyList(),
    val isLoading: Boolean = true,
    val errorMessage: String? = null
)

class NoteViewModel(private val useCase: ChronoUseCase) : ViewModel() {

    private val _state = MutableStateFlow(NoteUiState())
    val state: StateFlow<NoteUiState> = _state.asStateFlow()

    init {
        observeNotes()
    }

    /** Observasi realtime dari Firestore */
    private fun observeNotes() {
        viewModelScope.launch {
            useCase.observeNotes().collect { list ->
                _state.value = _state.value.copy(notes = list, isLoading = false)
            }
        }
    }

    /** Tambah catatan baru */
    fun addNote(note: NoteDto) {
        viewModelScope.launch {
            useCase.addNote(note)
        }
    }

    /** Update catatan */
    fun updateNote(note: NoteDto) {
        viewModelScope.launch {
            useCase.updateNote(note)
        }
    }

    /** Hapus catatan */
    fun deleteNote(noteId: String) {
        viewModelScope.launch {
            useCase.deleteNote(noteId)
        }
    }
}
