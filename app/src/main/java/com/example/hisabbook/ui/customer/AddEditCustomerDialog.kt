package com.example.hisabbook.ui.customer

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Call
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.hisabbook.data.local.entity.Customer

@Composable
fun AddEditCustomerDialog(
    customerToEdit: Customer? = null,
    onDismissRequest: () -> Unit,
    onSaveCustomer: (id: Long, name: String, phone: String, address: String, onError: (String) -> Unit) -> Unit
) {
    var name by remember(customerToEdit) { mutableStateOf(customerToEdit?.name ?: "") }
    var phone by remember(customerToEdit) { mutableStateOf(customerToEdit?.phone ?: "") }
    var address by remember(customerToEdit) { mutableStateOf(customerToEdit?.address ?: "") }
    var nameError by remember { mutableStateOf<String?>(null) }
    var phoneError by remember { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = onDismissRequest,
        title = {
            Text(
                text = if (customerToEdit == null) "Add New Customer" else "Edit Customer",
                style = MaterialTheme.typography.headlineSmall
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedTextField(
                    value = name,
                    onValueChange = {
                        name = it
                        if (it.isNotBlank()) nameError = null
                    },
                    label = { Text("Customer Name *") },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Rounded.Person,
                            contentDescription = null
                        )
                    },
                    isError = nameError != null,
                    supportingText = nameError?.let { { Text(it) } },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = phone,
                    onValueChange = {
                        phone = it
                        phoneError = null
                    },
                    label = { Text("Mobile Number (10 digits)") },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Rounded.Call,
                            contentDescription = null
                        )
                    },
                    isError = phoneError != null,
                    supportingText = phoneError?.let { { Text(it) } },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Phone,
                        imeAction = ImeAction.Next
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = address,
                    onValueChange = { address = it },
                    label = { Text("Address") },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Rounded.Home,
                            contentDescription = null
                        )
                    },
                    maxLines = 3,
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    nameError = null
                    phoneError = null
                    val trimmedName = name.trim()
                    val trimmedPhone = phone.trim()
                    if (trimmedName.isBlank()) {
                        nameError = "Customer name is required"
                    } else if (trimmedPhone.isNotBlank() && trimmedPhone.filter { it.isDigit() }.length != 10) {
                        phoneError = "Please enter a valid 10-digit mobile number"
                    } else {
                        onSaveCustomer(customerToEdit?.id ?: 0L, trimmedName, trimmedPhone, address) { err ->
                            if (err.contains("mobile", ignoreCase = true) || err.contains("phone", ignoreCase = true) || err.contains("exists", ignoreCase = true)) {
                                phoneError = err
                            } else {
                                nameError = err
                            }
                        }
                    }
                }
            ) {
                Text("Save")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismissRequest) {
                Text("Cancel")
            }
        }
    )
}
