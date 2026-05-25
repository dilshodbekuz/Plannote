package uz.apprica.plannote.presentation.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import uz.apprica.plannote.ui.theme.AccentAmber
import uz.apprica.plannote.ui.theme.ErrorRed
import uz.apprica.plannote.ui.theme.PrimaryTeal
import uz.apprica.plannote.ui.theme.appColors
import uz.apprica.plannote.ui.theme.strings

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(viewModel: SettingsViewModel = hiltViewModel()) {
    val c     = MaterialTheme.appColors
    val s     = MaterialTheme.strings
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    var showResetDialog    by remember { mutableStateOf(false) }
    var showLanguageDialog by remember { mutableStateOf(false) }
    val snackbarHostState  = remember { SnackbarHostState() }
    var snackMsg           by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(snackMsg) {
        snackMsg?.let { snackbarHostState.showSnackbar(it); snackMsg = null }
    }

    Scaffold(
        containerColor = c.background,
        snackbarHost   = { SnackbarHost(snackbarHostState) }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // ── Header ────────────────────────────────────────────────────────
            Text(
                text       = s.settingsTitle,
                style      = MaterialTheme.typography.headlineSmall,
                color      = c.textPrimary,
                fontWeight = FontWeight.Bold
            )

            // ── Streak info ───────────────────────────────────────────────────
            StreakInfoCard(
                currentStreak = state.currentStreak,
                bestStreak    = state.bestStreak,
                currentLabel  = s.currentStreakLabel,
                recordLabel   = s.recordStreakLabel
            )

            // ── Ko'rinish + Til + Streak ──────────────────────────────────────
            SettingsSection {
                ToggleRow(
                    icon            = if (state.isDarkMode) Icons.Default.DarkMode else Icons.Default.LightMode,
                    title           = s.darkMode,
                    subtitle        = if (state.isDarkMode) s.darkModeOn else s.darkModeOff,
                    checked         = state.isDarkMode,
                    onCheckedChange = { viewModel.setDarkMode(it) }
                )
                HorizontalDivider(color = c.divider, modifier = Modifier.padding(horizontal = 8.dp))
                ClickableRow(
                    icon     = Icons.Default.Language,
                    title    = s.languageLabel,
                    subtitle = when (state.language) {
                        "ru" -> "Русский 🇷🇺"
                        "en" -> "English 🇬🇧"
                        else -> "O'zbekcha 🇺🇿"
                    },
                    onClick  = { showLanguageDialog = true }
                )
                HorizontalDivider(color = c.divider, modifier = Modifier.padding(horizontal = 8.dp))
                ClickableRow(
                    icon      = Icons.Default.RestartAlt,
                    title     = s.resetStreak,
                    subtitle  = "${s.resetStreakCurrent}: ${state.currentStreak} ${s.days}",
                    onClick   = { showResetDialog = true },
                    tintColor = ErrorRed
                )
            }

            // ── Versiya + Ishlab chiquvchi + Min. Android ─────────────────────
            SettingsSection {
                InfoRow(icon = Icons.Default.Info,    title = s.versionLabel,    value = "1.0.0")
                HorizontalDivider(color = c.divider, modifier = Modifier.padding(horizontal = 8.dp))
                InfoRow(icon = Icons.Default.Code,    title = s.developerLabel,  value = "Plannote Team")
                HorizontalDivider(color = c.divider, modifier = Modifier.padding(horizontal = 8.dp))
                InfoRow(icon = Icons.Default.Android, title = s.minAndroidLabel, value = "Android 7.0+")
            }

            Spacer(Modifier.height(24.dp))
        }
    }

    // ── Streak reset dialog ───────────────────────────────────────────────────
    if (showResetDialog) {
        AlertDialog(
            onDismissRequest = { showResetDialog = false },
            containerColor   = MaterialTheme.appColors.card,
            title  = {
                Text(
                    s.resetStreakConfirmTitle,
                    color      = MaterialTheme.appColors.textPrimary,
                    fontWeight = FontWeight.Bold
                )
            },
            text   = {
                Text(
                    "${s.resetStreakCurrent} streak (${state.currentStreak} ${s.days}) ${s.resetStreakConfirmBody}",
                    color = MaterialTheme.appColors.textSecondary
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.confirmResetStreak()
                    showResetDialog = false
                    snackMsg = s.streakReset
                }) {
                    Text(s.resetStreakConfirmYes, color = ErrorRed, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showResetDialog = false }) {
                    Text(s.resetStreakConfirmNo, color = MaterialTheme.appColors.textSecondary)
                }
            }
        )
    }

    // ── Language picker dialog ────────────────────────────────────────────────
    if (showLanguageDialog) {
        LanguagePickerDialog(
            currentLanguage = state.language,
            onSelect = { lang ->
                viewModel.setLanguage(lang)
                showLanguageDialog = false
            },
            onDismiss = { showLanguageDialog = false }
        )
    }
}

// ── Language Picker Dialog ────────────────────────────────────────────────────

private val LANG_OPTIONS = listOf(
    Triple("uz", "O'zbekcha", "🇺🇿"),
    Triple("ru", "Русский",   "🇷🇺"),
    Triple("en", "English",   "🇬🇧")
)

@Composable
private fun LanguagePickerDialog(
    currentLanguage: String,
    onSelect: (String) -> Unit,
    onDismiss: () -> Unit
) {
    val c = MaterialTheme.appColors
    val s = MaterialTheme.strings

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor   = c.card,
        title = {
            Text(
                text       = s.languageLabel,
                color      = c.textPrimary,
                fontWeight = FontWeight.Bold,
                style      = MaterialTheme.typography.titleLarge
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                LANG_OPTIONS.forEach { (code, name, flag) ->
                    val isSelected = currentLanguage == code
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(if (isSelected) PrimaryTeal.copy(alpha = 0.12f) else c.cardAlt)
                            .then(
                                if (isSelected)
                                    Modifier.border(1.5.dp, PrimaryTeal, RoundedCornerShape(12.dp))
                                else Modifier
                            )
                            .clickable { onSelect(code) }
                            .padding(12.dp),
                        verticalAlignment     = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(flag, fontSize = 22.sp)
                        Text(
                            text       = name,
                            style      = MaterialTheme.typography.bodyMedium,
                            color      = if (isSelected) PrimaryTeal else c.textPrimary,
                            fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                            modifier   = Modifier.weight(1f)
                        )
                        if (isSelected) {
                            Box(
                                modifier = Modifier
                                    .size(20.dp)
                                    .clip(CircleShape)
                                    .background(PrimaryTeal),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector        = Icons.Default.Check,
                                    contentDescription = null,
                                    tint               = c.background,
                                    modifier           = Modifier.size(14.dp)
                                )
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {},
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(s.cancel, color = c.textSecondary)
            }
        }
    )
}

// ── Settings Section ──────────────────────────────────────────────────────────

@Composable
private fun SettingsSection(content: @Composable ColumnScope.() -> Unit) {
    val c = MaterialTheme.appColors
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(c.card)
    ) {
        Column { content() }
    }
}

// ── Toggle Row ────────────────────────────────────────────────────────────────

@Composable
private fun ToggleRow(
    icon: ImageVector,
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    val c = MaterialTheme.appColors
    Row(
        modifier          = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icon, contentDescription = null, tint = PrimaryTeal, modifier = Modifier.size(22.dp))
        Spacer(Modifier.width(14.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(title,    style = MaterialTheme.typography.bodyMedium, color = c.textPrimary,   fontWeight = FontWeight.Medium)
            Text(subtitle, style = MaterialTheme.typography.labelSmall, color = c.textSecondary)
        }
        Switch(
            checked         = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor   = c.background,
                checkedTrackColor   = PrimaryTeal,
                uncheckedThumbColor = c.textHint,
                uncheckedTrackColor = c.cardAlt
            )
        )
    }
}

// ── Clickable Row ─────────────────────────────────────────────────────────────

@Composable
private fun ClickableRow(
    icon: ImageVector,
    title: String,
    subtitle: String,
    onClick: () -> Unit,
    tintColor: androidx.compose.ui.graphics.Color = PrimaryTeal
) {
    val c = MaterialTheme.appColors
    Row(
        modifier          = Modifier.fillMaxWidth().clickable(onClick = onClick).padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icon, contentDescription = null, tint = tintColor, modifier = Modifier.size(22.dp))
        Spacer(Modifier.width(14.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(title,    style = MaterialTheme.typography.bodyMedium, color = c.textPrimary,   fontWeight = FontWeight.Medium)
            Text(subtitle, style = MaterialTheme.typography.labelSmall, color = c.textSecondary)
        }
        Icon(Icons.Default.ChevronRight, contentDescription = null, tint = c.textHint, modifier = Modifier.size(20.dp))
    }
}

// ── Info Row ──────────────────────────────────────────────────────────────────

@Composable
private fun InfoRow(icon: ImageVector, title: String, value: String) {
    val c = MaterialTheme.appColors
    Row(
        modifier          = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icon, contentDescription = null, tint = c.textSecondary, modifier = Modifier.size(20.dp))
        Spacer(Modifier.width(14.dp))
        Text(title, style = MaterialTheme.typography.bodyMedium, color = c.textPrimary, modifier = Modifier.weight(1f))
        Text(value, style = MaterialTheme.typography.bodySmall,  color = c.textHint)
    }
}

// ── Streak Info Card ──────────────────────────────────────────────────────────

@Composable
private fun StreakInfoCard(
    currentStreak: Int,
    bestStreak: Int,
    currentLabel: String,
    recordLabel: String
) {
    val c = MaterialTheme.appColors
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(c.card)
            .padding(20.dp)
    ) {
        Row(
            modifier              = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(Icons.Default.Whatshot, contentDescription = null, tint = AccentAmber, modifier = Modifier.size(36.dp))
                Text("$currentStreak", style = MaterialTheme.typography.titleLarge, color = AccentAmber, fontWeight = FontWeight.Bold)
                Text(currentLabel, style = MaterialTheme.typography.labelSmall, color = c.textSecondary)
            }
            HorizontalDivider(modifier = Modifier.height(60.dp).width(1.dp), color = c.divider)
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(Icons.Default.EmojiEvents, contentDescription = null, tint = PrimaryTeal, modifier = Modifier.size(36.dp))
                Text("$bestStreak", style = MaterialTheme.typography.titleLarge, color = PrimaryTeal, fontWeight = FontWeight.Bold)
                Text(recordLabel, style = MaterialTheme.typography.labelSmall, color = c.textSecondary)
            }
        }
    }
}
