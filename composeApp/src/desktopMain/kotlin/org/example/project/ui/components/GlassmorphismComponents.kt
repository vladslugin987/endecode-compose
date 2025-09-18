package org.example.project.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import org.example.project.ui.theme.*

/**
 * Glass Card - Modern glassmorphism card component
 */
@Composable
fun GlassCard(
    modifier: Modifier = Modifier,
    borderRadius: androidx.compose.ui.unit.Dp = Dimensions.radiusMedium,
    borderColor: Color = GlassCardBorder,
    backgroundColor: Color = GlassCard,
    elevation: androidx.compose.ui.unit.Dp = Dimensions.glassElevation,
    content: @Composable BoxScope.() -> Unit
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(borderRadius))
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        backgroundColor.copy(alpha = 0.1f),
                        backgroundColor.copy(alpha = 0.05f)
                    )
                )
            )
            .border(
                width = Dimensions.glassBorderWidth,
                color = borderColor,
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
 * Terminal-styled text field for modern console look
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
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = modifier,
        label = label?.let { { Text(it, color = TerminalAccent) } },
        supportingText = supportingText?.let { { Text(it, color = TerminalText) } },
        enabled = enabled,
        singleLine = singleLine,
        colors = OutlinedTextFieldDefaults.colors(
            focusedTextColor = DarkOnSurface,
            unfocusedTextColor = DarkOnSurface,
            focusedBorderColor = TerminalAccent,
            unfocusedBorderColor = TerminalText.copy(alpha = 0.5f),
            focusedLabelColor = TerminalAccent,
            unfocusedLabelColor = TerminalText,
            cursorColor = TerminalAccent,
            focusedContainerColor = TerminalBackground.copy(alpha = 0.8f),
            unfocusedContainerColor = TerminalBackground.copy(alpha = 0.6f)
        ),
        shape = RoundedCornerShape(Dimensions.radiusSmall)
    )
}

/**
 * Gradient background for the main app
 */
@Composable
fun GradientBackground(
    modifier: Modifier = Modifier,
    content: @Composable BoxScope.() -> Unit
) {
    Box(
        modifier = modifier
            .background(
                brush = Brush.radialGradient(
                    colors = listOf(
                        GlassBackground,
                        GlassSurface.copy(alpha = 0.3f),
                        GlassBackground
                    ),
                    radius = 1200f
                )
            ),
        content = content
    )
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
