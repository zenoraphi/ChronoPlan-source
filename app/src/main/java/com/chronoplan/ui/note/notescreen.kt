package com.chronoplan.ui.note

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.chronoplan.R
import com.chronoplan.di.AppViewModelFactory
import com.chronoplan.data.model.NoteDto
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NoteScreen(
    modifier: Modifier = Modifier,
    viewModel: NoteViewModel = viewModel(factory = AppViewModelFactory())
) {
    val uiState by viewModel.state.collectAsState()

    // Show Add/Edit Dialog
    if (uiState.showAddDialog) {
        AddEditNoteDialog(
            existingNote = if (uiState.isEditMode) uiState.selectedNote else null,
            onDismiss = { viewModel.hideAddDialog() },
            onSave = { note ->
                if (uiState.isEditMode) {
                    viewModel.updateNote(note)
                } else {
                    viewModel.addNote(note)
                }
            },
            onUploadImage = { uri ->
                viewModel.uploadAttachment(uri)
            }
        )
    }

    // Show Detail Dialog
    if (uiState.showDetailDialog && uiState.selectedNote != null) {
        NoteDetailDialog(
            note = uiState.selectedNote!!,
            onDismiss = { viewModel.hideDetailDialog() },
            onEdit = {
                viewModel.hideDetailDialog()
                viewModel.showEditDialog(uiState.selectedNote!!)
            },
            onDelete = {
                viewModel.deleteNote(uiState.selectedNote!!.id)
                viewModel.hideDetailDialog()
            },
            onToggleFavorite = {
                viewModel.toggleFavorite(uiState.selectedNote!!.id)
            }
        )
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFFF0F5FF))
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Header
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp),
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
                IconButton(
                    onClick = { /* TODO: fitur pencarian */ },
                    modifier = Modifier.align(Alignment.TopEnd).padding(top = 30.dp, end = 16.dp)
                ) {
                    Icon(Icons.Filled.Search, contentDescription = "Cari Catatan")
                }
            }

            // Grid catatan
            if (uiState.notes.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(bottom = 80.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Belum ada catatan.\nKlik tombol + untuk membuat.",
                        color = Color.Gray,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )
                }
            } else {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    contentPadding = PaddingValues(top = 8.dp, bottom = 80.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(uiState.notes, key = { note -> note.id }) { note ->
                        NoteCardItem(
                            note = note,
                            onClick = { viewModel.showDetailDialog(note) }
                        )
                    }
                }
            }
        }

        // FAB tambah catatan
        FloatingActionButton(
            onClick = { viewModel.showAddDialog() },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp),
            containerColor = MaterialTheme.colorScheme.primary
        ) {
            Icon(Icons.Filled.Add, contentDescription = "Tambah Catatan", tint = Color.White)
        }
    }
}

@Composable
fun NoteCardItem(note: NoteDto, onClick: () -> Unit) {
    val dateFormat = SimpleDateFormat("dd MMM yyyy", Locale("id", "ID"))

    Card(
        modifier = Modifier
            .width(179.dp)
            .height(166.dp)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(15.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(12.dp)
            ) {
                // Title + Favorite Icon
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = note.title,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        lineHeight = 16.sp,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                        color = Color.Black,
                        modifier = Modifier.weight(1f)
                    )

                    if (note.isFavorite) {
                        Icon(
                            imageVector = Icons.Filled.Star,
                            contentDescription = "Favorit",
                            tint = Color(0xFFFFC107),
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Content Preview
                Text(
                    text = note.contentPreview,
                    fontWeight = FontWeight.Normal,
                    fontSize = 12.sp,
                    lineHeight = 14.sp,
                    maxLines = 4,
                    overflow = TextOverflow.Ellipsis,
                    color = Color(0xFF6A786A)
                )

                Spacer(modifier = Modifier.weight(1f))
                // Labels (jika ada)
                if (note.labels.isNotEmpty()) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        modifier = Modifier.padding(bottom = 4.dp)
                    ) {
                        note.labels.take(2).forEach { label ->
                            Surface(
                                shape = RoundedCornerShape(4.dp),
                                color = MaterialTheme.colorScheme.primaryContainer
                            ) {
                                Text(
                                    text = label,
                                    fontSize = 8.sp,
                                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp),
                                    color = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                            }
                        }
                        if (note.labels.size > 2) {
                            Text(
                                text = "+${note.labels.size - 2}",
                                fontSize = 8.sp,
                                color = Color.Gray
                            )
                        }
                    }
                }

                // Date
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_calendar),
                        contentDescription = "Tanggal",
                        modifier = Modifier.size(12.dp),
                        tint = Color(0xFF6A786A)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = dateFormat.format(Date(note.updatedAt)),
                        fontWeight = FontWeight.Normal,
                        fontSize = 10.sp,
                        color = Color(0xFF6A786A)
                    )
                }
            }
        }
    }
}