package com.example.hisabbook.ui.payment

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.Note
import androidx.compose.material.icons.rounded.AttachMoney
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.hisabbook.ui.viewmodel.CustomerViewModel
import com.example.hisabbook.ui.viewmodel.PaymentViewModel
import com.example.hisabbook.util.CurrencyFormatter
import com.example.hisabbook.util.DateUtils
import com.example.hisabbook.util.parseCleanDouble

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun AddPaymentDialog(
    paymentViewModel: PaymentViewModel,
    customerViewModel: CustomerViewModel? = null,
    preSelectedCustomerId: Long? = null,
    onDismissRequest: () -> Unit
) {
    val customersWithBalance = customerViewModel?.customersWithBalance?.collectAsStateWithLifecycle()?.value ?: emptyList()

    var selectedCustomerId by remember(preSelectedCustomerId) {
        mutableLongStateOf(preSelectedCustomerId ?: customersWithBalance.firstOrNull()?.customer?.id ?: 0L)
    }

    val unpaidInvoices = paymentViewModel.getUnpaidInvoicesForCustomer(selectedCustomerId)
        .collectAsStateWithLifecycle().value

    var selectedBillId by remember(selectedCustomerId) { mutableStateOf<Long?>(null) }
    var amountText by remember { mutableStateOf("") }
    var paymentMode by remember { mutableStateOf("CASH") }
    var notes by remember { mutableStateOf("") }

    var customerDropdownExpanded by remember { mutableStateOf(false) }
    var billDropdownExpanded by remember { mutableStateOf(false) }

    var amountError by remember { mutableStateOf<String?>(null) }

    val paymentModes = listOf("CASH", "UPI", "BANK_TRANSFER", "CHEQUE", "OTHER")

    AlertDialog(
        onDismissRequest = onDismissRequest,
        title = { Text("Record Payment", style = MaterialTheme.typography.headlineSmall) },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Customer Selector (if not fixed)
                if (preSelectedCustomerId == null && customersWithBalance.isNotEmpty()) {
                    ExposedDropdownMenuBox(
                        expanded = customerDropdownExpanded,
                        onExpandedChange = { customerDropdownExpanded = it }
                    ) {
                        val currentCustomerName = customersWithBalance.find { it.customer.id == selectedCustomerId }?.customer?.name ?: "Select Customer"
                        OutlinedTextField(
                            value = currentCustomerName,
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Select Customer *") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = customerDropdownExpanded) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .menuAnchor(MenuAnchorType.PrimaryNotEditable)
                        )
                        ExposedDropdownMenu(
                            expanded = customerDropdownExpanded,
                            onDismissRequest = { customerDropdownExpanded = false }
                        ) {
                            customersWithBalance.forEach { item ->
                                DropdownMenuItem(
                                    text = {
                                        Column {
                                            Text(item.customer.name, style = MaterialTheme.typography.bodyLarge)
                                            if (item.outstandingBalance > 0) {
                                                Text(
                                                    "Pending: ${CurrencyFormatter.formatCurrency(item.outstandingBalance)}",
                                                    style = MaterialTheme.typography.labelSmall,
                                                    color = MaterialTheme.colorScheme.error
                                                )
                                            }
                                        }
                                    },
                                    onClick = {
                                        selectedCustomerId = item.customer.id
                                        selectedBillId = null
                                        customerDropdownExpanded = false
                                    }
                                )
                            }
                        }
                    }
                }

                // Bill Selector
                ExposedDropdownMenuBox(
                    expanded = billDropdownExpanded,
                    onExpandedChange = { billDropdownExpanded = it }
                ) {
                    val selectedBillNumber = unpaidInvoices.find { it.id == selectedBillId }?.let {
                        "Bill #${it.billNumber} (${CurrencyFormatter.formatCurrency(it.total)})"
                    } ?: "General Payment (No specific bill)"

                    OutlinedTextField(
                        value = selectedBillNumber,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Link to Bill (Optional)") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = billDropdownExpanded) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor(MenuAnchorType.PrimaryNotEditable)
                    )

                    ExposedDropdownMenu(
                        expanded = billDropdownExpanded,
                        onDismissRequest = { billDropdownExpanded = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("General Payment (No specific bill)") },
                            onClick = {
                                selectedBillId = null
                                billDropdownExpanded = false
                            }
                        )
                        unpaidInvoices.forEach { invoice ->
                            DropdownMenuItem(
                                text = { Text("Bill #${invoice.billNumber} - Total: ${CurrencyFormatter.formatCurrency(invoice.total)}") },
                                onClick = {
                                    selectedBillId = invoice.id
                                    amountText = CurrencyFormatter.formatAmountOnly(invoice.total)
                                    billDropdownExpanded = false
                                }
                            )
                        }
                    }
                }

                // Amount
                OutlinedTextField(
                    value = amountText,
                    onValueChange = {
                        amountText = it
                        val clean = it.parseCleanDouble()
                        if (clean != null && clean > 0) amountError = null
                    },
                    label = { Text("Payment Amount (₹) *") },
                    leadingIcon = { Icon(Icons.Rounded.AttachMoney, contentDescription = null) },
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Decimal,
                        imeAction = ImeAction.Next
                    ),
                    isError = amountError != null,
                    supportingText = amountError?.let { { Text(it) } },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                // Payment Mode Choice Chips
                Text("Payment Mode", style = MaterialTheme.typography.labelMedium)
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    paymentModes.forEach { mode ->
                        FilterChip(
                            selected = paymentMode == mode,
                            onClick = { paymentMode = mode },
                            label = { Text(mode.replace("_", " ")) }
                        )
                    }
                }

                // Date display
                Text(
                    text = "Payment Date: ${DateUtils.formatDate(System.currentTimeMillis())}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                // Notes
                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("Notes / Reference (Optional)") },
                    leadingIcon = { Icon(Icons.AutoMirrored.Rounded.Note, contentDescription = null) },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    amountError = null
                    val amount = amountText.parseCleanDouble()
                    if (amount == null || amount <= 0) {
                        amountError = "Payment amount must be greater than ₹0"
                    } else if (selectedCustomerId <= 0) {
                        amountError = "Please select a valid customer"
                    } else {
                        paymentViewModel.recordPayment(
                            customerId = selectedCustomerId,
                            billId = selectedBillId,
                            amount = amount,
                            paymentMode = paymentMode,
                            notes = notes,
                            onSuccess = onDismissRequest,
                            onError = { err -> amountError = err }
                        )
                    }
                }
            ) {
                Text("Record Payment")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismissRequest) {
                Text("Cancel")
            }
        }
    )
}
