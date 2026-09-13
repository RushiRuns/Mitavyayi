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
  - `getCurrentMonthYear(): String` (yyyy-MM)
  - `formatMonthYear(monthYear: String): String` (e.g. "September 2026")
  - `getMonthStartAndEndTimestamps(monthYear: String): Pair<Long, Long>` (epoch millis range)
  - `getAdjacentMonthYear(monthYear: String, offsetMonths: Int): String`
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
- **Motion & Animation Tokens** (`com.rushi.mitavyay.ui.theme.AppMotion`):
  - Centralized motion tokens: `durationFast` (150ms), `durationNormal` (300ms), `durationSlow` (500ms).
  - Standardized easing curves (`standard`, `emphasizedDecelerate`, `emphasizedAccelerate`, `linear`) and specs (`fastTween`, `normalTween`, `slowTween`, `bouncySpring`).
  - Screen transition generators (`screenEnterTransition`, `screenExitTransition`, `screenPopEnterTransition`, `screenPopExitTransition`, `tabCrossfadeEnter`, `tabCrossfadeExit`).
  - Access via `MaterialTheme.appMotion`.
  - *Location*: `ui/theme/Motion.kt`
- **Haptic Feedback Utilities** (`com.rushi.mitavyay.util.HapticFeedbackHelper`):
  - `fun Context.hapticLight(enabled: Boolean = true)`: Light tactile tap for chips, toggles, icon buttons.
  - `fun Context.hapticMedium(enabled: Boolean = true)`: Medium tactile click for button presses and navigation items.
  - `fun Context.hapticHeavy(enabled: Boolean = true)`: Heavy tactile feedback for primary/prominent actions.
  - `fun Context.hapticSuccess(enabled: Boolean = true)`: Double-pulse tactile confirmation pattern for save/creation actions.
  - `fun Context.hapticError(enabled: Boolean = true)`: Multi-pulse tactile pattern for validation failures, rejected actions, and destructive alerts.
  - Honors Android API level capabilities (API 31+ `VibratorManager`, API 26+ `VibrationEffect`) with complete exception suppression and safe degradation.
  - Controlled by user preference toggle in DataStore (`haptic_feedback_enabled`).
  - *Location*: `util/HapticFeedbackHelper.kt`

## Reusable UI Components (`com.rushi.mitavyay.ui.components`)

### Buttons (`ui/components/Buttons.kt`)
- `PrimaryButton`: Main CTA button with primary brand color, loading spinner, theme shape, and tactile bouncy press-scale micro-interaction (`pressScale`).
- `SecondaryButton`: Outlined button for secondary actions with `pressScale`.
- `TertiaryButton`: Text button for low-emphasis actions with `pressScale`.
- `DangerButton`: Destructive button with error color for deletions and destructive confirmations with `pressScale`.

### Animation Modifiers & Effects (`ui/components/AnimationModifiers.kt`, `ui/components/DelightfulAnimations.kt`, `ui/components/SkeletonLoader.kt`, `ui/components/IllustrationAssets.kt`)
- `Modifier.pressScale(pressedScale: Float = 0.96f)`: Reusable modifier animating an interactive bouncy scale reduction on touch press with spring release.
- `Modifier.bounceClickable(onClick: () -> Unit)`: Combines clickable with tactile spring bounce feedback.
- `CelebrationEffect`: Offline Compose Canvas confetti particle explosion for financial milestones and goal achievements.
- `OfflineLottieAnimation`: Reusable component for playing offline local Lottie JSON vector animations from raw resources.
- `SkeletonLoader` & `shimmerBrush`:
  - `shimmerBrush()`: Linear gradient transition across theme surface variant tokens.
  - `Modifier.shimmerPlaceholder(visible, shape)`: Applies shimmer brush placeholder to any component.
  - `SkeletonTransactionCard`, `SkeletonTransactionList(count)`: Mimics transaction card layout during loading.
  - `SkeletonCard(height)`: Generic card placeholder.
  - *Location*: `ui/components/SkeletonLoader.kt`
- `IllustrationAssets`:
  - Modern offline Compose Canvas vector illustrations for empty and state feedback:
    - `EmptyTransactionsIllustration`: Stylized wallet with floating coins.
    - `EmptySearchIllustration`: Stylized document with magnifying glass and search lines.
    - `EmptyBudgetIllustration`: Category target ring and balance scale.
    - `EmptyGoalsIllustration`: Flag platform milestone with stars.
  - *Location*: `ui/components/IllustrationAssets.kt`

### Pull-to-Refresh & Containers (`ui/components/PullToRefresh.kt`)
- `PullToRefreshBox`: Standard Material 3 pull-to-refresh container wrapper managing `PullToRefreshState`, nested scrolling, and haptic feedback with accent green primary spinner on neutral container surface.

### Cards (`ui/components/Cards.kt`)
- `TransactionCard`: Card displaying title, category, formatted amount (green for income, red for expenses), formatted date, and "Transfer" badge for paired transfer records.
- `AccountCard`: Card displaying account name, type chip (Cash/Bank/Credit), formatted balance, and active status.
- `GoalCard`: Card displaying goal progress bar, saved vs target amount, deadline, timeline text, status badge, linked account badge vs dedicated fund badge, and deposit/delete actions.
- `DebtCard`: Card displaying counterparty name, formatted amount (green for lent, red for borrowed), type chip, creation/settlement dates, notes, and "Mark Settled" CTA for active debts (strictly immutable, never exposes delete per ADR-007).
- `RepeatExpenseCard`: Card displaying description, formatted amount, frequency chip, category, last generated date, active toggle switch, and delete button.
- `BudgetCard` (`ui/screens/Budget/BudgetCards.kt`): Card displaying category name, category color indicator, actual spent vs budgeted amount, linear progress bar, % consumed, remaining/exceeded badge, warning indicator (80% / 100%), and edit/delete actions.
- `BudgetOverviewCard` (`ui/screens/Budget/BudgetCards.kt`): Summary card showing total budgeted, total actual spent, net remaining/exceeded, and overall budget health status.
- `BudgetVsActualChart` (`ui/screens/Budget/BudgetCards.kt`): Visual comparison chart with side-by-side comparative bars per category for budget vs actual spending.

### Inputs & Date Selection (`ui/components/Inputs.kt`, `CurrencyInput.kt`, `DatePicker.kt`)
- `AppTextField`: Outlined text field wrapper with theme token colors, shapes, and error state validation feedback.
- `CurrencyInput`: Dedicated monetary input with currency prefix, sanitized numeric entry, and direct `Long` paise conversion.
- `DatePickerField`, `AppDatePickerDialog`, & `AppDateRangePickerDialog`: Date and date-range selection components launching Material 3 DatePickerDialog / DateRangePickerDialog and emitting Unix timestamp ms (`Long`).
- `DateFilter` (`com.rushi.mitavyay.data.model.DateFilter`):
  - Sealed class representing chronological transaction filtering: `AllTime`, `Today`, `ThisWeek` (Mon–Sun), `ThisMonth`, and `CustomRange(startDateMs, endDateMs)`.
  - Includes `fun DateFilter.matches(timestamp: Long): Boolean` evaluation engine.
  - *Location*: `data/model/DateFilter.kt`

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
- `BatchAddTransactionsDialog`: Full-screen dialog for logging multiple transactions at once with repeatable rows, dynamic add/delete row controls, running expense/income summary banner, single-tap default account setting, and atomic multi-row commit.
- `EditTransactionDialog`: Form dialog for editing transaction amount, expense/income type, category, account, and description.
- `SetBudgetDialog` (`ui/screens/Budget/SetBudgetDialog.kt`): Form dialog for setting or updating monthly category budget with category picker chips, color swatches, CurrencyInput, and active toggle.
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

### Navigation & Scaffold (`ui/navigation/`, `ui/components/`, `ui/`, `ui/screens/Hub/`)
- `NavDestination` (`ui/navigation/NavDestinations.kt`): Sealed hierarchy defining routes (`transactions`, `analysis`, `more`, `accounts`, `categories`, `import_export`, `debts`, `repeat_expenses`, `goals`, `budgets`, `insights`, and `transaction_detail/{transactionId}`), top-level destinations (Transactions, Analysis, More), tab labels, and icons.
- `MitavyayNavHost` (`ui/navigation/NavHost.kt`): Top-level NavHost mapping routes to Compose screens without Fragments, including the 'More' HubScreen.
- `MitavyayAppState` & `rememberMitavyayAppState` (`ui/AppState.kt`): State holder for navigation controller, backstack resolution, and tab switching.
- `MitavyayBottomBar` (`ui/components/BottomNavBar.kt`): Material 3 navigation bar utilizing theme tokens with Transactions, Analysis, and More tabs.
- `HubScreen` & `HubMenuItem` (`ui/screens/Hub/HubScreen.kt`): Centralized hub menu screen presenting accessible card items for Accounts, Debt & Loans, Insights, Recurring Expenses, Saving Goals, Budget Planning, Categories, and Settings.
- `MitavyayApp` (`ui/MitavyayApp.kt`): Root application scaffold orchestrating uncluttered TopAppBar, BottomNavBar with More tab, FAB, and NavHost.
- `DebtListScreen` (`ui/screens/Debt/DebtListScreen.kt`): Screen managing active and settled debts with summary metrics (Total Lent, Total Borrowed), tab switching, settlement flow, and zero delete capability per ADR-007.
- `RepeatExpenseListScreen` (`ui/screens/RepeatExpense/RepeatExpenseListScreen.kt`): Screen managing recurring expenses with monthly commitment readout, All/Active/Paused tabs, on-demand due evaluation, and active toggle switch.
- `GoalsListScreen` (`ui/screens/Goals/GoalsListScreen.kt`): Screen managing savings targets with summary metrics (Total Target, Total Saved, Overall Progress, active/achieved counts), tab switching (All, In Progress, Achieved, Overdue), and savings deposits.
- `BudgetListScreen` (`ui/screens/Budget/BudgetListScreen.kt`): Screen managing monthly category budgets with month navigation (<, >, Today), threshold warning banner (80% / 100%), overall health summary card, budget vs actual comparative bar chart, copy from previous month action, and category budget item cards.
- `InsightsScreen` (`ui/screens/Insights/InsightsScreen.kt`): Simple, high-value financial statistics dashboard providing key metrics for any month (total spent with previous month comparison badge, daily burn rate across elapsed days, largest transaction of the month with detail navigation, most used category by frequency and volume, and monthly cash flow summary). Powered by repository-level reactive aggregations.
- `BasicInsightsData` & `BasicInsightsCalculator` (`data/repository/TransactionRepository.kt`): Data model and pure calculation engine for monthly basic insights, maintaining offline-first execution and `Long` paise financial precision.
- `SettingsScreen` & `SettingsViewModel` (`ui/screens/Settings/SettingsScreen.kt`, `SettingsViewModel.kt`): Full-page settings screen organizing Appearance (System/Light/Dark theme selector), Data Management (navigation link to Backup & Data CSV Import/Export), Preferences (tactile haptic feedback toggle), and About information.
- `ImportExportScreen` (`ui/screens/Settings/ImportExportScreen.kt`): Screen managing transaction export, SAF document creation, system sharing, and all-or-nothing CSV import.


## DO NOT CREATE
- Do not create a second currency formatter. Use `CurrencyFormatter.format`.
- Do not create ad-hoc date formatting in UI components. Use `DateTimeFormatter.formatDate`.
- Do not create custom spacer components per screen. Use `ui/components/Spacing.kt` or `MaterialTheme.spacing`.
- Do not create duplicate cards or buttons inside screen folders.
