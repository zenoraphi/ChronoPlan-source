package com.chronoplan.ui.note

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import coil.compose.rememberAsyncImagePainter
import com.chronoplan.data.model.NoteDto
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditNoteDialog(
    existingNote: NoteDto? = null,
    onDismiss: () -> Unit,
    onSave: (NoteDto) -> Unit,
    onUploadImage: (Uri) -> Unit = {}
) {
    var title by remember { mutableStateOf(existingNote?.title ?: "") }
    var content by remember { mutableStateOf(TextFieldValue(existingNote?.content ?: "")) }
    var labels by remember { mutableStateOf(existingNote?.labels ?: emptyList()) }
    var attachments by remember { mutableStateOf(existingNote?.attachments ?: emptyList()) }
    var currentLabel by remember { mutableStateOf("") }
    var showLabelInput by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    // Image picker
    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            onUploadImage(it)
            // Tambahkan URI ke attachments (nanti akan di-replace dengan URL dari Firebase)
            attachments = attachments + uri.toString()
        }
    }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.9f),
            shape = RoundedCornerShape(20.dp),
            color = Color.White
        ) {
            Column(
                modifier = Modifier
                    .verticalScroll(rememberScrollState())
                    .padding(24.dp)
            ) {
                Text(
                    text = if (existingNote == null) "Buat Catatan" else "Edit Catatan",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Judul Note
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Judul") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Toolbar Rich Text (Simplified)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFFF5F5F5), RoundedCornerShape(8.dp))
                        .padding(8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        IconButton(onClick = { /* Bold */ }, modifier = Modifier.size(32.dp)) {
                            Icon(Icons.Filled.FormatBold, contentDescription = "Bold", modifier = Modifier.size(20.dp))
                        }
                        IconButton(onClick = { /* Italic */ }, modifier = Modifier.size(32.dp)) {
                            Icon(Icons.Filled.FormatItalic, contentDescription = "Italic", modifier = Modifier.size(20.dp))
                        }
                        IconButton(onClick = { /* List */ }, modifier = Modifier.size(32.dp)) {
                            Icon(Icons.Filled.FormatListBulleted, contentDescription = "List", modifier = Modifier.size(20.dp))
                        }
                    }

                    IconButton(
                        onClick = { imagePickerLauncher.launch("image/*") },
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(Icons.Filled.Image, contentDescription = "Tambah Gambar", modifier = Modifier.size(20.dp))
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Content Editor
                OutlinedTextField(
                    value = content,
                    onValueChange = { content = it },
                    label = { Text("Isi Catatan") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(300.dp),
                    maxLines = 15
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Attachments Preview
                if (attachments.isNotEmpty()) {
                    Text(
                        text = "Lampiran (${attachments.size})",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(attachments) { attachment ->
                            Box(
                                modifier = Modifier
                                    .size(80.dp)
                                    .border(1.dp, Color.Gray, RoundedCornerShape(8.dp))
                                    .clickable { /* Preview image */ }
                            ) {
                                Image(
                                    painter = rememberAsyncImagePainter(model = attachment),
                                    contentDescription = "Attachment",
                                    modifier = Modifier.fillMaxSize(),
                                    contentScale = ContentScale.Crop
                                )

                                IconButton(
                                    onClick = {
                                        attachments = attachments.filter { it != attachment }
                                    },
                                    modifier = Modifier
                                        .align(Alignment.TopEnd)
                                        .size(24.dp)
                                        .background(Color.Red, RoundedCornerShape(12.dp))
                                ) {
                                    Icon(
                                        Icons.Filled.Close,
                                        contentDescription = "Hapus",
                                        tint = Color.White,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))
                }

                // Labels
                Text(
                    text = "Label",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium
                )
                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        items(labels) { label ->
                            AssistChip(
                                onClick = { labels = labels.filter { it != label } },
                                label = { Text(label, fontSize = 12.sp) },
                                trailingIcon = {
                                    Icon(
                                        Icons.Filled.Close,
                                        contentDescription = "Hapus",
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            )
                        }
                    }

                    IconButton(
                        onClick = { showLabelInput = !showLabelInput },
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(Icons.Filled.Add, contentDescription = "Tambah Label")
                    }
                }

                if (showLabelInput) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedTextField(
                            value = currentLabel,
                            onValueChange = {
                                if (it.length <= 15) currentLabel = it
                            },
                            label = { Text("Nama Label") },
                            modifier = Modifier.weight(1f),
                            singleLine = true
                        )
                        Button(
                            onClick = {
                                if (currentLabel.isNotBlank() && labels.size < 5) {
                                    labels = labels + currentLabel
                                    currentLabel = ""
                                    showLabelInput = false
                                }
                            }
                        ) {
                            Text("Tambah")
                        }
                    }
                    Text(
                        text = "Maksimal 15 karakter, ${labels.size}/5 label",
                        fontSize = 10.sp,
                        color = Color.Gray
                    )
                }

                if (errorMessage != null) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = errorMessage ?: "",
                        color = Color.Red,
                        fontSize = 12.sp
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Action Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
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
                                content.text.isBlank() -> {
                                    errorMessage = "Isi catatan harus diisi"
                                    return@Button
                                }
                            }

                            val contentPreview = content.text.take(100) + if (content.text.length > 100) "..." else ""

                            val note = NoteDto(
                                id = existingNote?.id ?: "",
                                title = title,
                                content = content.text,
                                contentPreview = contentPreview,
                                labels = labels,
                                attachments = attachments,
                                isFavorite = existingNote?.isFavorite ?: false,
                                createdAt = existingNote?.createdAt ?: System.currentTimeMillis(),
                                updatedAt = System.currentTimeMillis()
                            )

                            onSave(note)
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

@Composable
fun NoteDetailDialog(
    note: NoteDto,
    onDismiss: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onToggleFavorite: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.85f),
            shape = RoundedCornerShape(20.dp),
            color = Color.White
        ) {
            Column(
                modifier = Modifier
                    .verticalScroll(rememberScrollState())
                    .padding(24.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = note.title,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.weight(1f)
                    )

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        IconButton(onClick = onToggleFavorite) {
                            Icon(
                                imageVector = if (note.isFavorite) Icons.Filled.Star else Icons.Filled.StarBorder,
                                contentDescription = "Favorit",
                                tint = if (note.isFavorite) Color(0xFFFFC107) else Color.Gray
                            )
                        }

                        IconButton(onClick = onEdit) {
                            Icon(Icons.Filled.Edit, contentDescription = "Edit")
                        }

                        IconButton(onClick = onDelete) {
                            Icon(Icons.Filled.Delete, contentDescription = "Hapus", tint = Color.Red)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Labels
                if (note.labels.isNotEmpty()) {
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(note.labels) { label ->
                            AssistChip(
                                onClick = {},
                                label = { Text(label, fontSize = 12.sp) }
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                }

                Divider()

                Spacer(modifier = Modifier.height(16.dp))

                // Content
                Text(
                    text = note.content,
                    fontSize = 14.sp,
                    lineHeight = 20.sp
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Attachments
                if (note.attachments.isNotEmpty()) {
                    Text(
                        text = "Lampiran",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(note.attachments) { attachment ->
                            Box(
                                modifier = Modifier
                                    .size(120.dp)
                                    .border(1.dp, Color.Gray, RoundedCornerShape(8.dp))
                                    .clickable { /* Open fullscreen */ }
                            ) {
                                Image(
                                    painter = rememberAsyncImagePainter(model = attachment),
                                    contentDescription = "Attachment",
                                    modifier = Modifier.fillMaxSize(),
                                    contentScale = ContentScale.Crop
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))
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