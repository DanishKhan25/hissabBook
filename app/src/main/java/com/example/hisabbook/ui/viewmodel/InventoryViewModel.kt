package com.example.hisabbook.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.hisabbook.data.local.entity.InventoryItem
import com.example.hisabbook.data.local.entity.StockTransaction
import com.example.hisabbook.data.repository.InventoryRepository
import com.example.hisabbook.util.CurrencyFormatter
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class InventoryViewModel(
    private val inventoryRepository: InventoryRepository
) : ViewModel() {

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _showOnlyLowStock = MutableStateFlow(false)
    val showOnlyLowStock: StateFlow<Boolean> = _showOnlyLowStock.asStateFlow()

    private val _userMessage = MutableStateFlow<String?>(null)
    val userMessage: StateFlow<String?> = _userMessage.asStateFlow()

    val inventoryItems: StateFlow<List<InventoryItem>> = combine(
        inventoryRepository.allInventoryItems,
        _searchQuery,
        _showOnlyLowStock
    ) { items, query, lowStockOnly ->
        items.filter { item ->
            val matchesQuery = query.isBlank() ||
                    item.name.contains(query, ignoreCase = true) ||
                    item.unit.contains(query, ignoreCase = true)
            val matchesLowStock = !lowStockOnly || (item.quantity <= item.minimumStock)
            matchesQuery && matchesLowStock
        }
    }.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        emptyList()
    )

    val lowStockCount: StateFlow<Int> = inventoryRepository.lowStockItems
        .map { it.size }
        .stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000),
            0
        )

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun toggleLowStockFilter() {
        _showOnlyLowStock.value = !_showOnlyLowStock.value
    }

    fun setLowStockFilter(enabled: Boolean) {
        _showOnlyLowStock.value = enabled
    }

    fun addInventoryItem(
        name: String,
        unit: String,
        initialQuantity: Double,
        minimumStock: Double,
        purchasePrice: Double,
        onSuccess: () -> Unit,
        onError: ((String) -> Unit)? = null
    ) {
        val trimmedName = name.trim()
        if (trimmedName.isBlank()) {
            val err = "Item name cannot be empty"
            _userMessage.value = err
            onError?.invoke(err)
            return
        }

        viewModelScope.launch(Dispatchers.IO) {
            val existing = inventoryRepository.getInventoryItemByNameOneShot(trimmedName)
            if (existing != null) {
                val err = "An inventory item with this name already exists"
                _userMessage.value = err
                withContext(Dispatchers.Main) { onError?.invoke(err) }
                return@launch
            }

            val newItem = InventoryItem(
                name = trimmedName,
                unit = unit.ifBlank { "pcs" },
                quantity = 0.0,
                minimumStock = minimumStock.coerceAtLeast(0.0),
                purchasePrice = purchasePrice.coerceAtLeast(0.0)
            )
            val itemId = inventoryRepository.insertInventoryItem(newItem)

            if (initialQuantity > 0) {
                inventoryRepository.recordStockTransaction(
                    inventoryItemId = itemId,
                    type = "IN",
                    quantity = initialQuantity,
                    purchasePrice = purchasePrice,
                    reason = "Initial Stock",
                    reference = "OPENING"
                )
            }

            _userMessage.value = "Inventory item '$trimmedName' added successfully"
            withContext(Dispatchers.Main) { onSuccess() }
        }
    }

    fun stockIn(
        itemId: Long,
        quantity: Double,
        purchasePrice: Double,
        supplierNotes: String,
        onSuccess: () -> Unit
    ) {
        if (quantity <= 0) {
            _userMessage.value = "Quantity added must be greater than 0"
            return
        }

        viewModelScope.launch(Dispatchers.IO) {
            val item = inventoryRepository.getInventoryItemById(itemId).firstOrNull()
            inventoryRepository.recordStockTransaction(
                inventoryItemId = itemId,
                type = "IN",
                quantity = quantity,
                purchasePrice = purchasePrice,
                reason = supplierNotes.ifBlank { "Stock In" }
            )
            val itemName = item?.name ?: "Item"
            _userMessage.value = "Stock In recorded: +${CurrencyFormatter.formatAmountOnly(quantity)} ${item?.unit ?: ""} for $itemName"
            withContext(Dispatchers.Main) { onSuccess() }
        }
    }

    fun updateInventoryItemDetails(
        item: InventoryItem,
        name: String,
        unit: String,
        minimumStock: Double,
        purchasePrice: Double,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        val trimmedName = name.trim()
        if (trimmedName.isBlank()) {
            val err = "Item name cannot be empty"
            _userMessage.value = err
            onError(err)
            return
        }

        viewModelScope.launch(Dispatchers.IO) {
            val existing = inventoryRepository.getInventoryItemByNameOneShot(trimmedName)
            if (existing != null && existing.id != item.id) {
                val err = "An inventory item with this name already exists"
                _userMessage.value = err
                withContext(Dispatchers.Main) { onError(err) }
                return@launch
            }

            inventoryRepository.updateInventoryItem(
                item.copy(
                    name = trimmedName,
                    unit = unit.ifBlank { item.unit },
                    minimumStock = minimumStock.coerceAtLeast(0.0),
                    purchasePrice = purchasePrice.coerceAtLeast(0.0)
                )
            )

            _userMessage.value = "Inventory item '$trimmedName' updated"
            withContext(Dispatchers.Main) { onSuccess() }
        }
    }

    fun stockOut(
        itemId: Long,
        quantity: Double,
        reason: String,
        reference: String,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        if (quantity <= 0) {
            val msg = "Quantity removed must be greater than 0"
            _userMessage.value = msg
            onError(msg)
            return
        }

        viewModelScope.launch(Dispatchers.IO) {
            val item = inventoryRepository.getInventoryItemById(itemId).firstOrNull()
            if (item == null) {
                val msg = "Selected inventory item not found"
                _userMessage.value = msg
                withContext(Dispatchers.Main) { onError(msg) }
                return@launch
            }

            if (item.quantity < quantity) {
                val errorMsg = "Cannot reduce stock below 0! Available stock is ${CurrencyFormatter.formatAmountOnly(item.quantity)} ${item.unit}, requested: ${CurrencyFormatter.formatAmountOnly(quantity)} ${item.unit}"
                _userMessage.value = errorMsg
                withContext(Dispatchers.Main) { onError(errorMsg) }
                return@launch
            }

            inventoryRepository.recordStockTransaction(
                inventoryItemId = itemId,
                type = "OUT",
                quantity = quantity,
                purchasePrice = 0.0,
                reason = reason.ifBlank { "Stock Out" },
                reference = reference
            )
            val successMsg = "Stock Out recorded: -${CurrencyFormatter.formatAmountOnly(quantity)} ${item.unit} for ${item.name}"
            _userMessage.value = successMsg
            withContext(Dispatchers.Main) { onSuccess() }
        }
    }

    fun getItemTransactions(itemId: Long): Flow<List<StockTransaction>> {
        return inventoryRepository.getTransactionsForItem(itemId)
    }

    fun deleteInventoryItem(item: InventoryItem) {
        viewModelScope.launch(Dispatchers.IO) {
            inventoryRepository.deleteInventoryItem(item)
            _userMessage.value = "Inventory item '${item.name}' deleted"
        }
    }

    fun clearUserMessage() {
        _userMessage.value = null
    }
}
