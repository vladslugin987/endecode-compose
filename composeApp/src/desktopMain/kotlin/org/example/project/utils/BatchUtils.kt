package org.example.project.utils

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import mu.KotlinLogging
import java.io.File
import java.io.FileOutputStream
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream
import kotlin.io.path.Path
import kotlin.io.path.name

private val logger = KotlinLogging.logger {}

object BatchUtils {
    /**
     * Main function for batch copying and encoding.
     * Creates copies of 'sourceFolder', processes them, and optionally packs each into a ZIP.
     */
    suspend fun performBatchCopyAndEncode(
        sourceFolder: File,
        numCopies: Int,
        baseText: String,
        addSwap: Boolean,
        addWatermark: Boolean,
        createZip: Boolean,
        watermarkText: String? = null,
        photoNumber: Int? = null,
        progress: (Float) -> Unit
    ) = withContext(Dispatchers.IO) {
        try {
            // 1) Create main folder for all copies, e.g. "Test1-Bundle-Copies"
            val copiesFolder = File(sourceFolder.parent, "${sourceFolder.name}-Copies")
            if (!copiesFolder.exists()) {
                copiesFolder.mkdir()
            }

            val startNumber = extractStartNumber(baseText)
            val baseTextWithoutNumber = baseText.replace("\\d+$".toRegex(), "").trim()

            // Calculate total operations (simplified for clarity)
            val totalOperations = numCopies * (
                    1 + // copying
                            1 + // encoding
                            (if (addWatermark) 1 else 0) +
                            (if (addSwap) 1 else 0) +
                            (if (createZip) 1 else 0)
                    )
            var completedOperations = 0f

            // Will store (processedFolder, orderNumber) for ZIP creation
            val foldersToProcess = mutableListOf<Pair<File, String>>()

            // 2) First pass: create subfolders (001, 002, 003, ...) and copy sourceFolder there
            for (i in 0 until numCopies) {
                val orderNumber = (startNumber + i).toString().padStart(3, '0')
                val orderFolder = File(copiesFolder, orderNumber)
                orderFolder.mkdir()

                // destinationFolder is ".../Test1-Bundle-Copies/001/Test1-Bundle"
                val destinationFolder = File(orderFolder, sourceFolder.name)

                // Copy original
                FileUtils.copyDirectory(sourceFolder, destinationFolder)
                completedOperations++
                progress(completedOperations / totalOperations)

                // Process files (invisible watermark, rename, etc.)
                processFiles(destinationFolder, baseTextWithoutNumber, orderNumber)
                completedOperations++
                progress(completedOperations / totalOperations)

                // Visible watermark if needed
                if (addWatermark) {
                    val actualPhotoNumber = photoNumber ?: orderNumber.toInt()
                    val actualWatermarkText = watermarkText ?: orderNumber
                    addVisibleWatermarkToPhoto(destinationFolder, actualWatermarkText, actualPhotoNumber)
                    completedOperations++
                    progress(completedOperations / totalOperations)
                }

                // Swap if needed
                if (addSwap) {
                    performSwap(destinationFolder, orderNumber)
                    completedOperations++
                    progress(completedOperations / totalOperations)
                }

                // Save destination for ZIP stage
                foldersToProcess.add(destinationFolder to orderNumber)

                ConsoleState.log("Processed folder: $orderNumber")
            }

            // 3) Second pass: create ZIP archives and remove processed folders
            if (createZip) {
                foldersToProcess.forEach { (folderToZip, orderNumber) ->
                    // Creates ".../Test1-Bundle-Copies/001/Test1-Bundle.zip"
                    // and removes the "Test1-Bundle" subfolder afterwards
                    createNoCompressionZip(folderToZip)

                    // Now delete the folder (so only the zip remains in "001")
                    folderToZip.deleteRecursively()

                    completedOperations++
                    progress(completedOperations / totalOperations)
                }
            }

            ConsoleState.log("Batch processing completed successfully")
        } catch (e: Exception) {
            logger.error(e) { "Error during batch processing" }
            ConsoleState.log("Error during batch processing: ${e.message}")
            throw e
        }
    }

    /**
     * Process files based on their type (images or videos)
     */
    private suspend fun processFiles(folder: File, baseText: String, orderNumber: String) {
        val files = FileUtils.getSupportedFiles(folder)
        val encodedText = "$baseText $orderNumber"
        val encodedWatermark = EncodingUtils.encodeText(encodedText)
        val watermark = EncodingUtils.addWatermark(encodedText)

        files.forEach { file ->
            when {
                FileUtils.isVideoFile(file) -> {
                    // Only add invisible watermark to video files
                    WatermarkUtils.addWatermark(file, encodedWatermark)
                    ConsoleState.log("Added watermark to video: ${file.name}")
                }
                else -> {
                    // Process other files normally
                    EncodingUtils.processFile(file, watermark)
                }
            }
        }
    }

    /**
     * Determines if number should be used for swapping based on base folder number
     */
//    private fun shouldProcessNumberForSwap(fileNumber: Int, baseNumber: Int): Boolean {
//        val baseSeries = (baseNumber / 100) * 100
//        return fileNumber in (baseSeries + 1)..(baseSeries + 99)
//    }

    /**
     * Adds visible watermark to photo with specified number
     */
    private suspend fun addVisibleWatermarkToPhoto(
        folder: File,
        watermarkText: String,
        photoNumber: Int
    ) = withContext(Dispatchers.IO) {
        try {
            var found = false
            FileUtils.getSupportedFiles(folder)
                .filter { FileUtils.isImageFile(it) }
                .forEach { file ->
                    val fileNumber = extractFileNumber(file.name)
                    if (fileNumber == photoNumber) {
                        ImageUtils.addTextToImage(file, watermarkText)
                        found = true
                        return@forEach
                    }
                }

            if (!found) {
                ConsoleState.log("No photo with number $photoNumber found in ${folder.name}")
            }
        } catch (e: Exception) {
            logger.error(e) { "Error adding visible watermark in ${folder.name}" }
            ConsoleState.log("Error adding visible watermark: ${e.message}")
        }
    }

    /**
     * Performs swap operation for files in folder
     */
    private suspend fun performSwap(folder: File, orderNumber: String) = withContext(Dispatchers.IO) {
        try {
            val baseNumber = orderNumber.toInt()
            val swapNumber = baseNumber + 10

            ConsoleState.log("Starting swap operation for number $baseNumber with $swapNumber ...")

            // Take all images in a folder
            val allImages = FileUtils.getSupportedFiles(folder)
                .filter { FileUtils.isImageFile(it) }

            // Find file with the baseNumber
            val fileA = allImages.firstOrNull { extractFileNumber(it.name) == baseNumber }
            // Find file with the number: (baseNumber + 10)
            val fileB = allImages.firstOrNull { extractFileNumber(it.name) == swapNumber }

            // If there are no - stop swapping
            if (fileA == null || fileB == null) {
                ConsoleState.log("No matching pair found for swapping in folder ${folder.name} (need $baseNumber and $swapNumber)")
                return@withContext
            }

            // If found - rename
            swapFiles(fileA, fileB)
            ConsoleState.log("Finished swap operation for folder ${folder.name}")

        } catch (e: Exception) {
            logger.error(e) { "Error performing swap in ${folder.name}" }
            ConsoleState.log("Error during swap operation: ${e.message}")
        }
    }

    /**
     * Rename fileA -> temp,
     * fileB -> fileA,
     * temp -> fileB
     */
    private fun swapFiles(fileA: File, fileB: File) {
        try {
            ConsoleState.log("Swapping files:")
            ConsoleState.log("  - ${fileA.name}")
            ConsoleState.log("  - ${fileB.name}")

            val tempFile = File(fileA.parent, "temp_${System.currentTimeMillis()}_${fileA.name}")

            val renameAtoTemp = fileA.renameTo(tempFile)
            val renameBtoA = fileB.renameTo(fileA)
            val renameTempToB = tempFile.renameTo(fileB)

            if (renameAtoTemp && renameBtoA && renameTempToB) {
                ConsoleState.log("Successfully swapped ${fileA.name} <--> ${fileB.name}")
            } else {
                ConsoleState.log("Failed to swap files: rename operations returned false")
            }
        } catch (e: Exception) {
            ConsoleState.log("Error swapping files ${fileA.name} <--> ${fileB.name}: ${e.message}")
        }
    }

    /**
     * Creates ZIP archive without compression
     * Example:
     *      If folderToZip = ".../001/Test1-Bundle",
     *      the result is ".../001/Test1-Bundle.zip".
     */
    private fun createNoCompressionZip(folderToZip: File) {
        val zipFileName = "${folderToZip.name}.zip"  // "Test1-Bundle.zip"
        val zipFile = File(folderToZip.parentFile, zipFileName)

        ZipOutputStream(FileOutputStream(zipFile)).use { zipOut ->
            zipOut.setLevel(ZipOutputStream.STORED)

            val filesToZip = folderToZip.walkTopDown()
                .filter { file ->
                    !file.name.startsWith("__MACOSX") &&
                            !file.name.startsWith(".") &&
                            !file.name.endsWith(".DS_Store")
                }
                .toList()

            for (file in filesToZip) {
                val entryPath = file.relativeTo(folderToZip).path
                if (file.isFile) {
                    addFileToZip(file, entryPath, zipOut)
                } else {
                    addDirectoryToZip(entryPath, zipOut)
                }
            }
        }

        ConsoleState.log("Created ZIP archive: ${zipFile.absolutePath}")
    }

    /**
     * Adds file to ZIP archive
     */
    private fun addFileToZip(file: File, entryPath: String, zipOut: ZipOutputStream) {
        val fileBytes = file.readBytes()
        val crc = java.util.zip.CRC32().apply { update(fileBytes) }

        ZipEntry(entryPath).apply {
            method = ZipEntry.STORED
            size = fileBytes.size.toLong()
            compressedSize = size
            this.crc = crc.value
        }.also { entry ->
            zipOut.putNextEntry(entry)
            zipOut.write(fileBytes)
            zipOut.closeEntry()
        }
    }

    /**
     * Adds directory entry to ZIP archive
     */
    private fun addDirectoryToZip(entryPath: String, zipOut: ZipOutputStream) {
        ZipEntry("$entryPath/").apply {
            method = ZipEntry.STORED
            size = 0
            compressedSize = 0
            crc = 0
        }.also { entry ->
            zipOut.putNextEntry(entry)
            zipOut.closeEntry()
        }
    }

    /**
     * Finds pairs of files to swap based on numbers in filenames
     */
//    private fun findSwapPairs(files: List<File>, currentNum: String, swapNum: String): List<Pair<File, File>> {
//        val baseNumber = currentNum.toInt()
//
//        return files.mapNotNull { file ->
//            val numberPattern = """.*?(\d+).*""".toRegex()
//            val numberMatch = numberPattern.find(file.name) ?: return@mapNotNull null
//
//            val foundNumber = numberMatch.groupValues[1]
//            val fileNumber = foundNumber.toIntOrNull() ?: return@mapNotNull null
//
//            // Skip numbers that shouldn't be processed
//            if (!shouldProcessNumberForSwap(fileNumber, baseNumber)) {
//                return@mapNotNull null
//            }
//
//            val swapFileName = file.name.replace(
//                foundNumber,
//                swapNum.padStart(foundNumber.length, '0')
//            )
//            val swapFile = File(file.parent, swapFileName)
//
//            if (swapFile.exists()) {
//                ConsoleState.log("Found pair for swapping:")
//                ConsoleState.log("  - ${file.name}")
//                ConsoleState.log("  - ${swapFile.name}")
//                Pair(file, swapFile)
//            } else {
//                ConsoleState.log("No matching swap file found for: ${file.name}")
//                null
//            }
//        }
//    }

    /**
     * Performs actual swap of file pairs
     */
//    private fun swapFilePairs(pairs: List<Pair<File, File>>) {
//        pairs.forEach { (file1, file2) ->
//            try {
//                ConsoleState.log("Swapping files:")
//                ConsoleState.log("  - ${file1.name}")
//                ConsoleState.log("  - ${file2.name}")
//
//                val tempFile = File(
//                    file1.parent,
//                    "temp_${System.currentTimeMillis()}_${file1.name}"
//                )
//
//                if (file1.renameTo(tempFile) &&
//                    file2.renameTo(file1) &&
//                    tempFile.renameTo(file2)
//                ) {
//                    ConsoleState.log("Successfully swapped files")
//                } else {
//                    ConsoleState.log("Failed to swap files")
//                }
//            } catch (e: Exception) {
//                ConsoleState.log("Error swapping files: ${e.message}")
//            }
//        }
//    }

    /**
     * Extracts number from filename
     *  "Photo-001.jpg" -> 1
     *  "Photo-0011.jpg" -> 11
     *  "image-101.png" -> 101
     */
    private fun extractFileNumber(filename: String): Int? {
        return """.*?(\d+).*""".toRegex()
            .find(filename)
            ?.groupValues
            ?.get(1)
            ?.toIntOrNull()
    }

    /**
     * Extracts number from the end of text
     */
    private fun extractStartNumber(text: String): Int {
        return "\\d+$".toRegex()
            .find(text)
            ?.value
            ?.toIntOrNull()
            ?: 1
    }
}