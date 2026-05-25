package uz.apprica.plannote.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

// ── Dark scheme ───────────────────────────────────────────────────────────────
private val PlannoteDarkColorScheme = darkColorScheme(
    primary              = PrimaryTeal,
    onPrimary            = DarkBackground,
    primaryContainer     = PrimaryTealDim,
    onPrimaryContainer   = TextPrimary,

    secondary            = AccentAmber,
    onSecondary          = DarkBackground,
    secondaryContainer   = Color(0xFF3D2E00),
    onSecondaryContainer = AccentAmber,

    tertiary             = AccentPink,
    onTertiary           = DarkBackground,

    background           = DarkBackground,
    onBackground         = TextPrimary,

    surface              = DarkSurface,
    onSurface            = TextPrimary,
    surfaceVariant       = DarkCard,
    onSurfaceVariant     = TextSecondary,

    outline              = DividerColor,
    outlineVariant       = DividerColor,

    error                = ErrorRed,
    onError              = TextPrimary,

    scrim                = Color(0xFF000000)
)

// ── Light scheme ──────────────────────────────────────────────────────────────
private val PlannoteLightColorScheme = lightColorScheme(
    primary              = PrimaryTeal,
    onPrimary            = Color.White,
    primaryContainer     = Color(0xFFB2EBE8),
    onPrimaryContainer   = Color(0xFF001F1D),

    secondary            = AccentAmber,
    onSecondary          = Color.White,
    secondaryContainer   = Color(0xFFFFE0B2),
    onSecondaryContainer = Color(0xFF2A1A00),

    tertiary             = AccentPink,
    onTertiary           = Color.White,

    background           = Color(0xFFF5F7FA),
    onBackground         = Color(0xFF1A1C21),

    surface              = Color(0xFFFFFFFF),
    onSurface            = Color(0xFF1A1C21),
    surfaceVariant       = Color(0xFFE8ECF0),
    onSurfaceVariant     = Color(0xFF44474F),

    outline              = Color(0xFFCDD3DA),
    outlineVariant       = Color(0xFFDDE3EA),

    error                = ErrorRed,
    onError              = Color.White
)

@Composable
fun PlannoteTheme(
    darkTheme: Boolean = true,
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = if (darkTheme) PlannoteDarkColorScheme else PlannoteLightColorScheme,
        typography  = PlannoteTypography,
        content     = content
    )
}
