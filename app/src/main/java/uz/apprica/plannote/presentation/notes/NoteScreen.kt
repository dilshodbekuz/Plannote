package uz.apprica.plannote.presentation.notes

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.staggeredgrid.LazyVerticalStaggeredGrid
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridCells
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridItemSpan
import androidx.compose.foundation.lazy.staggeredgrid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Notes
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.PushPin
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import uz.apprica.plannote.domain.model.Note
import uz.apprica.plannote.ui.theme.AccentAmber
import uz.apprica.plannote.ui.theme.ErrorRed
import uz.apprica.plannote.ui.theme.appColors
import uz.apprica.plannote.ui.theme.strings
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NoteScreen(viewModel: NoteViewModel = hiltViewModel()) {
    val c             = MaterialTheme.appColors
    val s             = MaterialTheme.strings
    val searchQuery   by viewModel.searchQuery.collectAsStateWithLifecycle()
    val pinnedNotes   by viewModel.pinnedNotes.collectAsStateWithLifecycle()
    val unpinnedNotes by viewModel.unpinnedNotes.collectAsStateWithLifecycle()
    val showAddSheet  by viewModel.showAddSheet.collectAsStateWithLifecycle()
    val allNotes      by viewModel.notes.collectAsStateWithLifecycle()

    var selectedNote by remember { mutableStateOf<Note?>(null) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(c.background)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .padding(horizontal = 20.dp)
        ) {
            Text(
                text       = s.notesTitle,
                style      = MaterialTheme.typography.headlineSmall,
                color      = c.textPrimary,
                fontWeight = FontWeight.Bold
            )

            Spacer(Modifier.height(12.dp))

            OutlinedTextField(
                value         = searchQuery,
                onValueChange = viewModel::onSearchQueryChange,
                placeholder   = { Text(s.searchNotes, color = c.textHint) },
                leadingIcon   = {
                    Icon(Icons.Default.Search, null, tint = c.textSecondary, modifier = Modifier.size(20.dp))
                },
                trailingIcon = if (searchQuery.isNotEmpty()) ({
                    IconButton(onClick = { viewModel.onSearchQueryChange("") }) {
                        Icon(Icons.Default.Clear, null, tint = c.textSecondary, modifier = Modifier.size(18.dp))
                    }
                }) else null,
                modifier   = Modifier.fillMaxWidth(),
                colors     = noteSearchColors(),
                singleLine = true,
                shape      = RoundedCornerShape(14.dp)
            )

            Spacer(Modifier.height(16.dp))

            if (allNotes.isEmpty()) {
                NoteEmptyPlaceholder(noNotes = s.noNotes, noNotesHint = s.noNotesHint)
            } else {
                val columns = if (allNotes.size == 1) 1 else 2
                LazyVerticalStaggeredGrid(
                    columns               = StaggeredGridCells.Fixed(columns),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalItemSpacing   = 10.dp,
                    contentPadding        = PaddingValues(bottom = 88.dp)
                ) {
                    if (pinnedNotes.isNotEmpty() && searchQuery.isBlank()) {
                        item(span = StaggeredGridItemSpan.FullLine) { PinnedLabel(s.pinnedLabel) }
                        items(pinnedNotes, key = { "pinned_${it.id}" }) { note ->
                            NoteCard(
                                note        = note,
                                deleteLabel = s.delete,
                                onClick     = { selectedNote = note },
                                onDelete    = { viewModel.deleteNote(note) },
                                onTogglePin = { viewModel.togglePin(note) }
                            )
                        }
                    }
                    items(unpinnedNotes, key = { "note_${it.id}" }) { note ->
                        NoteCard(
                            note        = note,
                            deleteLabel = s.delete,
                            onClick     = { selectedNote = note },
                            onDelete    = { viewModel.deleteNote(note) },
                            onTogglePin = { viewModel.togglePin(note) }
                        )
                    }
                }
            }
        }

        FloatingActionButton(
            onClick        = viewModel::showAddSheet,
            modifier       = Modifier.align(Alignment.BottomEnd).padding(24.dp),
            containerColor = AccentAmber,
            contentColor   = c.background,
            shape          = CircleShape
        ) {
            Icon(Icons.Default.Add, contentDescription = s.newNote)
        }

        if (showAddSheet) {
            AddNoteBottomSheet(
                onDismiss = viewModel::hideAddSheet,
                onConfirm = { title, content, color -> viewModel.addNote(title, content, color) }
            )
        }

        selectedNote?.let { note ->
            NoteDetailBottomSheet(note = note, onDismiss = { selectedNote = null })
        }
    }
}

// ── Note Card ─────────────────────────────────────────────────────────────────

@Composable
fun NoteCard(
    note: Note,
    deleteLabel: String,
    onClick: () -> Unit,
    onDelete: () -> Unit,
    onTogglePin: () -> Unit,
    modifier: Modifier = Modifier
) {
    val c      = MaterialTheme.appColors
    val cardBg = c.noteCardColors.getOrNull(note.color) ?: c.card

    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(cardBg)
            .clickable { onClick() }
            .padding(12.dp)
    ) {
        Column {
            if (note.title.isNotBlank()) {
                Text(
                    text       = note.title,
                    style      = MaterialTheme.typography.titleSmall,
                    color      = c.textPrimary,
                    fontWeight = FontWeight.SemiBold,
                    maxLines   = 2,
                    overflow   = TextOverflow.Ellipsis
                )
            }
            if (note.content.isNotBlank()) {
                Spacer(Modifier.height(6.dp))
                Text(
                    text     = note.preview,
                    style    = MaterialTheme.typography.bodySmall,
                    color    = c.textSecondary,
                    maxLines = 6,
                    overflow = TextOverflow.Ellipsis
                )
            }
            Spacer(Modifier.height(8.dp))
            Row(
                modifier              = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment     = Alignment.CenterVertically
            ) {
                Text(
                    text  = SimpleDateFormat("d MMM", Locale.getDefault()).format(Date(note.updatedAt)),
                    style = MaterialTheme.typography.labelSmall,
                    color = c.textHint
                )
                Row {
                    IconButton(onClick = onTogglePin, modifier = Modifier.size(36.dp)) {
                        Icon(
                            imageVector        = Icons.Default.PushPin,
                            contentDescription = "Pin",
                            tint               = if (note.isPinned) AccentAmber else c.textHint,
                            modifier           = Modifier.size(22.dp)
                        )
                    }
                    IconButton(onClick = onDelete, modifier = Modifier.size(36.dp)) {
                        Icon(
                            imageVector        = Icons.Default.Delete,
                            contentDescription = deleteLabel,
                            tint               = ErrorRed.copy(alpha = 0.5f),
                            modifier           = Modifier.size(22.dp)
                        )
                    }
                }
            }
        }
    }
}

// ── Note Detail Bottom Sheet ──────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NoteDetailBottomSheet(note: Note, onDismiss: () -> Unit) {
    val c          = MaterialTheme.appColors
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val cardBg     = c.noteCardColors.getOrNull(note.color) ?: c.surface

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState       = sheetState,
        containerColor   = cardBg,
        shape            = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp)
                .padding(bottom = 48.dp)
        ) {
            if (note.title.isNotBlank()) {
                Text(
                    text       = note.title,
                    style      = MaterialTheme.typography.headlineSmall,
                    color      = c.textPrimary,
                    fontWeight = FontWeight.Bold
                )
                Spacer(Modifier.height(12.dp))
            }
            if (note.content.isNotBlank()) {
                Text(
                    text       = note.content,
                    style      = MaterialTheme.typography.bodyLarge,
                    color      = c.textSecondary,
                    lineHeight = 26.sp
                )
                Spacer(Modifier.height(24.dp))
            }
            HorizontalDivider(color = c.textHint.copy(alpha = 0.2f))
            Spacer(Modifier.height(12.dp))
            Text(
                text  = SimpleDateFormat("d MMMM yyyy, HH:mm", Locale.getDefault()).format(Date(note.updatedAt)),
                style = MaterialTheme.typography.labelSmall,
                color = c.textHint
            )
        }
    }
}

// ── Add Note Bottom Sheet ─────────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddNoteBottomSheet(
    onDismiss: () -> Unit,
    onConfirm: (title: String, content: String, color: Int) -> Unit
) {
    val c             = MaterialTheme.appColors
    val s             = MaterialTheme.strings
    val sheetState    = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var title         by remember { mutableStateOf("") }
    var content       by remember { mutableStateOf("") }
    var selectedColor by remember { mutableIntStateOf(0) }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState       = sheetState,
        containerColor   = c.surface,
        shape            = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp)
                .padding(bottom = 32.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Text(
                text  = s.noteColors,
                style = MaterialTheme.typography.labelMedium,
                color = c.textSecondary
            )
            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment     = Alignment.CenterVertically
            ) {
                c.noteCardColors.forEachIndexed { idx, color ->
                    Box(
                        modifier = Modifier
                            .size(if (selectedColor == idx) 48.dp else 40.dp)
                            .clip(CircleShape)
                            .background(color)
                            .then(
                                if (selectedColor == idx)
                                    Modifier.border(2.dp, c.textPrimary.copy(alpha = 0.7f), CircleShape)
                                else Modifier
                            )
                            .clickable { selectedColor = idx },
                        contentAlignment = Alignment.Center
                    ) {
                        if (selectedColor == idx) {
                            Icon(Icons.Default.Check, contentDescription = null, tint = c.textPrimary, modifier = Modifier.size(22.dp))
                        }
                    }
                }
            }

            Text(
                text       = s.newNote,
                style      = MaterialTheme.typography.titleLarge,
                color      = c.textPrimary,
                fontWeight = FontWeight.Bold
            )

            OutlinedTextField(
                value         = title,
                onValueChange = { title = it },
                placeholder   = { Text(s.noteTitleHint, color = c.textHint) },
                modifier      = Modifier.fillMaxWidth(),
                colors        = noteSearchColors(),
                singleLine    = true,
                shape         = RoundedCornerShape(12.dp)
            )

            OutlinedTextField(
                value         = content,
                onValueChange = { content = it },
                placeholder   = { Text(s.noteContentHint, color = c.textHint) },
                modifier      = Modifier.fillMaxWidth().heightIn(min = 120.dp),
                colors        = noteSearchColors(),
                maxLines      = 8,
                shape         = RoundedCornerShape(12.dp)
            )

            Button(
                onClick = {
                    if (title.isNotBlank() || content.isNotBlank()) {
                        onConfirm(title, content, selectedColor)
                    }
                },
                modifier = Modifier.fillMaxWidth().height(52.dp),
                shape    = RoundedCornerShape(14.dp),
                colors   = ButtonDefaults.buttonColors(
                    containerColor = AccentAmber,
                    contentColor   = c.background
                )
            ) {
                Text(s.save, fontWeight = FontWeight.SemiBold, fontSize = 16.sp)
            }
        }
    }
}

// ── Helpers ───────────────────────────────────────────────────────────────────

@Composable
private fun PinnedLabel(label: String) {
    val c = MaterialTheme.appColors
    Row(
        verticalAlignment     = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        modifier              = Modifier.padding(bottom = 4.dp)
    ) {
        Icon(Icons.Default.PushPin, contentDescription = null, tint = AccentAmber, modifier = Modifier.size(14.dp))
        Text(label, style = MaterialTheme.typography.labelMedium, color = c.textSecondary)
    }
}

@Composable
private fun NoteEmptyPlaceholder(noNotes: String, noNotesHint: String) {
    val c = MaterialTheme.appColors
    Column(
        modifier            = Modifier.fillMaxWidth().padding(top = 64.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Icon(Icons.AutoMirrored.Filled.Notes, contentDescription = null, tint = c.textHint, modifier = Modifier.size(64.dp))
        Text(noNotes,     style = MaterialTheme.typography.titleSmall, color = c.textSecondary)
        Text(noNotesHint, style = MaterialTheme.typography.bodySmall,  color = c.textHint)
    }
}

@Composable
private fun noteSearchColors() = OutlinedTextFieldDefaults.colors(
    focusedBorderColor   = AccentAmber,
    unfocusedBorderColor = MaterialTheme.appColors.divider,
    cursorColor          = AccentAmber,
    focusedTextColor     = MaterialTheme.appColors.textPrimary,
    unfocusedTextColor   = MaterialTheme.appColors.textPrimary
)
