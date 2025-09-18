package org.example.project

import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberWindowState
import org.example.project.ui.components.showInfo
import org.example.project.ui.screens.HomeScreen
import org.example.project.ui.theme.ENDEcodeTheme
import org.example.project.viewmodels.ThemeViewModel
import java.awt.Toolkit

fun main() {
    System.setProperty("sun.java2d.uiScale", "1.0")

    val scaleFactor = Toolkit.getDefaultToolkit().screenResolution / 96.0

    val windowWidth = (1024 / scaleFactor).dp
    val windowHeight = (768 / scaleFactor).dp

    application {
        Window(
            onCloseRequest = ::exitApplication,
            title = "ENDEcode Compose",
            state = rememberWindowState(size = DpSize(windowWidth, windowHeight))
        ) {
            window.rootPane.putClientProperty("apple.awt.contentScaleFactor", 1.0f)

            LaunchedEffect(Unit) {
                showInfo()
            }

            val themeViewModel = remember { ThemeViewModel() }
            
            ENDEcodeTheme(
                darkTheme = true // Default to dark theme for modern glassmorphism UI
            ) {
                androidx.compose.material3.Surface(
                    color = androidx.compose.material3.MaterialTheme.colorScheme.background,
                    modifier = androidx.compose.ui.Modifier.fillMaxSize()
                ) {
                    HomeScreen(window, themeViewModel)
                }
            }
        }
    }
}
