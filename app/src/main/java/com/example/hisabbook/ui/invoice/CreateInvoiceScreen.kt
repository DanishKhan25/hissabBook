package com.example.hisabbook.ui.invoice

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import com.example.hisabbook.ui.inventory.SearchableInventoryPicker
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.Inventory
import androidx.compose.material.icons.rounded.ReceiptLong
import androidx.compose.material.icons.rounded.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.hisabbook.data.local.entity.InventoryItem
import com.example.hisabbook.data.local.entity.InvoiceLineItem
import com.example.hisabbook.ui.customer.AddEditCustomerDialog
import com.example.hisabbook.ui.customer.SearchableCustomerPicker
import com.example.hisabbook.ui.viewmodel.InvoiceViewModel
import com.example.hisabbook.util.CurrencyFormatter
import com.example.hisabbook.util.parseCleanDouble
import com.example.hisabbook.util.parseCleanDoubleOrDefault

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun CreateInvoiceScreen(
    invoiceViewModel: InvoiceViewModel,
    preSelectedCustomerId: Long? = null,
    preSelectedQuotationId: Long? = null,
    invoiceIdToEdit: Long? = null,
    onBackClick: () -> Unit,
    onInvoiceCreated: (Long) -> Unit
) {
    val customers by invoiceViewModel.customers.collectAsStateWithLifecycle()
    val quotations by invoiceViewModel.quotations.collectAsStateWithLifecycle()
    val inventoryItems by invoiceViewModel.inventoryItems.collectAsStateWithLifecycle()
    val allInvoices by invoiceViewModel.allInvoices.collectAsStateWithLifecycle()

    val invoiceToEdit = remember(invoiceIdToEdit, allInvoices) {
        invoiceIdToEdit?.let { invId -> allInvoices.find { it.id == invId } }
    }

    var selectedCustomerId by remember(preSelectedCustomerId, invoiceToEdit) {
        mutableLongStateOf(
            invoiceToEdit?.customerId ?: preSelectedCustomerId ?: customers.firstOrNull()?.id ?: 0L
        )
    }

    var selectedQuotationId by remember(preSelectedQuotationId, invoiceToEdit) {
        mutableStateOf(invoiceToEdit?.quotationId ?: preSelectedQuotationId)
    }

    val displayBillNum = remember(invoiceToEdit) {
        invoiceToEdit?.billNumber ?: invoiceViewModel.generateNextBillNumber()
    }

    val genericUnits = remember {
        listOf(
            "pcs", "kg", "meter", "feet", "sq.ft", "sq.m",
            "box", "ltr", "doz", "packet", "bag", "set",
            "pair", "hrs", "job", "service", "work"
        )
    }

    var lineItems by remember { mutableStateOf(listOf<InvoiceLineItem>()) }
    var itemMode by remember { mutableStateOf("INVENTORY") } // "INVENTORY" or "CUSTOM"

    var selectedInventoryItem by remember { mutableStateOf<InventoryItem?>(null) }
    var itemDescriptionState by remember { mutableStateOf("") }
    var unitRateState by remember { mutableStateOf("") }
    var unitState by remember { mutableStateOf("pcs") }
    var quantityState by remember { mutableStateOf("1") }

    var quotationDropdownExpanded by remember { mutableStateOf(false) }
    var showAddNewCustomerDialog by remember { mutableStateOf(false) }

    var description by remember(invoiceToEdit) { mutableStateOf(invoiceToEdit?.description ?: "") }
    var amountText by remember(invoiceToEdit) { mutableStateOf(invoiceToEdit?.amount?.let { CurrencyFormatter.formatAmountOnly(it) } ?: "") }
    var discountText by remember(invoiceToEdit) { mutableStateOf(invoiceToEdit?.discount?.let { CurrencyFormatter.formatAmountOnly(it) } ?: "0.0") }

    var descriptionError by remember { mutableStateOf<String?>(null) }
    var customerError by remember { mutableStateOf<String?>(null) }
    var amountError by remember { mutableStateOf<String?>(null) }

    val customerQuotations = quotations.filter { it.customerId == selectedCustomerId }

    // If pre-selected quotation is provided, auto-fill
    LaunchedEffect(selectedQuotationId) {
        if (invoiceToEdit == null) {
            val q = quotations.find { it.id == selectedQuotationId }
            if (q != null) {
                description = q.description
                amountText = CurrencyFormatter.formatAmountOnly(q.amount * q.quantity)
                discountText = CurrencyFormatter.formatAmountOnly(q.discount)
            }
        }
    }

    fun updateSubtotalFromLineItems(items: List<InvoiceLineItem>) {
        if (items.isNotEmpty()) {
            val totalSubtotal = items.sumOf { it.totalAmount }
            amountText = CurrencyFormatter.formatAmountOnly(totalSubtotal)
            description = items.joinToString(", ") { "${it.description} (Qty: ${CurrencyFormatter.formatAmountOnly(it.quantity)} ${it.unit})" }
            descriptionError = null
            amountError = null
        }
    }

    val amount = amountText.parseCleanDoubleOrDefault(0.0)
    val discount = discountText.parseCleanDoubleOrDefault(0.0)
    val total = (amount - discount).coerceAtLeast(0.0)

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (invoiceToEdit != null) "Edit Bill / Invoice" else "New Bill / Invoice", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = "Back")
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
            // Bill # banner
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Bill / Invoice Number:", style = MaterialTheme.typography.bodyMedium)
                    Text(
                        text = displayBillNum,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }

            // Searchable Customer Picker
            SearchableCustomerPicker(
                customers = customers,
                selectedCustomerId = selectedCustomerId,
                onCustomerSelected = { customer ->
                    selectedCustomerId = customer.id
                    selectedQuotationId = null
                    customerError = null
                },
                isError = customerError != null,
                errorMessage = customerError,
                onAddNewCustomer = { showAddNewCustomerDialog = true }
            )

            // Optional Linked Quotation Selector
            if (customerQuotations.isNotEmpty()) {
                ExposedDropdownMenuBox(
                    expanded = quotationDropdownExpanded,
                    onExpandedChange = { quotationDropdownExpanded = it }
                ) {
                    val selectedQNum = customerQuotations.find { it.id == selectedQuotationId }?.let {
                        "Linked: ${it.quotationNumber} (${CurrencyFormatter.formatCurrency(it.total)})"
                    } ?: "Link to Quotation (Optional)"

                    OutlinedTextField(
                        value = selectedQNum,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Link to Quotation (Optional)") },
                        leadingIcon = { Icon(Icons.Rounded.ReceiptLong, contentDescription = null) },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = quotationDropdownExpanded) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor(MenuAnchorType.PrimaryNotEditable)
                    )

                    ExposedDropdownMenu(
                        expanded = quotationDropdownExpanded,
                        onDismissRequest = { quotationDropdownExpanded = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("None / Direct Bill") },
                            onClick = {
                                selectedQuotationId = null
                                quotationDropdownExpanded = false
                            }
                        )
                        customerQuotations.forEach { quotation ->
                            DropdownMenuItem(
                                text = { Text("${quotation.quotationNumber} - ${CurrencyFormatter.formatCurrency(quotation.total)}") },
                                onClick = {
                                    selectedQuotationId = quotation.id
                                    description = quotation.description
                                    amountText = CurrencyFormatter.formatAmountOnly(quotation.amount * quotation.quantity)
                                    discountText = CurrencyFormatter.formatAmountOnly(quotation.discount)
                                    quotationDropdownExpanded = false
                                }
                            )
                        }
                    }
                }
            }

            // --- Items Picker & Line Items Card (Inventory + Custom Item Modes) ---
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Inventory,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = "Add Bill Items",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    // Mode Selection Chips
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        FilterChip(
                            selected = itemMode == "INVENTORY",
                            onClick = { itemMode = "INVENTORY" },
                            label = { Text("a) From Inventory Stock") }
                        )
                        FilterChip(
                            selected = itemMode == "CUSTOM",
                            onClick = {
                                itemMode = "CUSTOM"
                                selectedInventoryItem = null
                                if (unitState.isBlank()) {
                                    unitState = "pcs"
                                }
                            },
                            label = { Text("b) Custom / Manual Item") }
                        )
                    }

                    if (itemMode == "INVENTORY") {
                        SearchableInventoryPicker(
                            inventoryItems = inventoryItems,
                            selectedItem = selectedInventoryItem,
                            onItemSelected = { item ->
                                selectedInventoryItem = item
                                itemDescriptionState = item.name
                                unitRateState = CurrencyFormatter.formatAmountOnly(item.purchasePrice)
                                unitState = item.unit
                                quantityState = "1"
                            }
                        )
                    }

                    // Selected Item Fields: Description, Rate, Quantity
                    OutlinedTextField(
                        value = itemDescriptionState,
                        onValueChange = { itemDescriptionState = it },
                        label = { Text(if (itemMode == "INVENTORY") "Item Description" else "Custom Item Name / Service") },
                        placeholder = { Text(if (itemMode == "INVENTORY") "Item details" else "e.g. Glass fabrication, Transport / Labor charges") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedTextField(
                            value = quantityState,
                            onValueChange = { quantityState = it },
                            label = { Text("Quantity") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            singleLine = true,
                            modifier = Modifier.weight(1f)
                        )

                        var unitDropdownExpanded by remember { mutableStateOf(false) }

                        ExposedDropdownMenuBox(
                            expanded = unitDropdownExpanded,
                            onExpandedChange = { unitDropdownExpanded = it },
                            modifier = Modifier.weight(1.2f)
                        ) {
                            OutlinedTextField(
                                value = unitState,
                                onValueChange = {
                                    unitState = it
                                    unitDropdownExpanded = true
                                },
                                label = { Text("Unit") },
                                singleLine = true,
                                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = unitDropdownExpanded) },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .menuAnchor(MenuAnchorType.PrimaryEditable)
                            )

                            val filteredUnits = remember(unitState) {
                                if (unitState.isBlank()) genericUnits
                                else {
                                    val matches = genericUnits.filter { it.contains(unitState, ignoreCase = true) }
                                    matches.ifEmpty { genericUnits }
                                }
                            }

                            ExposedDropdownMenu(
                                expanded = unitDropdownExpanded,
                                onDismissRequest = { unitDropdownExpanded = false }
                            ) {
                                filteredUnits.forEach { u ->
                                    DropdownMenuItem(
                                        text = { Text(u) },
                                        onClick = {
                                            unitState = u
                                            unitDropdownExpanded = false
                                        }
                                    )
                                }
                            }
                        }

                        OutlinedTextField(
                            value = unitRateState,
                            onValueChange = { unitRateState = it },
                            label = { Text("Unit Rate (₹)") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            singleLine = true,
                            modifier = Modifier.weight(1.2f)
                        )
                    }

                    // Inventory Stock Check Warning Banner
                    val itemQtyVal = quantityState.parseCleanDoubleOrDefault(0.0)
                    val currentInventoryItem = selectedInventoryItem
                    if (itemMode == "INVENTORY" && currentInventoryItem != null && itemQtyVal > currentInventoryItem.quantity) {
                        Card(
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.Warning,
                                    contentDescription = "Stock Warning",
                                    tint = MaterialTheme.colorScheme.onErrorContainer
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Warning: Requested quantity (${CurrencyFormatter.formatAmountOnly(itemQtyVal)}) exceeds available stock (${CurrencyFormatter.formatAmountOnly(currentInventoryItem.quantity)} ${currentInventoryItem.unit})!",
                                    style = MaterialTheme.typography.bodySmall,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onErrorContainer
                                )
                            }
                        }
                    }

                    // Calculated Item Total Preview
                    val itemRateVal = unitRateState.parseCleanDoubleOrDefault(0.0)
                    val currentItemTotal = itemQtyVal * itemRateVal

                    if (currentItemTotal > 0) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Item Amount:",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = CurrencyFormatter.formatCurrency(currentItemTotal),
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }

                    OutlinedButton(
                        onClick = {
                            val desc = itemDescriptionState.trim()
                            if (desc.isNotBlank() && itemQtyVal > 0) {
                                val newItem = InvoiceLineItem(
                                    inventoryItemId = if (itemMode == "INVENTORY") selectedInventoryItem?.id else null,
                                    description = desc,
                                    unit = unitState,
                                    unitRate = itemRateVal,
                                    quantity = itemQtyVal
                                )
                                val updatedList = lineItems + newItem
                                lineItems = updatedList
                                updateSubtotalFromLineItems(updatedList)

                                // Reset item inputs
                                selectedInventoryItem = null
                                itemDescriptionState = ""
                                unitRateState = ""
                                quantityState = "1"
                                if (itemMode == "CUSTOM") {
                                    unitState = "pcs"
                                }
                            }
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Rounded.Add, contentDescription = null)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Add Item to Bill")
                    }

                    // List of Added Line Items
                    if (lineItems.isNotEmpty()) {
                        HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))
                        Text("Added Bill Items (${lineItems.size}):", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold)

                        lineItems.forEachIndexed { index, item ->
                            Card(
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(12.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(item.description, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium)
                                        Text(
                                            "${CurrencyFormatter.formatAmountOnly(item.quantity)} ${item.unit} @ ${CurrencyFormatter.formatCurrency(item.unitRate)}" +
                                                    (if (item.inventoryItemId != null) " • [Stock Item]" else " • [Custom Item]"),
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(
                                            CurrencyFormatter.formatCurrency(item.totalAmount),
                                            fontWeight = FontWeight.Bold,
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = MaterialTheme.colorScheme.primary
                                        )
                                        IconButton(
                                            onClick = {
                                                val newList = lineItems.toMutableList().apply { removeAt(index) }
                                                lineItems = newList
                                                updateSubtotalFromLineItems(newList)
                                            }
                                        ) {
                                            Icon(Icons.Rounded.Delete, contentDescription = "Remove Item", tint = MaterialTheme.colorScheme.error)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Description
            OutlinedTextField(
                value = description,
                onValueChange = {
                    description = it
                    if (it.isNotBlank()) descriptionError = null
                },
                label = { Text("Bill Description / Summary *") },
                placeholder = { Text("e.g. Aluminium Fabrication & Glass Installation") },
                isError = descriptionError != null,
                supportingText = descriptionError?.let { { Text(it) } },
                minLines = 2,
                maxLines = 4,
                modifier = Modifier.fillMaxWidth()
            )

            // Amount & Discount
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedTextField(
                    value = amountText,
                    onValueChange = {
                        amountText = it
                        val clean = it.parseCleanDouble()
                        if (clean != null && clean > 0) amountError = null
                    },
                    label = { Text("Subtotal / Amount (₹) *") },
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Decimal,
                        imeAction = ImeAction.Next
                    ),
                    isError = amountError != null,
                    supportingText = amountError?.let { { Text(it) } },
                    singleLine = true,
                    modifier = Modifier.weight(1f)
                )

                OutlinedTextField(
                    value = discountText,
                    onValueChange = { discountText = it },
                    label = { Text("Discount (₹)") },
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Decimal,
                        imeAction = ImeAction.Done
                    ),
                    singleLine = true,
                    modifier = Modifier.weight(1f)
                )
            }

            // Live Calculation Card
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Subtotal Amount:", style = MaterialTheme.typography.bodyMedium)
                        Text(
                            text = CurrencyFormatter.formatCurrency(amount),
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }

                    if (discount > 0) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Discount Applied:", style = MaterialTheme.typography.bodyMedium)
                            Text(
                                text = "- ${CurrencyFormatter.formatCurrency(discount)}",
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            "Net Payable Total:",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                        Text(
                            text = CurrencyFormatter.formatCurrency(total),
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Create / Update Button
            Button(
                onClick = {
                    // Check if there is an unadded item currently filled in form
                    var finalLineItems = lineItems
                    val currentDesc = itemDescriptionState.trim()
                    val currentQty = quantityState.parseCleanDoubleOrDefault(0.0)
                    val currentRate = unitRateState.parseCleanDoubleOrDefault(0.0)

                    if (finalLineItems.isEmpty() && currentDesc.isNotBlank() && currentQty > 0) {
                        finalLineItems = listOf(
                            InvoiceLineItem(
                                inventoryItemId = if (itemMode == "INVENTORY") selectedInventoryItem?.id else null,
                                description = currentDesc,
                                unit = unitState,
                                unitRate = currentRate,
                                quantity = currentQty
                            )
                        )
                        if (amount <= 0) {
                            amountText = CurrencyFormatter.formatAmountOnly(currentQty * currentRate)
                        }
                        if (description.isBlank()) {
                            description = currentDesc
                        }
                    }

                    var isValid = true
                    if (selectedCustomerId <= 0) {
                        customerError = "Please select a customer"
                        isValid = false
                    }
                    if (description.isBlank()) {
                        descriptionError = "Please enter description"
                        isValid = false
                    }
                    val finalAmount = amountText.parseCleanDoubleOrDefault(0.0)
                    if (finalAmount <= 0) {
                        amountError = "Enter valid amount"
                        isValid = false
                    }

                    if (isValid) {
                        if (invoiceToEdit != null) {
                            invoiceViewModel.updateInvoice(
                                invoiceId = invoiceToEdit.id,
                                customerId = selectedCustomerId,
                                description = description.trim(),
                                amount = finalAmount,
                                discount = discount,
                                lineItems = finalLineItems,
                                onSuccess = {
                                    onInvoiceCreated(invoiceToEdit.id)
                                }
                            )
                        } else {
                            invoiceViewModel.createInvoice(
                                customerId = selectedCustomerId,
                                description = description.trim(),
                                amount = finalAmount,
                                discount = discount,
                                quotationId = selectedQuotationId,
                                lineItems = finalLineItems,
                                onSuccess = { createdId ->
                                    onInvoiceCreated(createdId)
                                }
                            )
                        }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
            ) {
                Icon(Icons.Rounded.Check, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text(if (invoiceToEdit != null) "Update Bill / Invoice" else "Create Bill / Invoice", fontWeight = FontWeight.Bold)
            }
        }
    }

    if (showAddNewCustomerDialog) {
        AddEditCustomerDialog(
            customerToEdit = null,
            onDismissRequest = { showAddNewCustomerDialog = false },
            onSaveCustomer = { _, name, phone, address, onError ->
                invoiceViewModel.saveCustomer(
                    name = name,
                    phone = phone,
                    address = address,
                    onSuccess = { createdCustomer ->
                        selectedCustomerId = createdCustomer.id
                        customerError = null
                        showAddNewCustomerDialog = false
                    },
                    onError = onError
                )
            }
        )
    }
}
