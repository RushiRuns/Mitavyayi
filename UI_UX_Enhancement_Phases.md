# Mitavyay — UI/UX Enhancement Plan

> **Safety First:** Every change must preserve ALL existing functionality that is not explicitly being modified.
> Read `ARCHITECTURE.md` before each session. Update `CHANGELOG_INTERNAL.md` after each session.
> Each change is isolated and testable. No "fix one thing, break another."

---

## Pre-Work: Read Before Anything

- [ ] Read `ARCHITECTURE.md` fully — understand layer rules
- [ ] Read `SCHEMA.md` — understand all existing entities
- [ ] Read `UTILITIES.md` — know existing components to avoid duplication
- [ ] Read `CHANGELOG_INTERNAL.md` — know what the current state of the app is
- [ ] Read `DONE.md` — remember the definition of done for every task
- [ ] Read `Finance_App_Project_Phases.md` — understand completed phases

---

## CHANGE 1: Replace 'Accounts' Tab with 'More' Hub Screen

> **What this is:** The 'Accounts' tab in the bottom navigation becomes a hub screen (like "More" or "Menu") that gives users centralized access to multiple features. The top bar icons for these features are completely removed.
>
> **Reference:** See Image 3 (Tesorin app) for the visual style of a hub/menu tab.

### Affected Files

- `ui/screens/Accounts/AccountsScreen.kt`
- `ui/navigation/NavDestinations.kt`
- `ui/components/BottomNavBar.kt`
- `MainActivity.kt` (or wherever the top AppBar icons are defined)
- `ui/navigation/NavHost.kt`

### Tasks

**Schema / Navigation**
- [x] Add new nav destinations for all features being moved into the hub, if not already present:
  - `AccountsListScreen` (the actual accounts list — keep it as a sub-screen)
  - `DebtListScreen` ✓ (already exists at `ui/screens/Debt/DebtListScreen.kt`)
  - `InsightsScreen` ✓ (already exists at `ui/screens/Insights/InsightsScreen.kt`)
  - `RepeatExpenseListScreen` ✓ (already exists at `ui/screens/RepeatExpense/RepeatExpenseListScreen.kt`)
  - `GoalsListScreen` ✓ (already exists at `ui/screens/Goals/GoalsListScreen.kt`)
  - `BudgetListScreen` ✓ (already exists at `ui/screens/Budget/BudgetListScreen.kt`)
  - `CategoriesScreen` ✓ (already exists at `ui/screens/Categories/CategoriesScreen.kt`)
  - `SettingsScreen` (new — see Change 2)
- [x] Update `NavDestinations.kt` — rename `Accounts` destination to `More` (or `Hub`)
- [x] Update `NavHost.kt` — route `More` to the new hub screen composable

**New Hub Screen**
- [x] Create `ui/screens/Hub/HubScreen.kt`
  - Display a scrollable list of menu items (similar to Image 3 style)
  - Each menu item: icon + label + chevron arrow (→)
  - Menu items in order:
    1. 🏦 Accounts
    2. 💳 Debt & Loans
    3. 💡 Insights
    4. 🔁 Recurring Expenses
    5. 🎯 Saving Goals
    6. 📊 Budget Planning
    7. 🏷️ Categories
    8. ⚙️ Settings
  - Use `Card` or `Surface` with rounded corners for each item (theme tokens only)
  - No hardcoded colors — use `MaterialTheme.colorScheme.*`

**Bottom Navigation Bar**
- [x] In `ui/components/BottomNavBar.kt`:
  - Change the third tab label from `"Accounts"` to `"More"` (or a suitable name)
  - Change the third tab icon to a grid/menu icon (e.g., `Icons.Default.GridView` or `Icons.Default.Apps`)
  - The Transactions and Analysis tabs remain unchanged

**Top Bar Cleanup**
- [x] Remove all feature shortcut icons from the top `TopAppBar`:
  - Remove: Accounts icon
  - Remove: Debt/Loans icon
  - Remove: Goals/Wishlist icon
  - Remove: Budget/Cart icon
  - Remove: Recurring/Favorites icon
  - Remove: Share icon
  - Remove: Settings icon (it moves to the Hub)
  - Keep only: App title + Refresh button (refresh stays, see Change 9)
- [x] After removal, verify the top bar is clean and uncluttered

**Testing**
- [x] Test: Tap 'More' tab → hub screen opens
- [x] Test: Tap each menu item → correct screen opens
- [x] Test: Back navigation from sub-screen returns to hub
- [x] Test: All moved features still function exactly as before
- [x] Test: No orphaned nav routes

**Closure**
- [x] Update `UTILITIES.md` with HubScreen
- [x] Update `CHANGELOG_INTERNAL.md`
- [x] Commit: `"Change 1: Accounts tab replaced with More hub screen; top bar icons removed"`

---

## CHANGE 2: Settings as Full-Page Screen + Move 'Backup & Data'

> **What this is:** Settings becomes a standalone full-screen destination (not a dialog or sheet). 'Backup & Data' (currently in top bar) is moved inside Settings.

### Affected Files

- `ui/screens/Settings/` (currently only `ImportExportScreen.kt` exists here)
- `ui/screens/Hub/HubScreen.kt` (from Change 1)
- Top bar / wherever Backup & Data is currently triggered

### Tasks

- [x] Create `ui/screens/Settings/SettingsScreen.kt`
  - Full-page composable, not a dialog
  - TopAppBar with back arrow
  - Sections:
    - **Appearance:** Theme (light/dark toggle), already implemented
    - **Data:** Backup & Data → navigates to existing `ImportExportScreen.kt`
    - **Behavior:** Haptic feedback toggle, already implemented
    - **About:** App version info
  - Use `Scaffold` with `LazyColumn` for the settings list
  - Each setting item: label + current value/toggle
- [x] Create `ui/screens/Settings/SettingsViewModel.kt` (if needed for reading preferences)
- [x] Add `SettingsScreen` route to `NavDestinations.kt` and `NavHost.kt`
- [x] In `HubScreen.kt`, tapping "Settings" navigates to `SettingsScreen`
- [x] Remove Backup & Data shortcut from top bar (part of Change 1 top bar cleanup)
- [x] Verify `ImportExportScreen.kt` still works when navigated to from `SettingsScreen`

**Testing**
- [x] Test: Open Settings from Hub → full-page screen
- [x] Test: Tap "Backup & Data" inside Settings → ImportExportScreen opens
- [x] Test: Back from ImportExportScreen → returns to SettingsScreen
- [x] Test: All existing settings (theme toggle, haptic toggle) still work
- [x] Test: Settings preferences persist after app restart

**Closure**
- [x] Update `UTILITIES.md`
- [x] Update `CHANGELOG_INTERNAL.md`
- [x] Commit: `"Change 2: Settings full-page screen with Backup & Data moved inside"`

---

## CHANGE 3: Green → Accent Color; Grey-based UI for Light & Dark Themes

> **What this is:** The primary green color is demoted from "used everywhere" to a true accent — only used for CTAs, key highlights, income amounts. The rest of the UI uses neutral greys.
>
> **Color Decision:**
> - **Light theme surfaces:** Off-white (#F5F5F5 background, #FFFFFF cards, #EEEEEE dividers)
> - **Light theme text:** Near-black (#121212 primary, #757575 secondary)
> - **Dark theme surfaces:** Near-black (#121212 background, #1E1E1E cards, #2A2A2A elevated surfaces)
> - **Dark theme text:** Off-white (#E0E0E0 primary, #9E9E9E secondary)
> - **Accent (green):** `#4CAF50` — used ONLY for: FABs, primary action buttons, income amounts, progress indicators, selected tab indicator
> - **Expense amounts:** Keep red as-is (it communicates meaning)
> - **Income amounts:** Green (accent) — keep as-is

### Affected Files

- `ui/theme/Color.kt`
- `ui/theme/Theme.kt`
- Any hardcoded colors in: `QuickAddExpenseSheet.kt`, `BottomNavBar.kt`, `Cards.kt`, `Buttons.kt`, `AnalysisCharts.kt`, `TransactionListScreen.kt`, `AccountsScreen.kt`

### Tasks

**Color.kt**
- [x] Redefine the full color palette:
  ```kotlin
  // Accent
  val AccentGreen = Color(0xFF4CAF50)
  val AccentGreenDark = Color(0xFF388E3C)
  val AccentGreenContainer = Color(0xFFE8F5E9)

  // Light Theme Neutrals
  val LightBackground = Color(0xFFF5F5F5)
  val LightSurface = Color(0xFFFFFFFF)
  val LightSurfaceVariant = Color(0xFFEEEEEE)
  val LightOnSurface = Color(0xFF121212)
  val LightOnSurfaceSecondary = Color(0xFF757575)
  val LightDivider = Color(0xFFE0E0E0)

  // Dark Theme Neutrals
  val DarkBackground = Color(0xFF121212)
  val DarkSurface = Color(0xFF1E1E1E)
  val DarkSurfaceVariant = Color(0xFF2A2A2A)
  val DarkSurfaceElevated = Color(0xFF333333)
  val DarkOnSurface = Color(0xFFE0E0E0)
  val DarkOnSurfaceSecondary = Color(0xFF9E9E9E)
  val DarkDivider = Color(0xFF3A3A3A)

  // Semantic colors (keep as-is)
  val ExpenseRed = Color(0xFFE53935)
  val IncomeGreen = Color(0xFF4CAF50)  // = AccentGreen
  val WarningAmber = Color(0xFFFFA000)
  ```
- [x] Document the palette decision in `docs/decisions/ADR-011-Color-Palette-Refactor.md`

**Theme.kt**
- [x] Update `LightColorScheme` to use the new neutral palette
- [x] Update `DarkColorScheme` to use the new neutral palette
- [x] `primary` = `AccentGreen`, `onPrimary` = white
- [x] `background`, `surface`, `surfaceVariant` = appropriate grey tokens
- [x] All secondary surfaces use grey, not green tints

**Audit & Fix Hardcoded Colors**
- [x] Search codebase for `Color(0xFF` — fix every instance to use a theme token
- [x] Search for `MaterialTheme.colorScheme.primary` being used as a background where grey is more appropriate
- [x] `BottomNavBar.kt`: selected tab uses green accent; unselected uses grey — correct
- [x] `QuickAddExpenseSheet.kt`: "Expense" toggle red, "Income" toggle green — keep; background grey
- [x] `Cards.kt` (TransactionCard): background should be `surfaceVariant` (grey card), not green-tinted

**Testing**
- [x] Test: All screens in light mode — no green except accents
- [x] Test: All screens in dark mode — no green except accents
- [x] Test: WCAG AA contrast ratios pass (text on background)
- [x] Test: Income amounts still green, expense amounts still red
- [x] Test: FAB and primary buttons still green

**Closure**
- [x] Update `CHANGELOG_INTERNAL.md`
- [x] Commit: `"Change 3: Green demoted to accent; grey-based neutral theme for light/dark"`

---

## CHANGE 4: Remove Notes Feature Entirely

> **What this is:** The "Notes" field on transactions is removed completely. Users can already use "Description" for contextual text.
>
> ⚠️ **Data Safety:** Do NOT remove the `notes` column from the database schema or the `Transaction` entity. Removing a DB column requires a migration and risks data loss. Simply hide it from the UI. The column remains but is invisible to users.

### Affected Files

- `ui/screens/QuickAddExpense/QuickAddExpenseSheet.kt` — remove Notes input
- `ui/screens/TransactionDetail/EditTransactionDialog.kt` — remove Notes input
- `ui/screens/TransactionDetail/TransactionDetailScreen.kt` — hide Notes display
- `ui/components/NotesField.kt` — delete
- `ui/screens/BatchAdd/BatchAddTransactionsDialog.kt` — remove Notes column if present

### Tasks

- [x] In `QuickAddExpenseSheet.kt`: remove the `NotesField` composable and its state variable
  - When saving, pass `notes = null` (or empty string, per existing schema)
- [x] In `EditTransactionDialog.kt`: remove Notes text field
- [x] In `TransactionDetailScreen.kt`: remove the Notes display section
- [x] In `BatchAddTransactionsDialog.kt`: remove Notes column/field
- [x] Delete `ui/components/NotesField.kt` (after verifying no other file imports it)
  - Grep for `NotesField` across all `.kt` files to confirm zero remaining usages
- [x] Remove the "Bullet", "Today", "#tax", "#reimbursable" quick-insert chips if they are Notes-specific helpers (visible in Image 2)

**Testing**
- [x] Test: Quick add expense has no Notes field
- [x] Test: Transaction detail has no Notes section
- [x] Test: Edit transaction has no Notes field
- [x] Test: All existing transactions with notes still load (notes data not lost)
- [x] Test: App compiles with zero references to NotesField

**Closure**
- [x] Update `UTILITIES.md` — remove NotesField entry
- [x] Update `CHANGELOG_INTERNAL.md`
- [x] Commit: `"Change 4: Notes feature removed from UI (data column preserved)"`

---

## CHANGE 5: Add Date Picker in 'Quick Add Expense'

> **What this is:** When recording a transaction in the Quick Add sheet, the user can pick the date on which the transaction occurred (defaulting to today). The selected date also appears on the transaction card in the transactions list.

### Affected Files

- `ui/screens/QuickAddExpense/QuickAddExpenseSheet.kt`
- `ui/screens/QuickAddExpense/QuickAddExpenseViewModel.kt`
- `ui/components/Cards.kt` — confirm `TransactionCard` shows date from `transaction.timestamp`

### Tasks

- [x] Add a date field row in the form (between Account and Category sections)
  - Display: calendar icon + formatted date (e.g., "Today, 13 Sept 2026")
  - Tapping opens the date picker dialog
- [x] Reuse the existing `DatePicker.kt` component (`ui/components/DatePicker.kt`)
- [x] Default value: current date (today)
- [x] In ViewModel, add `selectedDate: Long` state (unix timestamp, defaults to `System.currentTimeMillis()`)
- [x] Pass `selectedDate` as the `timestamp` field when calling `TransactionRepository.addTransaction()`
- [x] Confirm `TransactionCard` already displays the formatted date from `transaction.timestamp` — if not, add it

**Testing**
- [x] Test: Open Quick Add → date defaults to today
- [x] Test: Change date to yesterday → transaction saved with yesterday's timestamp
- [x] Test: Transaction appears in list with the chosen date
- [x] Test: DatePicker opens and closes correctly

**Closure**
- [x] Update `CHANGELOG_INTERNAL.md`
- [x] Commit: `"Change 5: Date picker added to Quick Add Expense"`

---

## CHANGE 6: Fix 'Quick Add Expense' Bottom Sheet Shape & Cropping

> **What this is:** The Quick Add Expense bottom sheet has a rounded top that crops the "Batch Add" button and looks broken (Image 2). Fix it to be square/flat at the top, fully revealed, and not crop any content.

### Affected Files

- `ui/screens/QuickAddExpense/QuickAddExpenseSheet.kt`

### Tasks

- [ ] Locate the `ModalBottomSheet` call for this sheet
- [ ] Set `shape = RectangleShape` OR `shape = RoundedCornerShape(topStart = 0.dp, topEnd = 0.dp)`
  - The top edge must be a straight line, not a rounded arc
- [ ] Set `skipPartiallyExpanded = true` on the sheet state so it opens fully expanded
- [ ] Verify the drag handle does not interfere with the layout
- [ ] Verify "Batch Add" button/link is fully visible and tappable after fix

**Testing**
- [ ] Test: Open Quick Add → no rounded top cropping
- [ ] Test: "Batch Add" is fully visible and tappable
- [ ] Test: Sheet dismiss (swipe down or back button) still works

**Closure**
- [ ] Update `CHANGELOG_INTERNAL.md`
- [ ] Commit: `"Change 6: Fixed Quick Add Expense bottom sheet shape and cropping"`

---

## CHANGE 7: Replace '+' Button with Filter Folder Icon in Transactions Tab

> **What this is:** In the Transactions tab, the `+` button next to the search bar (which opens Batch Add) is replaced with a folder/filter icon. Tapping it opens a date-filter menu: Today, This Week, This Month, Custom Date.

### Affected Files

- `ui/screens/TransactionList/TransactionListScreen.kt`
- `ui/screens/TransactionList/TransactionListViewModel.kt`

### Tasks

**UI Change**
- [ ] Replace the `+` `IconButton` next to the search bar with `Icons.Default.FolderOpen` (or similar folder icon)
- [ ] Change click handler to open a date filter dropdown/menu

**Batch Add Access**
- [ ] Batch Add remains accessible in the Quick Add sheet header (already visible in Image 2 as "Batch Add" link) — no further action needed

**Date Filter**
- [ ] Create a `DateFilterMenu` composable (dropdown or bottom sheet):
  - **All Time** — clear filter (default)
  - **Today**
  - **This Week** (Mon–Sun)
  - **This Month**
  - **Custom Date** — opens date range picker
- [ ] In `TransactionListViewModel.kt`:
  - Add `dateFilter: DateFilter` sealed class state
  - Filter the `transactions` Flow based on `dateFilter`
- [ ] Show active filter indicator: when a filter is active, the folder icon changes to accent green

**Testing**
- [ ] Test: Tap folder icon → filter dropdown appears
- [ ] Test: Each filter option shows correct transactions
- [ ] Test: Custom date range works
- [ ] Test: "All Time" clears filter
- [ ] Test: Batch Add still accessible via Quick Add sheet

**Closure**
- [ ] Update `UTILITIES.md` with `DateFilter` sealed class
- [ ] Update `CHANGELOG_INTERNAL.md`
- [ ] Commit: `"Change 7: Transaction date filter added; Batch Add button replaced with folder icon"`

---

## CHANGE 8: Remove All Feature Icons from Top Bar

> **What this is:** This is completed as part of Change 1. Tracked here for cross-reference.

- [ ] Confirm all top bar feature icons are removed after Change 1 is complete
- [ ] Verify: only App title + Refresh icon remain in the top bar
- [ ] Verify: no orphaned click handlers for removed icons

**Closure:** Included in Change 1 commit.

---

## CHANGE 9: Fix Pull-to-Refresh Behavior in Transactions Tab

> **What this is:** Currently, the refresh indicator gets stuck at the top of the screen (at the search bar level) instead of animating within the list area. Fix so the pull-to-refresh works smoothly within the list.

### Affected Files

- `ui/screens/TransactionList/TransactionListScreen.kt`
- `ui/screens/TransactionList/TransactionListViewModel.kt`

### Tasks

- [ ] Locate the `PullToRefreshBox` (or `pullRefresh` modifier) in `TransactionListScreen.kt`
- [ ] Ensure the refresh container wraps ONLY the `LazyColumn`, not the search bar:
  ```
  Column {
    SearchBar  ← outside pull-to-refresh
    PullToRefreshBox {
      LazyColumn { ... }
    }
  }
  ```
- [ ] Use the official Compose `PullToRefreshBox` (Material3 stable API)
- [ ] In `TransactionListViewModel.kt`, add `isRefreshing: Boolean` state
  - `onRefresh`: set `isRefreshing = true`, re-fetch, set `isRefreshing = false`
- [ ] Refresh indicator uses accent green color (per new theme)

**Testing**
- [ ] Test: Pull down on the list → indicator appears below search bar (not stuck at search bar)
- [ ] Test: Data reloads on pull-to-refresh
- [ ] Test: Indicator disappears after refresh completes
- [ ] Test: Search bar remains fixed during pull

**Closure**
- [ ] Update `CHANGELOG_INTERNAL.md`
- [ ] Commit: `"Change 9: Fixed pull-to-refresh positioning in Transactions tab"`

---

## CHANGE 10: Auto-Focus Amount Input in Quick Add Expense

> **What this is:** When the user opens the Quick Add Expense sheet, the keyboard automatically opens and focuses the amount field — no manual tap required.

### Affected Files

- `ui/screens/QuickAddExpense/QuickAddExpenseSheet.kt`

### Tasks

- [ ] Add `FocusRequester` for the amount `TextField`
- [ ] Attach it to the amount input field via `.focusRequester(focusRequester)` modifier
- [ ] Request focus when sheet opens:
  ```kotlin
  LaunchedEffect(Unit) {
    delay(200) // Wait for sheet animation to complete
    focusRequester.requestFocus()
  }
  ```
- [ ] Ensure `keyboardType = KeyboardType.Decimal` on the amount field

**Testing**
- [ ] Test: Open Quick Add sheet → keyboard opens automatically on amount field
- [ ] Test: Works on both Expense and Income modes

**Closure**
- [ ] Update `CHANGELOG_INTERNAL.md`
- [ ] Commit: `"Change 10: Auto-focus amount input on Quick Add Expense open"`

---

## CHANGE 11: Transactions Grouped by Date in Transactions Tab

> **What this is:** Instead of a flat list, transactions are grouped by date with sticky date headers (e.g., "Today", "Yesterday", "12 Sept 2026"). See Image 4 for reference.

### Affected Files

- `ui/screens/TransactionList/TransactionListScreen.kt`
- `ui/screens/TransactionList/TransactionListViewModel.kt`

### Tasks

**ViewModel**
- [ ] Transform flat `List<Transaction>` into grouped structure:
  ```kotlin
  data class TransactionGroup(
    val dateLabel: String,      // "Today", "Yesterday", "12 Sept 2026"
    val transactions: List<TransactionDisplayItem>
  )
  ```
- [ ] Date label logic:
  - today → "Today"
  - yesterday → "Yesterday"
  - otherwise → formatted date (e.g., "12 Sept 2026")
- [ ] Expose `groupedTransactions: StateFlow<List<TransactionGroup>>`
- [ ] Grouping must work with date filter (Change 7) and search filter

**UI**
- [ ] Replace flat `LazyColumn` with grouped version using `stickyHeader`:
  ```kotlin
  LazyColumn {
    for (group in groupedTransactions) {
      stickyHeader { DateSectionHeader(label = group.dateLabel) }
      items(group.transactions) { TransactionCard(it) }
    }
  }
  ```
- [ ] Create `DateSectionHeader` composable:
  - Date label in `onSurfaceVariant` color (subtle)
  - Thin `HorizontalDivider` below
- [ ] When filtered by search/date: only show groups with matching transactions

**Testing**
- [ ] Test: Transactions appear grouped by date
- [ ] Test: "Today" and "Yesterday" labels correct
- [ ] Test: Transaction for past date appears in correct group
- [ ] Test: Search and date filter work with grouped layout

**Closure**
- [ ] Update `UTILITIES.md` with `TransactionGroup` and `DateSectionHeader`
- [ ] Update `CHANGELOG_INTERNAL.md`
- [ ] Commit: `"Change 11: Transactions grouped by date with section headers"`

---

## CHANGE 12: Smooth Tab Switching Animations

> **What this is:** Tab switches between Transactions, Analysis, and More feel jarring. Add smooth fade transitions.

### Affected Files

- `ui/navigation/NavHost.kt`

### Tasks

- [ ] In `NavHost.kt`, add `enterTransition` and `exitTransition` for the three bottom-nav destinations:
  ```kotlin
  composable(
    route = NavDestination.Transactions.route,
    enterTransition = { fadeIn(animationSpec = tween(durationMillis = 250, easing = FastOutSlowInEasing)) },
    exitTransition = { fadeOut(animationSpec = tween(durationMillis = 250, easing = FastOutSlowInEasing)) }
  ) { TransactionListScreen() }
  ```
- [ ] Apply same transitions to Analysis and More/Hub composables
- [ ] Do NOT add transitions to sub-screens (detail screens, dialogs)

**Testing**
- [ ] Test: Each tab switch has smooth fade
- [ ] Test: Fast switching doesn't crash
- [ ] Test: No white flash or content pop

**Closure**
- [ ] Update `CHANGELOG_INTERNAL.md`
- [ ] Commit: `"Change 12: Smooth animated transitions for bottom tab switching"`

---

## CHANGE 13: FAB to Add New Accounts

> **What this is:** There is currently no way to add a new account after initial setup. Add a FAB on the Accounts screen.

### Affected Files

- `ui/screens/Accounts/AccountsScreen.kt`

### Tasks

- [ ] Add `FloatingActionButton` to the `Scaffold` in `AccountsScreen.kt`:
  ```kotlin
  floatingActionButton = {
    FloatingActionButton(onClick = { showAddAccountDialog = true }) {
      Icon(Icons.Default.Add, contentDescription = "Add Account")
    }
  }
  ```
- [ ] Wire to existing `AddAccountDialog.kt`
- [ ] FAB uses accent green color

**Testing**
- [ ] Test: Hub → Accounts → FAB visible
- [ ] Test: Tap FAB → dialog opens → account added → appears in list

**Closure**
- [ ] Update `CHANGELOG_INTERNAL.md`
- [ ] Commit: `"Change 13: FAB for adding new accounts on AccountsScreen"`

---

## CHANGE 14: Conditional FAB — Hide Quick Add on Non-Transaction/Analysis Screens

> **What this is:** The Quick Add FAB only appears on Transactions and Analysis tabs. It hides everywhere else.

### Affected Files

- `MainActivity.kt` (or root Scaffold composable)

### Tasks

- [ ] Make the FAB conditional based on current nav route:
  ```kotlin
  val currentRoute = navController.currentBackStackEntryAsState().value?.destination?.route
  val showFab = currentRoute in listOf(
    NavDestination.Transactions.route,
    NavDestination.Analysis.route
  )
  ```
- [ ] Wrap the FAB in `AnimatedVisibility`:
  ```kotlin
  AnimatedVisibility(
    visible = showFab,
    enter = scaleIn() + fadeIn(),
    exit = scaleOut() + fadeOut()
  ) {
    FloatingActionButton(...)
  }
  ```

**Testing**
- [ ] Test: FAB visible on Transactions and Analysis
- [ ] Test: FAB hidden on More/Hub and all sub-screens
- [ ] Test: FAB animates in/out smoothly

**Closure**
- [ ] Update `CHANGELOG_INTERNAL.md`
- [ ] Commit: `"Change 14: Quick Add FAB only shown on Transactions and Analysis screens"`

---

## CHANGE 15: Auto-Switch Focus to Income Amount; Hide Category for Income

> **What this is:** When "Income" mode is selected in Quick Add Expense: (1) cursor auto-focuses the Income Amount field, and (2) the Category section is hidden.

### Affected Files

- `ui/screens/QuickAddExpense/QuickAddExpenseSheet.kt`
- `ui/screens/QuickAddExpense/QuickAddExpenseViewModel.kt`

### Tasks

- [ ] When user taps "Income" button:
  ```kotlin
  LaunchedEffect(isIncomeMode) {
    if (isIncomeMode) { delay(100); focusRequester.requestFocus() }
  }
  ```
- [ ] Wrap Category section in conditional:
  ```kotlin
  AnimatedVisibility(visible = !isIncomeMode) {
    CategorySection(...)
  }
  ```
- [ ] For Income, default `category` to an "Income" category automatically
- [ ] Same focus behavior on switching back to Expense

**Testing**
- [ ] Test: Tap "Income" → amount field focused, category hidden
- [ ] Test: Tap "Expense" → amount field focused, category visible
- [ ] Test: Income transaction saves with correct default category
- [ ] Test: Transition is smooth (AnimatedVisibility)

**Closure**
- [ ] Update `CHANGELOG_INTERNAL.md`
- [ ] Commit: `"Change 15: Income mode auto-focuses amount and hides category"`

---

## CHANGE 16: Transfer Button in Quick Add Expense

> **What this is:** A third "Transfer" button is added alongside "Expense" and "Income" in the Quick Add sheet, enabling account-to-account transfers.

### Affected Files

- `ui/screens/QuickAddExpense/QuickAddExpenseSheet.kt`
- `ui/screens/QuickAddExpense/QuickAddExpenseViewModel.kt`

### Tasks

- [ ] Expand the Expense/Income toggle to a three-option row: `[Expense]` `[Income]` `[Transfer]`
  - "Transfer" uses neutral color (`secondaryContainer` or similar grey)
- [ ] Transfer mode UI:
  - Amount input (same as expense/income)
  - "From Account" dropdown
  - "To Account" dropdown (cannot equal From Account)
  - Category hidden
  - Submit button: "Transfer"
- [ ] Auto-focus amount field on Transfer mode switch
- [ ] In ViewModel: on submit in Transfer mode, call `TransferRepository.createTransfer(fromId, toId, amount)`
- [ ] Validate: From ≠ To

**Testing**
- [ ] Test: Tap "Transfer" → transfer UI shown
- [ ] Test: Transfer creates two linked transactions
- [ ] Test: Cannot select same account for From and To
- [ ] Test: Transfer appears in transaction list

**Closure**
- [ ] Update `CHANGELOG_INTERNAL.md`
- [ ] Commit: `"Change 16: Transfer button added to Quick Add Expense"`

---

## CHANGE 17: Need/Want Toggle in Quick Add Expense + Need vs. Want Chart in Analysis

> **What this is:**
> 1. A "Need / Want" toggle in Quick Add Expense, placed below Category and above Description.
> 2. A "Spending by Need vs. Want" chart in Analysis, placed above "Spending by Category".
>
> ⚠️ **This requires a database schema migration.**

### Affected Files

- `data/db/Transaction.kt` — add `isNeed` field
- `data/db/AppDatabase.kt` — version bump + migration
- `SCHEMA.md`
- `ui/screens/QuickAddExpense/QuickAddExpenseSheet.kt`
- `ui/screens/QuickAddExpense/QuickAddExpenseViewModel.kt`
- `ui/screens/TransactionDetail/EditTransactionDialog.kt`
- `ui/screens/TransactionDetail/TransactionDetailScreen.kt`
- `ui/screens/BatchAdd/BatchAddTransactionsDialog.kt`
- `ui/screens/Analysis/AnalysisScreen.kt`
- `ui/screens/Analysis/AnalysisViewModel.kt`

### Tasks

**Schema Migration (do this first)**
- [ ] Add `isNeed: Boolean` to `Transaction` entity:
  ```kotlin
  @ColumnInfo(defaultValue = "1")
  val isNeed: Boolean = true
  ```
- [ ] Bump `AppDatabase` version (N → N+1)
- [ ] Write Room migration:
  ```kotlin
  val MIGRATION_N_N1 = object : Migration(N, N+1) {
    override fun migrate(db: SupportSQLiteDatabase) {
      db.execSQL("ALTER TABLE transactions ADD COLUMN isNeed INTEGER NOT NULL DEFAULT 1")
    }
  }
  ```
- [ ] Add migration to `AppDatabase.kt` builder
- [ ] Update `SCHEMA.md`
- [ ] Create `docs/decisions/ADR-012-NeedWant-Field.md`

**Quick Add Expense Sheet**
- [ ] Add "Need / Want" `SegmentedButton` or two-option toggle row:
  - Placement: between Category and Description
  - Default: "Need"
  - Hide when Income or Transfer mode is active
- [ ] Wire to ViewModel: `isNeed: Boolean` state
- [ ] Pass `isNeed` when calling `addTransaction()`

**Transaction Detail / Edit**
- [ ] `TransactionDetailScreen.kt`: show "Need" or "Want" chip/badge
- [ ] `EditTransactionDialog.kt`: add Need/Want toggle for editing

**Batch Add**
- [ ] `BatchAddTransactionsDialog.kt`: add Need/Want column per row

**Analysis Screen**
- [ ] In `AnalysisViewModel.kt`: aggregate `needTotal` and `wantTotal` for selected date range
- [ ] In `AnalysisScreen.kt`, add chart section ABOVE "Spending by Category":
  - **Title:** "Spending: Need vs. Want"
  - **Chart:** Use existing `PieChart.kt` — two segments
  - Show amounts and percentages below chart
  - Date range selector consistent with rest of Analysis screen
- [ ] Reuse `PieChart.kt` — do NOT create a new chart component

**Testing**
- [ ] Test: App launches after migration — no crash
- [ ] Test: Existing transactions default to "Need" (isNeed = true)
- [ ] Test: Need/Want toggle in Quick Add saves correctly
- [ ] Test: Analysis shows correct Need vs. Want split
- [ ] Test: Chart renders with only Needs, only Wants, or empty state

**Closure**
- [ ] Update `SCHEMA.md`
- [ ] Update `UTILITIES.md` with `NeedWantData`
- [ ] Update `CHANGELOG_INTERNAL.md`
- [ ] Commit: `"Change 17: Need/Want toggle in Quick Add + Need vs. Want chart in Analysis (with DB migration)"`

---

## CHANGE 18: Allow Delete & Edit of Default Categories

> **What this is:** Default (predefined) categories can now be edited and deleted by the user, just like custom categories.

### Affected Files

- `ui/screens/Categories/CategoriesScreen.kt`
- `ui/screens/Categories/CategoriesViewModel.kt`
- `ui/components/AddCategoryDialog.kt`
- `data/repository/CategoryRepository.kt`

### Tasks

- [ ] In `CategoriesScreen.kt`: enable edit and delete for ALL categories (remove any `isCustom` guard on UI actions)
- [ ] Show confirmation dialog before deleting any category:
  - "Transactions with this category will keep the category name but may appear uncategorized."
  - Buttons: "Delete" / "Cancel"
- [ ] In `CategoriesViewModel.kt`: remove guard that blocks `deleteCategory()` or `updateCategory()` for non-custom categories
- [ ] Prevent re-seeding deleted defaults on app restart:
  - Add a `hasSeededDefaultCategories: Boolean` flag to DataStore preferences
  - Only seed categories on very first launch — document in ADR
- [ ] `AddCategoryDialog.kt`: support edit mode — accept optional `categoryToEdit` parameter:
  - If not null → pre-fill fields, title = "Edit Category", submit calls `updateCategory()`
  - If null → empty form, title = "Add Category", submit calls `addCategory()`

**Testing**
- [ ] Test: Default category shows edit and delete buttons
- [ ] Test: Edit a default category → changes saved
- [ ] Test: Delete a default category → removed, does not re-appear after restart
- [ ] Test: Custom categories unaffected
- [ ] Test: Transactions with deleted category still display correctly

**Closure**
- [ ] Update `CHANGELOG_INTERNAL.md`
- [ ] Update `SCHEMA.md` if `isCustom` field behavior changes
- [ ] Commit: `"Change 18: Default categories can now be deleted and edited"`

---

## Implementation Order & Dependencies

> Follow this order to minimize breakage. Changes that depend on others are listed after their dependencies.

| Order | Change | Depends On | Risk |
|-------|--------|------------|------|
| 1st | **Change 3** (Theme refactor) | None | Medium — affects all screens |
| 2nd | **Change 4** (Remove Notes) | None | Low |
| 3rd | **Change 17** (Need/Want — schema migration) | None functionally, do early | High — DB migration |
| 4th | **Change 1 + 8** (Hub screen + top bar cleanup) | Change 3 for colors | Medium |
| 5th | **Change 2** (Settings full page) | Change 1 (Hub exists) | Low |
| 6th | **Change 13** (Add Account FAB) | Change 1 (AccountsScreen is sub-screen) | Low |
| 7th | **Change 6** (Fix sheet shape) | None | Low |
| 8th | **Change 10** (Auto-focus amount) | None | Low |
| 9th | **Change 15** (Income mode focus + hide category) | Change 10 | Low |
| 10th | **Change 16** (Transfer button) | Change 15 (3-mode sheet) | Medium |
| 11th | **Change 5** (Date picker in Quick Add) | Change 16 (sheet layout stable) | Low |
| 12th | **Change 9** (Pull-to-refresh fix) | None | Low |
| 13th | **Change 11** (Group transactions by date) | Change 5 (dates are accurate) | Medium |
| 14th | **Change 7** (Filter folder icon) | Change 11 (grouping works) | Medium |
| 15th | **Change 12** (Smooth tab animations) | Change 1 (navigation stable) | Low |
| 16th | **Change 14** (Conditional FAB) | Change 12 (nav stable) | Low |
| 17th | **Change 18** (Edit/delete default categories) | None | Low |

---

## Cross-Cutting Concerns

### "Do Not Break" Checklist (Verify After Every Change)

- [ ] Transaction list still loads and shows data
- [ ] Quick Add Expense saves transactions correctly
- [ ] Analysis screen still renders all charts
- [ ] All accounts visible and accurate
- [ ] Import/Export still works
- [ ] Light and dark theme both work
- [ ] Haptic feedback still works
- [ ] Pull-to-refresh triggers data reload
- [ ] Batch Add transactions still accessible and functional
- [ ] Transfers still create two linked records
- [ ] Recurring expenses still generate
- [ ] Goals, Debt, Budget screens still function via Hub navigation
- [ ] App compiles with no errors after each change

### Session Checklist (Use for Every Implementation Session)

```
# Session Goal
[One sentence: what this session will accomplish]

# Files to Touch
[List files to create/modify]

# Files NOT to Touch
[Explicitly list off-limits files]

# Definition of Done
- [ ] Feature implemented per ARCHITECTURE.md
- [ ] No hardcoded colors/sizes — all theme tokens
- [ ] UTILITIES.md updated if new composable/utility added
- [ ] CHANGELOG_INTERNAL.md updated
- [ ] DONE.md checklist satisfied
- [ ] "Do Not Break" checklist verified manually
- [ ] Git committed with message from this document
```

---

## Governance Documents to Update

| Document | When to Update |
|----------|----------------|
| `SCHEMA.md` | Change 17 (adds `isNeed` field) |
| `UTILITIES.md` | Every change that adds a new Composable or utility |
| `CHANGELOG_INTERNAL.md` | After every session |
| `DEPENDENCIES.md` | Only if a new library is added (none expected) |
| `docs/decisions/ADR-011-Color-Palette-Refactor.md` | Change 3 |
| `docs/decisions/ADR-012-NeedWant-Field.md` | Change 17 |

---

**Build in the recommended order. Verify the "Do Not Break" checklist after every change. Commit often.**
