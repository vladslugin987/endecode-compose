package org.example.project.ui.screens

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.awt.ComposeWindow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import org.example.project.ui.components.ConsoleView
import org.example.project.ui.components.FileSelector
import org.example.project.ui.dialogs.*
import org.example.project.ui.theme.Dimensions
import org.example.project.viewmodels.HomeViewModel

@Composable
internal fun MainControlPanel(
    viewModel: HomeViewModel,
    window: ComposeWindow,
    onBatchCopy: () -> Unit,
    onAddText: () -> Unit,
    onDeleteWatermarks: () -> Unit,
    progressValue: Float
) {
    Card(
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
                    onClick = onBatchCopy,
                    enabled = !viewModel.isProcessing,
                    modifier = Modifier
                        .weight(1f)
                        .height(Dimensions.buttonHeight)
                ) {
                    Text("Batch Copy")
                }

                FilledTonalButton(
                    onClick = onAddText,
                    enabled = !viewModel.isProcessing,
                    modifier = Modifier
                        .weight(1f)
                        .height(Dimensions.buttonHeight)
                ) {
                    Text("Add Text")
                }
            }

            FilledTonalButton(
                onClick = onDeleteWatermarks,
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
                    progress = progressValue,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = Dimensions.spacingMedium)
                )
            }
        }
    }
}