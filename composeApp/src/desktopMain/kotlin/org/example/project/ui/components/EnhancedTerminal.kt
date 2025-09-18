package org.example.project.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.example.project.utils.ConsoleState
import kotlinx.coroutines.launch
import org.example.project.ui.theme.*

@Composable
fun EnhancedTerminalHeader(
    onInfoClick: () -> Unit,
    onClearClick: () -> Unit,
    onSearchToggle: () -> Unit,
    onFontSizeChange: (androidx.compose.ui.unit.TextUnit) -> Unit,
    onAutoScrollToggle: () -> Unit,
    onCopyAll: () -> Unit,
    onSaveLog: () -> Unit,
    fontSize: androidx.compose.ui.unit.TextUnit,
    autoScroll: Boolean,
    logFilter: String,
    onFilterChange: (String) -> Unit
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(Dimensions.spacingSmall)
    ) {
        // Main header row
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
                    imageVector = Icons.Default.Terminal,
                    contentDescription = "Terminal",
                    tint = TerminalAccent,
                    modifier = Modifier.size(Dimensions.iconMedium)
                )
                Text(
                    text = "Enhanced Terminal",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.SemiBold
                    ),
                    color = MaterialTheme.colorScheme.onSurface
                )
                
                // Status indicator
                StatusIndicator(
                    isActive = true,
                    activeColor = TerminalSuccess
                )
            }
            
            // Font size controls
            Row(
                horizontalArrangement = Arrangement.spacedBy(Dimensions.spacingXSmall),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = { 
                        val newSize = (fontSize.value - 1).coerceAtLeast(10f).sp
                        onFontSizeChange(newSize)
                    },
                    modifier = Modifier.size(Dimensions.iconButtonSize)
                ) {
                    Icon(
                        imageVector = Icons.Default.ZoomOut,
                        contentDescription = "Decrease font size",
                        modifier = Modifier.size(Dimensions.iconSmall),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Text(
                    text = "${fontSize.value.toInt()}sp",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                IconButton(
                    onClick = { 
                        val newSize = (fontSize.value + 1).coerceAtMost(20f).sp
                        onFontSizeChange(newSize)
                    },
                    modifier = Modifier.size(Dimensions.iconButtonSize)
                ) {
                    Icon(
                        imageVector = Icons.Default.ZoomIn,
                        contentDescription = "Increase font size",
                        modifier = Modifier.size(Dimensions.iconSmall),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
        
        // Controls row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Filter dropdown
            Row(
                horizontalArrangement = Arrangement.spacedBy(Dimensions.spacingSmall),
                verticalAlignment = Alignment.CenterVertically
            ) {
                var expanded by remember { mutableStateOf(false) }
                
                Box {
                    AnimatedGlassButton(
                        onClick = { expanded = true },
                        variant = ButtonVariant.SECONDARY,
                        modifier = Modifier.height(Dimensions.smallButtonHeight)
                    ) {
                        Icon(
                            imageVector = Icons.Default.FilterList,
                            contentDescription = null,
                            modifier = Modifier.size(Dimensions.iconSmall)
                        )
                        Spacer(Modifier.width(Dimensions.spacingXSmall))
                        Text(logFilter.uppercase(), fontSize = 11.sp)
                        Icon(
                            imageVector = Icons.Default.ArrowDropDown,
                            contentDescription = null,
                            modifier = Modifier.size(Dimensions.iconSmall)
                        )
                    }
                    
                    DropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false }
                    ) {
                        listOf("all", "info", "warning", "error", "success").forEach { filter ->
                            DropdownMenuItem(
                                text = { Text(filter.uppercase()) },
                                onClick = {
                                    onFilterChange(filter)
                                    expanded = false
                                }
                            )
                        }
                    }
                }
                
                // Auto-scroll toggle
                IconButton(
                    onClick = onAutoScrollToggle,
                    modifier = Modifier.size(Dimensions.iconButtonSize)
                ) {
                    Icon(
                        imageVector = if (autoScroll) Icons.Default.VerticalAlignBottom else Icons.Default.PauseCircle,
                        contentDescription = if (autoScroll) "Auto-scroll enabled" else "Auto-scroll disabled",
                        modifier = Modifier.size(Dimensions.iconSmall),
                        tint = if (autoScroll) TerminalSuccess else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            
            // Action buttons
            Row(
                horizontalArrangement = Arrangement.spacedBy(Dimensions.spacingXSmall)
            ) {
                IconButton(
                    onClick = onSearchToggle,
                    modifier = Modifier.size(Dimensions.iconButtonSize)
                ) {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = "Toggle search",
                        modifier = Modifier.size(Dimensions.iconSmall),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                
                IconButton(
                    onClick = onCopyAll,
                    modifier = Modifier.size(Dimensions.iconButtonSize)
                ) {
                    Icon(
                        imageVector = Icons.Default.ContentCopy,
                        contentDescription = "Copy all logs",
                        modifier = Modifier.size(Dimensions.iconSmall),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                
                IconButton(
                    onClick = onSaveLog,
                    modifier = Modifier.size(Dimensions.iconButtonSize)
                ) {
                    Icon(
                        imageVector = Icons.Default.Save,
                        contentDescription = "Save logs to file",
                        modifier = Modifier.size(Dimensions.iconSmall),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                
                AnimatedGlassButton(
                    onClick = onInfoClick,
                    variant = ButtonVariant.SECONDARY,
                    modifier = Modifier.height(Dimensions.smallButtonHeight)
                ) {
                    Icon(
                        imageVector = Icons.Default.Info,
                        contentDescription = "Show info",
                        modifier = Modifier.size(Dimensions.iconSmall)
                    )
                    Spacer(Modifier.width(Dimensions.spacingXSmall))
                    Text("Info", fontSize = 11.sp)
                }

                AnimatedGlassButton(
                    onClick = onClearClick,
                    variant = ButtonVariant.SECONDARY,
                    modifier = Modifier.height(Dimensions.smallButtonHeight)
                ) {
                    Icon(
                        imageVector = Icons.Default.Clear,
                        contentDescription = "Clear logs",
                        modifier = Modifier.size(Dimensions.iconSmall)
                    )
                    Spacer(Modifier.width(Dimensions.spacingXSmall))
                    Text("Clear", fontSize = 11.sp)
                }
            }
        }
    }
}

@Composable
fun EnhancedTerminalLogLine(
    text: String,
    lineNumber: Int,
    isLatest: Boolean,
    fontSize: androidx.compose.ui.unit.TextUnit,
    searchQuery: String = ""
) {
    val alpha by animateFloatAsState(
        targetValue = if (isLatest) 1f else 0.8f,
        animationSpec = tween(durationMillis = Dimensions.animationMedium),
        label = "log_line_alpha"
    )
    
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(Dimensions.spacingSmall)
    ) {
        // Line number
        val isDark = !MaterialTheme.colorScheme.surface.luminance().let { it > 0.5f }
        Text(
            text = String.format("%4d", lineNumber),
            style = MaterialTheme.typography.bodySmall.copy(
                fontFamily = FontFamily.Monospace,
                fontSize = (fontSize.value - 2).sp
            ),
            color = (if (isDark) DarkTerminalText else LightTerminalText).copy(alpha = 0.6f),
            modifier = Modifier.width(40.dp)
        )
        
        // Log content with search highlighting
        if (searchQuery.isNotEmpty() && text.contains(searchQuery, ignoreCase = true)) {
            val annotatedString = buildAnnotatedString {
                val parts = text.split(searchQuery, ignoreCase = true)
                for (i in parts.indices) {
                    append(parts[i])
                    if (i < parts.size - 1) {
                        withStyle(
                            style = SpanStyle(
                                background = TerminalWarning.copy(alpha = 0.3f),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        ) {
                            append(searchQuery)
                        }
                    }
                }
            }
            
            Text(
                text = annotatedString,
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontFamily = FontFamily.Monospace,
                    fontSize = fontSize,
                    lineHeight = (fontSize.value * 1.4).sp
                ),
                color = getLogLineColor(text).copy(alpha = alpha),
                modifier = Modifier.weight(1f)
            )
        } else {
            Text(
                text = text,
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontFamily = FontFamily.Monospace,
                    fontSize = fontSize,
                    lineHeight = (fontSize.value * 1.4).sp
                ),
                color = getLogLineColor(text).copy(alpha = alpha),
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun TerminalCursor(
    modifier: Modifier = Modifier,
    fontSize: androidx.compose.ui.unit.TextUnit = 14.sp
) {
    val infiniteTransition = rememberInfiniteTransition(label = "cursor_blink")
    val alpha by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1000),
            repeatMode = RepeatMode.Reverse
        ),
        label = "cursor_alpha"
    )
    
    Text(
        text = "█",
        style = MaterialTheme.typography.bodyMedium.copy(
            fontFamily = FontFamily.Monospace,
            fontSize = fontSize
        ),
        color = TerminalAccent.copy(alpha = alpha),
        modifier = modifier
    )
}

@Composable
private fun getLogLineColor(text: String): androidx.compose.ui.graphics.Color {
    val isDark = !MaterialTheme.colorScheme.surface.luminance().let { it > 0.5f }
    
    return when {
        text.contains("error", ignoreCase = true) -> if (isDark) DarkTerminalError else LightTerminalError
        text.contains("warning", ignoreCase = true) -> if (isDark) DarkTerminalWarning else LightTerminalWarning
        text.contains("success", ignoreCase = true) -> if (isDark) DarkTerminalSuccess else LightTerminalSuccess
        text.contains("info", ignoreCase = true) -> if (isDark) DarkTerminalAccent else LightTerminalAccent
        text.startsWith("=") -> if (isDark) DarkTerminalAccent else LightTerminalAccent
        text.contains("█") -> if (isDark) DarkTerminalAccent else LightTerminalAccent // ASCII art
        else -> if (isDark) DarkTerminalText else LightTerminalText
    }
}
