package com.chesstutor.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
    primary = ChessGreenPrimary,
    onPrimary = Color.White,
    primaryContainer = ChessGreenDark,
    onPrimaryContainer = Color.White,
    secondary = ChessYellowAccent,
    onSecondary = Color.Black,
    background = ChessDarkBg,
    onBackground = ChessTextPrimary,
    surface = ChessCardDark,
    onSurface = ChessTextPrimary,
    surfaceVariant = ChessCardElevated,
    onSurfaceVariant = ChessTextSecondary,
    outline = ChessCardBorder
)

@Composable
fun ChessTutorTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = DarkColorScheme,
        typography = ChessTypography,
        content = content
    )
}

