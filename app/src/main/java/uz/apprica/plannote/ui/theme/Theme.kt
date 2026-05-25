package uz.apprica.plannote.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.graphics.Color

// ── Dark scheme ───────────────────────────────────────────────────────────────

private val PlannoteDarkColorScheme = darkColorScheme(
    primary              = PrimaryTeal,
    onPrimary            = darkAppColors.background,
    primaryContainer     = PrimaryTealDim,
    onPrimaryContainer   = darkAppColors.textPrimary,
    secondary            = AccentAmber,
    onSecondary          = darkAppColors.background,
    secondaryContainer   = Color(0xFF3D2E00),
    onSecondaryContainer = AccentAmber,
    tertiary             = AccentPink,
    onTertiary           = darkAppColors.background,
    background           = darkAppColors.background,
    onBackground         = darkAppColors.textPrimary,
    surface              = darkAppColors.surface,
    onSurface            = darkAppColors.textPrimary,
    surfaceVariant       = darkAppColors.card,
    onSurfaceVariant     = darkAppColors.textSecondary,
    outline              = darkAppColors.divider,
    outlineVariant       = darkAppColors.divider,
    error                = ErrorRed,
    onError              = darkAppColors.textPrimary,
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
    background           = lightAppColors.background,
    onBackground         = lightAppColors.textPrimary,
    surface              = lightAppColors.surface,
    onSurface            = lightAppColors.textPrimary,
    surfaceVariant       = lightAppColors.card,
    onSurfaceVariant     = lightAppColors.textSecondary,
    outline              = lightAppColors.divider,
    outlineVariant       = lightAppColors.divider,
    error                = ErrorRed,
    onError              = Color.White
)

// ── Theme ─────────────────────────────────────────────────────────────────────

@Composable
fun PlannoteTheme(
    darkTheme: Boolean = true,
    language: String = "uz",
    content: @Composable () -> Unit
) {
    val colors  = if (darkTheme) darkAppColors else lightAppColors
    val strings = when (language) {
        "ru" -> ruStrings
        "en" -> enStrings
        else -> uzStrings
    }

    CompositionLocalProvider(
        LocalAppColors  provides colors,
        LocalAppStrings provides strings
    ) {
        MaterialTheme(
            colorScheme = if (darkTheme) PlannoteDarkColorScheme else PlannoteLightColorScheme,
            typography  = PlannoteTypography,
            content     = content
        )
    }
}
