package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.DeepOnyx

private val AvatarColors = listOf(
    Color(0xFFE2E8F0),
    Color(0xFFFDE68A),
    Color(0xFFBAE6FD),
    Color(0xFFBBF7D0),
    Color(0xFFDDD6FE),
    Color(0xFFFED7AA),
    Color(0xFFFBCFE8)
)

@Composable
fun PulseAvatar(
    initials: String,
    seed: String,
    size: Dp = 46.dp,
    modifier: Modifier = Modifier
) {
    val colorIndex = (seed.hashCode() and 0x7FFFFFFF) % AvatarColors.size
    val bgColor = AvatarColors[colorIndex]

    Box(
        modifier = modifier
            .size(size)
            .clip(CircleShape)
            .background(bgColor),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = initials,
            fontSize = if (size >= 46.dp) 15.sp else 12.sp,
            fontWeight = FontWeight.Bold,
            color = DeepOnyx,
            letterSpacing = (-0.5).sp
        )
    }
}
