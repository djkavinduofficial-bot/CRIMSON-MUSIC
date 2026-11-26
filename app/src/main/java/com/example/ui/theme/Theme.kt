package com.example.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp

private val CrimsonDarkColorScheme = darkColorScheme(
    primary = CrimsonNeon,
    onPrimary = CrimsonWhite,
    primaryContainer = CrimsonCardElevated,
    onPrimaryContainer = CrimsonSoftPink,
    secondary = CrimsonGlow,
    onSecondary = CrimsonWhite,
    secondaryContainer = CrimsonCardSurface,
    onSecondaryContainer = CrimsonRoseText,
    tertiary = CrimsonSoftPink,
    onTertiary = CrimsonDeepVoid,
    background = CrimsonDeepVoid,
    onBackground = CrimsonWhite,
    surface = CrimsonSurfaceBase,
    onSurface = CrimsonWhite,
    surfaceVariant = CrimsonCardSurface,
    onSurfaceVariant = CrimsonSubtitle,
    surfaceContainer = CrimsonCardSurface,
    surfaceContainerHigh = CrimsonCardElevated,
    outline = CrimsonLineBorder,
    outlineVariant = CrimsonArcTrack
)

private val CrimsonShapes = Shapes(
    small = RoundedCornerShape(12.dp),
    medium = RoundedCornerShape(18.dp),
    large = RoundedCornerShape(24.dp),
    extraLarge = RoundedCornerShape(99.dp)
)

@Composable
fun CrimsonMusicTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = CrimsonDarkColorScheme,
        typography = CrimsonTypography,
        shapes = CrimsonShapes,
        content = content
    )
}
