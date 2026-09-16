package com.example.hisabbook.ui.quotation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
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
import com.example.hisabbook.ui.viewmodel.QuotationViewModel
import com.example.hisabbook.util.CurrencyFormatter
import com.example.hisabbook.util.parseCleanDouble
import com.example.hisabbook.util.parseCleanDoubleOrDefault

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun CreateQuotationScreen(
    quotationViewModel: QuotationViewModel,
    preSelectedCustomerId: Long? = null,
    quotationIdToEdit: Long? = null,
    onBackClick: () -> Unit,
    onQuotationCreated: (Long) -> Unit
) {
    val customers by quotationViewModel.customers.collectAsStateWithLifecycle()
    val inventoryItems by quotationViewModel.inventoryItems.collectAsStateWithLifecycle()
    val allQuotations by quotationViewModel.allQuotations.collectAsStateWithLifecycle()

    val quotationToEdit = remember(quotationIdToEdit, allQuotations) {
        quotationIdToEdit?.let { qId -> allQuotations.find { it.id == qId } }
    }

    val genericUnits = remember {
        listOf(
            "pcs", "kg", "meter", "feet", "sq.ft", "sq.m",
            "box", "ltr", "doz", "packet", "bag", "set",
            "pair", "hrs", "job", "service", "work"
        )
    }

    var selectedCustomerId by remember(preSelectedCustomerId, quotationToEdit) {
        mutableLongStateOf(
            quotationToEdit?.customerId ?: preSelectedCustomerId ?: customers.firstOrNull()?.id ?: 0L
        )
    }

    val displayQuotationNum = remember(quotationToEdit) {
        quotationToEdit?.quotationNumber ?: quotationViewModel.generateNextQuotationNumber()
    }

    var description by remember(quotationToEdit) { mutableStateOf(quotationToEdit?.description ?: "") }
    var quantityText by remember(quotationToEdit) { mutableStateOf(quotationToEdit?.quantity?.toString() ?: "1.0") }
    var rateText by remember(quotationToEdit) { mutableStateOf(quotationToEdit?.amount?.let { CurrencyFormatter.formatAmountOnly(it) } ?: "") }
    var discountText by remember(quotationToEdit) { mutableStateOf(quotationToEdit?.discount?.let { CurrencyFormatter.formatAmountOnly(it) } ?: "0.0") }
    var notes by remember(quotationToEdit) { mutableStateOf(quotationToEdit?.notes ?: "") }
    var selectedStatus by remember(quotationToEdit) { mutableStateOf(quotationToEdit?.status ?: "Draft") }

    var lineItems by remember { mutableStateOf(listOf<InvoiceLineItem>()) }
    var itemMode by remember { mutableStateOf("CUSTOM") } // "INVENTORY" or "CUSTOM"

    var selectedInventoryItem by remember { mutableStateOf<InventoryItem?>(null) }
    var itemDescriptionState by remember { mutableStateOf("") }
    var unitRateState by remember { mutableStateOf("") }
    var unitState by remember { mutableStateOf("pcs") }
    var itemQuantityState by remember { mutableStateOf("1") }

    var inventoryDropdownExpanded by remember { mutableStateOf(false) }
    var showAddNewCustomerDialog by remember { mutableStateOf(false) }

    var descriptionError by remember { mutableStateOf<String?>(null) }
    var customerError by remember { mutableStateOf<String?>(null) }
    var rateError by remember { mutableStateOf<String?>(null) }

    val statusOptions = listOf("Draft", "Sent", "Approved", "Rejected")

    fun updateSubtotalFromLineItems(items: List<InvoiceLineItem>) {
        if (items.isNotEmpty()) {
            val totalSubtotal = items.sumOf { it.totalAmount }
            rateText = CurrencyFormatter.formatAmountOnly(totalSubtotal)
            quantityText = "1.0"
            description = items.joinToString(", ") { "${it.description} (Qty: ${CurrencyFormatter.formatAmountOnly(it.quantity)} ${it.unit})" }
            descriptionError = null
            rateError = null
        }
    }

    val quantity = quantityText.parseCleanDoubleOrDefault(1.0)
    val rate = rateText.parseCleanDoubleOrDefault(0.0)
    val discount = discountText.parseCleanDoubleOrDefault(0.0)
    val total = ((quantity * rate) - discount).coerceAtLeast(0.0)

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (quotationToEdit != null) "Edit Quotation" else "New Quotation", fontWeight = FontWeight.Bold) },
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
            // Quotation # banner
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
                    Text("Quotation Number:", style = MaterialTheme.typography.bodyMedium)
                    Text(
                        text = displayQuotationNum,
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
                    customerError = null
                },
                isError = customerError != null,
                errorMessage = customerError,
                onAddNewCustomer = { showAddNewCustomerDialog = true }
            )

            // Initial / Current Status Choice
            Text("Quotation Status", style = MaterialTheme.typography.labelMedium)
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                statusOptions.forEach { status ->
                    FilterChip(
                        selected = selectedStatus.equals(status, ignoreCase = true),
                        onClick = { selectedStatus = status },
                        label = { Text(status) }
                    )
                }
            }

            // --- Add Items Section (From Inventory OR Custom/Manual) ---
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
                            text = "Add Items to Quotation",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    // Mode Toggle: Inventory vs Custom Item
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        FilterChip(
                            selected = itemMode == "INVENTORY",
                            onClick = { itemMode = "INVENTORY" },
                            label = { Text("From Inventory") }
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
                            label = { Text("Custom / Manual Work") }
                        )
                    }

                    if (itemMode == "INVENTORY") {
                        // Inventory Item Picker Dropdown
                        ExposedDropdownMenuBox(
                            expanded = inventoryDropdownExpanded,
                            onExpandedChange = { inventoryDropdownExpanded = it }
                        ) {
                            val currentItemLabel = selectedInventoryItem?.let {
                                "${it.name} (Stock: ${CurrencyFormatter.formatAmountOnly(it.quantity)} ${it.unit})"
                            } ?: "Select Stock Item..."

                            OutlinedTextField(
                                value = currentItemLabel,
                                onValueChange = {},
                                readOnly = true,
                                label = { Text("Inventory Item") },
                                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = inventoryDropdownExpanded) },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .menuAnchor(MenuAnchorType.PrimaryNotEditable)
                            )

                            ExposedDropdownMenu(
                                expanded = inventoryDropdownExpanded,
                                onDismissRequest = { inventoryDropdownExpanded = false }
                            ) {
                                inventoryItems.forEach { item ->
                                    DropdownMenuItem(
                                        text = {
                                            Column {
                                                Text(item.name, fontWeight = FontWeight.Bold)
                                                Text(
                                                    "Stock: ${CurrencyFormatter.formatAmountOnly(item.quantity)} ${item.unit} • Rate: ${CurrencyFormatter.formatCurrency(item.purchasePrice)}",
                                                    style = MaterialTheme.typography.bodySmall,
                                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                                )
                                            }
                                        },
                                        onClick = {
                                            selectedInventoryItem = item
                                            itemDescriptionState = item.name
                                            unitRateState = CurrencyFormatter.formatAmountOnly(item.purchasePrice)
                                            unitState = item.unit
                                            itemQuantityState = "1"
                                            inventoryDropdownExpanded = false
                                        }
                                    )
                                }
                            }
                        }
                    }

                    // Item Description input
                    OutlinedTextField(
                        value = itemDescriptionState,
                        onValueChange = { itemDescriptionState = it },
                        label = { Text(if (itemMode == "INVENTORY") "Item Description" else "Custom Work / Item Name") },
                        placeholder = { Text(if (itemMode == "INVENTORY") "e.g. Aluminium Section 2 Inch" else "e.g. Sliding Window installation labor, Glass work") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedTextField(
                            value = itemQuantityState,
                            onValueChange = { itemQuantityState = it },
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
                            label = { Text("Rate (₹)") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            singleLine = true,
                            modifier = Modifier.weight(1.2f)
                        )
                    }

                    // Calculated Item Total Preview
                    val itemQtyVal = itemQuantityState.parseCleanDoubleOrDefault(0.0)
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
                                    inventoryItemId = selectedInventoryItem?.id,
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
                                itemQuantityState = "1"
                                if (itemMode == "CUSTOM") {
                                    unitState = "pcs"
                                }
                            }
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Rounded.Add, contentDescription = null)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Add Item to Quotation")
                    }

                    // List of Added Line Items
                    if (lineItems.isNotEmpty()) {
                        HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))
                        Text("Quotation Line Items (${lineItems.size}):", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold)

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
                                            "${CurrencyFormatter.formatAmountOnly(item.quantity)} ${item.unit} @ ${CurrencyFormatter.formatCurrency(item.unitRate)}",
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

            // Work Description / Items summary
            OutlinedTextField(
                value = description,
                onValueChange = {
                    description = it
                    if (it.isNotBlank()) descriptionError = null
                },
                label = { Text("Work Description / Summary *") },
                placeholder = { Text("e.g. Aluminium Sliding Windows 3Track - 120 sq ft") },
                isError = descriptionError != null,
                supportingText = descriptionError?.let { { Text(it) } },
                minLines = 2,
                maxLines = 4,
                modifier = Modifier.fillMaxWidth()
            )

            // Quantity & Rate row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedTextField(
                    value = quantityText,
                    onValueChange = { quantityText = it },
                    label = { Text("Quantity") },
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Decimal,
                        imeAction = ImeAction.Next
                    ),
                    singleLine = true,
                    modifier = Modifier.weight(1f)
                )

                OutlinedTextField(
                    value = rateText,
                    onValueChange = {
                        rateText = it
                        val clean = it.parseCleanDouble()
                        if (clean != null && clean > 0) rateError = null
                    },
                    label = { Text("Rate / Amount (₹) *") },
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Decimal,
                        imeAction = ImeAction.Next
                    ),
                    isError = rateError != null,
                    supportingText = rateError?.let { { Text(it) } },
                    singleLine = true,
                    modifier = Modifier.weight(1f)
                )
            }

            // Discount
            OutlinedTextField(
                value = discountText,
                onValueChange = { discountText = it },
                label = { Text("Discount (₹)") },
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Decimal,
                    imeAction = ImeAction.Next
                ),
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

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
                        Text("Subtotal:", style = MaterialTheme.typography.bodyMedium)
                        Text(
                            text = CurrencyFormatter.formatCurrency(quantity * rate),
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }

                    if (discount > 0) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Discount:", style = MaterialTheme.typography.bodyMedium)
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
                            "Net Total Amount:",
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

            // Notes
            OutlinedTextField(
                value = notes,
                onValueChange = { notes = it },
                label = { Text("Notes / Terms (Optional)") },
                placeholder = { Text("e.g. Valid for 15 days from quote date.") },
                minLines = 2,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Save / Update Button
            Button(
                onClick = {
                    var isValid = true
                    if (selectedCustomerId <= 0) {
                        customerError = "Please select a customer"
                        isValid = false
                    }
                    if (description.isBlank()) {
                        descriptionError = "Please enter work description"
                        isValid = false
                    }
                    if (rate <= 0) {
                        rateError = "Enter valid rate"
                        isValid = false
                    }

                    if (isValid) {
                        if (quotationToEdit != null) {
                            quotationViewModel.updateQuotation(
                                id = quotationToEdit.id,
                                customerId = selectedCustomerId,
                                description = description.trim(),
                                quantity = quantity,
                                amount = rate,
                                discount = discount,
                                notes = notes.trim(),
                                status = selectedStatus,
                                onSuccess = {
                                    onQuotationCreated(quotationToEdit.id)
                                }
                            )
                        } else {
                            quotationViewModel.createQuotation(
                                customerId = selectedCustomerId,
                                description = description.trim(),
                                quantity = quantity,
                                amount = rate,
                                discount = discount,
                                notes = notes.trim(),
                                status = selectedStatus,
                                onSuccess = { createdId ->
                                    onQuotationCreated(createdId)
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
                Text(if (quotationToEdit != null) "Update Quotation" else "Save Quotation", fontWeight = FontWeight.Bold)
            }
        }
    }

    if (showAddNewCustomerDialog) {
        AddEditCustomerDialog(
            customerToEdit = null,
            onDismissRequest = { showAddNewCustomerDialog = false },
            onSaveCustomer = { _, name, phone, address, onError ->
                quotationViewModel.saveCustomer(
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
