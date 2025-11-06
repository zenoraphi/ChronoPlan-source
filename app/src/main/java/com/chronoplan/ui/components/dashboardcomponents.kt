package com.chronoplan.ui.components // Pastikan package ini benar

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.chronoplan.ui.home.PieSlice // Import PieSlice dari package home
import com.chronoplan.R

// --- Komponen Bersama (Pindahan dari HomeScreen) ---

// Komponen Template Kartu Putih
@Composable
fun DashboardCard(
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        color = Color.White,
        shadowElevation = 4.dp
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            content = content
        )
    }
}

// Komponen untuk satu baris item jadwal di Home
@Composable
fun ScheduleItem(iconRes: Int, text: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(
            painter = painterResource(id = iconRes),
            contentDescription = null,
            modifier = Modifier.size(24.dp),
            tint = MaterialTheme.colorScheme.primary // Warna ikon
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(text = text, fontSize = 14.sp, maxLines = 1) // Batasi 1 baris
    }
}

// Komponen Pie Chart
@Composable
fun PieChart(
    size: Dp = 100.dp,
    thickness: Dp = 12.dp,
    slices: List<PieSlice> // Terima data PieSlice
) {
    Box(contentAlignment = Alignment.Center) {
        Canvas(
            modifier = Modifier.size(size)
        ) {
            var startAngle = -90f
            slices.forEach { slice ->
                val sweepAngle: Float = 360f * slice.percentage
                drawArc(
                    color = slice.color,
                    startAngle = startAngle,
                    sweepAngle = sweepAngle - 4f, // Celah antar slice
                    useCenter = false,
                    style = Stroke(
                        width = thickness.toPx(),
                        cap = StrokeCap.Butt
                    )
                )
                startAngle += sweepAngle
            }
        }
    }
}

// Komponen Legenda Pie Chart
@Composable
fun PieChartLegend(slices: List<PieSlice>) {
    Column(
        verticalArrangement = Arrangement.spacedBy(4.dp),
        horizontalAlignment = Alignment.Start // Rata kiri
    ) {
        slices.forEach { slice ->
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box( // Dot warna
                    modifier = Modifier
                        .size(8.dp)
                        .background(slice.color, CircleShape)
                )
                Spacer(modifier = Modifier.width(4.dp))
                // Label
                Text(text = slice.label, fontSize = 10.sp, color = Color.Gray)
            }
        }
    }
}