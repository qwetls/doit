package com.xeylabs.doit.ui.theme

import androidx.compose.material3.darkColorScheme
import androidx.compose.ui.graphics.Color

val VioletAccent = Color(0xFF8B5CF6)
val VioletAccentDark = Color(0xFF7C3AED)
val BackgroundDark = Color(0xFF0B0B0F)
val SurfaceDark = Color(0xFF15151C)
val SurfaceVariantDark = Color(0xFF1E1E28)
val TextPrimary = Color(0xFFF5F5F7)
val TextMuted = Color(0xFF9A9AA5)
val IncomeGreen = Color(0xFF4ADE80)
val ExpenseRed = Color(0xFFF87171)
val WarningAmber = Color(0xFFFBBF24)
val CategoryBlue = Color(0xFF60A5FA)
val CategoryGray = Color(0xFF9A9AA5)
val HeroViolet = Color(0xFF1B1530)
val CardBorder = Color(0x14FFFFFF)

val categoryColors: Map<String, Color> = mapOf(
    "BELANJA" to VioletAccent,
    "MAKAN" to IncomeGreen,
    "TRANSPORT" to CategoryBlue,
    "DARURAT" to WarningAmber,
    "LAIN_KELUAR" to CategoryGray
)

fun categoryColor(name: String): Color = categoryColors[name] ?: CategoryGray

val DoitColorScheme = darkColorScheme(
    primary = VioletAccent,
    onPrimary = Color.White,
    secondary = VioletAccentDark,
    background = BackgroundDark,
    onBackground = TextPrimary,
    surface = SurfaceDark,
    onSurface = TextPrimary,
    surfaceVariant = SurfaceVariantDark,
    onSurfaceVariant = TextMuted,
    error = ExpenseRed
)
