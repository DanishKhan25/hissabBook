package com.example.hisabbook.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.hisabbook.data.local.entity.Quotation
import kotlinx.coroutines.flow.Flow

@Dao
interface QuotationDao {
    @Query("SELECT * FROM quotations ORDER BY createdAt DESC")
    fun getAllQuotations(): Flow<List<Quotation>>

    @Query("SELECT * FROM quotations ORDER BY createdAt DESC")
    suspend fun getAllQuotationsOneShot(): List<Quotation>

    @Query("SELECT * FROM quotations WHERE id = :id")
    fun getQuotationById(id: Long): Flow<Quotation?>

    @Query("SELECT * FROM quotations WHERE id = :id LIMIT 1")
    suspend fun getQuotationByIdOneShot(id: Long): Quotation?

    @Query("SELECT * FROM quotations WHERE customerId = :customerId ORDER BY createdAt DESC")
    fun getQuotationsForCustomer(customerId: Long): Flow<List<Quotation>>

    @Query("SELECT MAX(id) FROM quotations")
    suspend fun getMaxId(): Long?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertQuotation(quotation: Quotation): Long

    @Update
    suspend fun updateQuotation(quotation: Quotation)

    @Delete
    suspend fun deleteQuotation(quotation: Quotation)

    @Query("UPDATE quotations SET status = :status WHERE id = :id")
    suspend fun updateStatus(id: Long, status: String)
}
