package org.example.project

import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberWindowState
import org.example.project.ui.components.showInfo
import org.example.project.ui.screens.HomeScreen
import org.example.project.ui.theme.AppTheme

fun main() = application {
    Window(
        onCloseRequest = ::exitApplication,
        title = "ENDEcode v2.0.0",
        state = rememberWindowState(size = DpSize(1024.dp, 768.dp))
    ) {
        LaunchedEffect(Unit) {
            showInfo()
        }

        AppTheme {
            HomeScreen(window)
        }
    }
}