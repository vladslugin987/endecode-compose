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

    private data class WatermarkInfo(
        val startPosition: Int,
        val endPosition: Int,
        val content: ByteArray? = null
    )

    /**
     * Removes invisible watermarks from files in a directory
     */
    suspend fun removeWatermarks(directory: File, progress: (Float) -> Unit) = withContext(Dispatchers.IO) {
        try {
            val files = directory.walk()
                .filter { it.isFile &&
                        (it.extension.lowercase() in setOf("jpg", "jpeg", "png", "mp4"))
                }
                .toList()

            var processedFiles = 0f
            val totalFiles = files.size

            if (totalFiles == 0) {
                ConsoleState.log("No supported files found for watermark removal in ${directory.name}")
                progress(1f)
                return@withContext true
            }

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
        readWatermarkData(file)?.let { (tailData, _) ->
            findWatermark(tailData, includeContent = true)?.content?.let {
                String(it, Charsets.UTF_8).also { text ->
                    logger.info { "Found watermark in ${file.name}: $text" }
                }
            }
        }
    }

    /**
     * Checks for the presence of a watermark in the file
     */
    suspend fun hasWatermark(file: File): Boolean = withContext(Dispatchers.IO) {
        readWatermarkData(file)?.let { (tailData, _) ->
            findWatermark(tailData) != null
        } ?: false
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
        val (tailData, fileSize) = readWatermarkData(file) ?: return@withContext false

        val watermarkInfo = findWatermark(tailData) ?: return@withContext false

        RandomAccessFile(file, "rw").use { randomAccessFile ->
            try {
                val watermarkPosition = fileSize - (tailData.size - watermarkInfo.startPosition)
                randomAccessFile.setLength(watermarkPosition)
                logger.info { "Removed watermark from ${file.name}" }
                true
            } catch (e: Exception) {
                logger.error(e) { "Error removing watermark from ${file.name}" }
                false
            }
        }
    }

    /**
     * Reads the last MAX_WATERMARK_LENGTH bytes from a file
     */
    private suspend fun readWatermarkData(file: File): Pair<ByteArray, Long>? = withContext(Dispatchers.IO) {
        RandomAccessFile(file, "r").use { randomAccessFile ->
            try {
                val fileSize = randomAccessFile.length()
                if (fileSize <= 0) return@use null

                val readLength = MAX_WATERMARK_LENGTH.coerceAtMost(fileSize.toInt())
                randomAccessFile.seek(fileSize - readLength)
                val tailData = ByteArray(readLength)
                randomAccessFile.read(tailData)

                tailData to fileSize
            } catch (e: Exception) {
                logger.error(e) { "Error reading watermark data from ${file.name}" }
                null
            }
        }
    }

    /**
     * Looks for watermark signatures in byte array
     */
    private fun findWatermark(data: ByteArray, includeContent: Boolean = false): WatermarkInfo? {
        val startPosition = findBytes(data, WATERMARK_START, reverse = true)
        if (startPosition == -1) return null

        val endPosition = findBytes(data, WATERMARK_END, startFrom = startPosition)
        if (endPosition == -1 || endPosition <= startPosition) return null

        val content = if (includeContent) {
            data.slice((startPosition + WATERMARK_START.size) until endPosition).toByteArray()
        } else null

        return WatermarkInfo(startPosition, endPosition, content)
    }

    /**
     * Searches for byte pattern in data array
     * @param reverse if true, searches from end to start
     * @param startFrom position to start search from
     */
    private fun findBytes(data: ByteArray, pattern: ByteArray, startFrom: Int = 0, reverse: Boolean = false): Int {
        val range = if (reverse) {
            (data.size - pattern.size) downTo startFrom
        } else {
            startFrom..data.size - pattern.size
        }

        for (i in range) {
            if (data.matchesAt(i, pattern)) return i
        }
        return -1
    }

    /**
     * Checks if byte array matches pattern at given position
     */
    private fun ByteArray.matchesAt(pos: Int, pattern: ByteArray): Boolean {
        return pattern.indices.all { this[pos + it] == pattern[it] }
    }
}