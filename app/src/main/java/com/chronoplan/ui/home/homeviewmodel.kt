package com.chronoplan.ui.home

import androidx.compose.ui.graphics.Color // <-- Import Color
import androidx.lifecycle.ViewModel
import com.chronoplan.R // Import R untuk mengakses drawable
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

// --- DEFINISIKAN DATA CLASS DI SINI ---
// Data class untuk satu 'slice' Pie Chart
data class PieSlice(
    val percentage: Float,
    val color: Color,
    val label: String
)

// Data class untuk semua data yang dibutuhkan UI HomeScreen
data class HomeUiState(
    val tanggal: String = "Selasa, 14 Mei 2025",
    val infoTugas: String = "3 Tugas & 1 Agenda",
    val jadwalHariIni: List<Map<String, Any>> = listOf(
        mapOf("icon" to R.drawable.ic_work, "text" to "09.00 Rapat Project Alpa"),
        mapOf("icon" to R.drawable.ic_school, "text" to "12.00 Mengerjakan Tugas Sekolah"),
        mapOf("icon" to R.drawable.ic_cleaning, "text" to "15.00 Membersihkan Rumah")
        // mapOf("icon" to R.drawable.ic_sports, "text" to "16.00 Olahraga") // Uncomment jika ikon ada
    ),
    val historyNotes: List<String> = listOf(
        "Struk Belanja",
        "Catatan Saya"
    ),
    val tugasTerlambat: Int = 5,
    // Pastikan properti pieChartData ada di sini
    val pieChartData: List<PieSlice> = listOf(
        PieSlice(0.4f, Color(0xFFF44336), "Terlambat"), // Merah
        PieSlice(0.3f, Color(0xFFFFC107), "Tertunda"), // Kuning
        PieSlice(0.3f, Color(0xFF4CAF50), "Selesai")    // Hijau
    ),
    val isLoading: Boolean = false
)
// --- BATAS DATA CLASS ---


// ViewModel-nya
class HomeViewModel : ViewModel() {

    // Data ini bersifat "dummy" untuk sekarang
    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState = _uiState.asStateFlow() // Expose as StateFlow

    // Nanti, kamu akan memanggil data dari Firebase di sini
    init {
        // loadDataFromFirebase()
    }

    private fun loadDataFromFirebase() {
        // Logika untuk mengambil data...
        // Contoh: _uiState.update { currentState ->
        //     currentState.copy(isLoading = false, tanggal = "...", ...)
        // }
    }
}