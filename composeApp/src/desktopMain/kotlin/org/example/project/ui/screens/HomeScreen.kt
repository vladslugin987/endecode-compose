package org.example.project.ui.screens

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.awt.ComposeWindow
import androidx.compose.ui.unit.dp
import org.example.project.ui.components.ConsoleView
import org.example.project.ui.components.FileSelector
import org.example.project.ui.dialogs.*
import org.example.project.ui.theme.Dimensions
import org.example.project.viewmodels.HomeViewModel

@Composable
fun HomeScreen(window: ComposeWindow) {
    val viewModel = remember { HomeViewModel() }

    var showBatchCopyDialog by remember { mutableStateOf(false) }
    var showAddTextDialog by remember { mutableStateOf(false) }
    var showDeleteWatermarksDialog by remember { mutableStateOf(false) }

    val progressAnimation by animateFloatAsState(
        targetValue = viewModel.progress,
        label = "Progress"
    )

    Row(
        modifier = Modifier
            .fillMaxSize()
            .padding(Dimensions.spacingMedium),
        horizontalArrangement = Arrangement.spacedBy(Dimensions.spacingMedium)
    ) {
        // Left Panel - Controls
        Column(
            modifier = Modifier
                .weight(0.4f)
                .fillMaxHeight(),
            verticalArrangement = Arrangement.spacedBy(Dimensions.spacingMedium)
        ) {
            // Main Control Panel
            ElevatedCard(
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(Dimensions.spacingMedium),
                    verticalArrangement = Arrangement.spacedBy(Dimensions.spacingSmall)
                ) {
                    // File Selector
                    FileSelector(
                        selectedPath = viewModel.selectedPath,
                        onPathSelected = viewModel::updateSelectedPath,
                        window = window,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Divider(modifier = Modifier.padding(vertical = Dimensions.spacingMedium))

                    // Main Actions
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(Dimensions.spacingSmall)
                    ) {
                        Button(
                            onClick = { viewModel.decrypt() },
                            enabled = !viewModel.isProcessing,
                            modifier = Modifier
                                .weight(1f)
                                .height(Dimensions.buttonHeight)
                        ) {
                            Text("DECRYPT")
                        }

                        Button(
                            onClick = { viewModel.encrypt() },
                            enabled = !viewModel.isProcessing,
                            modifier = Modifier
                                .weight(1f)
                                .height(Dimensions.buttonHeight)
                        ) {
                            Text("ENCRYPT")
                        }
                    }

                    // Name input
                    OutlinedTextField(
                        value = viewModel.nameToInject,
                        onValueChange = viewModel::updateNameToInject,
                        label = { Text("Name to inject") },
                        supportingText = { Text("Only latin characters, numbers and special characters") },
                        enabled = !viewModel.isProcessing,
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )

                    // Additional actions
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(Dimensions.spacingSmall)
                    ) {
                        FilledTonalButton(
                            onClick = { showBatchCopyDialog = true },
                            enabled = !viewModel.isProcessing,
                            modifier = Modifier
                                .weight(1f)
                                .height(Dimensions.buttonHeight)
                        ) {
                            Text("Batch Copy")
                        }

                        FilledTonalButton(
                            onClick = { showAddTextDialog = true },
                            enabled = !viewModel.isProcessing,
                            modifier = Modifier
                                .weight(1f)
                                .height(Dimensions.buttonHeight)
                        ) {
                            Text("Add Text")
                        }
                    }

                    FilledTonalButton(
                        onClick = { showDeleteWatermarksDialog = true },
                        enabled = !viewModel.isProcessing,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(Dimensions.buttonHeight)
                    ) {
                        Text("Delete Watermarks")
                    }

                    // Auto-clear checkbox
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(top = Dimensions.spacingSmall)
                    ) {
                        Checkbox(
                            checked = viewModel.autoClearConsole,
                            onCheckedChange = viewModel::updateAutoClearConsole,
                            enabled = !viewModel.isProcessing
                        )
                        Text(
                            "Auto-clear console",
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier.padding(start = 8.dp)
                        )
                    }

                    // Progress indicator
                    if (viewModel.isProcessing) {
                        LinearProgressIndicator(
                            progress = progressAnimation,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = Dimensions.spacingMedium)
                        )
                    }
                }
            }
        }

        // Right Panel - Console
        ConsoleView(
            modifier = Modifier
                .weight(0.6f)
                .fillMaxHeight()
        )
    }

    // Dialogs
    if (showBatchCopyDialog) {
        BatchCopyDialog(
            onDismiss = { showBatchCopyDialog = false },
            onConfirm = { copies, baseText, addSwap, addWatermark, createZip, watermarkText, photoNumber ->
                showBatchCopyDialog = false
                viewModel.performBatchCopy(
                    numCopies = copies,
                    baseText = baseText,
                    addSwap = addSwap,
                    addWatermark = addWatermark,
                    createZip = createZip,
                    watermarkText = watermarkText,
                    photoNumber = photoNumber
                )
            }
        )
    }

    if (showAddTextDialog) {
        AddTextDialog(
            onDismiss = { showAddTextDialog = false },
            onConfirm = { text, photoNumber ->
                showAddTextDialog = false
                viewModel.addTextToPhoto(text, photoNumber)
            }
        )
    }

    if (showDeleteWatermarksDialog) {
        DeleteWatermarksDialog(
            onDismiss = { showDeleteWatermarksDialog = false },
            onConfirm = {
                showDeleteWatermarksDialog = false
                viewModel.removeWatermarks()
            }
        )
    }
}