package com.example.ui.theme

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Liquid Glass Material tokens and modifiers mimicking iOS 26+ refractive dynamic glass.
 */
object LiquidGlassTokens {
    // Ultra-crisp frosted glass base
    val GlassSurface = Color(0xFFFFFFFF)
    val GlassSurfaceUltra = Color(0xFFFFFFFF)
    val GlassSurfaceSubtle = Color(0xFFF8FAFC)
    val GlassSurfaceDark = Color(0xFF0F172A)

    // Specular highlight gradients with crisp visible contrast on light porcelain background
    val SpecularRimLight = Brush.verticalGradient(
        colors = listOf(
            Color(0xFFCBD5E1),
            Color(0xFFE2E8F0),
            Color(0xFFF1F5F9)
        )
    )

    val SpecularRimDark = Brush.verticalGradient(
        colors = listOf(
            Color(0xFF475569),
            Color(0xFF334155),
            Color(0xFF1E293B)
        )
    )

    val GlassPillBackground = Brush.verticalGradient(
        colors = listOf(
            Color(0xFFFFFFFF),
            Color(0xFFF8FAFC),
            Color(0xFFF1F5F9)
        )
    )

    val GlassShadowAmbient = Color(0x180F172A)
    val GlassShadowSpot = Color(0x220F172A)
}

/**
 * Applies a Liquid Glass style to any Composable:
 * - Ambient diffused shadow
 * - Pure white refractive surface
 * - Visible specular rim border
 */
fun Modifier.liquidGlass(
    shape: Shape = RoundedCornerShape(24.dp),
    elevation: Dp = 6.dp,
    surfaceColor: Color = LiquidGlassTokens.GlassSurface,
    rimBrush: Brush = LiquidGlassTokens.SpecularRimLight,
    borderWidth: Dp = 1.dp
): Modifier = this
    .shadow(
        elevation = elevation,
        shape = shape,
        ambientColor = LiquidGlassTokens.GlassShadowAmbient,
        spotColor = LiquidGlassTokens.GlassShadowSpot
    )
    .clip(shape)
    .background(surfaceColor)
    .border(borderWidth, rimBrush, shape)
