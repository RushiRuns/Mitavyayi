# Shared Utilities & Components

This file serves as the index for all shared utilities, formatters, extensions, and reusable UI components. 

> **DO NOT CREATE** duplicate utilities. Check this list before writing new shared logic.

## Utilities & Formatters

- **Monetary Formatter** (`com.rushi.mitavyay.util.CurrencyFormatter`):
  - `format(amountPaise: Long, currencySymbol: String = "₹"): String`: Formats monetary amounts using Indian numbering system (Lakhs & Crores) into display strings without floating point math.
  - `parseToPaise(input: String): Long`: Parses user numeric input into smallest currency unit (paise: Long) without floating point math.
  - *Location*: `util/Formatters.kt`
- **Date/Time Formatter** (`com.rushi.mitavyay.util.DateTimeFormatter`):
  - `formatDate(timestampMs: Long, pattern: String = "dd MMM yyyy"): String`
  - `formatDateTime(timestampMs: Long, pattern: String = "dd MMM yyyy, hh:mm a"): String`
  - `formatShortDate(timestampMs: Long): String` (dd/MM/yyyy)
  - *Location*: `util/Formatters.kt`
- **Database Transaction Runner** (`com.rushi.mitavyay.data.db.DatabaseTransactionRunner`):
  - `withTransaction<R>(block: suspend () -> R): R`: Executes operations atomically inside a database transaction.
  - *Location*: `data/db/DatabaseTransactionRunner.kt`
- **Preferences & DataStore** (`com.rushi.mitavyay.data.datastore.PreferencesRepository`):
  - Theme, font scale, currency, language, and app opening count persistence.
  - *Location*: `data/datastore/PreferencesRepository.kt` & `PreferenceKeys.kt`
- **UI Display Model Mappers** (`com.rushi.mitavyay.data.model.*`):
  - `toDisplayItem()` extension functions for `Transaction`, `Account`, `Goal`, `Debt`, `RepeatExpense`, and `Category`.
  - Ensures entities are never exposed directly to UI components.
  - *Location*: `data/model/*DisplayItem.kt`
- **Currency Conversion Utilities**: Helper functions for currency processing (if applicable, entirely offline). *Status: Pending*
- **Transaction Categorization Logic**: Centralized logic for determining default or custom categories. *Status: Pending*

## Reusable UI Components (`com.rushi.mitavyay.ui.components`)

### Buttons (`ui/components/Buttons.kt`)
- `PrimaryButton`: Main CTA button with primary brand color, loading spinner, and theme shape.
- `SecondaryButton`: Outlined button for secondary actions.
- `TertiaryButton`: Text button for low-emphasis actions.
- `DangerButton`: Destructive button with error color for deletions and destructive confirmations.

### Cards (`ui/components/Cards.kt`)
- `TransactionCard`: Card displaying title, category, formatted amount (green for income, red for expenses), and formatted date.
- `AccountCard`: Card displaying account name, type chip (Cash/Bank/Credit), formatted balance, and active status.
- `GoalCard`: Card displaying goal progress bar, saved vs target amount, deadline, and percentage.

### Inputs & Date Selection (`ui/components/Inputs.kt`, `CurrencyInput.kt`, `DatePicker.kt`)
- `AppTextField`: Outlined text field wrapper with theme token colors, shapes, and error state validation feedback.
- `CurrencyInput`: Dedicated monetary input with currency prefix, sanitized numeric entry, and direct `Long` paise conversion.
- `DatePickerField` & `AppDatePickerDialog`: Date selection field launching a Material 3 DatePickerDialog and emitting Unix timestamp ms (`Long`).

### Dialogs & Bottom Sheets (`ui/components/Dialogs.kt`, `ui/screens/Accounts/AddAccountDialog.kt`, `ui/screens/QuickAddExpense/QuickAddExpenseSheet.kt`, `ui/screens/TransactionDetail/EditTransactionDialog.kt`)
- `AppAlertDialog`: Standardized confirmation and alert dialog with theme typography, colors, and confirm/dismiss actions.
- `AddAccountDialog`: Form dialog for creating and editing accounts with validated name, type selection, and initial balance input.
- `QuickAddExpenseSheet`: Material 3 ModalBottomSheet for rapid transaction entry with CurrencyInput, Expense/Income toggle, single-tap account/category chips, and atomic balance syncing.
- `EditTransactionDialog`: Form dialog for editing transaction amount, expense/income type, category, account, and description.

### Layout & Spacers (`ui/components/Spacing.kt`)
- `VerticalSpacer(height: Dp)` / `HorizontalSpacer(width: Dp)`
- Pre-bound spacers: `SpacerXs`, `SpacerSm`, `SpacerMd`, `SpacerLg`, `SpacerXl`
- Horizontal pre-bound spacers: `HSpacerXs`, `HSpacerSm`, `HSpacerMd`, `HSpacerLg`, `HSpacerXl`

### States (`ui/components/States.kt`)
- `LoadingState`: Centered spinner with theme primary color and message.
- `ErrorState`: Centered error icon, message, and retry button.
- `EmptyState`: Centered empty placeholder icon, title, description, and optional action CTA.

### Navigation & Scaffold (`ui/navigation/`, `ui/components/`, `ui/`)
- `NavDestination` (`ui/navigation/NavDestinations.kt`): Sealed hierarchy defining routes (`transactions`, `analysis`, `accounts`, and `transaction_detail/{transactionId}`), tab labels, and icons.
- `MitavyayNavHost` (`ui/navigation/NavHost.kt`): Top-level NavHost mapping routes to Compose screens without Fragments.
- `MitavyayAppState` & `rememberMitavyayAppState` (`ui/AppState.kt`): State holder for navigation controller, backstack resolution, and tab switching.
- `MitavyayBottomBar` (`ui/components/BottomNavBar.kt`): Material 3 navigation bar utilizing theme tokens.
- `MitavyayApp` (`ui/MitavyayApp.kt`): Root application scaffold orchestrating TopAppBar (with back button on detail destinations), BottomNavBar, FAB, and NavHost.


## DO NOT CREATE
- Do not create a second currency formatter. Use `CurrencyFormatter.format`.
- Do not create ad-hoc date formatting in UI components. Use `DateTimeFormatter.formatDate`.
- Do not create custom spacer components per screen. Use `ui/components/Spacing.kt` or `MaterialTheme.spacing`.
- Do not create duplicate cards or buttons inside screen folders.
