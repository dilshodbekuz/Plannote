package uz.apprica.plannote.presentation.stats

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import uz.apprica.plannote.domain.model.DayStats
import uz.apprica.plannote.domain.model.HabitStats
import uz.apprica.plannote.domain.model.HabitWeeklyData
import uz.apprica.plannote.domain.model.OverallStats
import uz.apprica.plannote.domain.model.TaskDayStats
import uz.apprica.plannote.ui.theme.*
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

@Composable
fun StatsScreen(viewModel: StatsViewModel = hiltViewModel()) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBackground)
            .verticalScroll(rememberScrollState())
            .statusBarsPadding()
            .padding(horizontal = 20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Spacer(Modifier.height(16.dp))

        // ── Header ────────────────────────────────────────────────────────────
        Text(
            text       = "Statistika",
            style      = MaterialTheme.typography.headlineSmall,
            color      = TextPrimary,
            fontWeight = FontWeight.Bold
        )
        Text(
            text  = "Joriy hafta natijalari",
            style = MaterialTheme.typography.bodySmall,
            color = TextSecondary
        )

        // ── Streak Card ───────────────────────────────────────────────────────
        StatsStreakCard(
            streak         = state.streak,
            bestStreak     = state.bestStreak,
            weeklyActivity = state.weeklyActivity
        )

        // ── Kayfiyat tarixi ───────────────────────────────────────────────────
        MoodHistorySection(moodHistory = state.moodHistory)

        // ── Haftalik vazifalar bar chart ──────────────────────────────────────
        if (state.weeklyTaskStats.isNotEmpty()) {
            SectionCard(title = "Haftalik vazifalar", badge = null) {
                TaskWeeklyBarChart(days = state.weeklyTaskStats)
            }
        }

        // ── Mavjud habit bar chart ────────────────────────────────────────────
        state.weeklyStats?.let { stats ->
            SectionCard(
                title = "Haftalik odatlar",
                badge = "${(stats.avgCompletionRate * 100).toInt()}% o'rtacha"
            ) {
                WeeklyBarChart(days = stats.days)
            }

            // ── Summary cards ─────────────────────────────────────────────────
            Row(
                modifier              = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                SummaryCard(
                    modifier   = Modifier.weight(1f),
                    emoji      = "🏆",
                    label      = "Eng uzun odat streak",
                    value      = "${stats.bestStreak} kun",
                    valueColor = AccentAmber,
                    subtitle   = stats.bestStreakHabitName.take(14)
                )
                SummaryCard(
                    modifier   = Modifier.weight(1f),
                    emoji      = "✅",
                    label      = "Haftalik vazifalar",
                    value      = "${state.completedTasksThisWeek}/${state.totalTasksThisWeek}",
                    valueColor = PrimaryTeal,
                    subtitle   = "bajarildi"
                )
            }
        }

        // ── Habit tracker (yangi HabitStats bilan) ────────────────────────────
        if (state.habitStats.isNotEmpty()) {
            SectionCard(title = "Odat tracker", badge = null) {
                HabitStatsHeader()
                HorizontalDivider(
                    color    = DividerColor,
                    modifier = Modifier.padding(vertical = 4.dp)
                )
                state.habitStats.forEach { hs ->
                    HabitStatsDotRow(habitStats = hs)
                }
            }
        } else if (state.habitWeeklyData.isNotEmpty()) {
            // Fallback: eski HabitWeeklyData bilan
            SectionCard(title = "Habit tracker", badge = null) {
                HabitStatsHeader()
                HorizontalDivider(
                    color    = DividerColor,
                    modifier = Modifier.padding(vertical = 4.dp)
                )
                state.habitWeeklyData.forEach { habitData ->
                    HabitDotRow(habitData = habitData)
                }
            }
        }

        // ── Umumiy statistika ─────────────────────────────────────────────────
        OverallStatsSection(stats = state.overallStats)

        // ── Empty state ───────────────────────────────────────────────────────
        if (state.habitWeeklyData.isEmpty()
            && state.weeklyStats == null
            && state.habitStats.isEmpty()
        ) {
            EmptyStatsPlaceholder()
        }

        Spacer(Modifier.height(16.dp))
    }
}

// ── Helpers ───────────────────────────────────────────────────────────────────

private val DATE_FMT_STATS = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

/**
 * index 0 = 6 kun oldin … index 6 = bugun
 * Haqiqiy hafta kunini qaytaradi (statik massiv emas).
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

private fun dateStrDaysAgo(daysAgo: Int): String {
    val cal = Calendar.getInstance().apply { add(Calendar.DAY_OF_YEAR, -daysAgo) }
    return DATE_FMT_STATS.format(cal.time)
}

// ── Streak Card ───────────────────────────────────────────────────────────────

@Composable
private fun StatsStreakCard(
    streak: Int,
    bestStreak: Int,
    weeklyActivity: List<Boolean>
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(
                Brush.horizontalGradient(
                    listOf(
                        AccentAmber.copy(alpha = 0.18f),
                        PrimaryTeal.copy(alpha = 0.10f)
                    )
                )
            )
            .padding(20.dp)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {

            // ── Streak raqamlari ──────────────────────────────────────────────
            Row(
                modifier              = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment     = Alignment.CenterVertically
            ) {
                // Joriy streak
                Column {
                    Text(
                        text  = "🔥 Joriy streak",
                        style = MaterialTheme.typography.labelMedium,
                        color = AccentAmber
                    )
                    Spacer(Modifier.height(4.dp))
                    Row(verticalAlignment = Alignment.Bottom) {
                        Text(
                            text       = "$streak",
                            style      = MaterialTheme.typography.headlineLarge,
                            color      = TextPrimary,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text     = " kun",
                            style    = MaterialTheme.typography.titleMedium,
                            color    = TextSecondary,
                            modifier = Modifier.padding(bottom = 6.dp)
                        )
                    }
                }

                // Eng yaxshi streak
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text  = "🏆 Rekord",
                        style = MaterialTheme.typography.labelMedium,
                        color = TextHint
                    )
                    Spacer(Modifier.height(4.dp))
                    Row(verticalAlignment = Alignment.Bottom) {
                        Text(
                            text       = "$bestStreak",
                            style      = MaterialTheme.typography.headlineMedium,
                            color      = AccentAmber,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text     = " kun",
                            style    = MaterialTheme.typography.bodyMedium,
                            color    = TextSecondary,
                            modifier = Modifier.padding(bottom = 4.dp)
                        )
                    }
                }
            }

            // ── 7-kunlik faollik dots ─────────────────────────────────────────
            Row(
                modifier              = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                weeklyActivity.forEachIndexed { index, active ->
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(12.dp)
                                .clip(CircleShape)
                                .background(
                                    when {
                                        index == weeklyActivity.lastIndex && active ->
                                            AccentAmber
                                        active -> PrimaryTeal
                                        else   -> DarkCardAlt
                                    }
                                )
                        )
                        Text(
                            text     = realDayLabel(index),
                            style    = MaterialTheme.typography.labelSmall,
                            color    = if (index == weeklyActivity.lastIndex) AccentAmber
                                       else if (active) TextSecondary else TextHint,
                            fontSize = 9.sp,
                            fontWeight = if (index == weeklyActivity.lastIndex)
                                FontWeight.SemiBold else FontWeight.Normal
                        )
                    }
                }
            }
        }
    }
}

// ── Section Card wrapper ──────────────────────────────────────────────────────

@Composable
private fun SectionCard(
    title: String,
    badge: String?,
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
            Row(
                modifier              = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment     = Alignment.CenterVertically
            ) {
                Text(
                    text       = title,
                    style      = MaterialTheme.typography.titleSmall,
                    color      = TextPrimary,
                    fontWeight = FontWeight.SemiBold
                )
                badge?.let {
                    Text(
                        text  = it,
                        style = MaterialTheme.typography.labelSmall,
                        color = PrimaryTeal
                    )
                }
            }
            Spacer(Modifier.height(16.dp))
            content()
        }
    }
}

// ── Task Weekly Bar Chart ─────────────────────────────────────────────────────

@Composable
fun TaskWeeklyBarChart(
    days: List<TaskDayStats>,
    modifier: Modifier = Modifier
) {
    val maxTotal = days.maxOfOrNull { it.total }.takeIf { it != 0 } ?: 1

    Row(
        modifier              = modifier.fillMaxWidth().height(110.dp),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment     = Alignment.Bottom
    ) {
        days.forEachIndexed { index, day ->
            val totalFraction = if (maxTotal > 0) day.total.toFloat() / maxTotal.toFloat() else 0f
            val doneFraction  = if (maxTotal > 0) day.completed.toFloat() / maxTotal.toFloat() else 0f

            val animTotal by animateFloatAsState(
                targetValue   = totalFraction,
                animationSpec = tween(800, delayMillis = index * 60, easing = FastOutSlowInEasing),
                label         = "task_bar_total_$index"
            )
            val animDone by animateFloatAsState(
                targetValue   = doneFraction,
                animationSpec = tween(800, delayMillis = index * 60 + 100, easing = FastOutSlowInEasing),
                label         = "task_bar_done_$index"
            )

            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Bottom,
                modifier            = Modifier.width(32.dp)
            ) {
                if (day.total > 0) {
                    Text(
                        text  = "${day.completed}/${day.total}",
                        style = MaterialTheme.typography.labelSmall,
                        color = if (day.isToday) PrimaryTeal else TextHint,
                        fontSize = 8.sp,
                        modifier = Modifier.padding(bottom = 2.dp)
                    )
                } else {
                    Spacer(Modifier.height(16.dp))
                }

                Box(
                    modifier         = Modifier.width(24.dp).height(80.dp),
                    contentAlignment = Alignment.BottomCenter
                ) {
                    // Jami vazifalar (orqa)
                    Box(
                        Modifier.fillMaxWidth()
                            .height((80 * animTotal).coerceAtLeast(if (day.total > 0) 3f else 0f).dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(DarkCardAlt)
                    )
                    // Bajarilganlar (old)
                    Box(
                        Modifier.fillMaxWidth()
                            .height((80 * animDone).coerceAtLeast(if (day.completed > 0) 3f else 0f).dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(
                                if (day.isToday) PrimaryTeal
                                else PrimaryTeal.copy(alpha = 0.55f)
                            )
                    )
                }

                Spacer(Modifier.height(6.dp))
                Text(
                    text  = day.dayLabel,
                    style = MaterialTheme.typography.labelSmall,
                    color = if (day.isToday) PrimaryTeal else TextSecondary,
                    fontWeight = if (day.isToday) FontWeight.Bold else FontWeight.Normal
                )
            }
        }
    }
}

// ── Weekly Bar Chart (habit-based, mavjud) ────────────────────────────────────

@Composable
fun WeeklyBarChart(
    days: List<DayStats>,
    modifier: Modifier = Modifier
) {
    val maxTotal = days.maxOfOrNull { it.totalHabits }.takeIf { it != 0 } ?: 1

    Row(
        modifier              = modifier.fillMaxWidth().height(110.dp),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment     = Alignment.Bottom
    ) {
        days.forEachIndexed { index, day ->
            val targetFraction = if (day.totalHabits > 0)
                day.completedCount.toFloat() / maxTotal.toFloat() else 0f

            val animFraction by animateFloatAsState(
                targetValue   = targetFraction,
                animationSpec = tween(800, delayMillis = index * 60, easing = FastOutSlowInEasing),
                label         = "bar_$index"
            )

            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Bottom,
                modifier            = Modifier.width(32.dp)
            ) {
                if (day.completedCount > 0) {
                    Text(
                        text   = "${day.completedCount}",
                        style  = MaterialTheme.typography.labelSmall,
                        color  = if (day.isToday) PrimaryTeal else TextHint,
                        modifier = Modifier.padding(bottom = 2.dp)
                    )
                } else {
                    Spacer(Modifier.height(16.dp))
                }

                Box(
                    modifier         = Modifier.width(24.dp).height(80.dp),
                    contentAlignment = Alignment.BottomCenter
                ) {
                    Box(
                        Modifier.fillMaxSize()
                            .clip(RoundedCornerShape(12.dp))
                            .background(DarkCardAlt)
                    )
                    val fillHeight = (80 * animFraction).coerceAtLeast(if (day.totalHabits > 0) 3f else 0f)
                    Box(
                        Modifier.fillMaxWidth()
                            .height(fillHeight.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(
                                when {
                                    day.isToday && day.completedCount > 0 -> PrimaryTeal
                                    day.completedCount > 0                -> PrimaryTeal.copy(alpha = 0.55f)
                                    else                                  -> DarkCardAlt
                                }
                            )
                    )
                }

                Spacer(Modifier.height(6.dp))
                Text(
                    text   = day.dayLabel,
                    style  = MaterialTheme.typography.labelSmall,
                    color  = if (day.isToday) PrimaryTeal else TextSecondary,
                    fontWeight = if (day.isToday) FontWeight.Bold else FontWeight.Normal
                )
            }
        }
    }
}

// ── Habit Stats Dot Row (yangi HabitStats model bilan) ───────────────────────

@Composable
private fun HabitStatsHeader() {
    Row(
        modifier              = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Spacer(Modifier.width(160.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
            listOf("D", "S", "C", "P", "J", "Sh", "Y").forEach { label ->
                Text(
                    text     = label,
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
fun HabitStatsDotRow(
    habitStats: HabitStats,
    modifier: Modifier = Modifier
) {
    Row(
        modifier          = modifier.fillMaxWidth().padding(vertical = 6.dp),
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
                text     = habitStats.habitName,
                style    = MaterialTheme.typography.bodySmall,
                color    = TextPrimary,
                maxLines = 1,
                fontWeight = FontWeight.Medium
            )
            Text(
                text  = "🔥 ${habitStats.currentStreak} kun · ${(habitStats.percentage * 100).toInt()}%",
                style = MaterialTheme.typography.labelSmall,
                color = AccentAmber
            )
        }

        Spacer(Modifier.weight(1f))

        // 7 dots
        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
            habitStats.weekDays.forEach { completed ->
                Box(
                    modifier = Modifier
                        .size(14.dp)
                        .clip(CircleShape)
                        .background(
                            if (completed) PrimaryTeal else DarkCardAlt
                        )
                )
            }
        }
    }
}

// ── Habit Dot Row (eski HabitWeeklyData bilan — fallback) ─────────────────────

@Composable
fun HabitDotRow(
    habitData: HabitWeeklyData,
    modifier: Modifier = Modifier
) {
    Row(
        modifier          = modifier.fillMaxWidth().padding(vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text     = habitData.habit.iconEmoji,
            fontSize = 18.sp,
            modifier = Modifier.width(28.dp)
        )
        Spacer(Modifier.width(6.dp))
        Column(modifier = Modifier.width(120.dp)) {
            Text(
                text     = habitData.habit.name,
                style    = MaterialTheme.typography.bodySmall,
                color    = TextPrimary,
                maxLines = 1,
                fontWeight = FontWeight.Medium
            )
            Text(
                text  = "🔥 ${habitData.habit.currentStreak} kun",
                style = MaterialTheme.typography.labelSmall,
                color = AccentAmber
            )
        }

        Spacer(Modifier.weight(1f))

        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
            (0..6).forEach { dayIndex ->
                val completed = dayIndex in habitData.completedDays
                Box(
                    modifier = Modifier
                        .size(14.dp)
                        .clip(CircleShape)
                        .background(
                            if (completed) PrimaryTeal else DarkCardAlt
                        )
                )
            }
        }
    }
}

// ── Umumiy Statistika ─────────────────────────────────────────────────────────

@Composable
private fun OverallStatsSection(stats: OverallStats) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text(
            text       = "Umumiy statistika",
            style      = MaterialTheme.typography.titleSmall,
            color      = TextPrimary,
            fontWeight = FontWeight.SemiBold
        )

        // ── 2x2 cards grid ────────────────────────────────────────────────────
        Row(
            modifier              = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            SummaryCard(
                modifier   = Modifier.weight(1f),
                emoji      = "📋",
                label      = "Jami vazifalar",
                value      = "${stats.totalTasks}",
                valueColor = PrimaryTeal,
                subtitle   = "${stats.completedTasks} bajarildi"
            )
            SummaryCard(
                modifier   = Modifier.weight(1f),
                emoji      = "📊",
                label      = "Bajarilish %",
                value      = "${stats.completionPercent.toInt()}%",
                valueColor = if (stats.completionPercent >= 70f) PrimaryTeal else AccentAmber,
                subtitle   = "${stats.completedTasks}/${stats.totalTasks}"
            )
        }

        Row(
            modifier              = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            SummaryCard(
                modifier   = Modifier.weight(1f),
                emoji      = "📝",
                label      = "Eslatmalar",
                value      = "${stats.totalNotes}",
                valueColor = AccentAmber,
                subtitle   = "jami"
            )
            SummaryCard(
                modifier   = Modifier.weight(1f),
                emoji      = "🎯",
                label      = "Faol odatlar",
                value      = "${stats.activeHabits}/${stats.totalHabits}",
                valueColor = PrimaryTeal,
                subtitle   = "aktiv"
            )
        }
    }
}

// ── Summary Card ──────────────────────────────────────────────────────────────

@Composable
fun SummaryCard(
    modifier: Modifier,
    emoji: String,
    label: String,
    value: String,
    valueColor: Color,
    subtitle: String
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
            if (subtitle.isNotBlank()) {
                Text(
                    text  = subtitle,
                    style = MaterialTheme.typography.labelSmall,
                    color = TextHint
                )
            }
        }
    }
}

// ── Mood History Section ──────────────────────────────────────────────────────

private val MOOD_EMOJIS  = listOf("😢", "😐", "😊", "😄", "🤩")
private val MOOD_COLORS  = listOf(
    Color(0xFFEF5350), // Yomon   — qizil
    Color(0xFFBDBDBD), // O'rtacha — kulrang
    Color(0xFF66BB6A), // Yaxshi   — yashil
    Color(0xFF29B6F6), // Ajoyib   — ko'k
    Color(0xFFFFB347)  // Super!   — sariq-to'q
)

@Composable
private fun MoodHistorySection(moodHistory: Map<String, Int>) {
    SectionCard(title = "Haftalik kayfiyat", badge = null) {
        Row(
            modifier              = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            (0..6).forEach { index ->
                val daysAgo = 6 - index
                val dateStr = dateStrDaysAgo(daysAgo)
                val mood    = moodHistory[dateStr]
                val isToday = daysAgo == 0

                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    // Emoji doirasi
                    Box(
                        modifier         = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(
                                if (mood != null)
                                    MOOD_COLORS[mood - 1].copy(alpha = 0.15f)
                                else
                                    DarkCardAlt
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        if (mood != null) {
                            Text(MOOD_EMOJIS[mood - 1], fontSize = 20.sp)
                        } else {
                            Text("·", fontSize = 18.sp, color = TextHint)
                        }
                    }

                    // Kun nomi
                    Text(
                        text       = realDayLabel(index),
                        fontSize   = 9.sp,
                        color      = if (isToday) AccentAmber else TextHint,
                        fontWeight = if (isToday) FontWeight.SemiBold else FontWeight.Normal
                    )
                }
            }
        }

        // Agar bu hafta hech kayfiyat belgilanmagan bo'lsa
        val hasAnyMood = (0..6).any { index ->
            moodHistory[dateStrDaysAgo(6 - index)] != null
        }
        if (!hasAnyMood) {
            Spacer(Modifier.height(8.dp))
            Text(
                text  = "Hali kayfiyat belgilanmagan. Home ekranidan belgilang!",
                style = MaterialTheme.typography.labelSmall,
                color = TextHint
            )
        }
    }
}

// ── Empty ─────────────────────────────────────────────────────────────────────

@Composable
private fun EmptyStatsPlaceholder() {
    Column(
        modifier              = Modifier.fillMaxWidth().padding(top = 64.dp),
        horizontalAlignment   = Alignment.CenterHorizontally,
        verticalArrangement   = Arrangement.spacedBy(8.dp)
    ) {
        Icon(Icons.Default.BarChart, contentDescription = null, tint = TextHint, modifier = Modifier.size(64.dp))
        Text(
            "Statistika yo'q",
            style = MaterialTheme.typography.titleSmall,
            color = TextSecondary
        )
        Text(
            "Odat yoki vazifa qo'shgandan keyin bu yerda ko'rinadi",
            style = MaterialTheme.typography.bodySmall,
            color = TextHint
        )
    }
}
