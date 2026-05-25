package uz.apprica.plannote.presentation.notes

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.staggeredgrid.LazyVerticalStaggeredGrid
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridCells
import androidx.compose.foundation.lazy.staggeredgrid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
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
import uz.apprica.plannote.utils.DateUtils
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NoteScreen(viewModel: NoteViewModel = hiltViewModel()) {
    val searchQuery   by viewModel.searchQuery.collectAsStateWithLifecycle()
    val pinnedNotes   by viewModel.pinnedNotes.collectAsStateWithLifecycle()
    val unpinnedNotes by viewModel.unpinnedNotes.collectAsStateWithLifecycle()
    val showAddSheet  by viewModel.showAddSheet.collectAsStateWithLifecycle()
    val allNotes      by viewModel.notes.collectAsStateWithLifecycle()

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
            Spacer(Modifier.height(16.dp))

            // ── Header ────────────────────────────────────────────────────
            Text(
                text       = "Eslatmalar",
                style      = MaterialTheme.typography.headlineSmall,
                color      = TextPrimary,
                fontWeight = FontWeight.Bold
            )
            Text(
                text  = "${allNotes.size} ta eslatma",
                style = MaterialTheme.typography.bodySmall,
                color = TextSecondary
            )

            Spacer(Modifier.height(12.dp))

            // ── Search bar ───────────────────────────────────────────────
            OutlinedTextField(
                value         = searchQuery,
                onValueChange = viewModel::onSearchQueryChange,
                placeholder   = { Text("Eslatmalarni qidirish...", color = TextHint) },
                leadingIcon   = {
                    Icon(Icons.Default.Search, null, tint = TextSecondary, modifier = Modifier.size(20.dp))
                },
                trailingIcon  = if (searchQuery.isNotEmpty()) ({
                    IconButton(onClick = { viewModel.onSearchQueryChange("") }) {
                        Icon(Icons.Default.Clear, null, tint = TextSecondary, modifier = Modifier.size(18.dp))
                    }
                }) else null,
                modifier      = Modifier.fillMaxWidth(),
                colors        = searchFieldColors(),
                singleLine    = true,
                shape         = RoundedCornerShape(14.dp)
            )

            Spacer(Modifier.height(16.dp))

            // ── Content ───────────────────────────────────────────────────
            if (allNotes.isEmpty()) {
                EmptyNotesPlaceholder()
            } else {
                LazyVerticalStaggeredGrid(
                    columns               = StaggeredGridCells.Fixed(2),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalItemSpacing   = 10.dp,
                    contentPadding        = PaddingValues(bottom = 88.dp)
                ) {
                    // Pinned section header
                    if (pinnedNotes.isNotEmpty() && searchQuery.isBlank()) {
                        item {
                            SectionLabel("📌 Pin qilingan")
                        }
                        item { /* placeholder for 2-col alignment */ Spacer(Modifier.height(0.dp)) }

                        items(pinnedNotes, key = { "pinned_${it.id}" }) { note ->
                            NoteCard(
                                note        = note,
                                onDelete    = { viewModel.deleteNote(note) },
                                onTogglePin = { viewModel.togglePin(note) }
                            )
                        }
                    }

                    // All / unpinned notes header
                    if (unpinnedNotes.isNotEmpty()) {
                        item {
                            SectionLabel(
                                if (pinnedNotes.isNotEmpty() && searchQuery.isBlank())
                                    "🗒️ Boshqalar"
                                else "🗒️ Barcha eslatmalar"
                            )
                        }
                        item { Spacer(Modifier.height(0.dp)) }

                        items(unpinnedNotes, key = { "note_${it.id}" }) { note ->
                            NoteCard(
                                note        = note,
                                onDelete    = { viewModel.deleteNote(note) },
                                onTogglePin = { viewModel.togglePin(note) }
                            )
                        }
                    }
                }
            }
        }

        // ── FAB ───────────────────────────────────────────────────────────
        FloatingActionButton(
            onClick        = viewModel::showAddSheet,
            modifier       = Modifier
                .align(Alignment.BottomEnd)
                .padding(24.dp),
            containerColor = AccentAmber,
            contentColor   = DarkBackground,
            shape          = CircleShape
        ) {
            Icon(Icons.Default.Add, contentDescription = "Eslatma qo'shish")
        }

        // ── Bottom sheet ──────────────────────────────────────────────────
        if (showAddSheet) {
            AddNoteBottomSheet(
                onDismiss = viewModel::hideAddSheet,
                onConfirm = { title, content, color, reminderAt ->
                    viewModel.addNote(title, content, color, reminderAt)
                }
            )
        }
    }
}

// ── Note Card ─────────────────────────────────────────────────────────────────

@Composable
fun NoteCard(
    note: Note,
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
            .padding(12.dp)
    ) {
        Column {
            // Pin + Delete row
            Row(
                modifier              = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment     = Alignment.Top
            ) {
                if (note.title.isNotBlank()) {
                    Text(
                        text       = note.title,
                        style      = MaterialTheme.typography.titleSmall,
                        color      = TextPrimary,
                        fontWeight = FontWeight.SemiBold,
                        maxLines   = 2,
                        overflow   = TextOverflow.Ellipsis,
                        modifier   = Modifier.weight(1f)
                    )
                }
                Column(horizontalAlignment = Alignment.End) {
                    IconButton(
                        onClick  = onTogglePin,
                        modifier = Modifier.size(24.dp)
                    ) {
                        Icon(
                            imageVector        = Icons.Default.PushPin,
                            contentDescription = "Pin",
                            tint               = if (note.isPinned) AccentAmber else TextHint,
                            modifier           = Modifier.size(14.dp)
                        )
                    }
                    IconButton(
                        onClick  = onDelete,
                        modifier = Modifier.size(24.dp)
                    ) {
                        Icon(
                            imageVector        = Icons.Default.Delete,
                            contentDescription = "O'chirish",
                            tint               = TextHint,
                            modifier           = Modifier.size(14.dp)
                        )
                    }
                }
            }

            if (note.content.isNotBlank()) {
                Spacer(Modifier.height(6.dp))
                Text(
                    text     = note.preview,
                    style    = MaterialTheme.typography.bodySmall,
                    color    = TextSecondary,
                    maxLines = 6,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Spacer(Modifier.height(8.dp))

            // Date + reminder
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment     = Alignment.CenterVertically
            ) {
                Text(
                    text  = SimpleDateFormat("d MMM", Locale("uz")).format(Date(note.updatedAt)),
                    style = MaterialTheme.typography.labelSmall,
                    color = TextHint
                )
                if (note.reminderAt != null) {
                    Text(
                        text  = "🔔 ${SimpleDateFormat("d MMM HH:mm", Locale("uz")).format(Date(note.reminderAt))}",
                        style = MaterialTheme.typography.labelSmall,
                        color = AccentAmber
                    )
                }
            }
        }
    }
}

// ── Add Note Bottom Sheet ─────────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddNoteBottomSheet(
    onDismiss: () -> Unit,
    onConfirm: (title: String, content: String, color: Int, reminderAt: Long?) -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    var title           by remember { mutableStateOf("") }
    var content         by remember { mutableStateOf("") }
    var selectedColor   by remember { mutableIntStateOf(0) }
    var selectedReminder by remember { mutableStateOf<Long?>(null) }

    // Two-step reminder picker: 1 = date, 2 = time
    var reminderStep    by remember { mutableIntStateOf(0) }
    val dpState         = rememberDatePickerState(
        initialSelectedDateMillis = System.currentTimeMillis()
    )
    val now             = Calendar.getInstance()
    val tpState         = rememberTimePickerState(
        initialHour   = now.get(Calendar.HOUR_OF_DAY),
        initialMinute = now.get(Calendar.MINUTE),
        is24Hour      = true
    )

    // ── Sana tanlash ─────────────────────────────────────────────────────────
    if (reminderStep == 1) {
        DatePickerDialog(
            onDismissRequest = { reminderStep = 0 },
            confirmButton = {
                TextButton(onClick = { reminderStep = 2 }) {
                    Text("Davom →", color = AccentAmber)
                }
            },
            dismissButton = {
                TextButton(onClick = { reminderStep = 0 }) {
                    Text("Bekor", color = TextSecondary)
                }
            }
        ) {
            DatePicker(state = dpState)
        }
    }

    // ── Vaqt tanlash ─────────────────────────────────────────────────────────
    if (reminderStep == 2) {
        AlertDialog(
            onDismissRequest = { reminderStep = 0 },
            containerColor   = DarkSurface,
            title = {
                Text(
                    "🔔  Eslatma vaqti",
                    color      = TextPrimary,
                    fontWeight = FontWeight.SemiBold
                )
            },
            text = {
                Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                    TimePicker(state = tpState)
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    val baseMs = dpState.selectedDateMillis?.let { DateUtils.startOfDay(it) }
                        ?: DateUtils.startOfDay()
                    val cal = Calendar.getInstance().apply {
                        timeInMillis = baseMs
                        set(Calendar.HOUR_OF_DAY, tpState.hour)
                        set(Calendar.MINUTE,      tpState.minute)
                        set(Calendar.SECOND,      0)
                        set(Calendar.MILLISECOND, 0)
                    }
                    selectedReminder = if (cal.timeInMillis > System.currentTimeMillis()) {
                        cal.timeInMillis
                    } else null
                    reminderStep = 0
                }) { Text("Saqlash", color = AccentAmber) }
            },
            dismissButton = {
                TextButton(onClick = { reminderStep = 1 }) {
                    Text("← Orqaga", color = TextSecondary)
                }
            }
        )
    }

    // ── ModalBottomSheet ─────────────────────────────────────────────────────
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState       = sheetState,
        containerColor   = DarkSurface,
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
                text       = "Yangi eslatma",
                style      = MaterialTheme.typography.titleLarge,
                color      = TextPrimary,
                fontWeight = FontWeight.Bold
            )

            // ── Sarlavha ──────────────────────────────────────────────────────
            OutlinedTextField(
                value         = title,
                onValueChange = { title = it },
                placeholder   = { Text("Sarlavha (ixtiyoriy)...", color = TextHint) },
                modifier      = Modifier.fillMaxWidth(),
                colors        = searchFieldColors(),
                singleLine    = true,
                shape         = RoundedCornerShape(12.dp)
            )

            // ── Matn ──────────────────────────────────────────────────────────
            OutlinedTextField(
                value         = content,
                onValueChange = { content = it },
                placeholder   = { Text("Fikrlaringizni yozing...", color = TextHint) },
                modifier      = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 120.dp),
                colors        = searchFieldColors(),
                maxLines      = 8,
                shape         = RoundedCornerShape(12.dp)
            )

            // ── Rang ──────────────────────────────────────────────────────────
            Column {
                Text("Rang", style = MaterialTheme.typography.labelMedium, color = TextSecondary)
                Spacer(Modifier.height(8.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    NoteCardColors.forEachIndexed { idx, color ->
                        Box(
                            modifier = Modifier
                                .size(if (selectedColor == idx) 34.dp else 28.dp)
                                .clip(CircleShape)
                                .background(color)
                                .clickable { selectedColor = idx }
                        ) {
                            if (selectedColor == idx) {
                                Box(
                                    Modifier.fillMaxSize(),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text("✓", color = TextPrimary, fontSize = 14.sp)
                                }
                            }
                        }
                    }
                }
            }

            // ── Eslatma vaqti ─────────────────────────────────────────────────
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(DarkCard)
                    .clickable { reminderStep = 1 }
                    .padding(horizontal = 14.dp, vertical = 13.dp),
                verticalAlignment     = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment     = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text("🔔", fontSize = 18.sp)
                    Text(
                        text  = selectedReminder?.let { formatReminderLabel(it) }
                            ?: "Eslatma qo'shish (ixtiyoriy)",
                        style = MaterialTheme.typography.bodyMedium,
                        color = if (selectedReminder != null) TextPrimary else TextHint
                    )
                }
                if (selectedReminder != null) {
                    IconButton(
                        onClick  = { selectedReminder = null },
                        modifier = Modifier.size(24.dp)
                    ) {
                        Icon(
                            Icons.Default.Clear,
                            contentDescription = "Tozalash",
                            tint               = TextHint,
                            modifier           = Modifier.size(16.dp)
                        )
                    }
                }
            }

            // ── Saqlash tugmasi ───────────────────────────────────────────────
            Button(
                onClick = {
                    if (title.isNotBlank() || content.isNotBlank()) {
                        onConfirm(title, content, selectedColor, selectedReminder)
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                shape  = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = AccentAmber,
                    contentColor   = DarkBackground
                )
            ) {
                Text("Saqlash", fontWeight = FontWeight.SemiBold, fontSize = 16.sp)
            }
        }
    }
}

// ── Helpers ───────────────────────────────────────────────────────────────────

@Composable
private fun SectionLabel(text: String) {
    Text(
        text     = text,
        style    = MaterialTheme.typography.labelMedium,
        color    = TextSecondary,
        modifier = Modifier.padding(bottom = 4.dp)
    )
}

@Composable
private fun EmptyNotesPlaceholder() {
    Column(
        modifier              = Modifier.fillMaxWidth().padding(top = 64.dp),
        horizontalAlignment   = Alignment.CenterHorizontally,
        verticalArrangement   = Arrangement.spacedBy(8.dp)
    ) {
        Text("📝", fontSize = 48.sp)
        Text("Hali eslatma yo'q", style = MaterialTheme.typography.titleSmall, color = TextSecondary)
        Text("FAB orqali birinchi eslatmani qo'shing", style = MaterialTheme.typography.bodySmall, color = TextHint)
    }
}

private fun formatReminderLabel(timestamp: Long): String =
    SimpleDateFormat("d MMM, HH:mm", Locale("uz")).format(Date(timestamp))

@Composable
private fun searchFieldColors() = OutlinedTextFieldDefaults.colors(
    focusedBorderColor   = AccentAmber,
    unfocusedBorderColor = DividerColor,
    cursorColor          = AccentAmber,
    focusedTextColor     = TextPrimary,
    unfocusedTextColor   = TextPrimary
)
