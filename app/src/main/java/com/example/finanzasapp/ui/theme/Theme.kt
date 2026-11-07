package com.example.finanzasapp.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val RyokuDarkScheme = darkColorScheme(
    primary = EmeraldAccent,
    onPrimary = Color.Black,
    primaryContainer = Color(0xFF063A2A),
    onPrimaryContainer = Color(0xFFA7F3D0),
    secondary = TerracottaAccent,
    onSecondary = Color.White,
    background = RyokuBlack,
    onBackground = InkWhite,
    surface = RyokuSurface,
    onSurface = InkWhite,
    surfaceVariant = RyokuSurfaceElevated,
    onSurfaceVariant = InkMuted,
    outline = RyokuCardBorder,
    error = CoralExpense,
    onError = Color.White
)

@Composable
fun FinanzasTheme(
    darkTheme: Boolean = true, // Ryoku es primordialmente Dark Mode
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = RyokuDarkScheme,
        typography = AppTypography,
        shapes = androidx.compose.material3.Shapes(
            small = AppShapes.Small,
            medium = AppShapes.Medium,
            large = AppShapes.Large
        ),
        content = content
    )
}