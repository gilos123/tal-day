package com.maayan.studytracker.ui.common

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle

/**
 * Two rounded pills (XP + Level, Streak) intended for placement inside a TopAppBar.
 * Values are reactively derived from [GamificationViewModel].
 */
@Composable
fun GamificationBadges(
    modifier: Modifier = Modifier,
    viewModel: GamificationViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Pill(
            text = "✨ ${formatXp(state.totalXp)} · LVL ${state.level}",
            bg = MaterialTheme.colorScheme.primaryContainer,
            fg = MaterialTheme.colorScheme.onPrimaryContainer
        )
        Pill(
            text = "🔥 ${state.streakDays}",
            bg = MaterialTheme.colorScheme.tertiaryContainer,
            fg = MaterialTheme.colorScheme.onTertiaryContainer
        )
    }
}

@Composable
private fun Pill(
    text: String,
    bg: androidx.compose.ui.graphics.Color,
    fg: androidx.compose.ui.graphics.Color
) {
    Text(
        text = text,
        style = MaterialTheme.typography.labelMedium,
        color = fg,
        modifier = Modifier
            .clip(RoundedCornerShape(12.dp))
            .background(bg)
            .padding(horizontal = 10.dp, vertical = 6.dp)
    )
}

private fun formatXp(xp: Int): String = when {
    xp < 1_000 -> xp.toString()
    xp < 10_000 -> "%.1fk".format(xp / 1000.0)
    else -> "${xp / 1000}k"
}
