package com.xeylabs.doit.ui.theme

import androidx.compose.material3.darkColorScheme
import androidx.compose.ui.graphics.Color

val Emerald = Color(0xFF4EDEA3)
val EmeraldAccent = Emerald
val EmeraldStrong = Color(0xFF10B981)
val EmeraldDeep = Color(0xFF059669)
val EmeraldLight = Color(0xFF6EE7B7)
val EmeraldPale = Color(0xFFA7F3D0)
val OnEmerald = Color(0xFF003824)
val BackgroundDark = Color(0xFF0E141C)
val SurfaceLowest = Color(0xFF090F16)
val SurfaceDark = Color(0xFF161C24)
val SurfaceContainer = Color(0xFF1A2028)
val SurfaceHigh = Color(0xFF242A33)
val SurfaceHighest = Color(0xFF2F353E)
val SurfaceVariantDark = SurfaceHigh
val TextPrimary = Color(0xFFDDE3EE)
val TextMuted = Color(0xFFBBCABF)
val OutlineMuted = Color(0xFF86948A)
val IncomeGreen = Emerald
val ExpenseRed = Color(0xFFFFB2B7)
val OutflowStrong = Color(0xFFF43F5E)
val WarningAmber = Color(0xFFFBBF24)
val CategoryGray = Color(0xFF86948A)
val HeroEmerald = Color(0xFF10231B)
val CardBorder = Color(0xFF1E293B)

val categoryColors: Map<String, Color> = mapOf(
    "BELANJA" to EmeraldStrong,
    "MAKAN" to Color(0xFF34D399),
    "TRANSPORT" to EmeraldLight,
    "DARURAT" to WarningAmber,
    "ROKOK" to Color(0xFFFB923C),
    "MINUMAN" to Color(0xFF38BDF8),
    "HIBURAN" to Color(0xFFA78BFA),
    "KESEHATAN" to Color(0xFFF43F5E),
    "TAGIHAN" to Color(0xFFF472B6),
    "PAYLATER_UTANG" to Color(0xFF60A5FA),
    "PIUTANG" to Color(0xFFA3E635),
    "LAIN_KELUAR" to CategoryGray
)

fun categoryColor(name: String): Color = categoryColors[name] ?: CategoryGray

val DoitColorScheme = darkColorScheme(
    primary = Emerald,
    onPrimary = OnEmerald,
    primaryContainer = EmeraldStrong,
    onPrimaryContainer = Color(0xFF00422B),
    secondary = EmeraldLight,
    background = BackgroundDark,
    onBackground = TextPrimary,
    surface = SurfaceDark,
    onSurface = TextPrimary,
    surfaceVariant = SurfaceHigh,
    onSurfaceVariant = TextMuted,
    outline = OutlineMuted,
    error = ExpenseRed
)
