package uz.apprica.plannote.presentation.language

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import uz.apprica.plannote.ui.theme.PrimaryTeal
import uz.apprica.plannote.ui.theme.appColors
import uz.apprica.plannote.ui.theme.strings

// ── Language options ──────────────────────────────────────────────────────────

private val LANGUAGES = listOf(
    Triple("uz", "O'zbekcha", "🇺🇿"),
    Triple("ru", "Русский",   "🇷🇺"),
    Triple("en", "English",   "🇬🇧")
)

// ── Screen ────────────────────────────────────────────────────────────────────

@Composable
fun LanguageScreen(
    onContinue: () -> Unit,
    viewModel: LanguageViewModel = hiltViewModel()
) {
    val s             = MaterialTheme.strings
    val c             = MaterialTheme.appColors
    val savedLanguage by viewModel.currentLanguage.collectAsStateWithLifecycle()
    var selectedLang  by remember(savedLanguage) { mutableStateOf(savedLanguage) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(c.background)
            .statusBarsPadding()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.Center)
                .padding(horizontal = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            // ── Globe icon ────────────────────────────────────────────────────
            Text("🌐", fontSize = 64.sp)

            // ── Title ─────────────────────────────────────────────────────────
            Text(
                text       = s.selectLanguage,
                style      = MaterialTheme.typography.headlineSmall,
                color      = c.textPrimary,
                fontWeight = FontWeight.Bold
            )

            // ── Description ───────────────────────────────────────────────────
            Text(
                text  = s.selectLanguageDesc,
                style = MaterialTheme.typography.bodyMedium,
                color = c.textSecondary
            )

            Spacer(Modifier.height(8.dp))

            // ── Language cards ────────────────────────────────────────────────
            LANGUAGES.forEach { (code, name, flag) ->
                val isSelected = selectedLang == code
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(if (isSelected) PrimaryTeal.copy(alpha = 0.12f) else c.card)
                        .then(
                            if (isSelected)
                                Modifier.border(2.dp, PrimaryTeal, RoundedCornerShape(16.dp))
                            else
                                Modifier
                        )
                        .clickable { selectedLang = code }
                        .padding(16.dp),
                    verticalAlignment     = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Text(flag, fontSize = 28.sp)
                    Text(
                        text       = name,
                        style      = MaterialTheme.typography.titleMedium,
                        color      = if (isSelected) PrimaryTeal else c.textPrimary,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                        modifier   = Modifier.weight(1f)
                    )
                    if (isSelected) {
                        Icon(
                            imageVector        = Icons.Default.Check,
                            contentDescription = null,
                            tint               = PrimaryTeal,
                            modifier           = Modifier.size(22.dp)
                        )
                    }
                }
            }

            Spacer(Modifier.height(8.dp))

            // ── Continue button ───────────────────────────────────────────────
            Button(
                onClick = {
                    viewModel.setLanguage(selectedLang)
                    onContinue()
                },
                modifier = Modifier.fillMaxWidth().height(54.dp),
                shape    = RoundedCornerShape(14.dp),
                colors   = ButtonDefaults.buttonColors(
                    containerColor = PrimaryTeal,
                    contentColor   = c.background
                )
            ) {
                Text(s.continueBtn, fontWeight = FontWeight.SemiBold, fontSize = 16.sp)
            }
        }
    }
}
