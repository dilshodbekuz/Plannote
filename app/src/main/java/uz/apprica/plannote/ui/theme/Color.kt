package uz.apprica.plannote.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

// ── Brand (har ikki rejimda bir xil) ─────────────────────────────────────────
val PrimaryTeal    = Color(0xFF4ECDC4)
val PrimaryTealDim = Color(0xFF2A9D96)
val AccentAmber    = Color(0xFFFFB347)
val AccentPink     = Color(0xFFFF6B9D)
val AccentPurple   = Color(0xFFB39DDB)

// ── Semantic ──────────────────────────────────────────────────────────────────
val SuccessGreen   = Color(0xFF4CAF50)
val WarningOrange  = Color(0xFFFF9800)
val ErrorRed       = Color(0xFFF44336)

// ── Priority ──────────────────────────────────────────────────────────────────
val PriorityLow    = Color(0xFF4CAF50)
val PriorityMedium = Color(0xFF29B6F6)
val PriorityHigh   = Color(0xFFFF4444)

// ── AppColors data class ──────────────────────────────────────────────────────

data class AppColors(
    val background: Color,
    val surface: Color,
    val card: Color,
    val cardAlt: Color,
    val textPrimary: Color,
    val textSecondary: Color,
    val textHint: Color,
    val divider: Color,
    val noteCardColors: List<Color>
)

// ── Dark palette ──────────────────────────────────────────────────────────────

val darkAppColors = AppColors(
    background    = Color(0xFF0D0F14),
    surface       = Color(0xFF161B22),
    card          = Color(0xFF1E2530),
    cardAlt       = Color(0xFF232B38),
    textPrimary   = Color(0xFFE8EAF6),
    textSecondary = Color(0xFF9E9E9E),
    textHint      = Color(0xFF616161),
    divider       = Color(0xFF2A3040),
    noteCardColors = listOf(
        Color(0xFF1E2530),
        Color(0xFF0D2E4A),
        Color(0xFF2A1250),
        Color(0xFF0D3D25),
        Color(0xFF3D2600),
        Color(0xFF45102A)
    )
)

// ── Light palette ─────────────────────────────────────────────────────────────

val lightAppColors = AppColors(
    background    = Color(0xFFF2F4F8),
    surface       = Color(0xFFFFFFFF),
    card          = Color(0xFFFFFFFF),
    cardAlt       = Color(0xFFECEFF4),
    textPrimary   = Color(0xFF1A1C21),
    textSecondary = Color(0xFF44474F),
    textHint      = Color(0xFF8A8D97),
    divider       = Color(0xFFDDE1EA),
    noteCardColors = listOf(
        Color(0xFFECEFF4),
        Color(0xFFDBEEFB),
        Color(0xFFEDE7F6),
        Color(0xFFE8F5E9),
        Color(0xFFFFF8E1),
        Color(0xFFFCE4EC)
    )
)

// ── CompositionLocal + extension ──────────────────────────────────────────────

val LocalAppColors = staticCompositionLocalOf { darkAppColors }

val MaterialTheme.appColors: AppColors
    @Composable
    @ReadOnlyComposable
    get() = LocalAppColors.current

// ── Backward-compat aliases (dark theme uchun, eski kodlar buzilmasin) ────────

val DarkBackground: Color  get() = darkAppColors.background
val DarkSurface: Color     get() = darkAppColors.surface
val DarkCard: Color        get() = darkAppColors.card
val DarkCardAlt: Color     get() = darkAppColors.cardAlt
val TextPrimary: Color     get() = darkAppColors.textPrimary
val TextSecondary: Color   get() = darkAppColors.textSecondary
val TextHint: Color        get() = darkAppColors.textHint
val DividerColor: Color    get() = darkAppColors.divider
val NoteCardColors: List<Color> get() = darkAppColors.noteCardColors
