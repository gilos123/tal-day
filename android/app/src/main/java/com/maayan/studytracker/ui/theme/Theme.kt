package com.maayan.studytracker.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

/**
 * Root theme for the app. Applies the Streak palette, Nunito typography, and chunky
 * shape tokens. Honors a user preference for Material You dynamic colors (off by
 * default; togglable from Settings) — when off we force the brand palette so the
 * app looks consistent across every device.
 */
@Composable
fun MaayanTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    useDynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val colors = when {
        useDynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val ctx = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(ctx) else dynamicLightColorScheme(ctx)
        }
        darkTheme -> StreakDarkColors
        else -> StreakLightColors
    }
    MaterialTheme(
        colorScheme = colors,
        typography = StreakTypography,
        shapes = StreakShapes,
        content = content
    )
}
