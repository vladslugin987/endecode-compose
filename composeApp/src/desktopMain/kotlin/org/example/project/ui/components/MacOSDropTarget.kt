package org.example.project.ui.components

import java.awt.datatransfer.DataFlavor
import java.awt.dnd.*
import java.io.File
import java.io.Reader
import java.net.URI
import java.nio.file.Paths
import org.example.project.utils.ConsoleState

class MacOSDropTarget(
    private val onFileDropped: (File) -> Unit,
    private val onDragStateChanged: (Boolean) -> Unit
) : DropTargetAdapter() {

    override fun dragEnter(event: DropTargetDragEvent) {
        ConsoleState.log("MacOS drag entered")
        onDragStateChanged(true)
        event.acceptDrag(DnDConstants.ACTION_COPY)
    }

    override fun dragExit(event: DropTargetEvent) {
        ConsoleState.log("MacOS drag exited")
        onDragStateChanged(false)
    }

    override fun dragOver(event: DropTargetDragEvent) {
        event.acceptDrag(DnDConstants.ACTION_COPY)
    }

    override fun drop(event: DropTargetDropEvent) {
        ConsoleState.log("MacOS drop occurred")
        try {
            event.acceptDrop(DnDConstants.ACTION_COPY)
            val transferable = event.transferable
            var fileFound = false

            if (transferable.isDataFlavorSupported(DataFlavor.javaFileListFlavor)) {
                val fileList = transferable.getTransferData(DataFlavor.javaFileListFlavor) as List<*>
                val file = fileList.firstOrNull() as? File
                if (file?.isDirectory == true) {
                    onFileDropped(file)
                    ConsoleState.log("MacOS file dropped via javaFileListFlavor: ${file.absolutePath}")
                    fileFound = true
                }
            }

            if (!fileFound) {
                for (flavor in transferable.transferDataFlavors) {
                    if (flavor.mimeType.contains("text/uri-list")) {
                        val reader = transferable.getTransferData(flavor) as? Reader
                        val uris = reader?.readLines() ?: emptyList()

                        for (uriString in uris) {
                            try {
                                val uri = URI(uriString.trim())
                                val file = Paths.get(uri).toFile()
                                if (file.isDirectory) {
                                    onFileDropped(file)
                                    ConsoleState.log("MacOS file dropped via uri-list: ${file.absolutePath}")
                                    fileFound = true
                                    break
                                }
                            } catch (e: Exception) {
                                ConsoleState.log("MacOS error parsing URI: ${e.message}")
                            }
                        }
                    }
                }
            }

            event.dropComplete(fileFound)
        } catch (e: Exception) {
            ConsoleState.log("MacOS error during drop: ${e.message}")
            event.dropComplete(false)
        } finally {
            onDragStateChanged(false)
        }
    }
}