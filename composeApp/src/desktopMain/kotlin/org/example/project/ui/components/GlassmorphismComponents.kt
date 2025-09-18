package org.example.project.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
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

/**
 * Animated Glass Button - Button with glassmorphism effects and hover animations
 */
@Composable
fun AnimatedGlassButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    isPrimary: Boolean = true,
    content: @Composable RowScope.() -> Unit
) {
    var isPressed by remember { mutableStateOf(false) }
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.95f else 1f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
        label = "button_scale"
    )
    
    val backgroundColor = if (isPrimary) {
        if (enabled) Primary500 else Primary500.copy(alpha = 0.5f)
    } else {
        if (enabled) GlassCard else GlassCard.copy(alpha = 0.5f)
    }
    
    val borderColor = if (isPrimary) {
        Primary400.copy(alpha = 0.6f)
    } else {
        GlassCardBorder
    }

    Button(
        onClick = {
            if (enabled) {
                isPressed = true
                onClick()
                // Reset press state after a short delay - simplified approach
                // In a real app, use LaunchedEffect in the calling composable
                isPressed = false
            }
        },
        enabled = enabled,
        modifier = modifier
            .height(Dimensions.buttonHeight),
        colors = ButtonDefaults.buttonColors(
            containerColor = backgroundColor,
            contentColor = if (isPrimary) Color.White else DarkOnSurface,
            disabledContainerColor = backgroundColor.copy(alpha = 0.3f),
            disabledContentColor = if (isPrimary) Color.White.copy(alpha = 0.5f) else DarkOnSurface.copy(alpha = 0.5f)
        ),
        shape = RoundedCornerShape(Dimensions.radiusSmall),
        border = androidx.compose.foundation.BorderStroke(
            width = if (isPrimary) 0.dp else Dimensions.glassBorderWidth,
            color = borderColor
        ),
        elevation = ButtonDefaults.buttonElevation(
            defaultElevation = if (isPrimary) Dimensions.elevationSmall else Dimensions.elevationNone
        ),
        content = content
    )
}

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
    singleLine: Boolean = true
) {
    val isDark = !MaterialTheme.colorScheme.surface.luminance().let { it > 0.5f }
    
    val terminalAccent = if (isDark) DarkTerminalAccent else LightTerminalAccent
    val terminalText = if (isDark) DarkTerminalText else LightTerminalText
    val onSurface = if (isDark) DarkOnSurface else LightOnSurface
    
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = modifier,
        label = label?.let { { Text(it, color = terminalAccent) } },
        supportingText = supportingText?.let { { Text(it, color = terminalText.copy(alpha = 0.8f)) } },
        enabled = enabled,
        singleLine = singleLine,
        colors = OutlinedTextFieldDefaults.colors(
            focusedTextColor = onSurface,
            unfocusedTextColor = onSurface.copy(alpha = 0.9f),
            disabledTextColor = onSurface.copy(alpha = 0.6f),
            focusedBorderColor = terminalAccent,
            unfocusedBorderColor = terminalText.copy(alpha = 0.6f),
            disabledBorderColor = terminalText.copy(alpha = 0.3f),
            focusedLabelColor = terminalAccent,
            unfocusedLabelColor = terminalText.copy(alpha = 0.8f),
            disabledLabelColor = terminalText.copy(alpha = 0.5f),
            cursorColor = terminalAccent,
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
