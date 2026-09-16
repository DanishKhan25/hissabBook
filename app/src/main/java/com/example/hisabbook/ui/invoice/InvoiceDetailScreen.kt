package com.example.hisabbook.ui.invoice

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.rounded.Payments
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material.icons.rounded.Share
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.hisabbook.data.local.entity.Payment
import com.example.hisabbook.ui.payment.AddPaymentDialog
import com.example.hisabbook.ui.viewmodel.CustomerViewModel
import com.example.hisabbook.ui.viewmodel.InvoiceViewModel
import com.example.hisabbook.ui.viewmodel.PaymentViewModel
import com.example.hisabbook.util.CurrencyFormatter
import com.example.hisabbook.util.DateUtils
import com.example.hisabbook.util.PdfGenerator
import com.example.hisabbook.util.PdfShareHelper

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InvoiceDetailScreen(
    invoiceId: Long,
    invoiceViewModel: InvoiceViewModel,
    paymentViewModel: PaymentViewModel,
    customerViewModel: CustomerViewModel? = null,
    onBackClick: () -> Unit,
    onEditInvoiceClick: ((Long) -> Unit)? = null
) {
    val context = LocalContext.current
    val invoice by invoiceViewModel.getInvoiceById(invoiceId).collectAsStateWithLifecycle(initialValue = null)
    val customers by invoiceViewModel.customers.collectAsStateWithLifecycle()
    val payments by invoiceViewModel.getPaymentsForInvoice(invoiceId).collectAsStateWithLifecycle(initialValue = emptyList())
    val businessProfile by invoiceViewModel.businessProfile.collectAsStateWithLifecycle()

    var showAddPaymentDialog by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }
    var paymentToDelete by remember { mutableStateOf<Payment?>(null) }

    val currentInvoice = invoice
    val customer = customers.find { it.id == currentInvoice?.customerId }

    if (currentInvoice == null) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("Bill Details") },
                    navigationIcon = {
                        IconButton(onClick = onBackClick) {
                            Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = "Back")
                        }
                    }
                )
            }
        ) { padding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = Alignment.Center
            ) {
                Text("Invoice not found")
            }
        }
        return
    }

    val totalPaid = payments.sumOf { it.amount }
    val balanceDue = (currentInvoice.total - totalPaid).coerceAtLeast(0.0)

    val onWhatsAppShareClick = {
        val pdfFile = PdfGenerator.generateInvoicePdf(
            context = context,
            invoice = currentInvoice,
            customer = customer,
            payments = payments,
            businessProfile = businessProfile
        )
        val bName = businessProfile?.businessName?.ifBlank { "Business Name" } ?: "Business Name"
        val custName = customer?.name?.ifBlank { "Customer" } ?: "Customer"
        PdfShareHelper.shareToWhatsApp(
            context = context,
            file = pdfFile,
            customerName = custName,
            docType = "Invoice",
            docNumber = currentInvoice.billNumber,
            amount = currentInvoice.total,
            businessName = bName
        )
    }

    val onStandardShareClick = {
        val pdfFile = PdfGenerator.generateInvoicePdf(
            context = context,
            invoice = currentInvoice,
            customer = customer,
            payments = payments,
            businessProfile = businessProfile
        )
        PdfShareHelper.sharePdf(context, pdfFile, "Bill ${currentInvoice.billNumber}")
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Bill Details", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    if (onEditInvoiceClick != null && (currentInvoice.status == "UNPAID" || currentInvoice.status == "PARTIALLY_PAID")) {
                        IconButton(onClick = { onEditInvoiceClick(currentInvoice.id) }) {
                            Icon(Icons.Rounded.Edit, contentDescription = "Edit Bill")
                        }
                    }

                    IconButton(onClick = onWhatsAppShareClick) {
                        Icon(
                            imageVector = Icons.Rounded.Share,
                            contentDescription = "Share on WhatsApp",
                            tint = Color(0xFF25D366)
                        )
                    }

                    IconButton(onClick = onStandardShareClick) {
                        Icon(Icons.Rounded.Share, contentDescription = "Share PDF")
                    }

                    IconButton(onClick = { showDeleteDialog = true }) {
                        Icon(Icons.Rounded.Delete, contentDescription = "Delete", tint = MaterialTheme.colorScheme.error)
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Header Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Bill #${currentInvoice.billNumber}",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )

                        InvoiceStatusBadge(status = currentInvoice.status, balance = balanceDue)
                    }

                    Text(
                        text = "Issued: ${DateUtils.formatDateTime(currentInvoice.createdAt)}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // Customer Info Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Rounded.Person, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Customer Info", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    }

                    HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))

                    Text(
                        text = customer?.name ?: "Customer #${currentInvoice.customerId}",
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.SemiBold
                    )

                    if (!customer?.phone.isNullOrEmpty()) {
                        Text(
                            text = "Phone: ${customer.phone}",
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }

                    if (!customer?.address.isNullOrEmpty()) {
                        Text(
                            text = "Address: ${customer.address}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            // Bill Summary & Balance Breakdown
            Card(
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text("Bill Details", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)

                    HorizontalDivider()

                    Text(
                        text = currentInvoice.description,
                        style = MaterialTheme.typography.bodyLarge
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Subtotal:", style = MaterialTheme.typography.bodyMedium)
                        Text(CurrencyFormatter.formatCurrency(currentInvoice.amount), style = MaterialTheme.typography.bodyMedium)
                    }

                    if (currentInvoice.discount > 0) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Discount:", style = MaterialTheme.typography.bodyMedium)
                            Text("- ${CurrencyFormatter.formatCurrency(currentInvoice.discount)}", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.error)
                        }
                    }

                    HorizontalDivider()

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Total Amount:", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        Text(CurrencyFormatter.formatCurrency(currentInvoice.total), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Received Amount:", style = MaterialTheme.typography.bodyMedium)
                        Text(CurrencyFormatter.formatCurrency(totalPaid), style = MaterialTheme.typography.bodyMedium, color = Color(0xFF15803D), fontWeight = FontWeight.SemiBold)
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Outstanding Balance:", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        Text(
                            text = CurrencyFormatter.formatCurrency(balanceDue),
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = if (balanceDue > 0) MaterialTheme.colorScheme.error else Color(0xFF15803D)
                        )
                    }
                }
            }

            // Payment History Section
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Rounded.Payments, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Payment History", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        }

                        if (balanceDue > 0) {
                            Button(
                                onClick = { showAddPaymentDialog = true },
                                contentPadding = ButtonDefaults.ButtonWithIconContentPadding
                            ) {
                                Icon(Icons.Rounded.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Add Payment")
                            }
                        }
                    }

                    HorizontalDivider()

                    if (payments.isEmpty()) {
                        Text(
                            text = "No payments recorded yet.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.outline,
                            modifier = Modifier.padding(vertical = 8.dp)
                        )
                    } else {
                        payments.forEach { payment ->
                            PaymentTimelineItem(
                                payment = payment,
                                onDeletePayment = { paymentToDelete = payment }
                            )
                        }
                    }
                }
            }

            // Prominent Action Buttons: WhatsApp + Share PDF
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Button(
                    onClick = onWhatsAppShareClick,
                    modifier = Modifier
                        .weight(1f)
                        .height(50.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF25D366),
                        contentColor = Color.White
                    )
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Share,
                        contentDescription = "Share via WhatsApp",
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("WhatsApp", fontWeight = FontWeight.Bold)
                }

                OutlinedButton(
                    onClick = onStandardShareClick,
                    modifier = Modifier
                        .weight(1f)
                        .height(50.dp)
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Share,
                        contentDescription = "Share PDF",
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Share PDF", fontWeight = FontWeight.Bold)
                }
            }
        }
    }

    if (showAddPaymentDialog) {
        AddPaymentDialog(
            paymentViewModel = paymentViewModel,
            customerViewModel = customerViewModel,
            preSelectedCustomerId = currentInvoice.customerId,
            onDismissRequest = { showAddPaymentDialog = false }
        )
    }

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Delete Bill / Invoice?") },
            text = { Text("Are you sure you want to delete bill #${currentInvoice.billNumber}? Payments recorded against this bill may remain in ledger.") },
            confirmButton = {
                Button(
                    onClick = {
                        showDeleteDialog = false
                        invoiceViewModel.deleteInvoice(currentInvoice)
                        onBackClick()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("Delete")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    val targetPaymentToDelete = paymentToDelete
    if (targetPaymentToDelete != null) {
        AlertDialog(
            onDismissRequest = { paymentToDelete = null },
            title = { Text("Delete Payment?") },
            text = { Text("Are you sure you want to delete this payment of ${CurrencyFormatter.formatCurrency(targetPaymentToDelete.amount)}? The bill's outstanding balance will be updated automatically.") },
            confirmButton = {
                Button(
                    onClick = {
                        paymentViewModel.deletePayment(targetPaymentToDelete)
                        paymentToDelete = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("Delete")
                }
            },
            dismissButton = {
                TextButton(onClick = { paymentToDelete = null }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
fun PaymentTimelineItem(
    payment: Payment,
    onDeletePayment: (() -> Unit)? = null
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(32.dp)
                .clip(CircleShape)
                .background(Color(0xFFDCFCE7)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Rounded.CheckCircle,
                contentDescription = null,
                tint = Color(0xFF15803D),
                modifier = Modifier.size(20.dp)
            )
        }

        Spacer(modifier = Modifier.width(12.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = CurrencyFormatter.formatCurrency(payment.amount),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF15803D)
            )

            Text(
                text = "Mode: ${payment.paymentMode.replace("_", " ")}" + (if (payment.notes.isNotEmpty()) " • ${payment.notes}" else ""),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Text(
                text = DateUtils.formatDate(payment.date),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.outline
            )
        }

        if (onDeletePayment != null) {
            IconButton(onClick = onDeletePayment) {
                Icon(
                    imageVector = Icons.Rounded.Delete,
                    contentDescription = "Delete Payment",
                    tint = MaterialTheme.colorScheme.error,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}
