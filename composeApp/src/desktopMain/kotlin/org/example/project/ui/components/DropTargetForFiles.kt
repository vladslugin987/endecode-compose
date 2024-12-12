// DropTargetForFiles.kt
package org.example.project.ui.components

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.awt.ComposePanel
import java.awt.datatransfer.DataFlavor
import java.awt.dnd.DnDConstants
import java.awt.dnd.DropTarget
import java.awt.dnd.DropTargetAdapter
import java.awt.dnd.DropTargetDropEvent
import java.io.File

@Composable
fun rememberDropTargetForFiles(onFileDropped: (File) -> Unit): DropTarget {
    return remember {
        val adapter = object : DropTargetAdapter() {
            override fun drop(event: DropTargetDropEvent) {
                try {
                    event.acceptDrop(DnDConstants.ACTION_COPY)
                    val transferable = event.transferable

                    if (transferable.isDataFlavorSupported(DataFlavor.javaFileListFlavor)) {
                        @Suppress("UNCHECKED_CAST")
                        val files = transferable.getTransferData(DataFlavor.javaFileListFlavor) as List<File>
                        files.firstOrNull()?.let { file ->
                            onFileDropped(file)
                        }
                    }
                    event.dropComplete(true)
                } catch (e: Exception) {
                    event.dropComplete(false)
                    e.printStackTrace()
                }
            }
        }

        DropTarget(ComposePanel(), adapter)
    }
}