package com.example.hisabbook.data.repository

import com.example.hisabbook.data.local.dao.CustomerDao
import com.example.hisabbook.data.local.dao.InvoiceDao
import com.example.hisabbook.data.local.dao.PaymentDao
import com.example.hisabbook.data.local.entity.Customer
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine

class CustomerRepository(
    private val customerDao: CustomerDao,
    private val invoiceDao: InvoiceDao,
    private val paymentDao: PaymentDao
) {
    val allCustomers: Flow<List<Customer>> = customerDao.getAllCustomers()

    fun getCustomerById(id: Long): Flow<Customer?> = customerDao.getCustomerById(id)

    suspend fun getCustomerByIdOneShot(id: Long): Customer? = customerDao.getCustomerByIdOneShot(id)

    suspend fun getCustomerByPhoneOneShot(phone: String): Customer? = customerDao.getCustomerByPhoneOneShot(phone)

    fun searchCustomers(query: String): Flow<List<Customer>> = customerDao.searchCustomers(query)

    suspend fun insertCustomer(customer: Customer): Long = customerDao.insertCustomer(customer)

    suspend fun updateCustomer(customer: Customer) = customerDao.updateCustomer(customer)

    suspend fun deleteCustomer(customer: Customer) = customerDao.deleteCustomer(customer)

    suspend fun deleteCustomerById(id: Long) = customerDao.deleteCustomerById(id)

    /**
     * Calculates customer's outstanding balance: Total Invoice Amount - Total Payments Made
     */
    fun getCustomerOutstandingBalance(customerId: Long): Flow<Double> {
        val invoicesFlow = invoiceDao.getInvoicesForCustomer(customerId)
        val paymentsFlow = paymentDao.getPaymentsForCustomer(customerId)

        return combine(invoicesFlow, paymentsFlow) { invoices, payments ->
            val totalInvoiced = invoices.filter { it.status != "CANCELLED" }.sumOf { it.total }
            val totalPaid = payments.sumOf { it.amount }
            val balance = totalInvoiced - totalPaid
            if (balance < 0) 0.0 else balance
        }
    }
}
