package org.example.project.ui.theme

import androidx.compose.ui.graphics.Color

// Modern Glassmorphism Color Palette

// Primary colors - Cyberpunk Blue/Purple gradient
val Primary50 = Color(0xFFF0F4FF)
val Primary100 = Color(0xFFDBE4FF)
val Primary200 = Color(0xFFBDD0FF)
val Primary300 = Color(0xFF91B4FF)
val Primary400 = Color(0xFF5E8EFF)
val Primary500 = Color(0xFF3B7EFF)
val Primary600 = Color(0xFF2563EB)
val Primary700 = Color(0xFF1D4ED8)
val Primary800 = Color(0xFF1E40AF)
val Primary900 = Color(0xFF1E3A8A)

// Secondary colors - Modern Violet/Purple
val Secondary50 = Color(0xFFFDF4FF)
val Secondary100 = Color(0xFFFAE8FF)
val Secondary200 = Color(0xFFF3D2FF)
val Secondary300 = Color(0xFFE9B0FF)
val Secondary400 = Color(0xFFDA82FF)
val Secondary500 = Color(0xFFC555FF)
val Secondary600 = Color(0xFFAD37E8)
val Secondary700 = Color(0xFF9025C4)
val Secondary800 = Color(0xFF7C209D)
val Secondary900 = Color(0xFF651C7F)

// Accent colors - Cyan/Teal for highlights
val Accent50 = Color(0xFFECFDF5)
val Accent100 = Color(0xFFD1FAE5)
val Accent200 = Color(0xFFA7F3D0)
val Accent300 = Color(0xFF6EE7B7)
val Accent400 = Color(0xFF34D399)
val Accent500 = Color(0xFF10B981)
val Accent600 = Color(0xFF059669)
val Accent700 = Color(0xFF047857)
val Accent800 = Color(0xFF065F46)
val Accent900 = Color(0xFF064E3B)

// Neutral colors - Modern grays
val Neutral50 = Color(0xFFFAFAFB)
val Neutral100 = Color(0xFFF4F4F6)
val Neutral200 = Color(0xFFE5E7EB)
val Neutral300 = Color(0xFFD1D5DB)
val Neutral400 = Color(0xFF9CA3AF)
val Neutral500 = Color(0xFF6B7280)
val Neutral600 = Color(0xFF4B5563)
val Neutral700 = Color(0xFF374151)
val Neutral800 = Color(0xFF1F2937)
val Neutral900 = Color(0xFF111827)

// Dark theme glassmorphism colors - More beautiful gradient
val DarkGlassSurface = Color(0xFF1A1B2E)
val DarkGlassSurfaceVariant = Color(0xFF2A2B4E) 
val DarkGlassBackground = Color(0xFF0F0F1C)
val DarkGlassCard = Color(0x80191A2D) // Semi-transparent card
val DarkGlassCardBorder = Color(0x40FFFFFF) // Subtle white border
val DarkGlassHighlight = Color(0x20FFFFFF) // Glass highlight effect

// Light theme glassmorphism colors - Very light and airy
val LightGlassSurface = Color(0xFFFBFCFF)
val LightGlassSurfaceVariant = Color(0xFFF5F7FA)
val LightGlassBackground = Color(0xFFFFFFFF)
val LightGlassCard = Color(0x80F8F9FF) // Semi-transparent card
val LightGlassCardBorder = Color(0x40E5E7EB) // Subtle border
val LightGlassHighlight = Color(0x20FFFFFF) // Glass highlight effect

// Backwards compatibility
val GlassSurface = DarkGlassSurface
val GlassSurfaceVariant = DarkGlassSurfaceVariant
val GlassBackground = DarkGlassBackground
val GlassCard = DarkGlassCard
val GlassCardBorder = DarkGlassCardBorder
val GlassHighlight = DarkGlassHighlight

// Status colors
val Error50 = Color(0xFFFEF2F2)
val Error200 = Color(0xFFFECACA)
val Error500 = Color(0xFFEF4444)
val Error600 = Color(0xFFDC2626)

val Success50 = Color(0xFFF0FDF4)
val Success200 = Color(0xFFBBF7D0)
val Success500 = Color(0xFF22C55E)
val Success600 = Color(0xFF16A34A)

val Warning50 = Color(0xFFFFFBEB)
val Warning200 = Color(0xFFFDE68A)
val Warning500 = Color(0xFFF59E0B)
val Warning600 = Color(0xFFD97706)

// Terminal colors for console - Dark theme
val DarkTerminalBackground = Color(0xFF0D1117)
val DarkTerminalText = Color(0xFF7C7C7C)
val DarkTerminalAccent = Color(0xFF58A6FF)
val DarkTerminalSuccess = Color(0xFF3FB950)
val DarkTerminalWarning = Color(0xFFD29922)
val DarkTerminalError = Color(0xFFF85149)

// Terminal colors for console - Light theme
val LightTerminalBackground = Color(0xFFFAFBFF)
val LightTerminalText = Color(0xFF4A5568)
val LightTerminalAccent = Color(0xFF3182CE)
val LightTerminalSuccess = Color(0xFF38A169)
val LightTerminalWarning = Color(0xFFD69E2E)
val LightTerminalError = Color(0xFFE53E3E)

// Backwards compatibility - defaults to dark
val TerminalBackground = DarkTerminalBackground
val TerminalText = DarkTerminalText
val TerminalAccent = DarkTerminalAccent
val TerminalSuccess = DarkTerminalSuccess
val TerminalWarning = DarkTerminalWarning
val TerminalError = DarkTerminalError

// Theme-specific main colors
val DarkSurface = DarkGlassSurface
val DarkSurfaceVariant = DarkGlassSurfaceVariant
val DarkBackground = DarkGlassBackground
val DarkOnSurface = Color(0xFFE3E8F0)
val DarkOnBackground = Color(0xFFE3E8F0)
val DarkOutline = Color(0xFF404040)

val LightSurface = LightGlassSurface
val LightSurfaceVariant = LightGlassSurfaceVariant
val LightBackground = LightGlassBackground
val LightOnSurface = Color(0xFF1A202C)
val LightOnBackground = Color(0xFF1A202C)
val LightOutline = Color(0xFFE2E8F0)
