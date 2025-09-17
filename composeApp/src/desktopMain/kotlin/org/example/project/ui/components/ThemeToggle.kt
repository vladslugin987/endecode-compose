package org.example.project.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Computer
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.Login
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import org.example.project.ui.theme.ThemeMode
import org.example.project.viewmodels.ThemeViewModel

@Composable
fun ThemeToggle(
    themeViewModel: ThemeViewModel,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }
    
    Box(modifier = modifier) {
        IconButton(
            onClick = { expanded = true }
        ) {
            Icon(
                imageVector = when (themeViewModel.themeMode) {
                    ThemeMode.LIGHT -> Icons.Default.LightMode
                    ThemeMode.DARK -> Icons.Default.DarkMode
                    ThemeMode.SYSTEM -> Icons.Default.Computer
                },
                contentDescription = "Theme settings",
                tint = MaterialTheme.colorScheme.onSurface
            )
        }
        
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            themeViewModel.getAvailableThemeModes().forEach { mode ->
                DropdownMenuItem(
                    text = {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = when (mode) {
                                    ThemeMode.LIGHT -> Icons.Default.LightMode
                                    ThemeMode.DARK -> Icons.Default.DarkMode
                                    ThemeMode.SYSTEM -> Icons.Default.Computer
                                },
                                contentDescription = null,
                                modifier = Modifier.size(20.dp)
                            )
                            Text(themeViewModel.getThemeModeDisplayName(mode))
                            if (mode == themeViewModel.themeMode) {
                                Spacer(Modifier.weight(1f))
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = "Selected",
                                    modifier = Modifier.size(16.dp),
                                    tint = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                    },
                    onClick = {
                        themeViewModel.updateThemeMode(mode)
                        expanded = false
                    }
                )
            }
        }
    }
}

@Composable
fun QuickThemeToggle(
    themeViewModel: ThemeViewModel,
    modifier: Modifier = Modifier
) {
    IconButton(
        onClick = { themeViewModel.toggleTheme() },
        modifier = modifier
    ) {
        Icon(
            imageVector = if (themeViewModel.isDarkTheme) {
                Icons.Default.LightMode
            } else {
                Icons.Default.DarkMode
            },
            contentDescription = if (themeViewModel.isDarkTheme) {
                "Switch to light theme"
            } else {
                "Switch to dark theme"
            },
            tint = MaterialTheme.colorScheme.onSurface
        )
    }
}

@Composable
fun ThemeCard(
    themeViewModel: ThemeViewModel,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Appearance",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                ThemeToggle(themeViewModel)
            }
            
            Text(
                text = "Current: ${themeViewModel.getThemeModeDisplayName(themeViewModel.themeMode)}",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            
            if (themeViewModel.themeMode == ThemeMode.SYSTEM) {
                Text(
                    text = "Following system preference: ${if (themeViewModel.isDarkTheme) "Dark" else "Light"}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
