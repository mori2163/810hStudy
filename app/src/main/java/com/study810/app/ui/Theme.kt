package com.study810.app.ui

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

val Ink = Color(0xFF000000)
val Panel = Color(0xFF111111)
val Paper = Color(0xFFFFFFFF)

private val StudyColors = darkColorScheme(
    primary = Paper,
    onPrimary = Ink,
    primaryContainer = Panel,
    onPrimaryContainer = Paper,
    secondary = Paper,
    onSecondary = Ink,
    background = Ink,
    onBackground = Paper,
    surface = Ink,
    onSurface = Paper,
    surfaceVariant = Panel,
    onSurfaceVariant = Color(0xFFB0B0B0),
    outline = Color(0xFF3A3A3A),
    error = Color(0xFFFF6B6B),
)

@Composable
fun Study810Theme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = StudyColors,
        content = content,
    )
}
