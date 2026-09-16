package com.example.hisabbook.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.hisabbook.data.local.entity.InvoiceLineItemEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface InvoiceLineItemDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(items: List<InvoiceLineItemEntity>)

    // ── Invoice queries ────────────────────────────────────────────────────

    @Query("DELETE FROM invoice_line_items WHERE invoiceId = :invoiceId")
    suspend fun deleteByInvoiceId(invoiceId: Long)

    @Query("SELECT * FROM invoice_line_items WHERE invoiceId = :invoiceId ORDER BY id ASC")
    fun getLineItemsForInvoice(invoiceId: Long): Flow<List<InvoiceLineItemEntity>>

    @Query("SELECT * FROM invoice_line_items WHERE invoiceId = :invoiceId ORDER BY id ASC")
    suspend fun getLineItemsForInvoiceOneShot(invoiceId: Long): List<InvoiceLineItemEntity>

    // ── Quotation queries ──────────────────────────────────────────────────

    @Query("DELETE FROM invoice_line_items WHERE quotationId = :quotationId")
    suspend fun deleteByQuotationId(quotationId: Long)

    @Query("SELECT * FROM invoice_line_items WHERE quotationId = :quotationId ORDER BY id ASC")
    fun getLineItemsForQuotation(quotationId: Long): Flow<List<InvoiceLineItemEntity>>

    @Query("SELECT * FROM invoice_line_items WHERE quotationId = :quotationId ORDER BY id ASC")
    suspend fun getLineItemsForQuotationOneShot(quotationId: Long): List<InvoiceLineItemEntity>

    // ── Backup ─────────────────────────────────────────────────────────────

    @Query("SELECT * FROM invoice_line_items ORDER BY id ASC")
    suspend fun getAllLineItemsOneShot(): List<InvoiceLineItemEntity>
}
