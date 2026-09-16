# HisabBook Logo Guide

This app already uses a ledger/invoice style logo, which fits the product well. A stronger final logo should stay simple enough to read at launcher-icon size and should communicate bookkeeping, invoices, trust, and payments.

## Recommended Logo Direction

Use a clean **ledger book + invoice + H monogram** mark.

- Primary symbol: a simple ledger/book shape.
- Secondary symbol: invoice lines or a check mark.
- Letter mark: a bold `H`, not the full app name.
- Colors: deep slate, strong blue, emerald green, and a small gold accent.
- Avoid tiny text, many line items, thin strokes, and detailed rupee symbols in the launcher icon because they become unclear on small screens.

Suggested palette:

| Purpose | Color |
| --- | --- |
| Background | `#0F172A` |
| Primary blue | `#2563EB` |
| Success green | `#10B981` |
| Accent gold | `#F59E0B` |
| Paper white | `#F8FAFC` |

## Current Logo Files

Launcher icon files:

- `app/src/main/res/mipmap-anydpi/ic_launcher.xml`
- `app/src/main/res/mipmap-anydpi/ic_launcher_round.xml`
- `app/src/main/res/drawable/ic_launcher_background.xml`
- `app/src/main/res/drawable/ic_launcher_foreground.xml`

In-app logo files:

- `app/src/main/res/drawable/ic_branding_logo.xml`
- `app/src/main/res/drawable/ic_logo.xml`

Current app references:

- `app/src/main/AndroidManifest.xml` uses `@mipmap/ic_launcher` and `@mipmap/ic_launcher_round`.
- `DashboardScreen.kt` and `SettingsScreen.kt` use `@drawable/ic_branding_logo`.
- `SettingsScreen.kt` also uses `@drawable/ic_logo`.

## Best Way To Change The App Icon

Use Android Studio's Image Asset tool because it generates the required launcher icon resources correctly.

1. Prepare a square source logo.
   - Best source format: SVG.
   - Good fallback: 1024 x 1024 PNG with transparent background.
   - Keep the main symbol centered with enough padding.

2. Open Android Studio.

3. Right-click `app/src/main/res`.

4. Select **New > Image Asset**.

5. Set **Icon Type** to **Launcher Icons (Adaptive and Legacy)**.

6. Set **Name** to:

   ```text
   ic_launcher
   ```

7. For **Foreground Layer**, choose the new SVG or PNG logo.

8. For **Background Layer**, use a solid color or simple gradient-style asset.
   - Recommended background color: `#0F172A`.

9. Check the previews for:
   - Circle
   - Squircle
   - Rounded square
   - Full square

10. Click **Next**, then **Finish**.

11. Confirm these references still exist in `AndroidManifest.xml`:

   ```xml
   android:icon="@mipmap/ic_launcher"
   android:roundIcon="@mipmap/ic_launcher_round"
   ```

## How To Change The In-App Logo

The launcher icon and in-app logo are separate. After changing the launcher icon, also update these drawables so the dashboard/settings branding matches.

1. Replace:

   ```text
   app/src/main/res/drawable/ic_branding_logo.xml
   ```

   Use this for small toolbar/header logo places.

2. Replace:

   ```text
   app/src/main/res/drawable/ic_logo.xml
   ```

   Use this for larger branding surfaces.

3. Keep both files visually consistent with `ic_launcher_foreground.xml`.

4. Use vector drawable XML if possible. It scales cleanly and keeps the APK small.

## Manual Vector Replacement Option

If you already have SVG artwork:

1. In Android Studio, open **File > New > Vector Asset**.

2. Choose **Local file (SVG, PSD)**.

3. Import the SVG.

4. Name it based on where it will be used:

   ```text
   ic_branding_logo
   ic_logo
   ic_launcher_foreground
   ```

5. Review the generated vector XML.

6. Remove unnecessary tiny paths if the logo looks too detailed.

7. Build the app and check the icon on a real device or emulator.

## Verification Steps

Run a build:

```bash
./gradlew assembleDebug
```

Install and verify:

```bash
./gradlew installDebug
```

Check these places:

- Home screen launcher icon.
- Recent apps screen icon.
- Dashboard logo.
- Settings logo.
- Light and dark system themes.
- Small icon size in Android launcher search.

## Design Checklist

Before finalizing, make sure:

- The logo is recognizable at 48 x 48 px.
- The icon does not depend on readable text.
- The logo has enough padding inside adaptive icon masks.
- The foreground does not touch the icon edges.
- The in-app logo and launcher icon feel like the same brand.
- The colors still match the app's bookkeeping/business feel.

## Suggested Final Logo Prompt

Use this prompt with a designer or image/logo generator:

```text
Create a modern Android app logo for "HisabBook", a bookkeeping and invoice app for small businesses. Use a simple ledger book shape with a bold H monogram, subtle invoice lines, and a small check mark to suggest completed accounts. Style should be clean, trustworthy, professional, and readable at small launcher icon size. Use deep slate background, vivid blue main shape, emerald green success accent, and a small gold bookkeeping accent. No tiny text, no complex details, no realistic 3D mockup.
```

