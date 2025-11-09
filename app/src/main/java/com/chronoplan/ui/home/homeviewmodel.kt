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
    val historyNotes: List<String> = emptyList(), // ✅ Akan diisi dengan judul note
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

    init {
        loadData()
    }

    // ✅ State untuk data lengkap (bukan hanya preview)
    private var allAgendas: List<com.chronoplan.data.model.AgendaDto> = emptyList()
    private var allNotes: List<com.chronoplan.data.model.NoteDto> = emptyList()

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
                // ✅ Simpan data lengkap
                allAgendas = agendas
                allNotes = notes

                val todayAgendas = agendas.filter { it.date == today }

                val done = todayAgendas.count { it.status == "done" }
                val pending = todayAgendas.count { it.status == "pending" }
                val missed = todayAgendas.count { it.status == "missed" }

                val lateCount = agendas.count { agenda ->
                    agenda.status != "done" &&
                            try {
                                val agendaDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).parse(agenda.date)
                                val currentDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).parse(today)
                                agendaDate?.before(currentDate) == true
                            } catch (e: Exception) {
                                false
                            }
                }

                val jadwalList = todayAgendas.take(4).map { agenda ->
                    mapOf(
                        "icon" to R.drawable.ic_work,
                        "text" to agenda.title
                    )
                }

                val total = todayAgendas.size.toFloat()
                val pieData = if (total > 0) {
                    listOf(
                        PieSlice(done / total, Color(0xFF4CAF50), "Selesai"),
                        PieSlice(pending / total, Color(0xFFFFC107), "Tertunda"),
                        PieSlice(missed / total, Color(0xFFF44336), "Terlewat")
                    ).filter { it.percentage > 0 }
                } else {
                    listOf(PieSlice(1f, Color.LightGray, "Belum ada"))
                }

                val notesList = notes.sortedByDescending { it.updatedAt }.take(3).map { it.title }

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

    // ✅ Fungsi untuk dialog
    fun getLateTasks(): List<com.chronoplan.data.model.AgendaDto> {
        val today = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
        return allAgendas.filter { agenda ->
            agenda.status != "done" &&
                    try {
                        val agendaDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).parse(agenda.date)
                        val currentDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).parse(today)
                        agendaDate?.before(currentDate) == true
                    } catch (e: Exception) {
                        false
                    }
        }
    }

    fun getFavoriteNotes(): List<com.chronoplan.data.model.NoteDto> {
        return allNotes.filter { it.isFavorite }
    }

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