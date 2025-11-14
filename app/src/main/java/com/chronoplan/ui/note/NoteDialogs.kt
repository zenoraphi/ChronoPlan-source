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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.*
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.*
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import coil.compose.rememberAsyncImagePainter
import com.chronoplan.data.model.NoteDto

data class TextFormat(
    val isBold: Boolean = false,
    val isItalic: Boolean = false,
    val isUnderline: Boolean = false
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditNoteDialog(
    existingNote: NoteDto? = null,
    onDismiss: () -> Unit,
    onSave: (NoteDto) -> Unit,
    onUploadImage: (Uri) -> Unit = {}
) {
    var title by remember { mutableStateOf(existingNote?.title ?: "") }
    var textFieldValue by remember {
        mutableStateOf(
            TextFieldValue(
                text = existingNote?.content ?: "",
                selection = TextRange(existingNote?.content?.length ?: 0)
            )
        )
    }
    var labels by remember { mutableStateOf(existingNote?.labels ?: emptyList()) }
    var attachments by remember { mutableStateOf(existingNote?.attachments ?: emptyList()) }
    var currentLabel by remember { mutableStateOf("") }
    var showLabelInput by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var showFullImage by remember { mutableStateOf<String?>(null) }
    var textFormat by remember { mutableStateOf(TextFormat()) }

    // Ambil warna di luar VisualTransformation
    val primaryColor = MaterialTheme.colorScheme.primary
    val outlineColor = MaterialTheme.colorScheme.outline
    val onSurfaceVariantColor = MaterialTheme.colorScheme.onSurfaceVariant

    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            onUploadImage(it)
            attachments = attachments + uri.toString()
        }
    }

    if (showFullImage != null) {
        Dialog(onDismissRequest = { showFullImage = null }) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black)
                    .clickable { showFullImage = null },
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painter = rememberAsyncImagePainter(model = showFullImage),
                    contentDescription = "Full Image",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Fit
                )
                IconButton(
                    onClick = { showFullImage = null },
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(16.dp)
                        .background(Color.Black.copy(alpha = 0.5f), CircleShape)
                ) {
                    Icon(Icons.Filled.Close, contentDescription = "Close", tint = Color.White)
                }
            }
        }
    }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.95f),
            shape = RoundedCornerShape(20.dp),
            color = Color.White
        ) {
            Box(modifier = Modifier.fillMaxSize()) {
                Column(modifier = Modifier.fillMaxSize()) {
                    // Header Fixed
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        color = Color.White,
                        shadowElevation = 2.dp
                    ) {
                        Column(modifier = Modifier.padding(24.dp)) {
                            Text(
                                text = if (existingNote == null) "Buat Catatan" else "Edit Catatan",
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }

                    // Scrollable Content
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .verticalScroll(rememberScrollState())
                            .padding(horizontal = 24.dp)
                    ) {
                        Spacer(modifier = Modifier.height(16.dp))

                        OutlinedTextField(
                            value = title,
                            onValueChange = { title = it },
                            label = { Text("Judul", color = onSurfaceVariantColor) },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            colors = TextFieldDefaults.outlinedTextFieldColors(
                                focusedTextColor = MaterialTheme.colorScheme.onSurface,
                                unfocusedTextColor = MaterialTheme.colorScheme.onSurface
                            )
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        // Toolbar
                        Surface(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(8.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant
                        ) {
                            Row(
                                modifier = Modifier.padding(8.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    IconToggleButton(
                                        checked = textFormat.isBold,
                                        onCheckedChange = { textFormat = textFormat.copy(isBold = it) }
                                    ) {
                                        Icon(
                                            Icons.Filled.FormatBold,
                                            contentDescription = "Bold",
                                            tint = if (textFormat.isBold) primaryColor
                                            else onSurfaceVariantColor
                                        )
                                    }
                                    IconToggleButton(
                                        checked = textFormat.isItalic,
                                        onCheckedChange = { textFormat = textFormat.copy(isItalic = it) }
                                    ) {
                                        Icon(
                                            Icons.Filled.FormatItalic,
                                            contentDescription = "Italic",
                                            tint = if (textFormat.isItalic) primaryColor
                                            else onSurfaceVariantColor
                                        )
                                    }
                                    IconToggleButton(
                                        checked = textFormat.isUnderline,
                                        onCheckedChange = { textFormat = textFormat.copy(isUnderline = it) }
                                    ) {
                                        Icon(
                                            Icons.Filled.FormatUnderlined,
                                            contentDescription = "Underline",
                                            tint = if (textFormat.isUnderline) primaryColor
                                            else onSurfaceVariantColor
                                        )
                                    }
                                    IconButton(onClick = { /* List */ }) {
                                        Icon(Icons.Filled.FormatListBulleted, contentDescription = "List")
                                    }
                                }

                                IconButton(onClick = { imagePickerLauncher.launch("image/*") }) {
                                    Icon(Icons.Filled.Image, contentDescription = "Tambah Gambar")
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // Text Field dengan format per seleksi
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(300.dp)
                                .border(1.dp, outlineColor, RoundedCornerShape(4.dp)),
                            color = Color.White
                        ) {
                            BasicTextField(
                                value = textFieldValue,
                                onValueChange = { textFieldValue = it },
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(12.dp),
                                cursorBrush = SolidColor(primaryColor),
                                visualTransformation = VisualTransformation { text ->
                                    val start = textFieldValue.selection.start.coerceAtMost(text.text.length)
                                    val end = textFieldValue.selection.end.coerceAtMost(text.text.length)

                                    val annotatedString = buildAnnotatedString {
                                        // Sebelum seleksi
                                        withStyle(
                                            SpanStyle(
                                                fontWeight = FontWeight.Normal,
                                                fontStyle = FontStyle.Normal,
                                                textDecoration = TextDecoration.None
                                            )
                                        ) {
                                            append(text.text.substring(0, start))
                                        }

                                        // Seleksi (dengan format aktif + background)
                                        withStyle(
                                            SpanStyle(
                                                fontWeight = if (textFormat.isBold) FontWeight.Bold else FontWeight.Normal,
                                                fontStyle = if (textFormat.isItalic) FontStyle.Italic else FontStyle.Normal,
                                                textDecoration = if (textFormat.isUnderline) TextDecoration.Underline else TextDecoration.None,
                                                background = primaryColor.copy(alpha = 0.2f)
                                            )
                                        ) {
                                            append(text.text.substring(start, end))
                                        }

                                        // Setelah seleksi (ikut format saat mengetik)
                                        withStyle(
                                            SpanStyle(
                                                fontWeight = if (textFormat.isBold) FontWeight.Bold else FontWeight.Normal,
                                                fontStyle = if (textFormat.isItalic) FontStyle.Italic else FontStyle.Normal,
                                                textDecoration = if (textFormat.isUnderline) TextDecoration.Underline else TextDecoration.None
                                            )
                                        ) {
                                            append(text.text.substring(end))
                                        }
                                    }
                                    TransformedText(annotatedString, OffsetMapping.Identity)
                                },
                                decorationBox = { innerTextField ->
                                    Box(
                                        modifier = Modifier.fillMaxSize(),
                                        contentAlignment = Alignment.TopStart
                                    ) {
                                        if (textFieldValue.text.isEmpty()) {
                                            Text(
                                                "Tulis catatan di sini...",
                                                color = onSurfaceVariantColor.copy(alpha = 0.5f),
                                                fontSize = 14.sp
                                            )
                                        }
                                        innerTextField()
                                    }
                                }
                            )
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        if (attachments.isNotEmpty()) {
                            Text(
                                text = "Lampiran (${attachments.size})",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Medium,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Spacer(modifier = Modifier.height(8.dp))

                            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                items(attachments) { attachment ->
                                    Box(
                                        modifier = Modifier
                                            .size(80.dp)
                                            .border(1.dp, Color.Gray, RoundedCornerShape(8.dp))
                                            .clickable { showFullImage = attachment }
                                    ) {
                                        Image(
                                            painter = rememberAsyncImagePainter(model = attachment),
                                            contentDescription = "Attachment",
                                            modifier = Modifier.fillMaxSize(),
                                            contentScale = ContentScale.Crop
                                        )
                                        IconButton(
                                            onClick = { attachments = attachments.filter { it != attachment } },
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

                        Text(
                            text = "Label",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(8.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            if (labels.isNotEmpty()) {
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
                            } else {
                                Spacer(modifier = Modifier.weight(1f))
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
                                    onValueChange = { if (it.length <= 15) currentLabel = it },
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
                                color = onSurfaceVariantColor
                            )
                        }

                        if (errorMessage != null) {
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = errorMessage ?: "",
                                color = MaterialTheme.colorScheme.error,
                                fontSize = 12.sp
                            )
                        }

                        Spacer(modifier = Modifier.height(16.dp))
                    }

                    // Bottom Actions Fixed
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        color = Color.White,
                        shadowElevation = 8.dp
                    ) {
                        Row(
                            modifier = Modifier.padding(24.dp),
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
                                        textFieldValue.text.isBlank() -> {
                                            errorMessage = "Isi catatan harus diisi"
                                            return@Button
                                        }
                                    }

                                    val contentPreview = textFieldValue.text.take(100) + if (textFieldValue.text.length > 100) "..." else ""

                                    val note = NoteDto(
                                        id = existingNote?.id ?: "",
                                        title = title,
                                        content = textFieldValue.text,
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
    var showFullImage by remember { mutableStateOf<String?>(null) }
    // ✅ FIXED: Track favorite state dari note prop
    val isFavorite = note.isFavorite

    if (showFullImage != null) {
        Dialog(onDismissRequest = { showFullImage = null }) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black)
                    .clickable { showFullImage = null },
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painter = rememberAsyncImagePainter(model = showFullImage),
                    contentDescription = "Full Image",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Fit
                )
                IconButton(
                    onClick = { showFullImage = null },
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(16.dp)
                        .background(Color.Black.copy(alpha = 0.5f), CircleShape)
                ) {
                    Icon(Icons.Filled.Close, contentDescription = "Close", tint = Color.White)
                }
            }
        }
    }

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
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = note.title,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.weight(1f)
                    )

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        // ✅ FIXED: Langsung pakai state dari prop
                        IconButton(onClick = onToggleFavorite) {
                            Icon(
                                imageVector = if (isFavorite) Icons.Filled.Star else Icons.Filled.StarBorder,
                                contentDescription = "Favorit",
                                tint = if (isFavorite) Color(0xFFFFC107) else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        IconButton(onClick = onEdit) {
                            Icon(Icons.Filled.Edit, contentDescription = "Edit")
                        }
                        IconButton(onClick = onDelete) {
                            Icon(
                                Icons.Filled.Delete,
                                contentDescription = "Hapus",
                                tint = MaterialTheme.colorScheme.error
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                if (note.labels.isNotEmpty()) {
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(note.labels) { label ->
                            AssistChip(onClick = {}, label = { Text(label, fontSize = 12.sp) })
                        }
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                }

                Divider()
                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = note.content,
                    fontSize = 14.sp,
                    lineHeight = 20.sp,
                    color = MaterialTheme.colorScheme.onSurface
                )

                Spacer(modifier = Modifier.height(16.dp))

                if (note.attachments.isNotEmpty()) {
                    Text(
                        text = "Lampiran",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(note.attachments) { attachment ->
                            Box(
                                modifier = Modifier
                                    .size(120.dp)
                                    .border(1.dp, Color.Gray, RoundedCornerShape(8.dp))
                                    .clickable { showFullImage = attachment }
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