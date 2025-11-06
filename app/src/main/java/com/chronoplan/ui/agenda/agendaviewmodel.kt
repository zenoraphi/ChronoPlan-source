package com.chronoplan.ui.agenda

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.chronoplan.data.model.AgendaDto
import com.chronoplan.domain.usecase.ChronoUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

/**
 * AgendaViewModel - mengatur data agenda user dari Firestore secara real-time.
 */
data class AgendaUiState(
    val agendas: List<AgendaDto> = emptyList(),
    val jadwalHariIni: List<AgendaDto> = emptyList(),
    val selectedDate: Long = System.currentTimeMillis(),
    val selectedDateFormatted: String = "",
    val selectedDayOfWeek: Int = Calendar.getInstance().get(Calendar.DAY_OF_WEEK),
    val isLoading: Boolean = true,
    val errorMessage: String? = null
)

class AgendaViewModel(private val useCase: ChronoUseCase) : ViewModel() {

    private val _state = MutableStateFlow(AgendaUiState())
    val state: StateFlow<AgendaUiState> = _state.asStateFlow()

    init {
        observeAgendas()
    }

    /** Observasi agenda dari Firestore realtime */
    private fun observeAgendas() {
        viewModelScope.launch {
            useCase.observeAgendas().collect { list ->
                val today = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                    .format(Date(_state.value.selectedDate))

                _state.value = _state.value.copy(
                    agendas = list,
                    jadwalHariIni = list.filter { it.date == today },
                    selectedDateFormatted = today,
                    isLoading = false
                )
            }
        }
    }

    /** Menandai agenda selesai */
    fun toggleTaskDone(agendaId: String) {
        viewModelScope.launch {
            val current = _state.value.agendas.find { it.id == agendaId } ?: return@launch
            val updated = current.copy(
                status = if (current.status == "done") "pending" else "done"
            )
            useCase.updateAgenda(updated)
        }
    }

    /** Tambah agenda baru */
    fun addAgenda(newAgenda: AgendaDto) {
        viewModelScope.launch {
            useCase.addAgenda(newAgenda)
        }
    }

    /** Hapus agenda */
    fun deleteAgenda(agendaId: String) {
        viewModelScope.launch {
            useCase.deleteAgenda(agendaId)
        }
    }
}
