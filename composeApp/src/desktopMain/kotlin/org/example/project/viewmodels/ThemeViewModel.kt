package org.example.project.viewmodels

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import mu.KotlinLogging
import org.example.project.data.PreferencesManager
import org.example.project.ui.theme.ThemeMode
import java.awt.Toolkit

private val logger = KotlinLogging.logger {}

class ThemeViewModel {
    private val scope = CoroutineScope(Dispatchers.Main)
    
    // Current theme mode from preferences
    var themeMode by mutableStateOf(PreferencesManager.themeMode)
        private set
    
    // Computed dark theme state
    val isDarkTheme: Boolean
        get() = when (themeMode) {
            ThemeMode.LIGHT -> false
            ThemeMode.DARK -> true
            ThemeMode.SYSTEM -> isSystemDarkTheme()
        }
    
    init {
        logger.info { "ThemeViewModel initialized with theme mode: $themeMode" }
    }
    
    /**
     * Update the theme mode and save to preferences
     */
    fun updateThemeMode(newMode: ThemeMode) {
        if (newMode != themeMode) {
            themeMode = newMode
            scope.launch {
                try {
                    PreferencesManager.themeMode = newMode
                    logger.info { "Theme mode updated to: $newMode" }
                } catch (e: Exception) {
                    logger.error(e) { "Failed to save theme mode preference" }
                }
            }
        }
    }
    
    /**
     * Toggle between light and dark themes
     */
    fun toggleTheme() {
        val newMode = when (themeMode) {
            ThemeMode.LIGHT -> ThemeMode.DARK
            ThemeMode.DARK -> ThemeMode.LIGHT
            ThemeMode.SYSTEM -> ThemeMode.DARK // From system, go to dark
        }
        updateThemeMode(newMode)
    }
    
    /**
     * Detect system dark theme preference
     * Works on both macOS and Windows
     */
    private fun isSystemDarkTheme(): Boolean {
        return try {
            val osName = System.getProperty("os.name").lowercase()
            when {
                osName.contains("mac") -> isSystemDarkThemeMacOS()
                osName.contains("windows") -> isSystemDarkThemeWindows()
                else -> false // Default to light for other systems
            }
        } catch (e: Exception) {
            logger.warn(e) { "Failed to detect system theme, defaulting to light" }
            false
        }
    }
    
    /**
     * Detect dark theme on macOS
     */
    private fun isSystemDarkThemeMacOS(): Boolean {
        return try {
            val process = ProcessBuilder("defaults", "read", "-g", "AppleInterfaceStyle")
                .start()
            process.waitFor()
            val output = process.inputStream.bufferedReader().readText().trim()
            output.equals("Dark", ignoreCase = true)
        } catch (e: Exception) {
            logger.debug(e) { "Could not read macOS appearance setting" }
            false
        }
    }
    
    /**
     * Detect dark theme on Windows
     */
    private fun isSystemDarkThemeWindows(): Boolean {
        return try {
            val process = ProcessBuilder(
                "reg", "query", 
                "HKEY_CURRENT_USER\\Software\\Microsoft\\Windows\\CurrentVersion\\Themes\\Personalize",
                "/v", "AppsUseLightTheme"
            ).start()
            process.waitFor()
            val output = process.inputStream.bufferedReader().readText()
            // AppsUseLightTheme = 0 means dark theme
            output.contains("0x0")
        } catch (e: Exception) {
            logger.debug(e) { "Could not read Windows theme setting" }
            false
        }
    }
    
    /**
     * Get theme mode display name for UI
     */
    fun getThemeModeDisplayName(mode: ThemeMode): String {
        return when (mode) {
            ThemeMode.LIGHT -> "Light"
            ThemeMode.DARK -> "Dark"
            ThemeMode.SYSTEM -> "System"
        }
    }
    
    /**
     * Get all available theme modes
     */
    fun getAvailableThemeModes(): List<ThemeMode> {
        return ThemeMode.entries
    }
}
