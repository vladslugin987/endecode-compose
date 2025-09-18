package org.example.project.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.unit.dp
import org.example.project.ui.theme.*
import kotlin.math.cos
import kotlin.math.sin

/**
 * Glass Card - Modern glassmorphism card component that adapts to theme
 */
@Composable
fun GlassCard(
    modifier: Modifier = Modifier,
    borderRadius: androidx.compose.ui.unit.Dp = Dimensions.radiusMedium,
    borderColor: Color? = null,
    backgroundColor: Color? = null,
    elevation: androidx.compose.ui.unit.Dp = Dimensions.glassElevation,
    content: @Composable BoxScope.() -> Unit
) {
    val isDark = !MaterialTheme.colorScheme.surface.luminance().let { it > 0.5f }
    
    val actualBorderColor = borderColor ?: if (isDark) DarkGlassCardBorder else LightGlassCardBorder
    val actualBackgroundColor = backgroundColor ?: if (isDark) DarkGlassCard else LightGlassCard
    
    Box(
        modifier = modifier
            .shadow(
                elevation = elevation,
                shape = RoundedCornerShape(borderRadius),
                clip = false
            )
            .clip(RoundedCornerShape(borderRadius))
            .background(
                brush = if (isDark) {
                    Brush.linearGradient(
                        colors = listOf(
                            actualBackgroundColor.copy(alpha = 0.4f),
                            actualBackgroundColor.copy(alpha = 0.25f),
                            actualBackgroundColor.copy(alpha = 0.35f)
                        ),
                        start = Offset(0f, 0f),
                        end = Offset(1000f, 1000f)
                    )
                } else {
                    Brush.linearGradient(
                        colors = listOf(
                            actualBackgroundColor.copy(alpha = 0.9f),
                            actualBackgroundColor.copy(alpha = 0.75f),
                            actualBackgroundColor.copy(alpha = 0.85f)
                        ),
                        start = Offset(0f, 0f),
                        end = Offset(1000f, 1000f)
                    )
                }
            )
            .border(
                width = Dimensions.glassBorderWidth,
                brush = Brush.linearGradient(
                    colors = if (isDark) {
                        listOf(
                            actualBorderColor.copy(alpha = 0.9f),
                            actualBorderColor.copy(alpha = 0.6f),
                            actualBorderColor.copy(alpha = 0.4f)
                        )
                    } else {
                        listOf(
                            actualBorderColor.copy(alpha = 0.8f),
                            actualBorderColor.copy(alpha = 0.5f),
                            actualBorderColor.copy(alpha = 0.3f)
                        )
                    },
                    start = Offset(0f, 0f),
                    end = Offset(500f, 500f)
                ),
                shape = RoundedCornerShape(borderRadius)
            ),
        content = content
    )
}

// Button variant enum for better hierarchy
enum class ButtonVariant {
    PRIMARY,
    SECONDARY,
    DESTRUCTIVE
}

/**
 * Animated Glass Button with proper variant system for better UI hierarchy
 */
@Composable
fun AnimatedGlassButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    variant: ButtonVariant = ButtonVariant.SECONDARY,
    content: @Composable RowScope.() -> Unit
) {
    var isPressed by remember { mutableStateOf(false) }
    val isDark = !MaterialTheme.colorScheme.surface.luminance().let { it > 0.5f }
    
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.95f else 1f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
        label = "button_scale"
    )
    
    val (backgroundColor, borderColor, contentColor) = when (variant) {
        ButtonVariant.PRIMARY -> {
            val bg = if (enabled) Primary500 else Primary500.copy(alpha = 0.5f)
            val border = Primary400.copy(alpha = 0.8f)
            val content = Color.White
            Triple(bg, border, content)
        }
        ButtonVariant.SECONDARY -> {
            val bg = if (enabled) {
                if (isDark) DarkGlassCard else LightGlassCard
            } else {
                if (isDark) DarkGlassCard.copy(alpha = 0.5f) else LightGlassCard.copy(alpha = 0.5f)
            }
            val border = if (isDark) DarkGlassCardBorder else LightGlassCardBorder
            val content = if (isDark) DarkOnSurface else LightOnSurface
            Triple(bg, border, content)
        }
        ButtonVariant.DESTRUCTIVE -> {
            val bg = if (enabled) Error500 else Error500.copy(alpha = 0.5f)
            val border = Error400.copy(alpha = 0.8f)
            val content = Color.White
            Triple(bg, border, content)
        }
    }

    Button(
        onClick = {
            if (enabled) {
                isPressed = true
                onClick()
                // Reset press state after a short delay
                isPressed = false
            }
        },
        enabled = enabled,
        modifier = modifier
            .height(Dimensions.buttonHeight),
        colors = ButtonDefaults.buttonColors(
            containerColor = backgroundColor,
            contentColor = contentColor,
            disabledContainerColor = backgroundColor.copy(alpha = 0.3f),
            disabledContentColor = contentColor.copy(alpha = 0.5f)
        ),
        shape = RoundedCornerShape(Dimensions.radiusSmall),
        border = androidx.compose.foundation.BorderStroke(
            width = if (variant == ButtonVariant.PRIMARY) 0.dp else Dimensions.glassBorderWidth,
            color = borderColor
        ),
        elevation = ButtonDefaults.buttonElevation(
            defaultElevation = if (variant == ButtonVariant.PRIMARY) Dimensions.elevationSmall else Dimensions.elevationNone
        ),
        content = content
    )
}

/**
 * Legacy function for backward compatibility - now uses ButtonVariant system
 */
@Deprecated("Use variant parameter instead", ReplaceWith("AnimatedGlassButton(onClick, modifier, enabled, if (isPrimary) ButtonVariant.PRIMARY else ButtonVariant.SECONDARY, content)"))
@Composable
fun AnimatedGlassButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    isPrimary: Boolean = true,
    content: @Composable RowScope.() -> Unit
) {
    AnimatedGlassButton(
        onClick = onClick,
        modifier = modifier,
        enabled = enabled,
        variant = if (isPrimary) ButtonVariant.PRIMARY else ButtonVariant.SECONDARY,
        content = content
    )
}

// Validation result for form fields
data class ValidationResult(
    val isValid: Boolean,
    val errorMessage: String? = null,
    val helpText: String? = null
)

/**
 * Terminal-styled text field that adapts to theme with full transparency
 */
@Composable
fun TerminalTextField(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    label: String? = null,
    supportingText: String? = null,
    enabled: Boolean = true,
    singleLine: Boolean = true,
    validation: ValidationResult? = null
) {
    val isDark = !MaterialTheme.colorScheme.surface.luminance().let { it > 0.5f }
    
    val terminalAccent = if (isDark) DarkTerminalAccent else LightTerminalAccent
    val terminalText = if (isDark) DarkTerminalText else LightTerminalText
    val onSurface = if (isDark) DarkOnSurface else LightOnSurface
    val isError = validation?.isValid == false
    
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = modifier,
        label = label?.let { { Text(it, color = terminalAccent) } },
        supportingText = {
            val textToShow = when {
                isError && validation?.errorMessage != null -> validation.errorMessage
                validation?.helpText != null -> validation.helpText
                supportingText != null -> supportingText
                else -> null
            }
            textToShow?.let { 
                Text(
                    it, 
                    color = if (isError) Error500 else terminalText.copy(alpha = 0.8f)
                ) 
            }
        },
        enabled = enabled,
        singleLine = singleLine,
        isError = isError,
        colors = OutlinedTextFieldDefaults.colors(
            focusedTextColor = onSurface,
            unfocusedTextColor = onSurface.copy(alpha = 0.9f),
            disabledTextColor = onSurface.copy(alpha = 0.6f),
            focusedBorderColor = if (isError) Error500 else terminalAccent,
            unfocusedBorderColor = if (isError) Error500 else terminalText.copy(alpha = 0.6f),
            disabledBorderColor = terminalText.copy(alpha = 0.3f),
            focusedLabelColor = if (isError) Error500 else terminalAccent,
            unfocusedLabelColor = if (isError) Error500 else terminalText.copy(alpha = 0.8f),
            disabledLabelColor = terminalText.copy(alpha = 0.5f),
            cursorColor = terminalAccent,
            errorBorderColor = Error500,
            errorLabelColor = Error500,
            // Complete transparency for containers
            focusedContainerColor = Color.Transparent,
            unfocusedContainerColor = Color.Transparent,
            disabledContainerColor = Color.Transparent,
            errorContainerColor = Color.Transparent
        ),
        shape = RoundedCornerShape(Dimensions.radiusSmall)
    )
}

/**
 * Enhanced text field with validation and live preview for name injection
 */
@Composable
fun NameInjectorInput(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    previewPattern: (String, Int) -> String = { name, index -> "${name}_${String.format("%03d", index)}" }
) {
    val allowedNameRegex = remember { Regex("^[A-Za-z0-9 _\\-\\.!@#\$%^&*()\\[\\]{};:,.'\"+=]*$") }
    
    val validation = remember(value) {
        when {
            value.isEmpty() -> ValidationResult(
                isValid = true,
                helpText = "Allowed: latin letters, numbers, special characters (.-_ etc.)"
            )
            !allowedNameRegex.matches(value) -> ValidationResult(
                isValid = false,
                errorMessage = "Only latin letters, numbers and special characters allowed",
                helpText = "Example: file_name, ORDER-001, data@2024"
            )
            value.length > 50 -> ValidationResult(
                isValid = false,
                errorMessage = "Name too long (max 50 characters)"
            )
            else -> ValidationResult(
                isValid = true,
                helpText = "Preview: ${previewPattern(value, 1)}.jpg, ${previewPattern(value, 2)}.jpg..."
            )
        }
    }
    
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(Dimensions.spacingSmall)
    ) {
        TerminalTextField(
            value = value,
            onValueChange = { newValue ->
                // Filter invalid characters on input
                val filtered = newValue.filter { allowedNameRegex.matches(it.toString()) }
                if (filtered.length <= 50) {
                    onValueChange(filtered)
                }
            },
            label = "Name to inject",
            enabled = enabled,
            validation = validation,
            modifier = Modifier.fillMaxWidth()
        )
        
        // Live preview section
        if (validation.isValid && value.isNotEmpty()) {
            GlassCard(
                borderRadius = Dimensions.radiusSmall,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(Dimensions.cardPadding),
                    verticalArrangement = Arrangement.spacedBy(Dimensions.spacingXSmall)
                ) {
                    Text(
                        "Preview:",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        "${previewPattern(value, 1)}.jpg",
                        style = MaterialTheme.typography.bodySmall.copy(
                            fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace
                        ),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        "${previewPattern(value, 2)}.jpg",
                        style = MaterialTheme.typography.bodySmall.copy(
                            fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace
                        ),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        "...",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

/**
 * Animated cosmic background with glassmorphism effect
 */
@Composable
fun GradientBackground(
    modifier: Modifier = Modifier,
    content: @Composable BoxScope.() -> Unit
) {
    val isDark = !MaterialTheme.colorScheme.surface.luminance().let { it > 0.5f }
    
    // Animation for floating particles
    val infiniteTransition = rememberInfiniteTransition(label = "cosmic_animation")
    
    val offset1 by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(20000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "offset1"
    )
    
    val offset2 by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = -360f,
        animationSpec = infiniteRepeatable(
            animation = tween(15000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "offset2"
    )
    
    val offset3 by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 180f,
        animationSpec = infiniteRepeatable(
            animation = tween(25000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "offset3"
    )
    
    Box(modifier = modifier) {
        // Enhanced cosmic background with improved gradients
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    brush = if (isDark) {
                        Brush.linearGradient(
                            colors = listOf(
                                Color(0xFF0B1426), // Deep space blue
                                Color(0xFF1A1F3A), // Rich dark purple
                                Color(0xFF2D1B69), // Deep violet
                                Color(0xFF1A0B2E), // Dark purple
                                Color(0xFF0F0B1F)  // Almost black
                            ),
                            start = Offset(0f, 0f),
                            end = Offset(1000f, 1000f)
                        )
                    } else {
                        Brush.linearGradient(
                            colors = listOf(
                                Color(0xFFF8FAFF), // Pure white
                                Color(0xFFEEF2FF), // Light blue tint
                                Color(0xFFE0E7FF), // Soft blue
                                Color(0xFFF0F4FF), // Very light blue
                                Color(0xFFFBFCFF)  // Almost white
                            ),
                            start = Offset(0f, 0f),
                            end = Offset(1000f, 1000f)
                        )
                    }
                )
        )
        
        // Animated floating orbs/particles
        AnimatedCosmicOrbs(
            isDark = isDark,
            offset1 = offset1,
            offset2 = offset2,
            offset3 = offset3
        )
        
        // Content on top
        Box(
            modifier = Modifier.fillMaxSize(),
            content = content
        )
    }
}

@Composable
private fun AnimatedCosmicOrbs(
    isDark: Boolean,
    offset1: Float,
    offset2: Float,
    offset3: Float
) {
    Canvas(modifier = Modifier.fillMaxSize()) {
        val width = size.width
        val height = size.height
        
        if (isDark) {
            // Enhanced dark theme cosmic orbs with stronger presence
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        Primary300.copy(alpha = 0.25f),
                        Primary500.copy(alpha = 0.15f),
                        Primary700.copy(alpha = 0.08f),
                        Color.Transparent
                    ),
                    radius = 250f
                ),
                radius = 250f,
                center = Offset(
                    width * 0.2f + cos(Math.toRadians(offset1.toDouble())).toFloat() * 100f,
                    height * 0.3f + sin(Math.toRadians(offset1.toDouble())).toFloat() * 80f
                )
            )
            
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        Secondary300.copy(alpha = 0.22f),
                        Secondary500.copy(alpha = 0.12f),
                        Secondary700.copy(alpha = 0.06f),
                        Color.Transparent
                    ),
                    radius = 200f
                ),
                radius = 200f,
                center = Offset(
                    width * 0.8f + cos(Math.toRadians(offset2.toDouble())).toFloat() * 120f,
                    height * 0.7f + sin(Math.toRadians(offset2.toDouble())).toFloat() * 100f
                )
            )
            
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        Accent300.copy(alpha = 0.18f),
                        Accent500.copy(alpha = 0.10f),
                        Accent700.copy(alpha = 0.05f),
                        Color.Transparent
                    ),
                    radius = 220f
                ),
                radius = 220f,
                center = Offset(
                    width * 0.5f + cos(Math.toRadians(offset3.toDouble())).toFloat() * 80f,
                    height * 0.5f + sin(Math.toRadians(offset3.toDouble())).toFloat() * 60f
                )
            )
        } else {
            // Enhanced light theme subtle orbs with better visibility
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        Primary300.copy(alpha = 0.4f),
                        Primary200.copy(alpha = 0.25f),
                        Primary100.copy(alpha = 0.12f),
                        Color.Transparent
                    ),
                    radius = 280f
                ),
                radius = 280f,
                center = Offset(
                    width * 0.15f + cos(Math.toRadians(offset1.toDouble())).toFloat() * 80f,
                    height * 0.25f + sin(Math.toRadians(offset1.toDouble())).toFloat() * 60f
                )
            )
            
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        Secondary300.copy(alpha = 0.35f),
                        Secondary200.copy(alpha = 0.20f),
                        Secondary100.copy(alpha = 0.10f),
                        Color.Transparent
                    ),
                    radius = 240f
                ),
                radius = 240f,
                center = Offset(
                    width * 0.85f + cos(Math.toRadians(offset2.toDouble())).toFloat() * 100f,
                    height * 0.75f + sin(Math.toRadians(offset2.toDouble())).toFloat() * 80f
                )
            )
            
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        Accent300.copy(alpha = 0.3f),
                        Accent200.copy(alpha = 0.18f),
                        Accent100.copy(alpha = 0.08f),
                        Color.Transparent
                    ),
                    radius = 260f
                ),
                radius = 260f,
                center = Offset(
                    width * 0.6f + cos(Math.toRadians(offset3.toDouble())).toFloat() * 70f,
                    height * 0.4f + sin(Math.toRadians(offset3.toDouble())).toFloat() * 50f
                )
            )
        }
    }
}

/**
 * Floating action button with glass effects
 */
@Composable
fun GlassFloatingActionButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    containerColor: Color = Primary500,
    content: @Composable () -> Unit
) {
    FloatingActionButton(
        onClick = onClick,
        modifier = modifier,
        containerColor = containerColor.copy(alpha = 0.9f),
        contentColor = Color.White,
        elevation = FloatingActionButtonDefaults.elevation(
            defaultElevation = Dimensions.elevationLarge
        ),
        shape = RoundedCornerShape(Dimensions.radiusMedium),
        content = content
    )
}

/**
 * Status indicator with glow effect
 */
@Composable
fun StatusIndicator(
    isActive: Boolean,
    modifier: Modifier = Modifier,
    activeColor: Color = TerminalSuccess,
    inactiveColor: Color = TerminalText
) {
    val color by animateColorAsState(
        targetValue = if (isActive) activeColor else inactiveColor,
        animationSpec = tween(durationMillis = Dimensions.animationMedium),
        label = "status_color"
    )
    
    val alpha by animateFloatAsState(
        targetValue = if (isActive) 1f else 0.5f,
        animationSpec = tween(durationMillis = Dimensions.animationMedium),
        label = "status_alpha"
    )
    
    Box(
        modifier = modifier
            .size(8.dp)
            .clip(androidx.compose.foundation.shape.CircleShape)
            .background(color.copy(alpha = alpha))
    )
}

/**
 * Undo snackbar for destructive operations
 */
@Composable
fun UndoSnackbar(
    message: String,
    onUndo: () -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
    duration: Long = 8000L // 8 seconds
) {
    var remainingTime by remember { mutableStateOf(duration) }
    
    LaunchedEffect(Unit) {
        val startTime = System.currentTimeMillis()
        while (remainingTime > 0) {
            kotlinx.coroutines.delay(100)
            remainingTime = (duration - (System.currentTimeMillis() - startTime)).coerceAtLeast(0)
        }
        onDismiss()
    }
    
    GlassCard(
        modifier = modifier,
        backgroundColor = Success500.copy(alpha = 0.9f),
        borderColor = Success400
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(Dimensions.cardPadding),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(Dimensions.spacingXSmall)
            ) {
                Text(
                    text = message,
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontWeight = FontWeight.Medium
                    ),
                    color = Color.White
                )
                val seconds = (remainingTime / 1000).toInt()
                Text(
                    text = "Auto-dismiss in ${seconds}s",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.White.copy(alpha = 0.8f)
                )
            }
            
            Row(
                horizontalArrangement = Arrangement.spacedBy(Dimensions.spacingSmall)
            ) {
                TextButton(
                    onClick = onUndo,
                    colors = ButtonDefaults.textButtonColors(
                        contentColor = Color.White
                    )
                ) {
                    Text(
                        "UNDO",
                        fontWeight = FontWeight.Bold
                    )
                }
                IconButton(
                    onClick = onDismiss
                ) {
                    Icon(
                        imageVector = androidx.compose.material.icons.Icons.Default.Close,
                        contentDescription = "Dismiss",
                        tint = Color.White,
                        modifier = Modifier.size(Dimensions.iconSmall)
                    )
                }
            }
        }
    }
}

// File drop state for enhanced file selector
enum class FileDropState {
    IDLE,
    HOVER,
    SELECTED,
    ERROR
}

/**
 * Enhanced file drop area with visual states and file information
 */
@Composable
fun FileDropArea(
    selectedFiles: List<java.io.File>,
    onFilesDropped: (List<java.io.File>) -> Unit,
    modifier: Modifier = Modifier,
    state: FileDropState = FileDropState.IDLE,
    errorMessage: String? = null,
    maxFiles: Int = Int.MAX_VALUE,
    allowedExtensions: List<String> = emptyList()
) {
    val isDark = !MaterialTheme.colorScheme.surface.luminance().let { it > 0.5f }
    
    val (backgroundColor, borderColor, contentColor) = when (state) {
        FileDropState.IDLE -> Triple(
            if (isDark) DarkGlassCard else LightGlassCard,
            if (isDark) DarkGlassCardBorder else LightGlassCardBorder,
            MaterialTheme.colorScheme.onSurfaceVariant
        )
        FileDropState.HOVER -> Triple(
            Primary500.copy(alpha = 0.1f),
            Primary400,
            Primary400
        )
        FileDropState.SELECTED -> Triple(
            Success500.copy(alpha = 0.1f),
            Success400,
            Success400
        )
        FileDropState.ERROR -> Triple(
            Error500.copy(alpha = 0.1f),
            Error400,
            Error400
        )
    }
    
    val scale by animateFloatAsState(
        targetValue = if (state == FileDropState.HOVER) 1.02f else 1f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
        label = "drop_area_scale"
    )
    
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(Dimensions.spacingMedium)
    ) {
        // Drop area
        GlassCard(
            backgroundColor = backgroundColor,
            borderColor = borderColor,
            modifier = Modifier
                .fillMaxWidth()
                .height(Dimensions.dropZoneHeight)
                .graphicsLayer {
                    scaleX = scale
                    scaleY = scale
                }
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(Dimensions.cardPadding),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(Dimensions.spacingMedium)
                ) {
                    val icon = when (state) {
                        FileDropState.IDLE -> androidx.compose.material.icons.Icons.Default.CloudUpload
                        FileDropState.HOVER -> androidx.compose.material.icons.Icons.Default.FileDownload
                        FileDropState.SELECTED -> androidx.compose.material.icons.Icons.Default.CheckCircle
                        FileDropState.ERROR -> androidx.compose.material.icons.Icons.Default.Error
                    }
                    
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        modifier = Modifier.size(Dimensions.iconXLarge),
                        tint = contentColor
                    )
                    
                    Text(
                        text = when (state) {
                            FileDropState.IDLE -> "Drop files here or click to select"
                            FileDropState.HOVER -> "Release to drop files"
                            FileDropState.SELECTED -> "${selectedFiles.size} files selected"
                            FileDropState.ERROR -> errorMessage ?: "Error occurred"
                        },
                        style = MaterialTheme.typography.bodyLarge.copy(
                            fontWeight = FontWeight.Medium
                        ),
                        color = contentColor,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )
                    
                    if (state == FileDropState.IDLE) {
                        Text(
                            text = buildString {
                                if (allowedExtensions.isNotEmpty()) {
                                    append("Allowed: ${allowedExtensions.joinToString(", ")}")
                                    if (maxFiles < Int.MAX_VALUE) append(" • ")
                                }
                                if (maxFiles < Int.MAX_VALUE) {
                                    append("Max $maxFiles files")
                                }
                            },
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                    }
                }
            }
        }
        
        // File list
        if (selectedFiles.isNotEmpty()) {
            FileList(
                files = selectedFiles,
                onRemoveFile = { file ->
                    onFilesDropped(selectedFiles - file)
                }
            )
        }
    }
}

@Composable
private fun FileList(
    files: List<java.io.File>,
    onRemoveFile: (java.io.File) -> Unit
) {
    GlassCard {
        Column(
            modifier = Modifier.padding(Dimensions.cardPadding),
            verticalArrangement = Arrangement.spacedBy(Dimensions.spacingSmall)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(Dimensions.spacingSmall)
            ) {
                Icon(
                    imageVector = androidx.compose.material.icons.Icons.Default.Folder,
                    contentDescription = null,
                    tint = Primary400,
                    modifier = Modifier.size(Dimensions.iconMedium)
                )
                Text(
                    text = "Selected Files (${files.size})",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.SemiBold
                    ),
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
            
            // File statistics
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Total size: ${formatFileSize(files.sumOf { it.length() })}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = files.firstOrNull()?.parent ?: "",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )
            }
            
            // Action buttons
            Row(
                horizontalArrangement = Arrangement.spacedBy(Dimensions.spacingSmall)
            ) {
                AnimatedGlassButton(
                    onClick = { /* TODO: Open folder in explorer */ },
                    variant = ButtonVariant.SECONDARY,
                    modifier = Modifier.height(Dimensions.smallButtonHeight)
                ) {
                    Icon(
                        imageVector = androidx.compose.material.icons.Icons.Default.Launch,
                        contentDescription = "Open in Explorer",
                        modifier = Modifier.size(Dimensions.iconSmall)
                    )
                    Spacer(Modifier.width(Dimensions.spacingXSmall))
                    Text("Open Folder", fontSize = 11.sp)
                }
                
                AnimatedGlassButton(
                    onClick = { onRemoveFile(files.first()) },
                    variant = ButtonVariant.SECONDARY,
                    modifier = Modifier.height(Dimensions.smallButtonHeight)
                ) {
                    Icon(
                        imageVector = androidx.compose.material.icons.Icons.Default.Clear,
                        contentDescription = "Remove files",
                        modifier = Modifier.size(Dimensions.iconSmall)
                    )
                    Spacer(Modifier.width(Dimensions.spacingXSmall))
                    Text("Remove", fontSize = 11.sp)
                }
            }
        }
    }
}

private fun formatFileSize(bytes: Long): String {
    return when {
        bytes >= 1024 * 1024 * 1024 -> "${bytes / (1024 * 1024 * 1024)} GB"
        bytes >= 1024 * 1024 -> "${bytes / (1024 * 1024)} MB"
        bytes >= 1024 -> "${bytes / 1024} KB"
        else -> "$bytes B"
    }
}
