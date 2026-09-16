package com.example.hisabbook.data.repository

import com.example.hisabbook.data.local.dao.InventoryDao
import com.example.hisabbook.data.local.dao.StockTransactionDao
import com.example.hisabbook.data.local.entity.InventoryItem
import com.example.hisabbook.data.local.entity.StockTransaction
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext

class InventoryRepository(
    private val inventoryDao: InventoryDao,
    private val stockTransactionDao: StockTransactionDao
) {
    val allInventoryItems: Flow<List<InventoryItem>> = inventoryDao.getAllInventoryItems()

    val lowStockItems: Flow<List<InventoryItem>> = inventoryDao.getLowStockItems()

    fun getInventoryItemById(id: Long): Flow<InventoryItem?> =
        inventoryDao.getInventoryItemById(id)

    suspend fun getInventoryItemByNameOneShot(name: String): InventoryItem? = withContext(Dispatchers.IO) {
        inventoryDao.getInventoryItemByNameOneShot(name)
    }

    fun searchInventoryItems(query: String): Flow<List<InventoryItem>> =
        inventoryDao.searchInventoryItems(query)

    suspend fun insertInventoryItem(item: InventoryItem): Long = withContext(Dispatchers.IO) {
        inventoryDao.insertInventoryItem(item)
    }

    suspend fun updateInventoryItem(item: InventoryItem) = withContext(Dispatchers.IO) {
        inventoryDao.updateInventoryItem(item)
    }

    suspend fun deleteInventoryItem(item: InventoryItem) = withContext(Dispatchers.IO) {
        inventoryDao.deleteInventoryItem(item)
    }

    fun getTransactionsForItem(itemId: Long): Flow<List<StockTransaction>> =
        stockTransactionDao.getTransactionsForItem(itemId)

    /**
     * Records stock update transaction and automatically adjusts item quantity.
     * type: "IN", "OUT", or "ADJUSTMENT"
     */
    suspend fun recordStockTransaction(
        inventoryItemId: Long,
        type: String,
        quantity: Double,
        purchasePrice: Double = 0.0,
        reason: String = "",
        reference: String = ""
    ): Long? = withContext(Dispatchers.IO) {
        val currentItem = inventoryDao.getInventoryItemByIdOneShot(inventoryItemId) ?: return@withContext null

        val newQuantity = when (type.uppercase()) {
            "IN" -> currentItem.quantity + quantity
            "OUT", "STOCK_OUT" -> (currentItem.quantity - quantity).coerceAtLeast(0.0)
            "ADJUSTMENT" -> quantity
            else -> currentItem.quantity
        }

        inventoryDao.updateStockQuantity(inventoryItemId, newQuantity)

        val transaction = StockTransaction(
            inventoryItemId = inventoryItemId,
            type = type.uppercase(),
            quantity = quantity,
            purchasePrice = if (purchasePrice > 0) purchasePrice else currentItem.purchasePrice,
            reason = reason,
            reference = reference
        )

        stockTransactionDao.insertStockTransaction(transaction)
    }
}
