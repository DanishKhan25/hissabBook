# Screen Catalog & UI Route Registry - HisabBook

This document provides a complete registry of all UI screens, modal dialogs, ViewModels, and Navigation 3 `HisabBookNavKey` routes in **HisabBook**.

---

## 🗺️ Navigation Map Overview

```
                        ┌──────────────────┐
                        │ OnboardingScreen │
                        └────────┬─────────┘
                                 │
                                 ▼
                     ┌──────────────────────┐
                     │  HomeDashboardScreen │ ◄──────────────────────┐
                     └──────────┬───────────┘                        │
                                │                                    │
   ┌────────────────┬───────────┼───────────┬────────────────┐       │
   │                │           │           │                │       │
   ▼                ▼           ▼           ▼                ▼       │
CustomerList    QuotationList InvoiceList PaymentHistory InventoryList Settings
   │                │           │                            │       │
   ▼                ▼           ▼                            │       │
CustomerDetail QuotationDetail InvoiceDetail ───────────────┤       │
                                                             │       │
   └─────────────────────────────────────────────────────────┴───────┘
```

---

## 🖥️ Detailed Catalog of Screens & Dialogs

### 1. `OnboardingScreen`
* **Route Key**: `HisabBookNavKey.Onboarding`
* **ViewModel**: `SettingsViewModel`
* **File Path**: [`OnboardingScreen.kt`](file:///Users/danishkhan/AndroidStudioProjects/HisabBook/app/src/main/java/com/example/hisabbook/ui/onboarding/OnboardingScreen.kt)
* **Description**: Introductory screen displayed on initial app launch if `isOnboardingCompleted == false`. Allows the user to configure business name, phone number, GSTIN, address, and select preferred app language (`English` / `Hindi`).
* **Key Actions**: Saves profile via `SettingsViewModel` and navigates to `HomeDashboard`.

---

### 2. `DashboardScreen` (Home Dashboard)
* **Route Key**: `HisabBookNavKey.HomeDashboard`
* **ViewModels**: `DashboardViewModel`, `CustomerViewModel`, `PaymentViewModel`, `InventoryViewModel`
* **File Path**: [`DashboardScreen.kt`](file:///Users/danishkhan/AndroidStudioProjects/HisabBook/app/src/main/java/com/example/hisabbook/ui/dashboard/DashboardScreen.kt)
* **Description**: Main landing screen featuring:
  * **Top Header**: Business title ("IMAGE ALUMINIUM AND GLASS") and quick link to Settings.
  * **Financial Overview Cards**: Total Sales Revenue and Total Collectibles Outstanding.
  * **Low Stock Warning Card**: Clickable banner showing low stock item count; clicking filters inventory to low-stock items.
  * **Quick Actions Row**: Shortcuts to Create Quote, Create Bill, Add Customer, Record Payment.
  * **Recent Transactions Feed**: Live stream of recent invoices with payment status badges (`PAID`, `PARTIALLY_PAID`, `UNPAID`).

---

### 3. `CustomerListScreen`
* **Route Key**: `HisabBookNavKey.CustomerList`
* **ViewModel**: `CustomerViewModel`
* **File Path**: [`CustomerListScreen.kt`](file:///Users/danishkhan/AndroidStudioProjects/HisabBook/app/src/main/java/com/example/hisabbook/ui/customer/CustomerListScreen.kt)
* **Description**: Comprehensive searchable list of all customers. Each customer card highlights:
  * Customer Name, Mobile Number, and Address.
  * Outstanding Balance Badge (colored red if balance > 0, green if settled).
  * Total Invoiced & Total Paid summary.
* **Key Actions**: Tap customer card to open `CustomerDetail`; Floating Action Button opens `AddEditCustomerDialog`.

---

### 4. `CustomerDetailScreen`
* **Route Key**: `HisabBookNavKey.CustomerDetail(val customerId: Long)`
* **ViewModels**: `CustomerViewModel`, `PaymentViewModel`
* **File Path**: [`CustomerDetailScreen.kt`](file:///Users/danishkhan/AndroidStudioProjects/HisabBook/app/src/main/java/com/example/hisabbook/ui/customer/CustomerDetailScreen.kt)
* **Description**: Full 360-degree customer ledger containing:
  * Header showing customer contact details and current outstanding balance.
  * **Action Buttons**: "New Quotation", "New Bill", "Record Payment", "Edit Profile".
  * **Tabbed View**: Invoices list, Quotations list, and Payment history for this customer.

---

### 5. `AddEditCustomerDialog` & `SearchableCustomerPicker`
* **ViewModels**: `CustomerViewModel`, `InvoiceViewModel`, `QuotationViewModel`
* **File Paths**:
  * [`AddEditCustomerDialog.kt`](file:///Users/danishkhan/AndroidStudioProjects/HisabBook/app/src/main/java/com/example/hisabbook/ui/customer/AddEditCustomerDialog.kt)
  * [`SearchableCustomerPicker.kt`](file:///Users/danishkhan/AndroidStudioProjects/HisabBook/app/src/main/java/com/example/hisabbook/ui/customer/SearchableCustomerPicker.kt)
* **Description**: Modal dialog for creating or updating customer information. Features real-time 10-digit mobile number validation and unique phone check. `SearchableCustomerPicker` offers an auto-completing dropdown search across existing customers when drafting invoices or quotations.

---

### 6. `QuotationListScreen`
* **Route Key**: `HisabBookNavKey.QuotationList`
* **ViewModel**: `QuotationViewModel`
* **File Path**: [`QuotationListScreen.kt`](file:///Users/danishkhan/AndroidStudioProjects/HisabBook/app/src/main/java/com/example/hisabbook/ui/quotation/QuotationListScreen.kt)
* **Description**: List of all estimated price quotes. Includes:
  * Search bar (matches quote number, customer name, description).
  * Filter chips (`ALL`, `DRAFT`, `SENT`, `APPROVED`, `REJECTED`, `CONVERTED`).
  * FAB to launch `CreateQuotationScreen`.

---

### 7. `CreateQuotationScreen`
* **Route Key**: `HisabBookNavKey.CreateQuotation(val customerId: Long? = null, val quotationIdToEdit: Long? = null)`
* **ViewModel**: `QuotationViewModel`
* **File Path**: [`CreateQuotationScreen.kt`](file:///Users/danishkhan/AndroidStudioProjects/HisabBook/app/src/main/java/com/example/hisabbook/ui/quotation/CreateQuotationScreen.kt)
* **Description**: Form for drafting a quotation or editing an existing one:
  * Customer selection via `SearchableCustomerPicker` (with "Add New Customer" button).
  * Item description, quantity, base rate/price, discount, and remarks/terms notes.
  * Real-time calculation of quote total.

---

### 8. `QuotationDetailScreen`
* **Route Key**: `HisabBookNavKey.QuotationDetail(val quotationId: Long)`
* **ViewModel**: `QuotationViewModel`
* **File Path**: [`QuotationDetailScreen.kt`](file:///Users/danishkhan/AndroidStudioProjects/HisabBook/app/src/main/java/com/example/hisabbook/ui/quotation/QuotationDetailScreen.kt)
* **Description**: Detailed quote document viewer:
  * Shows quote status badge (`Draft`, `Sent`, `Approved`, `Rejected`, `Converted`).
  * Actions to update quote status.
  * **"Convert to Bill / Invoice"**: One-click action converting quote into an active Invoice and setting status to `CONVERTED`.
  * **"Share PDF"**: Generates PDF via `PdfGenerator` and opens native Android share sheet (WhatsApp / Email).

---

### 9. `InvoiceListScreen`
* **Route Key**: `HisabBookNavKey.InvoiceList`
* **ViewModel**: `InvoiceViewModel`
* **File Path**: [`InvoiceListScreen.kt`](file:///Users/danishkhan/AndroidStudioProjects/HisabBook/app/src/main/java/com/example/hisabbook/ui/invoice/InvoiceListScreen.kt)
* **Description**: List of all generated invoices/bills:
  * Search bar (bill number, customer name, description).
  * Filter chips (`ALL`, `UNPAID`, `PARTIALLY PAID`, `PAID`).
  * Bill cards displaying bill number, customer name, total amount, and payment status badge.

---

### 10. `CreateInvoiceScreen`
* **Route Key**: `HisabBookNavKey.CreateInvoice(val customerId: Long? = null, val quotationId: Long? = null, val invoiceIdToEdit: Long? = null)`
* **ViewModel**: `InvoiceViewModel`
* **File Path**: [`CreateInvoiceScreen.kt`](file:///Users/danishkhan/AndroidStudioProjects/HisabBook/app/src/main/java/com/example/hisabbook/ui/invoice/CreateInvoiceScreen.kt)
* **Description**: Complete billing form supporting multi-item entry:
  * Customer picker and auto-generated bill number (e.g., `B-2026-001`).
  * Option to select inventory items from dropdown (links line items to inventory for automatic stock deduction).
  * Discount input and live payable total calculation.

---

### 11. `InvoiceDetailScreen`
* **Route Key**: `HisabBookNavKey.InvoiceDetail(val invoiceId: Long)`
* **ViewModels**: `InvoiceViewModel`, `PaymentViewModel`, `CustomerViewModel`
* **File Path**: [`InvoiceDetailScreen.kt`](file:///Users/danishkhan/AndroidStudioProjects/HisabBook/app/src/main/java/com/example/hisabbook/ui/invoice/InvoiceDetailScreen.kt)
* **Description**: Full invoice detail view with real-time payment tracking:
  * Displays bill breakdown, total amount, total received, and balance due.
  * List of recorded payments against this bill.
  * **"Record Payment" Button**: Launches `AddPaymentDialog`.
  * **"Share PDF" Button**: Generates invoice PDF with `NumberToWords` and opens share sheet.

---

### 12. `PaymentHistoryScreen`
* **Route Key**: `HisabBookNavKey.PaymentHistory`
* **ViewModels**: `PaymentViewModel`, `CustomerViewModel`
* **File Path**: [`PaymentHistoryScreen.kt`](file:///Users/danishkhan/AndroidStudioProjects/HisabBook/app/src/main/java/com/example/hisabbook/ui/payment/PaymentHistoryScreen.kt)
* **Description**: Audit log of all payments received across all customers. Includes search filtering, payment mode chips (`CASH`, `UPI`, `BANK_TRANSFER`, `CHEQUE`, `OTHER`), and total collected calculation.

---

### 13. `AddPaymentDialog`
* **ViewModel**: `PaymentViewModel`
* **File Path**: [`AddPaymentDialog.kt`](file:///Users/danishkhan/AndroidStudioProjects/HisabBook/app/src/main/java/com/example/hisabbook/ui/payment/AddPaymentDialog.kt)
* **Description**: Modal dialog for recording a payment against a bill. Shows remaining outstanding balance, allows selecting payment mode (`CASH`, `UPI`, `BANK_TRANSFER`, `CHEQUE`), and automatically recalculates invoice status.

---

### 14. `InventoryListScreen`
* **Route Key**: `HisabBookNavKey.InventoryList`
* **ViewModel**: `InventoryViewModel`
* **File Path**: [`InventoryListScreen.kt`](file:///Users/danishkhan/AndroidStudioProjects/HisabBook/app/src/main/java/com/example/hisabbook/ui/inventory/InventoryListScreen.kt)
* **Description**: Inventory management dashboard showing stock levels for raw materials and hardware. Features:
  * Search bar and "Low Stock Only" filter toggle.
  * Cards displaying item name, available quantity, unit, purchase price, and low stock alert indicator.
  * Actions for **Add Item** ([`AddInventoryItemDialog.kt`](file:///Users/danishkhan/AndroidStudioProjects/HisabBook/app/src/main/java/com/example/hisabbook/ui/inventory/AddInventoryItemDialog.kt)), **Stock In** ([`StockInDialog.kt`](file:///Users/danishkhan/AndroidStudioProjects/HisabBook/app/src/main/java/com/example/hisabbook/ui/inventory/StockInDialog.kt)), and **Stock Out** ([`StockOutDialog.kt`](file:///Users/danishkhan/AndroidStudioProjects/HisabBook/app/src/main/java/com/example/hisabbook/ui/inventory/StockOutDialog.kt)).

---

### 15. `SettingsScreen`
* **Route Key**: `HisabBookNavKey.Settings`
* **ViewModel**: `SettingsViewModel`
* **File Path**: [`SettingsScreen.kt`](file:///Users/danishkhan/AndroidStudioProjects/HisabBook/app/src/main/java/com/example/hisabbook/ui/settings/SettingsScreen.kt)
* **Description**: Business settings and data administration:
  * Edit business profile (Name, Subtitle, Contact, GSTIN, Address).
  * App Language Selector (`English` / `Hindi`).
  * **Data Management**: "Export JSON Backup", "Import JSON Backup", "Re-seed Demo Data".
