package com.example.hisabbook.ui.navigation

import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable

sealed interface HisabBookNavKey : NavKey {

    @Serializable
    data object Onboarding : HisabBookNavKey

    @Serializable
    data object HomeDashboard : HisabBookNavKey

    @Serializable
    data object CustomerList : HisabBookNavKey

    @Serializable
    data class CustomerDetail(val customerId: Long) : HisabBookNavKey

    @Serializable
    data object QuotationList : HisabBookNavKey

    @Serializable
    data class CreateQuotation(val customerId: Long? = null, val quotationIdToEdit: Long? = null) : HisabBookNavKey

    @Serializable
    data class QuotationDetail(val quotationId: Long) : HisabBookNavKey

    @Serializable
    data object InvoiceList : HisabBookNavKey

    @Serializable
    data class CreateInvoice(
        val customerId: Long? = null,
        val quotationId: Long? = null,
        val invoiceIdToEdit: Long? = null
    ) : HisabBookNavKey

    @Serializable
    data class InvoiceDetail(val invoiceId: Long) : HisabBookNavKey

    @Serializable
    data object PaymentHistory : HisabBookNavKey

    @Serializable
    data object InventoryList : HisabBookNavKey

    @Serializable
    data object Settings : HisabBookNavKey
}
