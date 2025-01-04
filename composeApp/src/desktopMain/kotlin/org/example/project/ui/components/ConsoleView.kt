package org.example.project.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import org.example.project.utils.ConsoleState
import kotlinx.coroutines.launch
import org.example.project.ui.theme.Dimensions

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

    ElevatedCard(
        modifier = modifier
    ) {
        Column(
            modifier = Modifier.padding(Dimensions.spacingMedium)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.End),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "Console",
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.weight(1f)
                )

                FilledTonalButton(
                    onClick = { showInfo() },
                    modifier = Modifier.height(Dimensions.buttonHeight)
                ) {
                    Text("Info")
                }

                FilledTonalButton(
                    onClick = { ConsoleState.clear() },
                    modifier = Modifier.height(Dimensions.buttonHeight)
                ) {
                    Text("Clear")
                }
            }

            Spacer(modifier = Modifier.height(Dimensions.spacingSmall))

            Surface(
                modifier = Modifier.fillMaxSize(),
                color = MaterialTheme.colorScheme.surfaceVariant,
                shape = MaterialTheme.shapes.small
            ) {
                SelectionContainer {
                    LazyColumn(
                        state = listState,
                        modifier = Modifier.padding(Dimensions.spacingSmall)
                    ) {
                        items(logs) { log ->
                            Text(
                                text = log,
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    fontFamily = FontFamily.Monospace
                                ),
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(vertical = 1.dp)
                            )
                        }
                    }
                }
            }
        }
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
                      [v2.0.9]

OS              MacOS
Language        Kotlin
Updated         January 4, 2025
Author          vsdev. | Vladislav Slugin
Contact         vslugin@vsdev.top

Features       • File encryption/decryption
               • Batch copying with numbering
               • Visible/invisible watermarks
               • Smart file swapping
               • Drag and drop support

File Support   • Images  (JPG, JPEG, PNG)
               • Videos  (MP4)
               • Text    (TXT)

Tech Stack     • Kotlin + Coroutines
               • Compose Multiplatform
               • OpenCV""".trimIndent())
    ConsoleState.log(line)
}