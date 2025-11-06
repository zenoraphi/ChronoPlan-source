package com.chronoplan.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.graphics.Color

@Composable
fun AuraAnimatedAvatar(
    modifier: Modifier = Modifier
) {
    // Very small placeholder "aura" using Canvas.
    Canvas(modifier = modifier.size(120.dp)) {
        // simple concentric circles as placeholder
        val centerX = size.width / 2f
        val centerY = size.height / 2f
        drawCircle(color = Color(0xFFBFE9FF), radius = size.minDimension / 2f)
        drawCircle(color = Color(0x80BFE9FF), radius = size.minDimension / 2.5f)
        drawCircle(color = Color(0x40BFE9FF), radius = size.minDimension / 3.2f)
    }
}
