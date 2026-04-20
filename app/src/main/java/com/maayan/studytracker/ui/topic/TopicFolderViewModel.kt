package com.maayan.studytracker.ui.topic

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.maayan.studytracker.data.db.entities.NoteEntity
import com.maayan.studytracker.data.db.entities.NoteStatus
import com.maayan.studytracker.data.db.entities.TopicFolderEntity
import com.maayan.studytracker.data.repository.TopicRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class TopicFolderViewModel @Inject constructor(
    private val repository: TopicRepository
) : ViewModel() {

    private val _topicName = MutableStateFlow<String?>(null)
    private val _folderId = MutableStateFlow<Long?>(null)

    val currentFolder: StateFlow<TopicFolderEntity?> = _folderId
        .filterNotNull()
        .map { repository.getFolder(it) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    val subfolders: StateFlow<List<TopicFolderEntity>> = _folderId
        .filterNotNull()
        .flatMapLatest { fid ->
            val topic = _topicName.value.orEmpty()
            repository.observeSubfolders(topic, fid)
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val notes: StateFlow<List<NoteEntity>> = _folderId
        .filterNotNull()
        .flatMapLatest { fid -> repository.observeNotes(fid) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val topicName: StateFlow<String> = _topicName.map { it.orEmpty() }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), "")

    fun init(topicName: String, initialFolderId: Long?) {
        if (_topicName.value == topicName && _folderId.value != null) return
        _topicName.value = topicName
        viewModelScope.launch {
            val fid = initialFolderId ?: repository.getOrCreateRootFolder(topicName)
            _folderId.value = fid
        }
    }

    fun createSubfolder(name: String) {
        val t = _topicName.value ?: return
        val f = _folderId.value ?: return
        if (name.isBlank()) return
        viewModelScope.launch { repository.createSubfolder(t, f, name) }
    }

    fun createNote(content: String) {
        val f = _folderId.value ?: return
        if (content.isBlank()) return
        viewModelScope.launch { repository.createNote(f, content) }
    }

    fun updateNoteContent(id: Long, content: String) {
        viewModelScope.launch { repository.updateNoteContent(id, content) }
    }

    fun cycleNoteStatus(id: Long, current: NoteStatus) {
        val next = when (current) {
            NoteStatus.NONE -> NoteStatus.DONE
            NoteStatus.DONE -> NoteStatus.NOT_DONE
            NoteStatus.NOT_DONE -> NoteStatus.NONE
        }
        viewModelScope.launch { repository.updateNoteStatus(id, next) }
    }

    fun deleteNote(id: Long) {
        viewModelScope.launch { repository.deleteNote(id) }
    }

    fun deleteFolder(id: Long) {
        viewModelScope.launch { repository.deleteFolder(id) }
    }
}
