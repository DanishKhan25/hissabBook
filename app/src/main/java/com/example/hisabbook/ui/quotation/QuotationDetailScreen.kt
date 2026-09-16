package com.example.hisabbook.ui.quotation

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
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material.icons.rounded.ReceiptLong
import androidx.compose.material.icons.rounded.Share
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.hisabbook.R
import com.example.hisabbook.ui.viewmodel.QuotationViewModel
import com.example.hisabbook.ui.viewmodel.SettingsViewModel
import com.example.hisabbook.util.CurrencyFormatter
import com.example.hisabbook.util.DateUtils
import com.example.hisabbook.util.PdfGenerator
import com.example.hisabbook.util.PdfShareHelper

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QuotationDetailScreen(
    quotationId: Long,
    quotationViewModel: QuotationViewModel,
    settingsViewModel: SettingsViewModel? = null,
    onBackClick: () -> Unit,
    onInvoiceCreated: (Long) -> Unit,
    onEditQuotationClick: ((Long) -> Unit)? = null
) {
    val context = LocalContext.current
    val quotation by quotationViewModel.getQuotationById(quotationId).collectAsStateWithLifecycle(initialValue = null)
    val customers by quotationViewModel.customers.collectAsStateWithLifecycle()
    val businessProfile by (settingsViewModel?.businessProfile ?: quotationViewModel.businessProfile).collectAsStateWithLifecycle()

    var showStatusMenu by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }

    val currentQuotation = quotation
    val customer = customers.find { it.id == currentQuotation?.customerId }

    if (currentQuotation == null) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("Quotation Details") },
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
                Text("Quotation not found")
            }
        }
        return
    }

    val onWhatsAppShareClick = {
        val pdfFile = PdfGenerator.generateQuotationPdf(
            context = context,
            quotation = currentQuotation,
            customer = customer,
            businessProfile = businessProfile
        )
        val defaultName = context.getString(R.string.app_name)
        val bName = businessProfile?.businessName?.takeIf { it.isNotBlank() } ?: defaultName
        val bPhone = businessProfile?.phone ?: ""
        val custName = customer?.name?.ifBlank { "Customer" } ?: "Customer"
        val custPhone = customer?.phone
        val qtnDate = DateUtils.formatDate(currentQuotation.createdAt)

        PdfShareHelper.shareToWhatsApp(
            context = context,
            file = pdfFile,
            customerName = custName,
            customerPhone = custPhone,
            docType = "Quotation",
            docNumber = currentQuotation.quotationNumber,
            docDate = qtnDate,
            totalAmount = currentQuotation.total,
            businessName = bName,
            businessPhone = bPhone
        )
    }

    val onStandardShareClick = {
        val pdfFile = PdfGenerator.generateQuotationPdf(
            context = context,
            quotation = currentQuotation,
            customer = customer,
            businessProfile = businessProfile
        )
        PdfShareHelper.sharePdf(context, pdfFile, "Quotation ${currentQuotation.quotationNumber}")
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Quotation Details", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    if (onEditQuotationClick != null && !currentQuotation.status.equals("CONVERTED", ignoreCase = true)) {
                        IconButton(onClick = { onEditQuotationClick(currentQuotation.id) }) {
                            Icon(Icons.Rounded.Edit, contentDescription = "Edit Quotation")
                        }
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
                            text = currentQuotation.quotationNumber,
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )

                        Box {
                            OutlinedButton(onClick = { showStatusMenu = true }) {
                                Text(currentQuotation.status)
                                Icon(Icons.Rounded.Edit, contentDescription = "Change Status", modifier = Modifier.padding(start = 4.dp))
                            }

                            DropdownMenu(
                                expanded = showStatusMenu,
                                onDismissRequest = { showStatusMenu = false }
                            ) {
                                listOf("Draft", "Sent", "Approved", "Rejected").forEach { statusChoice ->
                                    DropdownMenuItem(
                                        text = { Text(statusChoice) },
                                        onClick = {
                                            quotationViewModel.updateQuotationStatus(currentQuotation.id, statusChoice)
                                            showStatusMenu = false
                                        }
                                    )
                                }
                            }
                        }
                    }

                    Text(
                        text = "Created: ${DateUtils.formatDateTime(currentQuotation.createdAt)}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // Customer Details Card
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
                        text = customer?.name ?: "Customer #${currentQuotation.customerId}",
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

            // Line Items Breakdown Card
            Card(
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text("Quotation Items", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)

                    HorizontalDivider()

                    Text(
                        text = currentQuotation.description,
                        style = MaterialTheme.typography.bodyLarge
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "Qty: ${currentQuotation.quantity}  @ ₹${CurrencyFormatter.formatAmountOnly(currentQuotation.amount)}",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = CurrencyFormatter.formatCurrency(currentQuotation.quantity * currentQuotation.amount),
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                    }

                    if (currentQuotation.discount > 0) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Discount:", style = MaterialTheme.typography.bodyMedium)
                            Text(
                                text = "- ${CurrencyFormatter.formatCurrency(currentQuotation.discount)}",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.error
                            )
                        }
                    }

                    HorizontalDivider()

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Total Amount:", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                        Text(
                            text = CurrencyFormatter.formatCurrency(currentQuotation.total),
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }

            if (currentQuotation.notes.isNotEmpty()) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("Notes & Terms", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(currentQuotation.notes, style = MaterialTheme.typography.bodyMedium)
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Action Buttons
            if (!currentQuotation.status.equals("CONVERTED", ignoreCase = true)) {
                Button(
                    onClick = {
                        quotationViewModel.convertToInvoice(currentQuotation.id) { createdInvoiceId ->
                            onInvoiceCreated(createdInvoiceId)
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary)
                ) {
                    Icon(Icons.Rounded.ReceiptLong, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Convert to Bill / Invoice (1-Tap)", fontWeight = FontWeight.Bold)
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

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Delete Quotation?") },
            text = { Text("Are you sure you want to delete quotation ${currentQuotation.quotationNumber}? This action cannot be undone.") },
            confirmButton = {
                Button(
                    onClick = {
                        showDeleteDialog = false
                        quotationViewModel.deleteQuotation(currentQuotation)
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
}
