package org.example.project.data

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import mu.KotlinLogging
import org.example.project.ui.theme.ThemeMode
import java.io.File
import java.util.*

private val logger = KotlinLogging.logger {}

/**
 * Manages application preferences using Java Properties
 * Stores settings in user home directory for cross-platform compatibility
 */
object PreferencesManager {
    private const val PREFERENCES_FILE = "endecode.properties"
    private const val KEY_THEME_MODE = "theme_mode"
    private const val KEY_AUTO_CLEAR_CONSOLE = "auto_clear_console"
    private const val KEY_LAST_SELECTED_PATH = "last_selected_path"
    
    // Future auth keys
    private const val KEY_USER_EMAIL = "user_email"
    private const val KEY_REMEMBER_LOGIN = "remember_login"
    private const val KEY_USER_TOKEN = "user_token"
    
    private val preferencesFile: File by lazy {
        val userHome = System.getProperty("user.home")
        val configDir = File(userHome, ".endecode")
        if (!configDir.exists()) {
            configDir.mkdirs()
        }
        File(configDir, PREFERENCES_FILE)
    }
    
    private val properties = Properties()
    
    init {
        loadPreferences()
    }
    
    private fun loadPreferences() {
        try {
            if (preferencesFile.exists()) {
                preferencesFile.inputStream().use { input ->
                    properties.load(input)
                }
                logger.info { "Preferences loaded from ${preferencesFile.absolutePath}" }
            } else {
                logger.info { "No preferences file found, using defaults" }
            }
        } catch (e: Exception) {
            logger.error(e) { "Error loading preferences" }
        }
    }
    
    private suspend fun savePreferences() = withContext(Dispatchers.IO) {
        try {
            preferencesFile.outputStream().use { output ->
                properties.store(output, "ENDEcode Application Preferences")
            }
            logger.info { "Preferences saved to ${preferencesFile.absolutePath}" }
        } catch (e: Exception) {
            logger.error(e) { "Error saving preferences" }
        }
    }
    
    // Theme preferences
    var themeMode: ThemeMode
        get() = try {
            ThemeMode.valueOf(properties.getProperty(KEY_THEME_MODE, ThemeMode.SYSTEM.name))
        } catch (e: IllegalArgumentException) {
            ThemeMode.SYSTEM
        }
        set(value) {
            properties.setProperty(KEY_THEME_MODE, value.name)
            kotlin.runCatching { kotlinx.coroutines.runBlocking { savePreferences() } }
        }
    
    var autoClearConsole: Boolean
        get() = properties.getProperty(KEY_AUTO_CLEAR_CONSOLE, "false").toBoolean()
        set(value) {
            properties.setProperty(KEY_AUTO_CLEAR_CONSOLE, value.toString())
            kotlin.runCatching { kotlinx.coroutines.runBlocking { savePreferences() } }
        }
    
    var lastSelectedPath: String?
        get() = properties.getProperty(KEY_LAST_SELECTED_PATH)
        set(value) {
            if (value != null) {
                properties.setProperty(KEY_LAST_SELECTED_PATH, value)
            } else {
                properties.remove(KEY_LAST_SELECTED_PATH)
            }
            kotlin.runCatching { kotlinx.coroutines.runBlocking { savePreferences() } }
        }
    
    // Future auth preferences (prepared but not used yet)
    var userEmail: String?
        get() = properties.getProperty(KEY_USER_EMAIL)
        set(value) {
            if (value != null) {
                properties.setProperty(KEY_USER_EMAIL, value)
            } else {
                properties.remove(KEY_USER_EMAIL)
            }
            kotlin.runCatching { kotlinx.coroutines.runBlocking { savePreferences() } }
        }
    
    var rememberLogin: Boolean
        get() = properties.getProperty(KEY_REMEMBER_LOGIN, "false").toBoolean()
        set(value) {
            properties.setProperty(KEY_REMEMBER_LOGIN, value.toString())
            kotlin.runCatching { kotlinx.coroutines.runBlocking { savePreferences() } }
        }
    
    var userToken: String?
        get() = properties.getProperty(KEY_USER_TOKEN)
        set(value) {
            if (value != null) {
                properties.setProperty(KEY_USER_TOKEN, value)
            } else {
                properties.remove(KEY_USER_TOKEN)
            }
            kotlin.runCatching { kotlinx.coroutines.runBlocking { savePreferences() } }
        }
    
    /**
     * Clear all auth-related preferences (for logout)
     */
    fun clearAuthPreferences() {
        properties.remove(KEY_USER_EMAIL)
        properties.remove(KEY_USER_TOKEN)
        properties.setProperty(KEY_REMEMBER_LOGIN, "false")
        kotlin.runCatching { kotlinx.coroutines.runBlocking { savePreferences() } }
    }
    
    /**
     * Get all current preferences as a map (for debugging)
     */
    fun getAllPreferences(): Map<String, String> {
        return properties.stringPropertyNames().associateWith { key ->
            properties.getProperty(key)
        }
    }
}
