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
import uz.apprica.plannote.ui.theme.*
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NoteScreen(viewModel: NoteViewModel = hiltViewModel()) {
    val searchQuery by viewModel.searchQuery.collectAsStateWithLifecycle()
    val pinnedNotes by viewModel.pinnedNotes.collectAsStateWithLifecycle()
    val unpinnedNotes by viewModel.unpinnedNotes.collectAsStateWithLifecycle()
    val showAddSheet by viewModel.showAddSheet.collectAsStateWithLifecycle()
    val allNotes by viewModel.notes.collectAsStateWithLifecycle()

    var selectedNote by remember { mutableStateOf<Note?>(null) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBackground)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .padding(horizontal = 20.dp)
        ) {
            // ── Header ────────────────────────────────────────────────────
            Text(
                text = "Eslatmalar",
                style = MaterialTheme.typography.headlineSmall,
                color = TextPrimary,
                fontWeight = FontWeight.Bold
            )

            Spacer(Modifier.height(12.dp))

            // ── Search bar ───────────────────────────────────────────────
            OutlinedTextField(
                value = searchQuery,
                onValueChange = viewModel::onSearchQueryChange,
                placeholder = { Text("Eslatmalarni qidirish...", color = TextHint) },
                leadingIcon = {
                    Icon(
                        Icons.Default.Search,
                        null,
                        tint = TextSecondary,
                        modifier = Modifier.size(20.dp)
                    )
                },
                trailingIcon = if (searchQuery.isNotEmpty()) ({
                    IconButton(onClick = { viewModel.onSearchQueryChange("") }) {
                        Icon(
                            Icons.Default.Clear,
                            null,
                            tint = TextSecondary,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }) else null,
                modifier = Modifier.fillMaxWidth(),
                colors = searchFieldColors(),
                singleLine = true,
                shape = RoundedCornerShape(14.dp)
            )

            Spacer(Modifier.height(16.dp))

            // ── Content ───────────────────────────────────────────────────
            if (allNotes.isEmpty()) {
                EmptyNotesPlaceholder()
            } else {
                val columns = if (allNotes.size == 1) 1 else 2

                LazyVerticalStaggeredGrid(
                    columns = StaggeredGridCells.Fixed(columns),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalItemSpacing = 10.dp,
                    contentPadding = PaddingValues(bottom = 88.dp)
                ) {
                    // Pinned section header — to'liq qatorni egallaydi
                    if (pinnedNotes.isNotEmpty() && searchQuery.isBlank()) {
                        item(span = StaggeredGridItemSpan.FullLine) {
                            SectionLabel()
                        }
                        items(pinnedNotes, key = { "pinned_${it.id}" }) { note ->
                            NoteCard(
                                note = note,
                                onClick = { selectedNote = note },
                                onDelete = { viewModel.deleteNote(note) },
                                onTogglePin = { viewModel.togglePin(note) }
                            )
                        }
                    }

                    // Unpinned notes — label yo'q
                    items(unpinnedNotes, key = { "note_${it.id}" }) { note ->
                        NoteCard(
                            note = note,
                            onClick = { selectedNote = note },
                            onDelete = { viewModel.deleteNote(note) },
                            onTogglePin = { viewModel.togglePin(note) }
                        )
                    }
                }
            }
        }

        // ── FAB ───────────────────────────────────────────────────────────
        FloatingActionButton(
            onClick = viewModel::showAddSheet,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(24.dp),
            containerColor = AccentAmber,
            contentColor = DarkBackground,
            shape = CircleShape
        ) {
            Icon(Icons.Default.Add, contentDescription = "Eslatma qo'shish")
        }

        // ── Add bottom sheet ──────────────────────────────────────────────
        if (showAddSheet) {
            AddNoteBottomSheet(
                onDismiss = viewModel::hideAddSheet,
                onConfirm = { title, content, color ->
                    viewModel.addNote(title, content, color)
                }
            )
        }

        // ── Detail bottom sheet ───────────────────────────────────────────
        selectedNote?.let { note ->
            NoteDetailBottomSheet(
                note = note,
                onDismiss = { selectedNote = null }
            )
        }
    }
}

// ── Note Card ─────────────────────────────────────────────────────────────────

@Composable
fun NoteCard(
    note: Note,
    onClick: () -> Unit,
    onDelete: () -> Unit,
    onTogglePin: () -> Unit,
    modifier: Modifier = Modifier
) {
    val cardBg = NoteCardColors.getOrNull(note.color) ?: DarkCard

    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(cardBg)
            .clickable { onClick() }
            .padding(12.dp)
    ) {
        Column {
            // ── Sarlavha ──────────────────────────────────────────────────────
            if (note.title.isNotBlank()) {
                Text(
                    text = note.title,
                    style = MaterialTheme.typography.titleSmall,
                    color = TextPrimary,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }

            // ── Kontent ───────────────────────────────────────────────────────
            if (note.content.isNotBlank()) {
                Spacer(Modifier.height(6.dp))
                Text(
                    text = note.preview,
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary,
                    maxLines = 6,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Spacer(Modifier.height(8.dp))

            // ── Sana + Iconlar (pastda) ───────────────────────────────────────
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = SimpleDateFormat(
                        "d MMM",
                        Locale.getDefault()
                    ).format(Date(note.updatedAt)),
                    style = MaterialTheme.typography.labelSmall,
                    color = TextHint
                )
                Row {
                    IconButton(
                        onClick = onTogglePin,
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.PushPin,
                            contentDescription = "Pin",
                            tint = if (note.isPinned) AccentAmber else TextHint,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                    IconButton(
                        onClick = onDelete,
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "O'chirish",
                            tint = ErrorRed.copy(alpha = 0.5f),
                            modifier = Modifier.size(22.dp)
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
fun NoteDetailBottomSheet(
    note: Note,
    onDismiss: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val cardBg = NoteCardColors.getOrNull(note.color) ?: DarkSurface

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = cardBg,
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp)
                .padding(bottom = 48.dp)
        ) {
            // ── Sarlavha ──────────────────────────────────────────────────────
            if (note.title.isNotBlank()) {
                Text(
                    text = note.title,
                    style = MaterialTheme.typography.headlineSmall,
                    color = TextPrimary,
                    fontWeight = FontWeight.Bold
                )
                Spacer(Modifier.height(12.dp))
            }

            // ── Kontent ───────────────────────────────────────────────────────
            if (note.content.isNotBlank()) {
                Text(
                    text = note.content,
                    style = MaterialTheme.typography.bodyLarge,
                    color = TextSecondary,
                    lineHeight = 26.sp
                )
                Spacer(Modifier.height(24.dp))
            }

            // ── Sana ──────────────────────────────────────────────────────────
            HorizontalDivider(color = TextHint.copy(alpha = 0.2f))
            Spacer(Modifier.height(12.dp))
            Text(
                text = SimpleDateFormat("d MMMM yyyy, HH:mm", Locale.getDefault())
                    .format(Date(note.updatedAt)),
                style = MaterialTheme.typography.labelSmall,
                color = TextHint
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
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var title by remember { mutableStateOf("") }
    var content by remember { mutableStateOf("") }
    var selectedColor by remember { mutableIntStateOf(0) }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = DarkSurface,
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp)
                .padding(bottom = 32.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // ── Rang ──────────────────────────────────────────────────────────
            Text(
                text = "Eslatma uchun ranglar",
                style = MaterialTheme.typography.labelMedium,
                color = TextSecondary
            )
            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                NoteCardColors.forEachIndexed { idx, color ->
                    Box(
                        modifier = Modifier
                            .size(if (selectedColor == idx) 48.dp else 40.dp)
                            .clip(CircleShape)
                            .background(color)
                            .then(
                                if (selectedColor == idx)
                                    Modifier.border(
                                        2.dp,
                                        TextPrimary.copy(alpha = 0.7f),
                                        CircleShape
                                    )
                                else Modifier
                            )
                            .clickable { selectedColor = idx },
                        contentAlignment = Alignment.Center
                    ) {
                        if (selectedColor == idx) {
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = "Tanlangan",
                                tint = TextPrimary,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                    }
                }
            }

            Text(
                text = "Yangi eslatma",
                style = MaterialTheme.typography.titleLarge,
                color = TextPrimary,
                fontWeight = FontWeight.Bold
            )

            // ── Sarlavha ──────────────────────────────────────────────────────
            OutlinedTextField(
                value = title,
                onValueChange = { title = it },
                placeholder = { Text("Sarlavha (ixtiyoriy)...", color = TextHint) },
                modifier = Modifier.fillMaxWidth(),
                colors = searchFieldColors(),
                singleLine = true,
                shape = RoundedCornerShape(12.dp)
            )

            // ── Matn ──────────────────────────────────────────────────────────
            OutlinedTextField(
                value = content,
                onValueChange = { content = it },
                placeholder = { Text("Fikrlaringizni yozing...", color = TextHint) },
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 120.dp),
                colors = searchFieldColors(),
                maxLines = 8,
                shape = RoundedCornerShape(12.dp)
            )

            // ── Saqlash tugmasi ───────────────────────────────────────────────
            Button(
                onClick = {
                    if (title.isNotBlank() || content.isNotBlank()) {
                        onConfirm(title, content, selectedColor)
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = AccentAmber,
                    contentColor = DarkBackground
                )
            ) {
                Text("Saqlash", fontWeight = FontWeight.SemiBold, fontSize = 16.sp)
            }
        }
    }
}

// ── Helpers ───────────────────────────────────────────────────────────────────

@Composable
private fun SectionLabel() {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        modifier = Modifier.padding(bottom = 4.dp)
    ) {
        Icon(
            imageVector = Icons.Default.PushPin,
            contentDescription = null,
            tint = AccentAmber,
            modifier = Modifier.size(14.dp)
        )
        Text(
            text = "Pin qilingan",
            style = MaterialTheme.typography.labelMedium,
            color = TextSecondary
        )
    }
}

@Composable
private fun EmptyNotesPlaceholder() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 64.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Icon(
            Icons.AutoMirrored.Filled.Notes,
            contentDescription = null,
            tint = TextHint,
            modifier = Modifier.size(64.dp)
        )
        Text(
            "Hali eslatma yo'q",
            style = MaterialTheme.typography.titleSmall,
            color = TextSecondary
        )
        Text(
            "FAB orqali birinchi eslatmani qo'shing",
            style = MaterialTheme.typography.bodySmall,
            color = TextHint
        )
    }
}

@Composable
private fun searchFieldColors() = OutlinedTextFieldDefaults.colors(
    focusedBorderColor = AccentAmber,
    unfocusedBorderColor = DividerColor,
    cursorColor = AccentAmber,
    focusedTextColor = TextPrimary,
    unfocusedTextColor = TextPrimary
)
