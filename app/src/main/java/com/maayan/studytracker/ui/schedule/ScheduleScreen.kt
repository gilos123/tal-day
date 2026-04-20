package com.maayan.studytracker.ui.schedule

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import java.time.LocalDate

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScheduleScreen(
    onOpenTopic: (String) -> Unit,
    onOpenStats: () -> Unit,
    viewModel: ScheduleViewModel = hiltViewModel()
) {
    val items by viewModel.items.collectAsStateWithLifecycle()
    val todayIso = LocalDate.now().toString()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("My Schedule") },
                actions = {
                    IconButton(onClick = onOpenStats) {
                        Icon(Icons.Filled.BarChart, contentDescription = "Stats")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = viewModel::addRow) {
                Icon(Icons.Filled.Add, contentDescription = "Add row")
            }
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(items, key = { it.id }) { item ->
                val doneToday = item.lastDoneDate == todayIso
                ScheduleRow(
                    topicName = item.topicName,
                    minutes = item.plannedDurationMinutes,
                    doneToday = doneToday,
                    onToggleDone = { viewModel.toggleDone(item.id, doneToday) },
                    onTopicChange = { viewModel.updateTopicName(item.id, it) },
                    onMinutesChange = { viewModel.updateDuration(item.id, it) },
                    onOpenTopic = { if (item.topicName.isNotBlank()) onOpenTopic(item.topicName) },
                    onStartTimer = { viewModel.startTimerFor(item.id) },
                    onDelete = { viewModel.deleteRow(item.id) }
                )
            }
        }
    }
}

@Composable
private fun ScheduleRow(
    topicName: String,
    minutes: Int,
    doneToday: Boolean,
    onToggleDone: () -> Unit,
    onTopicChange: (String) -> Unit,
    onMinutesChange: (Int) -> Unit,
    onOpenTopic: () -> Unit,
    onStartTimer: () -> Unit,
    onDelete: () -> Unit
) {
    val rowModifier = Modifier
        .fillMaxWidth()
        .alpha(if (doneToday) 0.55f else 1f)

    val topicTextStyle = LocalTextStyle.current.copy(
        textDecoration = if (doneToday) TextDecoration.LineThrough else TextDecoration.None
    )

    Column(modifier = rowModifier) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Checkbox(
                checked = doneToday,
                onCheckedChange = { onToggleDone() }
            )
            OutlinedTextField(
                value = topicName,
                onValueChange = onTopicChange,
                label = { Text("Topic") },
                singleLine = true,
                textStyle = topicTextStyle,
                modifier = Modifier.weight(1f)
            )
            OutlinedTextField(
                value = minutes.toString(),
                onValueChange = { input -> input.toIntOrNull()?.let(onMinutesChange) },
                label = { Text("Min") },
                singleLine = true,
                modifier = Modifier.width(84.dp)
            )
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            TextButton(
                onClick = onOpenTopic,
                modifier = Modifier.weight(1f)
            ) { Text("Open topic folder") }
            Button(onClick = onStartTimer) {
                Icon(Icons.Filled.PlayArrow, contentDescription = null)
                Text("Start")
            }
            IconButton(onClick = onDelete) {
                Icon(Icons.Filled.Delete, contentDescription = "Delete")
            }
        }
    }
}
