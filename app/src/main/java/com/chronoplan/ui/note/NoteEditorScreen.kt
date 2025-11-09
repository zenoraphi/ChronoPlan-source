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
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import coil.compose.rememberAsyncImagePainter
import com.chronoplan.data.model.NoteDto

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NoteEditorScreen(
    existingNote: NoteDto? = null,
    onBack: () -> Unit,
    onSave: (NoteDto) -> Unit,
    onUploadImage: (Uri) -> Unit = {}
) {
    var title by remember { mutableStateOf(existingNote?.title ?: "") }
    var content by remember { mutableStateOf(existingNote?.content ?: "") }
    var labels by remember { mutableStateOf(existingNote?.labels ?: emptyList()) }
    var attachments by remember { mutableStateOf(existingNote?.attachments ?: emptyList()) }
    var currentLabel by remember { mutableStateOf("") }
    var showLabelInput by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var showFullImage by remember { mutableStateOf<String?>(null) }
    var textFormat by remember { mutableStateOf(TextFormat()) }

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

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = if (existingNote == null) "Buat Catatan" else "Edit Catatan",
                        color = Color(0xFF000000),
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Kembali", tint = Color(0xFF000000))
                    }
                },
                actions = {
                    IconButton(
                        onClick = {
                            when {
                                title.isBlank() -> {
                                    errorMessage = "Judul harus diisi"
                                    return@IconButton
                                }
                                content.isBlank() -> {
                                    errorMessage = "Isi catatan harus diisi"
                                    return@IconButton
                                }
                            }

                            val contentPreview = content.take(100) + if (content.length > 100) "..." else ""
                            val note = NoteDto(
                                id = existingNote?.id ?: "",
                                title = title,
                                content = content,
                                contentPreview = contentPreview,
                                labels = labels,
                                attachments = attachments,
                                isFavorite = existingNote?.isFavorite ?: false,
                                createdAt = existingNote?.createdAt ?: System.currentTimeMillis(),
                                updatedAt = System.currentTimeMillis()
                            )
                            onSave(note)
                            onBack()
                        }
                    ) {
                        Icon(Icons.Filled.Check, contentDescription = "Simpan", tint = Color(0xFF1976D2))
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.White
                )
            )
        },
        containerColor = Color.White
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp)
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            // Title
            BasicTextField(
                value = title,
                onValueChange = { title = it },
                modifier = Modifier.fillMaxWidth(),
                textStyle = LocalTextStyle.current.copy(
                    color = Color(0xFF000000),
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold
                ),
                cursorBrush = SolidColor(Color(0xFF1976D2)),
                decorationBox = { innerTextField ->
                    if (title.isEmpty()) {
                        Text(
                            "Judul...",
                            color = Color(0xFF9E9E9E),
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    innerTextField()
                }
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Toolbar
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(8.dp),
                color = Color(0xFFF5F5F5)
            ) {
                Row(
                    modifier = Modifier.padding(8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        IconToggleButton(
                            checked = textFormat.isBold,
                            onCheckedChange = { textFormat = textFormat.copy(isBold = it) }
                        ) {
                            Icon(
                                Icons.Filled.FormatBold,
                                contentDescription = "Bold",
                                tint = if (textFormat.isBold) Color(0xFF1976D2) else Color(0xFF757575)
                            )
                        }
                        IconToggleButton(
                            checked = textFormat.isItalic,
                            onCheckedChange = { textFormat = textFormat.copy(isItalic = it) }
                        ) {
                            Icon(
                                Icons.Filled.FormatItalic,
                                contentDescription = "Italic",
                                tint = if (textFormat.isItalic) Color(0xFF1976D2) else Color(0xFF757575)
                            )
                        }
                        IconToggleButton(
                            checked = textFormat.isUnderline,
                            onCheckedChange = { textFormat = textFormat.copy(isUnderline = it) }
                        ) {
                            Icon(
                                Icons.Filled.FormatUnderlined,
                                contentDescription = "Underline",
                                tint = if (textFormat.isUnderline) Color(0xFF1976D2) else Color(0xFF757575)
                            )
                        }
                    }
                    IconButton(onClick = { imagePickerLauncher.launch("image/*") }) {
                        Icon(Icons.Filled.Image, contentDescription = "Tambah Gambar", tint = Color(0xFF757575))
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Content
            BasicTextField(
                value = content,
                onValueChange = { content = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 400.dp),
                textStyle = LocalTextStyle.current.copy(
                    color = Color(0xFF000000),
                    fontSize = 16.sp,
                    fontWeight = if (textFormat.isBold) FontWeight.Bold else FontWeight.Normal,
                    fontStyle = if (textFormat.isItalic) FontStyle.Italic else FontStyle.Normal,
                    textDecoration = if (textFormat.isUnderline) TextDecoration.Underline else TextDecoration.None,
                    lineHeight = 24.sp
                ),
                cursorBrush = SolidColor(Color(0xFF1976D2)),
                decorationBox = { innerTextField ->
                    if (content.isEmpty()) {
                        Text(
                            "Tulis catatan di sini...",
                            color = Color(0xFF9E9E9E),
                            fontSize = 16.sp
                        )
                    }
                    innerTextField()
                }
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Attachments
            if (attachments.isNotEmpty()) {
                Text(
                    text = "Lampiran (${attachments.size})",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color(0xFF000000)
                )
                Spacer(modifier = Modifier.height(8.dp))

                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(attachments) { attachment ->
                        Box(
                            modifier = Modifier
                                .size(80.dp)
                                .border(1.dp, Color(0xFF9E9E9E), RoundedCornerShape(8.dp))
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
                Spacer(modifier = Modifier.height(16.dp))
            }

            // Labels
            Text(
                text = "Label",
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                color = Color(0xFF000000)
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
                                label = { Text(label, fontSize = 12.sp, color = Color(0xFF000000)) },
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
                    Icon(Icons.Filled.Add, contentDescription = "Tambah Label", tint = Color(0xFF1976D2))
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
                        label = { Text("Nama Label", color = Color(0xFF757575)) },
                        modifier = Modifier.weight(1f),
                        singleLine = true,
                        colors = TextFieldDefaults.outlinedTextFieldColors(
                            focusedTextColor = Color(0xFF000000),
                            unfocusedTextColor = Color(0xFF000000),
                            cursorColor = Color(0xFF1976D2)
                        )
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
                    color = Color(0xFF757575)
                )
            }

            if (errorMessage != null) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = errorMessage ?: "",
                    color = Color(0xFFC62828),
                    fontSize = 12.sp
                )
            }

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}