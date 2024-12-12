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
     * Основная функция для пакетного копирования и кодирования
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
            // create folder for copies
            val copiesFolder = File(sourceFolder.parent, "${sourceFolder.name}-Copies")
            if (!copiesFolder.exists()) {
                copiesFolder.mkdir()
            }

            // Extract the start number from baseText
            val startNumber = extractStartNumber(baseText)
            val baseTextWithoutNumber = baseText.replace("\\d+$".toRegex(), "").trim()

            // Calculate the total number of operations for the progress bar
            val totalOperations = numCopies * (
                    1 + // копирование
                            1 + // кодирование
                            (if (addWatermark) 1 else 0) + // водяной знак
                            (if (addSwap) 1 else 0) + // swap
                            (if (createZip) 1 else 0) // создание zip
                    )
            var completedOperations = 0f

            for (i in 0 until numCopies) {
                val orderNumber = (startNumber + i).toString().padStart(3, '0')
                val destinationFolder = File(copiesFolder, orderNumber)

                // Copy original folder
                val folderCopy = File(destinationFolder, sourceFolder.name)
                FileUtils.copyDirectory(sourceFolder, folderCopy)
                completedOperations++
                progress(completedOperations / totalOperations)

                // Encoding all files
                val encodedText = "$baseTextWithoutNumber $orderNumber"
                val watermark = EncodingUtils.addWatermark(encodedText)

                FileUtils.getSupportedFiles(folderCopy).forEach { file ->
                    EncodingUtils.processFile(file, watermark)
                }
                completedOperations++
                progress(completedOperations / totalOperations)

                // Add a "visible" watermark
                if (addWatermark) {
                    val actualPhotoNumber = photoNumber ?: orderNumber.toInt()
                    val actualWatermarkText = watermarkText ?: orderNumber
                    addVisibleWatermarkToPhoto(folderCopy, actualWatermarkText, actualPhotoNumber)
                    completedOperations++
                    progress(completedOperations / totalOperations)
                }

                // swap
                if (addSwap) {
                    performSwap(folderCopy, orderNumber)
                    completedOperations++
                    progress(completedOperations / totalOperations)
                }

                // Create zip
                if (createZip) {
                    createNoCompressionZip(destinationFolder, orderNumber)
                    destinationFolder.deleteRecursively()
                    completedOperations++
                    progress(completedOperations / totalOperations)
                }

                ConsoleState.log("Completed processing copy $orderNumber")
            }

            ConsoleState.log("Batch processing completed successfully")
        } catch (e: Exception) {
            logger.error(e) { "Error during batch processing" }
            ConsoleState.log("Error during batch processing: ${e.message}")
            throw e
        }
    }

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

    private suspend fun performSwap(folder: File, orderNumber: String) = withContext(Dispatchers.IO) {
        try {
            val currentNum = orderNumber
            val swapNum = (orderNumber.toInt() + 100).toString().padStart(3, '0')

            // templates for number lookup taking into account possible leading zeros
            val currentNumRegex = "0*$currentNum"  // can find both 006 and 6
            val swapNumRegex = "0*$swapNum"       // Находит как 106, так и 0106

            ConsoleState.log("Starting swap operation...")
            ConsoleState.log("Looking for files with numbers matching $currentNumRegex and $swapNumRegex")

            // finding all images in folder
            val allImages = FileUtils.getSupportedFiles(folder)
                .filter { FileUtils.isImageFile(it) }
                .toList()

            ConsoleState.log("Found ${allImages.size} image files in total")

            // Looking for file pairs to swap
            val filePairs = allImages.mapNotNull { file ->
                // Looking for the number in the file name, taking into account different formats and leading zeros
                val numberPattern = """.*[^0-9](0*$currentNum)[^0-9].*|.*[^0-9](0*$currentNum)$""".toRegex()
                val numberMatch = numberPattern.find(file.name)

                if (numberMatch != null) {
                    // Extract the found number (including leading zeros, if any)
                    val foundNumber = numberMatch.groupValues[1].takeIf { it.isNotEmpty() }
                        ?: numberMatch.groupValues[2]

                    // Create a paired file name by substituting the found number (preserving the format)
                    val swapFileName = file.name.replace(foundNumber, "0".repeat(foundNumber.length - swapNum.length) + swapNum)
                    val swapFile = File(file.parent, swapFileName)

                    if (swapFile.exists()) {
                        ConsoleState.log("Found matching pair:")
                        ConsoleState.log("  - ${file.name}")
                        ConsoleState.log("  - ${swapFile.name}")
                        Pair(file, swapFile)
                    } else {
                        ConsoleState.log("No matching swap file found for: ${file.name}")
                        null
                    }
                } else {
                    null
                }
            }

            if (filePairs.isEmpty()) {
                ConsoleState.log("No matching pairs found for swapping")
                return@withContext
            }

            // Perform a swap for each found pair
            filePairs.forEach { (file1, file2) ->
                try {
                    ConsoleState.log("Swapping files:")
                    ConsoleState.log("  - ${file1.name}")
                    ConsoleState.log("  - ${file2.name}")

                    val tempFile = File(file1.parent, "temp_${System.currentTimeMillis()}_${file1.name}")

                    if (file1.renameTo(tempFile) &&
                        file2.renameTo(file1) &&
                        tempFile.renameTo(file2)) {
                        ConsoleState.log("Successfully swapped files")
                    } else {
                        ConsoleState.log("Failed to swap files")
                    }
                } catch (e: Exception) {
                    ConsoleState.log("Error swapping files: ${e.message}")
                }
            }

        } catch (e: Exception) {
            logger.error(e) { "Error performing swap in ${folder.name}" }
            ConsoleState.log("Error during swap operation: ${e.message}")
        }
    }

    private fun createNoCompressionZip(folder: File, orderNumber: String) {
        val zipFile = File(folder.parent, "$orderNumber.zip")
        ZipOutputStream(FileOutputStream(zipFile)).use { zipOut ->
            zipOut.setLevel(ZipOutputStream.STORED) // No compression

            folder.walkTopDown()
                .filter { file ->
                    !file.name.startsWith("__MACOSX") &&
                            !file.name.startsWith(".") &&
                            !file.name.endsWith(".DS_Store")
                }
                .forEach { file ->
                    val entryPath = file.relativeTo(folder).path
                    if (file.isFile) {
                        // Read the file into memory to calculate CRC and size
                        val fileBytes = file.readBytes()

                        // Create CRC32 for a file
                        val crc = java.util.zip.CRC32()
                        crc.update(fileBytes)

                        // Create and customize ZipEntry
                        var entry = ZipEntry(entryPath)
                        entry.method = ZipEntry.STORED           // Storage method - uncompressed
                        entry.size = fileBytes.size.toLong()     // File size
                        entry.compressedSize = entry.size        // For STORED, it is the same as size
                        entry.crc = crc.value                    // CRC32 checksum

                        // Add entry and write data
                        zipOut.putNextEntry(entry)
                        zipOut.write(fileBytes)
                        zipOut.closeEntry()
                    } else if (file.isDirectory) {
                        // For directories, add an empty entry with “/” at the end
                        var entry = ZipEntry("$entryPath/")
                        entry.method = ZipEntry.STORED
                        entry.size = 0
                        entry.compressedSize = 0
                        entry.crc = 0
                        zipOut.putNextEntry(entry)
                        zipOut.closeEntry()
                    }
                }
        }
        ConsoleState.log("Created ZIP archive: ${zipFile.name}")
    }

    private fun extractStartNumber(text: String): Int {
        val match = "\\d+$".toRegex().find(text)
        return match?.value?.toIntOrNull() ?: 1
    }

    private fun extractFileNumber(filename: String): Int? {
        val match = "\\d+".toRegex().findAll(filename).lastOrNull()
        return match?.value?.toIntOrNull()
    }

    private fun hasExactNumber(filename: String, number: String): Boolean {
        // Check that the number is surrounded by non-numbers or is at the beginning/end of the string
        val pattern = "(^|\\D)$number(\\D|$)".toRegex()
        return pattern.containsMatchIn(filename)
    }
}