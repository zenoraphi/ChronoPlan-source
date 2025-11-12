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
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import coil.compose.rememberAsyncImagePainter
import com.chronoplan.data.model.NoteDto

// ✅ Data class untuk menyimpan format range
data class FormatRange(
    val start: Int,
    val end: Int,
    val isBold: Boolean = false,
    val isItalic: Boolean = false,
    val isUnderline: Boolean = false
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NoteEditorScreen(
    existingNote: NoteDto? = null,
    onBack: () -> Unit,
    onSave: (NoteDto) -> Unit,
    onUploadImage: (Uri) -> Unit = {}
) {
    var title by remember { mutableStateOf(existingNote?.title ?: "") }
    var contentText by remember { mutableStateOf(existingNote?.content ?: "") }
    var textFieldValue by remember {
        mutableStateOf(TextFieldValue(text = existingNote?.content ?: ""))
    }
    var labels by remember { mutableStateOf(existingNote?.labels ?: emptyList()) }
    var attachments by remember { mutableStateOf(existingNote?.attachments ?: emptyList()) }
    var currentLabel by remember { mutableStateOf("") }
    var showLabelInput by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var showFullImage by remember { mutableStateOf<String?>(null) }

    // ✅ Track active formatting
    var isBoldActive by remember { mutableStateOf(false) }
    var isItalicActive by remember { mutableStateOf(false) }
    var isUnderlineActive by remember { mutableStateOf(false) }

    // ✅ Store formatting ranges
    var formatRanges by remember { mutableStateOf<List<FormatRange>>(emptyList()) }
    var numberedListCounter by remember { mutableStateOf(1) }

    // ✅ Image picker dengan handling proper
    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            // Upload dulu, tunggu URL-nya
            onUploadImage(it)
            // Simpan URI sementara, nanti diganti URL dari Firebase
            attachments = attachments + it.toString()
        }
    }

    // ✅ Function untuk apply formatting pada selected text
    fun applyFormatting(type: String) {
        val selection = textFieldValue.selection
        if (selection.start == selection.end) {
            // No selection, set flag untuk text berikutnya
            when (type) {
                "bold" -> isBoldActive = !isBoldActive
                "italic" -> isItalicActive = !isItalicActive
                "underline" -> isUnderlineActive = !isUnderlineActive
            }
        } else {
            // Ada selection, apply formatting
            val newRange = FormatRange(
                start = selection.start,
                end = selection.end,
                isBold = when(type) { "bold" -> true else -> false },
                isItalic = when(type) { "italic" -> true else -> false },
                isUnderline = when(type) { "underline" -> true else -> false }
            )
            formatRanges = formatRanges + newRange
        }
    }

    // ✅ Build annotated string dengan formatting
    val annotatedContent = remember(textFieldValue.text, formatRanges) {
        buildAnnotatedString {
            append(textFieldValue.text)

            formatRanges.forEach { range ->
                if (range.start < textFieldValue.text.length && range.end <= textFieldValue.text.length) {
                    addStyle(
                        style = SpanStyle(
                            fontWeight = if (range.isBold) FontWeight.Bold else null,
                            fontStyle = if (range.isItalic) FontStyle.Italic else null,
                            textDecoration = if (range.isUnderline) TextDecoration.Underline else null
                        ),
                        start = range.start,
                        end = range.end
                    )
                }
            }
        }
    }

    // ✅ Full Image Dialog
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
                                textFieldValue.text.isBlank() -> {
                                    errorMessage = "Isi catatan harus diisi"
                                    return@IconButton
                                }
                            }

                            val content = textFieldValue.text
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

            // ✅ Toolbar dengan state aktif
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(8.dp),
                color = Color(0xFFF5F5F5)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        // Bold
                        IconToggleButton(
                            checked = isBoldActive,
                            onCheckedChange = {
                                applyFormatting("bold")
                            }
                        ) {
                            Icon(
                                Icons.Filled.FormatBold,
                                contentDescription = "Bold",
                                tint = if (isBoldActive) Color(0xFF1976D2) else Color(0xFF757575)
                            )
                        }

                        // Italic
                        IconToggleButton(
                            checked = isItalicActive,
                            onCheckedChange = {
                                applyFormatting("italic")
                            }
                        ) {
                            Icon(
                                Icons.Filled.FormatItalic,
                                contentDescription = "Italic",
                                tint = if (isItalicActive) Color(0xFF1976D2) else Color(0xFF757575)
                            )
                        }

                        // Underline
                        IconToggleButton(
                            checked = isUnderlineActive,
                            onCheckedChange = {
                                applyFormatting("underline")
                            }
                        ) {
                            Icon(
                                Icons.Filled.FormatUnderlined,
                                contentDescription = "Underline",
                                tint = if (isUnderlineActive) Color(0xFF1976D2) else Color(0xFF757575)
                            )
                        }

                        // Bullet List
                        IconButton(
                            onClick = {
                                val selection = textFieldValue.selection
                                val newText = textFieldValue.text.substring(0, selection.start) +
                                        "• " +
                                        textFieldValue.text.substring(selection.start)
                                textFieldValue = TextFieldValue(
                                    text = newText,
                                    selection = TextRange(selection.start + 2)
                                )
                            }
                        ) {
                            Icon(
                                Icons.Filled.FormatListBulleted,
                                contentDescription = "Bullet List",
                                tint = Color(0xFF757575)
                            )
                        }

                        // Numbered List
                        IconButton(
                            onClick = {
                                val selection = textFieldValue.selection
                                val newText = textFieldValue.text.substring(0, selection.start) +
                                        "$numberedListCounter. " +
                                        textFieldValue.text.substring(selection.start)
                                textFieldValue = TextFieldValue(
                                    text = newText,
                                    selection = TextRange(selection.start + "$numberedListCounter. ".length)
                                )
                                numberedListCounter++
                            }
                        ) {
                            Icon(
                                Icons.Filled.FormatListNumbered,
                                contentDescription = "Numbered List",
                                tint = Color(0xFF757575)
                            )
                        }

                        // Indent
                        IconButton(
                            onClick = {
                                val selection = textFieldValue.selection
                                val newText = textFieldValue.text.substring(0, selection.start) +
                                        "    " +
                                        textFieldValue.text.substring(selection.start)
                                textFieldValue = TextFieldValue(
                                    text = newText,
                                    selection = TextRange(selection.start + 4)
                                )
                            }
                        ) {
                            Icon(
                                Icons.Filled.FormatIndentIncrease,
                                contentDescription = "Indent",
                                tint = Color(0xFF757575)
                            )
                        }
                    }

                    IconButton(onClick = { imagePickerLauncher.launch("image/*") }) {
                        Icon(Icons.Filled.Image, contentDescription = "Tambah Gambar", tint = Color(0xFF757575))
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // ✅ Content Editor dengan AnnotatedString
            BasicTextField(
                value = textFieldValue,
                onValueChange = { newValue ->
                    textFieldValue = newValue

                    // Apply active formatting ke karakter baru
                    if (newValue.text.length > textFieldValue.text.length &&
                        (isBoldActive || isItalicActive || isUnderlineActive)) {
                        val newCharStart = textFieldValue.text.length
                        val newCharEnd = newValue.text.length

                        if (newCharEnd > newCharStart) {
                            formatRanges = formatRanges + FormatRange(
                                start = newCharStart,
                                end = newCharEnd,
                                isBold = isBoldActive,
                                isItalic = isItalicActive,
                                isUnderline = isUnderlineActive
                            )
                        }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 400.dp)
                    .background(Color.White),
                textStyle = LocalTextStyle.current.copy(
                    color = Color(0xFF000000),
                    fontSize = 16.sp,
                    lineHeight = 24.sp
                ),
                cursorBrush = SolidColor(Color(0xFF1976D2)),
                decorationBox = { innerTextField ->
                    Box(modifier = Modifier.fillMaxWidth()) {
                        if (textFieldValue.text.isEmpty()) {
                            Text(
                                "Tulis catatan di sini...\n\n" +
                                        "Tips:\n" +
                                        "• Pilih teks → klik Bold/Italic/Underline\n" +
                                        "• Atau aktifkan tombol → ketik teks baru\n" +
                                        "• Bullet/Number untuk list\n" +
                                        "• Indent untuk paragraf menjorok",
                                color = Color(0xFF9E9E9E),
                                fontSize = 16.sp,
                                lineHeight = 24.sp
                            )
                        }

                        // Render text dengan formatting
                        Text(
                            text = annotatedContent,
                            modifier = Modifier.fillMaxWidth()
                        )

                        // Cursor overlay
                        innerTextField()
                    }
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

            // Labels (keep existing code)
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