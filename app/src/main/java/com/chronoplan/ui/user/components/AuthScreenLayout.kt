package com.chronoplan.ui.user.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.background
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import com.chronoplan.R
import androidx.compose.ui.unit.dp

@Composable
fun AuthScreenLayout(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFFF0F5FF)) // Background biru muda solid
    ) {
        // Border Atas
        Image(
            painter = painterResource(id = R.drawable.ic_border),
            contentDescription = "Top Border",
            modifier = Modifier
                .width(200.dp) // Atur lebar border
                .height(150.dp) // Atur tinggi border
                .align(Alignment.TopEnd), // 1. Tempel di pojok KANAN ATAS
            contentScale = ContentScale.FillBounds
        )

        // Border Bawah
        Image(
            painter = painterResource(id = R.drawable.ic_border),
            contentDescription = "Bottom Border",
            modifier = Modifier
                .width(200.dp) // Atur lebar border
                .height(150.dp) // Atur tinggi border
                .align(Alignment.BottomStart) // 2. Tempel di pojok KIRI BAWAH
                .rotate(180f),
            contentScale = ContentScale.FillBounds
        )

        // Kontenmu (Logo, Teks, Container) akan muncul di atas ini
        content()
    }
}