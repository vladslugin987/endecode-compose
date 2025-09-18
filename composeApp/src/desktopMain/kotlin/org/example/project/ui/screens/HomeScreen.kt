package org.example.project.ui.screens

import androidx.compose.animation.core.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.TextButton
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.awt.ComposeWindow
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.example.project.ui.components.*
import org.example.project.ui.dialogs.*
import org.example.project.ui.theme.*
import org.example.project.viewmodels.HomeViewModel
import org.example.project.viewmodels.ThemeViewModel


@Composable
fun HomeScreen(window: ComposeWindow, themeViewModel: ThemeViewModel) {
    val viewModel = remember { HomeViewModel() }
    var showProfile by remember { mutableStateOf(false) }

    var showBatchCopyDialog by remember { mutableStateOf(false) }
    var showAddTextDialog by remember { mutableStateOf(false) }
    var showDeleteWatermarksDialog by remember { mutableStateOf(false) }

    val progressAnimation by animateFloatAsState(
        targetValue = viewModel.progress,
        label = "Progress"
    )

    GradientBackground(
        modifier = Modifier.fillMaxSize()
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(Dimensions.spacingLarge),
            verticalArrangement = Arrangement.spacedBy(Dimensions.spacingLarge)
        ) {
            // Modern Top Bar
            ModernTopBar(themeViewModel, onOpenProfile = { showProfile = true })
            
            // Switch between Profile and Main
            if (showProfile) {
                // Temporary simple profile - TODO: implement enhanced profile
                GlassCard(borderRadius = Dimensions.radiusLarge) {
                    Column(modifier = Modifier.padding(Dimensions.cardPaddingLarge)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Profile Section",
                                style = MaterialTheme.typography.headlineSmall,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            AnimatedGlassButton(
                                onClick = { showProfile = false },
                                isPrimary = false
                            ) {
                                Text("Back")
                            }
                        }
                        Spacer(Modifier.height(Dimensions.spacingLarge))
                        Text(
                            text = "Enhanced profile UI coming soon...",
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            } else {
                // Main Content Grid
                Row(
                modifier = Modifier.fillMaxSize(),
                horizontalArrangement = Arrangement.spacedBy(Dimensions.spacingLarge)
            ) {
                // Left Panel - Control Dashboard
                Column(
                    modifier = Modifier
                        .weight(0.42f)
                        .fillMaxHeight(),
                    verticalArrangement = Arrangement.spacedBy(Dimensions.spacingLarge)
                ) {
                    // File Selection Card
                    FileSelectionCard(
                        viewModel = viewModel,
                        window = window
                    )
                    
                    // Main Actions Card
                    MainActionsCard(
                        viewModel = viewModel,
                        progressAnimation = progressAnimation,
                        onBatchCopy = { showBatchCopyDialog = true },
                        onAddText = { showAddTextDialog = true },
                        onDeleteWatermarks = { showDeleteWatermarksDialog = true }
                    )
                }

                // Right Panel - Terminal Console
                ConsoleView(
                    modifier = Modifier
                        .weight(0.58f)
                        .fillMaxHeight()
                )
            }
            }
        }
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

@Composable
private fun ModernTopBar(
    themeViewModel: ThemeViewModel,
    modifier: Modifier = Modifier,
    onOpenProfile: () -> Unit = {}
) {
    GlassCard(
        modifier = modifier.fillMaxWidth(),
        borderRadius = Dimensions.radiusLarge
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(Dimensions.cardPaddingLarge),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(Dimensions.spacingLarge)
            ) {
                // App Logo/Icon
                Icon(
                    imageVector = Icons.Default.Security,
                    contentDescription = "ENDEcode",
                    modifier = Modifier.size(Dimensions.iconXLarge),
                    tint = Primary400
                )
                
                Column {
                    Text(
                        text = "ENDEcode",
                        style = MaterialTheme.typography.headlineSmall.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 24.sp
                        ),
                        color = DarkOnSurface
                    )
                    Text(
                        text = "File Encryption & Watermark Tool",
                        style = MaterialTheme.typography.bodySmall,
                        color = TerminalText.copy(alpha = 0.8f)
                    )
                }
                
                // Version badge
                Surface(
                    color = Primary500.copy(alpha = 0.2f),
                    shape = RoundedCornerShape(Dimensions.radiusSmall),
                    modifier = Modifier.padding(start = Dimensions.spacingSmall)
                ) {
                    Text(
                        text = "v2.1.1",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.SemiBold
                        ),
                        color = Primary400,
                        modifier = Modifier.padding(
                            horizontal = Dimensions.spacingSmall,
                            vertical = Dimensions.spacingXSmall
                        )
                    )
                }
            }
            
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(Dimensions.spacingSmall)
            ) {
                StatusIndicator(
                    isActive = true,
                    activeColor = TerminalSuccess
                )
                IconButton(onClick = onOpenProfile) {
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = "Profile",
                        tint = MaterialTheme.colorScheme.onSurface
                    )
                }
                QuickThemeToggle(themeViewModel)
            }
        }
    }
}

@Composable
private fun FileSelectionCard(
    viewModel: HomeViewModel,
    window: ComposeWindow
) {
    GlassCard(
        modifier = Modifier.fillMaxWidth(),
        borderRadius = Dimensions.radiusLarge
    ) {
        Column(
            modifier = Modifier.padding(Dimensions.cardPaddingLarge),
            verticalArrangement = Arrangement.spacedBy(Dimensions.spacingMedium)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(Dimensions.spacingSmall)
            ) {
                Icon(
                    imageVector = Icons.Default.Folder,
                    contentDescription = null,
                    tint = Accent400,
                    modifier = Modifier.size(Dimensions.iconMedium)
                )
                Text(
                    text = "File Selection",
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.SemiBold
                    ),
                    color = DarkOnSurface
                )
            }
            
            FileSelector(
                selectedPath = viewModel.selectedPath,
                onPathSelected = viewModel::updateSelectedPath,
                window = window,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@Composable
private fun MainActionsCard(
    viewModel: HomeViewModel,
    progressAnimation: Float,
    onBatchCopy: () -> Unit,
    onAddText: () -> Unit,
    onDeleteWatermarks: () -> Unit
) {
    GlassCard(
        modifier = Modifier.fillMaxWidth(),
        borderRadius = Dimensions.radiusLarge
    ) {
        Column(
            modifier = Modifier.padding(Dimensions.cardPaddingLarge),
            verticalArrangement = Arrangement.spacedBy(Dimensions.spacingLarge)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(Dimensions.spacingSmall)
            ) {
                Icon(
                    imageVector = Icons.Default.Settings,
                    contentDescription = null,
                    tint = Secondary400,
                    modifier = Modifier.size(Dimensions.iconMedium)
                )
                Text(
                    text = "Actions",
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.SemiBold
                    ),
                    color = DarkOnSurface
                )
            }
            
            // Primary Action Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(Dimensions.spacingMedium)
            ) {
                ModernActionButton(
                    onClick = { viewModel.decrypt() },
                    enabled = !viewModel.isProcessing,
                    icon = Icons.Default.LockOpen,
                    text = "DECRYPT",
                    modifier = Modifier.weight(1f),
                    isPrimary = true
                )

                ModernActionButton(
                    onClick = { viewModel.encrypt() },
                    enabled = !viewModel.isProcessing,
                    icon = Icons.Default.Lock,
                    text = "ENCRYPT",
                    modifier = Modifier.weight(1f),
                    isPrimary = true
                )
            }

            // Name input
            TerminalTextField(
                value = viewModel.nameToInject,
                onValueChange = viewModel::updateNameToInject,
                label = "Name to inject",
                supportingText = "Only latin characters, numbers and special characters",
                enabled = !viewModel.isProcessing,
                modifier = Modifier.fillMaxWidth()
            )

            // Secondary Actions Grid
            Column(
                verticalArrangement = Arrangement.spacedBy(Dimensions.spacingMedium)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(Dimensions.spacingMedium)
                ) {
                    ModernActionButton(
                        onClick = onBatchCopy,
                        enabled = !viewModel.isProcessing,
                        icon = Icons.Default.ContentCopy,
                        text = "Batch Copy",
                        modifier = Modifier.weight(1f),
                        isPrimary = false
                    )

                    ModernActionButton(
                        onClick = onAddText,
                        enabled = !viewModel.isProcessing,
                        icon = Icons.Default.TextFields,
                        text = "Add Text",
                        modifier = Modifier.weight(1f),
                        isPrimary = false
                    )
                }

                ModernActionButton(
                    onClick = onDeleteWatermarks,
                    enabled = !viewModel.isProcessing,
                    icon = Icons.Default.CleaningServices,
                    text = "Delete Watermarks",
                    modifier = Modifier.fillMaxWidth(),
                    isPrimary = false
                )
            }

            // Settings Row
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(Dimensions.spacingMedium),
                modifier = Modifier.fillMaxWidth()
            ) {
                Checkbox(
                    checked = viewModel.autoClearConsole,
                    onCheckedChange = viewModel::updateAutoClearConsole,
                    enabled = !viewModel.isProcessing,
                    colors = CheckboxDefaults.colors(
                        checkedColor = TerminalSuccess,
                        uncheckedColor = TerminalText,
                        checkmarkColor = GlassBackground
                    )
                )
                Text(
                    "Auto-clear console",
                    style = MaterialTheme.typography.bodyMedium,
                    color = DarkOnSurface,
                    modifier = Modifier.weight(1f)
                )
                
                if (viewModel.isProcessing) {
                    StatusIndicator(
                        isActive = true,
                        activeColor = TerminalWarning
                    )
                }
            }

            // Progress indicator with glassmorphism
            if (viewModel.isProcessing) {
                Column(
                    verticalArrangement = Arrangement.spacedBy(Dimensions.spacingSmall)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Processing...",
                            style = MaterialTheme.typography.bodySmall,
                            color = TerminalAccent
                        )
                        Text(
                            text = "${(progressAnimation * 100).toInt()}%",
                            style = MaterialTheme.typography.bodySmall.copy(
                                fontWeight = FontWeight.SemiBold
                            ),
                            color = TerminalAccent
                        )
                    }
                    LinearProgressIndicator(
                        progress = progressAnimation,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(6.dp),
                        color = Primary400,
                        trackColor = Primary400.copy(alpha = 0.2f)
                    )
                }
            }
        }
    }
}

@Composable
private fun ModernActionButton(
    onClick: () -> Unit,
    enabled: Boolean,
    icon: ImageVector,
    text: String,
    modifier: Modifier = Modifier,
    isPrimary: Boolean = false
) {
    AnimatedGlassButton(
        onClick = onClick,
        enabled = enabled,
        isPrimary = isPrimary,
        modifier = modifier.height(Dimensions.buttonHeightLarge)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.size(Dimensions.iconMedium)
        )
        Spacer(Modifier.width(Dimensions.spacingSmall))
        Text(
            text = text,
            fontWeight = if (isPrimary) FontWeight.SemiBold else FontWeight.Medium
        )
    }
}