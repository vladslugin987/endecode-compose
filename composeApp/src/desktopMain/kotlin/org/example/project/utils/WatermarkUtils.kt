package org.example.project.utils

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import mu.KotlinLogging
import java.io.File
import java.io.RandomAccessFile

private val logger = KotlinLogging.logger {}

object WatermarkUtils {
    private const val MAX_WATERMARK_LENGTH = 100
    private val WATERMARK_START = "<<==".toByteArray(Charsets.UTF_8)
    private val WATERMARK_END = "==>>".toByteArray(Charsets.UTF_8)

    /**
     * Removes invisible watermarks from files in a directory
     */
    suspend fun removeWatermarks(directory: File, progress: (Float) -> Unit) = withContext(Dispatchers.IO) {
        try {
            val files = directory.walk()
                .filter { it.isFile && it.extension.lowercase() in setOf("jpg", "jpeg", "png") }
                .toList()

            var processedFiles = 0f
            val totalFiles = files.size

            files.forEach { file ->
                try {
                    if (removeWatermarkFromFile(file)) {
                        ConsoleState.log("Watermark removed from ${file.name}")
                    } else {
                        ConsoleState.log("No watermark found in ${file.name}")
                    }
                } catch (e: Exception) {
                    logger.error(e) { "Error removing watermark from ${file.name}" }
                    ConsoleState.log("Error processing ${file.name}: ${e.message}")
                } finally {
                    processedFiles++
                    progress(processedFiles / totalFiles)
                }
            }

            ConsoleState.log("Watermark removal completed")
            true
        } catch (e: Exception) {
            logger.error(e) { "Error during watermark removal process" }
            ConsoleState.log("Error during watermark removal: ${e.message}")
            false
        }
    }

    /**
     * Extracts the encoded text from the watermark
     */
    suspend fun extractWatermarkText(file: File): String? = withContext(Dispatchers.IO) {
        RandomAccessFile(file, "r").use { randomAccessFile ->
            try {
                // Get the file size
                val fileSize = randomAccessFile.length()
                if (fileSize < MAX_WATERMARK_LENGTH) {
                    return@use null
                }

                // Read the last MAX_WATERMARK_LENGTH bytes
                val readLength = MAX_WATERMARK_LENGTH.coerceAtMost(fileSize.toInt())
                randomAccessFile.seek(fileSize - readLength)
                val tailData = ByteArray(readLength)
                randomAccessFile.read(tailData)

                // Looking for watermark signatures
                val watermarkStart = findLastIndex(tailData, WATERMARK_START)
                if (watermarkStart == -1) return@use null

                val watermarkEnd = findIndex(tailData, WATERMARK_END, watermarkStart)
                if (watermarkEnd == -1) return@use null

                // Extract the watermark text
                if (watermarkEnd > watermarkStart) {
                    val watermarkText = tailData.slice(
                        (watermarkStart + WATERMARK_START.size) until watermarkEnd
                    ).toByteArray()

                    val extractedText = String(watermarkText, Charsets.UTF_8)
                    logger.info { "Found watermark in ${file.name}: $extractedText" }
                    return@use extractedText
                }

                null
            } catch (e: Exception) {
                logger.error(e) { "Error extracting watermark from ${file.name}" }
                null
            }
        }
    }

    /**
     * Checks for the presence of a watermark in the file
     */
    suspend fun hasWatermark(file: File): Boolean = withContext(Dispatchers.IO) {
        RandomAccessFile(file, "r").use { randomAccessFile ->
            try {
                val fileSize = randomAccessFile.length()
                if (fileSize < MAX_WATERMARK_LENGTH) return@use false

                val readLength = MAX_WATERMARK_LENGTH.coerceAtMost(fileSize.toInt())
                randomAccessFile.seek(fileSize - readLength)
                val tailData = ByteArray(readLength)
                randomAccessFile.read(tailData)

                val watermarkStart = findLastIndex(tailData, WATERMARK_START)
                if (watermarkStart == -1) return@use false

                val watermarkEnd = findIndex(tailData, WATERMARK_END, watermarkStart)
                watermarkEnd != -1 && watermarkEnd > watermarkStart
            } catch (e: Exception) {
                logger.error(e) { "Error checking watermark in ${file.name}" }
                false
            }
        }
    }

    /**
     * Adds a watermark to a file
     */
    suspend fun addWatermark(file: File, encodedText: String): Boolean = withContext(Dispatchers.IO) {
        try {
            if (hasWatermark(file)) {
                ConsoleState.log("${file.name}: Already has watermark")
                return@withContext false
            }

            RandomAccessFile(file, "rw").use { randomAccessFile ->
                val watermark = WATERMARK_START + encodedText.toByteArray(Charsets.UTF_8) + WATERMARK_END
                randomAccessFile.seek(randomAccessFile.length())
                randomAccessFile.write(watermark)
            }

            ConsoleState.log("${file.name}: Watermark added successfully")
            true
        } catch (e: Exception) {
            logger.error(e) { "Error adding watermark to ${file.name}" }
            ConsoleState.log("Error adding watermark to ${file.name}: ${e.message}")
            false
        }
    }

    /**
     * Removes a watermark from a specific file
     */
    private suspend fun removeWatermarkFromFile(file: File): Boolean = withContext(Dispatchers.IO) {
        RandomAccessFile(file, "rw").use { randomAccessFile ->
            try {
                // getting file size
                val fileSize = randomAccessFile.length()
                if (fileSize < MAX_WATERMARK_LENGTH) {
                    return@use false
                }

                // Read the last MAX_WATERMARK_LENGTH bytes
                val readLength = MAX_WATERMARK_LENGTH.coerceAtMost(fileSize.toInt())
                randomAccessFile.seek(fileSize - readLength)
                val tailData = ByteArray(readLength)
                randomAccessFile.read(tailData)

                // Looking for watermark signatures
                val watermarkStart = findLastIndex(tailData, WATERMARK_START)
                if (watermarkStart == -1) return@use false

                val watermarkEnd = findIndex(tailData, WATERMARK_END, watermarkStart)
                if (watermarkEnd == -1) return@use false

                // Check that the watermark is found correctly
                if (watermarkEnd > watermarkStart) {
                    // Calculate the position to trim the file
                    val watermarkPosition = fileSize - (readLength - watermarkStart)
                    randomAccessFile.setLength(watermarkPosition)
                    logger.info { "Removed watermark from ${file.name}" }
                    return@use true
                }

                false
            } catch (e: Exception) {
                logger.error(e) { "Error removing watermark from ${file.name}" }
                false
            }
        }
    }

    /**
     * Searches for the last occurrence of the bytes array in data
     */
    private fun findLastIndex(data: ByteArray, bytes: ByteArray): Int {
        for (i in data.size - bytes.size downTo 0) {
            var found = true
            for (j in bytes.indices) {
                if (data[i + j] != bytes[j]) {
                    found = false
                    break
                }
            }
            if (found) return i
        }
        return -1
    }

    /**
     * Searches for the first occurrence of the bytes array in data, starting from the startFrom position
     */
    private fun findIndex(data: ByteArray, bytes: ByteArray, startFrom: Int): Int {
        for (i in startFrom..data.size - bytes.size) {
            var found = true
            for (j in bytes.indices) {
                if (data[i + j] != bytes[j]) {
                    found = false
                    break
                }
            }
            if (found) return i
        }
        return -1
    }
}