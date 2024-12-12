// utils/LocalWindow.kt
package org.example.project.utils

import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.awt.ComposeWindow

val LocalWindow = staticCompositionLocalOf<ComposeWindow> { error("No window provided") }