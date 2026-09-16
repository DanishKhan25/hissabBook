package com.example.hisabbook.ui.inventory

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.AddCircle
import androidx.compose.material.icons.rounded.Clear
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.rounded.History
import androidx.compose.material.icons.rounded.Inventory
import androidx.compose.material.icons.rounded.RemoveCircle
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material.icons.rounded.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.hisabbook.data.local.entity.InventoryItem
import com.example.hisabbook.data.local.entity.StockTransaction
import com.example.hisabbook.ui.theme.HisabBookTheme
import com.example.hisabbook.ui.viewmodel.InventoryViewModel
import com.example.hisabbook.util.CurrencyFormatter
import com.example.hisabbook.util.DateUtils

@Composable
fun InventoryListScreen(
    viewModel: InventoryViewModel,
    modifier: Modifier = Modifier
) {
    val searchQuery by viewModel.searchQuery.collectAsStateWithLifecycle()
    val showOnlyLowStock by viewModel.showOnlyLowStock.collectAsStateWithLifecycle()
    val items by viewModel.inventoryItems.collectAsStateWithLifecycle()
    val lowStockCount by viewModel.lowStockCount.collectAsStateWithLifecycle()
    val userMessage by viewModel.userMessage.collectAsStateWithLifecycle()

    var showAddItemDialog by remember { mutableStateOf(false) }
    var showEditItemDialog by remember { mutableStateOf(false) }
    var showStockInDialog by remember { mutableStateOf(false) }
    var showStockOutDialog by remember { mutableStateOf(false) }
    var showStockHistoryDialog by remember { mutableStateOf(false) }
    var selectedItemForAction by remember { mutableStateOf<InventoryItem?>(null) }
    var itemPendingDelete by remember { mutableStateOf<InventoryItem?>(null) }

    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(userMessage) {
        userMessage?.let { msg ->
            snackbarHostState.showSnackbar(msg)
            viewModel.clearUserMessage()
        }
    }

    Scaffold(
        modifier = modifier,
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = { showAddItemDialog = true },
                icon = { Icon(Icons.Rounded.Add, contentDescription = null) },
                text = { Text("Add Item") },
                containerColor = MaterialTheme.colorScheme.primaryContainer,
                contentColor = MaterialTheme.colorScheme.onPrimaryContainer
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp, vertical = 8.dp)
        ) {
            // Low Stock Alert Banner (if any low stock items exist)
            if (lowStockCount > 0) {
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = MaterialTheme.colorScheme.errorContainer,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 12.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Warning,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onErrorContainer,
                            modifier = Modifier.size(28.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Low Stock Alert: $lowStockCount item(s) running low!",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onErrorContainer
                            )
                            Text(
                                text = "Restock items soon to prevent stockout during sales.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onErrorContainer
                            )
                        }
                    }
                }
            }

            // Search Bar & Low Stock Filter Chip
            InventorySearchBar(
                searchQuery = searchQuery,
                onSearchQueryChange = { viewModel.setSearchQuery(it) },
                showOnlyLowStock = showOnlyLowStock,
                onToggleLowStockFilter = { viewModel.toggleLowStockFilter() }
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Inventory List
            if (items.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.padding(24.dp)
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = MaterialTheme.colorScheme.surfaceVariant,
                            modifier = Modifier.size(80.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Rounded.Inventory,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.size(40.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        Text(
                            text = if (showOnlyLowStock) "No low stock items" else if (searchQuery.isBlank()) "No inventory items added" else "No matching items found",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = "Tap + Add Item to start tracking stock levels, units, & prices.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(bottom = 88.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(items, key = { it.id }) { item ->
                        InventoryItemCard(
                            item = item,
                            onStockInClick = {
                                selectedItemForAction = item
                                showStockInDialog = true
                            },
                            onStockOutClick = {
                                selectedItemForAction = item
                                showStockOutDialog = true
                            },
                            onHistoryClick = {
                                selectedItemForAction = item
                                showStockHistoryDialog = true
                            },
                            onEditClick = {
                                selectedItemForAction = item
                                showEditItemDialog = true
                            },
                            onDeleteClick = { itemPendingDelete = item }
                        )
                    }
                }
            }
        }
    }

    if (showAddItemDialog) {
        AddInventoryItemDialog(
            onDismissRequest = { showAddItemDialog = false },
            onSaveItem = { name, unit, initialQuantity, minimumStock, purchasePrice, onError ->
                viewModel.addInventoryItem(
                    name = name,
                    unit = unit,
                    initialQuantity = initialQuantity,
                    minimumStock = minimumStock,
                    purchasePrice = purchasePrice,
                    onSuccess = { showAddItemDialog = false },
                    onError = onError
                )
            }
        )
    }

    if (showEditItemDialog) {
        selectedItemForAction?.let { item ->
            EditInventoryItemDialog(
                item = item,
                onDismissRequest = {
                    showEditItemDialog = false
                    selectedItemForAction = null
                },
                onSaveItem = { name, unit, minimumStock, purchasePrice, onError ->
                    viewModel.updateInventoryItemDetails(
                        item = item,
                        name = name,
                        unit = unit,
                        minimumStock = minimumStock,
                        purchasePrice = purchasePrice,
                        onSuccess = {
                            showEditItemDialog = false
                            selectedItemForAction = null
                        },
                        onError = onError
                    )
                }
            )
        }
    }

    if (showStockInDialog) {
        StockInDialog(
            inventoryViewModel = viewModel,
            preSelectedItem = selectedItemForAction?.id,
            onDismissRequest = {
                showStockInDialog = false
                selectedItemForAction = null
            }
        )
    }

    if (showStockOutDialog) {
        StockOutDialog(
            inventoryViewModel = viewModel,
            preSelectedItem = selectedItemForAction?.id,
            onDismissRequest = {
                showStockOutDialog = false
                selectedItemForAction = null
            }
        )
    }

    if (showStockHistoryDialog) {
        selectedItemForAction?.let { item ->
            val transactions by viewModel.getItemTransactions(item.id).collectAsStateWithLifecycle(emptyList())
            StockHistoryDialog(
                item = item,
                transactions = transactions,
                onDismissRequest = {
                    showStockHistoryDialog = false
                    selectedItemForAction = null
                }
            )
        }
    }

    itemPendingDelete?.let { item ->
        AlertDialog(
            onDismissRequest = { itemPendingDelete = null },
            title = { Text("Delete Item?") },
            text = {
                Text("Are you sure you want to delete '${item.name}'? Its stock history will also be removed.")
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.deleteInventoryItem(item)
                        itemPendingDelete = null
                    }
                ) {
                    Text("Delete")
                }
            },
            dismissButton = {
                TextButton(onClick = { itemPendingDelete = null }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
fun InventoryItemCard(
    item: InventoryItem,
    onStockInClick: () -> Unit,
    onStockOutClick: () -> Unit,
    onHistoryClick: () -> Unit,
    onEditClick: () -> Unit,
    onDeleteClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isLowStock = item.quantity <= item.minimumStock

    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = item.name,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    Spacer(modifier = Modifier.height(2.dp))

                    Text(
                        text = "Purchase Price: ${CurrencyFormatter.formatCurrency(item.purchasePrice)} / ${item.unit}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Row(
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {


                    IconButton(onClick = onEditClick) {
                        Icon(
                            imageVector = Icons.Rounded.Edit,
                            contentDescription = "Edit Item"
                        )
                    }

                    IconButton(onClick = onDeleteClick) {
                        Icon(
                            imageVector = Icons.Rounded.Delete,
                            contentDescription = "Delete Item",
                            tint = MaterialTheme.colorScheme.error
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ){
                Column {
                    Text(
                        text = "Current Quantity",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "${CurrencyFormatter.formatAmountOnly(item.quantity)} ${item.unit}",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = if (isLowStock) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary
                    )
                }
                // Status Badge
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = if (isLowStock) MaterialTheme.colorScheme.errorContainer else MaterialTheme.colorScheme.secondaryContainer
                ) {
                    Text(
                        text = if (isLowStock) "LOW STOCK" else "IN STOCK",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = if (isLowStock) MaterialTheme.colorScheme.onErrorContainer else MaterialTheme.colorScheme.onSecondaryContainer,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }


            Spacer(modifier = Modifier.height(12.dp))

            // Stock Actions
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedButton(
                    onClick = onStockOutClick,
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.Rounded.RemoveCircle, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Stock Out", style = MaterialTheme.typography.labelMedium)
                }

                OutlinedButton(
                    onClick = onStockInClick,
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.Rounded.AddCircle, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Stock In", style = MaterialTheme.typography.labelMedium)
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            OutlinedButton(
                onClick = onHistoryClick,
                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(Icons.Rounded.History, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("History", style = MaterialTheme.typography.labelMedium)
            }
        }
    }
}

@Composable
fun StockHistoryDialog(
    item: InventoryItem,
    transactions: List<StockTransaction>,
    onDismissRequest: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismissRequest,
        title = { Text("Stock History") },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 420.dp)
            ) {
                Text(
                    text = item.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Current: ${CurrencyFormatter.formatAmountOnly(item.quantity)} ${item.unit}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(12.dp))

                if (transactions.isEmpty()) {
                    Text(
                        text = "No stock history recorded yet.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                } else {
                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        items(transactions, key = { it.id }) { transaction ->
                            StockHistoryRow(
                                transaction = transaction,
                                unit = item.unit
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismissRequest) {
                Text("Close")
            }
        }
    )
}

@Composable
fun StockHistoryRow(
    transaction: StockTransaction,
    unit: String,
    modifier: Modifier = Modifier
) {
    val isStockIn = transaction.type.equals("IN", ignoreCase = true)
    val isBillSale = !isStockIn && transaction.reason.startsWith("Bill #", ignoreCase = true)
    val customerName = transaction.reason
        .substringAfter("Customer:", missingDelimiterValue = "")
        .trim()
    val displayReason = transaction.reason
        .substringBefore("|")
        .trim()
    val typeLabel = when {
        isStockIn -> "Stock In"
        isBillSale -> "Sold in Bill"
        else -> "Stock Out"
    }
    val signedQuantity = "${if (isStockIn) "+" else "-"}${CurrencyFormatter.formatAmountOnly(transaction.quantity)} $unit"
    val typeColor = if (isStockIn) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error

    Column(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Top
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = typeLabel,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = typeColor
                )
                Text(
                    text = DateUtils.formatDateTime(transaction.date),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Text(
                text = signedQuantity,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = typeColor
            )
        }

        if (!isBillSale && displayReason.isNotBlank()) {
            Text(
                text = "Reason: $displayReason",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        if (customerName.isNotBlank()) {
            Text(
                text = "Customer: $customerName",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        if (transaction.reference.isNotBlank()) {
            Text(
                text = if (isBillSale) "Bill: ${transaction.reference}" else "Reference: ${transaction.reference}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        if (transaction.purchasePrice > 0) {
            Text(
                text = "Price: ${CurrencyFormatter.formatCurrency(transaction.purchasePrice)}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        Spacer(modifier = Modifier.height(10.dp))
        HorizontalDivider()
    }
}

@Composable
fun InventorySearchBar(
    searchQuery: String,
    onSearchQueryChange: (String) -> Unit,
    showOnlyLowStock: Boolean,
    onToggleLowStockFilter: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxWidth()) {
        OutlinedTextField(
            value = searchQuery,
            onValueChange = onSearchQueryChange,
            placeholder = { Text("Search inventory item...") },
            leadingIcon = {
                Icon(imageVector = Icons.Rounded.Search, contentDescription = "Search")
            },
            trailingIcon = {
                if (searchQuery.isNotEmpty()) {
                    IconButton(onClick = { onSearchQueryChange("") }) {
                        Icon(imageVector = Icons.Rounded.Clear, contentDescription = "Clear search")
                    }
                }
            },
            shape = RoundedCornerShape(28.dp),
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(8.dp))

        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            FilterChip(
                selected = !showOnlyLowStock,
                onClick = { if (showOnlyLowStock) onToggleLowStockFilter() },
                label = { Text("All Items") }
            )
            FilterChip(
                selected = showOnlyLowStock,
                onClick = { if (!showOnlyLowStock) onToggleLowStockFilter() },
                label = { Text("Low Stock") },
                leadingIcon = if (showOnlyLowStock) {
                    { Icon(Icons.Rounded.Warning, contentDescription = null, modifier = Modifier.size(16.dp)) }
                } else null
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun InventorySearchBarPreview() {
    HisabBookTheme {
        InventorySearchBar(
            searchQuery = "",
            onSearchQueryChange = {},
            showOnlyLowStock = false,
            onToggleLowStockFilter = {},
            modifier = Modifier.padding(16.dp)
        )
    }
}

@Preview(showBackground = true)
@Composable
fun InventoryItemCardPreview() {
    HisabBookTheme {
        InventoryItemCard(
            item = InventoryItem(
                id = 1L,
                name = "Cement Bag 50kg",
                unit = "bag",
                quantity = 5.0,
                minimumStock = 10.0,
                purchasePrice = 380.0
            ),
            onStockInClick = {},
            onStockOutClick = {},
            onHistoryClick = {},
            onEditClick = {},
            onDeleteClick = {},
            modifier = Modifier.padding(16.dp)
        )
    }
}
