// App.kt
package org.example.project

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.awt.ComposeWindow
import androidx.compose.ui.graphics.Color
import org.example.project.ui.screens.HomeScreen
import org.example.project.utils.LocalWindow

@Composable
fun App(window: ComposeWindow) {
    CompositionLocalProvider(LocalWindow provides window) {
        MaterialTheme {
            Surface(
                color = Color(0xFFF5F5F5)
            ) {
                HomeScreen(window)
            }
        }
    }
}