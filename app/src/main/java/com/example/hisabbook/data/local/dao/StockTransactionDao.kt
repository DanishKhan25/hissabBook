package com.example.hisabbook.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.hisabbook.data.local.entity.StockTransaction
import kotlinx.coroutines.flow.Flow

@Dao
interface StockTransactionDao {
    @Query("SELECT * FROM stock_transactions ORDER BY date DESC")
    fun getAllStockTransactions(): Flow<List<StockTransaction>>

    @Query("SELECT * FROM stock_transactions ORDER BY date DESC")
    suspend fun getAllStockTransactionsOneShot(): List<StockTransaction>

    @Query("SELECT * FROM stock_transactions WHERE inventoryItemId = :inventoryItemId ORDER BY date DESC")
    fun getTransactionsForItem(inventoryItemId: Long): Flow<List<StockTransaction>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertStockTransaction(transaction: StockTransaction): Long

    @Delete
    suspend fun deleteStockTransaction(transaction: StockTransaction)
}
