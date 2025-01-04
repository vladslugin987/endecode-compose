package org.example.project.utils

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import mu.KotlinLogging
import org.apache.commons.io.FileUtils
import java.io.File

private val logger = KotlinLogging.logger {}

object FileUtils {
    // Supported file formats
    private val supportedExtensions = setOf(
        "txt", "jpg", "jpeg", "png", "mp4"
    )

    // Video file formats
    private val videoExtensions = setOf(
        "mp4", "avi", "mov", "mkv"
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
     * Checks if the file is an image
     */
    fun isImageFile(file: File): Boolean {
        return file.extension.lowercase() in setOf("jpg", "jpeg", "png")
    }

    /**
     * Checks if the file is a video
     */
    fun isVideoFile(file: File): Boolean {
        return file.extension.lowercase() in videoExtensions
    }
}