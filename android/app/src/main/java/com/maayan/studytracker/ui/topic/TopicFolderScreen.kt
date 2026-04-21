package com.maayan.studytracker.ui.topic

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.CreateNewFolder
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.HorizontalRule
import androidx.compose.material.icons.filled.NoteAdd
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.maayan.studytracker.data.db.entities.NoteStatus

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TopicFolderScreen(
    projectId: Long,
    topicName: String,
    initialFolderId: Long?,
    onOpenSubfolder: (Long) -> Unit,
    onBack: () -> Unit,
    viewModel: TopicFolderViewModel = hiltViewModel()
) {
    LaunchedEffect(projectId, topicName, initialFolderId) {
        viewModel.init(projectId, topicName, initialFolderId)
    }

    val currentFolder by viewModel.currentFolder.collectAsStateWithLifecycle()
    val subfolders by viewModel.subfolders.collectAsStateWithLifecycle()
    val notes by viewModel.notes.collectAsStateWithLifecycle()

    var showNewNoteDialog by remember { mutableStateOf(false) }
    var showNewFolderDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    val name = currentFolder?.name ?: topicName
                    Text(name)
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        },
        floatingActionButton = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                FloatingActionButton(onClick = { showNewFolderDialog = true }) {
                    Icon(Icons.Filled.CreateNewFolder, contentDescription = "New subfolder")
                }
                FloatingActionButton(onClick = { showNewNoteDialog = true }) {
                    Icon(Icons.Filled.NoteAdd, contentDescription = "New note")
                }
            }
        }
    ) { padding ->
        if (subfolders.isEmpty() && notes.isEmpty()) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(32.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text("🗂️", style = MaterialTheme.typography.displayLarge)
                Spacer(Modifier.height(12.dp))
                Text(
                    "Nothing here yet",
                    style = MaterialTheme.typography.titleMedium
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    "Jot your first note or create a subfolder 🚀",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            return@Scaffold
        }
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(subfolders, key = { "f-${it.id}" }) { folder ->
                SubfolderRow(
                    name = folder.name,
                    onOpen = { onOpenSubfolder(folder.id) },
                    onDelete = { viewModel.deleteFolder(folder.id) }
                )
            }
            items(notes, key = { "n-${it.id}" }) { note ->
                val status = runCatching { NoteStatus.valueOf(note.status) }.getOrDefault(NoteStatus.NONE)
                NoteRow(
                    content = note.content,
                    status = status,
                    onContentChange = { viewModel.updateNoteContent(note.id, it) },
                    onStatusCycle = { viewModel.cycleNoteStatus(note.id, status) },
                    onDelete = { viewModel.deleteNote(note.id) }
                )
            }
        }
    }

    if (showNewFolderDialog) {
        InputDialog(
            title = "New subfolder",
            label = "Name",
            onDismiss = { showNewFolderDialog = false },
            onConfirm = {
                viewModel.createSubfolder(it)
                showNewFolderDialog = false
            }
        )
    }
    if (showNewNoteDialog) {
        InputDialog(
            title = "New note",
            label = "Note",
            onDismiss = { showNewNoteDialog = false },
            onConfirm = {
                viewModel.createNote(it)
                showNewNoteDialog = false
            }
        )
    }
}

@Composable
private fun SubfolderRow(name: String, onOpen: () -> Unit, onDelete: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onOpen() }
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(Icons.Filled.Folder, contentDescription = null)
            Text(name, modifier = Modifier.weight(1f).padding(start = 8.dp))
            IconButton(onClick = onDelete) {
                Icon(Icons.Filled.Delete, contentDescription = "Delete")
            }
        }
    }
}

@Composable
private fun NoteRow(
    content: String,
    status: NoteStatus,
    onContentChange: (String) -> Unit,
    onStatusCycle: () -> Unit,
    onDelete: () -> Unit
) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.padding(8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            IconButton(onClick = onStatusCycle) {
                val (icon, tint) = when (status) {
                    NoteStatus.DONE -> Icons.Filled.Check to MaterialTheme.colorScheme.primary
                    NoteStatus.NOT_DONE -> Icons.Filled.Close to MaterialTheme.colorScheme.error
                    NoteStatus.NONE -> Icons.Filled.HorizontalRule to MaterialTheme.colorScheme.outline
                }
                Icon(icon, contentDescription = status.name, tint = tint)
            }
            OutlinedTextField(
                value = content,
                onValueChange = onContentChange,
                modifier = Modifier.weight(1f)
            )
            IconButton(onClick = onDelete) {
                Icon(Icons.Filled.Delete, contentDescription = "Delete note")
            }
        }
    }
}

@Composable
private fun InputDialog(
    title: String,
    label: String,
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit
) {
    var text by remember { mutableStateOf("") }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = {
            OutlinedTextField(
                value = text,
                onValueChange = { text = it },
                label = { Text(label) },
                singleLine = false
            )
        },
        confirmButton = {
            TextButton(onClick = { onConfirm(text) }) { Text("Save") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}
