package com.example.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

private val LightColorScheme = lightColorScheme(
    primary = DeepOnyx,
    onPrimary = PureWhite,
    primaryContainer = SurfaceSubtle,
    onPrimaryContainer = DeepOnyx,
    secondary = MutedSlate,
    onSecondary = PureWhite,
    secondaryContainer = SurfaceSubtle,
    onSecondaryContainer = DeepOnyx,
    background = PorcelainWhite,
    onBackground = DeepOnyx,
    surface = PureWhite,
    onSurface = DeepOnyx,
    surfaceVariant = SurfaceSubtle,
    onSurfaceVariant = MutedSlate,
    outline = SurfaceBorder,
    outlineVariant = SurfaceBorderHairline
)

private val DarkColorScheme = darkColorScheme(
    primary = PureWhite,
    onPrimary = DeepOnyx,
    primaryContainer = DarkSurface,
    onPrimaryContainer = PureWhite,
    secondary = LightSlate,
    onSecondary = DarkCanvas,
    secondaryContainer = DarkSurface,
    onSecondaryContainer = PureWhite,
    background = DarkCanvas,
    onBackground = PureWhite,
    surface = DarkSurface,
    onSurface = PureWhite,
    surfaceVariant = DarkSurface,
    onSurfaceVariant = LightSlate,
    outline = DarkBorder
)

@Composable
fun PulseTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false, // Preserve bespoke porcelain white aesthetic
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}

// Backward compatibility alias
@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    PulseTheme(darkTheme = darkTheme, dynamicColor = dynamicColor, content = content)
}
