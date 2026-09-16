package com.example.hisabbook.ui.inventory

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Clear
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.hisabbook.data.local.entity.InventoryItem
import com.example.hisabbook.util.CurrencyFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchableInventoryPicker(
    inventoryItems: List<InventoryItem>,
    selectedItem: InventoryItem?,
    onItemSelected: (InventoryItem) -> Unit,
    modifier: Modifier = Modifier,
    label: String = "Select Inventory Item",
    placeholder: String = "Type to search inventory",
) {
    var expanded by remember { mutableStateOf(false) }
    var searchQuery by remember {
        mutableStateOf(selectedItem?.name ?: "")
    }

    LaunchedEffect(selectedItem) {
        searchQuery = selectedItem?.name ?: ""
    }

    val filteredItems = remember(inventoryItems, searchQuery) {
        if (searchQuery.isBlank()) {
            inventoryItems
        } else {
            inventoryItems.filter {
                it.name.contains(searchQuery, ignoreCase = true)
            }
        }
    }

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = it },
        modifier = modifier.fillMaxWidth()
    ) {
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { query ->
                searchQuery = query
                expanded = true
            },
            label = { Text(label) },
            placeholder = { Text(placeholder) },
            leadingIcon = {
                Icon(
                    imageVector = Icons.Rounded.Search,
                    contentDescription = "Search Items"
                )
            },
            trailingIcon = {
                if (searchQuery.isNotEmpty()) {
                    IconButton(onClick = {
                        searchQuery = ""
                        expanded = true
                    }) {
                        Icon(
                            imageVector = Icons.Rounded.Clear,
                            contentDescription = "Clear search"
                        )
                    }
                } else {
                    ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded)
                }
            },
            singleLine = true,
            modifier = Modifier
                .fillMaxWidth()
                .menuAnchor(MenuAnchorType.PrimaryEditable)
        )

        if (filteredItems.isNotEmpty()) {
            ExposedDropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false },
                modifier = Modifier.heightIn(max = 280.dp)
            ) {
                filteredItems.forEach { item ->
                    DropdownMenuItem(
                        text = {
                            Column {
                                Text(item.name, fontWeight = FontWeight.Bold)
                                Text(
                                    "Stock: ${CurrencyFormatter.formatAmountOnly(item.quantity)} ${item.unit} • Rate: ${CurrencyFormatter.formatCurrency(item.purchasePrice)}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        },
                        onClick = {
                            searchQuery = item.name
                            onItemSelected(item)
                            expanded = false
                        }
                    )
                }
            }
        } else if (expanded && searchQuery.isNotBlank()) {
            ExposedDropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false }
            ) {
                DropdownMenuItem(
                    text = {
                        Text(
                            text = "No matching inventory items found",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    },
                    onClick = { expanded = false }
                )
            }
        }
    }
}
