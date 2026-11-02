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
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.browseglass.data.BastionNative

/**
 * Add / remove / set-default search engines (Phase 4.23). The engine list is a
 * light wrapper over the Rust core defaults reported via [BastionNative].
 */
@Composable
fun SearchEngineManager(
    engines: List<SearchEngineEntry>,
    defaultName: String,
    onSetDefault: (String) -> Unit,
    onAdd: (String, String) -> Unit,
    onRemove: (String) -> Unit,
) {
    var newName by remember { mutableStateOf("") }
    var newUrl by remember { mutableStateOf("") }

    Column(Modifier.padding(16.dp)) {
        Text("Search engines", style = MaterialTheme.typography.titleMedium)
        LazyColumn {
            items(engines, key = { it.name }) { engine ->
                Row(
                    Modifier
                        .fillMaxWidth()
                        .clickable { onSetDefault(engine.name) }
                        .padding(vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(engine.name, modifier = Modifier.weight(1f))
                    if (engine.name == defaultName) {
                        Icon(Icons.Default.Check, "Default", tint = MaterialTheme.colorScheme.primary)
                    }
                    IconButton(onClick = { onRemove(engine.name) }) {
                        Text("✕", style = MaterialTheme.typography.titleSmall)
                    }
                }
            }
        }

        Row(verticalAlignment = Alignment.CenterVertically) {
            OutlinedTextField(
                value = newName,
                onValueChange = { newName = it },
                label = { Text("Name") },
                modifier = Modifier.weight(1f),
            )
        }
        OutlinedTextField(
            value = newUrl,
            onValueChange = { newUrl = it },
            label = { Text("URL with {query}") },
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp),
        )
        IconButton(onClick = {
            if (newName.isNotBlank() && newUrl.contains("{query}")) {
                onAdd(newName.trim(), newUrl.trim())
                newName = ""
                newUrl = ""
            }
        }) {
            Icon(Icons.Default.Add, "Add engine")
        }
    }
}

data class SearchEngineEntry(val name: String, val urlTemplate: String)