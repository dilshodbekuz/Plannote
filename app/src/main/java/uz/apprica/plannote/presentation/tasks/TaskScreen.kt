package uz.apprica.plannote.presentation.tasks

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import uz.apprica.plannote.domain.model.Priority
import uz.apprica.plannote.domain.model.Task
import uz.apprica.plannote.ui.theme.*
import kotlin.math.roundToInt

// ─────────────────────────────────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TaskScreen(viewModel: TaskViewModel = hiltViewModel()) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

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

            Spacer(Modifier.height(20.dp))

            Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                CircularProgressRing(
                    progress = state.progressFraction,
                    done     = state.completedCount,
                    total    = state.totalCount
                )
            }

            Spacer(Modifier.height(20.dp))

            if (state.tasks.isEmpty() && !state.isLoading) {
                EmptyTasksPlaceholder()
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    contentPadding      = PaddingValues(bottom = 88.dp)
                ) {
                    items(state.tasks, key = { it.id }) { task ->
                        TaskItem(
                            task     = task,
                            onToggle = { viewModel.toggleTask(task.id, it) },
                            onDelete = { viewModel.deleteTask(task) }
                        )
                    }
                }
            }
        }

        FloatingActionButton(
            onClick        = viewModel::showAddSheet,
            modifier       = Modifier
                .align(Alignment.BottomEnd)
                .padding(24.dp),
            containerColor = PrimaryTeal,
            contentColor   = DarkBackground,
            shape          = CircleShape
        ) {
            Icon(Icons.Default.Add, contentDescription = "Vazifa qo'shish")
        }

        if (state.showAddSheet) {
            AddTaskBottomSheet(
                onDismiss = viewModel::hideAddSheet,
                onConfirm = { title, category, priority ->
                    viewModel.addTask(title, category, priority)
                }
            )
        }
    }
}

// ── Circular Progress Ring ────────────────────────────────────────────────────

@Composable
fun CircularProgressRing(
    progress: Float,
    done: Int,
    total: Int,
    modifier: Modifier = Modifier,
    size: androidx.compose.ui.unit.Dp = 148.dp,
    strokeDp: androidx.compose.ui.unit.Dp = 14.dp
) {
    val animatedProgress by animateFloatAsState(
        targetValue   = progress.coerceIn(0f, 1f),
        animationSpec = tween(1200, easing = FastOutSlowInEasing),
        label         = "ring"
    )
    val strokePx = with(LocalDensity.current) { strokeDp.toPx() }

    Box(modifier = modifier.size(size), contentAlignment = Alignment.Center) {
        androidx.compose.foundation.Canvas(modifier = Modifier.fillMaxSize()) {
            val radius = (this.size.minDimension - strokePx) / 2f
            val center = Offset(this.size.width / 2f, this.size.height / 2f)
            drawCircle(
                color  = DarkCard,
                radius = radius,
                center = center,
                style  = Stroke(strokePx, cap = StrokeCap.Round)
            )
            if (animatedProgress > 0f) {
                drawArc(
                    color      = PrimaryTeal,
                    startAngle = -90f,
                    sweepAngle = 360f * animatedProgress,
                    useCenter  = false,
                    topLeft    = Offset(center.x - radius, center.y - radius),
                    size       = Size(radius * 2, radius * 2),
                    style      = Stroke(strokePx, cap = StrokeCap.Round)
                )
            }
        }
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text       = "${(animatedProgress * 100).roundToInt()}%",
                style      = MaterialTheme.typography.headlineSmall,
                color      = PrimaryTeal,
                fontWeight = FontWeight.Bold
            )
            Text("$done / $total", style = MaterialTheme.typography.bodySmall,  color = TextSecondary)
            Text("bajarildi",      style = MaterialTheme.typography.labelSmall, color = TextSecondary)
        }
    }
}

// ── Task Item ─────────────────────────────────────────────────────────────────

@Composable
fun TaskItem(
    task: Task,
    onToggle: (Boolean) -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier
) {
    val done         = task.isCompleted
    val priorityTint = task.priority.color()

    Card(
        modifier  = modifier.fillMaxWidth(),
        shape     = RoundedCornerShape(16.dp),
        colors    = CardDefaults.cardColors(
            containerColor = if (done) DarkCardAlt else DarkCard
        ),
        elevation = CardDefaults.cardElevation(0.dp)
    ) {
        Row(
            modifier          = Modifier
                .fillMaxWidth()
                .height(IntrinsicSize.Min),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // ── Priority bar ──────────────────────────────────────────────
            Box(
                modifier = Modifier
                    .width(5.dp)
                    .fillMaxHeight()
                    .background(priorityTint.copy(alpha = if (done) 0.25f else 0.85f))
            )

            // ── Checkbox ──────────────────────────────────────────────────
            Checkbox(
                checked         = done,
                onCheckedChange = onToggle,
                modifier        = Modifier.alpha(if (done) 0.55f else 1f),
                colors          = CheckboxDefaults.colors(
                    checkedColor   = PrimaryTeal,
                    uncheckedColor = TextSecondary,
                    checkmarkColor = DarkBackground
                )
            )

            // ── Content ───────────────────────────────────────────────────
            Column(
                modifier = Modifier
                    .weight(1f)
                    .alpha(if (done) 0.50f else 1f)
                    .padding(top = 12.dp, bottom = 12.dp, end = 4.dp)
            ) {
                // Kategoriya — yuqorida, katta, ko'rinadigan
                if (task.category.isNotBlank()) {
                    Text(
                        text       = task.category,
                        fontSize   = 12.sp,
                        color      = TextSecondary,
                        fontWeight = FontWeight.Medium,
                        letterSpacing = 0.3.sp
                    )
                    Spacer(Modifier.height(3.dp))
                }

                // Sarlavha
                Text(
                    text           = task.title,
                    style          = MaterialTheme.typography.bodyLarge,
                    color          = TextPrimary,
                    fontWeight     = if (done) FontWeight.Normal else FontWeight.Medium,
                    textDecoration = if (done) TextDecoration.LineThrough else null,
                    maxLines       = 2,
                    overflow       = TextOverflow.Ellipsis
                )

                // Priority — pastda, kichik, rangdor
                Spacer(Modifier.height(3.dp))
                Text(
                    text       = task.priority.label(),
                    fontSize   = 11.sp,
                    color      = priorityTint,
                    fontWeight = FontWeight.SemiBold
                )
            }

            // ── Delete ────────────────────────────────────────────────────
            IconButton(
                onClick  = onDelete,
                modifier = Modifier
                    .size(46.dp)
                    .padding(end = 6.dp)
            ) {
                Icon(
                    imageVector        = Icons.Default.Delete,
                    contentDescription = "O'chirish",
                    tint               = ErrorRed.copy(alpha = if (done) 0.25f else 0.60f),
                    modifier           = Modifier.size(20.dp)
                )
            }
        }
    }
}

// ── Add Task Bottom Sheet ─────────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddTaskBottomSheet(
    onDismiss: () -> Unit,
    onConfirm: (title: String, category: String, priority: Priority) -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    var title      by remember { mutableStateOf("") }
    var category   by remember { mutableStateOf("Shaxsiy") }
    var priority   by remember { mutableStateOf(Priority.MEDIUM) }
    var titleError by remember { mutableStateOf(false) }

    val categories = listOf("Ish", "Shaxsiy", "Salomatlik", "Ta'lim", "Boshqa")
    val priorities = listOf(
        Priority.LOW    to "Past",
        Priority.MEDIUM to "O'rta",
        Priority.HIGH   to "Yuqori"
    )

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
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text       = "Yangi vazifa",
                style      = MaterialTheme.typography.titleLarge,
                color      = TextPrimary,
                fontWeight = FontWeight.Bold
            )

            // ── Nom ──────────────────────────────────────────────────────────
            OutlinedTextField(
                value          = title,
                onValueChange  = { title = it; titleError = false },
                placeholder    = { Text("Vazifa nomi *", color = TextHint) },
                isError        = titleError,
                supportingText = if (titleError) ({ Text("Nom kiritish shart!") }) else null,
                modifier       = Modifier.fillMaxWidth(),
                colors         = taskFieldColors(),
                singleLine     = true,
                shape          = RoundedCornerShape(12.dp)
            )

            // ── Kategoriya ───────────────────────────────────────────────────
            Column {
                Text(
                    "Kategoriya",
                    style = MaterialTheme.typography.labelMedium,
                    color = TextSecondary
                )
                Spacer(Modifier.height(8.dp))
                Row(
                    modifier              = Modifier.horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    categories.forEach { cat ->
                        FilterChip(
                            selected = category == cat,
                            onClick  = { category = cat },
                            label    = { Text(cat, fontSize = 12.sp) },
                            colors   = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = PrimaryTeal,
                                selectedLabelColor     = DarkBackground,
                                containerColor         = DarkCard,
                                labelColor             = TextSecondary
                            )
                        )
                    }
                }
            }

            // ── Muhimlik ─────────────────────────────────────────────────────
            Column {
                Text(
                    "Muhimlik",
                    style = MaterialTheme.typography.labelMedium,
                    color = TextSecondary
                )
                Spacer(Modifier.height(8.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    priorities.forEach { (p, label) ->
                        FilterChip(
                            selected = priority == p,
                            onClick  = { priority = p },
                            label    = { Text(label, fontSize = 12.sp) },
                            colors   = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = p.color(),
                                selectedLabelColor     = DarkBackground,
                                containerColor         = DarkCard,
                                labelColor             = TextSecondary
                            )
                        )
                    }
                }
            }

            // ── Qo'shish ─────────────────────────────────────────────────────
            Button(
                onClick = {
                    if (title.isBlank()) { titleError = true; return@Button }
                    onConfirm(title.trim(), category, priority)
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                shape  = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = PrimaryTeal,
                    contentColor   = DarkBackground
                )
            ) {
                Text("Qo'shish", fontWeight = FontWeight.SemiBold, fontSize = 16.sp)
            }
        }
    }
}

// ── Empty state ───────────────────────────────────────────────────────────────

@Composable
private fun EmptyTasksPlaceholder() {
    Column(
        modifier              = Modifier
            .fillMaxWidth()
            .padding(top = 48.dp),
        horizontalAlignment   = Alignment.CenterHorizontally,
        verticalArrangement   = Arrangement.spacedBy(8.dp)
    ) {
        Icon(Icons.Default.CheckCircle, contentDescription = null, tint = TextHint, modifier = Modifier.size(64.dp))
        Text(
            text  = "Bugun uchun vazifa yo'q",
            style = MaterialTheme.typography.titleSmall,
            color = TextSecondary
        )
        Text(
            text  = "+ tugmasi orqali vazifa qo'shing",
            style = MaterialTheme.typography.bodySmall,
            color = TextHint
        )
    }
}

// ── Helpers ───────────────────────────────────────────────────────────────────

private fun Priority.color(): Color = when (this) {
    Priority.LOW    -> PriorityLow
    Priority.MEDIUM -> PriorityMedium
    Priority.HIGH   -> PriorityHigh
}

private fun Priority.label(): String = when (this) {
    Priority.LOW    -> "Past"
    Priority.MEDIUM -> "O'rta"
    Priority.HIGH   -> "Yuqori"
}

@Composable
private fun taskFieldColors() = OutlinedTextFieldDefaults.colors(
    focusedBorderColor   = PrimaryTeal,
    unfocusedBorderColor = DividerColor,
    cursorColor          = PrimaryTeal,
    focusedLabelColor    = PrimaryTeal,
    focusedTextColor     = TextPrimary,
    unfocusedTextColor   = TextPrimary
)
