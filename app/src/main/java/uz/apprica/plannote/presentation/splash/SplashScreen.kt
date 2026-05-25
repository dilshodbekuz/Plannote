package uz.apprica.plannote.presentation.splash

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.coroutines.delay
import uz.apprica.plannote.ui.theme.*

@Composable
fun SplashScreen(
    onNavigateToHome: () -> Unit,
    onNavigateToLanguage: () -> Unit,
    viewModel: SplashViewModel = hiltViewModel()
) {
    val isFirstLaunch by viewModel.isFirstLaunch.collectAsStateWithLifecycle()

    // Animatsiya holatlari
    val infiniteTransition = rememberInfiniteTransition(label = "splash_pulse")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue  = 1f,
        targetValue   = 1.08f,
        animationSpec = infiniteRepeatable(
            animation = tween(900, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse"
    )

    // Fade-in animatsiyasi
    var visible by remember { mutableStateOf(false) }
    val alpha by animateFloatAsState(
        targetValue   = if (visible) 1f else 0f,
        animationSpec = tween(600),
        label         = "splash_alpha"
    )
    val slideY by animateFloatAsState(
        targetValue   = if (visible) 0f else 40f,
        animationSpec = tween(700, easing = FastOutSlowInEasing),
        label         = "splash_slide"
    )

    LaunchedEffect(Unit) {
        visible = true
        delay(2000L)   // 2 soniya
        if (isFirstLaunch == true) onNavigateToLanguage()
        else onNavigateToHome()
    }

    Box(
        modifier         = Modifier
            .fillMaxSize()
            .background(DarkBackground),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .alpha(alpha)
                .offset(y = slideY.dp)
        ) {
            // ── Logo emoji ────────────────────────────────────────────────────
            Text(
                text     = "📋",
                fontSize = 72.sp,
                modifier = Modifier.scale(pulseScale)
            )

            Spacer(Modifier.height(20.dp))

            // ── App nomi ──────────────────────────────────────────────────────
            Text(
                text       = "Plannote",
                style      = MaterialTheme.typography.displaySmall,
                color      = TextPrimary,
                fontWeight = FontWeight.Bold,
                letterSpacing = 2.sp
            )

            Spacer(Modifier.height(8.dp))

            // ── Tagline ───────────────────────────────────────────────────────
            Text(
                text  = "Rejalashtir · Kuzat · Muvaffaqiyatga eris",
                style = MaterialTheme.typography.bodyMedium,
                color = TextSecondary
            )

            Spacer(Modifier.height(48.dp))

            // ── Brand accent line ─────────────────────────────────────────────
            Row(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment     = Alignment.CenterVertically
            ) {
                listOf(PrimaryTeal, AccentAmber, AccentPink).forEach { color ->
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .scale(pulseScale)
                            .background(color, shape = androidx.compose.foundation.shape.CircleShape)
                    )
                }
            }
        }

        // ── Version ───────────────────────────────────────────────────────────
        Text(
            text     = "v1.0.0",
            style    = MaterialTheme.typography.labelSmall,
            color    = TextHint,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 32.dp)
                .alpha(alpha)
        )
    }
}
