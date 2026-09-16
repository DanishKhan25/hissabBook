package com.example.hisabbook.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.hisabbook.data.local.entity.Invoice
import kotlinx.coroutines.flow.Flow

@Dao
interface InvoiceDao {
    @Query("SELECT * FROM invoices ORDER BY createdAt DESC")
    fun getAllInvoices(): Flow<List<Invoice>>

    @Query("SELECT * FROM invoices ORDER BY createdAt DESC")
    suspend fun getAllInvoicesOneShot(): List<Invoice>

    @Query("SELECT * FROM invoices WHERE id = :id")
    fun getInvoiceById(id: Long): Flow<Invoice?>

    @Query("SELECT * FROM invoices WHERE id = :id LIMIT 1")
    suspend fun getInvoiceByIdOneShot(id: Long): Invoice?

    @Query("SELECT * FROM invoices WHERE customerId = :customerId ORDER BY createdAt DESC")
    fun getInvoicesForCustomer(customerId: Long): Flow<List<Invoice>>

    @Query("SELECT * FROM invoices WHERE customerId = :customerId ORDER BY createdAt DESC")
    suspend fun getInvoicesForCustomerOneShot(customerId: Long): List<Invoice>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertInvoice(invoice: Invoice): Long

    @Update
    suspend fun updateInvoice(invoice: Invoice)

    @Delete
    suspend fun deleteInvoice(invoice: Invoice)

    @Query("UPDATE invoices SET status = :status WHERE id = :id")
    suspend fun updateInvoiceStatus(id: Long, status: String)

    @Query("SELECT COUNT(*) FROM invoices")
    fun getInvoiceCount(): Flow<Int>

    @Query("SELECT SUM(total) FROM invoices")
    fun getTotalInvoiceAmount(): Flow<Double?>
}
