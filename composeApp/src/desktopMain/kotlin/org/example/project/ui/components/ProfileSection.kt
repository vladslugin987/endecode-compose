package org.example.project.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import org.example.project.data.PreferencesManager
import org.example.project.ui.theme.Dimensions

@Composable
fun ProfileSection(
    modifier: Modifier = Modifier,
    onLoginClick: () -> Unit = {},
    onLogoutClick: () -> Unit = {}
) {
    // Check if user is logged in (prepare for future auth)
    val isLoggedIn = remember { PreferencesManager.userEmail != null }
    val userEmail = remember { PreferencesManager.userEmail }
    
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(
            modifier = Modifier.padding(Dimensions.cardPadding),
            verticalArrangement = Arrangement.spacedBy(Dimensions.spacingMedium)
        ) {
            if (isLoggedIn) {
                // Logged in state
                LoggedInProfile(
                    email = userEmail ?: "",
                    onLogoutClick = onLogoutClick
                )
            } else {
                // Not logged in state
                GuestProfile(onLoginClick = onLoginClick)
            }
        }
    }
}

@Composable
private fun LoggedInProfile(
    email: String,
    onLogoutClick: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(Dimensions.spacingMedium),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Profile avatar placeholder
        Surface(
            modifier = Modifier
                .size(48.dp)
                .clip(CircleShape),
            color = MaterialTheme.colorScheme.primary
        ) {
            Box(
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Person,
                    contentDescription = "Profile",
                    tint = MaterialTheme.colorScheme.onPrimary,
                    modifier = Modifier.size(Dimensions.iconLarge)
                )
            }
        }
        
        Column(
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = "Welcome back!",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = email,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        
        IconButton(onClick = onLogoutClick) {
            Icon(
                imageVector = Icons.Default.Logout,
                contentDescription = "Logout",
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun GuestProfile(
    onLoginClick: () -> Unit
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(Dimensions.spacingMedium)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(Dimensions.spacingMedium)
        ) {
            Icon(
                imageVector = Icons.Default.AccountCircle,
                contentDescription = null,
                modifier = Modifier.size(Dimensions.iconLarge),
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
            
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Profile",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "Sign in to sync your settings",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        
        Row(
            horizontalArrangement = Arrangement.spacedBy(Dimensions.spacingSmall)
        ) {
            OutlinedButton(
                onClick = onLoginClick,
                modifier = Modifier.weight(1f)
            ) {
                Icon(
                    imageVector = Icons.Default.Login,
                    contentDescription = null,
                    modifier = Modifier.size(Dimensions.iconSmall)
                )
                Spacer(Modifier.width(Dimensions.spacingSmall))
                Text("Sign In")
            }
            
            OutlinedButton(
                onClick = { /* Future: open registration */ },
                modifier = Modifier.weight(1f)
            ) {
                Icon(
                    imageVector = Icons.Default.PersonAdd,
                    contentDescription = null,
                    modifier = Modifier.size(Dimensions.iconSmall)
                )
                Spacer(Modifier.width(Dimensions.spacingSmall))
                Text("Sign Up")
            }
        }
    }
}

@Composable
fun CompactProfileButton(
    modifier: Modifier = Modifier,
    onClick: () -> Unit = {}
) {
    val isLoggedIn = remember { PreferencesManager.userEmail != null }
    
    IconButton(
        onClick = onClick,
        modifier = modifier
    ) {
        Icon(
            imageVector = if (isLoggedIn) Icons.Default.AccountCircle else Icons.Default.Login,
            contentDescription = if (isLoggedIn) "Profile" else "Sign in",
            tint = if (isLoggedIn) {
                MaterialTheme.colorScheme.primary
            } else {
                MaterialTheme.colorScheme.onSurfaceVariant
            }
        )
    }
}
