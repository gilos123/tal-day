package com.maayan.studytracker.ui.achievements

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AchievementsScreen(
    onBack: () -> Unit,
    viewModel: AchievementsViewModel = hiltViewModel()
) {
    val list by viewModel.achievements.collectAsStateWithLifecycle()
    val anyUnlocked = list.any { it.unlocked }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Achievements") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        if (!anyUnlocked && list.size == com.maayan.studytracker.domain.AchievementCatalogue.all.size) {
            // Show the grid still (they render as locked placeholders) but add a top message.
            Column(modifier = Modifier.fillMaxSize().padding(padding)) {
                EmptyHeader()
                Grid(list)
            }
        } else {
            Grid(list, modifier = Modifier.padding(padding))
        }
    }
}

@Composable
private fun EmptyHeader() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                "No badges yet",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(Modifier.height(6.dp))
            Text(
                "Start a session to unlock your first 🚀",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun Grid(
    list: List<AchievementUi>,
    modifier: Modifier = Modifier
) {
    LazyVerticalGrid(
        columns = GridCells.Fixed(3),
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(12.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(list, key = { it.rule.code }) { item -> AchievementCard(item) }
    }
}

@Composable
private fun AchievementCard(item: AchievementUi) {
    val container =
        if (item.unlocked) MaterialTheme.colorScheme.tertiaryContainer
        else MaterialTheme.colorScheme.surfaceVariant
    val content =
        if (item.unlocked) MaterialTheme.colorScheme.onTertiaryContainer
        else MaterialTheme.colorScheme.onSurfaceVariant

    Card(
        colors = CardDefaults.cardColors(containerColor = container, contentColor = content),
        modifier = Modifier
            .fillMaxWidth()
            .alpha(if (item.unlocked) 1f else 0.55f)
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = item.rule.icon,
                fontSize = 32.sp
            )
            Spacer(Modifier.height(6.dp))
            Text(
                text = item.rule.title,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(Modifier.height(4.dp))
            Text(
                text = item.rule.description,
                style = MaterialTheme.typography.bodySmall,
                color = content
            )
            if (item.unlocked && item.unlockedAt != null) {
                Spacer(Modifier.height(6.dp))
                Text(
                    text = formatUnlockDate(item.unlockedAt),
                    style = MaterialTheme.typography.labelSmall,
                    color = content
                )
            }
        }
    }
}

private val UNLOCK_DATE_FMT: DateTimeFormatter = DateTimeFormatter.ofPattern("MMM d, yyyy")

private fun formatUnlockDate(epochMillis: Long): String =
    Instant.ofEpochMilli(epochMillis).atZone(ZoneId.systemDefault()).toLocalDate().format(UNLOCK_DATE_FMT)
