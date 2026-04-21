package com.maayan.studytracker.ui.common

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.unit.dp
import com.maayan.studytracker.ui.theme.ProjectColorPresets

/**
 * Horizontal row of 10 color swatches (see [ProjectColorPresets]). The selected one
 * gets a contrasting ring so it's clearly highlighted.
 */
@Composable
fun ProjectColorPicker(
    selectedHex: String,
    onPick: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val ringColor = MaterialTheme.colorScheme.onSurface
    Row(
        modifier = modifier.horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        ProjectColorPresets.forEach { color ->
            val hex = color.toHex()
            val isSelected = hex.equals(selectedHex, ignoreCase = true)
            Box(
                modifier = Modifier
                    .size(28.dp)
                    .clip(CircleShape)
                    .background(color)
                    .let { if (isSelected) it.border(2.dp, ringColor, CircleShape) else it }
                    .clickable { onPick(hex) }
            )
        }
    }
}

/** Formats a Compose Color as `#RRGGBB` lowercase hex. */
fun Color.toHex(): String {
    val argb = toArgb()
    return String.format("#%06X", 0xFFFFFF and argb)
}

/** Parses a `#RRGGBB` (or `#AARRGGBB`) hex string into a Compose Color. */
fun hexToColor(hex: String, fallback: Color = Color(0xFF5BE32A)): Color =
    runCatching {
        val clean = hex.removePrefix("#")
        val argb = when (clean.length) {
            6 -> (0xFF000000L or clean.toLong(16)).toInt()
            8 -> clean.toLong(16).toInt()
            else -> return@runCatching fallback
        }
        Color(argb)
    }.getOrDefault(fallback)
