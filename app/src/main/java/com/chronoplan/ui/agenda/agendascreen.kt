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
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.chronoplan.R
import com.chronoplan.di.AppViewModelFactory
import com.chronoplan.domain.model.Agenda
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.format.TextStyle
import java.util.Locale
import com.chronoplan.data.model.AgendaDto

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AgendaScreen(
    modifier: Modifier = Modifier,
    viewModel: AgendaViewModel = viewModel(factory = AppViewModelFactory())
) {
    val uiState by viewModel.state.collectAsState()

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
                            if (uiState.selectedDayOfWeek in 1..7) uiState.selectedDayOfWeek else LocalDate.now().dayOfWeek.value
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
                                text = "Tidak ada jadwal untuk tanggal ini.",
                                color = Color.Gray,
                                modifier = Modifier.padding(top = 16.dp)
                            )
                        }
                    } else {
                        items(uiState.jadwalHariIni, key = { it.id }) { agenda ->
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
                onClick = { /* TODO: fitur waktu */ },
                modifier = Modifier
                    .size(56.dp)
                    .background(Color.White, CircleShape)
                    .border(1.dp, Color.LightGray.copy(alpha = 0.5f), CircleShape)
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_time),
                    contentDescription = "Waktu",
                    tint = LocalContentColor.current.copy(alpha = 0.7f)
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            Button(
                onClick = { /* TODO: tambah agenda */ },
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
                    Text(text = agenda.date, fontSize = 12.sp, color = Color.Gray)
                    Text(text = agenda.title, fontWeight = FontWeight.Medium, maxLines = 1)
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
