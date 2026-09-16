package com.example.hisabbook.ui.invoice

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.hisabbook.R
import com.example.hisabbook.data.local.entity.BusinessProfile
import com.example.hisabbook.data.local.entity.Customer
import com.example.hisabbook.data.local.entity.Invoice
import com.example.hisabbook.data.local.entity.Payment
import com.example.hisabbook.data.local.entity.Quotation
import com.example.hisabbook.ui.theme.HisabBookTheme
import com.example.hisabbook.util.CurrencyFormatter
import com.example.hisabbook.util.DateUtils
import com.example.hisabbook.util.NumberToWords
import com.example.hisabbook.util.PdfGenerator

data class SalesInvoiceItem(
    val name: String,
    val quantity: String,
    val rate: String,
    val amount: String,
)

@Composable
fun DocumentPreviewTemplate(
    modifier: Modifier = Modifier,
    docTypeTag: String = "BILL OF SUPPLY",
    badgeText: String = "ORIGINAL",
    accentColor: Color = Color(0xFFC59B27),
    bgColor: Color = Color(0xFFEEEEEE),
    docNumberLabel: String = "Invoice No.:",
    docNumber: String = "2",
    dateLabel: String = "Invoice Date:",
    invoiceDate: String = "02/09/2026 8:33 PM",
    dueDateLabel: String = "Due Date:",
    dueDate: String = "09/09/2026",
    businessName: String = "IMAGE ALUMINIUM AND GLASS",
    businessPhone: String = "8999127482",
    recipientTitle: String = "BILL TO",
    customerName: String = "Sahil@",
    customerPhone: String = "7058540991",
    itemColHeader: String = "ITEMS",
    rateColHeader: String = "RATE",
    amountColHeader: String = "AMOUNT",
    items: List<SalesInvoiceItem> = sampleInvoiceItems(),
    totalQty: String = "97.93",
    subtotalLabel: String = "SUBTOTAL",
    subtotalAmount: String = "₹ 16,632.55",
    totalAmountLabel: String = "Grand Total",
    totalAmount: String = "₹ 16,632.55",
    receivedAmount: String = "₹ 6,000",
    balanceAmount: String = "₹ 10,632.55",
    previousBalance: String = "₹ 0",
    currentBalance: String = "₹ 10,632.55",
    amountInWordsTitle: String = "Total Amount (in words)",
    amountInWords: String = "Sixteen Thousand Six Hundred Thirty Two Rupees and Fifty Five Paise",
    termsLine1: String = "1. Goods once sold will not be taken back or exchanged",
    termsLine2: String = "2. All disputes are subject to jurisdiction only",
    isQuotation: Boolean = false,
) {
    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = Color(0xFFF3F4F6)
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(2.dp),
                color = Color.White,
                shadowElevation = 1.dp
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp)
                ) {
                    SalesInvoiceHeaderSection(
                        docTypeTag = docTypeTag,
                        badgeText = badgeText,
                        accentColor = accentColor,
                        businessName = businessName,
                        businessPhone = businessPhone
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    SalesInvoiceMetadataBar(
                        docNumberLabel = docNumberLabel,
                        docNumber = docNumber,
                        dateLabel = dateLabel,
                        invoiceDate = invoiceDate,
                        dueDateLabel = dueDateLabel,
                        dueDate = dueDate,
                        bgColor = bgColor
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    SalesInvoiceBillToSection(
                        recipientTitle = recipientTitle,
                        customerName = customerName,
                        customerPhone = customerPhone
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    SalesInvoiceItemsTable(
                        items = items,
                        totalQty = totalQty,
                        subtotalLabel = subtotalLabel,
                        subtotalAmount = subtotalAmount,
                        accentColor = accentColor,
                        itemColHeader = itemColHeader,
                        rateColHeader = rateColHeader,
                        amountColHeader = amountColHeader
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    SalesInvoiceSummarySection(
                        totalAmountLabel = totalAmountLabel,
                        totalAmount = totalAmount,
                        receivedAmount = receivedAmount,
                        balanceAmount = balanceAmount,
                        previousBalance = previousBalance,
                        currentBalance = currentBalance,
                        amountInWordsTitle = amountInWordsTitle,
                        amountInWords = amountInWords,
                        termsLine1 = termsLine1,
                        termsLine2 = termsLine2,
                        isQuotation = isQuotation
                    )
                }
            }
        }
    }
}

@Composable
fun SalesInvoicePreviewScreen(
    modifier: Modifier = Modifier,
    businessName: String = "IMAGE ALUMINIUM AND GLASS",
    businessPhone: String = "8999127482",
    invoiceNo: String = "2",
    invoiceDate: String = "02/09/2026 8:33 PM",
    dueDate: String = "09/09/2026",
    customerName: String = "Sahil@",
    customerPhone: String = "7058540991",
    items: List<SalesInvoiceItem> = sampleInvoiceItems(),
    totalQty: String = "97.93",
    subtotalAmount: String = "₹ 16,632.55",
    totalAmount: String = "₹ 16,632.55",
    receivedAmount: String = "₹ 6,000",
    balanceAmount: String = "₹ 10,632.55",
    previousBalance: String = "₹ 0",
    currentBalance: String = "₹ 10,632.55",
    amountInWords: String = "Sixteen Thousand Six Hundred Thirty Two Rupees and Fifty Five Paise",
) {
    DocumentPreviewTemplate(
        modifier = modifier,
        docTypeTag = stringResource(id = R.string.bill_of_supply),
        badgeText = stringResource(id = R.string.original),
        accentColor = Color(0xFFC59B27),
        bgColor = Color(0xFFEEEEEE),
        docNumberLabel = stringResource(id = R.string.invoice_no_label),
        docNumber = invoiceNo,
        dateLabel = stringResource(id = R.string.invoice_date_label),
        invoiceDate = invoiceDate,
        dueDateLabel = stringResource(id = R.string.due_date_label),
        dueDate = dueDate,
        businessName = businessName,
        businessPhone = businessPhone,
        recipientTitle = stringResource(id = R.string.bill_to_label),
        customerName = customerName,
        customerPhone = customerPhone,
        itemColHeader = stringResource(id = R.string.items),
        rateColHeader = stringResource(id = R.string.rate_label),
        amountColHeader = stringResource(id = R.string.amount),
        items = items,
        totalQty = totalQty,
        subtotalLabel = stringResource(id = R.string.subtotal),
        subtotalAmount = subtotalAmount,
        totalAmountLabel = stringResource(id = R.string.grand_total),
        totalAmount = totalAmount,
        receivedAmount = receivedAmount,
        balanceAmount = balanceAmount,
        previousBalance = previousBalance,
        currentBalance = currentBalance,
        amountInWordsTitle = stringResource(id = R.string.total_amount_in_words),
        amountInWords = amountInWords,
        termsLine1 = stringResource(id = R.string.terms_line_1),
        termsLine2 = stringResource(id = R.string.terms_line_2),
        isQuotation = false
    )
}

@Composable
fun QuotationPreviewScreen(
    modifier: Modifier = Modifier,
    businessName: String = "IMAGE ALUMINIUM AND GLASS",
    businessPhone: String = "8999127482",
    quotationNo: String = "Q-1001",
    quotationDate: String = "02/09/2026 8:33 PM",
    validUntilDate: String = "02/10/2026",
    customerName: String = "Sahil@",
    customerPhone: String = "7058540991",
    items: List<SalesInvoiceItem> = sampleInvoiceItems(),
    totalQty: String = "97.93",
    subtotalAmount: String = "₹ 16,632.55",
    totalAmount: String = "₹ 16,632.55",
    amountInWords: String = "Sixteen Thousand Six Hundred Thirty Two Rupees and Fifty Five Paise",
) {
    DocumentPreviewTemplate(
        modifier = modifier,
        docTypeTag = stringResource(id = R.string.quotation_estimate),
        badgeText = stringResource(id = R.string.estimate_badge),
        accentColor = Color(0xFF1E40AF),
        bgColor = Color(0xFFF0F7FF),
        docNumberLabel = stringResource(id = R.string.quotation_no_label),
        docNumber = quotationNo,
        dateLabel = stringResource(id = R.string.quotation_date_label),
        invoiceDate = quotationDate,
        dueDateLabel = stringResource(id = R.string.valid_until_label),
        dueDate = validUntilDate,
        businessName = businessName,
        businessPhone = businessPhone,
        recipientTitle = stringResource(id = R.string.quotation_for_label),
        customerName = customerName,
        customerPhone = customerPhone,
        itemColHeader = "ITEM DESCRIPTION",
        rateColHeader = "UNIT RATE",
        amountColHeader = "EST. AMOUNT",
        items = items,
        totalQty = totalQty,
        subtotalLabel = "ESTIMATED TOTAL",
        subtotalAmount = subtotalAmount,
        totalAmountLabel = stringResource(id = R.string.estimated_net_total),
        totalAmount = totalAmount,
        amountInWordsTitle = stringResource(id = R.string.total_estimated_amount_in_words),
        amountInWords = amountInWords,
        termsLine1 = stringResource(id = R.string.quotation_terms_line_1),
        termsLine2 = stringResource(id = R.string.quotation_terms_line_2),
        isQuotation = true
    )
}

@Composable
fun InvoiceDocumentPreview(
    invoice: Invoice,
    customer: Customer?,
    businessProfile: BusinessProfile?,
    modifier: Modifier = Modifier,
    payments: List<Payment> = emptyList(),
    customLineItems: List<PdfGenerator.TableItem>? = null,
    previousBalanceVal: Double = 0.0,
) {
    val bName = businessProfile?.businessName?.ifBlank { "Business Name" } ?: "Business Name"
    val bPhone = businessProfile?.phone ?: ""
    val custName = customer?.name?.ifBlank { "Customer" } ?: "Customer"
    val custPhone = customer?.phone ?: ""

    val invDateStr = DateUtils.formatDate(invoice.createdAt)
    val dueDateStr = DateUtils.formatDate(invoice.createdAt + (15L * 24 * 60 * 60 * 1000))

    val displayItems = customLineItems?.map {
        SalesInvoiceItem(
            name = it.description,
            quantity = "${it.quantity} ${it.unit}",
            rate = CurrencyFormatter.formatAmountOnly(it.rate),
            amount = CurrencyFormatter.formatAmountOnly(it.amount)
        )
    } ?: listOf(
        SalesInvoiceItem(
            name = invoice.description.ifBlank { "Invoice Items" },
            quantity = "1 PCS",
            rate = CurrencyFormatter.formatAmountOnly(invoice.amount),
            amount = CurrencyFormatter.formatAmountOnly(invoice.amount)
        )
    )

    val totalPaid = payments.sumOf { it.amount }
    val bal = (invoice.total - totalPaid).coerceAtLeast(0.0)
    val currBal = bal + previousBalanceVal

    SalesInvoicePreviewScreen(
        modifier = modifier,
        businessName = bName,
        businessPhone = bPhone,
        invoiceNo = invoice.billNumber,
        invoiceDate = invDateStr,
        dueDate = dueDateStr,
        customerName = custName,
        customerPhone = custPhone,
        items = displayItems,
        totalQty = "${displayItems.size} PCS",
        subtotalAmount = CurrencyFormatter.formatCurrency(invoice.amount),
        totalAmount = CurrencyFormatter.formatCurrency(invoice.total),
        receivedAmount = CurrencyFormatter.formatCurrency(totalPaid),
        balanceAmount = CurrencyFormatter.formatCurrency(bal),
        previousBalance = CurrencyFormatter.formatCurrency(previousBalanceVal),
        currentBalance = CurrencyFormatter.formatCurrency(currBal),
        amountInWords = NumberToWords.convertToWordsCapitalized(invoice.total)
    )
}

@Composable
fun QuotationDocumentPreview(
    quotation: Quotation,
    customer: Customer?,
    businessProfile: BusinessProfile?,
    modifier: Modifier = Modifier,
    customLineItems: List<PdfGenerator.TableItem>? = null,
) {
    val bName = businessProfile?.businessName?.ifBlank { "Business Name" } ?: "Business Name"
    val bPhone = businessProfile?.phone ?: ""
    val custName = customer?.name?.ifBlank { "Customer" } ?: "Customer"
    val custPhone = customer?.phone ?: ""

    val qtnDateStr = DateUtils.formatDate(quotation.createdAt)
    val validUntilStr = DateUtils.formatDate(quotation.createdAt + (30L * 24 * 60 * 60 * 1000))

    val displayItems = customLineItems?.map {
        SalesInvoiceItem(
            name = it.description,
            quantity = "${it.quantity} ${it.unit}",
            rate = CurrencyFormatter.formatAmountOnly(it.rate),
            amount = CurrencyFormatter.formatAmountOnly(it.amount)
        )
    } ?: listOf(
        SalesInvoiceItem(
            name = quotation.description.ifBlank { "Quotation Items" },
            quantity = "${quotation.quantity} PCS",
            rate = CurrencyFormatter.formatAmountOnly(quotation.amount),
            amount = CurrencyFormatter.formatAmountOnly(quotation.quantity * quotation.amount)
        )
    )

    QuotationPreviewScreen(
        modifier = modifier,
        businessName = bName,
        businessPhone = bPhone,
        quotationNo = quotation.quotationNumber,
        quotationDate = qtnDateStr,
        validUntilDate = validUntilStr,
        customerName = custName,
        customerPhone = custPhone,
        items = displayItems,
        totalQty = "${displayItems.size} PCS",
        subtotalAmount = CurrencyFormatter.formatCurrency(quotation.total),
        totalAmount = CurrencyFormatter.formatCurrency(quotation.total),
        amountInWords = NumberToWords.convertToWordsCapitalized(quotation.total)
    )
}

@Composable
fun SalesInvoiceHeaderSection(
    businessName: String,
    businessPhone: String,
    modifier: Modifier = Modifier,
    docTypeTag: String = stringResource(id = R.string.bill_of_supply),
    badgeText: String = stringResource(id = R.string.original),
    accentColor: Color = Color(0xFFC59B27),
) {
    Column(modifier = modifier.fillMaxWidth()) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Text(
                text = docTypeTag,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF1E293B)
            )

            Box(
                modifier = Modifier
                    .border(0.8.dp, accentColor, RoundedCornerShape(2.dp))
                    .padding(horizontal = 4.dp, vertical = 1.dp)
            ) {
                Text(
                    text = badgeText,
                    fontSize = 9.sp,
                    color = accentColor,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = businessName,
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = accentColor
        )

        Spacer(modifier = Modifier.height(2.dp))

        Row {
            Text(
                text = "Mobile: ",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF1E293B)
            )
            Text(
                text = businessPhone,
                fontSize = 11.sp,
                color = Color(0xFF1E293B)
            )
        }
    }
}

@Composable
fun SalesInvoiceMetadataBar(
    docNumber: String,
    invoiceDate: String,
    dueDate: String,
    modifier: Modifier = Modifier,
    docNumberLabel: String = stringResource(id = R.string.invoice_no_label),
    dateLabel: String = stringResource(id = R.string.invoice_date_label),
    dueDateLabel: String = stringResource(id = R.string.due_date_label),
    bgColor: Color = Color(0xFFEEEEEE),
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .background(bgColor)
            .padding(vertical = 8.dp, horizontal = 6.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1.0f)
            ) {
                Text(
                    text = "$docNumberLabel ",
                    fontSize = 8.5.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1E293B)
                )
                Text(
                    text = docNumber,
                    fontSize = 8.5.sp,
                    color = Color(0xFF1E293B)
                )
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1.8f)
            ) {
                Text(
                    text = "$dateLabel ",
                    fontSize = 8.5.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1E293B)
                )
                Text(
                    text = invoiceDate,
                    fontSize = 8.5.sp,
                    color = Color(0xFF1E293B),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.End,
                modifier = Modifier.weight(1.2f)
            ) {
                Text(
                    text = "$dueDateLabel ",
                    fontSize = 8.5.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1E293B)
                )
                Text(
                    text = dueDate,
                    fontSize = 8.5.sp,
                    color = Color(0xFF1E293B)
                )
            }
        }
    }
}

@Composable
fun SalesInvoiceBillToSection(
    customerName: String,
    customerPhone: String,
    modifier: Modifier = Modifier,
    recipientTitle: String = stringResource(id = R.string.bill_to_label),
) {
    Column(modifier = modifier.fillMaxWidth()) {
        Text(
            text = recipientTitle,
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF64748B)
        )

        Text(
            text = customerName,
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF0F172A)
        )

        Row {
            Text(
                text = "Mobile: ",
                fontSize = 11.sp,
                fontWeight = FontWeight.Normal,
                color = Color(0xFF1E293B)
            )
            Text(
                text = customerPhone,
                fontSize = 11.sp,
                color = Color(0xFF1E293B)
            )
        }
    }
}

@Composable
fun SalesInvoiceItemsTable(
    items: List<SalesInvoiceItem>,
    totalQty: String,
    subtotalAmount: String,
    modifier: Modifier = Modifier,
    subtotalLabel: String = stringResource(id = R.string.subtotal),
    accentColor: Color = Color(0xFFC59B27),
    itemColHeader: String = stringResource(id = R.string.items),
    rateColHeader: String = stringResource(id = R.string.rate_label),
    amountColHeader: String = stringResource(id = R.string.amount),
) {
    Column(modifier = modifier.fillMaxWidth()) {
        HorizontalDivider(
            thickness = 1.5.dp,
            color = accentColor
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = itemColHeader,
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF1E293B),
                modifier = Modifier.weight(2.6f)
            )
            Text(
                text = stringResource(id = R.string.qty_label),
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF1E293B),
                textAlign = TextAlign.End,
                modifier = Modifier.weight(1.0f)
            )
            Text(
                text = rateColHeader,
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF1E293B),
                textAlign = TextAlign.End,
                modifier = Modifier.weight(1.0f)
            )
            Text(
                text = amountColHeader,
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF1E293B),
                textAlign = TextAlign.End,
                modifier = Modifier.weight(1.2f)
            )
        }

        HorizontalDivider(
            thickness = 0.8.dp,
            color = Color(0xFFE2E8F0)
        )

        items.forEach { item ->
            SalesInvoiceItemRow(item = item)
        }

        HorizontalDivider(
            thickness = 1.2.dp,
            color = accentColor
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = subtotalLabel,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF0F172A),
                modifier = Modifier.weight(2.6f)
            )
            Text(
                text = totalQty,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF0F172A),
                textAlign = TextAlign.End,
                modifier = Modifier.weight(1.0f)
            )
            Spacer(modifier = Modifier.weight(1.0f))
            Text(
                text = subtotalAmount,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF0F172A),
                textAlign = TextAlign.End,
                modifier = Modifier.weight(1.2f)
            )
        }

        HorizontalDivider(
            thickness = 1.2.dp,
            color = accentColor
        )
    }
}

@Composable
fun SalesInvoiceItemRow(
    item: SalesInvoiceItem,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = item.name,
            fontSize = 10.sp,
            fontWeight = FontWeight.SemiBold,
            color = Color(0xFF1E293B),
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.weight(2.6f)
        )

        Text(
            text = item.quantity,
            fontSize = 10.sp,
            color = Color(0xFF1E293B),
            textAlign = TextAlign.End,
            modifier = Modifier.weight(1.0f)
        )

        Text(
            text = item.rate,
            fontSize = 10.sp,
            color = Color(0xFF1E293B),
            textAlign = TextAlign.End,
            modifier = Modifier.weight(1.0f)
        )

        Text(
            text = item.amount,
            fontSize = 10.sp,
            color = Color(0xFF1E293B),
            textAlign = TextAlign.End,
            modifier = Modifier.weight(1.2f)
        )
    }
}

@Composable
fun SalesInvoiceSummarySection(
    totalAmount: String,
    receivedAmount: String,
    balanceAmount: String,
    previousBalance: String,
    currentBalance: String,
    amountInWords: String,
    modifier: Modifier = Modifier,
    totalAmountLabel: String = stringResource(id = R.string.grand_total),
    amountInWordsTitle: String = stringResource(id = R.string.total_amount_in_words),
    termsLine1: String = stringResource(id = R.string.terms_line_1),
    termsLine2: String = stringResource(id = R.string.terms_line_2),
    isQuotation: Boolean = false,
) {
    Column(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(
                modifier = Modifier.weight(1.1f)
            ) {
                Text(
                    text = stringResource(id = R.string.terms_and_conditions),
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF0F172A)
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = termsLine1,
                    fontSize = 9.sp,
                    color = Color(0xFF475569),
                    lineHeight = 12.sp
                )
                Text(
                    text = termsLine2,
                    fontSize = 9.sp,
                    color = Color(0xFF475569),
                    lineHeight = 12.sp
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(
                modifier = Modifier.weight(1.0f)
            ) {
                HorizontalDivider(thickness = 0.8.dp, color = Color(0xFFCBD5E1))

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 3.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = totalAmountLabel,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF0F172A)
                    )
                    Text(
                        text = totalAmount,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF0F172A)
                    )
                }

                HorizontalDivider(thickness = 0.8.dp, color = Color(0xFFCBD5E1))

                if (!isQuotation) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 2.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = stringResource(id = R.string.received_amount),
                            fontSize = 9.sp,
                            color = Color(0xFF334155)
                        )
                        Text(
                            text = receivedAmount,
                            fontSize = 9.sp,
                            color = Color(0xFF334155)
                        )
                    }

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 2.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = stringResource(id = R.string.balance),
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF0F172A)
                        )
                        Text(
                            text = balanceAmount,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF0F172A)
                        )
                    }

                    HorizontalDivider(thickness = 0.8.dp, color = Color(0xFFCBD5E1))

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 2.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = stringResource(id = R.string.previous_balance),
                            fontSize = 9.sp,
                            color = Color(0xFF334155)
                        )
                        Text(
                            text = previousBalance,
                            fontSize = 9.sp,
                            color = Color(0xFF334155)
                        )
                    }

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 2.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = stringResource(id = R.string.current_balance),
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF0F172A)
                        )
                        Text(
                            text = currentBalance,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF0F172A)
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.End
        ) {
            Text(
                text = amountInWordsTitle,
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF0F172A)
            )
            Text(
                text = amountInWords,
                fontSize = 9.sp,
                color = Color(0xFF334155),
                textAlign = TextAlign.End
            )
        }
    }
}

fun sampleInvoiceItems(): List<SalesInvoiceItem> = listOf(
    SalesInvoiceItem("BRUSH RUBBER", "1 PCS", "350", "350"),
    SalesInvoiceItem("CLEAR PVC RUBBER", "3.5 KGS", "135", "472.5"),
    SalesInvoiceItem("CUBEX STEEL BEARING", "25 PCS", "18", "450"),
    SalesInvoiceItem("HD- ALUMINIUM", "23.43 KGS", "535", "12,535.05"),
    SalesInvoiceItem("LOCK 44 GOLD", "7 PCS", "45", "315"),
    SalesInvoiceItem("PINK BERING", "30 PCS", "11", "330"),
    SalesInvoiceItem("SILCON-NEXO", "3 PCS", "120", "360"),
    SalesInvoiceItem("SS STEEL KIT", "3 PCS", "140", "420"),
    SalesInvoiceItem("UNI-BOND. SHEET", "2 SHEET", "700", "1,400")
)

@Preview(showBackground = true, name = "1. Gold Invoice Template")
@Composable
fun SalesInvoicePreviewScreenPreview() {
    HisabBookTheme {
        SalesInvoicePreviewScreen()
    }
}

@Preview(showBackground = true, name = "2. Royal Blue Quotation Template")
@Composable
fun QuotationPreviewScreenPreview() {
    HisabBookTheme {
        QuotationPreviewScreen()
    }
}
