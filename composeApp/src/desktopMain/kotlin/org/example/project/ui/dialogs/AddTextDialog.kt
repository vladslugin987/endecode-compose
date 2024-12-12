package org.example.project.ui.dialogs

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import org.example.project.ui.theme.Dimensions
import org.example.project.utils.ConsoleState

@Composable
fun AddTextDialog(
    onDismiss: () -> Unit,
    onConfirm: (text: String, photoNumber: Int) -> Unit
) {
    var textToAdd by remember { mutableStateOf("") }
    var photoNumber by remember { mutableStateOf("") }
    var showError by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                "Add Text to Photo",
                style = MaterialTheme.typography.titleLarge
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .padding(Dimensions.spacingMedium)
                    .fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(Dimensions.spacingSmall)
            ) {
                OutlinedTextField(
                    value = textToAdd,
                    onValueChange = {
                        textToAdd = it
                        showError = false
                    },
                    label = { Text("Text to add") },
                    supportingText = { Text("Text that will be added to the photo") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    isError = showError
                )

                OutlinedTextField(
                    value = photoNumber,
                    onValueChange = {
                        photoNumber = it.filter { char -> char.isDigit() }
                        showError = false
                    },
                    label = { Text("Photo number") },
                    supportingText = { Text("Enter the photo number (e.g., for photo_12.jpg enter 12)") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    isError = showError
                )

                if (showError) {
                    Text(
                        "Please enter valid data",
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier.padding(start = 4.dp)
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val number = photoNumber.toIntOrNull()
                    if (textToAdd.isNotEmpty() && number != null) {
                        onConfirm(textToAdd, number)
                    } else {
                        showError = true
                    }
                }
            ) {
                Text("Add Text")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}