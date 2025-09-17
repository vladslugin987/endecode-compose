package org.example.project.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.awt.ComposeWindow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInWindow
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import java.awt.datatransfer.DataFlavor
import java.io.File
import java.io.Reader
import java.net.URI
import java.nio.file.Paths
import javax.swing.JFileChooser
import javax.swing.JWindow
import javax.swing.SwingUtilities
import org.example.project.utils.ConsoleState
import org.example.project.ui.theme.Dimensions
import java.awt.dnd.*
import java.awt.Point
import javax.swing.JPanel
import java.awt.Dimension
import java.awt.event.*
import java.awt.Color as AWTColor

@Composable
fun FileSelector(
    selectedPath: String?,
    onPathSelected: (String) -> Unit,
    window: ComposeWindow,
    modifier: Modifier = Modifier
) {
    var isDragging by remember { mutableStateOf(false) }
    var dropBounds by remember { mutableStateOf<BoxLayoutInfo?>(null) }

    DisposableEffect(window) {
        val dndWindow = JWindow(window).apply {
            background = AWTColor(0, 0, 0, 0)
            isVisible = true
        }

        val panel = JPanel().apply {
            background = AWTColor(0, 0, 0, 0)
            isOpaque = false
        }
        dndWindow.contentPane.add(panel)

        val dropTarget = DropTarget(panel, object : DropTargetAdapter() {
            init {
                // ConsoleState.log("DnD initialized")
            }

            override fun dragEnter(dtde: DropTargetDragEvent) {
                // ConsoleState.log("Drag entered")
                isDragging = true
                dtde.acceptDrag(DnDConstants.ACTION_COPY)
            }

            override fun dragOver(dtde: DropTargetDragEvent) {
                // On Windows, accept must be called continuously during dragOver
                isDragging = true
                dtde.acceptDrag(DnDConstants.ACTION_COPY)
            }

            override fun dropActionChanged(dtde: DropTargetDragEvent) {
                dtde.acceptDrag(DnDConstants.ACTION_COPY)
            }

            override fun dragExit(dte: DropTargetEvent) {
                // ConsoleState.log("Drag exited")
                isDragging = false
            }

            override fun drop(dtde: DropTargetDropEvent) {
                // ConsoleState.log("Drop occurred")
                var handled = false
                try {
                    dtde.acceptDrop(DnDConstants.ACTION_COPY)
                    val transferable = dtde.transferable

                    if (transferable.isDataFlavorSupported(DataFlavor.javaFileListFlavor)) {
                        val list = transferable.getTransferData(DataFlavor.javaFileListFlavor) as List<*>
                        val file = list.firstOrNull() as? File

                        if (file?.isDirectory == true) {
                            ConsoleState.log("Directory dropped: ${file.absolutePath}")
                            SwingUtilities.invokeLater {
                                onPathSelected(file.absolutePath)
                            }
                            handled = true
                        }
                    }

                    if (!handled) {
                        // Fallback for macOS: handle text/uri-list flavor
                        for (flavor in transferable.transferDataFlavors) {
                            if (flavor.mimeType.contains("text/uri-list")) {
                                val reader = transferable.getTransferData(flavor) as? Reader
                                val uris = reader?.buffered()?.use { it.readLines() } ?: emptyList()
                                for (uriString in uris) {
                                    try {
                                        val uri = URI(uriString.trim())
                                        val f = Paths.get(uri).toFile()
                                        if (f.isDirectory) {
                                            ConsoleState.log("Directory dropped (uri-list): ${f.absolutePath}")
                                            SwingUtilities.invokeLater {
                                                onPathSelected(f.absolutePath)
                                            }
                                            handled = true
                                            break
                                        }
                                    } catch (_: Exception) {
                                        // ignore malformed URIs
                                    }
                                }
                                if (handled) break
                            }
                        }
                    }
                } catch (e: Exception) {
                    ConsoleState.log("Drop error: ${e.message}")
                } finally {
                    dtde.dropComplete(handled)
                    isDragging = false
                }
            }
        })

        val componentListener = object : ComponentAdapter() {
            override fun componentMoved(e: ComponentEvent) {
                updateDndWindowPosition(window, dndWindow, dropBounds)
            }

            override fun componentResized(e: ComponentEvent) {
                updateDndWindowPosition(window, dndWindow, dropBounds)
            }
        }
        window.addComponentListener(componentListener)

        val windowListener = object : WindowAdapter() {
            override fun windowIconified(e: WindowEvent) {
                dndWindow.isVisible = false
            }

            override fun windowDeiconified(e: WindowEvent) {
                dndWindow.isVisible = true
                updateDndWindowPosition(window, dndWindow, dropBounds)
            }
        }
        window.addWindowListener(windowListener)

        onDispose {
            window.removeComponentListener(componentListener)
            window.removeWindowListener(windowListener)
            dropTarget.removeNotify()
            dndWindow.dispose()
        }
    }

    Column(modifier = modifier.fillMaxWidth()) {
        Button(
            onClick = {
                val fileChooser = JFileChooser().apply {
                    fileSelectionMode = JFileChooser.DIRECTORIES_ONLY
                    dialogTitle = "Select Folder"
                }
                val result = fileChooser.showOpenDialog(window)
                if (result == JFileChooser.APPROVE_OPTION) {
                    onPathSelected(fileChooser.selectedFile.absolutePath)
                }
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(selectedPath ?: "Choose folder with files")
        }

        Spacer(modifier = Modifier.height(Dimensions.spacingMedium))

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(Dimensions.dropZoneHeight)
                .border(
                    width = if (isDragging) 3.dp else 2.dp,
                    color = if (isDragging)
                        MaterialTheme.colorScheme.primary
                    else
                        MaterialTheme.colorScheme.outline,
                    shape = MaterialTheme.shapes.small
                )
                .background(
                    if (isDragging)
                        MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
                    else
                        MaterialTheme.colorScheme.surfaceVariant
                )
                .onGloballyPositioned { coordinates ->
                    val location = coordinates.positionInWindow()
                    dropBounds = BoxLayoutInfo(
                        x = location.x,
                        y = location.y,
                        width = coordinates.size.width,
                        height = coordinates.size.height
                    )
                    updateDndWindowPosition(window, window.ownedWindows.firstOrNull { it is JWindow } as? JWindow, dropBounds)
                },
            contentAlignment = Alignment.Center
        ) {
            Text(
                if (isDragging) "Release to drop folder" else "Drop folder here",
                style = MaterialTheme.typography.bodyLarge,
                color = if (isDragging)
                    MaterialTheme.colorScheme.primary
                else
                    MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

private data class BoxLayoutInfo(
    val x: Float,
    val y: Float,
    val width: Int,
    val height: Int
)

private fun updateDndWindowPosition(parentWindow: ComposeWindow, dndWindow: JWindow?, bounds: BoxLayoutInfo?) {
    if (dndWindow == null || bounds == null) return

    SwingUtilities.invokeLater {
        val windowLocation = parentWindow.location

        dndWindow.location = Point(
            windowLocation.x + bounds.x.toInt(),
            windowLocation.y + bounds.y.toInt()
        )
        dndWindow.size = Dimension(bounds.width, bounds.height)
    }
}