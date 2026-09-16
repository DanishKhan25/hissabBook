# Data Models & Room Schema - HisabBook

This document details the database architecture, Room entities, field schemas, data types, primary keys, foreign key relationships, and data transfer structures in **HisabBook**.

---

## 📊 Entity Relationship Diagram (ERD)

```
                       ┌─────────────────────────┐
                       │     BusinessProfile     │
                       └─────────────────────────┘
                                    
 ┌─────────────────┐ 1                 N ┌──────────────────┐
 │    Customer     │ ─────────────────── │    Quotation     │
 └────────┬────────┘                     └────────┬─────────┘
          │ 1                                     │ 0..1
          │                                       │ (Conversion)
          │                                       ▼ 1
          │ 1                          N ┌──────────────────┐
          └───────────────────────────── │     Invoice      │
          │                              └────────┬─────────┘
          │ 1                                     │ 1
          │                                       │
          │                                       ▼ N
          │                            N ┌──────────────────┐
          └───────────────────────────── │     Payment      │
                                         └──────────────────┘

 ┌─────────────────┐ 1                 N ┌──────────────────┐
 │  InventoryItem  │ ─────────────────── │ StockTransaction │
 └────────┬────────┘                     └──────────────────┘
          │ 1
          │ (Referenced during billing)
          ▼ N
 ┌─────────────────┐
 │ InvoiceLineItem │  (In-Memory Billing Helper)
 └─────────────────┘
```

---

## 🗄️ Detailed Room Entities

All entities are stored in the Room SQLite database named `"hisab_book_database"`.

### 1. `Customer` (`customers` table)
Represents a client or buyer profile.

| Field Name | Kotlin Type | Database Column | Constraints | Description |
| :--- | :--- | :--- | :--- | :--- |
| `id` | `Long` | `id` | `@PrimaryKey(autoGenerate = true)` | Unique customer ID |
| `name` | `String` | `name` | `NOT NULL` | Customer or party full name |
| `phone` | `String` | `phone` | Default `""` | 10-digit mobile number |
| `address` | `String` | `address` | Default `""` | Billing/Delivery street address |
| `createdAt` | `Long` | `createdAt` | Default `System.currentTimeMillis()` | Timestamp of customer creation |

---

### 2. `Quotation` (`quotations` table)
Represents an estimated price quote or proposal issued to a customer.

| Field Name | Kotlin Type | Database Column | Constraints | Description |
| :--- | :--- | :--- | :--- | :--- |
| `id` | `Long` | `id` | `@PrimaryKey(autoGenerate = true)` | Unique quotation ID |
| `quotationNumber` | `String` | `quotationNumber` | `NOT NULL` | Serial code (e.g., `Q-2026-001`) |
| `customerId` | `Long` | `customerId` | `NOT NULL` | Foreign key referencing `Customer.id` |
| `description` | `String` | `description` | `NOT NULL` | Scope of work / items summary |
| `quantity` | `Double` | `quantity` | Default `1.0` | Total quantity or units |
| `amount` | `Double` | `amount` | Default `0.0` | Base rate / price before discount |
| `discount` | `Double` | `discount` | Default `0.0` | Deduction amount |
| `total` | `Double` | `total` | Default `0.0` | Calculated total (`(quantity * amount) - discount`) |
| `status` | `String` | `status` | Default `"PENDING"` / `"Draft"` | Status (`Draft`, `Sent`, `Approved`, `Rejected`, `CONVERTED`) |
| `notes` | `String` | `notes` | Default `""` | Additional remarks or terms |
| `createdAt` | `Long` | `createdAt` | Default `System.currentTimeMillis()` | Creation timestamp |

---

### 3. `Invoice` (`invoices` table)
Represents a tax invoice or bill issued to a customer.

| Field Name | Kotlin Type | Database Column | Constraints | Description |
| :--- | :--- | :--- | :--- | :--- |
| `id` | `Long` | `id` | `@PrimaryKey(autoGenerate = true)` | Unique invoice/bill ID |
| `billNumber` | `String` | `billNumber` | `NOT NULL` | Bill serial number (e.g., `B-2026-001`) |
| `customerId` | `Long` | `customerId` | `NOT NULL` | Foreign key referencing `Customer.id` |
| `quotationId` | `Long?` | `quotationId` | Nullable | FK referencing `Quotation.id` if converted |
| `description` | `String` | `description` | `NOT NULL` | Description of goods / works |
| `amount` | `Double` | `amount` | Default `0.0` | Subtotal amount |
| `discount` | `Double` | `discount` | Default `0.0` | Discount deducted |
| `total` | `Double` | `total` | Default `0.0` | Final payable total (`amount - discount`) |
| `status` | `String` | `status` | Default `"UNPAID"` | Status (`UNPAID`, `PARTIALLY_PAID`, `PAID`, `CANCELLED`) |
| `createdAt` | `Long` | `createdAt` | Default `System.currentTimeMillis()` | Billing timestamp |

---

### 4. `InvoiceLineItem` (In-Memory Billing Model)
A Kotlin data class used during invoice creation to handle multi-item billing and trigger automatic inventory stock deduction.

```kotlin
data class InvoiceLineItem(
    val inventoryItemId: Long? = null,
    val description: String = "",
    val unit: String = "Pcs",
    val unitRate: Double = 0.0,
    val quantity: Double = 1.0
) {
    val totalAmount: Double get() = unitRate * quantity
}
```

---

### 5. `Payment` (`payments` table)
Represents a monetary payment received from a customer against a specific invoice.

| Field Name | Kotlin Type | Database Column | Constraints | Description |
| :--- | :--- | :--- | :--- | :--- |
| `id` | `Long` | `id` | `@PrimaryKey(autoGenerate = true)` | Unique payment transaction ID |
| `billId` | `Long` | `billId` | `NOT NULL` | Foreign key referencing `Invoice.id` |
| `customerId` | `Long` | `customerId` | `NOT NULL` | Foreign key referencing `Customer.id` |
| `amount` | `Double` | `amount` | `NOT NULL` | Payment amount received |
| `paymentMode` | `String` | `paymentMode` | Default `"CASH"` | Mode (`CASH`, `UPI`, `BANK_TRANSFER`, `CHEQUE`, `OTHER`) |
| `date` | `Long` | `date` | Default `System.currentTimeMillis()` | Payment timestamp |
| `notes` | `String` | `notes` | Default `""` | Payment note / reference number |

---

### 6. `InventoryItem` (`inventory_items` table)
Represents a raw material, hardware, glass, or finished product held in inventory.

| Field Name | Kotlin Type | Database Column | Constraints | Description |
| :--- | :--- | :--- | :--- | :--- |
| `id` | `Long` | `id` | `@PrimaryKey(autoGenerate = true)` | Unique inventory item ID |
| `name` | `String` | `name` | `NOT NULL` | Item name (e.g., `TOUGHENED GLASS 12MM`) |
| `unit` | `String` | `unit` | Default `"Pcs"` | Unit of measurement (`Pcs`, `Kg`, `sq.ft`, `meter`, etc.) |
| `quantity` | `Double` | `quantity` | Default `0.0` | Current available stock balance |
| `minimumStock` | `Double` | `minimumStock` | Default `0.0` | Threshold for low stock warning |
| `purchasePrice` | `Double` | `purchasePrice` | Default `0.0` | Cost / purchase price per unit |
| `createdAt` | `Long` | `createdAt` | Default `System.currentTimeMillis()` | Item registration timestamp |

---

### 7. `StockTransaction` (`stock_transactions` table)
Represents an audit trail entry for stock movement (stock in, stock out, or billing adjustment).

| Field Name | Kotlin Type | Database Column | Constraints | Description |
| :--- | :--- | :--- | :--- | :--- |
| `id` | `Long` | `id` | `@PrimaryKey(autoGenerate = true)` | Unique stock movement ID |
| `inventoryItemId` | `Long` | `inventoryItemId` | `NOT NULL` | Foreign key referencing `InventoryItem.id` |
| `type` | `String` | `type` | `NOT NULL` | Transaction type (`IN`, `OUT`, `STOCK_OUT`, `ADJUSTMENT`) |
| `quantity` | `Double` | `quantity` | `NOT NULL` | Quantity added or removed |
| `purchasePrice` | `Double` | `purchasePrice` | Default `0.0` | Price per unit for this batch |
| `reason` | `String` | `reason` | Default `""` | Remark or bill reference (e.g., `Bill #B-2026-001`) |
| `reference` | `String` | `reference` | Default `""` | Document or invoice reference |
| `date` | `Long` | `date` | Default `System.currentTimeMillis()` | Transaction timestamp |

---

### 8. `BusinessProfile` (`business_profile` table)
Singleton table storing the business owner's company branding, contact details, and app configuration.

| Field Name | Kotlin Type | Database Column | Constraints | Description |
| :--- | :--- | :--- | :--- | :--- |
| `id` | `Int` | `id` | `@PrimaryKey` (Fixed = `1`) | Fixed primary key ensuring single row |
| `businessName` | `String` | `businessName` | Default `"IMAGE ALUMINIUM AND GLASS"` | Business title for headers & PDFs |
| `subtitle` | `String` | `subtitle` | Default `"Fabrication, Sliding Windows..."` | Subheading / specialization |
| `description` | `String` | `description` | Default `""` | Detailed company bio |
| `phone` | `String` | `phone` | Default `"8999127482"` | Primary business contact phone |
| `address` | `String` | `address` | Default `""` | Registered business address |
| `languagePreference` | `String` | `languagePreference` | Default `"en"` | Language code (`"en"` or `"hi"`) |
| `currencySymbol` | `String` | `currencySymbol` | Default `"₹"` | Active currency symbol |
| `gstNumber` | `String` | `gstNumber` | Default `""` | GSTIN tax identification number |
| `isOnboardingCompleted` | `Boolean` | `isOnboardingCompleted` | Default `false` | Flag tracking onboarding completion |
