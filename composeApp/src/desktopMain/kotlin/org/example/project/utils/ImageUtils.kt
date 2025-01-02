package org.example.project.utils

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import mu.KotlinLogging
import org.opencv.core.Core
import org.opencv.core.Mat
import org.opencv.core.Point
import org.opencv.core.Scalar
import org.opencv.imgcodecs.Imgcodecs
import org.opencv.imgproc.Imgproc
import java.io.File

private val logger = KotlinLogging.logger {}

object ImageUtils {
    init {
        try {
            nu.pattern.OpenCV.loadLocally()
            logger.info { "OpenCV initialized successfully" }
        } catch (e: Exception) {
            logger.error(e) { "Failed to initialize OpenCV" }
            ConsoleState.log("Error initializing OpenCV: ${e.message}")
        }
    }

    /**
     * Adds a small semi-transparent text (visible watermark) to an image
     */
    suspend fun addTextToImage(
        imageFile: File,
        text: String,
        position: TextPosition = TextPosition.BOTTOM_RIGHT
    ) = withContext(Dispatchers.IO) {
        try {
            val image = Imgcodecs.imread(imageFile.absolutePath)
            if (image.empty()) {
                ConsoleState.log("Failed to load image: ${imageFile.name}")
                return@withContext false
            }

            val fontFace = Imgproc.FONT_HERSHEY_SIMPLEX
            val fontScale = 0.4
            val thickness = 1
            val alpha = 0.5
            val color = Scalar(255.0, 255.0, 255.0)

            val baseline = IntArray(1)
            val textSize = Imgproc.getTextSize(text, fontFace, fontScale, thickness, baseline)

            val padding = 5 // edge margin
            val textPoint = when (position) {
                TextPosition.BOTTOM_RIGHT -> Point(
                    (image.cols() - textSize.width - padding).toDouble(),
                    (image.rows() - padding).toDouble()
                )
                TextPosition.BOTTOM_LEFT -> Point(
                    padding.toDouble(),
                    (image.rows() - padding).toDouble()
                )
                TextPosition.TOP_RIGHT -> Point(
                    (image.cols() - textSize.width - padding).toDouble(),
                    (textSize.height + padding).toDouble()
                )
                TextPosition.TOP_LEFT -> Point(
                    padding.toDouble(),
                    (textSize.height + padding).toDouble()
                )
                TextPosition.CENTER -> Point(
                    (image.cols() - textSize.width) / 2.0,
                    (image.rows() + textSize.height) / 2.0
                )
            }

            // Create a separate image for text with alpha channel
            val overlay = Mat.zeros(image.size(), image.type())
            Imgproc.putText(
                overlay,
                text,
                textPoint,
                fontFace,
                fontScale,
                color,
                thickness,
                Imgproc.LINE_AA
            )

            // Blend images with transparency
            Core.addWeighted(image, 1.0, overlay, alpha, 0.0, image)

            // save imageß
            val success = Imgcodecs.imwrite(imageFile.absolutePath, image)
            if (success) {
                logger.info { "Successfully added text to ${imageFile.name}" }
                ConsoleState.log("Added text to ${imageFile.name}")
            } else {
                logger.error { "Failed to save image ${imageFile.name}" }
                ConsoleState.log("Failed to save image ${imageFile.name}")
            }

            // releasing resources
            overlay.release()
            image.release()

            success
        } catch (e: Exception) {
            logger.error(e) { "Error adding text to image ${imageFile.name}" }
            ConsoleState.log("Error processing image ${imageFile.name}: ${e.message}")
            false
        }
    }
}

enum class TextPosition {
    TOP_LEFT,
    TOP_RIGHT,
    CENTER,
    BOTTOM_LEFT,
    BOTTOM_RIGHT
}