package com.chronoplan.ui.components

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.layout.Box
import androidx.compose.ui.Alignment

/**
 * AnimatedCard
 * - content sekarang bertipe ColumnScope.() -> Unit supaya bisa memanfaatkan ColumnScope
 *   (mis. Modifier.weight, alignByBaseline, dll) dari dalam content.
 */
@Composable
fun AnimatedCard(
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit
) {
    var visible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { visible = true }

    AnimatedVisibility(
        visible = visible,
        enter = fadeIn(tween(400)) + slideInVertically { it / 3 },
        exit = fadeOut()
    ) {
        Card(
            modifier = modifier,
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                content = content // sekarang cocok: ColumnScope.() -> Unit
            )
        }
    }
}

/**
 * ScaleInAnimation
 * - animateFloatAsState but beri targetValue dinamis; kita mulai dari 0.95 -> 1f
 * - jika kamu ingin memicu animasi masuk hanya sekali, bisa tambahkan key atau state trigger
 */
@Composable
fun ScaleInAnimation(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    // mulai sedikit kecil lalu mengembang ke 1f
    var enabled by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { enabled = true }

    val target = if (enabled) 1f else 0.95f
    val scale by animateFloatAsState(
        targetValue = target,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        )
    )

    Box(
        modifier = modifier.scale(scale),
        contentAlignment = Alignment.Center
    ) {
        content()
    }
}

/**
 * PulseAnimation
 * - efek pulse berulang menggunakan rememberInfiniteTransition
 */
@Composable
fun PulseAnimation(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    val infiniteTransition = rememberInfiniteTransition()
    val scale by infiniteTransition.animateFloat(
        initialValue = 0.95f,
        targetValue = 1.05f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1000),
            repeatMode = RepeatMode.Reverse
        )
    )

    Box(
        modifier = modifier.scale(scale),
        contentAlignment = Alignment.Center
    ) {
        content()
    }
}
