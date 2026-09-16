package com.example.hisabbook.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.hisabbook.data.local.entity.InventoryItem
import kotlinx.coroutines.flow.Flow

@Dao
interface InventoryDao {
    @Query("SELECT * FROM inventory_items ORDER BY name ASC")
    fun getAllInventoryItems(): Flow<List<InventoryItem>>

    @Query("SELECT * FROM inventory_items ORDER BY name ASC")
    suspend fun getAllInventoryItemsOneShot(): List<InventoryItem>

    @Query("SELECT * FROM inventory_items WHERE id = :id")
    fun getInventoryItemById(id: Long): Flow<InventoryItem?>

    @Query("SELECT * FROM inventory_items WHERE id = :id LIMIT 1")
    suspend fun getInventoryItemByIdOneShot(id: Long): InventoryItem?

    @Query("SELECT * FROM inventory_items WHERE LOWER(name) = LOWER(:name) LIMIT 1")
    suspend fun getInventoryItemByNameOneShot(name: String): InventoryItem?

    @Query("SELECT * FROM inventory_items WHERE quantity <= minimumStock ORDER BY quantity ASC")
    fun getLowStockItems(): Flow<List<InventoryItem>>

    @Query("SELECT * FROM inventory_items WHERE name LIKE '%' || :query || '%' ORDER BY name ASC")
    fun searchInventoryItems(query: String): Flow<List<InventoryItem>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertInventoryItem(item: InventoryItem): Long

    @Update
    suspend fun updateInventoryItem(item: InventoryItem)

    @Delete
    suspend fun deleteInventoryItem(item: InventoryItem)

    @Query("UPDATE inventory_items SET quantity = :newQuantity WHERE id = :itemId")
    suspend fun updateStockQuantity(itemId: Long, newQuantity: Double)
}
