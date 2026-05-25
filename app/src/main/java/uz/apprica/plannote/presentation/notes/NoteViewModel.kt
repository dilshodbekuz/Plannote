package uz.apprica.plannote.presentation.notes

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import uz.apprica.plannote.domain.model.Note
import uz.apprica.plannote.domain.repository.NoteRepository
import uz.apprica.plannote.domain.usecase.note.AddNoteUseCase
import uz.apprica.plannote.domain.usecase.note.DeleteNoteUseCase
import uz.apprica.plannote.domain.usecase.note.GetAllNotesUseCase
import uz.apprica.plannote.domain.usecase.note.UpdateNoteUseCase
import uz.apprica.plannote.notification.AlarmScheduler
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class NoteViewModel @Inject constructor(
    private val getAllNotesUseCase: GetAllNotesUseCase,
    private val addNoteUseCase: AddNoteUseCase,
    private val updateNoteUseCase: UpdateNoteUseCase,
    private val deleteNoteUseCase: DeleteNoteUseCase,
    private val noteRepository: NoteRepository,
    private val alarmScheduler: AlarmScheduler
) : ViewModel() {

    // ── Search query ──────────────────────────────────────────────────────────
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _showAddSheet = MutableStateFlow(false)
    val showAddSheet: StateFlow<Boolean> = _showAddSheet.asStateFlow()

    // ── Notes (reactive, with search filter) ─────────────────────────────────
    val notes: StateFlow<List<Note>> = _searchQuery
        .flatMapLatest { query ->
            if (query.isBlank()) getAllNotesUseCase()
            else noteRepository.searchNotes(query)
        }
        .stateIn(
            scope        = viewModelScope,
            started      = SharingStarted.WhileSubscribed(5_000),
            initialValue = emptyList()
        )

    val pinnedNotes: StateFlow<List<Note>> = notes
        .map { list -> list.filter { it.isPinned } }
        .stateIn(
            scope        = viewModelScope,
            started      = SharingStarted.WhileSubscribed(5_000),
            initialValue = emptyList()
        )

    val unpinnedNotes: StateFlow<List<Note>> = notes
        .map { list -> list.filter { !it.isPinned } }
        .stateIn(
            scope        = viewModelScope,
            started      = SharingStarted.WhileSubscribed(5_000),
            initialValue = emptyList()
        )

    // ── Actions ───────────────────────────────────────────────────────────────

    fun onSearchQueryChange(query: String) { _searchQuery.update { query } }

    fun showAddSheet()  { _showAddSheet.update { true  } }
    fun hideAddSheet()  { _showAddSheet.update { false } }

    fun addNote(
        title: String,
        content: String,
        color: Int = 0,
        reminderAt: Long? = null
    ) {
        viewModelScope.launch {
            try {
                val newId = addNoteUseCase(
                    Note(
                        title      = title.trim(),
                        content    = content.trim(),
                        color      = color,
                        reminderAt = reminderAt
                    )
                )
                reminderAt?.let { time ->
                    alarmScheduler.scheduleNoteReminder(newId, title.trim(), time)
                }
                hideAddSheet()
            } catch (e: IllegalArgumentException) { /* ignore empty notes */ }
        }
    }

    fun deleteNote(note: Note) {
        viewModelScope.launch {
            alarmScheduler.cancelNoteReminder(note.id)
            deleteNoteUseCase(note)
        }
    }

    fun togglePin(note: Note) {
        viewModelScope.launch {
            updateNoteUseCase(note.copy(isPinned = !note.isPinned))
        }
    }

    fun toggleArchive(note: Note) {
        viewModelScope.launch {
            noteRepository.toggleArchive(note.id, !note.isArchived)
        }
    }

    fun updateNoteColor(note: Note, colorIndex: Int) {
        viewModelScope.launch {
            updateNoteUseCase(note.copy(color = colorIndex))
        }
    }
}
