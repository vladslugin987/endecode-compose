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

// Dark theme glassmorphism colors - Enhanced visibility and beauty
val DarkGlassSurface = Color(0xFF1A1B2E)
val DarkGlassSurfaceVariant = Color(0xFF252640) 
val DarkGlassBackground = Color(0xFF0F1419)
val DarkGlassCard = Color(0xDD1A1B2E) // More prominent card
val DarkGlassCardBorder = Color(0x80FFFFFF) // Brighter border
val DarkGlassHighlight = Color(0x40FFFFFF) // Enhanced glass highlight effect

// Light theme glassmorphism colors - Enhanced contrast and elegance
val LightGlassSurface = Color(0xFFF5F7FA)
val LightGlassSurfaceVariant = Color(0xFFE2E8F0)
val LightGlassBackground = Color(0xFFFAFBFC)
val LightGlassCard = Color(0xEAF5F7FA) // More prominent card
val LightGlassCardBorder = Color(0x90CBD5E0) // More defined border
val LightGlassHighlight = Color(0x50FFFFFF) // Enhanced glass highlight effect

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
val Error400 = Color(0xFFF87171)
val Error500 = Color(0xFFEF4444)
val Error600 = Color(0xFFDC2626)

val Success50 = Color(0xFFF0FDF4)
val Success200 = Color(0xFFBBF7D0)
val Success400 = Color(0xFF4ADE80)
val Success500 = Color(0xFF22C55E)
val Success600 = Color(0xFF16A34A)

val Warning50 = Color(0xFFFFFBEB)
val Warning200 = Color(0xFFFDE68A)
val Warning500 = Color(0xFFF59E0B)
val Warning600 = Color(0xFFD97706)

// Terminal colors for console - Dark theme
val DarkTerminalBackground = Color(0xFF1A1D29)
val DarkTerminalText = Color(0xFFADB5BD)
val DarkTerminalAccent = Color(0xFF58A6FF)
val DarkTerminalSuccess = Color(0xFF3FB950)
val DarkTerminalWarning = Color(0xFFD29922)
val DarkTerminalError = Color(0xFFF85149)

// Terminal colors for console - Light theme
val LightTerminalBackground = Color(0xFFF1F3F4)
val LightTerminalText = Color(0xFF343A40)
val LightTerminalAccent = Color(0xFF0D6EFD)
val LightTerminalSuccess = Color(0xFF198754)
val LightTerminalWarning = Color(0xFFFFC107)
val LightTerminalError = Color(0xFFDC3545)

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
val LightOnSurface = Color(0xFF212529)
val LightOnBackground = Color(0xFF212529)
val LightOutline = Color(0xFFCED4DA)
