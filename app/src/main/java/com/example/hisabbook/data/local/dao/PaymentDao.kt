package com.example.hisabbook.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.hisabbook.data.local.entity.Payment
import kotlinx.coroutines.flow.Flow

@Dao
interface PaymentDao {
    @Query("SELECT * FROM payments ORDER BY date DESC")
    fun getAllPayments(): Flow<List<Payment>>

    @Query("SELECT * FROM payments ORDER BY date DESC")
    suspend fun getAllPaymentsOneShot(): List<Payment>

    @Query("SELECT * FROM payments WHERE billId = :billId ORDER BY date DESC")
    fun getPaymentsForInvoice(billId: Long): Flow<List<Payment>>

    @Query("SELECT * FROM payments WHERE billId = :billId ORDER BY date DESC")
    suspend fun getPaymentsForInvoiceOneShot(billId: Long): List<Payment>

    @Query("SELECT * FROM payments WHERE customerId = :customerId ORDER BY date DESC")
    fun getPaymentsForCustomer(customerId: Long): Flow<List<Payment>>

    @Query("SELECT * FROM payments WHERE customerId = :customerId ORDER BY date DESC")
    suspend fun getPaymentsForCustomerOneShot(customerId: Long): List<Payment>

    @Query("SELECT SUM(amount) FROM payments WHERE billId = :billId")
    fun getTotalPaymentsForInvoice(billId: Long): Flow<Double?>

    @Query("SELECT SUM(amount) FROM payments WHERE billId = :billId")
    suspend fun getTotalPaymentsForInvoiceOneShot(billId: Long): Double?

    @Query("SELECT SUM(amount) FROM payments")
    fun getTotalPaymentsAmount(): Flow<Double?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPayment(payment: Payment): Long

    @Update
    suspend fun updatePayment(payment: Payment)

    @Delete
    suspend fun deletePayment(payment: Payment)
}
