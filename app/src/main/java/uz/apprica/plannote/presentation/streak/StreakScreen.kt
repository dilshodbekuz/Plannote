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

@Composable
fun StreakScreen(
    onNavigateBack: () -> Unit = {},
    viewModel: StreakViewModel = hiltViewModel()
) {
    val c     = MaterialTheme.appColors
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(c.background)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Spacer(Modifier.height(4.dp))

        // ── Header ────────────────────────────────────────────────────────────
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
            Box(
                modifier = Modifier.size(36.dp).clip(CircleShape).background(c.card).clickable(onClick = onNavigateBack),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Orqaga", tint = c.textSecondary, modifier = Modifier.size(18.dp))
            }
            Spacer(Modifier.width(12.dp))
            Text("🔥  Streak va Faollik", style = MaterialTheme.typography.headlineSmall, color = c.textPrimary, fontWeight = FontWeight.Bold)
        }

        HeroStreakCard(currentStreak = state.currentStreak, bestStreak = state.bestStreak, weeklyActivity = state.weeklyActivity)

        if (state.weeklyTaskStats.isNotEmpty()) {
            StreakSectionCard(title = "Haftalik vazifa faolligi") { WeeklyCanvasBarChart(days = state.weeklyTaskStats) }
        }

        if (state.habitStats.isNotEmpty()) {
            StreakSectionCard(title = "Odat tracker") {
                StreakHabitHeader()
                HorizontalDivider(color = MaterialTheme.appColors.divider, modifier = Modifier.padding(vertical = 4.dp))
                state.habitStats.forEach { hs -> StreakHabitDotRow(habitStats = hs) }
            }
        }

        StreakSectionCard(title = "Haftalik kayfiyat") { StreakMoodRow(moodHistory = state.moodHistory) }

        Spacer(Modifier.height(16.dp))
    }
}

// ── Hero Streak Card ──────────────────────────────────────────────────────────

@Composable
private fun HeroStreakCard(currentStreak: Int, bestStreak: Int, weeklyActivity: List<Boolean>) {
    val c = MaterialTheme.appColors
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(Brush.horizontalGradient(listOf(AccentAmber.copy(alpha = 0.20f), PrimaryTeal.copy(alpha = 0.10f))))
            .padding(20.dp)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(20.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly, verticalAlignment = Alignment.CenterVertically) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Default.Whatshot, contentDescription = null, tint = AccentAmber, modifier = Modifier.size(40.dp))
                    Spacer(Modifier.height(4.dp))
                    Text("$currentStreak", style = MaterialTheme.typography.displaySmall, color = c.textPrimary, fontWeight = FontWeight.ExtraBold)
                    Text("kun",            style = MaterialTheme.typography.bodyMedium,   color = c.textSecondary)
                    Spacer(Modifier.height(2.dp))
                    Text("Ketma-ket",      style = MaterialTheme.typography.labelSmall,  color = AccentAmber)
                }
                Box(modifier = Modifier.height(80.dp).width(1.dp).background(c.divider))
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Default.EmojiEvents, contentDescription = null, tint = AccentAmber, modifier = Modifier.size(40.dp))
                    Spacer(Modifier.height(4.dp))
                    Text("$bestStreak", style = MaterialTheme.typography.displaySmall, color = AccentAmber, fontWeight = FontWeight.ExtraBold)
                    Text("kun",          style = MaterialTheme.typography.bodyMedium,  color = c.textSecondary)
                    Spacer(Modifier.height(2.dp))
                    Text("Rekord",       style = MaterialTheme.typography.labelSmall, color = c.textHint)
                }
            }

            HorizontalDivider(color = c.divider.copy(alpha = 0.6f))

            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text("So'nggi 7 kun", style = MaterialTheme.typography.labelMedium, color = c.textSecondary)
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    weeklyActivity.forEachIndexed { index, active ->
                        val isToday = index == weeklyActivity.lastIndex
                        Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            Box(
                                modifier = Modifier
                                    .size(if (isToday) 14.dp else 12.dp)
                                    .clip(CircleShape)
                                    .background(when { isToday && active -> AccentAmber; active -> PrimaryTeal; else -> c.cardAlt })
                            )
                            Text(
                                text       = realDayLabel(index),
                                fontSize   = 9.sp,
                                color      = when { isToday -> AccentAmber; active -> c.textSecondary; else -> c.textHint },
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
    val a0 by animateFloatAsState(targetValue = days.getOrNull(0)?.completionRate ?: 0f, animationSpec = tween(800,   0, FastOutSlowInEasing), label = "cb0")
    val a1 by animateFloatAsState(targetValue = days.getOrNull(1)?.completionRate ?: 0f, animationSpec = tween(800,  80, FastOutSlowInEasing), label = "cb1")
    val a2 by animateFloatAsState(targetValue = days.getOrNull(2)?.completionRate ?: 0f, animationSpec = tween(800, 160, FastOutSlowInEasing), label = "cb2")
    val a3 by animateFloatAsState(targetValue = days.getOrNull(3)?.completionRate ?: 0f, animationSpec = tween(800, 240, FastOutSlowInEasing), label = "cb3")
    val a4 by animateFloatAsState(targetValue = days.getOrNull(4)?.completionRate ?: 0f, animationSpec = tween(800, 320, FastOutSlowInEasing), label = "cb4")
    val a5 by animateFloatAsState(targetValue = days.getOrNull(5)?.completionRate ?: 0f, animationSpec = tween(800, 400, FastOutSlowInEasing), label = "cb5")
    val a6 by animateFloatAsState(targetValue = days.getOrNull(6)?.completionRate ?: 0f, animationSpec = tween(800, 480, FastOutSlowInEasing), label = "cb6")
    val fracs = listOf(a0, a1, a2, a3, a4, a5, a6)

    if (days.isEmpty()) return

    val c        = MaterialTheme.appColors
    val bgCol    = c.cardAlt
    val fillCol  = PrimaryTeal
    val todayCol = AccentAmber

    Column {
        Canvas(modifier = Modifier.fillMaxWidth().height(120.dp)) {
            val n = 7; val gap = 10.dp.toPx(); val barW = (size.width - gap * (n - 1)) / n
            val maxH = size.height; val cr = CornerRadius(barW / 3f, barW / 3f)
            repeat(n) { i ->
                val day = days.getOrNull(i) ?: return@repeat
                val x   = i * (barW + gap)
                val frac = fracs.getOrElse(i) { 0f }
                drawRoundRect(color = bgCol, topLeft = Offset(x, 0f), size = Size(barW, maxH), cornerRadius = cr)
                val fillH = (maxH * frac).coerceAtLeast(if (day.total > 0) 4f else 0f)
                if (fillH > 0f) drawRoundRect(color = if (day.isToday) todayCol else fillCol, topLeft = Offset(x, maxH - fillH), size = Size(barW, fillH), cornerRadius = cr)
            }
        }
        Spacer(Modifier.height(8.dp))
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            days.forEach { day ->
                Text(day.dayLabel, fontSize = 10.sp, color = if (day.isToday) AccentAmber else c.textSecondary, fontWeight = if (day.isToday) FontWeight.Bold else FontWeight.Normal, modifier = Modifier.weight(1f), textAlign = TextAlign.Center)
            }
        }
        Spacer(Modifier.height(4.dp))
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            days.forEach { day ->
                Text(if (day.total > 0) "${day.completed}/${day.total}" else "–", fontSize = 8.sp, color = if (day.isToday) AccentAmber.copy(alpha = 0.8f) else c.textHint, modifier = Modifier.weight(1f), textAlign = TextAlign.Center)
            }
        }
    }
}

// ── Habit Tracker ─────────────────────────────────────────────────────────────

@Composable
private fun StreakHabitHeader() {
    val c = MaterialTheme.appColors
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        Spacer(Modifier.width(160.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
            listOf("D", "S", "C", "P", "J", "Sh", "Y").forEach { lbl ->
                Text(lbl, style = MaterialTheme.typography.labelSmall, color = c.textHint, modifier = Modifier.width(16.dp), fontSize = 9.sp)
            }
        }
    }
}

@Composable
private fun StreakHabitDotRow(habitStats: HabitStats) {
    val c = MaterialTheme.appColors
    Row(modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp), verticalAlignment = Alignment.CenterVertically) {
        Text(habitStats.iconEmoji, fontSize = 18.sp, modifier = Modifier.width(28.dp))
        Spacer(Modifier.width(6.dp))
        Column(modifier = Modifier.width(120.dp)) {
            Text(habitStats.habitName, style = MaterialTheme.typography.bodySmall, color = c.textPrimary, maxLines = 1, fontWeight = FontWeight.Medium)
            Text("🔥 ${habitStats.currentStreak} kun · ${(habitStats.percentage * 100).toInt()}%", style = MaterialTheme.typography.labelSmall, color = AccentAmber)
        }
        Spacer(Modifier.weight(1f))
        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
            habitStats.weekDays.forEach { done ->
                Box(modifier = Modifier.size(14.dp).clip(CircleShape).background(if (done) PrimaryTeal else c.cardAlt))
            }
        }
    }
}

// ── Mood Row ──────────────────────────────────────────────────────────────────

private val SMOOD_EMOJIS = listOf("😢", "😐", "😊", "😄", "🤩")
private val SMOOD_COLORS = listOf(Color(0xFFEF5350), Color(0xFFBDBDBD), Color(0xFF66BB6A), Color(0xFF29B6F6), Color(0xFFFFB347))
private val MOOD_DATE_FMT = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

@Composable
private fun StreakMoodRow(moodHistory: Map<String, Int>) {
    val c = MaterialTheme.appColors
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            (0..6).forEach { index ->
                val daysAgo = 6 - index
                val cal     = Calendar.getInstance().apply { add(Calendar.DAY_OF_YEAR, -daysAgo) }
                val dateStr = MOOD_DATE_FMT.format(cal.time)
                val mood    = moodHistory[dateStr]
                val isToday = daysAgo == 0
                Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Box(
                        modifier         = Modifier.size(40.dp).clip(CircleShape).background(if (mood != null) SMOOD_COLORS[mood - 1].copy(alpha = 0.18f) else c.cardAlt),
                        contentAlignment = Alignment.Center
                    ) {
                        if (mood != null) Text(SMOOD_EMOJIS[mood - 1], fontSize = 20.sp)
                        else Text("·", fontSize = 20.sp, color = c.textHint)
                    }
                    Text(realDayLabel(index), fontSize = 9.sp, color = if (isToday) AccentAmber else c.textHint, fontWeight = if (isToday) FontWeight.SemiBold else FontWeight.Normal)
                }
            }
        }
        val anyMood = (0..6).any { i ->
            val cal = Calendar.getInstance().apply { add(Calendar.DAY_OF_YEAR, -(6 - i)) }
            moodHistory[MOOD_DATE_FMT.format(cal.time)] != null
        }
        if (!anyMood) {
            Text("Hali kayfiyat belgilanmagan. Home ekranidan belgilang!", style = MaterialTheme.typography.labelSmall, color = c.textHint)
        }
    }
}

// ── Section Card ──────────────────────────────────────────────────────────────

@Composable
private fun StreakSectionCard(title: String, content: @Composable ColumnScope.() -> Unit) {
    val c = MaterialTheme.appColors
    Box(modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(16.dp)).background(c.card).padding(16.dp)) {
        Column {
            Text(title, style = MaterialTheme.typography.titleSmall, color = c.textPrimary, fontWeight = FontWeight.SemiBold)
            Spacer(Modifier.height(16.dp))
            content()
        }
    }
}

// ── Helpers ───────────────────────────────────────────────────────────────────

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
