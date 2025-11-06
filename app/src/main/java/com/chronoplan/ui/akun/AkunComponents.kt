package com.chronoplan.ui.akun

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * Kartu informasi profil pengguna.
 */
@Composable
fun ProfileInfoCard(
    username: String,
    level: String,
    modifier: Modifier = Modifier,
    colorBlueSoft: Color = Color(0xFFD9EAFD),
    colorGraySoft: Color = Color(0xFF6A786A)
) {
    Box(
        modifier = modifier
            .height(63.dp)
            .background(colorBlueSoft, RoundedCornerShape(15.dp))
            .border(1.dp, colorGraySoft.copy(alpha = 0.3f), RoundedCornerShape(15.dp)),
        contentAlignment = Alignment.CenterStart
    ) {
        Column(modifier = Modifier.padding(horizontal = 16.dp)) {
            Text(
                text = username.ifEmpty { "Guest" },
                color = colorGraySoft,
                fontSize = 18.sp,
                fontWeight = FontWeight.ExtraBold
            )
            Text(
                text = "Level: ${level.ifEmpty { "Unknown" }}",
                color = colorGraySoft.copy(alpha = 0.7f),
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

/**
 * Placeholder sederhana untuk grid pencapaian user.
 */
@Composable
fun AchievementGridPlaceholder(
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(12.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Belum ada pencapaian 😅",
            color = Color(0xFF428F9C),
            fontSize = 16.sp,
            fontWeight = FontWeight.Medium
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "Kerjakan aktivitas dulu untuk membuka achievement!",
            color = Color.Gray,
            fontSize = 13.sp
        )
    }
}
