package com.chronoplan.ui.agenda

import android.app.DatePickerDialog
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
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AgendaScreen(
    modifier: Modifier = Modifier,
    viewModel: AgendaViewModel = viewModel(factory = AppViewModelFactory())
) {
    val context = LocalContext.current
    val uiState by viewModel.state.collectAsState()

    // ✅ Reset to today when screen appears
    DisposableEffect(Unit) {
        onDispose {
            viewModel.resetToToday()
        }
    }

    // ✅ Date Picker Dialog
    if (uiState.showDatePicker) {
        val calendar = Calendar.getInstance()
        calendar.timeInMillis = uiState.selectedDate

        DatePickerDialog(
            context,
            { _, year, month, day ->
                val newCalendar = Calendar.getInstance()
                newCalendar.set(year, month, day)
                viewModel.changeDate(newCalendar.timeInMillis)
                viewModel.hideDatePicker()
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        ).apply {
            setOnDismissListener { viewModel.hideDatePicker() }
            show()
        }
    }

    // Dialogs
    if (uiState.showAddDialog) {
        AddAgendaDialog(
            onDismiss = { viewModel.hideAddDialog() },
            onSave = { agenda -> viewModel.addAgenda(agenda, context) }
        )
    }

    if (uiState.showHistoryDialog) {
        AgendaHistoryDialog(
            agendas = uiState.agendas,
            onDismiss = { viewModel.hideHistoryDialog() },
            onDelete = { agendaId -> viewModel.deleteAgenda(agendaId) }
        )
    }

    // ✅ NEW: Agenda Detail Dialog
    if (uiState.showDetailDialog && uiState.selectedAgenda != null) {
        AgendaDetailDialog(
            agenda = uiState.selectedAgenda!!,
            onDismiss = { viewModel.hideAgendaDetail() },
            onDelete = {
                viewModel.deleteAgenda(uiState.selectedAgenda!!.id)
                viewModel.hideAgendaDetail()
            },
            onToggleDone = {
                viewModel.toggleTaskDone(uiState.selectedAgenda!!.id)
            }
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
                modifier = Modifier.padding(top = 16.dp, start = 16.dp, end = 16.dp)
            ) {
                // ✅ FIXED: Tanggal di tengah dan clickable
                Text(
                    text = uiState.selectedDateFormatted,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { viewModel.showDatePicker() }
                        .padding(vertical = 8.dp)
                )

                Spacer(modifier = Modifier.height(16.dp))

                LazyColumn(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    contentPadding = PaddingValues(bottom = 16.dp)
                ) {
                    if (uiState.jadwalHariIni.isEmpty()) {
                        item {
                            Text(
                                text = "Belum ada agenda untuk tanggal ini.\nKlik 'Jadwalkan Tugas' untuk menambah.",
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
                                onToggleDone = { viewModel.toggleTaskDone(agenda.id) },
                                onClick = { viewModel.showAgendaDetail(agenda) } // ✅ NEW
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
private fun AgendaListItem(
    agenda: AgendaDto,
    onToggleDone: () -> Unit,
    onClick: () -> Unit // ✅ NEW
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick), // ✅ Make clickable
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
fun AgendaDetailDialog(
    agenda: AgendaDto,
    onDismiss: () -> Unit,
    onDelete: () -> Unit,
    onToggleDone: () -> Unit
) {
    val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
    val dateFormat = SimpleDateFormat("dd MMMM yyyy", Locale("id", "ID"))

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            color = Color.White
        ) {
            Column(modifier = Modifier.padding(24.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = agenda.title,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.weight(1f)
                    )

                    Row {
                        IconButton(onClick = onToggleDone) {
                            Icon(
                                imageVector = Icons.Filled.CheckCircle,
                                contentDescription = "Toggle Done",
                                tint = if (agenda.status == "done") Color(0xFF4CAF50)
                                else Color.LightGray
                            )
                        }
                        IconButton(onClick = onDelete) {
                            Icon(
                                imageVector = Icons.Filled.Delete,
                                contentDescription = "Hapus",
                                tint = Color(0xFFC62828)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = agenda.description,
                    fontSize = 14.sp,
                    color = Color(0xFF616161)
                )

                Spacer(modifier = Modifier.height(16.dp))

                Divider()

                Spacer(modifier = Modifier.height(16.dp))

                InfoRow("Tanggal", dateFormat.format(Date(agenda.startAt)))
                InfoRow("Waktu Mulai", timeFormat.format(Date(agenda.startAt)))
                InfoRow("Waktu Selesai", timeFormat.format(Date(agenda.endAt)))
                InfoRow("Status", when (agenda.status) {
                    "done" -> "Selesai"
                    "pending" -> "Menunggu"
                    "missed" -> "Terlewat"
                    else -> "Unknown"
                })

                if (agenda.reminderMinutesBefore > 0) {
                    InfoRow("Pengingat", "${agenda.reminderMinutesBefore} menit sebelumnya")
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
private fun InfoRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            fontSize = 14.sp,
            color = Color(0xFF757575),
            fontWeight = FontWeight.Medium
        )
        Text(
            text = value,
            fontSize = 14.sp,
            color = Color(0xFF000000)
        )
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