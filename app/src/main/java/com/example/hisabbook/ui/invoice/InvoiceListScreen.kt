package com.example.hisabbook.ui.invoice

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ReceiptLong
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Clear
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.hisabbook.data.local.entity.Invoice
import com.example.hisabbook.ui.theme.HisabBookTheme
import com.example.hisabbook.ui.viewmodel.InvoiceViewModel
import com.example.hisabbook.util.CurrencyFormatter
import com.example.hisabbook.util.DateUtils

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InvoiceListScreen(
    invoiceViewModel: InvoiceViewModel,
    onInvoiceClick: (Long) -> Unit,
    onCreateInvoiceClick: () -> Unit
) {
    val invoices by invoiceViewModel.filteredInvoices.collectAsStateWithLifecycle()
    val customers by invoiceViewModel.customers.collectAsStateWithLifecycle()
    val payments by invoiceViewModel.allPayments.collectAsStateWithLifecycle()
    val searchQuery by invoiceViewModel.searchQuery.collectAsStateWithLifecycle()
    val statusFilter by invoiceViewModel.statusFilter.collectAsStateWithLifecycle()

    val customerMap = customers.associateBy { it.id }
    val paymentsByInvoice = payments.groupBy { it.billId }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Invoices & Bills", fontWeight = FontWeight.Bold) }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onCreateInvoiceClick,
                containerColor = MaterialTheme.colorScheme.primaryContainer,
                contentColor = MaterialTheme.colorScheme.onPrimaryContainer
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Rounded.Add, contentDescription = "New Bill")
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("New Bill", fontWeight = FontWeight.SemiBold)
                }
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Search Bar & Filter Chips
            InvoiceSearchBar(
                searchQuery = searchQuery,
                onSearchQueryChange = { invoiceViewModel.setSearchQuery(it) },
                statusFilter = statusFilter,
                onStatusFilterChange = { invoiceViewModel.setStatusFilter(it) },
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
            )

            if (invoices.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Rounded.ReceiptLong,
                            contentDescription = null,
                            modifier = Modifier.size(72.dp),
                            tint = MaterialTheme.colorScheme.outline
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = if (searchQuery.isNotEmpty() || statusFilter != null) "No matching bills found" else "No Bills / Invoices Created",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Tap '+ New Bill' to create a bill.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.outline
                        )
                    }
                }
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(invoices, key = { it.id }) { invoice ->
                        val customerName = customerMap[invoice.customerId]?.name ?: "Customer #${invoice.customerId}"
                        val invoicePayments = paymentsByInvoice[invoice.id] ?: emptyList()
                        val paidAmount = invoicePayments.sumOf { it.amount }
                        val outstandingBalance = (invoice.total - paidAmount).coerceAtLeast(0.0)

                        InvoiceCard(
                            invoice = invoice,
                            customerName = customerName,
                            paidAmount = paidAmount,
                            outstandingBalance = outstandingBalance,
                            onClick = { onInvoiceClick(invoice.id) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun InvoiceCard(
    invoice: Invoice,
    customerName: String,
    paidAmount: Double,
    outstandingBalance: Double,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Bill #${invoice.billNumber}",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )

                InvoiceStatusBadge(status = invoice.status, balance = outstandingBalance)
            }

            Spacer(modifier = Modifier.height(6.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = customerName,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold
                )

                Text(
                    text = DateUtils.formatDate(invoice.createdAt),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            if (invoice.description.isNotEmpty()) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = invoice.description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 2
                )
            }

            HorizontalDivider(modifier = Modifier.padding(vertical = 10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text("Total Amount", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.outline)
                    Text(
                        text = CurrencyFormatter.formatCurrency(invoice.total),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }

                Column(horizontalAlignment = Alignment.End) {
                    Text("Paid: ${CurrencyFormatter.formatCurrency(paidAmount)}", style = MaterialTheme.typography.labelSmall)
                    Text(
                        text = if (outstandingBalance > 0) "Due: ${CurrencyFormatter.formatCurrency(outstandingBalance)}" else "Fully Paid",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold,
                        color = if (outstandingBalance > 0) MaterialTheme.colorScheme.error else Color(0xFF15803D)
                    )
                }
            }
        }
    }
}

@Composable
fun InvoiceStatusBadge(status: String, balance: Double) {
    val (bgColor, textColor, label) = when {
        status.equals("PAID", ignoreCase = true) || balance <= 0 -> Triple(
            Color(0xFFDCFCE7),
            Color(0xFF15803D),
            "PAID"
        )
        status.equals("PARTIALLY_PAID", ignoreCase = true) || status.equals("PARTIALLY PAID", ignoreCase = true) -> Triple(
            Color(0xFFFEF3C7),
            Color(0xFFB45309),
            "PARTIALLY PAID"
        )
        else -> Triple(
            Color(0xFFFEE2E2),
            Color(0xFFB91C1C),
            "UNPAID"
        )
    }

    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(12.dp))
            .background(bgColor)
            .padding(horizontal = 10.dp, vertical = 4.dp)
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
            color = textColor
        )
    }
}

@Composable
fun InvoiceSearchBar(
    searchQuery: String,
    onSearchQueryChange: (String) -> Unit,
    statusFilter: String?,
    onStatusFilterChange: (String?) -> Unit,
    modifier: Modifier = Modifier
) {
    val filterOptions = listOf(
        "ALL" to "All",
        "UNPAID" to "Unpaid",
        "PARTIALLY_PAID" to "Partially Paid",
        "PAID" to "Paid"
    )

    Column(modifier = modifier.fillMaxWidth()) {
        OutlinedTextField(
            value = searchQuery,
            onValueChange = onSearchQueryChange,
            placeholder = {
                Text(
                    text = "Search bill #, customer name or items...",
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            },
            leadingIcon = {
                Icon(imageVector = Icons.Rounded.Search, contentDescription = "Search")
            },
            trailingIcon = {
                if (searchQuery.isNotEmpty()) {
                    IconButton(onClick = { onSearchQueryChange("") }) {
                        Icon(imageVector = Icons.Rounded.Clear, contentDescription = "Clear search")
                    }
                }
            },
            shape = RoundedCornerShape(28.dp),
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(8.dp))

        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(filterOptions) { (key, label) ->
                val isSelected = (statusFilter == null && key == "ALL") || statusFilter.equals(key, ignoreCase = true)
                FilterChip(
                    selected = isSelected,
                    onClick = {
                        if (key == "ALL") {
                            onStatusFilterChange(null)
                        } else {
                            onStatusFilterChange(key)
                        }
                    },
                    label = { Text(label) }
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun InvoiceSearchBarPreview() {
    HisabBookTheme {
        InvoiceSearchBar(
            searchQuery = "",
            onSearchQueryChange = {},
            statusFilter = null,
            onStatusFilterChange = {},
            modifier = Modifier.padding(16.dp)
        )
    }
}

@Preview(showBackground = true)
@Composable
fun InvoiceCardPreview() {
    HisabBookTheme {
        InvoiceCard(
            invoice = Invoice(
                id = 1L,
                billNumber = "1001",
                customerId = 1L,
                description = "Hardware materials and paint supplies",
                amount = 12500.0,
                total = 12500.0,
                status = "PARTIALLY_PAID"
            ),
            customerName = "Suresh Sharma",
            paidAmount = 5000.0,
            outstandingBalance = 7500.0,
            onClick = {}
        )
    }
}


