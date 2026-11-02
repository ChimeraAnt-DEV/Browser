package com.browseglass.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties

/**
 * Beautiful glassmorphic permission prompt (Phase 5.32). Used for Camera,
 * Microphone and Location — invoked from GeckoSession.PermissionDelegate.
 */
@Composable
fun GlassPermissionDialog(
    title: String,
    description: String,
    iconAsText: String,
    onAllow: () -> Unit,
    onDeny: () -> Unit,
) {
    Dialog(
        onDismissRequest = onDeny,
        properties = DialogProperties(usePlatformDefaultWidth = false),
    ) {
        Column(
            Modifier
                .fillMaxWidth(0.85f)
                .glassPanel(fillAlpha = 0.30f, blurAmount = 28)
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text(iconAsText, style = MaterialTheme.typography.displaySmall)
            Text(
                title,
                style = MaterialTheme.typography.titleLarge,
                color = Color.White,
            )
            Text(
                description,
                style = MaterialTheme.typography.bodyMedium,
                color = Color.White.copy(alpha = 0.7f),
            )
            androidx.compose.foundation.layout.Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Button(
                    onClick = onDeny,
                    colors = androidx.compose.material3.ButtonDefaults.buttonColors(
                        containerColor = Color.White.copy(alpha = 0.12f),
                    ),
                ) {
                    Text("Deny")
                }
                Button(onClick = onAllow) {
                    Text("Allow")
                }
            }
        }
    }
}