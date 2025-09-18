package org.example.project.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.unit.dp
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

// Theme mode enum
enum class ThemeMode {
    LIGHT, DARK, SYSTEM
}

// Custom theme data for additional colors
data class CustomColors(
    val success: ColorFamily,
    val warning: ColorFamily,
    val neutral: ColorFamily
)

data class ColorFamily(
    val color: androidx.compose.ui.graphics.Color,
    val onColor: androidx.compose.ui.graphics.Color,
    val container: androidx.compose.ui.graphics.Color,
    val onContainer: androidx.compose.ui.graphics.Color,
)

// Light theme color scheme - Very bright and clean
private val LightColorScheme = lightColorScheme(
    primary = Primary500,
    onPrimary = androidx.compose.ui.graphics.Color.White,
    primaryContainer = Primary50,
    onPrimaryContainer = Primary900,
    
    secondary = Secondary600,
    onSecondary = androidx.compose.ui.graphics.Color.White,
    secondaryContainer = Secondary50,
    onSecondaryContainer = Secondary800,
    
    tertiary = Accent600,
    onTertiary = androidx.compose.ui.graphics.Color.White,
    tertiaryContainer = Accent50,
    onTertiaryContainer = Accent800,
    
    error = Error600,
    onError = androidx.compose.ui.graphics.Color.White,
    errorContainer = Error50,
    onErrorContainer = Error600,
    
    background = LightGlassBackground,
    onBackground = LightOnBackground,
    surface = LightGlassSurface,
    onSurface = LightOnSurface,
    surfaceVariant = LightGlassSurfaceVariant,
    onSurfaceVariant = Neutral600,
    
    outline = LightOutline,
    scrim = androidx.compose.ui.graphics.Color.Black,
    
    inverseSurface = Neutral800,
    inverseOnSurface = Neutral100,
    inversePrimary = Primary300
)

// Dark glassmorphism theme color scheme - Beautiful gradient background
private val DarkColorScheme = darkColorScheme(
    primary = Primary300,
    onPrimary = Neutral900,
    primaryContainer = Primary700,
    onPrimaryContainer = Primary100,
    
    secondary = Secondary400,
    onSecondary = Neutral900,
    secondaryContainer = Secondary800,
    onSecondaryContainer = Secondary200,
    
    tertiary = Accent400,
    onTertiary = Neutral900,
    tertiaryContainer = Accent800,
    onTertiaryContainer = Accent200,
    
    error = Error500,
    onError = androidx.compose.ui.graphics.Color.Black,
    errorContainer = androidx.compose.ui.graphics.Color(0xFF5F1A1A),
    onErrorContainer = Error200,
    
    background = DarkGlassBackground,
    onBackground = DarkOnBackground,
    surface = DarkGlassSurface,
    onSurface = DarkOnSurface,
    surfaceVariant = DarkGlassSurfaceVariant,
    onSurfaceVariant = Neutral300,
    
    outline = DarkOutline,
    scrim = androidx.compose.ui.graphics.Color.Black,
    
    inverseSurface = Neutral100,
    inverseOnSurface = Neutral800,
    inversePrimary = Primary600
)

// Custom colors for light theme
private val LightCustomColors = CustomColors(
    success = ColorFamily(
        color = Success600,
        onColor = androidx.compose.ui.graphics.Color.White,
        container = Success50,
        onContainer = Success600
    ),
    warning = ColorFamily(
        color = Warning600,
        onColor = androidx.compose.ui.graphics.Color.White,
        container = Warning50,
        onContainer = Warning600
    ),
    neutral = ColorFamily(
        color = Neutral600,
        onColor = androidx.compose.ui.graphics.Color.White,
        container = Neutral100,
        onContainer = Neutral800
    )
)

// Custom colors for dark theme
private val DarkCustomColors = CustomColors(
    success = ColorFamily(
        color = Success500,
        onColor = androidx.compose.ui.graphics.Color.Black,
        container = androidx.compose.ui.graphics.Color(0xFF0F3A1F),
        onContainer = Success200
    ),
    warning = ColorFamily(
        color = Warning500,
        onColor = androidx.compose.ui.graphics.Color.Black,
        container = androidx.compose.ui.graphics.Color(0xFF3A2B0F),
        onContainer = Warning200
    ),
    neutral = ColorFamily(
        color = Neutral400,
        onColor = androidx.compose.ui.graphics.Color.Black,
        container = Neutral800,
        onContainer = Neutral200
    )
)

// Typography
private val AppTypography = Typography(
    displayLarge = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Normal,
        fontSize = 57.sp,
        lineHeight = 64.sp,
        letterSpacing = (-0.25).sp,
    ),
    displayMedium = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Normal,
        fontSize = 45.sp,
        lineHeight = 52.sp,
        letterSpacing = 0.sp,
    ),
    displaySmall = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Normal,
        fontSize = 36.sp,
        lineHeight = 44.sp,
        letterSpacing = 0.sp,
    ),
    headlineLarge = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Normal,
        fontSize = 32.sp,
        lineHeight = 40.sp,
        letterSpacing = 0.sp,
    ),
    headlineMedium = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Normal,
        fontSize = 28.sp,
        lineHeight = 36.sp,
        letterSpacing = 0.sp,
    ),
    headlineSmall = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Normal,
        fontSize = 24.sp,
        lineHeight = 32.sp,
        letterSpacing = 0.sp,
    ),
    titleLarge = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Normal,
        fontSize = 22.sp,
        lineHeight = 28.sp,
        letterSpacing = 0.sp,
    ),
    titleMedium = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Medium,
        fontSize = 16.sp,
        lineHeight = 24.sp,
        letterSpacing = 0.15.sp,
    ),
    titleSmall = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Medium,
        fontSize = 14.sp,
        lineHeight = 20.sp,
        letterSpacing = 0.1.sp,
    ),
    bodyLarge = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp,
        lineHeight = 24.sp,
        letterSpacing = 0.5.sp,
    ),
    bodyMedium = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Normal,
        fontSize = 14.sp,
        lineHeight = 20.sp,
        letterSpacing = 0.25.sp,
    ),
    bodySmall = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Normal,
        fontSize = 12.sp,
        lineHeight = 16.sp,
        letterSpacing = 0.4.sp,
    ),
    labelLarge = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Medium,
        fontSize = 14.sp,
        lineHeight = 20.sp,
        letterSpacing = 0.1.sp,
    ),
    labelMedium = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Medium,
        fontSize = 12.sp,
        lineHeight = 16.sp,
        letterSpacing = 0.5.sp,
    ),
    labelSmall = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Medium,
        fontSize = 11.sp,
        lineHeight = 16.sp,
        letterSpacing = 0.5.sp,
    ),
)

// CompositionLocal for custom colors
val LocalCustomColors = staticCompositionLocalOf { LightCustomColors }

// Extension property to access custom colors
val MaterialTheme.customColors: CustomColors
    @Composable get() = LocalCustomColors.current

// Modern glassmorphism shapes
private val AppShapes = Shapes(
    extraSmall = RoundedCornerShape(Dimensions.radiusXSmall),
    small = RoundedCornerShape(Dimensions.radiusSmall),
    medium = RoundedCornerShape(Dimensions.radiusMedium),
    large = RoundedCornerShape(Dimensions.radiusLarge),
    extraLarge = RoundedCornerShape(Dimensions.radiusXLarge)
)

@Composable
fun ENDEcodeTheme(
    darkTheme: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme
    val customColors = if (darkTheme) DarkCustomColors else LightCustomColors

    CompositionLocalProvider(LocalCustomColors provides customColors) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = AppTypography,
            shapes = AppShapes,
            content = content
        )
    }
}
