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
    val isLoading: Boolean = true,
    val errorMessage: String? = null,
    val showAddDialog: Boolean = false,
    val showHistoryDialog: Boolean = false,
    val showDatePicker: Boolean = false, // ✅ Tambah state untuk date picker
    val selectedAgenda: AgendaDto? = null, // ✅ Tambah untuk detail view
    val showDetailDialog: Boolean = false
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
                val calendar = Calendar.getInstance()
                calendar.timeInMillis = _state.value.selectedDate

                val selectedDateStr = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                    .format(calendar.time)

                val dateFormatIndo = SimpleDateFormat("EEEE, dd MMMM yyyy", Locale("id", "ID"))
                val formattedDate = dateFormatIndo.format(calendar.time)

                _state.value = _state.value.copy(
                    agendas = list,
                    jadwalHariIni = list.filter { it.date == selectedDateStr },
                    selectedDateFormatted = formattedDate,
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

    fun addAgenda(newAgenda: AgendaDto, context: Context) {
        viewModelScope.launch {
            val result = useCase.addAgenda(newAgenda)

            if (result.isSuccess && newAgenda.reminderMinutesBefore > 0) {
                val agendaId = result.getOrNull() ?: return@launch
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

    // ✅ NEW: Show/hide date picker
    fun showDatePicker() {
        _state.value = _state.value.copy(showDatePicker = true)
    }

    fun hideDatePicker() {
        _state.value = _state.value.copy(showDatePicker = false)
    }

    // ✅ NEW: Change selected date
    fun changeDate(newDate: Long) {
        _state.value = _state.value.copy(selectedDate = newDate)
        observeAgendas()
    }

    // ✅ NEW: Reset to today when navigating away
    fun resetToToday() {
        _state.value = _state.value.copy(selectedDate = System.currentTimeMillis())
        observeAgendas()
    }

    // ✅ NEW: Show agenda detail
    fun showAgendaDetail(agenda: AgendaDto) {
        _state.value = _state.value.copy(
            selectedAgenda = agenda,
            showDetailDialog = true
        )
    }

    fun hideAgendaDetail() {
        _state.value = _state.value.copy(
            selectedAgenda = null,
            showDetailDialog = false
        )
    }
}