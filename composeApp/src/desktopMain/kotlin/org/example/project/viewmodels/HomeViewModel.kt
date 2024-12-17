package org.example.project.viewmodels

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import kotlinx.coroutines.*
import kotlinx.coroutines.swing.Swing
import mu.KotlinLogging
import java.io.File
import org.example.project.utils.*

private val logger = KotlinLogging.logger {}

class HomeViewModel {
    // UI States
    private var _selectedPath by mutableStateOf<String?>(null)
    val selectedPath: String? get() = _selectedPath

    private var _nameToInject by mutableStateOf("")
    val nameToInject: String get() = _nameToInject

    private var _autoClearConsole by mutableStateOf(false)
    val autoClearConsole: Boolean get() = _autoClearConsole

    var isProcessing by mutableStateOf(false)
        private set
    var progress by mutableStateOf(0f)
        private set

    private val scope = CoroutineScope(Dispatchers.Swing)
    private var currentJob: Job? = null

    // Update functions
    fun updateSelectedPath(path: String?) {
        _selectedPath = path
        if (_autoClearConsole) {
            ConsoleState.clear()
        }
        ConsoleState.log("***** Folder successfully selected! *****")
        ConsoleState.log("")
    }

    fun updateNameToInject(name: String) {
        _nameToInject = name
    }

    fun updateAutoClearConsole(value: Boolean) {
        _autoClearConsole = value
    }

    // Main functions
    fun encrypt() {
        if (!validateInput()) return

        launchOperation {
            try {
                val folder = File(selectedPath!!)
                val files = FileUtils.getSupportedFiles(folder)
                    .sortedBy { it.name }

                ConsoleState.log("Starting encryption process...")
                ConsoleState.log("Total files to process: ${files.size}")

                files.forEachIndexed { index, file ->
                    withContext(Dispatchers.IO) {
                        try {
                            val watermark = EncodingUtils.addWatermark(nameToInject)
                            EncodingUtils.processFile(file, watermark)
                        } catch (e: Exception) {
                            ConsoleState.log("Error processing ${file.name}: ${e.message}")
                        }
                    }
                    withContext(Dispatchers.Main) {
                        progress = (index + 1).toFloat() / files.size
                    }
                }

                ConsoleState.log("Encryption completed successfully")
            } catch (e: Exception) {
                logger.error(e) { "Error during encryption" }
                ConsoleState.log("Error during encryption: ${e.message}")
            }
        }
    }

    fun decrypt() {
        if (!validatePath()) return

        launchOperation {
            try {
                val folder = File(selectedPath!!)
                val files = FileUtils.getSupportedFiles(folder)
                    .sortedBy { it.name }

                ConsoleState.log("Starting decryption process...")
                ConsoleState.log("Total files to process: ${files.size}")
                ConsoleState.log("=".repeat(40))
                ConsoleState.log("Full Decryption:")

                files.forEachIndexed { index, file ->
                    withContext(Dispatchers.IO) {
                        try {
                            val watermark = WatermarkUtils.extractWatermarkText(file)
                            if (watermark != null) {
                                val decodedText = EncodingUtils.decodeText(watermark)
                                ConsoleState.log("${file.name} [new format]: $decodedText")
                                logger.debug { "File: ${file.name}, Raw watermark: $watermark, Decoded: $decodedText" }
                            } else {
                                val content = file.readText(Charsets.UTF_8)
                                if (content.contains("<<=") || content.contains("=>>")) {
                                    ConsoleState.log("${file.name}: Partial watermark found, might be corrupted")
                                    logger.warn { "File ${file.name} contains partial watermark: ${content.takeLast(100)}" }
                                } else {
                                    ConsoleState.log("${file.name}: No watermark found")
                                }
                            }
                        } catch (e: Exception) {
                            ConsoleState.log("Error decoding ${file.name}: ${e.message}")
                            logger.error(e) { "Error processing file ${file.name}" }
                        }
                    }
                    withContext(Dispatchers.Swing) {
                        progress = (index + 1).toFloat() / files.size
                    }
                }

                ConsoleState.log("=".repeat(40))
                ConsoleState.log("Decryption completed successfully")
                ConsoleState.log("")
            } catch (e: Exception) {
                logger.error(e) { "Error during decryption" }
                ConsoleState.log("Error during decryption: ${e.message}")
            }
        }
    }

    fun performBatchCopy(
        numCopies: Int,
        baseText: String,
        addSwap: Boolean,
        addWatermark: Boolean,
        createZip: Boolean,
        watermarkText: String?,
        photoNumber: Int?
    ) {
        if (!validatePath()) return

        launchOperation {
            try {
                val sourceFolder = File(selectedPath!!)
                ConsoleState.log("Starting batch copy process...")
                ConsoleState.log("Number of copies: $numCopies")
                ConsoleState.log("Base text: $baseText")
                ConsoleState.log("Additional swap: $addSwap")
                ConsoleState.log("Add watermark: $addWatermark")
                ConsoleState.log("Create ZIP: $createZip")

                BatchUtils.performBatchCopyAndEncode(
                    sourceFolder = sourceFolder,
                    numCopies = numCopies,
                    baseText = baseText,
                    addSwap = addSwap,
                    addWatermark = addWatermark,
                    createZip = createZip,
                    watermarkText = watermarkText,
                    photoNumber = photoNumber
                ) { progress ->
                    this.progress = progress
                }

                ConsoleState.log("Batch copy completed successfully")
            } catch (e: Exception) {
                logger.error(e) { "Error during batch copy" }
                ConsoleState.log("Error during batch copy: ${e.message}")
            }
        }
    }

    fun addTextToPhoto(text: String, photoNumber: Int) {
        if (!validatePath()) return

        launchOperation {
            try {
                val folder = File(selectedPath!!)
                ConsoleState.log("Adding text to photo...")
                ConsoleState.log("Text: $text")
                ConsoleState.log("Photo number: $photoNumber")

                var found = false
                FileUtils.getSupportedFiles(folder)
                    .filter { FileUtils.isImageFile(it) }
                    .forEach { file ->
                        if (file.name.contains(photoNumber.toString().padStart(3, '0'))) {
                            ImageUtils.addTextToImage(file, text)
                            found = true
                            ConsoleState.log("Successfully added text to ${file.name}")
                        }
                    }

                if (!found) {
                    ConsoleState.log("No photo with number $photoNumber found")
                }
            } catch (e: Exception) {
                logger.error(e) { "Error adding text to photo" }
                ConsoleState.log("Error adding text to photo: ${e.message}")
            }
        }
    }

    fun removeWatermarks() {
        if (!validatePath()) return

        launchOperation {
            try {
                val folder = File(selectedPath!!)
                ConsoleState.log("Starting watermark removal process...")

                WatermarkUtils.removeWatermarks(folder) { processed ->
                    progress = processed.toFloat()
                }

                ConsoleState.log("Watermark removal completed successfully")
            } catch (e: Exception) {
                logger.error(e) { "Error removing watermarks" }
                ConsoleState.log("Error removing watermarks: ${e.message}")
            }
        }
    }

    // Helper functions
    private fun validatePath(): Boolean {
        if (selectedPath == null) {
            ConsoleState.log("Error: No folder selected")
            return false
        }
        return true
    }

    private fun validateInput(): Boolean {
        if (!validatePath()) return false

        if (nameToInject.isBlank()) {
            ConsoleState.log("Error: Name to inject is empty")
            return false
        }
        return true
    }

    private fun launchOperation(block: suspend () -> Unit) {
        currentJob?.cancel()
        currentJob = scope.launch {
            try {
                isProcessing = true
                progress = 0f
                if (_autoClearConsole) ConsoleState.clear()
                block()
            } finally {
                isProcessing = false
                progress = 0f
            }
        }
    }
}