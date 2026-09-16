package com.example.hisabbook.ui.inventory

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AttachMoney
import androidx.compose.material.icons.rounded.Category
import androidx.compose.material.icons.rounded.Inventory
import androidx.compose.material.icons.rounded.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.hisabbook.ui.theme.HisabBookTheme
import com.example.hisabbook.util.parseCleanDoubleOrDefault

@Composable
fun AddInventoryItemDialog(
    onDismissRequest: () -> Unit,
    onSaveItem: (name: String, unit: String, initialQuantity: Double, minimumStock: Double, purchasePrice: Double, onError: (String) -> Unit) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var unit by remember { mutableStateOf("pcs") }
    var initialQtyText by remember { mutableStateOf("0") }
    var minStockText by remember { mutableStateOf("10") }
    var purchasePriceText by remember { mutableStateOf("0") }

    var nameError by remember { mutableStateOf<String?>(null) }

    val commonUnits = listOf("pcs", "kg", "meter", "feet", "sq.ft", "sq.m", "box", "ltr")

    AlertDialog(
        onDismissRequest = onDismissRequest,
        title = { Text("Add Inventory Item", style = MaterialTheme.typography.headlineSmall) },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Name
                OutlinedTextField(
                    value = name,
                    onValueChange = {
                        name = it
                        if (it.isNotBlank()) nameError = null
                    },
                    label = { Text("Item Name *") },
                    leadingIcon = { Icon(Icons.Rounded.Inventory, contentDescription = null) },
                    isError = nameError != null,
                    supportingText = nameError?.let { { Text(it) } },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                    modifier = Modifier.fillMaxWidth()
                )

                // Unit Selector
                Text("Select Unit", style = MaterialTheme.typography.labelMedium)
                // Replaced FlowRow with a horizontally scrollable Row to prevent infinite layout measurement loops in Compose Preview inside AlertDialog
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    commonUnits.forEach { u ->
                        FilterChip(
                            selected = unit.equals(u, ignoreCase = true),
                            onClick = { unit = u },
                            label = { Text(u) }
                        )
                    }
                }

                // Initial Quantity
                OutlinedTextField(
                    value = initialQtyText,
                    onValueChange = { initialQtyText = it },
                    label = { Text("Initial Stock Quantity") },
                    leadingIcon = { Icon(Icons.Rounded.Category, contentDescription = null) },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Decimal,
                        imeAction = ImeAction.Next
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                // Min Stock Level (Low stock threshold)
                OutlinedTextField(
                    value = minStockText,
                    onValueChange = { minStockText = it },
                    label = { Text("Low Stock Threshold (Min Stock)") },
                    leadingIcon = { Icon(Icons.Rounded.Warning, contentDescription = null) },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Decimal,
                        imeAction = ImeAction.Next
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                // Purchase Price
                OutlinedTextField(
                    value = purchasePriceText,
                    onValueChange = { purchasePriceText = it },
                    label = { Text("Purchase Price per Unit (₹)") },
                    leadingIcon = { Icon(Icons.Rounded.AttachMoney, contentDescription = null) },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Decimal,
                        imeAction = ImeAction.Done
                    ),
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    nameError = null
                    val trimmedName = name.trim()
                    if (trimmedName.isBlank()) {
                        nameError = "Item name is required"
                    } else {
                        val initQty = initialQtyText.parseCleanDoubleOrDefault(0.0)
                        val minStock = minStockText.parseCleanDoubleOrDefault(0.0)
                        val price = purchasePriceText.parseCleanDoubleOrDefault(0.0)
                        onSaveItem(trimmedName, unit, initQty, minStock, price) { err ->
                            nameError = err
                        }
                    }
                }
            ) {
                Text("Save Item")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismissRequest) {
                Text("Cancel")
            }
        }
    )
}

@Preview(showBackground = true)
@Composable
fun AddInventoryItemDialogPreview() {
    HisabBookTheme {
        AddInventoryItemDialog(
            onDismissRequest = {},
            onSaveItem = { _, _, _, _, _, _ -> }
        )
    }
}

