package com.chronoplan.ui.home

// --- IMPORTS ---
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState // Import jika ingin scroll
// import androidx.compose.foundation.verticalScroll // Uncomment jika ingin scroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer // Import untuk rotate modern
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.chronoplan.R
import com.chronoplan.di.AppViewModelFactory
import com.chronoplan.ui.components.AnimatedCard
// --- IMPOR KOMPONEN DARI PACKAGE LAIN ---
import com.chronoplan.ui.components.DashboardCard
import com.chronoplan.ui.components.ScheduleItem
import com.chronoplan.ui.components.PieChart
import com.chronoplan.ui.components.PieChartLegend
// -----------------------------------------

// --- KOMPONEN UTAMA: Home Screen (TANPA SCAFFOLD & KOMPONEN DUPLIKAT) ---

@Composable
fun HomeScreen(
    modifier: Modifier = Modifier,
    viewModel: HomeViewModel = viewModel(factory = AppViewModelFactory())
) {
    // Ambil state dari ViewModel
    val uiState by viewModel.uiState.collectAsState()

    // Box terluar untuk background dan border
    Box(
        modifier = modifier // Langsung gunakan modifier dari parameter NavHost
            .fillMaxSize() // Pastikan mengisi area NavHost
            .background(Color(0xFFF0F5FF)) // Background biru muda
    ) {
        // Image Border Atas (nempel pojok kanan atas Box ini)
        Image(
            painter = painterResource(id = R.drawable.ic_border),
            contentDescription = "Top Border",
            modifier = Modifier
                .width(200.dp)
                .height(150.dp)
                .align(Alignment.TopEnd), // Nempel di pojok
            contentScale = ContentScale.FillBounds
        )
        // Image Border Bawah (nempel pojok kiri bawah Box ini)
        Image(
            painter = painterResource(id = R.drawable.ic_border),
            contentDescription = "Bottom Border",
            modifier = Modifier
                .width(200.dp)
                .height(150.dp)
                .align(Alignment.BottomStart) // Nempel di pojok
                .graphicsLayer(rotationZ = 180f), // Cara rotate modern
            contentScale = ContentScale.FillBounds
        )


        // Column untuk konten utama (di atas border)
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp) // Padding HANYA untuk konten
            // .verticalScroll(rememberScrollState()), // Hapus/komen untuk non-scroll
            ,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Header
            Spacer(modifier = Modifier.height(40.dp))
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Image(
                    painter = painterResource(id = R.drawable.ic_chronoplan_logo),
                    contentDescription = "Logo",
                    modifier = Modifier.size(120.dp)
                )
                Text(
                    text = "CHRONOPLAN",
                    fontSize = 32.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "${uiState.tanggal} • ${uiState.infoTugas}",
                fontSize = 14.sp,
                color = Color.Gray
            )
            Spacer(modifier = Modifier.height(24.dp))

            // Kartu Jadwal
            ScheduleCard( // Ini Composable private di bawah
                jadwalList = uiState.jadwalHariIni,
                pieData = uiState.pieChartData
            )
            Spacer(modifier = Modifier.height(16.dp))

            // Kartu History & Favorit
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(IntrinsicSize.Min),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    HistoryNotesCard( // Ini Composable private di bawah
                        notes = uiState.historyNotes,
                        modifier = Modifier.fillMaxHeight()
                    )
                }
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    LateTaskCard(taskCount = uiState.tugasTerlambat) // Ini Composable private di bawah
                    FavoritesCard() // Ini Composable private di bawah
                }
            }
            Spacer(modifier = Modifier.height(24.dp)) // Spacer di akhir konten
        } // Akhir Column konten utama
    } // Akhir Box terluar
}

// --- Composable KECIL (PRIVATE) KHUSUS UNTUK HomeScreen ---
// (Fungsi-fungsi ini MEMANGGIL komponen dari ui.components)


@Composable
private fun ScheduleCard(jadwalList: List<Map<String, Any>>, pieData: List<PieSlice>) {
    AnimatedCard { // Ganti dari DashboardCard
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = "Jadwal Hari ini",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
                if (jadwalList.isEmpty()) {
                    Text(text = "Tidak ada jadwal hari ini.", color = Color.Gray)
                } else {
                    jadwalList.take(4).forEach { jadwal ->
                        val iconRes = jadwal["icon"] as? Int ?: R.drawable.ic_work
                        val text = jadwal["text"] as? String ?: "Jadwal tidak valid"
                        // Gunakan ScheduleItem dari components
                        ScheduleItem(
                            iconRes = iconRes,
                            text = text
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                // Gunakan PieChart dari components
                PieChart(slices = pieData)
                Spacer(modifier = Modifier.height(8.dp))
                // Gunakan PieChartLegend dari components
                PieChartLegend(slices = pieData)
            }
        }
    }
}

@Composable
private fun HistoryNotesCard(notes: List<String>, modifier: Modifier = Modifier) {
    // Gunakan DashboardCard dari components
    DashboardCard(modifier = modifier.defaultMinSize(minHeight = 150.dp)) {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(
                text = "History Notes",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )
            if (notes.isEmpty()) {
                Text(text = "Belum ada catatan.", color = Color.Gray)
            } else {
                notes.take(3).forEach { note ->
                    Text(text = "• $note")
                }
            }
        }
    }
}

@Composable
private fun LateTaskCard(taskCount: Int) {
    // Gunakan DashboardCard dari components
    DashboardCard(modifier = Modifier.height(100.dp)) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Icon(
                painter = painterResource(id = R.drawable.ic_late),
                contentDescription = "Tugas Terlambat",
                modifier = Modifier.size(32.dp),
                tint = if (taskCount > 0) Color.Red else Color.Gray
            )
            Text(
                text = "• $taskCount Tugas Terlambat",
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

@Composable
private fun FavoritesCard() {
    // Gunakan DashboardCard dari components
    DashboardCard(modifier = Modifier.height(100.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "Favorit",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )
            Icon(
                painter = painterResource(id = R.drawable.ic_star),
                contentDescription = "Favorit",
                modifier = Modifier.size(32.dp),
                tint = Color(0xFFFFC107)
            )
        }
    }
}

// --- FUNGSI DashboardCard, ScheduleItem, PieChart, PieChartLegend SUDAH DIHAPUS DARI SINI ---