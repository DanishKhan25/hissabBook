# System Architecture - HisabBook

This document describes the architectural pattern, data layers, navigation system, dependency injection, and utility infrastructure of **HisabBook**.

---

## 🏗️ Architectural Overview (Clean MVVM)

HisabBook strictly adheres to the official **Android Recommended Architecture Guide**, implementing a clean **Model-View-ViewModel (MVVM)** pattern with Unidirectional Data Flow (UDF).

```
 ┌─────────────────────────────────────────────────────────┐
 │                       UI Layer                          │
 │  Jetpack Compose Screens, Dialogs, NavDisplay           │
 └───────────────────────────▲─────────────────────────────┘
                             │ StateFlow / Events
 ┌───────────────────────────▼─────────────────────────────┐
 │                    ViewModel Layer                      │
 │  CustomerVM, InvoiceVM, QuotationVM, InventoryVM, etc. │
 └───────────────────────────▲─────────────────────────────┘
                             │ Kotlin Coroutines / Flow
 ┌───────────────────────────▼─────────────────────────────┐
 │                    Repository Layer                     │
 │  CustomerRepo, InvoiceRepo, QuotationRepo, AppRepository│
 └───────────────────────────▲─────────────────────────────┘
                             │ DAO Interface Method Calls
 ┌───────────────────────────▼─────────────────────────────┐
 │                     Data Layer                          │
 │  Room Database (AppDatabase), Room DAOs, SQLite DB      │
 └─────────────────────────────────────────────────────────┘
```

---

## 📂 Layer Responsibilities

### 1. UI Layer (`com.example.hisabbook.ui.*`)
* **Framework**: Built entirely with **Jetpack Compose** and **Material Design 3 (M3 Expressive)**.
* **Edge-to-Edge**: Enabled in [`MainActivity.kt`](file:///Users/danishkhan/AndroidStudioProjects/HisabBook/app/src/main/java/com/example/hisabbook/MainActivity.kt) using `enableEdgeToEdge()`.
* **State Consumption**: UI components collect immutable `StateFlow` from ViewModels using `collectAsStateWithLifecycle()`.
* **Zero XML Layouts**: 100% declarative Compose components.

### 2. ViewModel Layer (`com.example.hisabbook.ui.viewmodel.*`)
ViewModels manage UI state, process user input, and coordinate business logic:
* **`DashboardViewModel`**: Exposes high-level metrics (total sales revenue, outstanding collectibles, low stock count, recent activity feed).
* **`CustomerViewModel`**: Manages customer list, customer balances (`CustomerWithBalance`), search filtering, and detailed customer ledger (`CustomerDetailUiState`).
* **`InvoiceViewModel`**: Handles invoice creation, bill number generation (`B-YYYY-XXX`), status updates (`UNPAID`, `PARTIALLY_PAID`, `PAID`), payment recording, and stock auto-deduction.
* **`QuotationViewModel`**: Handles quotation drafting (`Q-YYYY-XXX`), status lifecycle (`Draft`, `Sent`, `Approved`, `Rejected`), and one-click quotation-to-invoice conversion.
* **`InventoryViewModel`**: Manages inventory stock levels, low-stock threshold queries, Stock-In operations, and Stock-Out operations with stock validation.
* **`PaymentViewModel`**: Manages global payment history, payment mode breakdowns, and invoice-payment correlations.
* **`SettingsViewModel`**: Manages business profile editing, language preference switching, JSON backup export/import, and demo data seeding.

### 3. Repository Layer (`com.example.hisabbook.data.repository.*`)
Repositories serve as the single source of truth for business operations:
* **`AppRepository`**: Unified repository container providing singleton access to all underlying repositories.
* **`CustomerRepository`**: Wraps `CustomerDao`.
* **`InvoiceRepository`**: Wraps `InvoiceDao` and `PaymentDao`, offering composite queries like `getInvoiceOutstandingAmount()` and `recalculateInvoiceStatus()`.
* **`QuotationRepository`**: Wraps `QuotationDao` and executes atomic quotation-to-invoice conversions.
* **`PaymentRepository`**: Wraps `PaymentDao` and automatically notifies `InvoiceRepository` to recalculate invoice payment status upon payment recording.
* **`InventoryRepository`**: Wraps `InventoryDao` and `StockTransactionDao`, coordinating stock quantity updates with transaction history records.
* **`BusinessProfileRepository`**: Wraps `BusinessProfileDao`.

### 4. Data Layer (`com.example.hisabbook.data.local.*`)
* **`AppDatabase`**: Abstract Room database extending `RoomDatabase`. Contains 7 Room entities: `Customer`, `Quotation`, `Invoice`, `Payment`, `InventoryItem`, `StockTransaction`, `BusinessProfile`.
* **Transactional Restores**: Contains `@Transaction open suspend fun restoreDatabase(...)` to clear and re-populate all database tables atomically during backup restoration.
* **Database Singleton**: Thread-safe initialization using `@Volatile` instance and `synchronized(this)` in `AppDatabase.getInstance(context)`.

---

## 🧭 Navigation Architecture (Navigation 3)

HisabBook utilizes **Jetpack Navigation 3** (`androidx.navigation3`):

* **Route Declarations**: All routes are defined as type-safe `@Serializable` data objects/classes implementing `HisabBookNavKey` in [`HisabBookNavKey.kt`](file:///Users/danishkhan/AndroidStudioProjects/HisabBook/app/src/main/java/com/example/hisabbook/ui/navigation/HisabBookNavKey.kt):
  ```kotlin
  sealed interface HisabBookNavKey : NavKey {
      @Serializable data object Onboarding : HisabBookNavKey
      @Serializable data object HomeDashboard : HisabBookNavKey
      @Serializable data object CustomerList : HisabBookNavKey
      @Serializable data class CustomerDetail(val customerId: Long) : HisabBookNavKey
      @Serializable data object QuotationList : HisabBookNavKey
      @Serializable data class CreateQuotation(val customerId: Long? = null, val quotationIdToEdit: Long? = null) : HisabBookNavKey
      @Serializable data class QuotationDetail(val quotationId: Long) : HisabBookNavKey
      @Serializable data object InvoiceList : HisabBookNavKey
      @Serializable data class CreateInvoice(val customerId: Long? = null, val quotationId: Long? = null, val invoiceIdToEdit: Long? = null) : HisabBookNavKey
      @Serializable data class InvoiceDetail(val invoiceId: Long) : HisabBookNavKey
      @Serializable data object PaymentHistory : HisabBookNavKey
      @Serializable data object InventoryList : HisabBookNavKey
      @Serializable data object Settings : HisabBookNavKey
  }
  ```
* **Backstack Management**: Stack state is maintained by `rememberNavBackStack(HisabBookNavKey.HomeDashboard)`.
* **Rendering**: Handled by `NavDisplay` combined with `entryProvider` in [`HisabBookNavigation.kt`](file:///Users/danishkhan/AndroidStudioProjects/HisabBook/app/src/main/java/com/example/hisabbook/ui/navigation/HisabBookNavigation.kt).

---

## 💉 Dependency Injection (`HisabBookViewModelFactory`)

Dependency injection is implemented using a custom, lightweight factory pattern:

```kotlin
class HisabBookViewModelFactory(
    private val application: Application
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        val repository = AppRepository.getInstance(application)
        val database = AppDatabase.getInstance(application)
        
        return when {
            modelClass.isAssignableFrom(DashboardViewModel::class.java) -> DashboardViewModel(...) as T
            modelClass.isAssignableFrom(CustomerViewModel::class.java) -> CustomerViewModel(...) as T
            modelClass.isAssignableFrom(InvoiceViewModel::class.java) -> InvoiceViewModel(...) as T
            modelClass.isAssignableFrom(QuotationViewModel::class.java) -> QuotationViewModel(...) as T
            modelClass.isAssignableFrom(InventoryViewModel::class.java) -> InventoryViewModel(...) as T
            modelClass.isAssignableFrom(PaymentViewModel::class.java) -> PaymentViewModel(...) as T
            modelClass.isAssignableFrom(SettingsViewModel::class.java) -> SettingsViewModel(...) as T
            else -> throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
        }
    }
}
```

This approach avoids third-party DI reflection overhead (such as Hilt or Koin) while preserving testability and clear component boundaries.

---

## 🛠️ Utility Services

1. **`PdfGenerator`** ([`PdfGenerator.kt`](file:///Users/danishkhan/AndroidStudioProjects/HisabBook/app/src/main/java/com/example/hisabbook/util/PdfGenerator.kt)):
   * Uses Android's native `android.graphics.pdf.PdfDocument`.
   * Draws company header, document title bar, customer details, item table, total calculations, number in words, terms, and signature block on an A4 canvas (595 x 842 pt).
   * Shares generated PDFs securely via Android `FileProvider`.

2. **`BackupManager`** ([`BackupManager.kt`](file:///Users/danishkhan/AndroidStudioProjects/HisabBook/app/src/main/java/com/example/hisabbook/data/local/BackupManager.kt)):
   * Serializes all 7 database entities into a structured, human-readable JSON payload.
   * Parses JSON backups and restores records inside a single atomic Room transaction.

3. **`NumberToWords`** ([`NumberToWords.kt`](file:///Users/danishkhan/AndroidStudioProjects/HisabBook/app/src/main/java/com/example/hisabbook/util/NumberToWords.kt)):
   * Converts numerical monetary amounts into formal Indian English words (handling Thousand, Lakh, Crore, and Paise).

4. **`LocaleHelper`** ([`LocaleHelper.kt`](file:///Users/danishkhan/AndroidStudioProjects/HisabBook/app/src/main/java/com/example/hisabbook/util/LocaleHelper.kt)):
   * Applies runtime locale settings (`en` / `hi`) via Android `LocaleManager`.
