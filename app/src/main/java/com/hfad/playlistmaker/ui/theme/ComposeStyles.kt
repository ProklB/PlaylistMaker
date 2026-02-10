package com.hfad.playlistmaker.ui.theme

import androidx.compose.material3.lightColorScheme
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.hfad.playlistmaker.R

val LightColorScheme = lightColorScheme(
    primary = Color(0xFF3772E7),
    secondary = Color(0xFF1A1B22),
    surface = Color(0xFFFFFFFF),
    background = Color(0xFFFFFFFF),
    onSurface = Color(0xFF1A1B22),
    surfaceVariant = Color(0xFFE6E8EB)
)

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