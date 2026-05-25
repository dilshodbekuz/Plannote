package uz.apprica.plannote.presentation.onboarding

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import uz.apprica.plannote.ui.theme.*

private data class OnBoardingPage(
    val emoji: String,
    val title: String,
    val description: String,
    val gradientColors: List<androidx.compose.ui.graphics.Color>
)

private val PAGES = listOf(
    OnBoardingPage(
        emoji      = "✅",
        title      = "Kunlik vazifalarni rejalashtiring",
        description = "Bugungi ishlaringizni tartibga soling, muhimlilik darajasini belgilang va notification bilan eslatmalar oling.",
        gradientColors = listOf(PrimaryTeal.copy(0.25f), PrimaryTeal.copy(0.05f))
    ),
    OnBoardingPage(
        emoji      = "📝",
        title      = "Eslatmalar va bildirishnomalar",
        description = "Muhim fikrlaringizni yozing, eslatmalar qo'shing. Vaqtida yetkazib beriladigan bildirishnomalar hech narsani unutdirmaydi.",
        gradientColors = listOf(AccentAmber.copy(0.25f), AccentAmber.copy(0.05f))
    ),
    OnBoardingPage(
        emoji      = "📊",
        title      = "Haftalik statistikangizni kuzating",
        description = "Streak, odatlar va vazifalar bo'yicha batafsil statistika. O'z rivojlanishingizni ko'z ostida saqlang.",
        gradientColors = listOf(AccentPink.copy(0.25f), AccentPink.copy(0.05f))
    )
)

@Composable
fun OnBoardingScreen(
    onFinish: () -> Unit,
    viewModel: OnBoardingViewModel = hiltViewModel()
) {
    var currentPage by remember { mutableIntStateOf(0) }
    val page = PAGES[currentPage]

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBackground)
    ) {
        Column(
            modifier            = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
                .padding(horizontal = 28.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(Modifier.height(48.dp))

            // ── Sahifa kontenti (animatsiya bilan) ───────────────────────────
            AnimatedContent(
                targetState  = currentPage,
                transitionSpec = {
                    if (targetState > initialState) {
                        (slideInHorizontally(tween(400)) { it } + fadeIn(tween(400))) togetherWith
                            (slideOutHorizontally(tween(400)) { -it } + fadeOut(tween(400)))
                    } else {
                        (slideInHorizontally(tween(400)) { -it } + fadeIn(tween(400))) togetherWith
                            (slideOutHorizontally(tween(400)) { it } + fadeOut(tween(400)))
                    }
                },
                modifier = Modifier.weight(1f),
                label = "onboarding_content"
            ) { pageIndex ->
                val p = PAGES[pageIndex]
                PageContent(page = p)
            }

            // ── Pagination dots ───────────────────────────────────────────────
            Row(
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.padding(vertical = 32.dp)
            ) {
                PAGES.indices.forEach { index ->
                    Box(
                        modifier = Modifier
                            .height(8.dp)
                            .then(
                                if (index == currentPage)
                                    Modifier.width(28.dp)
                                else
                                    Modifier.width(8.dp)
                            )
                            .clip(RoundedCornerShape(4.dp))
                            .background(
                                if (index == currentPage) PrimaryTeal
                                else DarkCardAlt
                            )
                    )
                }
            }

            // ── Tugmalar ──────────────────────────────────────────────────────
            Row(
                modifier              = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 32.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment     = Alignment.CenterVertically
            ) {
                // O'tkazib yuborish
                if (currentPage < PAGES.lastIndex) {
                    TextButton(onClick = {
                        viewModel.markOnBoardingDone()
                        onFinish()
                    }) {
                        Text(
                            text  = "O'tkazib yuborish",
                            color = TextHint,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                } else {
                    Spacer(Modifier.width(1.dp))
                }

                // Keyingi / Boshlash
                Button(
                    onClick = {
                        if (currentPage < PAGES.lastIndex) {
                            currentPage++
                        } else {
                            viewModel.markOnBoardingDone()
                            onFinish()
                        }
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = PrimaryTeal,
                        contentColor   = DarkBackground
                    ),
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier.height(50.dp)
                ) {
                    Text(
                        text       = if (currentPage < PAGES.lastIndex) "Keyingisi →" else "🚀  Boshlash",
                        fontWeight = FontWeight.Bold,
                        modifier   = Modifier.padding(horizontal = 8.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun PageContent(page: OnBoardingPage) {
    Column(
        modifier            = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // ── Emoji circle ──────────────────────────────────────────────────────
        Box(
            modifier         = Modifier
                .size(140.dp)
                .clip(CircleShape)
                .background(Brush.radialGradient(page.gradientColors)),
            contentAlignment = Alignment.Center
        ) {
            Text(text = page.emoji, fontSize = 64.sp)
        }

        Spacer(Modifier.height(40.dp))

        // ── Sarlavha ──────────────────────────────────────────────────────────
        Text(
            text       = page.title,
            style      = MaterialTheme.typography.headlineSmall,
            color      = TextPrimary,
            fontWeight = FontWeight.Bold,
            textAlign  = TextAlign.Center,
            lineHeight = 34.sp
        )

        Spacer(Modifier.height(20.dp))

        // ── Tavsif ────────────────────────────────────────────────────────────
        Text(
            text      = page.description,
            style     = MaterialTheme.typography.bodyLarge,
            color     = TextSecondary,
            textAlign = TextAlign.Center,
            lineHeight = 26.sp
        )
    }
}
