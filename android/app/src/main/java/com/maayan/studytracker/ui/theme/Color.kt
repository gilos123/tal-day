package com.maayan.studytracker.ui.theme

import androidx.compose.material3.ColorScheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.ui.graphics.Color

// ===== Streak palette (light) =====
private val StreakLimePrimary = Color(0xFF3BBA10)          // deeper lime: AA contrast on white
private val StreakLimePrimaryContainer = Color(0xFFE4FBD3)
private val StreakLimeOnPrimary = Color(0xFFFFFFFF)
private val StreakLimeOnPrimaryContainer = Color(0xFF0F3A00)

private val StreakCoralSecondary = Color(0xFFFF6B6B)
private val StreakCoralSecondaryContainer = Color(0xFFFFE2E2)
private val StreakCoralOnSecondary = Color(0xFFFFFFFF)
private val StreakCoralOnSecondaryContainer = Color(0xFF5C1F1F)

private val StreakFlameTertiary = Color(0xFFFF9F1C)        // streak flame
private val StreakFlameTertiaryContainer = Color(0xFFFFEED1)
private val StreakFlameOnTertiary = Color(0xFF3A2000)
private val StreakFlameOnTertiaryContainer = Color(0xFF4A2E00)

private val StreakSkyBackground = Color(0xFFF0F7FF)
private val StreakSurface = Color(0xFFFFFFFF)
private val StreakSurfaceVariant = Color(0xFFDCE8F5)
private val StreakOutline = Color(0xFFBED4E8)
private val StreakOutlineVariant = Color(0xFFD5E2EF)
private val StreakOnBackground = Color(0xFF14192A)
private val StreakOnSurface = Color(0xFF14192A)
private val StreakOnSurfaceVariant = Color(0xFF3F4E60)

// ===== Streak palette (dark) =====
private val StreakLimePrimaryDark = Color(0xFF7DE84A)
private val StreakLimePrimaryContainerDark = Color(0xFF2B5A10)
private val StreakLimeOnPrimaryDark = Color(0xFF0B1A00)
private val StreakLimeOnPrimaryContainerDark = Color(0xFFD4FFB8)

private val StreakCoralSecondaryDark = Color(0xFFFF8B8B)
private val StreakCoralSecondaryContainerDark = Color(0xFF5C1F1F)
private val StreakCoralOnSecondaryDark = Color(0xFF2B0000)
private val StreakCoralOnSecondaryContainerDark = Color(0xFFFFE2E2)

private val StreakFlameTertiaryDark = Color(0xFFFFB852)
private val StreakFlameTertiaryContainerDark = Color(0xFF5C3C00)
private val StreakFlameOnTertiaryDark = Color(0xFF2B1B00)
private val StreakFlameOnTertiaryContainerDark = Color(0xFFFFEED1)

private val StreakIndigoNightBackground = Color(0xFF121629)
private val StreakSurfaceDark = Color(0xFF1B2038)
private val StreakSurfaceVariantDark = Color(0xFF252B47)
private val StreakOutlineDark = Color(0xFF3D4B6A)
private val StreakOutlineVariantDark = Color(0xFF2B334C)
private val StreakOnBackgroundDark = Color(0xFFE4ECFA)
private val StreakOnSurfaceDark = Color(0xFFE4ECFA)
private val StreakOnSurfaceVariantDark = Color(0xFFB5C3D9)

val StreakLightColors: ColorScheme = lightColorScheme(
    primary = StreakLimePrimary,
    onPrimary = StreakLimeOnPrimary,
    primaryContainer = StreakLimePrimaryContainer,
    onPrimaryContainer = StreakLimeOnPrimaryContainer,
    secondary = StreakCoralSecondary,
    onSecondary = StreakCoralOnSecondary,
    secondaryContainer = StreakCoralSecondaryContainer,
    onSecondaryContainer = StreakCoralOnSecondaryContainer,
    tertiary = StreakFlameTertiary,
    onTertiary = StreakFlameOnTertiary,
    tertiaryContainer = StreakFlameTertiaryContainer,
    onTertiaryContainer = StreakFlameOnTertiaryContainer,
    background = StreakSkyBackground,
    onBackground = StreakOnBackground,
    surface = StreakSurface,
    onSurface = StreakOnSurface,
    surfaceVariant = StreakSurfaceVariant,
    onSurfaceVariant = StreakOnSurfaceVariant,
    outline = StreakOutline,
    outlineVariant = StreakOutlineVariant
)

val StreakDarkColors: ColorScheme = darkColorScheme(
    primary = StreakLimePrimaryDark,
    onPrimary = StreakLimeOnPrimaryDark,
    primaryContainer = StreakLimePrimaryContainerDark,
    onPrimaryContainer = StreakLimeOnPrimaryContainerDark,
    secondary = StreakCoralSecondaryDark,
    onSecondary = StreakCoralOnSecondaryDark,
    secondaryContainer = StreakCoralSecondaryContainerDark,
    onSecondaryContainer = StreakCoralOnSecondaryContainerDark,
    tertiary = StreakFlameTertiaryDark,
    onTertiary = StreakFlameOnTertiaryDark,
    tertiaryContainer = StreakFlameTertiaryContainerDark,
    onTertiaryContainer = StreakFlameOnTertiaryContainerDark,
    background = StreakIndigoNightBackground,
    onBackground = StreakOnBackgroundDark,
    surface = StreakSurfaceDark,
    onSurface = StreakOnSurfaceDark,
    surfaceVariant = StreakSurfaceVariantDark,
    onSurfaceVariant = StreakOnSurfaceVariantDark,
    outline = StreakOutlineDark,
    outlineVariant = StreakOutlineVariantDark
)

/** 10 preset swatches for per-project color tags. Must stay in sync with ProjectColorPicker. */
val ProjectColorPresets: List<Color> = listOf(
    Color(0xFF5BE32A), // lime
    Color(0xFFFF6B6B), // coral
    Color(0xFFFF9F1C), // flame
    Color(0xFF4DB2FF), // sky
    Color(0xFF9C6BFF), // violet
    Color(0xFFEC4899), // rose
    Color(0xFF14B8A6), // teal
    Color(0xFFF59E0B), // amber
    Color(0xFFA855F7), // grape
    Color(0xFF64748B)  // graphite
)

val DefaultProjectColorHex: String = "#5BE32A"
