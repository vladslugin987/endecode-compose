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
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Terminal
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.example.project.utils.ConsoleState
import kotlinx.coroutines.launch
import org.example.project.ui.theme.*

@Composable
fun ConsoleView(
    modifier: Modifier = Modifier
) {
    val listState = rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()
    val logs = ConsoleState.logs

    LaunchedEffect(logs.size) {
        if (logs.isNotEmpty()) {
            coroutineScope.launch {
                listState.animateScrollToItem(logs.size - 1)
            }
        }
    }

    GlassCard(
        modifier = modifier,
        borderRadius = Dimensions.radiusMedium
    ) {
        Column(
            modifier = Modifier.padding(Dimensions.cardPadding)
        ) {
            // Terminal header with modern styling
            ModernTerminalHeader(
                onInfoClick = { showInfo() },
                onClearClick = { ConsoleState.clear() }
            )

            Spacer(modifier = Modifier.height(Dimensions.spacingMedium))

            // Terminal content area - theme adaptive
            val isDark = !MaterialTheme.colorScheme.surface.luminance().let { it > 0.5f }
            val terminalBg = if (isDark) DarkTerminalBackground else LightTerminalBackground
            
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .clip(RoundedCornerShape(Dimensions.radiusSmall))
                    .background(
                        brush = Brush.linearGradient(
                            colors = listOf(
                                terminalBg.copy(alpha = 0.95f),
                                terminalBg.copy(alpha = 0.8f),
                                terminalBg.copy(alpha = 0.9f)
                            )
                        )
                    )
            ) {
                SelectionContainer {
                    LazyColumn(
                        state = listState,
                        modifier = Modifier.padding(Dimensions.spacingMedium),
                        verticalArrangement = Arrangement.spacedBy(2.dp)
                    ) {
                        items(logs.withIndex().toList()) { (index, log) ->
                            TerminalLogLine(
                                text = log,
                                lineNumber = index + 1,
                                isLatest = index == logs.size - 1
                            )
                        }
                    }
                }
                
                // Terminal cursor (blinking)
                if (logs.isNotEmpty()) {
                    TerminalCursor(
                        modifier = Modifier
                            .align(Alignment.BottomStart)
                            .padding(Dimensions.spacingMedium)
                    )
                }
            }
        }
    }
}

@Composable
private fun ModernTerminalHeader(
    onInfoClick: () -> Unit,
    onClearClick: () -> Unit
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
                imageVector = Icons.Default.Terminal,
                contentDescription = null,
                tint = TerminalAccent,
                modifier = Modifier.size(Dimensions.iconMedium)
            )
            Text(
                text = "Terminal",
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.SemiBold,
                    color = DarkOnSurface
                )
            )
            StatusIndicator(
                isActive = true,
                activeColor = TerminalSuccess
            )
        }

        Row(
            horizontalArrangement = Arrangement.spacedBy(Dimensions.spacingSmall)
        ) {
            AnimatedGlassButton(
                onClick = onInfoClick,
                isPrimary = false,
                modifier = Modifier.height(Dimensions.smallButtonHeight)
            ) {
                Icon(
                    imageVector = Icons.Default.Info,
                    contentDescription = null,
                    modifier = Modifier.size(Dimensions.iconSmall)
                )
                Spacer(Modifier.width(Dimensions.spacingXSmall))
                Text("Info", fontSize = 12.sp)
            }

            AnimatedGlassButton(
                onClick = onClearClick,
                isPrimary = false,
                modifier = Modifier.height(Dimensions.smallButtonHeight)
            ) {
                Icon(
                    imageVector = Icons.Default.Clear,
                    contentDescription = null,
                    modifier = Modifier.size(Dimensions.iconSmall)
                )
                Spacer(Modifier.width(Dimensions.spacingXSmall))
                Text("Clear", fontSize = 12.sp)
            }
        }
    }
}

@Composable
private fun TerminalLogLine(
    text: String,
    lineNumber: Int,
    isLatest: Boolean
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
            text = String.format("%3d", lineNumber),
            style = MaterialTheme.typography.bodySmall.copy(
                fontFamily = FontFamily.Monospace,
                fontSize = 10.sp
            ),
            color = (if (isDark) DarkTerminalText else LightTerminalText).copy(alpha = 0.6f),
            modifier = Modifier.width(30.dp)
        )
        
        // Log content
        Text(
            text = text,
            style = MaterialTheme.typography.bodyMedium.copy(
                fontFamily = FontFamily.Monospace,
                fontSize = 13.sp
            ),
            color = getLogLineColor(text).copy(alpha = alpha),
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun TerminalCursor(
    modifier: Modifier = Modifier
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
            fontFamily = FontFamily.Monospace
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

fun showInfo() {
    val width = 70
    val line = "=".repeat(width)
    ConsoleState.log(line)
    ConsoleState.log("""
███████╗███╗   ██╗██████╗ ███████╗ ██████╗ ██████╗ ██████╗ ███████╗
██╔════╝████╗  ██║██╔══██╗██╔════╝██╔════╝██╔═══██╗██╔══██╗██╔════╝
█████╗  ██╔██╗ ██║██║  ██║█████╗  ██║     ██║   ██║██║  ██║█████╗  
██╔══╝  ██║╚██╗██║██║  ██║██╔══╝  ██║     ██║   ██║██║  ██║██╔══╝  
███████╗██║ ╚████║██████╔╝███████╗╚██████╗╚██████╔╝██████╔╝███████╗
╚══════╝╚═╝  ╚═══╝╚═════╝ ╚══════╝ ╚═════╝ ╚═════╝ ╚═════╝ ╚══════╝""".trimIndent())
    ConsoleState.log(line)
    ConsoleState.log("""
                    EnDeCode by vsdev.
                      [v2.1.1]

OS              MacOS
Language        Kotlin
Updated         January 7, 2025
Author          vsdev. | Vladislav Slugin
Contact         vslugin@vsdev.top

Features       • File encryption/decryption
               • Batch copying with numbering
               • Visible/invisible watermarks
               • Smart file swapping
               • Drag and drop support

File Support   • Images  (.jpg, .jpeg, .png)
               • Videos  (.mp4, .avi, .mov, .mkv)
               • Text    (.txt)

Tech Stack     • Kotlin + Coroutines
               • Compose Multiplatform
               • OpenCV""".trimIndent())
    ConsoleState.log(line)
}