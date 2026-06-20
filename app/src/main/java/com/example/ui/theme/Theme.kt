package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val GeometricBalanceColorScheme = lightColorScheme(
    primary = GeoPrimary,
    onPrimary = Color.White,
    primaryContainer = GeoPrimaryContainer,
    onPrimaryContainer = GeoOnPrimaryContainer,
    secondary = GeoPrimary,
    onSecondary = Color.White,
    tertiary = GeoSunsetTertiary,
    background = GeoBackground,
    surface = GeoSurface,
    surfaceVariant = GeoSurfaceVariant,
    onBackground = GeoTextPrimary,
    onSurface = GeoTextPrimary,
    onSurfaceVariant = GeoTextMuted,
    outline = GeoBorder
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = false, // Force Geometric Balance style light-tint backdrop by default
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit,
) {
    // We strictly use Geometric Balance Color Scheme to capture the requested palette!
    val colorScheme = GeometricBalanceColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
