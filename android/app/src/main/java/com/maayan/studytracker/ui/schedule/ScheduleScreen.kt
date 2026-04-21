package com.maayan.studytracker.ui.schedule

import androidx.compose.foundation.clickable
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
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.PlayArrow
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
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.maayan.studytracker.data.db.entities.ProjectEntity
import kotlinx.coroutines.launch
import java.time.LocalDate

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScheduleScreen(
    onOpenTopic: (projectId: Long, topicName: String) -> Unit,
    onOpenStats: () -> Unit,
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

    // Material 3's ModalNavigationDrawer always opens on the start edge. Wrapping it in
    // LayoutDirection.Rtl flips it to the end edge (visually: right in an LTR app). The
    // drawer sheet and main content are then forced back to Ltr so their own children
    // render normally.
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
                        onDelete = { deleteTarget = it }
                    )
                }
            }
        ) {
            CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
                Scaffold(
                    topBar = {
                        TopAppBar(
                            title = { Text(selectedProject?.name ?: "My Schedule") },
                            actions = {
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

    if (showNewProjectDialog) {
        ProjectNameDialog(
            title = "New project",
            initial = "",
            onDismiss = { showNewProjectDialog = false },
            onConfirm = { name ->
                viewModel.createProject(name)
                showNewProjectDialog = false
            }
        )
    }
    renameTarget?.let { target ->
        ProjectNameDialog(
            title = "Rename project",
            initial = target.name,
            onDismiss = { renameTarget = null },
            onConfirm = { name ->
                viewModel.renameProject(target.id, name)
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
private fun ProjectsDrawerSheet(
    projects: List<ProjectEntity>,
    selectedId: Long?,
    onSelect: (Long) -> Unit,
    onAdd: () -> Unit,
    onRename: (ProjectEntity) -> Unit,
    onDelete: (ProjectEntity) -> Unit
) {
    ModalDrawerSheet {
        Text(
            "Projects",
            modifier = Modifier.padding(horizontal = 24.dp, vertical = 16.dp),
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        LazyColumn(
            modifier = Modifier.weight(1f, fill = true)
        ) {
            items(projects, key = { it.id }) { project ->
                NavigationDrawerItem(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 2.dp),
                    label = {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
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
private fun ProjectNameDialog(
    title: String,
    initial: String,
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit
) {
    var name by remember { mutableStateOf(initial) }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = {
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("Name") },
                singleLine = true
            )
        },
        confirmButton = {
            TextButton(
                onClick = { onConfirm(name) },
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
