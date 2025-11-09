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

            // Combine agendas dan notes jadi satu flow
            combine(
                useCase.observeAgendas(),
                useCase.observeNotes()
            ) { agendas, notes ->
                Pair(agendas, notes)
            }.collect { (agendas, notes) ->
                val todayAgendas = agendas.filter { it.date == today }

                val done = todayAgendas.count { it.status == "done" }
                val pending = todayAgendas.count { it.status == "pending" }
                val missed = agendas.count {
                    it.status != "done" &&
                            try {
                                SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                                    .parse(it.date)?.before(Date()) == true
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

                // Pie Chart
                val total = todayAgendas.size.toFloat()
                val pieData = if (total > 0) {
                    listOf(
                        PieSlice(done / total, Color(0xFF4CAF50), "Selesai"),
                        PieSlice(pending / total, Color(0xFFFFC107), "Tertunda"),
                        PieSlice(missed.toFloat() / total.coerceAtLeast(1f), Color(0xFFF44336), "Terlambat")
                    ).filter { it.percentage > 0 }
                } else {
                    listOf(PieSlice(1f, Color.LightGray, "Belum ada"))
                }

                val notesList = notes.take(3).map { it.title }

                _uiState.value = _uiState.value.copy(
                    infoTugas = "${todayAgendas.size} Tugas Hari Ini",
                    jadwalHariIni = jadwalList,
                    historyNotes = notesList,
                    tugasTerlambat = missed,
                    pieChartData = pieData,
                    isLoading = false
                )
            }
        }
    }
}