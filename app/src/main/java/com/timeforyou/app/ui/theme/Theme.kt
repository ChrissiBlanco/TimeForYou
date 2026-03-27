package com.timeforyou.app.ui.theme

import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Brush

private val DarkColors = darkColorScheme(
    primary = WellnessPrimary,
    onPrimary = WellnessDeepBackground,
    primaryContainer = WellnessSurfaceBright,
    onPrimaryContainer = WellnessOnSurface,
    secondary = WellnessSecondary,
    onSecondary = WellnessDeepBackground,
    tertiary = WellnessGlow,
    onTertiary = WellnessDeepBackground,
    background = WellnessGradientBottom,
    onBackground = WellnessOnSurface,
    surface = WellnessSurface,
    onSurface = WellnessOnSurface,
    surfaceVariant = WellnessSurfaceBright,
    onSurfaceVariant = WellnessOnSurfaceMuted,
    outline = WellnessOnSurfaceMuted,
)

/** Dark-first wellness theme; light scheme can be added later. */
@Composable
fun TimeForYouTheme(content: @Composable () -> Unit) {
    val colors: ColorScheme = DarkColors
    MaterialTheme(
        colorScheme = colors,
        typography = TimeForYouTypography,
        shapes = TimeForYouShapes,
        content = content,
    )
}

object WellnessBackgroundBrushes {
    val screenGradientVertical: Brush = Brush.verticalGradient(
        colors = listOf(
            WellnessGradientTop,
            WellnessGradientBottom,
        ),
    )

    val cardSoftGlow: Brush = Brush.verticalGradient(
        colors = listOf(
            WellnessSurfaceBright.copy(alpha = 0.35f),
            WellnessSurface.copy(alpha = 0.95f),
        ),
    )
}
