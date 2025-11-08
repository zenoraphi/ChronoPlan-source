package com.chronoplan.ui.agenda

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.viewmodel.compose.viewModel
import com.chronoplan.R
import com.chronoplan.di.AppViewModelFactory
import com.chronoplan.data.model.AgendaDto
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.format.TextStyle
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AgendaScreen(
    modifier: Modifier = Modifier,
    viewModel: AgendaViewModel = viewModel(factory = AppViewModelFactory())
) {
    val context = LocalContext.current
    val uiState by viewModel.state.collectAsState()

    if (uiState.showAddDialog) {
        AddAgendaDialog(
            onDismiss = { viewModel.hideAddDialog() },
            onSave = { agenda ->
                viewModel.addAgenda(agenda, context)
            }
        )
    }

    if (uiState.showHistoryDialog) {
        AgendaHistoryDialog(
            agendas = uiState.agendas,
            onDismiss = { viewModel.hideHistoryDialog() },
            onDelete = { agendaId -> viewModel.deleteAgenda(agendaId) }
        )
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFFF0F5FF))
    ) {
        // Header
        Box(
            modifier = Modifier.fillMaxWidth(),
            contentAlignment = Alignment.TopCenter
        ) {
            Image(
                painter = painterResource(id = R.drawable.ic_border),
                contentDescription = null,
                modifier = Modifier
                    .width(200.dp)
                    .height(150.dp)
                    .align(Alignment.TopEnd),
                contentScale = ContentScale.FillBounds
            )
            Column(
                modifier = Modifier.padding(top = 40.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Image(
                    painter = painterResource(id = R.drawable.ic_chronoplan_logo),
                    contentDescription = "Logo Chronoplan",
                    modifier = Modifier.size(80.dp)
                )
                Text(
                    text = "CHRONOPLAN",
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }
        }

        // Kontainer putih utama
        Surface(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            shape = RoundedCornerShape(20.dp),
            color = Color.White,
            shadowElevation = 4.dp
        ) {
            Column(
                modifier = Modifier
                    .padding(top = 16.dp, start = 16.dp, end = 16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = uiState.selectedDateFormatted,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.clickable { /* TODO: kalender */ }
                    )
                    DayIndicatorStrip(
                        DayOfWeek.of(
                            if (uiState.selectedDayOfWeek in 1..7) uiState.selectedDayOfWeek
                            else LocalDate.now().dayOfWeek.value
                        )
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                LazyColumn(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    contentPadding = PaddingValues(bottom = 16.dp)
                ) {
                    if (uiState.jadwalHariIni.isEmpty()) {
                        item {
                            Text(
                                text = "Belum ada agenda untuk hari ini.\nKlik 'Jadwalkan Tugas' untuk menambah.",
                                color = Color.Gray,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(top = 16.dp),
                                textAlign = TextAlign.Center
                            )
                        }
                    } else {
                        items(
                            items = uiState.jadwalHariIni,
                            key = { agenda -> agenda.id }
                        ) { agenda ->
                            AgendaListItem(
                                agenda = agenda,
                                onToggleDone = { viewModel.toggleTaskDone(agenda.id) }
                            )
                        }
                    }
                }
            }
        }

        // Tombol bawah
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = { viewModel.showHistoryDialog() },
                modifier = Modifier
                    .size(56.dp)
                    .background(Color.White, CircleShape)
                    .border(1.dp, Color.LightGray.copy(alpha = 0.5f), CircleShape)
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_time),
                    contentDescription = "History",
                    tint = LocalContentColor.current.copy(alpha = 0.7f)
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            Button(
                onClick = { viewModel.showAddDialog() },
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .height(56.dp)
                    .weight(1f),
                contentPadding = PaddingValues(horizontal = 16.dp)
            ) {
                Text(
                    text = "Jadwalkan Tugas",
                    modifier = Modifier.weight(1f),
                    textAlign = TextAlign.Center
                )
                Icon(Icons.Filled.Add, contentDescription = null)
            }
        }
    }
}

@Composable
private fun DayIndicatorStrip(selectedDay: DayOfWeek) {
    val daysOfWeek = DayOfWeek.values()
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        daysOfWeek.forEach { day ->
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = day.getDisplayName(TextStyle.SHORT, Locale("id", "ID")),
                    fontSize = 12.sp,
                    color = if (day == selectedDay) MaterialTheme.colorScheme.primary else Color.Gray,
                    fontWeight = if (day == selectedDay) FontWeight.Bold else FontWeight.Normal
                )
                Spacer(modifier = Modifier.height(4.dp))
                if (day == selectedDay) {
                    Box(
                        modifier = Modifier
                            .size(6.dp)
                            .background(MaterialTheme.colorScheme.primary, CircleShape)
                    )
                } else Spacer(modifier = Modifier.height(6.dp))
            }
        }
    }
}

@Composable
private fun AgendaListItem(agenda: AgendaDto, onToggleDone: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .padding(horizontal = 16.dp, vertical = 12.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .background(MaterialTheme.colorScheme.primaryContainer, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_work),
                        contentDescription = "Kategori",
                        modifier = Modifier.size(18.dp),
                        tint = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(text = agenda.title, fontWeight = FontWeight.Medium, maxLines = 1)
                    Text(text = agenda.date, fontSize = 12.sp, color = Color.Gray)
                }
            }
            Icon(
                imageVector = Icons.Filled.CheckCircle,
                contentDescription = "Tandai Selesai",
                tint = if (agenda.status == "done") Color(0xFF4CAF50)
                else Color.LightGray.copy(alpha = 0.5f),
                modifier = Modifier
                    .size(24.dp)
                    .clickable(onClick = onToggleDone)
            )
        }
    }
}

@Composable
fun AgendaHistoryDialog(
    agendas: List<AgendaDto>,
    onDismiss: () -> Unit,
    onDelete: (String) -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(max = 500.dp),
            shape = RoundedCornerShape(20.dp),
            color = Color.White
        ) {
            Column(
                modifier = Modifier.padding(24.dp)
            ) {
                Text(
                    text = "Semua Agenda",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(16.dp))

                if (agendas.isEmpty()) {
                    Text(
                        text = "Belum ada agenda",
                        color = Color.Gray,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 32.dp),
                        textAlign = TextAlign.Center
                    )
                } else {
                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(agendas.sortedByDescending { it.createdAt }, key = { it.id }) { agenda ->
                            AgendaHistoryItem(
                                agenda = agenda,
                                onDelete = { onDelete(agenda.id) }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = onDismiss,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Tutup")
                }
            }
        }
    }
}

@Composable
private fun AgendaHistoryItem(
    agenda: AgendaDto,
    onDelete: () -> Unit
) {
    var showDeleteConfirm by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { showDeleteConfirm = !showDeleteConfirm },
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = when (agenda.status) {
                "done" -> Color(0xFFE8F5E9)
                "missed" -> Color(0xFFFFEBEE)
                else -> Color(0xFFFFF9C4)
            }
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = agenda.title,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.weight(1f)
                )

                if (showDeleteConfirm) {
                    IconButton(
                        onClick = onDelete,
                        modifier = Modifier.size(24.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Delete,
                            contentDescription = "Hapus",
                            tint = Color.Red
                        )
                    }
                }
            }

            Text(
                text = agenda.description,
                fontSize = 12.sp,
                color = Color.Gray,
                maxLines = 2
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = "${agenda.date} • ${
                    when (agenda.status) {
                        "done" -> "Selesai"
                        "missed" -> "Terlewat"
                        "pending" -> "Menunggu"
                        else -> "Unknown"
                    }
                }",
                fontSize = 10.sp,
                color = Color.Gray
            )
        }
    }
}