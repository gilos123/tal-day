package com.maayan.studytracker.ui.schedule

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.NavigationDrawerItemDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.maayan.studytracker.data.db.entities.ProjectEntity
import com.maayan.studytracker.ui.common.GamificationBadges
import com.maayan.studytracker.ui.common.ProjectColorPicker
import com.maayan.studytracker.ui.common.hexToColor
import com.maayan.studytracker.ui.theme.DefaultProjectColorHex
import kotlinx.coroutines.launch
import java.time.LocalDate

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScheduleScreen(
    onOpenTopic: (projectId: Long, topicName: String) -> Unit,
    onOpenStats: () -> Unit,
    onOpenSettings: () -> Unit,
    onOpenAchievements: () -> Unit,
    viewModel: ScheduleViewModel = hiltViewModel()
) {
    val items by viewModel.items.collectAsStateWithLifecycle()
    val projects by viewModel.projects.collectAsStateWithLifecycle()
    val selectedProject by viewModel.selectedProject.collectAsStateWithLifecycle()
    val selectedProjectId by viewModel.selectedProjectId.collectAsStateWithLifecycle()
    val todayIso = LocalDate.now().toString()

    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()

    var showNewProjectDialog by remember { mutableStateOf(false) }
    var renameTarget by remember { mutableStateOf<ProjectEntity?>(null) }
    var deleteTarget by remember { mutableStateOf<ProjectEntity?>(null) }

    val projectStripeColor =
        hexToColor(selectedProject?.color ?: DefaultProjectColorHex)

    // Flip the drawer to open from the right by wrapping in LayoutDirection.Rtl.
    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
        ModalNavigationDrawer(
            drawerState = drawerState,
            drawerContent = {
                CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
                    ProjectsDrawerSheet(
                        projects = projects,
                        selectedId = selectedProjectId,
                        onSelect = { id ->
                            viewModel.selectProject(id)
                            scope.launch { drawerState.close() }
                        },
                        onAdd = { showNewProjectDialog = true },
                        onRename = { renameTarget = it },
                        onDelete = { deleteTarget = it },
                        onOpenSettings = {
                            scope.launch { drawerState.close() }
                            onOpenSettings()
                        },
                        onOpenAchievements = {
                            scope.launch { drawerState.close() }
                            onOpenAchievements()
                        }
                    )
                }
            }
        ) {
            CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
                Scaffold(
                    topBar = {
                        TopAppBar(
                            title = {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    ProjectDot(projectStripeColor, size = 10.dp)
                                    Spacer(Modifier.width(8.dp))
                                    Text(selectedProject?.name ?: "My Schedule")
                                }
                            },
                            actions = {
                                GamificationBadges(
                                    modifier = Modifier.padding(end = 4.dp)
                                )
                                IconButton(onClick = onOpenStats) {
                                    Icon(Icons.Filled.BarChart, contentDescription = "Stats")
                                }
                                IconButton(onClick = { scope.launch { drawerState.open() } }) {
                                    Icon(Icons.Filled.Menu, contentDescription = "Projects")
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
                    if (items.isEmpty()) {
                        EmptyScheduleState(modifier = Modifier.padding(padding))
                    } else {
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
                                    stripeColor = projectStripeColor,
                                    topicName = item.topicName,
                                    minutes = item.plannedDurationMinutes,
                                    doneToday = doneToday,
                                    onToggleDone = { viewModel.toggleDone(item.id, doneToday) },
                                    onTopicChange = { viewModel.updateTopicName(item.id, it) },
                                    onMinutesChange = { viewModel.updateDuration(item.id, it) },
                                    onOpenTopic = {
                                        val pid = selectedProjectId
                                        if (pid != null && item.topicName.isNotBlank()) {
                                            onOpenTopic(pid, item.topicName)
                                        }
                                    },
                                    onStartTimer = { viewModel.startTimerFor(item.id) },
                                    onDelete = { viewModel.deleteRow(item.id) }
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    if (showNewProjectDialog) {
        ProjectNameDialog(
            title = "New project",
            initialName = "",
            initialColorHex = DefaultProjectColorHex,
            onDismiss = { showNewProjectDialog = false },
            onConfirm = { name, color ->
                viewModel.createProject(name, color)
                showNewProjectDialog = false
            }
        )
    }
    renameTarget?.let { target ->
        ProjectNameDialog(
            title = "Edit project",
            initialName = target.name,
            initialColorHex = target.color,
            onDismiss = { renameTarget = null },
            onConfirm = { name, color ->
                if (name != target.name) viewModel.renameProject(target.id, name)
                if (!color.equals(target.color, ignoreCase = true)) {
                    viewModel.setProjectColor(target.id, color)
                }
                renameTarget = null
            }
        )
    }
    deleteTarget?.let { target ->
        AlertDialog(
            onDismissRequest = { deleteTarget = null },
            title = { Text("Delete project?") },
            text = {
                Text(
                    "\"${target.name}\" and all of its schedule rows and topic notes will be " +
                        "permanently deleted. Timer history is kept in your stats."
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.deleteProject(target.id)
                    deleteTarget = null
                }) { Text("Delete") }
            },
            dismissButton = {
                TextButton(onClick = { deleteTarget = null }) { Text("Cancel") }
            }
        )
    }
}

// ---------------------------------------------------------------------------------------

@Composable
private fun EmptyScheduleState(modifier: Modifier) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .padding(32.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text("🎯", style = MaterialTheme.typography.displayLarge)
            Spacer(Modifier.height(12.dp))
            Text(
                "Let's go!",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(Modifier.height(4.dp))
            Text(
                "Tap + to add your first topic.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun ProjectDot(color: Color, size: androidx.compose.ui.unit.Dp = 12.dp) {
    Box(
        modifier = Modifier
            .size(size)
            .clip(CircleShape)
            .background(color)
    )
}

@Composable
private fun ProjectsDrawerSheet(
    projects: List<ProjectEntity>,
    selectedId: Long?,
    onSelect: (Long) -> Unit,
    onAdd: () -> Unit,
    onRename: (ProjectEntity) -> Unit,
    onDelete: (ProjectEntity) -> Unit,
    onOpenSettings: () -> Unit,
    onOpenAchievements: () -> Unit
) {
    ModalDrawerSheet {
        Text(
            "Projects",
            modifier = Modifier.padding(horizontal = 24.dp, vertical = 16.dp),
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        LazyColumn(modifier = Modifier.weight(1f, fill = true)) {
            items(projects, key = { it.id }) { project ->
                NavigationDrawerItem(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 2.dp),
                    label = {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            ProjectDot(hexToColor(project.color))
                            Spacer(Modifier.width(10.dp))
                            Text(project.name, modifier = Modifier.weight(1f))
                            IconButton(onClick = { onRename(project) }) {
                                Icon(
                                    Icons.Filled.Edit,
                                    contentDescription = "Rename",
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            IconButton(onClick = { onDelete(project) }) {
                                Icon(
                                    Icons.Filled.Delete,
                                    contentDescription = "Delete",
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    },
                    selected = project.id == selectedId,
                    onClick = { onSelect(project.id) },
                    colors = NavigationDrawerItemDefaults.colors()
                )
            }
        }

        HorizontalDivider()
        DrawerExtraRow(icon = Icons.Filled.EmojiEvents, label = "Achievements", onClick = onOpenAchievements)
        DrawerExtraRow(icon = Icons.Filled.Settings, label = "Settings", onClick = onOpenSettings)
        HorizontalDivider()
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onAdd() }
                .padding(horizontal = 24.dp, vertical = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Icon(Icons.Filled.Add, contentDescription = null)
            Text("New project", style = MaterialTheme.typography.bodyLarge)
        }
    }
}

@Composable
private fun DrawerExtraRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(horizontal = 24.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(label, style = MaterialTheme.typography.bodyLarge)
    }
}

@Composable
private fun ProjectNameDialog(
    title: String,
    initialName: String,
    initialColorHex: String,
    onDismiss: () -> Unit,
    onConfirm: (name: String, colorHex: String) -> Unit
) {
    var name by remember { mutableStateOf(initialName) }
    var colorHex by remember { mutableStateOf(initialColorHex) }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Name") },
                    singleLine = true
                )
                Text(
                    "Color tag",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                ProjectColorPicker(
                    selectedHex = colorHex,
                    onPick = { colorHex = it }
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = { onConfirm(name, colorHex) },
                enabled = name.trim().isNotEmpty()
            ) { Text("Save") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}

@Composable
private fun ScheduleRow(
    stripeColor: Color,
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
    val haptics = LocalHapticFeedback.current
    val checkScale by animateFloatAsState(
        targetValue = if (doneToday) 1.1f else 1f,
        animationSpec = spring(dampingRatio = 0.4f, stiffness = 550f),
        label = "checkScale"
    )
    val topicTextStyle = LocalTextStyle.current.copy(
        textDecoration = if (doneToday) TextDecoration.LineThrough else TextDecoration.None
    )

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .alpha(if (doneToday) 0.55f else 1f)
    ) {
        // Color stripe on leading edge
        Box(
            modifier = Modifier
                .width(4.dp)
                .fillMaxHeight()
                .clip(MaterialTheme.shapes.extraSmall)
                .background(stripeColor)
        )
        Spacer(Modifier.width(8.dp))
        Column(modifier = Modifier.weight(1f)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Checkbox(
                    checked = doneToday,
                    onCheckedChange = {
                        haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                        onToggleDone()
                    },
                    modifier = Modifier.scale(checkScale)
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
}
