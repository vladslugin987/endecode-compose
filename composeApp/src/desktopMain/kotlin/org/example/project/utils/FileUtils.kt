package org.example.project.utils

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import mu.KotlinLogging
import org.apache.commons.io.FileUtils
import java.io.File
import java.nio.file.Files
import java.nio.file.Path
import kotlin.io.path.isDirectory
import kotlin.io.path.walk

private val logger = KotlinLogging.logger {}

object FileUtils {
    // Supported file formats
    private val supportedExtensions = setOf(
        "txt", "jpg", "jpeg", "png", "mp4"
    )

    /**
     * Creates a copy of the directory with a new name
     */
    suspend fun copyDirectory(source: File, destination: File) = withContext(Dispatchers.IO) {
        try {
            FileUtils.copyDirectory(source, destination)
            ConsoleState.log("Directory copied: ${destination.name}")
            true
        } catch (e: Exception) {
            logger.error(e) { "Error copying directory from ${source.path} to ${destination.path}" }
            ConsoleState.log("Error copying directory: ${e.message}")
            false
        }
    }

    /**
     * Gets a list of all supported files in the directory
     */
    suspend fun getSupportedFiles(directory: File): List<File> = withContext(Dispatchers.IO) {
        try {
            directory.walk()
                .filter { file ->
                    file.isFile && file.extension.lowercase() in supportedExtensions
                }
                .toList()
        } catch (e: Exception) {
            logger.error(e) { "Error getting files from directory ${directory.path}" }
            ConsoleState.log("Error scanning directory: ${e.message}")
            emptyList()
        }
    }

    /**
     * Counts the number of files in the directory for the progress bar
     */
    suspend fun countFiles(directory: File): Int = withContext(Dispatchers.IO) {
        try {
            directory.walk()
                .filter { it.isFile && it.extension.lowercase() in supportedExtensions }
                .count()
        } catch (e: Exception) {
            logger.error(e) { "Error counting files in directory ${directory.path}" }
            0
        }
    }

    /**
     * Swaps two files
     */
    suspend fun swapFiles(file1: File, file2: File) = withContext(Dispatchers.IO) {
        try {
            val tempFile = File("${file1.parent}/temp_${System.currentTimeMillis()}")
            file1.renameTo(tempFile)
            file2.renameTo(file1)
            tempFile.renameTo(file2)
            logger.info { "Swapped files: ${file1.name} <-> ${file2.name}" }
            ConsoleState.log("Swapped: ${file1.name} <-> ${file2.name}")
            true
        } catch (e: Exception) {
            logger.error(e) { "Error swapping files ${file1.name} and ${file2.name}" }
            ConsoleState.log("Error swapping files: ${e.message}")
            false
        }
    }

    /**
     * Checks if the file contains binary data
     */
    suspend fun isBinaryFile(file: File): Boolean = withContext(Dispatchers.IO) {
        try {
            val bytes = file.readBytes().take(8000)
            val textChars = (7..127).toSet() + setOf(9, 10, 13)
            bytes.any { it.toInt() !in textChars }
        } catch (e: Exception) {
            logger.error(e) { "Error checking if file is binary: ${file.name}" }
            true
        }
    }

    /**
     * Safely reads the contents of a file
     */
    suspend fun safeReadText(file: File): String? = withContext(Dispatchers.IO) {
        try {
            if (!isBinaryFile(file)) {
                file.readText(Charsets.UTF_8)
            } else {
                null
            }
        } catch (e: Exception) {
            logger.error(e) { "Error reading file: ${file.name}" }
            null
        }
    }

    /**
     * Safely writes text to a file
     */
    suspend fun safeWriteText(file: File, text: String) = withContext(Dispatchers.IO) {
        try {
            file.writeText(text, Charsets.UTF_8)
            true
        } catch (e: Exception) {
            logger.error(e) { "Error writing to file: ${file.name}" }
            ConsoleState.log("Error writing to file ${file.name}: ${e.message}")
            false
        }
    }

    /**
     * Creates a unique name for the folder copy
     */
    fun createUniqueFolderName(baseFolder: File, number: Int): File {
        val parentDir = baseFolder.parentFile
        val baseName = baseFolder.name
        return File(parentDir, "$baseName-${number.toString().padStart(3, '0')}")
    }

    /**
     * Checks if the file is an image
     */
    fun isImageFile(file: File): Boolean {
        return file.extension.lowercase() in setOf("jpg", "jpeg", "png")
    }
}