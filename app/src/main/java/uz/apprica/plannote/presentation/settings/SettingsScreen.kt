package uz.apprica.plannote.presentation.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
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
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import uz.apprica.plannote.ui.theme.AccentAmber
import uz.apprica.plannote.ui.theme.ErrorRed
import uz.apprica.plannote.ui.theme.PrimaryTeal
import uz.apprica.plannote.ui.theme.appColors

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(viewModel: SettingsViewModel = hiltViewModel()) {
    val c     = MaterialTheme.appColors
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    var showResetDialog   by remember { mutableStateOf(false) }
    val snackbarHostState = remember { SnackbarHostState() }
    var snackMsg          by remember { mutableStateOf<String?>(null) }

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
                text       = "Sozlamalar",
                style      = MaterialTheme.typography.headlineSmall,
                color      = c.textPrimary,
                fontWeight = FontWeight.Bold
            )

            // ── Streak info ───────────────────────────────────────────────────
            StreakInfoCard(currentStreak = state.currentStreak, bestStreak = state.bestStreak)

            // ── Ko'rinish + Streak ────────────────────────────────────────────
            SettingsSection {
                ToggleRow(
                    icon            = if (state.isDarkMode) Icons.Default.DarkMode else Icons.Default.LightMode,
                    title           = "Tungi rejim",
                    subtitle        = if (state.isDarkMode) "Qorong'i fon" else "Yorug' fon",
                    checked         = state.isDarkMode,
                    onCheckedChange = { viewModel.setDarkMode(it) }
                )
                HorizontalDivider(color = c.divider, modifier = Modifier.padding(horizontal = 8.dp))
                ClickableRow(
                    icon      = Icons.Default.RestartAlt,
                    title     = "Streakni nollashtirish",
                    subtitle  = "Joriy: ${state.currentStreak} kun",
                    onClick   = { showResetDialog = true },
                    tintColor = ErrorRed
                )
            }

            // ── Versiya + Ishlab chiquvchi + Min. Android ─────────────────────
            SettingsSection {
                InfoRow(icon = Icons.Default.Info,    title = "Versiya",                value = "1.0.0")
                HorizontalDivider(color = c.divider, modifier = Modifier.padding(horizontal = 8.dp))
                InfoRow(icon = Icons.Default.Code,    title = "Ishlab chiquvchi",        value = "Plannote Team")
                HorizontalDivider(color = c.divider, modifier = Modifier.padding(horizontal = 8.dp))
                InfoRow(icon = Icons.Default.Android, title = "Min. Android versiyasi",  value = "Android 7.0+")
            }

            Spacer(Modifier.height(24.dp))
        }
    }

    if (showResetDialog) {
        AlertDialog(
            onDismissRequest = { showResetDialog = false },
            containerColor   = MaterialTheme.appColors.card,
            title  = { Text("Streakni nollashtirish", color = MaterialTheme.appColors.textPrimary, fontWeight = FontWeight.Bold) },
            text   = {
                Text(
                    "Joriy streak (${state.currentStreak} kun) o'chiriladi. Bu amalni qaytarib bo'lmaydi.",
                    color = MaterialTheme.appColors.textSecondary
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.confirmResetStreak()
                    showResetDialog = false
                    snackMsg = "Streak nollashtirildi ✓"
                }) {
                    Text("Ha, nollash", color = ErrorRed, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showResetDialog = false }) {
                    Text("Bekor qilish", color = MaterialTheme.appColors.textSecondary)
                }
            }
        )
    }
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
            Text(title,    style = MaterialTheme.typography.bodyMedium,  color = c.textPrimary,   fontWeight = FontWeight.Medium)
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
            Text(title,    style = MaterialTheme.typography.bodyMedium,  color = c.textPrimary,   fontWeight = FontWeight.Medium)
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
private fun StreakInfoCard(currentStreak: Int, bestStreak: Int) {
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
                Text("Joriy streak", style = MaterialTheme.typography.labelSmall, color = c.textSecondary)
            }
            HorizontalDivider(modifier = Modifier.height(60.dp).width(1.dp), color = c.divider)
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(Icons.Default.EmojiEvents, contentDescription = null, tint = PrimaryTeal, modifier = Modifier.size(36.dp))
                Text("$bestStreak", style = MaterialTheme.typography.titleLarge, color = PrimaryTeal, fontWeight = FontWeight.Bold)
                Text("Rekord streak", style = MaterialTheme.typography.labelSmall, color = c.textSecondary)
            }
        }
    }
}
