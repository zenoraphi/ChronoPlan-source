package com.chronoplan.ui.agenda

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.chronoplan.R
import com.chronoplan.data.model.AgendaDto
import java.text.SimpleDateFormat
import java.util.*
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.ArrowDropDown



// List icon yang tersedia untuk agenda
val availableAgendaIcons = listOf(
    R.drawable.ic_work to "Kerja",
    R.drawable.ic_school to "Sekolah",
    R.drawable.ic_cleaning to "Bersih-bersih",
    R.drawable.ic_calendar to "Meeting",
    R.drawable.ic_note to "Catatan",
    R.drawable.ic_star to "Penting"
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddAgendaDialog(
    onDismiss: () -> Unit,
    onSave: (AgendaDto) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var selectedDate by remember { mutableStateOf(Calendar.getInstance()) }
    var selectedStartTime by remember { mutableStateOf(Calendar.getInstance()) }
    var selectedEndTime by remember { mutableStateOf(Calendar.getInstance().apply { add(Calendar.HOUR_OF_DAY, 1) }) }
    var selectedIconIndex by remember { mutableStateOf(0) }
    var reminderEnabled by remember { mutableStateOf(true) }
    var reminderMinutes by remember { mutableStateOf(30) }
    var showIconPicker by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    val context = LocalContext.current
    val dateFormat = SimpleDateFormat("dd MMM yyyy", Locale("id", "ID"))
    val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())

    val datePickerDialog = remember {
        DatePickerDialog(
            context,
            { _, year, month, day ->
                selectedDate.set(year, month, day)
            },
            selectedDate.get(Calendar.YEAR),
            selectedDate.get(Calendar.MONTH),
            selectedDate.get(Calendar.DAY_OF_MONTH)
        )
    }

    val startTimePickerDialog = remember {
        TimePickerDialog(
            context,
            { _, hour, minute ->
                selectedStartTime.set(Calendar.HOUR_OF_DAY, hour)
                selectedStartTime.set(Calendar.MINUTE, minute)
                selectedEndTime.timeInMillis = selectedStartTime.timeInMillis
                selectedEndTime.add(Calendar.HOUR_OF_DAY, 1)
            },
            selectedStartTime.get(Calendar.HOUR_OF_DAY),
            selectedStartTime.get(Calendar.MINUTE),
            true
        )
    }

    val endTimePickerDialog = remember {
        TimePickerDialog(
            context,
            { _, hour, minute ->
                selectedEndTime.set(Calendar.HOUR_OF_DAY, hour)
                selectedEndTime.set(Calendar.MINUTE, minute)
            },
            selectedEndTime.get(Calendar.HOUR_OF_DAY),
            selectedEndTime.get(Calendar.MINUTE),
            true
        )
    }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.95f), // Perbesar dikit biar gak kebanting
            shape = RoundedCornerShape(20.dp),
            color = Color.White
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
            ) {
                // Header Fixed
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color.White)
                        .padding(24.dp)
                ) {
                    Text(
                        text = "Jadwalkan Tugas",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                // Scrollable Content
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .verticalScroll(rememberScrollState())
                        .padding(horizontal = 24.dp)
                ) {
                    OutlinedTextField(
                        value = title,
                        onValueChange = { title = it },
                        label = { Text("Judul Agenda") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedTextField(
                        value = description,
                        onValueChange = { description = it },
                        label = { Text("Deskripsi/Catatan") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(100.dp),
                        maxLines = 4
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedTextField(
                        value = dateFormat.format(selectedDate.time),
                        onValueChange = {},
                        label = { Text("Tanggal") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { datePickerDialog.show() },
                        enabled = false,
                        colors = TextFieldDefaults.outlinedTextFieldColors(
                            disabledTextColor = MaterialTheme.colorScheme.onSurface,
                            disabledBorderColor = MaterialTheme.colorScheme.outline,
                            disabledLabelColor = MaterialTheme.colorScheme.onSurfaceVariant
                        ),
                        trailingIcon = {
                            Icon(Icons.Filled.DateRange, contentDescription = null)
                        }
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        OutlinedTextField(
                            value = timeFormat.format(selectedStartTime.time),
                            onValueChange = {},
                            label = { Text("Mulai") },
                            modifier = Modifier
                                .weight(1f)
                                .clickable { startTimePickerDialog.show() },
                            enabled = false,
                            colors = TextFieldDefaults.outlinedTextFieldColors(
                                disabledTextColor = MaterialTheme.colorScheme.onSurface,
                                disabledBorderColor = MaterialTheme.colorScheme.outline,
                                disabledLabelColor = MaterialTheme.colorScheme.onSurfaceVariant
                            ),
                            trailingIcon = {
                                Icon(Icons.Filled.AccessTime, contentDescription = null)
                            }
                        )

                        OutlinedTextField(
                            value = timeFormat.format(selectedEndTime.time),
                            onValueChange = {},
                            label = { Text("Selesai") },
                            modifier = Modifier
                                .weight(1f)
                                .clickable { endTimePickerDialog.show() },
                            enabled = false,
                            colors = TextFieldDefaults.outlinedTextFieldColors(
                                disabledTextColor = MaterialTheme.colorScheme.onSurface,
                                disabledBorderColor = MaterialTheme.colorScheme.outline,
                                disabledLabelColor = MaterialTheme.colorScheme.onSurfaceVariant
                            ),
                            trailingIcon = {
                                Icon(Icons.Filled.AccessTime, contentDescription = null)
                            }
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = "Pilih Icon",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color.Gray
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(8.dp))
                            .clickable { showIconPicker = !showIconPicker }
                            .padding(12.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    painter = painterResource(id = availableAgendaIcons[selectedIconIndex].first),
                                    contentDescription = null,
                                    modifier = Modifier.size(24.dp),
                                    tint = MaterialTheme.colorScheme.primary
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(text = availableAgendaIcons[selectedIconIndex].second)
                            }
                            Icon(
                                imageVector = if (showIconPicker) Icons.Filled.KeyboardArrowUp else Icons.Filled.KeyboardArrowDown,
                                contentDescription = null
                            )
                        }
                    }

                    if (showIconPicker) {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 4.dp),
                            colors = CardDefaults.cardColors(containerColor = Color.White),
                            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                        ) {
                            Column {
                                availableAgendaIcons.forEachIndexed { index, (iconRes, label) ->
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clickable {
                                                selectedIconIndex = index
                                                showIconPicker = false
                                            }
                                            .padding(12.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(
                                            painter = painterResource(id = iconRes),
                                            contentDescription = null,
                                            modifier = Modifier.size(24.dp),
                                            tint = if (index == selectedIconIndex) MaterialTheme.colorScheme.primary
                                            else Color.Gray
                                        )
                                        Spacer(modifier = Modifier.width(12.dp))
                                        Text(
                                            text = label,
                                            fontWeight = if (index == selectedIconIndex) FontWeight.Bold else FontWeight.Normal
                                        )
                                    }
                                    if (index < availableAgendaIcons.size - 1) {
                                        Divider()
                                    }
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(text = "Pengingat", fontSize = 14.sp)
                        Switch(
                            checked = reminderEnabled,
                            onCheckedChange = { reminderEnabled = it }
                        )
                    }

                    if (reminderEnabled) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(text = "Ingatkan", fontSize = 14.sp, modifier = Modifier.weight(1f))

                            // Dropdown untuk pilih waktu reminder (ganti text field)
                            var expanded by remember { mutableStateOf(false) }
                            val reminderOptions = listOf(5, 10, 15, 30, 60, 120)

                            Box {
                                OutlinedButton(
                                    onClick = { expanded = true },
                                    modifier = Modifier.width(120.dp)
                                ) {
                                    Text("$reminderMinutes menit")
                                    Icon(Icons.Filled.ArrowDropDown, null)
                                }

                                DropdownMenu(
                                    expanded = expanded,
                                    onDismissRequest = { expanded = false }
                                ) {
                                    reminderOptions.forEach { minutes ->
                                        DropdownMenuItem(
                                            text = { Text("$minutes menit") },
                                            onClick = {
                                                reminderMinutes = minutes
                                                expanded = false
                                            }
                                        )
                                    }
                                }
                            }
                        }
                    }

                    if (errorMessage != null) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = errorMessage ?: "",
                            color = Color.Red,
                            fontSize = 12.sp
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))
                }

                // Bottom Actions Fixed
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color.White)
                        .padding(24.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Batal")
                    }

                    Button(
                        onClick = {
                            when {
                                title.isBlank() -> {
                                    errorMessage = "Judul harus diisi"
                                    return@Button
                                }
                                description.isBlank() -> {
                                    errorMessage = "Deskripsi harus diisi"
                                    return@Button
                                }
                            }

                            val now = Calendar.getInstance()
                            val scheduledDateTime = Calendar.getInstance().apply {
                                set(Calendar.YEAR, selectedDate.get(Calendar.YEAR))
                                set(Calendar.MONTH, selectedDate.get(Calendar.MONTH))
                                set(Calendar.DAY_OF_MONTH, selectedDate.get(Calendar.DAY_OF_MONTH))
                                set(Calendar.HOUR_OF_DAY, selectedStartTime.get(Calendar.HOUR_OF_DAY))
                                set(Calendar.MINUTE, selectedStartTime.get(Calendar.MINUTE))
                            }

                            if (scheduledDateTime.before(now)) {
                                errorMessage = "Tidak bisa membuat agenda di masa lalu"
                                return@Button
                            }

                            if (selectedEndTime.timeInMillis <= selectedStartTime.timeInMillis) {
                                errorMessage = "Waktu selesai harus setelah waktu mulai"
                                return@Button
                            }

                            val dateStr = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                                .format(selectedDate.time)

                            val agenda = AgendaDto(
                                title = title,
                                description = description,
                                date = dateStr,
                                startAt = scheduledDateTime.timeInMillis,
                                endAt = selectedEndTime.timeInMillis,
                                status = "pending",
                                reminderMinutesBefore = if (reminderEnabled) reminderMinutes else 0,
                                createdAt = System.currentTimeMillis(),
                                updatedAt = System.currentTimeMillis()
                            )

                            onSave(agenda)
                            onDismiss()
                        },
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Simpan")
                    }
                }
            }
        }
    }
}