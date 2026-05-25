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
    val c     = MaterialTheme.appColors
    val s     = MaterialTheme.strings
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(c.background)
            .verticalScroll(rememberScrollState())
            .statusBarsPadding()
            .padding(horizontal = 20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Spacer(Modifier.height(16.dp))

        Text(s.statistics,    style = MaterialTheme.typography.headlineSmall, color = c.textPrimary,   fontWeight = FontWeight.Bold)
        Text(s.weeklyResults, style = MaterialTheme.typography.bodySmall,     color = c.textSecondary)

        StatsStreakCard(
            streak         = state.streak,
            bestStreak     = state.bestStreak,
            weeklyActivity = state.weeklyActivity,
            daysLabel      = s.days,
            rekord         = s.rekord,
            dayAbbrs       = s.dayAbbreviations
        )

        MoodHistorySection(
            moodHistory = state.moodHistory,
            title       = s.weeklyMood,
            noMoodYet   = s.noMoodYet,
            dayAbbrs    = s.dayAbbreviations
        )

        if (state.weeklyTaskStats.isNotEmpty()) {
            SectionCard(title = s.weeklyTasks, badge = null) { TaskWeeklyBarChart(days = state.weeklyTaskStats) }
        }

        state.weeklyStats?.let { stats ->
            SectionCard(
                title = s.weeklyHabits,
                badge = "${(stats.avgCompletionRate * 100).toInt()}% ${s.averageLabel}"
            ) {
                WeeklyBarChart(days = stats.days)
            }
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                SummaryCard(
                    modifier   = Modifier.weight(1f),
                    emoji      = "🏆",
                    label      = s.longestHabitStreakLabel,
                    value      = "${stats.bestStreak} ${s.days}",
                    valueColor = AccentAmber,
                    subtitle   = stats.bestStreakHabitName.take(14)
                )
                SummaryCard(
                    modifier   = Modifier.weight(1f),
                    emoji      = "✅",
                    label      = s.weeklyTasks,
                    value      = "${state.completedTasksThisWeek}/${state.totalTasksThisWeek}",
                    valueColor = PrimaryTeal,
                    subtitle   = s.completed
                )
            }
        }

        if (state.habitStats.isNotEmpty()) {
            SectionCard(title = s.habitTracker, badge = null) {
                HabitStatsHeader(dayAbbrs = s.dayAbbreviations)
                HorizontalDivider(color = c.divider, modifier = Modifier.padding(vertical = 4.dp))
                state.habitStats.forEach { hs -> HabitStatsDotRow(habitStats = hs, daysLabel = s.days) }
            }
        } else if (state.habitWeeklyData.isNotEmpty()) {
            SectionCard(title = s.habitTracker, badge = null) {
                HabitStatsHeader(dayAbbrs = s.dayAbbreviations)
                HorizontalDivider(color = c.divider, modifier = Modifier.padding(vertical = 4.dp))
                state.habitWeeklyData.forEach { habitData -> HabitDotRow(habitData = habitData, daysLabel = s.days) }
            }
        }

        OverallStatsSection(stats = state.overallStats)

        if (state.habitWeeklyData.isEmpty() && state.weeklyStats == null && state.habitStats.isEmpty()) {
            EmptyStatsPlaceholder(noStats = s.noStats, noStatsHint = s.noStatsHint)
        }

        Spacer(Modifier.height(16.dp))
    }
}

// ── Helpers ───────────────────────────────────────────────────────────────────

private val DATE_FMT_STATS = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

private fun realDayLabel(index: Int, abbrs: List<String>): String {
    val daysAgo = 6 - index
    val cal = Calendar.getInstance().apply { add(Calendar.DAY_OF_YEAR, -daysAgo) }
    val dayIdx = when (cal.get(Calendar.DAY_OF_WEEK)) {
        Calendar.MONDAY    -> 0
        Calendar.TUESDAY   -> 1
        Calendar.WEDNESDAY -> 2
        Calendar.THURSDAY  -> 3
        Calendar.FRIDAY    -> 4
        Calendar.SATURDAY  -> 5
        Calendar.SUNDAY    -> 6
        else               -> 0
    }
    return abbrs.getOrElse(dayIdx) { "?" }
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
    weeklyActivity: List<Boolean>,
    daysLabel: String,
    rekord: String,
    dayAbbrs: List<String>
) {
    val c = MaterialTheme.appColors
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(Brush.horizontalGradient(listOf(AccentAmber.copy(alpha = 0.18f), PrimaryTeal.copy(alpha = 0.10f))))
            .padding(20.dp)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Column {
                    Text("🔥 Streak", style = MaterialTheme.typography.labelMedium, color = AccentAmber)
                    Spacer(Modifier.height(4.dp))
                    Row(verticalAlignment = Alignment.Bottom) {
                        Text("$streak", style = MaterialTheme.typography.headlineLarge, color = c.textPrimary, fontWeight = FontWeight.Bold)
                        Text(" $daysLabel", style = MaterialTheme.typography.titleMedium, color = c.textSecondary, modifier = Modifier.padding(bottom = 6.dp))
                    }
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text("🏆 $rekord", style = MaterialTheme.typography.labelMedium, color = c.textHint)
                    Spacer(Modifier.height(4.dp))
                    Row(verticalAlignment = Alignment.Bottom) {
                        Text("$bestStreak", style = MaterialTheme.typography.headlineMedium, color = AccentAmber, fontWeight = FontWeight.Bold)
                        Text(" $daysLabel", style = MaterialTheme.typography.bodyMedium, color = c.textSecondary, modifier = Modifier.padding(bottom = 4.dp))
                    }
                }
            }
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                weeklyActivity.forEachIndexed { index, active ->
                    Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Box(
                            modifier = Modifier.size(12.dp).clip(CircleShape).background(
                                when {
                                    index == weeklyActivity.lastIndex && active -> AccentAmber
                                    active -> PrimaryTeal
                                    else   -> c.cardAlt
                                }
                            )
                        )
                        Text(
                            text       = realDayLabel(index, dayAbbrs),
                            style      = MaterialTheme.typography.labelSmall,
                            color      = if (index == weeklyActivity.lastIndex) AccentAmber else if (active) c.textSecondary else c.textHint,
                            fontSize   = 9.sp,
                            fontWeight = if (index == weeklyActivity.lastIndex) FontWeight.SemiBold else FontWeight.Normal
                        )
                    }
                }
            }
        }
    }
}

// ── Section Card ──────────────────────────────────────────────────────────────

@Composable
private fun SectionCard(title: String, badge: String?, content: @Composable ColumnScope.() -> Unit) {
    val c = MaterialTheme.appColors
    Box(
        modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(16.dp)).background(c.card).padding(16.dp)
    ) {
        Column {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text(title, style = MaterialTheme.typography.titleSmall, color = c.textPrimary, fontWeight = FontWeight.SemiBold)
                badge?.let { Text(it, style = MaterialTheme.typography.labelSmall, color = PrimaryTeal) }
            }
            Spacer(Modifier.height(16.dp))
            content()
        }
    }
}

// ── Task Weekly Bar Chart ─────────────────────────────────────────────────────

@Composable
fun TaskWeeklyBarChart(days: List<TaskDayStats>, modifier: Modifier = Modifier) {
    val c = MaterialTheme.appColors
    val maxTotal = days.maxOfOrNull { it.total }.takeIf { it != 0 } ?: 1
    Row(modifier = modifier.fillMaxWidth().height(110.dp), horizontalArrangement = Arrangement.SpaceEvenly, verticalAlignment = Alignment.Bottom) {
        days.forEachIndexed { index, day ->
            val animTotal by animateFloatAsState(targetValue = if (maxTotal > 0) day.total.toFloat() / maxTotal.toFloat() else 0f, animationSpec = tween(800, delayMillis = index * 60, easing = FastOutSlowInEasing), label = "task_bar_total_$index")
            val animDone  by animateFloatAsState(targetValue = if (maxTotal > 0) day.completed.toFloat() / maxTotal.toFloat() else 0f, animationSpec = tween(800, delayMillis = index * 60 + 100, easing = FastOutSlowInEasing), label = "task_bar_done_$index")
            Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Bottom, modifier = Modifier.width(32.dp)) {
                if (day.total > 0) Text("${day.completed}/${day.total}", style = MaterialTheme.typography.labelSmall, color = if (day.isToday) PrimaryTeal else c.textHint, fontSize = 8.sp, modifier = Modifier.padding(bottom = 2.dp))
                else Spacer(Modifier.height(16.dp))
                Box(modifier = Modifier.width(24.dp).height(80.dp), contentAlignment = Alignment.BottomCenter) {
                    Box(Modifier.fillMaxWidth().height((80 * animTotal).coerceAtLeast(if (day.total > 0) 3f else 0f).dp).clip(RoundedCornerShape(12.dp)).background(c.cardAlt))
                    Box(Modifier.fillMaxWidth().height((80 * animDone).coerceAtLeast(if (day.completed > 0) 3f else 0f).dp).clip(RoundedCornerShape(12.dp)).background(if (day.isToday) PrimaryTeal else PrimaryTeal.copy(alpha = 0.55f)))
                }
                Spacer(Modifier.height(6.dp))
                Text(day.dayLabel, style = MaterialTheme.typography.labelSmall, color = if (day.isToday) PrimaryTeal else c.textSecondary, fontWeight = if (day.isToday) FontWeight.Bold else FontWeight.Normal)
            }
        }
    }
}

// ── Weekly Bar Chart ──────────────────────────────────────────────────────────

@Composable
fun WeeklyBarChart(days: List<DayStats>, modifier: Modifier = Modifier) {
    val c = MaterialTheme.appColors
    val maxTotal = days.maxOfOrNull { it.totalHabits }.takeIf { it != 0 } ?: 1
    Row(modifier = modifier.fillMaxWidth().height(110.dp), horizontalArrangement = Arrangement.SpaceEvenly, verticalAlignment = Alignment.Bottom) {
        days.forEachIndexed { index, day ->
            val targetFraction = if (day.totalHabits > 0) day.completedCount.toFloat() / maxTotal.toFloat() else 0f
            val animFraction by animateFloatAsState(targetValue = targetFraction, animationSpec = tween(800, delayMillis = index * 60, easing = FastOutSlowInEasing), label = "bar_$index")
            Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Bottom, modifier = Modifier.width(32.dp)) {
                if (day.completedCount > 0) Text("${day.completedCount}", style = MaterialTheme.typography.labelSmall, color = if (day.isToday) PrimaryTeal else c.textHint, modifier = Modifier.padding(bottom = 2.dp))
                else Spacer(Modifier.height(16.dp))
                Box(modifier = Modifier.width(24.dp).height(80.dp), contentAlignment = Alignment.BottomCenter) {
                    Box(Modifier.fillMaxSize().clip(RoundedCornerShape(12.dp)).background(c.cardAlt))
                    val fillH = (80 * animFraction).coerceAtLeast(if (day.totalHabits > 0) 3f else 0f)
                    Box(Modifier.fillMaxWidth().height(fillH.dp).clip(RoundedCornerShape(12.dp)).background(when { day.isToday && day.completedCount > 0 -> PrimaryTeal; day.completedCount > 0 -> PrimaryTeal.copy(alpha = 0.55f); else -> c.cardAlt }))
                }
                Spacer(Modifier.height(6.dp))
                Text(day.dayLabel, style = MaterialTheme.typography.labelSmall, color = if (day.isToday) PrimaryTeal else c.textSecondary, fontWeight = if (day.isToday) FontWeight.Bold else FontWeight.Normal)
            }
        }
    }
}

// ── Habit Dot Rows ────────────────────────────────────────────────────────────

@Composable
private fun HabitStatsHeader(dayAbbrs: List<String>) {
    val c = MaterialTheme.appColors
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        Spacer(Modifier.width(160.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
            dayAbbrs.forEach { label ->
                Text(label, style = MaterialTheme.typography.labelSmall, color = c.textHint, modifier = Modifier.width(16.dp), fontSize = 9.sp)
            }
        }
    }
}

@Composable
fun HabitStatsDotRow(habitStats: HabitStats, daysLabel: String, modifier: Modifier = Modifier) {
    val c = MaterialTheme.appColors
    Row(modifier = modifier.fillMaxWidth().padding(vertical = 6.dp), verticalAlignment = Alignment.CenterVertically) {
        Text(habitStats.iconEmoji, fontSize = 18.sp, modifier = Modifier.width(28.dp))
        Spacer(Modifier.width(6.dp))
        Column(modifier = Modifier.width(120.dp)) {
            Text(habitStats.habitName, style = MaterialTheme.typography.bodySmall, color = c.textPrimary, maxLines = 1, fontWeight = FontWeight.Medium)
            Text("🔥 ${habitStats.currentStreak} $daysLabel · ${(habitStats.percentage * 100).toInt()}%", style = MaterialTheme.typography.labelSmall, color = AccentAmber)
        }
        Spacer(Modifier.weight(1f))
        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
            habitStats.weekDays.forEach { completed ->
                Box(modifier = Modifier.size(14.dp).clip(CircleShape).background(if (completed) PrimaryTeal else c.cardAlt))
            }
        }
    }
}

@Composable
fun HabitDotRow(habitData: HabitWeeklyData, daysLabel: String, modifier: Modifier = Modifier) {
    val c = MaterialTheme.appColors
    Row(modifier = modifier.fillMaxWidth().padding(vertical = 6.dp), verticalAlignment = Alignment.CenterVertically) {
        Text(habitData.habit.iconEmoji, fontSize = 18.sp, modifier = Modifier.width(28.dp))
        Spacer(Modifier.width(6.dp))
        Column(modifier = Modifier.width(120.dp)) {
            Text(habitData.habit.name, style = MaterialTheme.typography.bodySmall, color = c.textPrimary, maxLines = 1, fontWeight = FontWeight.Medium)
            Text("🔥 ${habitData.habit.currentStreak} $daysLabel", style = MaterialTheme.typography.labelSmall, color = AccentAmber)
        }
        Spacer(Modifier.weight(1f))
        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
            (0..6).forEach { dayIndex ->
                Box(modifier = Modifier.size(14.dp).clip(CircleShape).background(if (dayIndex in habitData.completedDays) PrimaryTeal else c.cardAlt))
            }
        }
    }
}

// ── Overall Stats ─────────────────────────────────────────────────────────────

@Composable
private fun OverallStatsSection(stats: OverallStats) {
    val c = MaterialTheme.appColors
    val s = MaterialTheme.strings
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text(s.overallStats, style = MaterialTheme.typography.titleSmall, color = c.textPrimary, fontWeight = FontWeight.SemiBold)
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            SummaryCard(modifier = Modifier.weight(1f), emoji = "📋", label = s.totalTasksLabel,   value = "${stats.totalTasks}",                    valueColor = PrimaryTeal, subtitle = "${stats.completedTasks} ${s.completed}")
            SummaryCard(modifier = Modifier.weight(1f), emoji = "📊", label = s.completionPctLabel, value = "${stats.completionPercent.toInt()}%",   valueColor = if (stats.completionPercent >= 70f) PrimaryTeal else AccentAmber, subtitle = "${stats.completedTasks}/${stats.totalTasks}")
        }
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            SummaryCard(modifier = Modifier.weight(1f), emoji = "📝", label = s.navNotes,           value = "${stats.totalNotes}",                   valueColor = AccentAmber, subtitle = s.allLabel)
            SummaryCard(modifier = Modifier.weight(1f), emoji = "🎯", label = s.activeHabitsLabel,  value = "${stats.activeHabits}/${stats.totalHabits}", valueColor = PrimaryTeal, subtitle = s.activeLabel)
        }
    }
}

@Composable
fun SummaryCard(modifier: Modifier, emoji: String, label: String, value: String, valueColor: Color, subtitle: String) {
    val c = MaterialTheme.appColors
    Box(modifier = modifier.clip(RoundedCornerShape(16.dp)).background(c.card).padding(14.dp)) {
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(emoji, fontSize = 22.sp)
            Text(value,    style = MaterialTheme.typography.titleMedium, color = valueColor,     fontWeight = FontWeight.Bold)
            Text(label,    style = MaterialTheme.typography.labelSmall,  color = c.textSecondary)
            if (subtitle.isNotBlank()) Text(subtitle, style = MaterialTheme.typography.labelSmall, color = c.textHint)
        }
    }
}

// ── Mood History ──────────────────────────────────────────────────────────────

private val MOOD_EMOJIS = listOf("😢", "😐", "😊", "😄", "🤩")
private val MOOD_COLORS = listOf(Color(0xFFEF5350), Color(0xFFBDBDBD), Color(0xFF66BB6A), Color(0xFF29B6F6), Color(0xFFFFB347))

@Composable
private fun MoodHistorySection(
    moodHistory: Map<String, Int>,
    title: String,
    noMoodYet: String,
    dayAbbrs: List<String>
) {
    SectionCard(title = title, badge = null) {
        val c = MaterialTheme.appColors
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            (0..6).forEach { index ->
                val daysAgo = 6 - index
                val dateStr = dateStrDaysAgo(daysAgo)
                val mood    = moodHistory[dateStr]
                val isToday = daysAgo == 0
                Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Box(
                        modifier         = Modifier.size(40.dp).clip(CircleShape).background(if (mood != null) MOOD_COLORS[mood - 1].copy(alpha = 0.15f) else c.cardAlt),
                        contentAlignment = Alignment.Center
                    ) {
                        if (mood != null) Text(MOOD_EMOJIS[mood - 1], fontSize = 20.sp)
                        else Text("·", fontSize = 18.sp, color = c.textHint)
                    }
                    Text(realDayLabel(index, dayAbbrs), fontSize = 9.sp, color = if (isToday) AccentAmber else c.textHint, fontWeight = if (isToday) FontWeight.SemiBold else FontWeight.Normal)
                }
            }
        }
        val hasAnyMood = (0..6).any { index -> moodHistory[dateStrDaysAgo(6 - index)] != null }
        if (!hasAnyMood) {
            Spacer(Modifier.height(8.dp))
            Text(noMoodYet, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.appColors.textHint)
        }
    }
}

// ── Empty ─────────────────────────────────────────────────────────────────────

@Composable
private fun EmptyStatsPlaceholder(noStats: String, noStatsHint: String) {
    val c = MaterialTheme.appColors
    Column(modifier = Modifier.fillMaxWidth().padding(top = 64.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Icon(Icons.Default.BarChart, contentDescription = null, tint = c.textHint, modifier = Modifier.size(64.dp))
        Text(noStats,     style = MaterialTheme.typography.titleSmall, color = c.textSecondary)
        Text(noStatsHint, style = MaterialTheme.typography.bodySmall,  color = c.textHint)
    }
}
