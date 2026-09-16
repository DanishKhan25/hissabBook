package com.example.hisabbook.util

import android.content.Context
import android.content.Intent
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import androidx.core.content.FileProvider
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

    fun generateQuotationPdf(
        context: Context,
        quotation: Quotation,
        customer: Customer?,
        businessProfile: BusinessProfile?
    ): File {
        val pdfDocument = PdfDocument()
        val pageInfo = PdfDocument.PageInfo.Builder(PAGE_WIDTH, PAGE_HEIGHT, 1).create()
        val page = pdfDocument.startPage(pageInfo)
        val canvas = page.canvas

        var currentY = MARGIN

        // Header Business
        currentY = drawHeader(canvas, businessProfile, currentY)

        // Document Title Bar
        currentY = drawDocumentTitleBar(
            canvas,
            title = "QUOTATION",
            docNumberLabel = "Quotation No:",
            docNumber = quotation.quotationNumber,
            date = DateUtils.formatDate(quotation.createdAt),
            startY = currentY
        )

        // Customer Info
        currentY = drawCustomerInfo(canvas, customer, currentY)

        // Items Table
        currentY = drawItemsTable(
            canvas,
            items = listOf(
                TableItem(
                    description = quotation.description,
                    quantity = quotation.quantity,
                    rate = quotation.amount,
                    amount = quotation.quantity * quotation.amount
                )
            ),
            startY = currentY
        )

        // Total Section
        currentY = drawTotalSection(
            canvas,
            subtotal = quotation.quantity * quotation.amount,
            discount = quotation.discount,
            grandTotal = quotation.total,
            paidAmount = null,
            balanceDue = null,
            startY = currentY
        )

        // Notes / Terms & Conditions
        drawFooterAndTerms(
            canvas,
            notes = quotation.notes,
            businessName = businessProfile?.businessName ?: "IMAGE ALUMINIUM AND GLASS",
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

    fun generateInvoicePdf(
        context: Context,
        invoice: Invoice,
        customer: Customer?,
        payments: List<Payment>,
        businessProfile: BusinessProfile?
    ): File {
        val pdfDocument = PdfDocument()
        val pageInfo = PdfDocument.PageInfo.Builder(PAGE_WIDTH, PAGE_HEIGHT, 1).create()
        val page = pdfDocument.startPage(pageInfo)
        val canvas = page.canvas

        var currentY = MARGIN

        // Header Business
        currentY = drawHeader(canvas, businessProfile, currentY)

        // Document Title Bar
        currentY = drawDocumentTitleBar(
            canvas,
            title = "BILL OF SUPPLY / INVOICE",
            docNumberLabel = "Invoice No:",
            docNumber = invoice.billNumber,
            date = DateUtils.formatDate(invoice.createdAt),
            startY = currentY
        )

        // Customer Info
        currentY = drawCustomerInfo(canvas, customer, currentY)

        // Items Table
        currentY = drawItemsTable(
            canvas,
            items = listOf(
                TableItem(
                    description = invoice.description,
                    quantity = 1.0,
                    rate = invoice.amount,
                    amount = invoice.amount
                )
            ),
            startY = currentY
        )

        val totalPaid = payments.sumOf { it.amount }
        val balanceDue = (invoice.total - totalPaid).coerceAtLeast(0.0)

        // Total Section
        currentY = drawTotalSection(
            canvas,
            subtotal = invoice.amount,
            discount = invoice.discount,
            grandTotal = invoice.total,
            paidAmount = totalPaid,
            balanceDue = balanceDue,
            startY = currentY
        )

        // Footer & Terms
        currentY = drawFooterAndTerms(
            canvas,
            notes = "",
            businessName = businessProfile?.businessName ?: "IMAGE ALUMINIUM AND GLASS",
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

    private fun drawHeader(canvas: Canvas, profile: BusinessProfile?, startY: Float): Float {
        var y = startY + 10f

        val titlePaint = Paint().apply {
            color = Color.parseColor("#1E3A8A")
            textSize = 18f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            isAntiAlias = true
        }

        val subtitlePaint = Paint().apply {
            color = Color.parseColor("#334155")
            textSize = 10f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            isAntiAlias = true
        }

        val contactPaint = Paint().apply {
            color = Color.parseColor("#64748B")
            textSize = 9f
            isAntiAlias = true
        }

        val businessName = profile?.businessName?.ifEmpty { "IMAGE ALUMINIUM AND GLASS" } ?: "IMAGE ALUMINIUM AND GLASS"
        val subtitle = profile?.subtitle?.ifEmpty { "Fabrication, Sliding Windows & Toughened Glass Works" } ?: "Fabrication, Sliding Windows & Toughened Glass Works"
        val phone = profile?.phone?.ifEmpty { "+91 98765 43210" } ?: "+91 98765 43210"
        val address = profile?.address ?: ""
        val gst = profile?.gstNumber?.takeIf { it.isNotEmpty() }?.let { " | GSTIN: $it" } ?: ""

        canvas.drawText(businessName, MARGIN, y, titlePaint)
        y += 16f

        canvas.drawText(subtitle, MARGIN, y, subtitlePaint)
        y += 14f

        val contactLine = "Mobile: $phone$gst" + (if (address.isNotEmpty()) " | $address" else "")
        canvas.drawText(contactLine, MARGIN, y, contactPaint)
        y += 20f

        // Top Divider Line
        val linePaint = Paint().apply {
            color = Color.parseColor("#CBD5E1")
            strokeWidth = 1f
        }
        canvas.drawLine(MARGIN, y, PAGE_WIDTH - MARGIN, y, linePaint)
        y += 15f

        return y
    }

    private fun drawDocumentTitleBar(
        canvas: Canvas,
        title: String,
        docNumberLabel: String,
        docNumber: String,
        date: String,
        startY: Float
    ): Float {
        var y = startY

        val barRect = RectF(MARGIN, y, PAGE_WIDTH - MARGIN, y + 28f)
        val bgPaint = Paint().apply {
            color = Color.parseColor("#F1F5F9")
            style = Paint.Style.FILL
        }
        val borderPaint = Paint().apply {
            color = Color.parseColor("#CBD5E1")
            style = Paint.Style.STROKE
            strokeWidth = 1f
        }

        canvas.drawRect(barRect, bgPaint)
        canvas.drawRect(barRect, borderPaint)

        val titlePaint = Paint().apply {
            color = Color.parseColor("#0F172A")
            textSize = 12f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            isAntiAlias = true
        }

        val metaPaint = Paint().apply {
            color = Color.parseColor("#334155")
            textSize = 10f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
            isAntiAlias = true
        }

        canvas.drawText(title, MARGIN + 10f, y + 18f, titlePaint)

        val rightText = "$docNumberLabel $docNumber   |   Date: $date"
        val textWidth = metaPaint.measureText(rightText)
        canvas.drawText(rightText, PAGE_WIDTH - MARGIN - 10f - textWidth, y + 18f, metaPaint)

        y += 40f
        return y
    }

    private fun drawCustomerInfo(canvas: Canvas, customer: Customer?, startY: Float): Float {
        var y = startY

        val labelPaint = Paint().apply {
            color = Color.parseColor("#475569")
            textSize = 9f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            isAntiAlias = true
        }

        val namePaint = Paint().apply {
            color = Color.parseColor("#0F172A")
            textSize = 11f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            isAntiAlias = true
        }

        val detailPaint = Paint().apply {
            color = Color.parseColor("#334155")
            textSize = 10f
            isAntiAlias = true
        }

        canvas.drawText("BILL TO / CUSTOMER DETAILS", MARGIN, y, labelPaint)
        y += 14f

        canvas.drawText(customer?.name ?: "Cash / Walk-in Customer", MARGIN, y, namePaint)
        y += 14f

        if (!customer?.phone.isNullOrEmpty()) {
            canvas.drawText("Phone: ${customer.phone}", MARGIN, y, detailPaint)
            y += 12f
        }

        if (!customer?.address.isNullOrEmpty()) {
            canvas.drawText("Address: ${customer.address}", MARGIN, y, detailPaint)
            y += 12f
        }

        y += 10f
        return y
    }

    private fun drawItemsTable(canvas: Canvas, items: List<TableItem>, startY: Float): Float {
        var y = startY

        val tableHeaderBg = RectF(MARGIN, y, PAGE_WIDTH - MARGIN, y + 22f)
        val bgPaint = Paint().apply {
            color = Color.parseColor("#1E293B")
            style = Paint.Style.FILL
        }
        canvas.drawRect(tableHeaderBg, bgPaint)

        val headerTextPaint = Paint().apply {
            color = Color.WHITE
            textSize = 9f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            isAntiAlias = true
        }

        val colDescX = MARGIN + 10f
        val colQtyX = 350f
        val colRateX = 420f
        val colAmtX = 490f

        canvas.drawText("ITEMS / DESCRIPTION", colDescX, y + 15f, headerTextPaint)
        canvas.drawText("QTY", colQtyX, y + 15f, headerTextPaint)
        canvas.drawText("RATE (₹)", colRateX, y + 15f, headerTextPaint)
        canvas.drawText("AMOUNT (₹)", colAmtX, y + 15f, headerTextPaint)

        y += 22f

        val cellTextPaint = Paint().apply {
            color = Color.parseColor("#1E293B")
            textSize = 10f
            isAntiAlias = true
        }

        val linePaint = Paint().apply {
            color = Color.parseColor("#E2E8F0")
            strokeWidth = 1f
        }

        items.forEach { item ->
            canvas.drawText(item.description.ifEmpty { "General Work / Materials" }, colDescX, y + 16f, cellTextPaint)
            canvas.drawText(String.format(Locale.getDefault(), "%.1f", item.quantity), colQtyX, y + 16f, cellTextPaint)
            canvas.drawText(CurrencyFormatter.formatAmountOnly(item.rate), colRateX, y + 16f, cellTextPaint)
            canvas.drawText(CurrencyFormatter.formatAmountOnly(item.amount), colAmtX, y + 16f, cellTextPaint)

            y += 24f
            canvas.drawLine(MARGIN, y, PAGE_WIDTH - MARGIN, y, linePaint)
        }

        y += 15f
        return y
    }

    private fun drawTotalSection(
        canvas: Canvas,
        subtotal: Double,
        discount: Double,
        grandTotal: Double,
        paidAmount: Double?,
        balanceDue: Double?,
        startY: Float
    ): Float {
        var y = startY

        val labelPaint = Paint().apply {
            color = Color.parseColor("#475569")
            textSize = 10f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
            isAntiAlias = true
        }

        val boldLabelPaint = Paint().apply {
            color = Color.parseColor("#0F172A")
            textSize = 11f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            isAntiAlias = true
        }

        val rightX = PAGE_WIDTH - MARGIN - 10f
        val leftX = 350f

        fun drawRightAlignedRow(label: String, value: String, isBold: Boolean = false) {
            val paint = if (isBold) boldLabelPaint else labelPaint
            canvas.drawText(label, leftX, y, paint)
            val width = paint.measureText(value)
            canvas.drawText(value, rightX - width, y, paint)
            y += 16f
        }

        drawRightAlignedRow("Subtotal:", "₹ ${CurrencyFormatter.formatAmountOnly(subtotal)}")
        if (discount > 0) {
            drawRightAlignedRow("Discount:", "- ₹ ${CurrencyFormatter.formatAmountOnly(discount)}")
        }

        // Total Box
        val totalBox = RectF(leftX - 5f, y - 2f, PAGE_WIDTH - MARGIN, y + 20f)
        val totalBg = Paint().apply {
            color = Color.parseColor("#EFF6FF")
            style = Paint.Style.FILL
        }
        val totalBorder = Paint().apply {
            color = Color.parseColor("#BFDBFE")
            style = Paint.Style.STROKE
            strokeWidth = 1f
        }
        canvas.drawRect(totalBox, totalBg)
        canvas.drawRect(totalBox, totalBorder)

        canvas.drawText("Total Amount:", leftX + 5f, y + 13f, boldLabelPaint)
        val totalVal = "₹ ${CurrencyFormatter.formatAmountOnly(grandTotal)}"
        val totalValWidth = boldLabelPaint.measureText(totalVal)
        canvas.drawText(totalVal, rightX - totalValWidth, y + 13f, boldLabelPaint)
        y += 28f

        if (paidAmount != null && balanceDue != null) {
            drawRightAlignedRow("Received Amount:", "₹ ${CurrencyFormatter.formatAmountOnly(paidAmount)}")
            val dueColor = if (balanceDue > 0) Color.parseColor("#DC2626") else Color.parseColor("#16A34A")
            val duePaint = Paint().apply {
                color = dueColor
                textSize = 11f
                typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                isAntiAlias = true
            }
            canvas.drawText("Outstanding Balance:", leftX, y, duePaint)
            val dueVal = "₹ ${CurrencyFormatter.formatAmountOnly(balanceDue)}"
            val dueValWidth = duePaint.measureText(dueVal)
            canvas.drawText(dueVal, rightX - dueValWidth, y, duePaint)
            y += 20f
        }

        // Amount in Words
        val wordsBox = RectF(MARGIN, y, PAGE_WIDTH - MARGIN, y + 24f)
        val wordsBg = Paint().apply {
            color = Color.parseColor("#F8FAFC")
            style = Paint.Style.FILL
        }
        val wordsBorder = Paint().apply {
            color = Color.parseColor("#E2E8F0")
            style = Paint.Style.STROKE
            strokeWidth = 1f
        }
        canvas.drawRect(wordsBox, wordsBg)
        canvas.drawRect(wordsBox, wordsBorder)

        val wordsPaint = Paint().apply {
            color = Color.parseColor("#334155")
            textSize = 9.5f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD_ITALIC)
            isAntiAlias = true
        }

        val amountWords = NumberToWords.convert(grandTotal)
        canvas.drawText("Amount in Words: $amountWords", MARGIN + 10f, y + 16f, wordsPaint)

        y += 35f
        return y
    }

    private fun drawFooterAndTerms(
        canvas: Canvas,
        notes: String,
        businessName: String,
        startY: Float
    ): Float {
        var y = startY

        val termHeaderPaint = Paint().apply {
            color = Color.parseColor("#475569")
            textSize = 9f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            isAntiAlias = true
        }

        val termTextPaint = Paint().apply {
            color = Color.parseColor("#64748B")
            textSize = 8.5f
            isAntiAlias = true
        }

        canvas.drawText("TERMS & CONDITIONS", MARGIN, y, termHeaderPaint)
        y += 12f

        canvas.drawText("1. Goods once sold/fabricated will not be returned or exchanged.", MARGIN, y, termTextPaint)
        y += 10f
        canvas.drawText("2. Subject to local jurisdiction only.", MARGIN, y, termTextPaint)
        y += 10f
        canvas.drawText("3. Payment to be made as per agreed payment terms.", MARGIN, y, termTextPaint)
        y += 15f

        if (notes.isNotEmpty()) {
            canvas.drawText("Notes: $notes", MARGIN, y, termTextPaint)
            y += 20f
        }

        // Signatory box
        val sigX = PAGE_WIDTH - MARGIN - 180f
        val sigY = PAGE_WIDTH - 60f // Near bottom of page

        val sigPaint = Paint().apply {
            color = Color.parseColor("#0F172A")
            textSize = 9.5f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            isAntiAlias = true
        }

        canvas.drawText("For $businessName", sigX, sigY - 30f, sigPaint)
        canvas.drawText("Authorized Signatory", sigX, sigY, sigPaint)

        return sigY
    }

    fun sharePdf(context: Context, file: File, title: String) {
        val uri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            file
        )

        val shareIntent = Intent(Intent.ACTION_SEND).apply {
            type = "application/pdf"
            putExtra(Intent.EXTRA_STREAM, uri)
            putExtra(Intent.EXTRA_SUBJECT, title)
            putExtra(Intent.EXTRA_TEXT, "Please find attached $title.")
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }

        val chooser = Intent.createChooser(shareIntent, "Share Document via")
        chooser.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(chooser)
    }

    data class TableItem(
        val description: String,
        val quantity: Double,
        val rate: Double,
        val amount: Double
    )
}
