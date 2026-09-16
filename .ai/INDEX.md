# HisabBook - Comprehensive AI Documentation Suite

Welcome to the central entry point for the **HisabBook** AI Documentation suite. This documentation serves as an instant context guide for AI agents, developers, and maintainers working on the HisabBook codebase.

---

## 📌 High-Level Summary

**HisabBook** is an offline-first, Material 3 Jetpack Compose Android application designed specifically for small businesses, fabricators, and glass contractors in India—with tailored defaults for **IMAGE ALUMINIUM AND GLASS**. 

The app functions as a digital ledger ("Hisab Book"), invoice maker, quotation builder, payment tracker, and inventory manager. It enables business owners to manage customer balances, record payments, calculate outstanding dues, generate professionally formatted PDF bills/quotations, and manage stock—all 100% offline without any cloud dependency.

---

## 🛠️ Technology Stack

| Layer | Technology / Library | Description |
| :--- | :--- | :--- |
| **Language** | Kotlin 2.0+ | Idiomatic Kotlin with Coroutines & StateFlow |
| **UI Framework** | Jetpack Compose + Material 3 | Modern declarative UI, Edge-to-Edge display, dynamic M3 components |
| **Navigation** | Jetpack Navigation 3 (`androidx.navigation3`) | Type-safe, state-driven navigation using `@Serializable` `NavKey` objects |
| **Database** | Room DB (`androidx.room`) | SQLite database (`hisab_book_database`), DAO pattern, reactive `Flow` queries |
| **Dependency Injection** | Manual Factory Pattern | Centralized `HisabBookViewModelFactory` & `AppRepository` singleton |
| **PDF Engine** | Native Android `PdfDocument` | Custom Canvas rendering in `PdfGenerator` with `FileProvider` sharing |
| **Number Utility** | `NumberToWords` | Custom converter for Indian numbering system (Rupees, Lakhs, Crores, Paise) |
| **Data Backup** | JSON Backup & Restore | Full database import/export managed transactionally via `BackupManager` |
| **Localization** | Android `LocaleManager` & `LocaleHelper` | Dual language support (English `en` & Hindi `hi`) |

---

## 📚 Documentation Index

Click any link below to explore specific topics in detail:

1. [**`PROJECT_OVERVIEW.md`**](file:///Users/danishkhan/AndroidStudioProjects/HisabBook/.ai/PROJECT_OVERVIEW.md)
   * Business domain details (**IMAGE ALUMINIUM AND GLASS**).
   * Product vision, core objectives, and target audience.
   * Offline-first architecture philosophy.
   * Localization strategy (English & Hindi) and currency formatting.

2. [**`ARCHITECTURE.md`**](file:///Users/danishkhan/AndroidStudioProjects/HisabBook/.ai/ARCHITECTURE.md)
   * Clean MVVM architecture (UI, ViewModel, Repository, DAO/Database).
   * Jetpack Navigation 3 routing and backstack mechanics.
   * Manual Dependency Injection setup via `HisabBookViewModelFactory`.
   * Utility services (`PdfGenerator`, `BackupManager`, `LocaleHelper`).

3. [**`DATA_MODELS.md`**](file:///Users/danishkhan/AndroidStudioProjects/HisabBook/.ai/DATA_MODELS.md)
   * Complete schema definitions for all Room entities (`Customer`, `Quotation`, `Invoice`, `Payment`, `InventoryItem`, `StockTransaction`, `BusinessProfile`).
   * Field descriptions, data types, primary keys, and constraints.
   * Entity relationship diagrams and data flows.

4. [**`SCREEN_CATALOG.md`**](file:///Users/danishkhan/AndroidStudioProjects/HisabBook/.ai/SCREEN_CATALOG.md)
   * Breakdown of all 14+ UI screens, dialogs, and navigation routes.
   * Mapping of screens to ViewModels and Navigation 3 `HisabBookNavKey` routes.
   * Key UI components, actions, and user flows.

5. [**`BUSINESS_RULES.md`**](file:///Users/danishkhan/AndroidStudioProjects/HisabBook/.ai/BUSINESS_RULES.md)
   * Financial & inventory calculation formulas.
   * State machines for Invoice and Quotation statuses.
   * Automatic inventory stock deduction rules on bill creation.
   * PDF document layout, rendering, and number-to-words rules.
   * Input validation criteria and demo data seeding strategy.

---

## ⚡ Core Project Rules for AI Agents & Developers

> [!IMPORTANT]
> When maintaining or extending HisabBook, always follow these project conventions:

1. **Offline-First Guarantee**:
   * Do not introduce mandatory cloud/network calls. All data must persist locally in Room SQLite DB.
   * All screens must work flawlessly offline.

2. **Navigation 3 Rule**:
   * **Never** use legacy `NavController` or `NavHost` (Nav 2.x).
   * Always define new routes as `@Serializable` objects/classes implementing `HisabBookNavKey` in [`HisabBookNavKey.kt`](file:///Users/danishkhan/AndroidStudioProjects/HisabBook/app/src/main/java/com/example/hisabbook/ui/navigation/HisabBookNavKey.kt).
   * Manage navigation stack via `rememberNavBackStack()` and `NavDisplay`.

3. **Material Design 3 & Edge-to-Edge**:
   * Always maintain edge-to-edge support with Compose `Scaffold` handling `WindowInsets`.
   * Prefer Material 3 Expressive UI components and vector icons (`Icons.Rounded`, `Icons.AutoMirrored.Rounded`).

4. **ViewModel Instantiation**:
   * All ViewModels must be instantiated using `HisabBookViewModelFactory` in [`HisabBookNavigation.kt`](file:///Users/danishkhan/AndroidStudioProjects/HisabBook/app/src/main/java/com/example/hisabbook/ui/navigation/HisabBookNavigation.kt).

5. **Data Consistency & Validation**:
   * Customer mobile numbers must be validated as 10 digits when provided.
   * Stock quantities cannot drop below zero during manual Stock-Out transactions.
   * Payments against bills must automatically trigger invoice status recalculations (`UNPAID` -> `PARTIALLY_PAID` -> `PAID`).
