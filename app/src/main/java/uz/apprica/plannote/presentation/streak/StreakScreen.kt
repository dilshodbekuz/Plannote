package uz.apprica.plannote.presentation.streak

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Whatshot
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import uz.apprica.plannote.domain.model.HabitStats
import uz.apprica.plannote.domain.model.OverallStats
import uz.apprica.plannote.domain.model.TaskDayStats
import uz.apprica.plannote.ui.theme.*
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

// ─────────────────────────────────────────────────────────────────────────────

@Composable
fun StreakScreen(
    onNavigateBack: () -> Unit = {},
    viewModel: StreakViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBackground)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Spacer(Modifier.height(4.dp))

        // ── Header + Back ─────────────────────────────────────────────────────
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier          = Modifier.fillMaxWidth()
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(DarkCard)
                    .clickable(onClick = onNavigateBack),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector        = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Orqaga",
                    tint               = TextSecondary,
                    modifier           = Modifier.size(18.dp)
                )
            }
            Spacer(Modifier.width(12.dp))
            Text(
                text       = "🔥  Streak va Faollik",
                style      = MaterialTheme.typography.headlineSmall,
                color      = TextPrimary,
                fontWeight = FontWeight.Bold
            )
        }

        // ── Hero card: streak + rekord + 7-kunlik dots ────────────────────────
        HeroStreakCard(
            currentStreak  = state.currentStreak,
            bestStreak     = state.bestStreak,
            weeklyActivity = state.weeklyActivity
        )

        // ── Haftalik vazifa faolligi (Canvas bar chart) ───────────────────────
        if (state.weeklyTaskStats.isNotEmpty()) {
            StreakSectionCard(title = "Haftalik vazifa faolligi") {
                WeeklyCanvasBarChart(days = state.weeklyTaskStats)
            }
        }

        // ── Odat tracker ──────────────────────────────────────────────────────
        if (state.habitStats.isNotEmpty()) {
            StreakSectionCard(title = "Odat tracker") {
                StreakHabitHeader()
                HorizontalDivider(
                    color    = DividerColor,
                    modifier = Modifier.padding(vertical = 4.dp)
                )
                state.habitStats.forEach { hs ->
                    StreakHabitDotRow(habitStats = hs)
                }
            }
        }

        // ── Haftalik kayfiyat ─────────────────────────────────────────────────
        StreakSectionCard(title = "Haftalik kayfiyat") {
            StreakMoodRow(moodHistory = state.moodHistory)
        }

        Spacer(Modifier.height(16.dp))
    }
}

// ── Hero Streak Card ──────────────────────────────────────────────────────────

@Composable
private fun HeroStreakCard(
    currentStreak: Int,
    bestStreak: Int,
    weeklyActivity: List<Boolean>
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(
                Brush.horizontalGradient(
                    listOf(
                        AccentAmber.copy(alpha = 0.20f),
                        PrimaryTeal.copy(alpha = 0.10f)
                    )
                )
            )
            .padding(20.dp)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(20.dp)) {

            // ── Streak + Rekord ───────────────────────────────────────────────
            Row(
                modifier              = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment     = Alignment.CenterVertically
            ) {
                // Joriy streak
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Default.Whatshot, contentDescription = null, tint = AccentAmber, modifier = Modifier.size(40.dp))
                    Spacer(Modifier.height(4.dp))
                    Text(
                        text       = "$currentStreak",
                        style      = MaterialTheme.typography.displaySmall,
                        color      = TextPrimary,
                        fontWeight = FontWeight.ExtraBold
                    )
                    Text(
                        text  = "kun",
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextSecondary
                    )
                    Spacer(Modifier.height(2.dp))
                    Text(
                        text  = "Ketma-ket",
                        style = MaterialTheme.typography.labelSmall,
                        color = AccentAmber
                    )
                }

                // Vertikal chiziq
                Box(
                    modifier = Modifier
                        .height(80.dp)
                        .width(1.dp)
                        .background(DividerColor)
                )

                // Rekord streak
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Default.EmojiEvents, contentDescription = null, tint = AccentAmber, modifier = Modifier.size(40.dp))
                    Spacer(Modifier.height(4.dp))
                    Text(
                        text       = "$bestStreak",
                        style      = MaterialTheme.typography.displaySmall,
                        color      = AccentAmber,
                        fontWeight = FontWeight.ExtraBold
                    )
                    Text(
                        text  = "kun",
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextSecondary
                    )
                    Spacer(Modifier.height(2.dp))
                    Text(
                        text  = "Rekord",
                        style = MaterialTheme.typography.labelSmall,
                        color = TextHint
                    )
                }
            }

            HorizontalDivider(color = DividerColor.copy(alpha = 0.6f))

            // ── So'nggi 7 kun dots ────────────────────────────────────────────
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    text  = "So'nggi 7 kun",
                    style = MaterialTheme.typography.labelMedium,
                    color = TextSecondary
                )
                Row(
                    modifier              = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    weeklyActivity.forEachIndexed { index, active ->
                        val isToday = index == weeklyActivity.lastIndex
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(if (isToday) 14.dp else 12.dp)
                                    .clip(CircleShape)
                                    .background(
                                        when {
                                            isToday && active -> AccentAmber
                                            active            -> PrimaryTeal
                                            else              -> DarkCardAlt
                                        }
                                    )
                            )
                            Text(
                                text       = realDayLabel(index),
                                fontSize   = 9.sp,
                                color      = when {
                                    isToday -> AccentAmber
                                    active  -> TextSecondary
                                    else    -> TextHint
                                },
                                fontWeight = if (isToday) FontWeight.SemiBold else FontWeight.Normal
                            )
                        }
                    }
                }
            }
        }
    }
}

// ── Canvas Bar Chart ──────────────────────────────────────────────────────────

@Composable
private fun WeeklyCanvasBarChart(days: List<TaskDayStats>) {
    // Animatsiya calllarini doim 7 ta chaqiramiz (early return yo'q — Compose qoidasi)
    val a0 by animateFloatAsState(
        targetValue   = days.getOrNull(0)?.completionRate ?: 0f,
        animationSpec = tween(durationMillis = 800, delayMillis =   0, easing = FastOutSlowInEasing),
        label         = "cb0"
    )
    val a1 by animateFloatAsState(
        targetValue   = days.getOrNull(1)?.completionRate ?: 0f,
        animationSpec = tween(durationMillis = 800, delayMillis =  80, easing = FastOutSlowInEasing),
        label         = "cb1"
    )
    val a2 by animateFloatAsState(
        targetValue   = days.getOrNull(2)?.completionRate ?: 0f,
        animationSpec = tween(durationMillis = 800, delayMillis = 160, easing = FastOutSlowInEasing),
        label         = "cb2"
    )
    val a3 by animateFloatAsState(
        targetValue   = days.getOrNull(3)?.completionRate ?: 0f,
        animationSpec = tween(durationMillis = 800, delayMillis = 240, easing = FastOutSlowInEasing),
        label         = "cb3"
    )
    val a4 by animateFloatAsState(
        targetValue   = days.getOrNull(4)?.completionRate ?: 0f,
        animationSpec = tween(durationMillis = 800, delayMillis = 320, easing = FastOutSlowInEasing),
        label         = "cb4"
    )
    val a5 by animateFloatAsState(
        targetValue   = days.getOrNull(5)?.completionRate ?: 0f,
        animationSpec = tween(durationMillis = 800, delayMillis = 400, easing = FastOutSlowInEasing),
        label         = "cb5"
    )
    val a6 by animateFloatAsState(
        targetValue   = days.getOrNull(6)?.completionRate ?: 0f,
        animationSpec = tween(durationMillis = 800, delayMillis = 480, easing = FastOutSlowInEasing),
        label         = "cb6"
    )

    val fracs = listOf(a0, a1, a2, a3, a4, a5, a6)

    if (days.isEmpty()) return   // barcha @Composable calllardan KEYIN — xavfsiz

    // Canvas uchun rang qiymatlarini tashqarida olamiz
    val bgCol    = DarkCardAlt
    val fillCol  = PrimaryTeal
    val todayCol = AccentAmber

    Column {
        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .height(120.dp)
        ) {
            val n      = 7
            val gap    = 10.dp.toPx()
            val barW   = (size.width - gap * (n - 1)) / n
            val maxH   = size.height
            val cr     = CornerRadius(barW / 3f, barW / 3f)

            repeat(n) { i ->
                val day  = days.getOrNull(i) ?: return@repeat
                val x    = i * (barW + gap)
                val frac = fracs.getOrElse(i) { 0f }

                // Orqa (bo'sh) bar
                drawRoundRect(
                    color        = bgCol,
                    topLeft      = Offset(x, 0f),
                    size         = Size(barW, maxH),
                    cornerRadius = cr
                )

                // To'ldirilgan qism
                val fillH = (maxH * frac).coerceAtLeast(
                    if (day.total > 0) 4f else 0f
                )
                if (fillH > 0f) {
                    drawRoundRect(
                        color        = if (day.isToday) todayCol else fillCol,
                        topLeft      = Offset(x, maxH - fillH),
                        size         = Size(barW, fillH),
                        cornerRadius = cr
                    )
                }
            }
        }

        Spacer(Modifier.height(8.dp))

        // Kun nomlari
        Row(
            modifier              = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            days.forEach { day ->
                Text(
                    text       = day.dayLabel,
                    fontSize   = 10.sp,
                    color      = if (day.isToday) AccentAmber else TextSecondary,
                    fontWeight = if (day.isToday) FontWeight.Bold else FontWeight.Normal,
                    modifier   = Modifier.weight(1f),
                    textAlign  = TextAlign.Center
                )
            }
        }

        Spacer(Modifier.height(4.dp))

        // Bajarilish raqamlari
        Row(
            modifier              = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            days.forEach { day ->
                Text(
                    text      = if (day.total > 0) "${day.completed}/${day.total}" else "–",
                    fontSize  = 8.sp,
                    color     = if (day.isToday) AccentAmber.copy(alpha = 0.8f) else TextHint,
                    modifier  = Modifier.weight(1f),
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

// ── Habit Tracker ─────────────────────────────────────────────────────────────

@Composable
private fun StreakHabitHeader() {
    Row(
        modifier              = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Spacer(Modifier.width(160.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
            listOf("D", "S", "C", "P", "J", "Sh", "Y").forEach { lbl ->
                Text(
                    text     = lbl,
                    style    = MaterialTheme.typography.labelSmall,
                    color    = TextHint,
                    modifier = Modifier.width(16.dp),
                    fontSize = 9.sp
                )
            }
        }
    }
}

@Composable
private fun StreakHabitDotRow(habitStats: HabitStats) {
    Row(
        modifier          = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text     = habitStats.iconEmoji,
            fontSize = 18.sp,
            modifier = Modifier.width(28.dp)
        )
        Spacer(Modifier.width(6.dp))
        Column(modifier = Modifier.width(120.dp)) {
            Text(
                text       = habitStats.habitName,
                style      = MaterialTheme.typography.bodySmall,
                color      = TextPrimary,
                maxLines   = 1,
                fontWeight = FontWeight.Medium
            )
            Text(
                text  = "🔥 ${habitStats.currentStreak} kun · ${(habitStats.percentage * 100).toInt()}%",
                style = MaterialTheme.typography.labelSmall,
                color = AccentAmber
            )
        }
        Spacer(Modifier.weight(1f))
        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
            habitStats.weekDays.forEach { done ->
                Box(
                    modifier = Modifier
                        .size(14.dp)
                        .clip(CircleShape)
                        .background(if (done) PrimaryTeal else DarkCardAlt)
                )
            }
        }
    }
}

// ── Mood Row ──────────────────────────────────────────────────────────────────

private val SMOOD_EMOJIS = listOf("😢", "😐", "😊", "😄", "🤩")
private val SMOOD_COLORS = listOf(
    Color(0xFFEF5350),   // 1 — qizil
    Color(0xFFBDBDBD),   // 2 — kulrang
    Color(0xFF66BB6A),   // 3 — yashil
    Color(0xFF29B6F6),   // 4 — ko'k
    Color(0xFFFFB347)    // 5 — sariq
)
private val MOOD_DATE_FMT = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

@Composable
private fun StreakMoodRow(moodHistory: Map<String, Int>) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Row(
            modifier              = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            (0..6).forEach { index ->
                val daysAgo = 6 - index
                val cal     = Calendar.getInstance().apply { add(Calendar.DAY_OF_YEAR, -daysAgo) }
                val dateStr = MOOD_DATE_FMT.format(cal.time)
                val mood    = moodHistory[dateStr]
                val isToday = daysAgo == 0

                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Box(
                        modifier         = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(
                                if (mood != null)
                                    SMOOD_COLORS[mood - 1].copy(alpha = 0.18f)
                                else
                                    DarkCardAlt
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        if (mood != null) {
                            Text(SMOOD_EMOJIS[mood - 1], fontSize = 20.sp)
                        } else {
                            Text("·", fontSize = 20.sp, color = TextHint)
                        }
                    }
                    Text(
                        text       = realDayLabel(index),
                        fontSize   = 9.sp,
                        color      = if (isToday) AccentAmber else TextHint,
                        fontWeight = if (isToday) FontWeight.SemiBold else FontWeight.Normal
                    )
                }
            }
        }

        // Hech qaysi kunda kayfiyat belgilanmasa
        val anyMood = (0..6).any { i ->
            val cal = Calendar.getInstance().apply { add(Calendar.DAY_OF_YEAR, -(6 - i)) }
            moodHistory[MOOD_DATE_FMT.format(cal.time)] != null
        }
        if (!anyMood) {
            Text(
                text  = "Hali kayfiyat belgilanmagan. Home ekranidan belgilang!",
                style = MaterialTheme.typography.labelSmall,
                color = TextHint
            )
        }
    }
}

// ── Overall Stats ─────────────────────────────────────────────────────────────

@Composable
private fun StreakOverallSection(stats: OverallStats) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text(
            text       = "Umumiy statistika",
            style      = MaterialTheme.typography.titleSmall,
            color      = TextPrimary,
            fontWeight = FontWeight.SemiBold
        )
        Row(
            modifier              = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            StreakMiniCard(
                modifier   = Modifier.weight(1f),
                emoji      = "✅",
                label      = "Bajarilgan vazifalar",
                value      = "${stats.completedTasks}",
                sub        = "${stats.totalTasks} tadan",
                valueColor = PrimaryTeal
            )
            StreakMiniCard(
                modifier   = Modifier.weight(1f),
                emoji      = "📝",
                label      = "Eslatmalar",
                value      = "${stats.totalNotes}",
                sub        = "jami yozilgan",
                valueColor = AccentAmber
            )
        }
        Row(
            modifier              = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            StreakMiniCard(
                modifier   = Modifier.weight(1f),
                emoji      = "🏆",
                label      = "Eng yaxshi streak",
                value      = "${stats.bestStreak} kun",
                sub        = "shaxsiy rekord",
                valueColor = AccentAmber
            )
            StreakMiniCard(
                modifier   = Modifier.weight(1f),
                emoji      = "📊",
                label      = "Bajarilish foizi",
                value      = "${stats.completionPercent.toInt()}%",
                sub        = "${stats.completedTasks}/${stats.totalTasks}",
                valueColor = if (stats.completionPercent >= 70f) PrimaryTeal else AccentAmber
            )
        }
    }
}

@Composable
private fun StreakMiniCard(
    modifier: Modifier,
    emoji: String,
    label: String,
    value: String,
    sub: String,
    valueColor: Color
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .background(DarkCard)
            .padding(14.dp)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(text = emoji, fontSize = 22.sp)
            Text(
                text       = value,
                style      = MaterialTheme.typography.titleMedium,
                color      = valueColor,
                fontWeight = FontWeight.Bold
            )
            Text(
                text  = label,
                style = MaterialTheme.typography.labelSmall,
                color = TextSecondary
            )
            Text(
                text  = sub,
                style = MaterialTheme.typography.labelSmall,
                color = TextHint
            )
        }
    }
}

// ── Section Card ──────────────────────────────────────────────────────────────

@Composable
private fun StreakSectionCard(
    title: String,
    content: @Composable ColumnScope.() -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(DarkCard)
            .padding(16.dp)
    ) {
        Column {
            Text(
                text       = title,
                style      = MaterialTheme.typography.titleSmall,
                color      = TextPrimary,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(Modifier.height(16.dp))
            content()
        }
    }
}

// ── Helpers ───────────────────────────────────────────────────────────────────

/**
 * index 0 = 6 kun oldin … index 6 = bugun
 * Haqiqiy hafta kunini dinamik qaytaradi.
 */
private fun realDayLabel(index: Int): String {
    val daysAgo = 6 - index
    val cal     = Calendar.getInstance().apply { add(Calendar.DAY_OF_YEAR, -daysAgo) }
    return when (cal.get(Calendar.DAY_OF_WEEK)) {
        Calendar.MONDAY    -> "Du"
        Calendar.TUESDAY   -> "Se"
        Calendar.WEDNESDAY -> "Ch"
        Calendar.THURSDAY  -> "Pa"
        Calendar.FRIDAY    -> "Ju"
        Calendar.SATURDAY  -> "Sh"
        Calendar.SUNDAY    -> "Ya"
        else               -> "?"
    }
}
