package com.example.hisabbook.util

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import com.example.hisabbook.data.local.entity.BusinessProfile
import com.example.hisabbook.data.local.entity.Customer
import com.example.hisabbook.data.local.entity.Invoice
import com.example.hisabbook.data.local.entity.Payment
import com.example.hisabbook.data.local.entity.Quotation
import java.io.File
import java.io.FileOutputStream
import java.util.Locale

object PdfGenerator {

    private const val PAGE_WIDTH = 595 // A4 width in points
    private const val PAGE_HEIGHT = 842 // A4 height in points
    private const val MARGIN = 36f

    // Color Palette
    private const val COLOR_GOLD = "#C59B27"
    private const val COLOR_GRAY_BG = "#F0F0F0"
    private const val COLOR_DARK_TEXT = "#1A1A1A"
    private const val COLOR_MUTED_TEXT = "#555555"
    private const val COLOR_DIVIDER = "#E2E8F0"
    private const val COLOR_BORDER_GRAY = "#CBD5E1"

    data class TableItem(
        val description: String,
        val quantity: Double = 1.0,
        val unit: String = "PCS",
        val rate: Double = 0.0,
        val amount: Double = 0.0
    )

    fun generateInvoicePdf(
        context: Context,
        invoice: Invoice,
        customer: Customer?,
        payments: List<Payment>,
        businessProfile: BusinessProfile?,
        lineItems: List<TableItem>? = null,
        previousBalance: Double = 0.0
    ): File {
        val pdfDocument = PdfDocument()
        val pageInfo = PdfDocument.PageInfo.Builder(PAGE_WIDTH, PAGE_HEIGHT, 1).create()
        val page = pdfDocument.startPage(pageInfo)
        val canvas = page.canvas

        var currentY = MARGIN

        // 1. Top Tag & Business Header
        currentY = drawTopTagAndHeader(
            canvas = canvas,
            docTypeTag = "BILL OF SUPPLY",
            businessProfile = businessProfile,
            startY = currentY
        )

        // 2. Invoice Metadata Bar
        val dateStr = DateUtils.formatDate(invoice.createdAt)
        val dueDateStr = DateUtils.formatDate(invoice.createdAt + 15L * 24 * 60 * 60 * 1000)

        currentY = drawMetadataBar(
            canvas = canvas,
            docNumberLabel = "Invoice No.:",
            docNumber = invoice.billNumber,
            date = dateStr,
            dueDate = dueDateStr,
            startY = currentY
        )

        // 3. BILL TO Section
        currentY = drawBillToSection(
            canvas = canvas,
            customer = customer,
            startY = currentY
        )

        // Parse or resolve table items
        val parsedItems = parseItemsFromDescription(
            rawDescription = invoice.description,
            fallbackAmount = invoice.amount,
            customItems = lineItems
        )

        // 4. Table Structure
        currentY = drawItemsTable(
            canvas = canvas,
            items = parsedItems,
            startY = currentY
        )

        // 5. Subtotal Bar
        val subtotalVal = if (invoice.amount > 0) invoice.amount else parsedItems.sumOf { it.amount }
        currentY = drawSubtotalBar(
            canvas = canvas,
            items = parsedItems,
            subtotal = subtotalVal,
            startY = currentY
        )

        // 6. Footer Two-Column Layout & Amount in Words
        val totalPaid = payments.sumOf { it.amount }
        val grandTotal = invoice.total

        drawFooterTwoColumnLayout(
            canvas = canvas,
            totalAmount = grandTotal,
            receivedAmount = totalPaid,
            previousBalance = previousBalance,
            notes = "",
            startY = currentY
        )

        pdfDocument.finishPage(page)

        val outputDir = File(context.cacheDir, "documents").apply { if (!exists()) mkdirs() }
        val outputFile = File(outputDir, "Invoice_${invoice.billNumber.replace("/", "_")}.pdf")

        FileOutputStream(outputFile).use { out ->
            pdfDocument.writeTo(out)
        }
        pdfDocument.close()

        return outputFile
    }

    fun generateQuotationPdf(
        context: Context,
        quotation: Quotation,
        customer: Customer?,
        businessProfile: BusinessProfile?,
        lineItems: List<TableItem>? = null
    ): File {
        val pdfDocument = PdfDocument()
        val pageInfo = PdfDocument.PageInfo.Builder(PAGE_WIDTH, PAGE_HEIGHT, 1).create()
        val page = pdfDocument.startPage(pageInfo)
        val canvas = page.canvas

        var currentY = MARGIN

        // 1. Top Tag & Business Header
        currentY = drawTopTagAndHeader(
            canvas = canvas,
            docTypeTag = "QUOTATION",
            businessProfile = businessProfile,
            startY = currentY
        )

        // 2. Metadata Bar
        val dateStr = DateUtils.formatDate(quotation.createdAt)
        val dueDateStr = DateUtils.formatDate(quotation.createdAt + 30L * 24 * 60 * 60 * 1000)

        currentY = drawMetadataBar(
            canvas = canvas,
            docNumberLabel = "Quotation No.:",
            docNumber = quotation.quotationNumber,
            date = dateStr,
            dueDate = dueDateStr,
            startY = currentY
        )

        // 3. BILL TO Section
        currentY = drawBillToSection(
            canvas = canvas,
            customer = customer,
            startY = currentY
        )

        val itemAmount = quotation.quantity * quotation.amount
        val parsedItems = if (!lineItems.isNullOrEmpty()) {
            lineItems
        } else {
            listOf(
                TableItem(
                    description = quotation.description.ifEmpty { "QUOTATION ITEMS & MATERIALS" },
                    quantity = quotation.quantity,
                    unit = "PCS",
                    rate = quotation.amount,
                    amount = itemAmount
                )
            )
        }

        // 4. Table Structure
        currentY = drawItemsTable(
            canvas = canvas,
            items = parsedItems,
            startY = currentY
        )

        // 5. Subtotal Bar
        currentY = drawSubtotalBar(
            canvas = canvas,
            items = parsedItems,
            subtotal = itemAmount,
            startY = currentY
        )

        // 6. Footer Two-Column Layout
        drawFooterTwoColumnLayout(
            canvas = canvas,
            totalAmount = quotation.total,
            receivedAmount = 0.0,
            previousBalance = 0.0,
            notes = quotation.notes,
            startY = currentY
        )

        pdfDocument.finishPage(page)

        val outputDir = File(context.cacheDir, "documents").apply { if (!exists()) mkdirs() }
        val outputFile = File(outputDir, "Quotation_${quotation.quotationNumber.replace("/", "_")}.pdf")

        FileOutputStream(outputFile).use { out ->
            pdfDocument.writeTo(out)
        }
        pdfDocument.close()

        return outputFile
    }

    // --- Helper Drawing Functions ---

    private fun drawTopTagAndHeader(
        canvas: Canvas,
        docTypeTag: String,
        businessProfile: BusinessProfile?,
        startY: Float
    ): Float {
        var y = startY

        // 1. Top Left Tag: "BILL OF SUPPLY" with "ORIGINAL" outline box next to it
        val tagTextPaint = Paint().apply {
            color = Color.parseColor(COLOR_DARK_TEXT)
            textSize = 10.5f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            isAntiAlias = true
        }
        canvas.drawText(docTypeTag, MARGIN, y, tagTextPaint)

        val tagTextWidth = tagTextPaint.measureText(docTypeTag)
        val boxLeft = MARGIN + tagTextWidth + 10f
        val boxTop = y - 10f
        val boxRight = boxLeft + 52f
        val boxBottom = y + 3f

        val boxOutlinePaint = Paint().apply {
            color = Color.parseColor(COLOR_DARK_TEXT)
            style = Paint.Style.STROKE
            strokeWidth = 1f
            isAntiAlias = true
        }
        val origBoxRect = RectF(boxLeft, boxTop, boxRight, boxBottom)
        canvas.drawRect(origBoxRect, boxOutlinePaint)

        val origTextPaint = Paint().apply {
            color = Color.parseColor(COLOR_DARK_TEXT)
            textSize = 7.5f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            isAntiAlias = true
        }
        val origTextWidth = origTextPaint.measureText("ORIGINAL")
        canvas.drawText("ORIGINAL", boxLeft + (52f - origTextWidth) / 2f, y - 1f, origTextPaint)

        y += 24f

        // 2. Business Title: Bold Golden Yellow (#C59B27)
        val titlePaint = Paint().apply {
            color = Color.parseColor(COLOR_GOLD)
            textSize = 18f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            isAntiAlias = true
        }
        val businessName = businessProfile?.businessName?.ifBlank { "Business Name" } ?: "Business Name"
        canvas.drawText(businessName, MARGIN, y, titlePaint)

        y += 15f

        // 3. Subtitle / Tagline & Mobile
        val subtitlePaint = Paint().apply {
            color = Color.parseColor(COLOR_MUTED_TEXT)
            textSize = 9.5f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
            isAntiAlias = true
        }
        val subtitleStr = businessProfile?.subtitle?.ifBlank { "Business Tagline" } ?: "Business Tagline"
        val phoneStr = businessProfile?.phone?.ifBlank { "" } ?: ""
        val subtitleLine = if (phoneStr.isNotBlank()) "$subtitleStr  •  Mobile: $phoneStr" else subtitleStr
        canvas.drawText(subtitleLine, MARGIN, y, subtitlePaint)

        y += 12f

        // 4. Solid Gold Line Separator (#C59B27)
        val goldLinePaint = Paint().apply {
            color = Color.parseColor(COLOR_GOLD)
            strokeWidth = 1.5f
            isAntiAlias = true
        }
        canvas.drawLine(MARGIN, y, PAGE_WIDTH - MARGIN, y, goldLinePaint)

        return y + 14f
    }

    private fun drawMetadataBar(
        canvas: Canvas,
        docNumberLabel: String,
        docNumber: String,
        date: String,
        dueDate: String,
        startY: Float
    ): Float {
        val barHeight = 28f
        val barRect = RectF(MARGIN, startY, PAGE_WIDTH - MARGIN, startY + barHeight)

        val bgPaint = Paint().apply {
            color = Color.parseColor(COLOR_GRAY_BG)
            style = Paint.Style.FILL
            isAntiAlias = true
        }
        canvas.drawRect(barRect, bgPaint)

        val labelPaint = Paint().apply {
            color = Color.parseColor(COLOR_DARK_TEXT)
            textSize = 9f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            isAntiAlias = true
        }
        val valuePaint = Paint().apply {
            color = Color.parseColor(COLOR_DARK_TEXT)
            textSize = 9f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
            isAntiAlias = true
        }

        val textY = startY + 18f
        val col1X = MARGIN + 10f
        val col2X = MARGIN + 180f
        val col3X = MARGIN + 360f

        // Column 1
        canvas.drawText(docNumberLabel, col1X, textY, labelPaint)
        val lbl1Width = labelPaint.measureText(docNumberLabel)
        canvas.drawText(" $docNumber", col1X + lbl1Width, textY, valuePaint)

        // Column 2
        canvas.drawText("Invoice Date:", col2X, textY, labelPaint)
        val lbl2Width = labelPaint.measureText("Invoice Date:")
        canvas.drawText(" $date", col2X + lbl2Width, textY, valuePaint)

        // Column 3
        canvas.drawText("Due Date:", col3X, textY, labelPaint)
        val lbl3Width = labelPaint.measureText("Due Date:")
        canvas.drawText(" $dueDate", col3X + lbl3Width, textY, valuePaint)

        return startY + barHeight + 14f
    }

    private fun drawBillToSection(
        canvas: Canvas,
        customer: Customer?,
        startY: Float
    ): Float {
        var y = startY

        val billToHeaderPaint = Paint().apply {
            color = Color.parseColor(COLOR_DARK_TEXT)
            textSize = 10f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            isAntiAlias = true
        }
        canvas.drawText("BILL TO", MARGIN, y, billToHeaderPaint)

        y += 14f

        val custNamePaint = Paint().apply {
            color = Color.parseColor(COLOR_DARK_TEXT)
            textSize = 10.5f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            isAntiAlias = true
        }
        val custName = customer?.name?.ifEmpty { "Customer" } ?: "Customer"
        canvas.drawText(custName, MARGIN, y, custNamePaint)

        y += 13f

        val custDetailPaint = Paint().apply {
            color = Color.parseColor(COLOR_MUTED_TEXT)
            textSize = 9f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
            isAntiAlias = true
        }
        val custPhone = customer?.phone ?: ""
        if (custPhone.isNotEmpty()) {
            canvas.drawText("Mobile: $custPhone", MARGIN, y, custDetailPaint)
            y += 12f
        }

        // Solid gold line separator below BILL TO
        val goldLinePaint = Paint().apply {
            color = Color.parseColor(COLOR_GOLD)
            strokeWidth = 1.5f
            isAntiAlias = true
        }
        canvas.drawLine(MARGIN, y, PAGE_WIDTH - MARGIN, y, goldLinePaint)

        return y + 14f
    }

    private fun drawItemsTable(
        canvas: Canvas,
        items: List<TableItem>,
        startY: Float
    ): Float {
        var y = startY

        val colItemsX = MARGIN + 8f
        val colQtyX = MARGIN + 310f
        val colRateX = MARGIN + 410f
        val colAmtX = PAGE_WIDTH - MARGIN - 8f

        val goldLinePaint = Paint().apply {
            color = Color.parseColor(COLOR_GOLD)
            strokeWidth = 1.5f
            isAntiAlias = true
        }

        val headerHeight = 22f

        // Top Gold Line Border around Header
        canvas.drawLine(MARGIN, y, PAGE_WIDTH - MARGIN, y, goldLinePaint)

        val tableHeaderPaint = Paint().apply {
            color = Color.parseColor(COLOR_DARK_TEXT)
            textSize = 9.5f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            isAntiAlias = true
        }

        val headerTextY = y + 15f
        canvas.drawText("ITEMS", colItemsX, headerTextY, tableHeaderPaint)

        val qtyHeaderWidth = tableHeaderPaint.measureText("QTY.")
        canvas.drawText("QTY.", colQtyX - qtyHeaderWidth, headerTextY, tableHeaderPaint)

        val rateHeaderWidth = tableHeaderPaint.measureText("RATE")
        canvas.drawText("RATE", colRateX - rateHeaderWidth, headerTextY, tableHeaderPaint)

        val amtHeaderWidth = tableHeaderPaint.measureText("AMOUNT")
        canvas.drawText("AMOUNT", colAmtX - amtHeaderWidth, headerTextY, tableHeaderPaint)

        y += headerHeight
        // Bottom Gold Line Border around Header
        canvas.drawLine(MARGIN, y, PAGE_WIDTH - MARGIN, y, goldLinePaint)

        val rowCellPaint = Paint().apply {
            color = Color.parseColor(COLOR_DARK_TEXT)
            textSize = 9f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
            isAntiAlias = true
        }
        val dividerPaint = Paint().apply {
            color = Color.parseColor(COLOR_DIVIDER)
            strokeWidth = 0.8f
            isAntiAlias = true
        }

        val rowHeight = 22f

        items.forEach { item ->
            val rowTextY = y + 15f

            // 1. Item Name in UPPERCASE
            val itemDesc = item.description.uppercase(Locale.ROOT)
            canvas.drawText(itemDesc, colItemsX, rowTextY, rowCellPaint)

            // 2. QTY with Unit (e.g. "1 PCS", "3.5 KGS", "23.43 KGS")
            val qtyFormatted = formatQuantity(item.quantity)
            val unitFormatted = item.unit.ifBlank { "PCS" }.uppercase(Locale.ROOT)
            val qtyStr = "$qtyFormatted $unitFormatted"
            val qtyWidth = rowCellPaint.measureText(qtyStr)
            canvas.drawText(qtyStr, colQtyX - qtyWidth, rowTextY, rowCellPaint)

            // 3. RATE formatted without currency symbol
            val rateStr = CurrencyFormatter.formatAmountOnly(item.rate)
            val rateWidth = rowCellPaint.measureText(rateStr)
            canvas.drawText(rateStr, colRateX - rateWidth, rowTextY, rowCellPaint)

            // 4. AMOUNT with comma formatting
            val amtStr = CurrencyFormatter.formatAmountOnly(item.amount)
            val amtWidth = rowCellPaint.measureText(amtStr)
            canvas.drawText(amtStr, colAmtX - amtWidth, rowTextY, rowCellPaint)

            y += rowHeight
            // Subtle row divider between items
            canvas.drawLine(MARGIN, y, PAGE_WIDTH - MARGIN, y, dividerPaint)
        }

        return y
    }

    private fun drawSubtotalBar(
        canvas: Canvas,
        items: List<TableItem>,
        subtotal: Double,
        startY: Float
    ): Float {
        var y = startY

        val colItemsX = MARGIN + 8f
        val colQtyX = MARGIN + 310f
        val colAmtX = PAGE_WIDTH - MARGIN - 8f

        val goldLinePaint = Paint().apply {
            color = Color.parseColor(COLOR_GOLD)
            strokeWidth = 1.5f
            isAntiAlias = true
        }

        val subtotalRowHeight = 22f

        // Top Gold Border around Subtotal
        canvas.drawLine(MARGIN, y, PAGE_WIDTH - MARGIN, y, goldLinePaint)

        val subtotalLabelPaint = Paint().apply {
            color = Color.parseColor(COLOR_DARK_TEXT)
            textSize = 9.5f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            isAntiAlias = true
        }

        val subtotalTextY = y + 15f
        canvas.drawText("SUBTOTAL", colItemsX, subtotalTextY, subtotalLabelPaint)

        // Total sum of item quantities
        val totalQtySum = items.sumOf { it.quantity }
        val primaryUnit = items.firstOrNull()?.unit?.ifBlank { "PCS" }?.uppercase(Locale.ROOT) ?: "PCS"
        val totalQtyStr = "${formatQuantity(totalQtySum)} $primaryUnit"
        val totalQtyWidth = subtotalLabelPaint.measureText(totalQtyStr)
        canvas.drawText(totalQtyStr, colQtyX - totalQtyWidth, subtotalTextY, subtotalLabelPaint)

        // "₹ [subtotal]"
        val subtotalStr = "₹ " + CurrencyFormatter.formatAmountOnly(subtotal)
        val subtotalValWidth = subtotalLabelPaint.measureText(subtotalStr)
        canvas.drawText(subtotalStr, colAmtX - subtotalValWidth, subtotalTextY, subtotalLabelPaint)

        y += subtotalRowHeight
        // Bottom Gold Border around Subtotal
        canvas.drawLine(MARGIN, y, PAGE_WIDTH - MARGIN, y, goldLinePaint)

        return y + 18f
    }

    private fun drawFooterTwoColumnLayout(
        canvas: Canvas,
        totalAmount: Double,
        receivedAmount: Double,
        previousBalance: Double,
        notes: String,
        startY: Float
    ) {
        val footerStartY = startY
        val leftX = MARGIN
        val rightTableLeftX = 330f
        val rightTableValX = PAGE_WIDTH - MARGIN - 8f

        // --- Left side: TERMS AND CONDITIONS ---
        var leftY = footerStartY
        val termsHeaderPaint = Paint().apply {
            color = Color.parseColor(COLOR_DARK_TEXT)
            textSize = 9.5f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            isAntiAlias = true
        }
        canvas.drawText("TERMS AND CONDITIONS", leftX, leftY, termsHeaderPaint)

        leftY += 14f
        val termsTextPaint = Paint().apply {
            color = Color.parseColor(COLOR_MUTED_TEXT)
            textSize = 8.5f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
            isAntiAlias = true
        }
        canvas.drawText("1. Goods once sold will not be taken back or exchanged", leftX, leftY, termsTextPaint)
        leftY += 12f
        canvas.drawText("2. All disputes are subject to jurisdiction only", leftX, leftY, termsTextPaint)

        if (notes.isNotEmpty()) {
            leftY += 14f
            canvas.drawText("Notes: $notes", leftX, leftY, termsTextPaint)
        }

        // --- Right side: Financial Summary Table ---
        var rightY = footerStartY

        val balance = (totalAmount - receivedAmount).coerceAtLeast(0.0)
        val currBal = balance + previousBalance

        fun drawSummaryRow(label: String, amount: Double, isBold: Boolean = false) {
            val lblPaint = Paint().apply {
                color = Color.parseColor(COLOR_DARK_TEXT)
                textSize = 9f
                typeface = if (isBold) Typeface.create(Typeface.DEFAULT, Typeface.BOLD) else Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
                isAntiAlias = true
            }
            val valPaint = Paint().apply {
                color = Color.parseColor(COLOR_DARK_TEXT)
                textSize = 9f
                typeface = if (isBold) Typeface.create(Typeface.DEFAULT, Typeface.BOLD) else Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
                isAntiAlias = true
            }

            canvas.drawText(label, rightTableLeftX, rightY, lblPaint)
            val valStr = "₹ " + CurrencyFormatter.formatAmountOnly(amount)
            val valWidth = valPaint.measureText(valStr)
            canvas.drawText(valStr, rightTableValX - valWidth, rightY, valPaint)
            rightY += 14f
        }

        drawSummaryRow("Total Amount:", totalAmount, isBold = true)
        drawSummaryRow("Received Amount:", receivedAmount)
        drawSummaryRow("Balance:", balance, isBold = true)

        // Line separator
        val thinLinePaint = Paint().apply {
            color = Color.parseColor(COLOR_BORDER_GRAY)
            strokeWidth = 0.8f
            isAntiAlias = true
        }
        canvas.drawLine(rightTableLeftX, rightY - 2f, PAGE_WIDTH - MARGIN, rightY - 2f, thinLinePaint)
        rightY += 6f

        drawSummaryRow("Previous Balance:", previousBalance)
        drawSummaryRow("Current Balance:", currBal, isBold = true)

        rightY += 12f

        // --- Bottom Right: Total Amount (in words) ---
        val wordsHeaderPaint = Paint().apply {
            color = Color.parseColor(COLOR_DARK_TEXT)
            textSize = 9f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            isAntiAlias = true
        }
        canvas.drawText("Total Amount (in words)", rightTableLeftX, rightY, wordsHeaderPaint)

        rightY += 13f

        val amountWordsStr = NumberToWords.convertToWordsCapitalized(totalAmount)
        val wordsValuePaint = Paint().apply {
            color = Color.parseColor(COLOR_DARK_TEXT)
            textSize = 8.5f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            isAntiAlias = true
        }

        drawTextWrapped(canvas, amountWordsStr, rightTableLeftX, rightY, (PAGE_WIDTH - MARGIN) - rightTableLeftX, wordsValuePaint)
    }

    private fun parseItemsFromDescription(
        rawDescription: String,
        fallbackAmount: Double,
        customItems: List<TableItem>?
    ): List<TableItem> {
        if (!customItems.isNullOrEmpty()) return customItems

        val raw = rawDescription.trim()
        if (raw.isEmpty()) {
            return listOf(
                TableItem(
                    description = "FABRICATION / GLASS WORKS",
                    quantity = 1.0,
                    unit = "PCS",
                    rate = fallbackAmount,
                    amount = fallbackAmount
                )
            )
        }

        val result = mutableListOf<TableItem>()
        val tokens = raw.split(Regex("[\n,]")).map { it.trim() }.filter { it.isNotEmpty() }
        val itemRegex = Regex("""^(.*?)\s*\(Qty:\s*([0-9.]+)\s*([A-Za-z]*)\)$""", RegexOption.IGNORE_CASE)

        for (token in tokens) {
            val match = itemRegex.find(token)
            if (match != null) {
                val desc = match.groupValues[1].trim()
                val qty = match.groupValues[2].toDoubleOrNull() ?: 1.0
                val unit = match.groupValues[3].ifBlank { "PCS" }
                result.add(TableItem(description = desc, quantity = qty, unit = unit, rate = 0.0, amount = 0.0))
            }
        }

        if (result.isNotEmpty()) {
            if (result.size == 1) {
                val item = result[0]
                val rate = if (item.quantity > 0) fallbackAmount / item.quantity else fallbackAmount
                return listOf(item.copy(rate = rate, amount = fallbackAmount))
            } else {
                val totalQty = result.sumOf { it.quantity }
                val unitRate = if (totalQty > 0) fallbackAmount / totalQty else fallbackAmount
                return result.map { it.copy(rate = unitRate, amount = it.quantity * unitRate) }
            }
        }

        return listOf(
            TableItem(
                description = raw,
                quantity = 1.0,
                unit = "PCS",
                rate = fallbackAmount,
                amount = fallbackAmount
            )
        )
    }

    private fun formatQuantity(qty: Double): String {
        return if (qty == qty.toLong().toDouble()) {
            qty.toLong().toString()
        } else {
            String.format(Locale.US, "%.2f", qty).trimEnd('0').trimEnd('.')
        }
    }

    private fun drawTextWrapped(
        canvas: Canvas,
        text: String,
        x: Float,
        startY: Float,
        maxWidth: Float,
        paint: Paint
    ): Float {
        val words = text.split(" ")
        var currentLine = ""
        var y = startY
        val lineHeight = paint.textSize + 3f

        for (word in words) {
            val testLine = if (currentLine.isEmpty()) word else "$currentLine $word"
            if (paint.measureText(testLine) <= maxWidth) {
                currentLine = testLine
            } else {
                canvas.drawText(currentLine, x, y, paint)
                y += lineHeight
                currentLine = word
            }
        }
        if (currentLine.isNotEmpty()) {
            canvas.drawText(currentLine, x, y, paint)
            y += lineHeight
        }
        return y
    }

    fun sharePdf(context: Context, file: File, title: String) {
        PdfShareHelper.sharePdf(context, file, title)
    }
}
