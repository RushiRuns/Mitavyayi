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
  - Includes transfer flags (`transferId`, `isTransfer`) on `TransactionDisplayItem`.
  - *Location*: `data/model/*DisplayItem.kt`
- **Category Icon & Color Utilities** (`com.rushi.mitavyay.ui.components`):
  - `getCategoryIcon(iconName: String): ImageVector`: Maps icon identifiers to safe `material-icons-core` vectors.
  - `CategoryColorOptions`: Curated palette of 16 theme-harmonized hex colors for custom category creation.
  - `CategoryIconOptions`: List of 13 supported category icons with human-readable labels.
  - *Location*: `ui/components/CategoryUtils.kt`
- **Currency Conversion Utilities**: Helper functions for currency processing (if applicable, entirely offline). *Status: Pending*
- **Transaction Categorization Logic**: Centralized logic for determining default or custom categories. *Status: Complete via CategoryRepository & DatabaseModule pre-population*
- **CSV Import & Export Engine** (`com.rushi.mitavyay.data.repository.CsvRepository`):
  - OpenCSV-powered streaming CSV reader and writer.
  - Implements ADR-006 strict All-or-Nothing pre-validation, atomic Room transaction commit/rollback, dynamic column mapping, and automatic account reconciliation.
  - *Location*: `data/repository/CsvRepository.kt`

## Reusable UI Components (`com.rushi.mitavyay.ui.components`)

### Buttons (`ui/components/Buttons.kt`)
- `PrimaryButton`: Main CTA button with primary brand color, loading spinner, and theme shape.
- `SecondaryButton`: Outlined button for secondary actions.
- `TertiaryButton`: Text button for low-emphasis actions.
- `DangerButton`: Destructive button with error color for deletions and destructive confirmations.

### Cards (`ui/components/Cards.kt`)
- `TransactionCard`: Card displaying title, category, formatted amount (green for income, red for expenses), formatted date, and "Transfer" badge for paired transfer records.
- `AccountCard`: Card displaying account name, type chip (Cash/Bank/Credit), formatted balance, and active status.
- `GoalCard`: Card displaying goal progress bar, saved vs target amount, deadline, timeline text, status badge, linked account badge vs dedicated fund badge, and deposit/delete actions.
- `DebtCard`: Card displaying counterparty name, formatted amount (green for lent, red for borrowed), type chip, creation/settlement dates, notes, and "Mark Settled" CTA for active debts (strictly immutable, never exposes delete per ADR-007).
- `RepeatExpenseCard`: Card displaying description, formatted amount, frequency chip, category, last generated date, active toggle switch, and delete button.

### Inputs & Date Selection (`ui/components/Inputs.kt`, `CurrencyInput.kt`, `DatePicker.kt`, `NotesField.kt`)
- `AppTextField`: Outlined text field wrapper with theme token colors, shapes, and error state validation feedback.
- `CurrencyInput`: Dedicated monetary input with currency prefix, sanitized numeric entry, and direct `Long` paise conversion.
- `DatePickerField` & `AppDatePickerDialog`: Date selection field launching a Material 3 DatePickerDialog and emitting Unix timestamp ms (`Long`).
- `NotesField`: Rich multi-line notes editor with character counter, clear button, itemized bullet list helper, date stamp helper, and financial quick tag chips (`#tax`, `#reimbursable`, `#split`, `#warranty`, `#bill`).

### Dialogs & Bottom Sheets (`ui/components/Dialogs.kt`, `ui/screens/Accounts/AddAccountDialog.kt`, `ui/screens/Transfer/TransferDialog.kt`, `ui/screens/QuickAddExpense/QuickAddExpenseSheet.kt`, `ui/screens/BatchAdd/BatchAddTransactionsDialog.kt`, `ui/screens/TransactionDetail/EditTransactionDialog.kt`, `ui/components/ThemeSelectionDialog.kt`, `ui/components/AddCategoryDialog.kt`, `ui/screens/Debt/AddDebtDialog.kt`, `ui/screens/RepeatExpense/AddRepeatDialog.kt`, `ui/screens/Goals/AddGoalDialog.kt`, `ui/screens/Goals/AddGoalSavingsDialog.kt`)
- `AppAlertDialog`: Standardized confirmation and alert dialog with theme typography, colors, and confirm/dismiss actions.
- `AddAccountDialog`: Form dialog for creating and editing accounts with validated name, type selection, and initial balance input.
- `TransferDialog`: Dialog allowing fund transfers between two distinct active accounts with currency input, swap button, balance preview, notes, validation, and atomic ledger creation.
- `AddCategoryDialog`: Form dialog for creating and editing custom categories with live preview, color swatch picker, and icon grid.
- `AddDebtDialog`: Form dialog for recording lent and borrowed debts with type selector, counterparty name input, CurrencyInput, notes, and validation.
- `AddRepeatDialog`: Form dialog for creating recurring expenses with description, CurrencyInput amount, frequency picker, category chips, and active toggle switch.
- `AddGoalDialog`: Form dialog for creating savings goals towards targets and deadlines, supporting dedicated fund or linked account modes, initial deposit, category, and notes.
- `AddGoalSavingsDialog`: Form dialog for contributing savings deposits to dedicated fund goals with progress preview and CurrencyInput.
- `QuickAddExpenseSheet`: Material 3 ModalBottomSheet for rapid transaction entry with CurrencyInput, Expense/Income toggle, single-tap account/category chips, and atomic balance syncing.
- `BatchAddTransactionsDialog`: Full-screen dialog for logging multiple transactions at once with repeatable rows, dynamic add/delete row controls, running expense/income summary banner, single-tap default account setting, notes support, and atomic multi-row commit.
- `EditTransactionDialog`: Form dialog for editing transaction amount, expense/income type, category, account, description, and notes via NotesField.
- `EditNoteDialog`: Dedicated lightweight dialog in TransactionDetailScreen for editing transaction notes directly without reopening full transaction form.
- `ThemeSelectionDialog`: Dialog for selecting app theme (System default, Light mode, Dark mode) with DataStore persistence.

### Layout & Spacers (`ui/components/Spacing.kt`)
- `VerticalSpacer(height: Dp)` / `HorizontalSpacer(width: Dp)`
- Pre-bound spacers: `SpacerXs`, `SpacerSm`, `SpacerMd`, `SpacerLg`, `SpacerXl`
- Horizontal pre-bound spacers: `HSpacerXs`, `HSpacerSm`, `HSpacerMd`, `HSpacerLg`, `HSpacerXl`

### States (`ui/components/States.kt`)
- `LoadingState`: Centered spinner with theme primary color and message.
- `ErrorState`: Centered error icon, message, and retry button.
- `EmptyState`: Centered empty placeholder icon, title, description, and optional action CTA.

### Charts & Data Visualization (`ui/components/PieChart.kt`, `ui/components/AnalysisCharts.kt`, `ui/screens/Analysis/AnalysisScreen.kt`)
- `CategoryDonutChart`: Theme-aware animated Compose Canvas Donut/Pie chart displaying category spending distribution with centered total spent readout.
- `CategorySpendingLegendList`: Ranked category spending list with color indicators, transaction counts, percentage badges, and formatted amounts.
- `CategoryCompositionStackedBar`: Animated horizontal stacked segmented bar showing proportional category spending composition with percentage badges.
- `SpendingTrendCard`: Vico chart card supporting dynamic toggle between Line (`lineChart()`) and Bar (`columnChart()`) visualizations with formatted currency and date axes.
- `TrendForecastCard`: Directional spending indicator (increasing/decreasing vs prior period), daily burn rate (₹/day), and projected period-end forecast.
- `PeriodComparisonCard`: Side-by-side comparative analysis (Year-over-Year, Month-over-Month, Week-over-Week) with net delta badges and top category movers.
- `AccountBreakdownCard`: Multi-dimensional breakdown of spending across accounts and payment methods with percentage progress bars.
- `parseCategoryColor`: Pure Kotlin color hex parser with fallback palette for safe JVM unit testing and Android device rendering.

### Navigation & Scaffold (`ui/navigation/`, `ui/components/`, `ui/`)
- `NavDestination` (`ui/navigation/NavDestinations.kt`): Sealed hierarchy defining routes (`transactions`, `analysis`, `accounts`, `categories`, `import_export`, `debts`, `repeat_expenses`, `goals`, and `transaction_detail/{transactionId}`), tab labels, and icons.
- `MitavyayNavHost` (`ui/navigation/NavHost.kt`): Top-level NavHost mapping routes to Compose screens without Fragments.
- `MitavyayAppState` & `rememberMitavyayAppState` (`ui/AppState.kt`): State holder for navigation controller, backstack resolution, and tab switching.
- `MitavyayBottomBar` (`ui/components/BottomNavBar.kt`): Material 3 navigation bar utilizing theme tokens.
- `MitavyayApp` (`ui/MitavyayApp.kt`): Root application scaffold orchestrating TopAppBar (with Debts & Loans, Recurring Expenses, Savings Goals, Categories, Backup & CSV Data, and theme dialog), BottomNavBar, FAB, and NavHost.
- `DebtListScreen` (`ui/screens/Debt/DebtListScreen.kt`): Screen managing active and settled debts with summary metrics (Total Lent, Total Borrowed), tab switching, settlement flow, and zero delete capability per ADR-007.
- `RepeatExpenseListScreen` (`ui/screens/RepeatExpense/RepeatExpenseListScreen.kt`): Screen managing recurring expenses with monthly commitment readout, All/Active/Paused tabs, on-demand due evaluation, and active toggle switch.
- `GoalsListScreen` (`ui/screens/Goals/GoalsListScreen.kt`): Screen managing savings targets with summary metrics (Total Target, Total Saved, Overall Progress, active/achieved counts), tab switching (All, In Progress, Achieved, Overdue), and savings deposits.
- `ImportExportScreen` (`ui/screens/Settings/ImportExportScreen.kt`): Screen managing transaction export, SAF document creation, system sharing, and all-or-nothing CSV import.


## DO NOT CREATE
- Do not create a second currency formatter. Use `CurrencyFormatter.format`.
- Do not create ad-hoc date formatting in UI components. Use `DateTimeFormatter.formatDate`.
- Do not create custom spacer components per screen. Use `ui/components/Spacing.kt` or `MaterialTheme.spacing`.
- Do not create duplicate cards or buttons inside screen folders.
