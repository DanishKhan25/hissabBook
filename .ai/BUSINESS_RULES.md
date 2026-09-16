# Business Rules & Calculation Formulas - HisabBook

This document specifies the business formulas, financial calculations, status state machines, stock deduction mechanics, PDF layout rules, input validation, and data backup procedures enforced in **HisabBook**.

---

## 📐 Financial Calculation Formulas

All monetary calculations in HisabBook strictly enforce non-negative constraints using Kotlin's `.coerceAtLeast(0.0)`.

### 1. Quotation Total
$$\text{Subtotal} = \text{Quantity} \times \text{Amount}$$
$$\text{Quotation Total} = \max\left(0.0, \text{Subtotal} - \text{Discount}\right)$$

### 2. Invoice / Bill Total
For a simple bill or a multi-line invoice:
$$\text{Line Item Amount} = \text{Unit Rate} \times \text{Quantity}$$
$$\text{Invoice Amount} = \sum \text{Line Item Amounts} \quad (\text{or base bill amount})$$
$$\text{Invoice Total} = \max\left(0.0, \text{Invoice Amount} - \text{Discount}\right)$$

### 3. Payment & Outstanding Balance
For a specific invoice with ID $\text{billId}$:
$$\text{Total Paid} = \sum_{\text{Payments for billId}} \text{Payment Amount}$$
$$\text{Balance Due} = \max\left(0.0, \text{Invoice Total} - \text{Total Paid}\right)$$

### 4. Global Dashboard Metrics
* **Total Sales Revenue**:
  $$\text{Total Revenue} = \sum_{\text{Invoices where status} \neq \text{"CANCELLED"}} \text{Invoice Total}$$
* **Total Collectibles (Outstanding Balance Across All Customers)**:
  $$\text{Total Collectibles} = \max\left(0.0, \text{Total Revenue} - \sum_{\text{All Payments}} \text{Payment Amount}\right)$$

---

## 🔄 Status State Machines

### 1. Invoice Payment Status
The payment status of an invoice is computed dynamically whenever a payment is recorded or an invoice is updated in [`InvoiceRepository.kt`](file:///Users/danishkhan/AndroidStudioProjects/HisabBook/app/src/main/java/com/example/hisabbook/data/repository/InvoiceRepository.kt):

```
                       ┌───────────┐
                       │  CANCELLED│
                       └─────▲─────┘
                             │ (Manual Cancellation)
                             │
                       ┌─────┴─────┐
                       │  UNPAID   │ (Initial State, Total Paid == 0)
                       └─────┬─────┘
                             │
                             │ (0 < Total Paid < Invoice Total)
                             ▼
                    ┌─────────────────┐
                    │ PARTIALLY_PAID  │
                    └────────┬────────┘
                             │
                             │ (Total Paid >= Invoice Total)
                             ▼
                       ┌───────────┐
                       │   PAID    │
                       └───────────┘
```

**State Logic**:
```kotlin
val newStatus = when {
    totalPaid >= invoice.total && invoice.total > 0 -> "PAID"
    totalPaid > 0 -> "PARTIALLY_PAID"
    else -> "UNPAID"
}
```

---

### 2. Quotation Status Lifecycle
Quotations follow a progressive approval workflow in [`QuotationViewModel.kt`](file:///Users/danishkhan/AndroidStudioProjects/HisabBook/app/src/main/java/com/example/hisabbook/ui/viewmodel/QuotationViewModel.kt):

```
       ┌────────────────────────┐
       │     Draft / PENDING    │ (Initial Creation)
       └───────────┬────────────┘
                   │
                   ▼
       ┌────────────────────────┐
       │          Sent          │ (Issued to Client)
       └─────┬────────────┬─────┘
             │            │
             ▼            ▼
   ┌──────────────┐   ┌──────────────┐
   │ Approved /   │   │ Rejected /   │
   │ ACCEPTED     │   │ DECLINED     │
   └──────┬───────┘   └──────────────┘
          │
          │ (Click "Convert to Invoice")
          ▼
   ┌──────────────┐
   │  CONVERTED   │ (Invoice generated, quote locked)
   └──────────────┘
```

**Quotation Conversion Rule**:
When a quotation is converted:
1. An Invoice record is created with `quotationId = quotation.id`, copying description, amount, discount, and customer ID.
2. An auto-generated bill number (e.g., `B-2026-001`) is assigned.
3. The quotation status is updated to `CONVERTED` (or `Converted`).

---

## 📦 Automatic Inventory Stock Deduction Rules

1. **Billing Deduction**:
   When an invoice is created with line items containing an `inventoryItemId` ($> 0$) and `quantity > 0`:
   * A `StockTransaction` of type `"STOCK_OUT"` is recorded referencing the bill number (e.g., `Bill #B-2026-001`).
   * The `InventoryItem.quantity` is reduced by the line item quantity.

2. **Manual Stock-In**:
   Adding stock via `StockInDialog` creates a `StockTransaction` of type `"IN"` and increases `InventoryItem.quantity`.

3. **Manual Stock-Out Validation**:
   When removing stock via `StockOutDialog`, the requested quantity **cannot exceed currently available stock**:
   $$\text{Requested Quantity} \le \text{Current Inventory Quantity}$$
   If $\text{Requested Quantity} > \text{Current Inventory Quantity}$, an error message is triggered and the transaction is aborted.

4. **Low Stock Threshold**:
   An item is classified as low stock whenever:
   $$\text{Current Quantity} \le \text{Minimum Stock Threshold}$$

---

## 📄 PDF Rendering & Document Generation Rules

PDF generation is performed locally using Android's native Canvas API in [`PdfGenerator.kt`](file:///Users/danishkhan/AndroidStudioProjects/HisabBook/app/src/main/java/com/example/hisabbook/util/PdfGenerator.kt):

1. **Canvas Page Specifications**:
   * Page Format: Standard A4 ($595 \times 842$ points).
   * Margins: $36\text{ pt}$ ($0.5\text{ inch}$) on all sides.

2. **Layout Sections**:
   * **Header**: Business Name ("IMAGE ALUMINIUM AND GLASS"), subtitle, mobile contact, and GSTIN (if present).
   * **Document Title Bar**: Highlighted banner with document type (`QUOTATION` or `BILL OF SUPPLY / INVOICE`), serial number, and date.
   * **Customer Details**: "BILL TO" section with customer name, phone number, and address.
   * **Items Table**: Dark styled header (`ITEMS / DESCRIPTION`, `QTY`, `RATE (₹)`, `AMOUNT (₹)`).
   * **Summary Section**: Subtotal, Discount, Grand Total, Total Received, and Balance Due.
   * **Amount in Words**: Formatted using [`NumberToWords.kt`](file:///Users/danishkhan/AndroidStudioProjects/HisabBook/app/src/main/java/com/example/hisabbook/util/NumberToWords.kt) (e.g., `Rupees Forty-Eight Thousand Only`).
   * **Terms & Conditions**: Default terms ("Goods once sold will not be returned", "Subject to local jurisdiction").
   * **Signature Block**: "For [Business Name]" with Authorized Signatory space.

3. **File Provider & Sharing**:
   * PDFs are saved to `context.cacheDir/documents/`.
   * Shared via `FileProvider` with URI `com.example.hisabbook.fileprovider`.

---

## ⚡ Input Validation Rules

| Entity | Field | Rule | Error Message |
| :--- | :--- | :--- | :--- |
| **Customer** | `name` | Must not be empty or blank | `"Customer name cannot be empty"` |
| **Customer** | `phone` | Optional, but if provided, must be exactly 10 digits | `"Please enter a valid 10-digit mobile number"` |
| **Customer** | `phone` | Must be unique across all existing customers | `"A customer with this mobile number already exists"` |
| **InventoryItem**| `name` | Must not be empty or blank | `"Item name cannot be empty"` |
| **InventoryItem**| `name` | Must be unique across all existing items | `"An inventory item with this name already exists"` |
| **InventoryItem**| `quantity`| Stock-out quantity cannot exceed current stock | `"Cannot reduce stock below 0! Available stock is X..."` |
| **Payment** | `amount` | Must be greater than `0.0` | `"Payment amount must be greater than 0"` |

---

## 💾 Demo Data Seeding & Backup Strategy

### 1. Automatic Demo Data Seeding
* Initialized in `AppDatabase.getInstance()` via [`DemoDataSeeder.kt`](file:///Users/danishkhan/AndroidStudioProjects/HisabBook/app/src/main/java/com/example/hisabbook/data/local/DemoDataSeeder.kt).
* Checks if the database is empty (`db.customerDao().getAllCustomersOneShot()`). If empty, seeds default business profile, 4 customers, 11 inventory items, stock transactions, 1 quotation, 3 invoices, and 3 payments.

### 2. JSON Backup Export & Import
* **Export**: `BackupManager.exportBackupToJson(db)` builds a JSON document containing database version (`1`), export timestamp, and arrays for all 7 entity tables.
* **Import**: `BackupManager.importBackupFromJson(db, json)` parses the JSON structure and invokes `AppDatabase.restoreDatabase(...)` inside a single `@Transaction`, atomically wiping current tables and populating restored records.
