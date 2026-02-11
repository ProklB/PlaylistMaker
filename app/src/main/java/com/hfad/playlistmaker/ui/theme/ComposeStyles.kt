package com.hfad.playlistmaker.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.hfad.playlistmaker.R

// Цвета дневной темы
val LightColorScheme = lightColorScheme(
    primary = Color(0xFF3772E7),
    secondary = Color(0xFF1A1B22),
    surface = Color(0xFFFFFFFF),
    background = Color(0xFFFFFFFF),
    onSurface = Color(0xFF1A1B22),
    surfaceVariant = Color(0xFFE6E8EB),

    // Свитч
    primaryContainer = Color(0xFFE6E8EB),
    onPrimaryContainer = Color(0xFFAEAFB4),
    inversePrimary = Color(0xFF3772E7),
    inverseSurface = Color(0xFF9FBBF3)
)

// Цвета ночной темы
val DarkColorScheme = darkColorScheme(
    primary = Color(0xFF3772E7),
    secondary = Color(0xFFFFFFFF),
    surface = Color(0xFF1A1B22),
    background = Color(0xFF1A1B22),
    onSurface = Color(0xFFFFFFFF),
    surfaceVariant = Color(0xFFE6E8EB),

    // Свитч
    primaryContainer = Color(0xFF9FBBF3),
    onPrimaryContainer = Color(0xFF3772E7),
    inversePrimary = Color(0xFF3772E7),
    inverseSurface = Color(0xFF9FBBF3)
)

@Composable
fun PlaylistMakerTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) {
        DarkColorScheme
    } else {
        LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = MaterialTheme.typography,
        content = content
    )
}

fun MyTitleTextStyle(): TextStyle {
    return TextStyle(
        fontSize = 22.sp,
        fontWeight = FontWeight.Bold,
        fontFamily = FontFamily(Font(R.font.ys_display_bold))
    )
}

fun MaterialTextViewStyle(): TextStyle {
    return TextStyle(
        fontSize = 16.sp,
        fontFamily = FontFamily(Font(R.font.ys_display_medium)),
    )
}