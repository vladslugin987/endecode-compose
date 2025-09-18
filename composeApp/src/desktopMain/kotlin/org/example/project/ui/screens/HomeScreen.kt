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
                ProfileSection(onBack = { showProfile = false })
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

@Composable
private fun ProfileSection(onBack: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(Dimensions.spacingLarge)
    ) {
        // Profile Header
        ProfileHeaderCard(onBack = onBack)
        
        Row(
            modifier = Modifier.fillMaxSize(),
            horizontalArrangement = Arrangement.spacedBy(Dimensions.spacingLarge)
        ) {
            // Left Column
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(Dimensions.spacingLarge)
            ) {
                // Statistics Card
                ProfileStatsCard()
                
                // Activity Card
                RecentActivityCard()
            }
            
            // Right Column
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(Dimensions.spacingLarge)
            ) {
                // Settings Card
                ProfileSettingsCard()
                
                // Recent Files Card
                RecentFilesCard()
            }
        }
    }
}

@Composable
private fun ProfileHeaderCard(onBack: () -> Unit) {
    GlassCard(borderRadius = Dimensions.radiusLarge) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(Dimensions.cardPaddingLarge),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(Dimensions.spacingLarge)
            ) {
                // Avatar with glassmorphism
                Box(
                    modifier = Modifier.size(72.dp),
                    contentAlignment = Alignment.Center
                ) {
                    GlassCard(
                        borderRadius = Dimensions.radiusXLarge,
                        modifier = Modifier.fillMaxSize()
                    ) {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "V",
                                style = MaterialTheme.typography.headlineLarge.copy(
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 28.sp
                                ),
                                color = Primary400
                            )
                        }
                    }
                    // Status indicator
                    Box(
                        modifier = Modifier
                            .size(14.dp)
                            .align(Alignment.BottomEnd)
                    ) {
                        StatusIndicator(
                            isActive = true,
                            activeColor = TerminalSuccess,
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                }
                
                Column(verticalArrangement = Arrangement.spacedBy(Dimensions.spacingXSmall)) {
                    Text(
                        text = "Vsdev",
                        style = MaterialTheme.typography.headlineSmall.copy(
                            fontWeight = FontWeight.Bold
                        ),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(Dimensions.spacingSmall)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Verified,
                            contentDescription = null,
                            tint = TerminalSuccess,
                            modifier = Modifier.size(Dimensions.iconSmall)
                        )
                        Text(
                            text = "Developer",
                            style = MaterialTheme.typography.bodyMedium,
                            color = TerminalSuccess
                        )
                    }
                    Text(
                        text = "Member since October 2024",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            
            AnimatedGlassButton(
                onClick = onBack,
                isPrimary = false,
                modifier = Modifier.height(Dimensions.buttonHeight)
            ) {
                Icon(
                    imageVector = Icons.Default.ArrowBack,
                    contentDescription = null,
                    modifier = Modifier.size(Dimensions.iconSmall)
                )
                Spacer(Modifier.width(Dimensions.spacingSmall))
                Text("Back")
            }
        }
    }
}

@Composable
private fun ProfileStatsCard() {
    GlassCard(borderRadius = Dimensions.radiusLarge) {
        Column(
            modifier = Modifier.padding(Dimensions.cardPaddingLarge),
            verticalArrangement = Arrangement.spacedBy(Dimensions.spacingLarge)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(Dimensions.spacingSmall)
            ) {
                Icon(
                    imageVector = Icons.Default.Analytics,
                    contentDescription = null,
                    tint = Secondary400,
                    modifier = Modifier.size(Dimensions.iconMedium)
                )
                Text(
                    text = "Statistics",
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.SemiBold
                    ),
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
            
            Column(verticalArrangement = Arrangement.spacedBy(Dimensions.spacingMedium)) {
                val stats = listOf(
                    Triple(Icons.Default.Lock, "Files Encrypted", "342"),
                    Triple(Icons.Default.LockOpen, "Files Decrypted", "189"),
                    Triple(Icons.Default.ContentCopy, "Batch Operations", "47"),
                    Triple(Icons.Default.TextFields, "Text Additions", "23")
                )
                
                stats.forEach { (icon, name, value) ->
                    StatItem(icon = icon, name = name, value = value)
                }
            }
        }
    }
}

@Composable
private fun StatItem(icon: ImageVector, name: String, value: String) {
    GlassCard(borderRadius = Dimensions.radiusMedium) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(Dimensions.cardPadding),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(Dimensions.spacingMedium)
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = Primary400,
                    modifier = Modifier.size(Dimensions.iconMedium)
                )
                Text(
                    text = name,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Text(
                text = value,
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold
                ),
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

@Composable
private fun RecentActivityCard() {
    GlassCard(borderRadius = Dimensions.radiusLarge) {
        Column(
            modifier = Modifier.padding(Dimensions.cardPaddingLarge),
            verticalArrangement = Arrangement.spacedBy(Dimensions.spacingLarge)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(Dimensions.spacingSmall)
            ) {
                Icon(
                    imageVector = Icons.Default.Timeline,
                    contentDescription = null,
                    tint = Accent400,
                    modifier = Modifier.size(Dimensions.iconMedium)
                )
                Text(
                    text = "Recent Activity",
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.SemiBold
                    ),
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
            
            Column(verticalArrangement = Arrangement.spacedBy(Dimensions.spacingMedium)) {
                val activities = listOf(
                    Triple("Encrypted document.pdf", "2 minutes ago", Icons.Default.Lock),
                    Triple("Batch copied 5 files", "1 hour ago", Icons.Default.ContentCopy),
                    Triple("Added watermark to image.jpg", "3 hours ago", Icons.Default.TextFields),
                    Triple("Decrypted archive.zip", "Yesterday", Icons.Default.LockOpen)
                )
                
                activities.forEach { (action, time, icon) ->
                    ActivityItem(action = action, time = time, icon = icon)
                }
            }
        }
    }
}

@Composable
private fun ActivityItem(action: String, time: String, icon: ImageVector) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(Dimensions.spacingMedium),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = Primary400,
            modifier = Modifier.size(Dimensions.iconSmall)
        )
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = action,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = time,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun ProfileSettingsCard() {
    GlassCard(borderRadius = Dimensions.radiusLarge) {
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
                    text = "Preferences",
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.SemiBold
                    ),
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
            
            Column(verticalArrangement = Arrangement.spacedBy(Dimensions.spacingMedium)) {
                SettingItem(
                    icon = Icons.Default.Notifications,
                    title = "Notifications",
                    subtitle = "Get notified about operations",
                    isEnabled = true
                )
                SettingItem(
                    icon = Icons.Default.Security,
                    title = "Auto-encrypt",
                    subtitle = "Automatically encrypt new files",
                    isEnabled = false
                )
                SettingItem(
                    icon = Icons.Default.Backup,
                    title = "Auto-backup",
                    subtitle = "Create backups before operations",
                    isEnabled = true
                )
            }
        }
    }
}

@Composable
private fun SettingItem(
    icon: ImageVector,
    title: String,
    subtitle: String,
    isEnabled: Boolean
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(Dimensions.spacingMedium),
            modifier = Modifier.weight(1f)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = if (isEnabled) Primary400 else MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(Dimensions.iconMedium)
            )
            Column {
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        Switch(
            checked = isEnabled,
            onCheckedChange = { /* TODO: Handle setting change */ },
            colors = SwitchDefaults.colors(
                checkedThumbColor = Primary400,
                checkedTrackColor = Primary400.copy(alpha = 0.3f),
                uncheckedThumbColor = MaterialTheme.colorScheme.onSurfaceVariant,
                uncheckedTrackColor = MaterialTheme.colorScheme.surfaceVariant
            )
        )
    }
}

@Composable
private fun RecentFilesCard() {
    GlassCard(borderRadius = Dimensions.radiusLarge) {
        Column(
            modifier = Modifier.padding(Dimensions.cardPaddingLarge),
            verticalArrangement = Arrangement.spacedBy(Dimensions.spacingLarge)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(Dimensions.spacingSmall)
                ) {
                    Icon(
                        imageVector = Icons.Default.History,
                        contentDescription = null,
                        tint = Accent400,
                        modifier = Modifier.size(Dimensions.iconMedium)
                    )
                    Text(
                        text = "Recent Files",
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.SemiBold
                        ),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
                TextButton(onClick = { /* TODO: Show all files */ }) {
                    Text(
                        text = "View All",
                        color = Primary400,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
            
            Column(verticalArrangement = Arrangement.spacedBy(Dimensions.spacingMedium)) {
                val files = listOf(
                    Triple("document.pdf", "2.3 MB", Icons.Default.PictureAsPdf),
                    Triple("image.jpg", "1.8 MB", Icons.Default.Image),
                    Triple("archive.zip", "15.2 MB", Icons.Default.Archive),
                    Triple("data.txt", "0.5 MB", Icons.Default.TextSnippet)
                )
                
                files.forEach { (name, size, icon) ->
                    FileItem(name = name, size = size, icon = icon)
                }
            }
        }
    }
}

@Composable
private fun FileItem(name: String, size: String, icon: ImageVector) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(Dimensions.spacingMedium),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = Primary400,
            modifier = Modifier.size(Dimensions.iconMedium)
        )
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = name,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = size,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        IconButton(
            onClick = { /* TODO: Handle file action */ },
            modifier = Modifier.size(Dimensions.iconButtonSize)
        ) {
            Icon(
                imageVector = Icons.Default.MoreVert,
                contentDescription = "File options",
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(Dimensions.iconSmall)
            )
        }
    }
}