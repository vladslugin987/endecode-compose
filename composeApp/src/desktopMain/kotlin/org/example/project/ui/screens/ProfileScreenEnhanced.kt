package org.example.project.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.example.project.ui.components.*
import org.example.project.ui.theme.*

@Composable
fun EnhancedProfileScreen(onBack: () -> Unit) {
    GradientBackground(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier.fillMaxSize().padding(Dimensions.spacingLarge),
            verticalArrangement = Arrangement.spacedBy(Dimensions.spacingLarge)
        ) {
            // Enhanced Profile Header
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
                    // Enhanced Statistics Card
                    EnhancedStatsCard()
                    
                    // Activity Timeline Card
                    ActivityTimelineCard()
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
                // Modern Avatar with glassmorphism
                Box(
                    modifier = Modifier.size(80.dp),
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
                            val nickname = remember { "Vsdev" }
                            val avatarLetter = nickname.firstOrNull()?.uppercaseChar()?.toString() ?: "V"
                            Text(
                                text = avatarLetter,
                                style = MaterialTheme.typography.headlineLarge.copy(
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 32.sp
                                ),
                                color = Primary400
                            )
                        }
                    }
                    // Status indicator
                    Box(
                        modifier = Modifier
                            .size(16.dp)
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
                    val nickname = remember { "Vsdev" }
                    Text(
                        text = nickname,
                        style = MaterialTheme.typography.headlineSmall.copy(
                            fontWeight = FontWeight.Bold
                        ),
                        color = DarkOnSurface
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
                        text = "Member since ${remember { "October 2024" }}",
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
private fun EnhancedStatsCard() {
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
                    color = DarkOnSurface
                )
            }
            
            Column(verticalArrangement = Arrangement.spacedBy(Dimensions.spacingMedium)) {
                val stats = remember {
                    listOf(
                        Triple(Icons.Default.Lock, "Files Encrypted", "342"),
                        Triple(Icons.Default.LockOpen, "Files Decrypted", "189"),
                        Triple(Icons.Default.ContentCopy, "Batch Operations", "47"),
                        Triple(Icons.Default.TextFields, "Text Additions", "23")
                    )
                }
                
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
                color = DarkOnSurface
            )
        }
    }
}

@Composable
private fun ActivityTimelineCard() {
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
                    color = DarkOnSurface
                )
            }
            
            Column(verticalArrangement = Arrangement.spacedBy(Dimensions.spacingMedium)) {
                val activities = remember {
                    listOf(
                        Triple("Encrypted document.pdf", "2 minutes ago", Icons.Default.Lock),
                        Triple("Batch copied 5 files", "1 hour ago", Icons.Default.ContentCopy),
                        Triple("Added watermark to image.jpg", "3 hours ago", Icons.Default.TextFields),
                        Triple("Decrypted archive.zip", "Yesterday", Icons.Default.LockOpen)
                    )
                }
                
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
            tint = TerminalAccent,
            modifier = Modifier.size(Dimensions.iconSmall)
        )
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = action,
                style = MaterialTheme.typography.bodyMedium,
                color = DarkOnSurface
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
                    color = DarkOnSurface
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
                    color = DarkOnSurface
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
                        color = DarkOnSurface
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
                val files = remember {
                    listOf(
                        Triple("document.pdf", "2.3 MB", Icons.Default.PictureAsPdf),
                        Triple("image.jpg", "1.8 MB", Icons.Default.Image),
                        Triple("archive.zip", "15.2 MB", Icons.Default.Archive),
                        Triple("data.txt", "0.5 MB", Icons.Default.TextSnippet)
                    )
                }
                
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
                color = DarkOnSurface
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
