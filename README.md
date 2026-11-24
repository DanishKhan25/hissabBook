# 📱 HisabBook — Business Accounting, Billing & Inventory

<p align="center">
  <img src="https://img.shields.io/badge/Platform-Android-3DDC84?style=for-the-badge&logo=android&logoColor=white" alt="Platform" />
  <img src="https://img.shields.io/badge/Language-Kotlin-7F52FF?style=for-the-badge&logo=kotlin&logoColor=white" alt="Kotlin" />
  <img src="https://img.shields.io/badge/UI-Jetpack%20Compose-4285F4?style=for-the-badge&logo=jetpackcompose&logoColor=white" alt="Jetpack Compose" />
  <img src="https://img.shields.io/badge/Storage-Room%20DB-00758F?style=for-the-badge&logo=sqlite&logoColor=white" alt="Room DB" />
  <img src="https://img.shields.io/badge/Architecture-MVVM%20%2B%20Clean-FF6F00?style=for-the-badge" alt="Architecture" />
  <img src="https://img.shields.io/badge/Language%20Support-English%20%7C%20Hindi-E91E63?style=for-the-badge" alt="Multilingual" />
</p>

**HisabBook** is a modern, offline-first business ledger, billing, quotation, and inventory management application built natively for Android. Designed specifically for retail shops, wholesalers, freelancers, and small-to-medium businesses (SMBs), it replaces traditional paper ledgers (*Khata*) with a fast, private, and automated digital system.

---

## 🌟 Key Features

### 🧾 1. Invoicing & Smart Billing
- **Instant Invoice Creation**: Add bill items, search existing inventory with auto-filled prices, or create custom on-the-fly line items.
- **Professional PDF Generation**: Generate clean, shareable sales invoices and GST-ready receipts.
- **1-Click Share**: Share invoices directly with customers via WhatsApp, Email, or standard Android share sheets.
- **Indian Rupee Formatting & Words**: Automatic conversion of total invoice amounts into words (e.g., *₹12,450 — Twelve Thousand Four Hundred Fifty Rupees Only*).

### 📑 2. Quotations & Estimates
- Create professional quotations and price estimates for prospective clients.
- One-tap conversion of accepted quotations directly into official sales invoices.
- Track quotation statuses (*Draft*, *Sent*, *Accepted*, *Invoiced*).

### 📦 3. Real-Time Inventory & Stock Management
- **Stock Tracking**: Track item quantities, purchase prices, selling prices, and units (Pcs, Kg, Box, Litre, etc.).
- **Automated Stock Deductions**: Stock levels automatically adjust upon invoice creation.
- **Stock-In & Stock-Out Audits**: Manually log stock replenishments or damaged/returned goods with detailed audit history.
- **Customer Linking**: View which customer orders impacted specific item stock levels.

### 👥 4. Customer Khata (Digital Ledger)
- **Receivables & Payables Tracking**: Monitor customer balances (*Udhar / Jama*) in real-time.
- **Customer Profiles**: Store customer phone numbers, addresses, GSTIN, and credit limits.
- **Ledger Statements**: View complete chronological transaction histories per customer.

### 💳 5. Payment & Cashflow Tracking
- Log full or partial invoice payments.
- Support for multiple payment modes: **Cash**, **UPI**, **Bank Transfer**, and **Cheque**.
- Payment history screen with date filters and customer summaries.

### 🌐 6. Multilingual & Localization
- Native bilingual support for **English** and **Hindi (हिंदी)** with instant in-app language switching.
- Localized date, number, and currency formatting.

### 🔒 7. 100% Offline-First & Private
- All business records, customer ledgers, and transactions are stored securely on the local device using **Room Database (SQLite)**.
- **JSON Backup & Restore**: Export all business data as a standalone encrypted backup file for seamless data migration.

---

## 🏗️ Architecture & Tech Stack

The application follows **Clean Architecture** and the **MVVM (Model-View-ViewModel)** design pattern with unidirectional data flow:

```
┌────────────────────────────────────────────────────────┐
│                   UI Layer (Presentation)              │
│       Jetpack Compose • Material 3 • Navigation        │
└───────────────────────────▲────────────────────────────┘
                            │ StateFlow / Events
┌───────────────────────────┴────────────────────────────┐
│                    ViewModel Layer                     │
│    InvoiceViewModel • CustomerViewModel • InventoryVM  │
└───────────────────────────▲────────────────────────────┘
                            │ Kotlin Coroutines
┌───────────────────────────┴────────────────────────────┐
│                    Repository Layer                    │
│   InvoiceRepository • CustomerRepository • AppRepo     │
└───────────────────────────▲────────────────────────────┘
                            │ Room DAO
┌───────────────────────────┴────────────────────────────┐
│                   Data & Storage Layer                 │
│         Room SQLite Database • Local Backup Manager    │
└────────────────────────────────────────────────────────┘
```

- **Language**: Kotlin 1.9+
- **UI Toolkit**: Jetpack Compose & Material 3
- **Local Persistence**: Android Room Persistence Library (SQLite)
- **Asynchronous Flow**: Kotlin Coroutines & `StateFlow`
- **Dependency Injection / Factory**: Custom ViewModel Factories & Database Providers
- **PDF Engine**: Android `PdfDocument` & Canvas API
- **File Sharing**: Android `FileProvider`

---

## 📁 Project Structure

```
app/src/main/java/com/example/hisabbook/
├── data/
│   ├── local/
│   │   ├── dao/                 # Room DAOs (CustomerDao, InvoiceDao, ItemDao, etc.)
│   │   ├── entity/              # Database entities (Customer, Invoice, InventoryItem)
│   │   ├── AppDatabase.kt       # Room Database configuration
│   │   ├── BackupManager.kt     # JSON backup and restore utilities
│   │   └── DemoDataSeeder.kt    # Sample business data seeder
│   └── repository/              # Repository interfaces & implementations
│
├── ui/
│   ├── customer/                # Customer list, ledger & customer detail screens
│   ├── dashboard/               # Financial dashboard & quick actions
│   ├── inventory/               # Stock list, stock-in/out dialogs & item editor
│   ├── invoice/                 # Invoice creation, invoice preview & detail screens
│   ├── payment/                 # Payment logging dialogs & payment history screen
│   ├── quotation/               # Quotation management & conversion screens
│   ├── settings/                # Business profile, language picker & backup settings
│   ├── navigation/              # Type-safe Navigation keys & route definitions
│   ├── theme/                   # Material 3 colors, typography & shapes
│   └── viewmodel/               # ViewModels for business logic orchestration
│
└── util/                        # PDF Generator, Currency formatter, Number-to-Words
```

---

## 🚀 Getting Started

### Prerequisites
- **Android Studio** Hedgehog | 2023.1.1 or newer
- **JDK 17**
- Android SDK with `minSdkVersion = 26` and `targetSdkVersion = 34`

### Build & Run
1. Clone the repository:
   ```bash
   git clone https://github.com/DanishKhan25/HisabBook.git
   ```
2. Open the project in Android Studio.
3. Sync Gradle and build the project:
   ```bash
   ./gradlew assembleDebug
   ```
4. Run on an Android device or emulator running Android 8.0 (API 26) or higher.

---

## 📄 License

This project is licensed under the MIT License — see the [LICENSE](LICENSE) file for details.
