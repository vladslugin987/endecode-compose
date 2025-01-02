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
     * Main function for batch copying and encoding
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

            val startNumber = extractStartNumber(baseText)
            val baseTextWithoutNumber = baseText.replace("\\d+$".toRegex(), "").trim()

            // Calculate the total number of operations
            val totalOperations = numCopies * (
                    1 + // copying
                            1 + // encoding
                            (if (addWatermark) 1 else 0) + // watermark
                            (if (addSwap) 1 else 0) + // swap
                            (if (createZip) 1 else 0) // zip creation
                    )
            var completedOperations = 0f

            // Store folders for zip processing
            val foldersToProcess = mutableListOf<Pair<File, String>>()

            // First pass - create and process folders
            for (i in 0 until numCopies) {
                val orderNumber = (startNumber + i).toString().padStart(3, '0')
                val destinationFolder = File(copiesFolder, orderNumber)
                val folderCopy = File(destinationFolder, sourceFolder.name)

                // Copy folder
                FileUtils.copyDirectory(sourceFolder, folderCopy)
                completedOperations++
                progress(completedOperations / totalOperations)

                // Encode files
                val encodedText = "$baseTextWithoutNumber $orderNumber"
                val watermark = EncodingUtils.addWatermark(encodedText)
                FileUtils.getSupportedFiles(folderCopy).forEach { file ->
                    EncodingUtils.processFile(file, watermark)
                }
                completedOperations++
                progress(completedOperations / totalOperations)

                // Add visible watermark if needed
                if (addWatermark) {
                    val actualPhotoNumber = photoNumber ?: orderNumber.toInt()
                    val actualWatermarkText = watermarkText ?: orderNumber
                    addVisibleWatermarkToPhoto(folderCopy, actualWatermarkText, actualPhotoNumber)
                    completedOperations++
                    progress(completedOperations / totalOperations)
                }

                // Perform swap if needed
                if (addSwap) {
                    performSwap(folderCopy, orderNumber)
                    completedOperations++
                    progress(completedOperations / totalOperations)
                }

                foldersToProcess.add(destinationFolder to orderNumber)
                ConsoleState.log("Processed folder: $orderNumber")
            }

            // Second pass - create ZIP archives
            if (createZip) {
                foldersToProcess.forEach { (folder, orderNumber) ->
                    createNoCompressionZip(folder, orderNumber)
                    folder.deleteRecursively()
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
            val currentNum = orderNumber
            val swapNum = (orderNumber.toInt() + 100).toString().padStart(3, '0')

            ConsoleState.log("Starting swap operation...")
            ConsoleState.log("Looking for files with numbers $currentNum and $swapNum")

            val allImages = FileUtils.getSupportedFiles(folder)
                .filter { FileUtils.isImageFile(it) }
                .toList()

            ConsoleState.log("Found ${allImages.size} image files total")

            // Find pairs to swap
            val filePairs = findSwapPairs(allImages, currentNum, swapNum)

            if (filePairs.isEmpty()) {
                ConsoleState.log("No matching pairs found for swapping")
                return@withContext
            }

            // Perform swaps
            swapFilePairs(filePairs)
        } catch (e: Exception) {
            logger.error(e) { "Error performing swap in ${folder.name}" }
            ConsoleState.log("Error during swap operation: ${e.message}")
        }
    }

    /**
     * Creates ZIP archive without compression
     */
    private fun createNoCompressionZip(folder: File, orderNumber: String) {
        val zipFile = File(folder.parent, "$orderNumber.zip")
        ZipOutputStream(FileOutputStream(zipFile)).use { zipOut ->
            zipOut.setLevel(ZipOutputStream.STORED) // No compression

            val filesToZip = folder.walkTopDown()
                .filter { file ->
                    !file.name.startsWith("__MACOSX") &&
                            !file.name.startsWith(".") &&
                            !file.name.endsWith(".DS_Store")
                }
                .toList()

            for (file in filesToZip) {
                val entryPath = file.relativeTo(folder).path
                if (file.isFile) {
                    addFileToZip(file, entryPath, zipOut)
                } else {
                    addDirectoryToZip(entryPath, zipOut)
                }
            }
        }
        ConsoleState.log("Created ZIP archive: ${zipFile.name}")
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
    private fun findSwapPairs(files: List<File>, currentNum: String, swapNum: String): List<Pair<File, File>> {
        return files.mapNotNull { file ->
            val numberPattern = """.*[^0-9](0*$currentNum)[^0-9].*|.*[^0-9](0*$currentNum)$""".toRegex()
            val numberMatch = numberPattern.find(file.name) ?: return@mapNotNull null

            val foundNumber = numberMatch.groupValues[1].takeIf { it.isNotEmpty() }
                ?: numberMatch.groupValues[2]

            val swapFileName = file.name.replace(
                foundNumber,
                "0".repeat(foundNumber.length - swapNum.length) + swapNum
            )
            val swapFile = File(file.parent, swapFileName)

            if (swapFile.exists()) {
                ConsoleState.log("Found pair for swapping:")
                ConsoleState.log("  - ${file.name}")
                ConsoleState.log("  - ${swapFile.name}")
                Pair(file, swapFile)
            } else {
                ConsoleState.log("No matching swap file found for: ${file.name}")
                null
            }
        }
    }

    /**
     * Performs actual swap of file pairs
     */
    private fun swapFilePairs(pairs: List<Pair<File, File>>) {
        pairs.forEach { (file1, file2) ->
            try {
                ConsoleState.log("Swapping files:")
                ConsoleState.log("  - ${file1.name}")
                ConsoleState.log("  - ${file2.name}")

                val tempFile = File(
                    file1.parent,
                    "temp_${System.currentTimeMillis()}_${file1.name}"
                )

                if (file1.renameTo(tempFile) &&
                    file2.renameTo(file1) &&
                    tempFile.renameTo(file2)
                ) {
                    ConsoleState.log("Successfully swapped files")
                } else {
                    ConsoleState.log("Failed to swap files")
                }
            } catch (e: Exception) {
                ConsoleState.log("Error swapping files: ${e.message}")
            }
        }
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

    /**
     * Extracts last number from filename
     */
    private fun extractFileNumber(filename: String): Int? {
        return "\\d+".toRegex()
            .findAll(filename)
            .lastOrNull()
            ?.value
            ?.toIntOrNull()
    }
}