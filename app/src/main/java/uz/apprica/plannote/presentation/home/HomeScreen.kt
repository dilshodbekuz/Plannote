package uz.apprica.plannote.presentation.home

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.Whatshot
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import uz.apprica.plannote.ui.theme.AccentAmber
import uz.apprica.plannote.ui.theme.DarkBackground
import uz.apprica.plannote.ui.theme.DarkCard
import uz.apprica.plannote.ui.theme.DarkCardAlt
import uz.apprica.plannote.ui.theme.PrimaryTeal
import uz.apprica.plannote.ui.theme.TextHint
import uz.apprica.plannote.ui.theme.TextPrimary
import uz.apprica.plannote.ui.theme.TextSecondary

@Composable
fun HomeScreen(
    onNavigateToTasks: () -> Unit = {},
    onNavigateToNotes: () -> Unit = {},
    onNavigateToStreak: () -> Unit = {},
    viewModel: HomeViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBackground)
            .verticalScroll(rememberScrollState())
            .statusBarsPadding()
            .padding(horizontal = 20.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        GreetingSection(state)
        QuoteCard(quote = state.quote, onRefresh = viewModel::refreshQuote)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            StatCard(
                modifier = Modifier.weight(1f),
                label = "Bugungi vazifalar",
                value = "${state.todayTasksDone}/${state.todayTasksTotal}",
                emoji = "✅",
                accentColor = PrimaryTeal,
                onClick = onNavigateToTasks
            )
            StatCard(
                modifier = Modifier.weight(1f),
                label = "Eslatmalar",
                value = state.notesCount.toString(),
                emoji = "📝",
                accentColor = AccentAmber,
                onClick = onNavigateToNotes
            )
        }
        StreakCard(
            streak = state.currentStreak,
            bestStreak = state.bestStreak,
            onClick = onNavigateToStreak
        )
        MoodTracker(
            selected = state.selectedMood,
            onSelected = viewModel::onMoodSelected
        )

        Spacer(Modifier.height(8.dp))
    }
}

@Composable
private fun GreetingSection(state: HomeUiState) {
    Column {
        Text(
            text = "${state.greetingEmoji}  ${state.greeting}",
            style = MaterialTheme.typography.headlineSmall,
            color = TextPrimary,
            fontWeight = FontWeight.Bold
        )
        Spacer(Modifier.height(4.dp))
        Text(
            text = state.todayDate,
            style = MaterialTheme.typography.bodyMedium,
            color = TextSecondary
        )
    }
}

@Composable
private fun QuoteCard(quote: String, onRefresh: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(
                Brush.horizontalGradient(
                    listOf(
                        PrimaryTeal.copy(alpha = 0.18f),
                        AccentAmber.copy(alpha = 0.10f)
                    )
                )
            )
            .padding(16.dp)
    ) {
        Column {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(Icons.Default.Lightbulb, contentDescription = null, tint = PrimaryTeal, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(6.dp))
                Text(
                    text = "Kunlik ilhom",
                    style = MaterialTheme.typography.labelMedium,
                    color = PrimaryTeal
                )
                Spacer(Modifier.weight(1f))
                IconButton(onClick = onRefresh, modifier = Modifier.size(28.dp)) {
                    Icon(
                        imageVector = Icons.Default.AutoAwesome,
                        contentDescription = "Yangi iqtibos",
                        tint = PrimaryTeal,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
            Spacer(Modifier.height(8.dp))
            Text(
                text = "\"$quote\"",
                style = MaterialTheme.typography.bodyMedium,
                color = TextPrimary,
                fontStyle = FontStyle.Italic,
                lineHeight = 22.sp
            )
        }
    }
}

@Composable
private fun StatCard(
    modifier: Modifier,
    label: String,
    value: String,
    emoji: String,
    accentColor: Color,
    onClick: () -> Unit
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .background(DarkCard)
            .clickable(onClick = onClick)
            .padding(16.dp)
    ) {
        Column {
            Text(emoji, fontSize = 24.sp)
            Spacer(Modifier.height(8.dp))
            Text(
                text = value,
                style = MaterialTheme.typography.titleLarge,
                color = accentColor,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = TextSecondary
            )
        }
    }
}

@Composable
private fun StreakCard(streak: Int, bestStreak: Int, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(DarkCard)
            .clickable(onClick = onClick)
            .padding(horizontal = 20.dp, vertical = 18.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text(
                    text = "🔥 Ketma-ket streak",
                    style = MaterialTheme.typography.labelMedium,
                    color = AccentAmber
                )
                Spacer(Modifier.height(4.dp))
                Row(verticalAlignment = Alignment.Bottom) {
                    Text(
                        text = "$streak",
                        style = MaterialTheme.typography.headlineLarge,
                        color = TextPrimary,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(Modifier.width(6.dp))
                    Text(
                        text = "kun",
                        style = MaterialTheme.typography.titleMedium,
                        color = TextSecondary,
                        modifier = Modifier.padding(bottom = 6.dp)
                    )
                }
                Spacer(Modifier.height(6.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(Icons.Default.EmojiEvents, contentDescription = null, tint = TextHint, modifier = Modifier.size(16.dp))
                    Text(
                        text = "Rekord: $bestStreak kun",
                        style = MaterialTheme.typography.labelMedium,
                        color = TextHint
                    )
                }
            }

            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Box(
                    modifier = Modifier
                        .size(64.dp)
                        .clip(CircleShape)
                        .background(AccentAmber.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.Whatshot, contentDescription = null, tint = AccentAmber, modifier = Modifier.size(40.dp))
                }
                Spacer(Modifier.height(4.dp))
                Text(
                    text = "Batafsil →",
                    style = MaterialTheme.typography.labelSmall,
                    color = TextHint
                )
            }
        }
    }
}

private val MOODS = listOf("😢", "😐", "😊", "😄", "🤩")
private val MOOD_LABELS = listOf("Yomon", "O'rtacha", "Yaxshi", "Ajoyib", "Super!")

@Composable
private fun MoodTracker(selected: Int?, onSelected: (Int) -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(DarkCard)
            .padding(16.dp)
    ) {
        Text(
            text = "Bugungi kayfiyat",
            style = MaterialTheme.typography.titleSmall,
            color = TextPrimary,
            fontWeight = FontWeight.SemiBold
        )
        Spacer(Modifier.height(12.dp))
        AnimatedVisibility(
            visible = selected != null,
            enter = expandVertically(tween(250)) + fadeIn(tween(250)),
            exit = shrinkVertically(tween(200)) + fadeOut(tween(200))
        ) {
            Column {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(PrimaryTeal.copy(alpha = 0.07f))
                        .padding(vertical = 18.dp),
                    contentAlignment = Alignment.Center
                ) {
                    if (selected != null) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Text(MOODS[selected - 1], fontSize = 44.sp)
                            Spacer(Modifier.height(2.dp))
                            Text(
                                text = "Bugungi kayfiyatingiz:",
                                style = MaterialTheme.typography.labelSmall,
                                color = TextHint
                            )
                            Text(
                                text = MOOD_LABELS[selected - 1],
                                style = MaterialTheme.typography.titleSmall,
                                color = PrimaryTeal,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }
                Spacer(Modifier.height(14.dp))
            }
        }
        Text(
            text = if (selected != null) "✏️  O'zgartirish" else "Bugun kayfiyatingizni belgilang",
            style = MaterialTheme.typography.labelSmall,
            color = TextHint
        )
        Spacer(Modifier.height(8.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            MOODS.forEachIndexed { index, emoji ->
                val moodIndex = index + 1
                val isSelected = selected == moodIndex

                val bgColor by animateColorAsState(
                    targetValue = if (isSelected) PrimaryTeal.copy(alpha = 0.25f) else DarkCardAlt,
                    animationSpec = tween(200),
                    label = "mood_bg_$moodIndex"
                )
                val emojiScale by animateFloatAsState(
                    targetValue = if (isSelected) 1.25f else 1f,
                    animationSpec = spring(
                        dampingRatio = Spring.DampingRatioMediumBouncy,
                        stiffness = Spring.StiffnessMedium
                    ),
                    label = "mood_scale_$moodIndex"
                )

                Box(
                    modifier = Modifier
                        .scale(emojiScale)
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(bgColor)
                        .clickable { onSelected(moodIndex) },
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = emoji,
                        fontSize = if (isSelected) 26.sp else 22.sp
                    )
                }
            }
        }
    }
}

