package com.chronoplan.ui.agenda

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.chronoplan.data.model.AgendaDto
import com.chronoplan.domain.usecase.ChronoUseCase
import com.chronoplan.work.ReminderScheduler
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

data class AgendaUiState(
    val agendas: List<AgendaDto> = emptyList(),
    val jadwalHariIni: List<AgendaDto> = emptyList(),
    val selectedDate: Long = System.currentTimeMillis(),
    val selectedDateFormatted: String = "",
    val selectedDayOfWeek: Int = Calendar.getInstance().get(Calendar.DAY_OF_WEEK),
    val isLoading: Boolean = true,
    val errorMessage: String? = null,
    val showAddDialog: Boolean = false,
    val showHistoryDialog: Boolean = false
)

class AgendaViewModel(private val useCase: ChronoUseCase) : ViewModel() {

    private val _state = MutableStateFlow(AgendaUiState())
    val state: StateFlow<AgendaUiState> = _state.asStateFlow()

    init {
        observeAgendas()
    }

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

    fun toggleTaskDone(agendaId: String) {
        viewModelScope.launch {
            val current = _state.value.agendas.find { it.id == agendaId } ?: return@launch
            val updated = current.copy(
                status = if (current.status == "done") "pending" else "done",
                updatedAt = System.currentTimeMillis()
            )
            useCase.updateAgenda(updated)
        }
    }

    // Tambahkan di dalam class AgendaViewModel

    fun addAgenda(newAgenda: AgendaDto, context: Context) {
        viewModelScope.launch {
            val result = useCase.addAgenda(newAgenda)

            if (result.isSuccess && newAgenda.reminderMinutesBefore > 0) {
                val agendaId = result.getOrNull() ?: return@launch

                // Hitung delay
                val reminderTime = newAgenda.startAt - (newAgenda.reminderMinutesBefore * 60 * 1000)
                val now = System.currentTimeMillis()
                val delayMinutes = (reminderTime - now) / (60 * 1000)

                if (delayMinutes > 0) {
                    ReminderScheduler.scheduleReminder(
                        context = context,
                        agendaId = agendaId,
                        title = newAgenda.title,
                        desc = "Dimulai ${newAgenda.reminderMinutesBefore} menit lagi",
                        delayInMinutes = delayMinutes
                    )
                }
            }
        }
    }

    fun deleteAgenda(agendaId: String) {
        viewModelScope.launch {
            useCase.deleteAgenda(agendaId)
        }
    }

    fun showAddDialog() {
        _state.value = _state.value.copy(showAddDialog = true)
    }

    fun hideAddDialog() {
        _state.value = _state.value.copy(showAddDialog = false)
    }

    fun showHistoryDialog() {
        _state.value = _state.value.copy(showHistoryDialog = true)
    }

    fun hideHistoryDialog() {
        _state.value = _state.value.copy(showHistoryDialog = false)
    }

    fun changeDate(newDate: Long) {
        _state.value = _state.value.copy(selectedDate = newDate)
        observeAgendas()
    }
}