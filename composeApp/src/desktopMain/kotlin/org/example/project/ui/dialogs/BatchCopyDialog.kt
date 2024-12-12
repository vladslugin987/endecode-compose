package org.example.project.ui.dialogs

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import org.example.project.ui.theme.Dimensions

@Composable
fun BatchCopyDialog(
    onDismiss: () -> Unit,
    onConfirm: (
        copies: Int,
        baseText: String,
        addSwap: Boolean,
        addWatermark: Boolean,
        createZip: Boolean,
        watermarkText: String?,
        photoNumber: Int?
    ) -> Unit
) {
    var numberOfCopies by remember { mutableStateOf("") }
    var baseText by remember { mutableStateOf("ORDER") }
    var addSwapEncoding by remember { mutableStateOf(false) }
    var addVisibleWatermark by remember { mutableStateOf(false) }
    var createZip by remember { mutableStateOf(false) }
    var watermarkText by remember { mutableStateOf("") }
    var photoNumber by remember { mutableStateOf("") }
    var useOrderNumberAsPhotoNumber by remember { mutableStateOf(true) }
    var showError by remember { mutableStateOf(false) }

    AlertDialog(
        modifier = Modifier
            .width(600.dp)
            .heightIn(max = 700.dp),
        onDismissRequest = onDismiss,
        title = {
            Text(
                "Batch Copy Settings",
                style = MaterialTheme.typography.titleLarge
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .padding(Dimensions.spacingMedium)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(Dimensions.spacingMedium)
            ) {
                // Main settings
                ElevatedCard {
                    Column(
                        modifier = Modifier
                            .padding(Dimensions.spacingMedium)
                            .fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(Dimensions.spacingSmall)
                    ) {
                        Text("Basic Settings", style = MaterialTheme.typography.titleMedium)

                        OutlinedTextField(
                            value = numberOfCopies,
                            onValueChange = {
                                numberOfCopies = it.filter { char -> char.isDigit() }
                                showError = false
                            },
                            label = { Text("Number of copies") },
                            isError = showError,
                            supportingText = { Text("How many copies to create") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )

                        OutlinedTextField(
                            value = baseText,
                            onValueChange = { baseText = it },
                            label = { Text("Text base for encoding") },
                            supportingText = {
                                Text("Example: ORDER 001 (number will auto-increment for each copy)")
                            },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )
                    }
                }

                // Additional options
                ElevatedCard {
                    Column(
                        modifier = Modifier
                            .padding(Dimensions.spacingMedium)
                            .fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(Dimensions.spacingSmall)
                    ) {
                        Text("Additional Options", style = MaterialTheme.typography.titleMedium)

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Checkbox(
                                checked = addSwapEncoding,
                                onCheckedChange = { addSwapEncoding = it }
                            )
                            Text(
                                "Additional Swap File Encoding (e.g., swap 003 with 103)",
                                modifier = Modifier.padding(start = 8.dp)
                            )
                        }

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Checkbox(
                                checked = addVisibleWatermark,
                                onCheckedChange = { addVisibleWatermark = it }
                            )
                            Text(
                                "Add visible watermark to photos",
                                modifier = Modifier.padding(start = 8.dp)
                            )
                        }

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Checkbox(
                                checked = createZip,
                                onCheckedChange = { createZip = it }
                            )
                            Text(
                                "Create ZIP archives (No compression, No password)",
                                modifier = Modifier.padding(start = 8.dp)
                            )
                        }
                    }
                }

                // Watermark settings
                if (addVisibleWatermark) {
                    ElevatedCard {
                        Column(
                            modifier = Modifier
                                .padding(Dimensions.spacingMedium)
                                .fillMaxWidth(),
                            verticalArrangement = Arrangement.spacedBy(Dimensions.spacingSmall)
                        ) {
                            Text("Watermark Settings", style = MaterialTheme.typography.titleMedium)

                            OutlinedTextField(
                                value = watermarkText,
                                onValueChange = { watermarkText = it },
                                label = { Text("Watermark text") },
                                supportingText = { Text("Leave empty to use folder number") },
                                modifier = Modifier.fillMaxWidth(),
                                singleLine = true
                            )

                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Checkbox(
                                    checked = useOrderNumberAsPhotoNumber,
                                    onCheckedChange = { useOrderNumberAsPhotoNumber = it }
                                )
                                Text(
                                    "Use order number as photo number",
                                    modifier = Modifier.padding(start = 8.dp)
                                )
                            }

                            if (!useOrderNumberAsPhotoNumber) {
                                OutlinedTextField(
                                    value = photoNumber,
                                    onValueChange = { photoNumber = it.filter { char -> char.isDigit() } },
                                    label = { Text("Photo number") },
                                    supportingText = { Text("Enter specific photo number for watermark") },
                                    modifier = Modifier.fillMaxWidth(),
                                    singleLine = true
                                )
                            }
                        }
                    }
                }

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
                    val copies = numberOfCopies.toIntOrNull()
                    if (copies != null && copies > 0 && baseText.isNotBlank()) {
                        onConfirm(
                            copies,
                            baseText,
                            addSwapEncoding,
                            addVisibleWatermark,
                            createZip,
                            if (addVisibleWatermark && watermarkText.isNotEmpty()) watermarkText else null,
                            if (addVisibleWatermark && !useOrderNumberAsPhotoNumber && photoNumber.isNotEmpty())
                                photoNumber.toIntOrNull()
                            else null
                        )
                    } else {
                        showError = true
                    }
                }
            ) {
                Text("Start")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}