package org.example.project.ui.theme

import androidx.compose.runtime.Composable

/**
 * @deprecated Use ENDEcodeTheme from Theme.kt instead
 */
@Deprecated("Use ENDEcodeTheme instead", ReplaceWith("ENDEcodeTheme(content = content)"))
@Composable
fun AppTheme(
    content: @Composable () -> Unit
) {
    ENDEcodeTheme(content = content)
}