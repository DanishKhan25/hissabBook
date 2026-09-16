package com.example.hisabbook.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.hisabbook.util.CurrencyFormatter

/**
 * A single row in the items table.
 */
data class BillLineItem(
    val name: String,
    val quantity: Double,
    val unit: String,
    val rate: Double,
    val amount: Double
)

/**
 * Shared embeddable card showing the itemised breakdown for both Invoice and Quotation
 * detail screens.
 *
 * - Shows: items table (name, qty, rate, amount), subtotal row, optional discount,
 *   grand total, and — when [paidAmount]/[balanceDue] are provided — received and
 *   outstanding balance rows.
 * - Does NOT include customer info or branding (those are already on the parent screen).
 * - Has no Scaffold / verticalScroll — safe to embed inside any scrollable parent.
 *
 * @param items         Line items to display. Pass a single-item list for quotations.
 * @param subtotal      Sum of all item amounts before discount.
 * @param discount      Discount applied (0.0 = not shown).
 * @param grandTotal    Final total after discount.
 * @param paidAmount    Amount already received. Pass null to hide payment rows (quotation mode).
 * @param balanceDue    Outstanding balance. Pass null to hide payment rows (quotation mode).
 * @param title         Card section title. Defaults to "Items".
 */
@Composable
fun BillItemsCard(
    items: List<BillLineItem>,
    subtotal: Double,
    discount: Double = 0.0,
    grandTotal: Double,
    paidAmount: Double? = null,
    balanceDue: Double? = null,
    title: String = "Items",
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(0.dp)
    ) {

        // ── Section title ────────────────────────────────────────────────────
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(10.dp))

        // ── Table header ─────────────────────────────────────────────────────
        TableHeaderRow()

        HorizontalDivider(thickness = 1.dp, color = MaterialTheme.colorScheme.outlineVariant)

        // ── Item rows ────────────────────────────────────────────────────────
        if (items.isEmpty()) {
            Text(
                text = "No items added.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.outline,
                modifier = Modifier.padding(vertical = 8.dp)
            )
        } else {
            items.forEachIndexed { index, item ->
                ItemRow(item = item)
                if (index < items.lastIndex) {
                    HorizontalDivider(
                        thickness = 0.5.dp,
                        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)
                    )
                }
            }
        }

        HorizontalDivider(thickness = 1.dp, color = MaterialTheme.colorScheme.outlineVariant)

        Spacer(modifier = Modifier.height(8.dp))

        // ── Subtotal ─────────────────────────────────────────────────────────
        SummaryRow(
            label = "Subtotal",
            value = CurrencyFormatter.formatCurrency(subtotal)
        )

        // ── Discount (only shown when > 0) ───────────────────────────────────
        if (discount > 0) {
            Spacer(modifier = Modifier.height(2.dp))
            SummaryRow(
                label = "Discount",
                value = "- ${CurrencyFormatter.formatCurrency(discount)}",
                valueColor = MaterialTheme.colorScheme.error
            )
        }

        Spacer(modifier = Modifier.height(4.dp))
        HorizontalDivider(thickness = 0.8.dp, color = MaterialTheme.colorScheme.outlineVariant)
        Spacer(modifier = Modifier.height(6.dp))

        // ── Grand Total ──────────────────────────────────────────────────────
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Grand Total",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = CurrencyFormatter.formatCurrency(grandTotal),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
        }

        // ── Payment rows (invoice mode only) ─────────────────────────────────
        if (paidAmount != null && balanceDue != null) {
            Spacer(modifier = Modifier.height(8.dp))
            HorizontalDivider(thickness = 0.8.dp, color = MaterialTheme.colorScheme.outlineVariant)
            Spacer(modifier = Modifier.height(6.dp))

            SummaryRow(
                label = "Received",
                value = CurrencyFormatter.formatCurrency(paidAmount),
                valueColor = Color(0xFF15803D)
            )

            Spacer(modifier = Modifier.height(4.dp))

            // Outstanding balance — red if pending, green if fully paid
            val isFullyPaid = balanceDue <= 0.0
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Outstanding Balance",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = CurrencyFormatter.formatCurrency(balanceDue),
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = if (isFullyPaid) Color(0xFF15803D) else MaterialTheme.colorScheme.error
                )
            }
        }
    }
}

// ── Private helpers ───────────────────────────────────────────────────────────

@Composable
private fun TableHeaderRow() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .padding(vertical = 6.dp, horizontal = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "ITEM",
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.weight(2.8f)
        )
        Text(
            text = "QTY",
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.End,
            modifier = Modifier.weight(1.1f)
        )
        Text(
            text = "RATE",
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.End,
            modifier = Modifier.weight(1.2f)
        )
        Text(
            text = "AMOUNT",
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.End,
            modifier = Modifier.weight(1.4f)
        )
    }
}

@Composable
private fun ItemRow(item: BillLineItem) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 7.dp, horizontal = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = item.name,
            style = MaterialTheme.typography.bodySmall,
            fontWeight = FontWeight.SemiBold,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.weight(2.8f)
        )
        Text(
            text = formatQty(item.quantity, item.unit),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.End,
            modifier = Modifier.weight(1.1f)
        )
        Text(
            text = if (item.rate > 0) CurrencyFormatter.formatAmountOnly(item.rate) else "—",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.End,
            modifier = Modifier.weight(1.2f)
        )
        Text(
            text = CurrencyFormatter.formatAmountOnly(item.amount),
            style = MaterialTheme.typography.bodySmall,
            fontWeight = FontWeight.Medium,
            textAlign = TextAlign.End,
            modifier = Modifier.weight(1.4f)
        )
    }
}

@Composable
private fun SummaryRow(
    label: String,
    value: String,
    valueColor: Color = Color.Unspecified
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.SemiBold,
            color = if (valueColor == Color.Unspecified) MaterialTheme.colorScheme.onSurface else valueColor
        )
    }
}

/** Formats quantity with unit, trimming unnecessary decimal zeros. */
private fun formatQty(qty: Double, unit: String): String {
    val qtyStr = if (qty == qty.toLong().toDouble()) {
        qty.toLong().toString()
    } else {
        String.format("%.2f", qty).trimEnd('0').trimEnd('.')
    }
    return if (unit.isBlank()) qtyStr else "$qtyStr $unit"
}
