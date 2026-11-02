package com.browseglass.ui.settings

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

/**
 * User-script editor (Phase 4.24): add / toggle / delete injected JS + CSS per
 * site pattern. The list is persisted as JSON via DataStore.
 */
@Composable
fun UserScriptsScreen(
    scripts: List<UserScriptUi>,
    onToggle: (Long, Boolean) -> Unit,
    onDelete: (Long) -> Unit,
    onAdd: (String, String, String) -> Unit,
) {
    var pattern by remember { mutableStateOf("") }
    var js by remember { mutableStateOf("") }
    var css by remember { mutableStateOf("") }

    Column(Modifier.padding(16.dp)) {
        Text("User scripts", style = MaterialTheme.typography.titleMedium)

        LazyColumn {
            items(scripts, key = { it.id }) { script ->
                Row(
                    Modifier
                        .fillMaxWidth()
                        .padding(vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Column(Modifier.weight(1f)) {
                        Text(script.matchPattern, style = MaterialTheme.typography.bodyLarge)
                        Text(
                            (if (script.js.isNotBlank()) "JS " else "") +
                                (if (script.css.isNotBlank()) "CSS" else "") +
                                if (script.js.isBlank() && script.css.isBlank()) "empty" else "",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                    Switch(checked = script.enabled, onCheckedChange = { onToggle(script.id, it) })
                    IconButton(onClick = { onDelete(script.id) }) {
                        Icon(Icons.Default.Delete, "Delete")
                    }
                }
            }
        }

        OutlinedTextField(
            value = pattern,
            onValueChange = { pattern = it },
            label = { Text("Site pattern (e.g. *://*.example.com/*)") },
            modifier = Modifier.fillMaxWidth(),
        )
        OutlinedTextField(
            value = js,
            onValueChange = { js = it },
            label = { Text("JavaScript to inject") },
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp),
        )
        OutlinedTextField(
            value = css,
            onValueChange = { css = it },
            label = { Text("CSS to inject") },
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp),
        )
        IconButton(onClick = {
            if (pattern.isNotBlank() && (js.isNotBlank() || css.isNotBlank())) {
                onAdd(pattern.trim(), js, css)
                pattern = ""
                js = ""
                css = ""
            }
        }) {
            Icon(Icons.Default.Add, "Add script")
        }
    }
}

data class UserScriptUi(
    val id: Long,
    val matchPattern: String,
    val js: String,
    val css: String,
    val enabled: Boolean,
)

fun com.browseglass.data.UserScript.toUi() = UserScriptUi(
    id = id,
    matchPattern = matchPattern,
    js = js,
    css = css,
    enabled = enabled,
)