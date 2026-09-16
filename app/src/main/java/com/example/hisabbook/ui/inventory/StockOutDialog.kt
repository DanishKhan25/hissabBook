package com.example.hisabbook.ui.inventory

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Description
import androidx.compose.material.icons.rounded.RemoveCircle
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuAnchorType
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
import com.example.hisabbook.ui.viewmodel.InventoryViewModel
import com.example.hisabbook.util.CurrencyFormatter
import com.example.hisabbook.util.parseCleanDouble

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun StockOutDialog(
    inventoryViewModel: InventoryViewModel,
    preSelectedItem: Long? = null,
    onDismissRequest: () -> Unit
) {
    val items = inventoryViewModel.inventoryItems.collectAsStateWithLifecycle().value

    var selectedItemId by remember(preSelectedItem) {
        mutableLongStateOf(preSelectedItem ?: items.firstOrNull()?.id ?: 0L)
    }

    LaunchedEffect(preSelectedItem, items) {
        selectedItemId = preSelectedItem
            ?: selectedItemId.takeIf { it > 0 }
            ?: items.firstOrNull()?.id
            ?: 0L
    }

    val selectedItem = items.find { it.id == selectedItemId }

    var quantityText by remember { mutableStateOf("") }
    var reason by remember { mutableStateOf("Job/Sale") }
    var reference by remember { mutableStateOf("") }

    var itemDropdownExpanded by remember { mutableStateOf(false) }
    var qtyError by remember { mutableStateOf<String?>(null) }

    val reasons = listOf("Job/Sale", "Wastage/Damaged", "Return", "Other")

    AlertDialog(
        onDismissRequest = onDismissRequest,
        title = { Text("Stock Out (-)", style = MaterialTheme.typography.headlineSmall) },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Item Selector
                if (preSelectedItem == null && items.isNotEmpty()) {
                    ExposedDropdownMenuBox(
                        expanded = itemDropdownExpanded,
                        onExpandedChange = { itemDropdownExpanded = it }
                    ) {
                        OutlinedTextField(
                            value = selectedItem?.name ?: "Select Item",
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Select Inventory Item *") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = itemDropdownExpanded) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .menuAnchor(MenuAnchorType.PrimaryNotEditable)
                        )

                        ExposedDropdownMenu(
                            expanded = itemDropdownExpanded,
                            onDismissRequest = { itemDropdownExpanded = false }
                        ) {
                            items.forEach { item ->
                                DropdownMenuItem(
                                    text = { Text("${item.name} (${CurrencyFormatter.formatAmountOnly(item.quantity)} ${item.unit})") },
                                    onClick = {
                                        selectedItemId = item.id
                                        itemDropdownExpanded = false
                                        qtyError = null
                                    }
                                )
                            }
                        }
                    }
                } else if (selectedItem != null) {
                    Text(
                        text = "Item: ${selectedItem.name} (Available: ${CurrencyFormatter.formatAmountOnly(selectedItem.quantity)} ${selectedItem.unit})",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                }

                // Quantity Removed
                OutlinedTextField(
                    value = quantityText,
                    onValueChange = {
                        quantityText = it
                        qtyError = null
                    },
                    label = { Text("Quantity Removed (${selectedItem?.unit ?: "units"}) *") },
                    leadingIcon = { Icon(Icons.Rounded.RemoveCircle, contentDescription = null) },
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Decimal,
                        imeAction = ImeAction.Next
                    ),
                    isError = qtyError != null,
                    supportingText = qtyError?.let { { Text(it) } },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                // Reason Choice Chips
                Text("Reason", style = MaterialTheme.typography.labelMedium)
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    reasons.forEach { r ->
                        FilterChip(
                            selected = reason == r,
                            onClick = { reason = r },
                            label = { Text(r) }
                        )
                    }
                }

                // Reference Bill # / Job #
                OutlinedTextField(
                    value = reference,
                    onValueChange = { reference = it },
                    label = { Text("Reference Bill # / Job # (Optional)") },
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
                    } else if (selectedItem == null) {
                        qtyError = "Please select a valid item"
                    } else if (qty > selectedItem.quantity) {
                        qtyError = "Insufficient stock! Available stock is ${CurrencyFormatter.formatAmountOnly(selectedItem.quantity)} ${selectedItem.unit}"
                    } else {
                        inventoryViewModel.stockOut(
                            itemId = selectedItemId,
                            quantity = qty,
                            reason = reason,
                            reference = reference,
                            onSuccess = onDismissRequest,
                            onError = { err -> qtyError = err }
                        )
                    }
                }
            ) {
                Text("Confirm Stock Out")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismissRequest) {
                Text("Cancel")
            }
        }
    )
}
