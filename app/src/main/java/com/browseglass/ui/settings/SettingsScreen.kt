package com.browseglass.ui.settings

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.browseglass.data.Prefs
import kotlinx.coroutines.launch

/**
 * Settings screen (Phase 4 + Phase 5 toggles), glass-themed and driven by
 * DataStore-backed [Prefs].
 */
@Composable
fun SettingsScreen(prefs: Prefs) {
    val scope = rememberCoroutineScope()

    val httpsOnly by prefs.httpsOnly.collectAsStateWithLifecycle(initialValue = true)
    val cookieBlock by prefs.cookieBannerBlocking.collectAsStateWithLifecycle(initialValue = true)
    val blockAds by prefs.blockAds.collectAsStateWithLifecycle(initialValue = true)
    val dataSaver by prefs.dataSaver.collectAsStateWithLifecycle(initialValue = false)
    val biometricLock by prefs.biometricLock.collectAsStateWithLifecycle(initialValue = true)
    val storedTextScale by prefs.textScale.collectAsStateWithLifecycle(initialValue = 1f)
    var textScale by remember { mutableFloatStateOf(storedTextScale) }

    val layoutCycler = remember { arrayOf(Prefs.LAYOUT_BOTTOM, Prefs.LAYOUT_TOP, Prefs.LAYOUT_HIDDEN) }
    var layoutIndex = remember { androidx.compose.runtime.mutableIntStateOf(0) }

    Column(
        Modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
            .padding(start = 20.dp, end = 20.dp, bottom = 120.dp),
    ) {
        SectionTitle("Privacy & security")
        ToggleRow(
            "HTTPS-only mode",
            "Upgrade all connections to HTTPS",
            httpsOnly,
            onToggle = { v -> scope.launch { prefs.setHttpsOnly(v) } },
        )
        ToggleRow(
            "Cookie banner blocking",
            "Hide consent popups",
            cookieBlock,
            onToggle = { v -> scope.launch { prefs.setCookieBannerBlocking(v) } },
        )
        ToggleRow(
            "Block ads",
            "Native content blocker (Rust core)",
            blockAds,
            onToggle = { v -> scope.launch { prefs.setBlockAds(v) } },
        )
        ToggleRow(
            "Data saver",
            "Block trackers and lazy-load images",
            dataSaver,
            onToggle = { v -> scope.launch { prefs.setDataSaver(v) } },
        )
        ToggleRow(
            "Biometric lock",
            "Require fingerprint for private tabs",
            biometricLock,
            onToggle = { v -> scope.launch { prefs.setBiometricLock(v) } },
        )

        HorizontalDivider(Modifier.padding(vertical = 16.dp))

        SectionTitle("Appearance")
        Text(
            "Text scale: ${"%.2f".format(textScale)}×",
            style = MaterialTheme.typography.bodyMedium,
        )
        Slider(
            value = textScale,
            onValueChange = { v ->
                textScale = v
                scope.launch { prefs.setTextScale(Prefs.clampTextScale(v)) }
            },
            valueRange = 0.5f..2.0f,
        )

        Row(
            Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text("Toolbar layout", modifier = Modifier.weight(1f))
            val label = when (layoutCycler[layoutIndex.intValue]) {
                Prefs.LAYOUT_TOP -> "Top"
                Prefs.LAYOUT_HIDDEN -> "Hidden (full screen)"
                else -> "Bottom"
            }
            Text(
                label,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier
                    .clickable {
                        layoutIndex.intValue = (layoutIndex.intValue + 1) % layoutCycler.size
                        scope.launch {
                            prefs.setToolbarLayout(layoutCycler[layoutIndex.intValue])
                        }
                    }
                    .padding(4.dp),
            )
        }
    }
}

@Composable
private fun SectionTitle(text: String) {
    Text(
        text,
        style = MaterialTheme.typography.titleSmall,
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier.padding(vertical = 8.dp),
    )
}

@Composable
private fun ToggleRow(label: String, desc: String, checked: Boolean, onToggle: (Boolean) -> Unit) {
    Row(
        Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(Modifier.weight(1f)) {
            Text(label, style = MaterialTheme.typography.bodyLarge)
            Text(
                desc,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        Switch(checked = checked, onCheckedChange = onToggle)
    }
}