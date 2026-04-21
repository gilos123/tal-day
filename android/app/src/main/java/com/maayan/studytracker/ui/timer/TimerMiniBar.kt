package com.maayan.studytracker.ui.timer

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.maayan.studytracker.ui.common.Confetti
import kotlinx.coroutines.delay

/**
 * Persistent bottom bar that shows the currently running timer on every screen.
 * Hidden when no timer is active. Tapping the label expands to the full [TimerScreen];
 * the stop button ends the session in place.
 */
@Composable
fun TimerMiniBar(
    onExpand: () -> Unit,
    viewModel: TimerViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    LaunchedEffect(state.finished) {
        if (state.finished) {
            delay(2_000)
            viewModel.clearFinishedState()
        }
    }

    if (!state.running && !state.finished) return

    Box(modifier = Modifier.fillMaxWidth()) {
      Surface(
        color = MaterialTheme.colorScheme.primaryContainer,
        contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
        modifier = Modifier.fillMaxWidth()
      ) {
        Column {
            val progress = if (state.plannedSeconds > 0) {
                1f - (state.remainingSeconds.toFloat() / state.plannedSeconds.toFloat())
            } else 0f
            LinearProgressIndicator(
                progress = { progress.coerceIn(0f, 1f) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(3.dp),
                color = MaterialTheme.colorScheme.primary,
                trackColor = Color.Transparent
            )
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .clickable { onExpand() }
                ) {
                    Text(
                        state.topicName.ifBlank { "Session" },
                        fontWeight = FontWeight.SemiBold,
                        style = MaterialTheme.typography.bodyLarge,
                        maxLines = 1
                    )
                    Spacer(Modifier.height(2.dp))
                    Text(
                        if (state.finished) "Session saved ✨"
                        else "${formatTime(state.remainingSeconds)} remaining",
                        style = MaterialTheme.typography.bodySmall
                    )
                }
                if (!state.finished) {
                    IconButton(onClick = viewModel::stop) {
                        Icon(Icons.Filled.Stop, contentDescription = "Stop timer")
                    }
                }
            }
        }
      }
      // Subtle celebration burst when the session just ended; disappears automatically.
      Confetti(
          active = state.finished,
          particleCount = 25,
          durationMillis = 1400L
      )
    }
}

private fun formatTime(seconds: Long): String {
    val m = seconds / 60
    val s = seconds % 60
    return "%02d:%02d".format(m, s)
}
