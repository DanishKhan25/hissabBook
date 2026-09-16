package com.example.hisabbook.data.repository

import com.example.hisabbook.data.local.dao.InventoryDao
import com.example.hisabbook.data.local.dao.StockTransactionDao
import com.example.hisabbook.data.local.entity.InventoryItem
import com.example.hisabbook.data.local.entity.StockTransaction
import kotlinx.coroutines.flow.Flow

class InventoryRepository(
    private val inventoryDao: InventoryDao,
    private val stockTransactionDao: StockTransactionDao
) {
    val allInventoryItems: Flow<List<InventoryItem>> = inventoryDao.getAllInventoryItems()

    val lowStockItems: Flow<List<InventoryItem>> = inventoryDao.getLowStockItems()

    fun getInventoryItemById(id: Long): Flow<InventoryItem?> =
        inventoryDao.getInventoryItemById(id)

    suspend fun getInventoryItemByNameOneShot(name: String): InventoryItem? =
        inventoryDao.getInventoryItemByNameOneShot(name)

    fun searchInventoryItems(query: String): Flow<List<InventoryItem>> =
        inventoryDao.searchInventoryItems(query)

    suspend fun insertInventoryItem(item: InventoryItem): Long =
        inventoryDao.insertInventoryItem(item)

    suspend fun updateInventoryItem(item: InventoryItem) =
        inventoryDao.updateInventoryItem(item)

    suspend fun deleteInventoryItem(item: InventoryItem) =
        inventoryDao.deleteInventoryItem(item)

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
    ): Long? {
        val currentItem = inventoryDao.getInventoryItemByIdOneShot(inventoryItemId) ?: return null

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

        return stockTransactionDao.insertStockTransaction(transaction)
    }
}
