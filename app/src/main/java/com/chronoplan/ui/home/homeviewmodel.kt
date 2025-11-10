package com.chronoplan.ui.home

import androidx.compose.ui.graphics.Color
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.chronoplan.R
import com.chronoplan.domain.usecase.ChronoUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

data class PieSlice(
    val percentage: Float,
    val color: Color,
    val label: String
)

data class HomeUiState(
    val tanggal: String = "",
    val infoTugas: String = "Memuat...",
    val jadwalHariIni: List<Map<String, Any>> = emptyList(),
    val historyNotes: List<String> = emptyList(),
    val tugasTerlambat: Int = 0,
    val pieChartData: List<PieSlice> = listOf(
        PieSlice(1f, Color.LightGray, "Belum ada data")
    ),
    val isLoading: Boolean = true
)

class HomeViewModel(
    private val useCase: ChronoUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState = _uiState.asStateFlow()

    // ✅ State untuk data lengkap
    private var allAgendas: List<com.chronoplan.data.model.AgendaDto> = emptyList()
    private var allNotes: List<com.chronoplan.data.model.NoteDto> = emptyList()

    init {
        loadData()
    }

    private fun loadData() {
        viewModelScope.launch {
            val dateFormat = SimpleDateFormat("EEEE, dd MMMM yyyy", Locale("id", "ID"))
            val today = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())

            _uiState.value = _uiState.value.copy(
                tanggal = dateFormat.format(Date()),
                isLoading = true
            )

            combine(
                useCase.observeAgendas(),
                useCase.observeNotes()
            ) { agendas, notes ->
                Pair(agendas, notes)
            }.collect { (agendas, notes) ->
                allAgendas = agendas
                allNotes = notes

                // ✅ FILTER AGENDA HARI INI SAJA
                val todayAgendas = agendas.filter { it.date == today }

                // ✅ HITUNG STATUS HARI INI
                val done = todayAgendas.count { it.status == "done" }
                val pending = todayAgendas.count { it.status == "pending" }
                val missed = todayAgendas.count { it.status == "missed" }

                // ✅ TUGAS TERLAMBAT (agenda dengan tanggal < hari ini DAN belum selesai)
                val lateCount = agendas.count { agenda ->
                    agenda.status != "done" && agenda.date < today
                }

                // ✅ JADWAL LIST (max 4)
                val jadwalList = todayAgendas.take(4).map { agenda ->
                    mapOf(
                        "icon" to R.drawable.ic_work,
                        "text" to agenda.title
                    )
                }

                // ✅ PIE CHART HANYA DARI AGENDA HARI INI
                val total = todayAgendas.size.toFloat()
                val pieData = if (total > 0) {
                    listOf(
                        PieSlice(done / total, Color(0xFF4CAF50), "Selesai"),
                        PieSlice(pending / total, Color(0xFFFFC107), "Tertunda"),
                        PieSlice(missed / total, Color(0xFFF44336), "Terlewat")
                    ).filter { it.percentage > 0 }
                } else {
                    listOf(PieSlice(1f, Color(0xFFE0E0E0), "Belum ada"))
                }

                // ✅ HISTORY NOTES (max 3 terbaru)
                val notesList = notes
                    .sortedByDescending { it.updatedAt }
                    .take(3)
                    .map { it.title }

                _uiState.value = _uiState.value.copy(
                    infoTugas = "${todayAgendas.size} Tugas Hari Ini",
                    jadwalHariIni = jadwalList,
                    historyNotes = notesList,
                    tugasTerlambat = lateCount,
                    pieChartData = pieData,
                    isLoading = false
                )
            }
        }
    }

    // ✅ GET LATE TASKS (dengan tanggal < hari ini)
    fun getLateTasks(): List<com.chronoplan.data.model.AgendaDto> {
        val today = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
        return allAgendas.filter { it.status != "done" && it.date < today }
            .sortedBy { it.date }
    }

    // ✅ GET FAVORITE NOTES
    fun getFavoriteNotes(): List<com.chronoplan.data.model.NoteDto> {
        return allNotes.filter { it.isFavorite }
            .sortedByDescending { it.updatedAt }
    }

    // ✅ GET ALL NOTES
    fun getAllNotes(): List<com.chronoplan.data.model.NoteDto> {
        return allNotes.sortedByDescending { it.updatedAt }
    }

    fun deleteAgenda(id: String) {
        viewModelScope.launch {
            useCase.deleteAgenda(id)
        }
    }

    fun deleteNote(id: String) {
        viewModelScope.launch {
            useCase.deleteNote(id)
        }
    }

    fun toggleFavorite(noteId: String) {
        viewModelScope.launch {
            val note = allNotes.find { it.id == noteId } ?: return@launch
            val updated = note.copy(isFavorite = !note.isFavorite)
            useCase.updateNote(updated)
        }
    }
}