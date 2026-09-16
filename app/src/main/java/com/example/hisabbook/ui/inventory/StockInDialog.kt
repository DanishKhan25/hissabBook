package com.example.hisabbook.ui.inventory

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AddCircle
import androidx.compose.material.icons.rounded.AttachMoney
import androidx.compose.material.icons.rounded.Description
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.hisabbook.data.local.entity.InventoryItem
import com.example.hisabbook.ui.viewmodel.InventoryViewModel
import com.example.hisabbook.util.CurrencyFormatter
import com.example.hisabbook.util.parseCleanDouble
import com.example.hisabbook.util.parseCleanDoubleOrDefault

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StockInDialog(
    inventoryViewModel: InventoryViewModel,
    preSelectedItem: Long? = null,
    onDismissRequest: () -> Unit
) {
    val items = inventoryViewModel.inventoryItems.collectAsStateWithLifecycle().value

    var selectedItemId by remember(preSelectedItem) {
        mutableLongStateOf(preSelectedItem ?: 0L)
    }

    LaunchedEffect(preSelectedItem, items) {
        selectedItemId = preSelectedItem
            ?: selectedItemId.takeIf { it > 0 }
            ?: 0L
    }

    val selectedItem = items.find { it.id == selectedItemId }

    var quantityText by remember { mutableStateOf("") }
    var priceText by remember(selectedItem) {
        mutableStateOf(selectedItem?.purchasePrice?.let { CurrencyFormatter.formatAmountOnly(it) } ?: "0")
    }
    var supplierNotes by remember { mutableStateOf("") }

    var qtyError by remember { mutableStateOf<String?>(null) }
    var itemError by remember { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = onDismissRequest,
        title = { Text("Stock In (+)", style = MaterialTheme.typography.headlineSmall) },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                if (preSelectedItem == null) {
                    SearchableInventoryPicker(
                        inventoryItems = items,
                        selectedItem = selectedItem,
                        onItemSelected = { item: InventoryItem ->
                            selectedItemId = item.id
                            priceText = CurrencyFormatter.formatAmountOnly(item.purchasePrice)
                            itemError = null
                        },
                        label = "Select Inventory Item *",
                        placeholder = if (items.isEmpty()) "No inventory items available" else "Type item name"
                    )
                    itemError?.let {
                        Text(
                            text = it,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                } else if (selectedItem != null) {
                    Text(
                        text = "Item: ${selectedItem.name} (Current: ${CurrencyFormatter.formatAmountOnly(selectedItem.quantity)} ${selectedItem.unit})",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                }

                // Quantity Added
                OutlinedTextField(
                    value = quantityText,
                    onValueChange = {
                        quantityText = it
                        val clean = it.parseCleanDouble()
                        if (clean != null && clean > 0) qtyError = null
                    },
                    label = { Text("Quantity Added (${selectedItem?.unit ?: "units"}) *") },
                    leadingIcon = { Icon(Icons.Rounded.AddCircle, contentDescription = null) },
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Decimal,
                        imeAction = ImeAction.Next
                    ),
                    isError = qtyError != null,
                    supportingText = qtyError?.let { { Text(it) } },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                // Purchase Price per unit
                OutlinedTextField(
                    value = priceText,
                    onValueChange = { priceText = it },
                    label = { Text("Purchase Price per Unit (₹)") },
                    leadingIcon = { Icon(Icons.Rounded.AttachMoney, contentDescription = null) },
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Decimal,
                        imeAction = ImeAction.Next
                    ),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                // Supplier / Notes
                OutlinedTextField(
                    value = supplierNotes,
                    onValueChange = { supplierNotes = it },
                    label = { Text("Supplier Name / Notes (Optional)") },
                    leadingIcon = { Icon(Icons.Rounded.Description, contentDescription = null) },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val qty = quantityText.parseCleanDouble()
                    if (qty == null || qty <= 0) {
                        qtyError = "Enter a valid quantity greater than 0"
                    } else if (selectedItemId <= 0) {
                        itemError = "Select a valid item"
                    } else {
                        val price = priceText.parseCleanDoubleOrDefault(selectedItem?.purchasePrice ?: 0.0)
                        inventoryViewModel.stockIn(
                            itemId = selectedItemId,
                            quantity = qty,
                            purchasePrice = price,
                            supplierNotes = supplierNotes,
                            onSuccess = onDismissRequest
                        )
                    }
                }
            ) {
                Text("Confirm Stock In")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismissRequest) {
                Text("Cancel")
            }
        }
    )
}
