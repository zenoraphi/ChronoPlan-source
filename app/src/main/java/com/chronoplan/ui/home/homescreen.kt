package com.chronoplan.ui.home

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.viewmodel.compose.viewModel
import com.chronoplan.R
import com.chronoplan.data.model.AgendaDto
import com.chronoplan.data.model.NoteDto
import com.chronoplan.di.AppViewModelFactory
import com.chronoplan.ui.components.*
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun HomeScreen(
    modifier: Modifier = Modifier,
    viewModel: HomeViewModel = viewModel(factory = AppViewModelFactory())
) {
    val uiState by viewModel.uiState.collectAsState()
    var showLateTasksDialog by remember { mutableStateOf(false) }
    var showFavoritesDialog by remember { mutableStateOf(false) }
    var showHistoryNotesDialog by remember { mutableStateOf(false) }

    // ✅ Dialogs
    if (showLateTasksDialog) {
        LateTasksDialog(
            lateTasks = viewModel.getLateTasks(),
            onDismiss = { showLateTasksDialog = false },
            onDeleteTask = { viewModel.deleteAgenda(it) }
        )
    }

    if (showFavoritesDialog) {
        FavoriteNotesDialog(
            favoriteNotes = viewModel.getFavoriteNotes(),
            onDismiss = { showFavoritesDialog = false },
            onRemoveFavorite = { viewModel.toggleFavorite(it) },
            onDeleteNote = { viewModel.deleteNote(it) }
        )
    }

    if (showHistoryNotesDialog) {
        HistoryNotesDialog(
            notes = viewModel.getAllNotes(),
            onDismiss = { showHistoryNotesDialog = false },
            onNoteClick = { /* Navigate to note detail */ }
        )
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFFF0F5FF))
    ) {
        // Border atas
        Image(
            painter = painterResource(id = R.drawable.ic_border),
            contentDescription = "Top Border",
            modifier = Modifier
                .width(200.dp)
                .height(150.dp)
                .align(Alignment.TopEnd),
            contentScale = ContentScale.FillBounds
        )

        // Border bawah
        Image(
            painter = painterResource(id = R.drawable.ic_border),
            contentDescription = "Bottom Border",
            modifier = Modifier
                .width(200.dp)
                .height(150.dp)
                .align(Alignment.BottomStart)
                .graphicsLayer(rotationZ = 180f),
            contentScale = ContentScale.FillBounds
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Header
            Spacer(modifier = Modifier.height(40.dp))
            PulseAnimation {
                Image(
                    painter = painterResource(id = R.drawable.ic_chronoplan_logo),
                    contentDescription = "Logo",
                    modifier = Modifier.size(120.dp)
                )
            }
            Text(
                text = "CHRONOPLAN",
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF000000),
                modifier = Modifier.padding(top = 8.dp)
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "${uiState.tanggal} • ${uiState.infoTugas}",
                fontSize = 14.sp,
                color = Color(0xFF424242)
            )
            Spacer(modifier = Modifier.height(24.dp))

            // Kartu Jadwal dengan animasi
            AnimatedCard {
                ScheduleCard(
                    jadwalList = uiState.jadwalHariIni,
                    pieData = uiState.pieChartData
                )
            }
            Spacer(modifier = Modifier.height(16.dp))

            // Row History & Favorit
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
                    ScaleInAnimation {
                        HistoryNotesCard(
                            notes = uiState.historyNotes,
                            onClick = { showHistoryNotesDialog = true },
                            modifier = Modifier.fillMaxHeight()
                        )
                    }
                }
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    ScaleInAnimation {
                        LateTaskCard(
                            taskCount = uiState.tugasTerlambat,
                            onClick = { showLateTasksDialog = true }
                        )
                    }
                    ScaleInAnimation {
                        FavoritesCard(onClick = { showFavoritesDialog = true })
                    }
                }
            }
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

// ✅ Components dengan onClick
@Composable
private fun ScheduleCard(jadwalList: List<Map<String, Any>>, pieData: List<PieSlice>) {
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
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            if (jadwalList.isEmpty()) {
                Text(
                    text = "Tidak ada jadwal hari ini.",
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            } else {
                jadwalList.take(4).forEach { jadwal ->
                    val iconRes = jadwal["icon"] as? Int ?: R.drawable.ic_work
                    val text = jadwal["text"] as? String ?: "Jadwal tidak valid"
                    ScheduleItem(iconRes = iconRes, text = text)
                }
            }
        }
        Spacer(modifier = Modifier.width(16.dp))
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            PieChart(slices = pieData)
            Spacer(modifier = Modifier.height(8.dp))
            PieChartLegend(slices = pieData)
        }
    }
}

@Composable
private fun HistoryNotesCard(
    notes: List<String>,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    DashboardCard(
        modifier = modifier
            .defaultMinSize(minHeight = 150.dp)
            .clickable(onClick = onClick)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "History Notes",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Icon(
                    imageVector = Icons.Filled.ArrowForward,
                    contentDescription = "Lihat semua",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(20.dp)
                )
            }
            if (notes.isEmpty()) {
                Text(
                    text = "Belum ada catatan.",
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            } else {
                notes.take(3).forEach { note ->
                    Text(
                        text = "• $note",
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }
    }
}

@Composable
private fun LateTaskCard(taskCount: Int, onClick: () -> Unit) {
    DashboardCard(
        modifier = Modifier
            .height(100.dp)
            .clickable(onClick = onClick)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Icon(
                painter = painterResource(id = R.drawable.ic_late),
                contentDescription = "Tugas Terlambat",
                modifier = Modifier.size(32.dp),
                tint = if (taskCount > 0) MaterialTheme.colorScheme.error
                else MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = "• $taskCount Tugas Terlambat",
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

@Composable
private fun FavoritesCard(onClick: () -> Unit) {
    DashboardCard(
        modifier = Modifier
            .height(100.dp)
            .clickable(onClick = onClick)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "Favorit",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
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

// ✅ Dialog Components
@Composable
fun LateTasksDialog(
    lateTasks: List<AgendaDto>,
    onDismiss: () -> Unit,
    onDeleteTask: (String) -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(max = 500.dp),
            shape = RoundedCornerShape(20.dp),
            color = Color.White
        ) {
            Column(modifier = Modifier.padding(24.dp)) {
                Text(
                    text = "Tugas Terlambat",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(16.dp))

                if (lateTasks.isEmpty()) {
                    Text(
                        text = "Tidak ada tugas terlambat! 🎉",
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(vertical = 32.dp)
                    )
                } else {
                    LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        items(lateTasks, key = { it.id }) { task ->
                            LateTaskItem(task = task, onDelete = { onDeleteTask(task.id) })
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
                Button(onClick = onDismiss, modifier = Modifier.fillMaxWidth()) {
                    Text("Tutup")
                }
            }
        }
    }
}

@Composable
private fun LateTaskItem(task: AgendaDto, onDelete: () -> Unit) {
    val dateFormat = SimpleDateFormat("dd MMM yyyy, HH:mm", Locale("id", "ID"))

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFFFEBEE))
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = task.title,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = task.description,
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = dateFormat.format(Date(task.startAt)),
                    fontSize = 10.sp,
                    color = MaterialTheme.colorScheme.error
                )
            }
            IconButton(onClick = onDelete) {
                Icon(
                    imageVector = Icons.Filled.Delete,
                    contentDescription = "Hapus",
                    tint = MaterialTheme.colorScheme.error
                )
            }
        }
    }
}

@Composable
fun FavoriteNotesDialog(
    favoriteNotes: List<NoteDto>,
    onDismiss: () -> Unit,
    onRemoveFavorite: (String) -> Unit,
    onDeleteNote: (String) -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(max = 500.dp),
            shape = RoundedCornerShape(20.dp),
            color = Color.White
        ) {
            Column(modifier = Modifier.padding(24.dp)) {
                Text(
                    text = "Catatan Favorit",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(16.dp))

                if (favoriteNotes.isEmpty()) {
                    Text(
                        text = "Belum ada catatan favorit.",
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(vertical = 32.dp)
                    )
                } else {
                    LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        items(favoriteNotes, key = { it.id }) { note ->
                            FavoriteNoteItem(
                                note = note,
                                onRemoveFavorite = { onRemoveFavorite(note.id) },
                                onDelete = { onDeleteNote(note.id) }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
                Button(onClick = onDismiss, modifier = Modifier.fillMaxWidth()) {
                    Text("Tutup")
                }
            }
        }
    }
}

@Composable
private fun FavoriteNoteItem(
    note: NoteDto,
    onRemoveFavorite: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF9C4))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = note.title,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.weight(1f)
                )
                Row {
                    IconButton(onClick = onRemoveFavorite) {
                        Icon(
                            imageVector = Icons.Filled.Star,
                            contentDescription = "Hapus Favorit",
                            tint = Color(0xFFFFC107)
                        )
                    }
                    IconButton(onClick = onDelete) {
                        Icon(
                            imageVector = Icons.Filled.Delete,
                            contentDescription = "Hapus",
                            tint = MaterialTheme.colorScheme.error
                        )
                    }
                }
            }
            Text(
                text = note.contentPreview,
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 3,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
fun HistoryNotesDialog(
    notes: List<NoteDto>,
    onDismiss: () -> Unit,
    onNoteClick: (NoteDto) -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(max = 500.dp),
            shape = RoundedCornerShape(20.dp),
            color = Color.White
        ) {
            Column(modifier = Modifier.padding(24.dp)) {
                Text(
                    text = "Semua Catatan",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(16.dp))

                if (notes.isEmpty()) {
                    Text(
                        text = "Belum ada catatan.",
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(vertical = 32.dp)
                    )
                } else {
                    LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        items(notes, key = { it.id }) { note ->
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { onNoteClick(note) },
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Column(modifier = Modifier.padding(16.dp)) {
                                    Text(
                                        text = note.title,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Text(
                                        text = note.contentPreview,
                                        fontSize = 12.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        maxLines = 2,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
                Button(onClick = onDismiss, modifier = Modifier.fillMaxWidth()) {
                    Text("Tutup")
                }
            }
        }
    }
}