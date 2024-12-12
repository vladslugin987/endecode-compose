package org.example.project.utils

import mu.KotlinLogging
import java.io.File

private val logger = KotlinLogging.logger {}

object EncodingUtils {
    private const val SHIFT = 7
    private const val WATERMARK_PREFIX = "<<=="
    private const val WATERMARK_SUFFIX = "==>>"
    private const val OLD_WATERMARK_PREFIX = "*/"

    fun encodeText(text: String): String {
        return buildString {
            text.forEach { char ->
                append(
                    when {
                        char.isUpperCase() -> {
                            val index = char - 'A'
                            val shifted = (index + SHIFT) % 26 + 'A'.code
                            shifted.toChar()
                        }
                        char.isLowerCase() -> {
                            val index = char - 'a'
                            val shifted = (index + SHIFT) % 26 + 'a'.code
                            shifted.toChar()
                        }
                        char.isDigit() -> {
                            ((char.digitToInt() + SHIFT) % 10).toString()
                        }
                        else -> char
                    }
                )
            }
        }
    }

    fun decodeText(text: String): String {
        return buildString {
            text.forEach { char ->
                append(
                    when {
                        char.isUpperCase() -> {
                            val index = char - 'A'
                            val shifted = (index - SHIFT + 26) % 26 + 'A'.code
                            shifted.toChar()
                        }
                        char.isLowerCase() -> {
                            val index = char - 'a'
                            val shifted = (index - SHIFT + 26) % 26 + 'a'.code
                            shifted.toChar()
                        }
                        char.isDigit() -> {
                            ((char.digitToInt() - SHIFT + 10) % 10).toString()
                        }
                        else -> char
                    }
                )
            }
        }
    }

    fun addWatermark(text: String): String {
        return "$WATERMARK_PREFIX${encodeText(text)}$WATERMARK_SUFFIX"
    }

    fun extractWatermark(content: String): String? {
        return when {
            content.contains(WATERMARK_PREFIX) -> {
                val start = content.lastIndexOf(WATERMARK_PREFIX) + WATERMARK_PREFIX.length
                val end = content.lastIndexOf(WATERMARK_SUFFIX)
                if (start <= end) {
                    content.substring(start, end)
                } else null
            }
            content.contains(OLD_WATERMARK_PREFIX) -> {
                val start = content.lastIndexOf(OLD_WATERMARK_PREFIX) + OLD_WATERMARK_PREFIX.length
                content.substring(start).trim()
            }
            else -> null
        }
    }

    suspend fun processFile(file: File, watermark: String): Boolean {
        return try {
            val content = file.readText(Charsets.UTF_8)
            if (!content.contains(watermark)) {
                file.appendText(watermark)
                logger.info { "${file.name}: Encrypted successfully" }
                ConsoleState.log("${file.name}: Success ✔")
                true
            } else {
                logger.info { "${file.name}: Already contains encrypted text." }
                ConsoleState.log("${file.name}: Encrypted text already present")
                false
            }
        } catch (e: Exception) {
            logger.error(e) { "Error processing file ${file.name}" }
            ConsoleState.log("Error processing ${file.name}: ${e.message}")
            false
        }
    }
}