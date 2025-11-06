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


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NoteScreen(
    modifier: Modifier = Modifier,
    viewModel: NoteViewModel = viewModel(factory = AppViewModelFactory())
) {
    val uiState by viewModel.state.collectAsState()

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
                    NoteCardItem(note = note)
                }
            }
        }

        // FAB tambah catatan
        FloatingActionButton(
            onClick = {
                viewModel.addNote(
                    NoteDto(
                        title = "Catatan Baru",
                        content = "Isi catatan di sini",
                        contentPreview = "Isi catatan di sini..."
                    )
                )
            },
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
fun NoteCardItem(note: NoteDto) {
    Card(
        modifier = Modifier
            .width(179.dp)
            .height(166.dp)
            .clickable { /* TODO: Buka detail note */ },
        shape = RoundedCornerShape(15.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            Text(
                text = note.title,
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp,
                lineHeight = 16.sp,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                color = Color.Black,
                modifier = Modifier
                    .padding(top = 11.dp, start = 6.dp, end = 6.dp)
                    .fillMaxWidth()
            )

            Text(
                text = note.contentPreview,
                fontWeight = FontWeight.SemiBold,
                fontSize = 10.sp,
                lineHeight = 12.sp,
                maxLines = 3,
                overflow = TextOverflow.Ellipsis,
                color = Color(0xFF6A786A),
                modifier = Modifier
                    .padding(top = 45.dp, start = 6.dp, end = 6.dp)
                    .fillMaxWidth()
            )

            Row(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(start = 11.dp, bottom = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_calendar),
                    contentDescription = "Tanggal",
                    modifier = Modifier.size(14.dp),
                    tint = Color(0xFF6A786A)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = note.updatedAt.toString(),
                    fontWeight = FontWeight.Normal,
                    fontSize = 8.sp,
                    lineHeight = 9.sp,
                    color = Color(0xFF6A786A)
                )
            }
        }
    }
}
