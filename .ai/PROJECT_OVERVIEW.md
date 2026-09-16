# Project Overview - HisabBook

This document details the business domain, product vision, offline-first philosophy, and localization strategy for **HisabBook**.

---

## 🏢 Business Domain: IMAGE ALUMINIUM AND GLASS

**HisabBook** is built with specialized defaults for fabricators, glass contractors, and hardware dealers, operating under the default profile of **IMAGE ALUMINIUM AND GLASS**.

### Business Profile Details
* **Business Name**: `IMAGE ALUMINIUM AND GLASS`
* **Subtitle**: `Fabrication, Sliding Windows & Toughened Glass Works`
* **Description**: `High Quality Aluminium Fabrication, Glass Fittings & Hardware`
* **Primary Contact Phone**: `8999127482` / `9876543210`
* **GST Identification**: Configurable GSTIN field for tax compliance.

### Specific Inventory & Trade Requirements
Aluminium and glass fabrication businesses operate with unique material units and stock items:
* **Units Supported**: `Pcs` (Pieces), `Kg` / `Kgs` (Kilograms), `sq.ft` (Square Feet), `Meter` / `mtr` (Meters), `Box`, `Ltr`.
* **Preset Hardware & Stock Catalog**:
  * **HD- ALUMINIUM** (KGS) - High Density Aluminium Extrusions.
  * **TOUGHENED GLASS 12MM** (sq.ft) - Architectural Safety Glass.
  * **SLIDING WINDOW FRAME** (meter) - Aluminium Window Tracks & Profiles.
  * **CLEAR PVC RUBBER** (KGS) - Window Gaskets & Weather Strips.
  * **CUBEX STEEL BEARING** (PCS) - Roller Bearings for Sliding Systems.
  * **LOCK 44 GOLD** (PCS) - Window & Door Locks.
  * **SILCON-NEXO** (PCS) - Weatherproof Silicone Sealants.
  * **SS STEEL KIT** (PCS) - Stainless Steel Railing & Glass Fittings.
  * **BRUSH RUBBER** (PCS) - Weather Stripping Brush Seals.
  * **PINK BERING** (PCS) - Sliding Rollers.
  * **UNI-BOND SHEET** (PCS) - Aluminium Composite Panels (ACP).

---

## 🎯 Product Vision & Core Purpose

Small scale fabricators and traders in India traditionally rely on paper notebooks ("Khata Books") or physical receipt books. This leads to common challenges:
* Lost record books and forgotten customer outstanding dues.
* Errors in manually calculating totals, discounts, and partial payment balances.
* Delay in issuing formal quotes or invoices to prospective clients.
* Difficulty tracking stock levels of high-value raw materials (aluminium channels, toughened glass).

**HisabBook solves these challenges** by providing a lightweight, lightning-fast Android mobile application that functions as:
1. **Digital Customer Ledger**: Tracks every invoice, payment, and outstanding balance per customer.
2. **Instant Quotation Creator**: Allows business owners to draft formal quotes on-site and convert them into bills with a single tap.
3. **Bill & Tax Invoice Generator**: Generates professional PDF invoices complete with business branding, GST details, item breakdown, and amount in words.
4. **Payment Tracker**: Logs payments received via Cash, UPI, Bank Transfer, or Cheque with immediate balance recalculation.
5. **Stock & Inventory Monitor**: Automatically deducts inventory stock when items are billed and flags low-stock warnings.

---

## 📵 Offline-First Philosophy

HisabBook is engineered around a **100% Offline-First Core**:

1. **Zero External Dependencies**:
   * No backend servers, cloud databases, or API keys are required for operation.
   * All database read and write operations execute directly on the local Android device using Room SQLite.

2. **Uninterrupted On-Site Usage**:
   * Fabricators often work in basements, construction sites, or remote locations without cellular reception. HisabBook ensures 100% functionality regardless of network connectivity.

3. **Data Ownership & Privacy**:
   * Customer records, sales figures, and financial data never leave the user's phone.
   * Full data sovereignty is guaranteed through local JSON Backup & Restore capabilities in [`BackupManager.kt`](file:///Users/danishkhan/AndroidStudioProjects/HisabBook/app/src/main/java/com/example/hisabbook/data/local/BackupManager.kt).

---

## 🌐 Localization & Regionalization

To cater to business owners across India, HisabBook supports multi-language operation and regional financial standards:

### 1. Dual Language Support (English & Hindi)
* **Supported Locales**:
  * English (`en`) - Default system language.
  * Hindi (`hi` / हिन्दी) - Regional language option.
* **Locale Manager**:
  * [`LocaleHelper.kt`](file:///Users/danishkhan/AndroidStudioProjects/HisabBook/app/src/main/java/com/example/hisabbook/util/LocaleHelper.kt) integrates with Android's system `LocaleManager` (`applicationLocales`) for instant runtime language switching without requiring an app restart.

### 2. Currency & Number Formatting
* **Currency Symbol**: Indian Rupee (`₹`) by default, configurable in Business Profile settings.
* **Indian Number Format**: Uses `Locale("en", "IN")` formatting in [`CurrencyFormatter.kt`](file:///Users/danishkhan/AndroidStudioProjects/HisabBook/app/src/main/java/com/example/hisabbook/util/CurrencyFormatter.kt) for commas and decimal representation (e.g., `₹ 1,50,000.00`).
* **Amount in Words**: [`NumberToWords.kt`](file:///Users/danishkhan/AndroidStudioProjects/HisabBook/app/src/main/java/com/example/hisabbook/util/NumberToWords.kt) automatically converts decimal totals into formal Indian currency words (e.g., `Rupees Forty-Eight Thousand Only` or `Rupees Sixteen Thousand Six Hundred Thirty-Two and Fifty-Five Paise Only`).
