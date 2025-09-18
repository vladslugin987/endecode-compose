package org.example.project.ui.dialogs

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import org.example.project.ui.components.AnimatedGlassButton
import org.example.project.ui.components.ButtonVariant
import org.example.project.ui.components.GlassCard
import org.example.project.ui.theme.*

@Composable
fun DeleteWatermarksDialog(
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    var confirmationStep by remember { mutableStateOf(false) }
    
    AlertDialog(
        modifier = Modifier.width(480.dp),
        onDismissRequest = onDismiss,
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(Dimensions.spacingMedium)
            ) {
                Icon(
                    imageVector = Icons.Default.Warning,
                    contentDescription = "Warning",
                    tint = Error500,
                    modifier = Modifier.size(Dimensions.iconLarge)
                )
                Text(
                    if (confirmationStep) "Confirm Deletion" else "Delete Watermarks",
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.SemiBold
                    ),
                    color = Error500
                )
            }
        },
        text = {
            Column(
                modifier = Modifier.padding(Dimensions.spacingMedium),
                verticalArrangement = Arrangement.spacedBy(Dimensions.spacingMedium)
            ) {
                if (!confirmationStep) {
                    // Initial warning
                    GlassCard(
                        backgroundColor = Error500.copy(alpha = 0.1f),
                        borderColor = Error500.copy(alpha = 0.3f)
                    ) {
                        Column(
                            modifier = Modifier.padding(Dimensions.cardPadding),
                            verticalArrangement = Arrangement.spacedBy(Dimensions.spacingSmall)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(Dimensions.spacingSmall)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Info,
                                    contentDescription = null,
                                    tint = Error500,
                                    modifier = Modifier.size(Dimensions.iconSmall)
                                )
                                Text(
                                    "What will happen:",
                                    style = MaterialTheme.typography.labelMedium.copy(
                                        fontWeight = FontWeight.SemiBold
                                    ),
                                    color = Error500
                                )
                            }
                            Text(
                                "• Remove invisible watermarks from all files in selected folder",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                "• Create backup copies automatically (.bak files)",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                "• Process may take time for large files",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                    
                    Text(
                        "This operation will remove watermarks permanently. Backup files will be created for safety.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                } else {
                    // Final confirmation
                    GlassCard(
                        backgroundColor = Error500.copy(alpha = 0.15f),
                        borderColor = Error500.copy(alpha = 0.4f)
                    ) {
                        Column(
                            modifier = Modifier.padding(Dimensions.cardPadding),
                            verticalArrangement = Arrangement.spacedBy(Dimensions.spacingSmall)
                        ) {
                            Text(
                                "⚠️ Final Confirmation",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold
                                ),
                                color = Error500
                            )
                            Text(
                                "Are you absolutely sure you want to delete watermarks?",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                "This action will start immediately and cannot be stopped.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            if (!confirmationStep) {
                AnimatedGlassButton(
                    onClick = { confirmationStep = true },
                    variant = ButtonVariant.DESTRUCTIVE
                ) {
                    Text("Continue")
                }
            } else {
                AnimatedGlassButton(
                    onClick = onConfirm,
                    variant = ButtonVariant.DESTRUCTIVE
                ) {
                    Icon(
                        imageVector = Icons.Default.DeleteForever,
                        contentDescription = null,
                        modifier = Modifier.size(Dimensions.iconSmall)
                    )
                    Spacer(Modifier.width(Dimensions.spacingSmall))
                    Text("Delete Watermarks", fontWeight = FontWeight.SemiBold)
                }
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(
                    "Cancel",
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    )
}