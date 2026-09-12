# Finance App — Complete Project Breakdown into Phases & Tasks

> **Governance First:** All foundational documents are written before ANY production code is written.
> This prevents the vibe coding problems that plagued other projects.

---

## PHASE 0: Governance & Foundation Setup (Weeks 1-2)

### Documentation & Rules

- [x] Review and finalize `ARCHITECTURE.md` (already created — validate all sections apply)
- [x] Create `SCHEMA.md` — document the complete data model in plain language
  - Tables: Transaction, Account, Transfer, Goal, Debt, RepeatExpense, Category, Label
  - Relationships and constraints
  - Invariants that must never break
- [x] Create `UTILITIES.md` — index of all shared utilities, formatters, extensions, components
  - Monetary formatter (Long → ₹ display)
  - Date/time formatter (timestamps → readable dates)
  - Currency conversion utilities
  - Transaction categorization logic
  - Mark section for "Do NOT create" duplicates
- [x] Create `DEPENDENCIES.md` — approved stack
  - Approved: Kotlin, Jetpack Compose, Room, DataStore, Hilt, Glance, Vico, OpenCSV, Lottie
  - Banned alternatives: SharedPreferences (use DataStore), Firebase (offline only), Retrofit (no network)
  - Banned completely: Any authentication library, cloud sync library, analytics
- [x] Create `DONE.md` — definition of done checklist
  - Happy path + empty state + error state
  - Data persistence test (survive restart and crash)
  - No architectural drift introduced
  - Documentation updated
  - At least one test written
- [x] Create `CHANGELOG_INTERNAL.md` — (start empty, populate after each session)
- [x] Create `docs/decisions/ADR-000-Offline-Only-Constraint.md`
  - Decision: App is fully offline, no network access, no backend
  - Reasoning: User privacy, no dependencies on external services
  - Consequences: Import/export only mechanism for data movement, schema migrations must work flawlessly

### Project Setup in Antigravity

- [x] Initialize empty Android project with Kotlin + Compose + Hilt
- [x] Create folder structure per `ARCHITECTURE.md`:
  ```
  app/
  ├── data/
  │   ├── db/
  │   ├── datastore/
  │   ├── repository/
  │   └── model/
  ├── ui/
  │   ├── screens/
  │   ├── components/
  │   ├── theme/
  │   └── navigation/
  ├── widget/
  ├── util/
  └── di/
  ```
- [x] Set up build.gradle with locked dependency versions
  - Record each dependency in `DEPENDENCIES.md`
  - Pin versions — no floating versions
- [x] Create `docs/decisions/` folder for ADRs
- [x] Set up git with initial commit (checkpoint before ANY code)

---

## PHASE 1: Design System & Theme (Week 2-3)

> All visual tokens are defined upfront. AI cannot drift from them because they're mechanically enforced.

### Design Tokens

- [x] Create `ui/theme/Color.kt`
  - Light theme: primary, secondary, tertiary, surface, background, error, success, warning
  - Dark theme: inverse mappings
  - Ensure WCAG AA contrast ratios
  - Document the chosen color palette rationale in `docs/decisions/ADR-001-Color-Palette.md`
- [x] Create `ui/theme/Type.kt`
  - Font family: choose (e.g., Roboto, Inter, or system default)
  - Font sizes: xs (12sp), sm (13sp), body (15sp), lg (18sp), xl (20sp), title (24sp)
  - Line heights and letter spacing
  - Styles: bodySmall, bodyMedium, titleMedium, labelMedium, etc.
- [x] Create `ui/theme/Shape.kt`
  - Border radius: none (0dp), small (4dp), medium (8dp), large (12dp), full (9999dp)
  - Consistent shape usage per component type
- [x] Create `ui/theme/Spacing.kt`
  - Spacing scale: xs (4dp), sm (8dp), md (16dp), lg (24dp), xl (32dp)
  - Custom `Spacing` class with computed values for consistency
- [x] Create `ui/theme/Theme.kt`
  - Root MaterialTheme composable
  - Light/dark mode switching via DataStore preference
  - Font scale adjustment (for v4 feature: "font size increase")
  - Dual theme must be tested on both light and dark mode

### Design System Components

- [x] Create `ui/components/Buttons.kt`
  - Primary, secondary, tertiary button variants
  - All use theme tokens (no hardcoded colors/sizes)
- [x] Create `ui/components/Cards.kt`
  - TransactionCard, AccountCard, GoalCard variants
  - All use theme tokens
- [x] Create `ui/components/Inputs.kt`
  - TextField with proper validation feedback
  - All use theme tokens
- [x] Create `ui/components/Dialogs.kt`
  - Basic AlertDialog wrapper
  - All use theme tokens
- [x] Create `ui/components/Spacing.kt`
  - Spacer utilities using theme spacing scale
- [x] Create base reusable Composables (extend as features are built):
  - LoadingState, ErrorState, EmptyState screens
  - CurrencyInput with proper formatting
  - DatePicker integration

### Governance Update

- [x] Update `UTILITIES.md` with all new Composables created
- [x] Update `CHANGELOG_INTERNAL.md`: "Phase 1: Design system and theme defined. All tokens centralized."
- [x] Commit to git: "Phase 1 complete: Design tokens and theme system"

---

## PHASE 2: Database Schema & Core Repositories (Week 3-4)

> Data model is defined and tested before features touch it.

### Room Database Schema

- [x] Create `data/db/Transaction.kt` (Entity)
  ```kotlin
  @Entity
  data class Transaction(
    @PrimaryKey val id: String,
    val accountId: String,
    val amount: Long, // smallest currency unit
    val description: String,
    val timestamp: Long, // Unix ms
    val category: String,
    val tags: String, // JSON array
    val transferId: String?, // if linked to another transaction
    val notes: String?
  )
  ```
- [x] Create `data/db/Account.kt` (Entity)
  - Fields: id, name, type (cash/bank/credit), balance (Long), currency, createdAt, isActive
- [x] Create `data/db/Transfer.kt` (Entity)
  - Tracks transfers between accounts (two Transaction records linked by transferId)
  - Fields: id, fromAccountId, toAccountId, amount (Long), timestamp, notes
- [x] Create `data/db/Goal.kt` (Entity)
  - Fields: id, name, targetAmount (Long), deadline, currentAmount (Long), linkedAccountId?, category, notes
- [x] Create `data/db/Debt.kt` (Entity)
  - Fields: id, type (lent/borrowed), counterparty, amount (Long), createdAt, settledAt?, notes
  - **Never deleted** — only marked settled
- [x] Create `data/db/RepeatExpense.kt` (Entity)
  - Fields: id, description, amount (Long), frequency (DAILY/WEEKLY/MONTHLY/YEARLY), lastGenerated, category, isActive
- [x] Create `data/db/Category.kt` (Entity)
  - Predefined + user-created categories
  - Fields: id, name, icon, color, isCustom
- [x] Create `data/db/AppDatabase.kt`
  - Room database class with all DAOs
  - Version 1, no migrations needed yet

### DAO Interfaces

- [x] Create `data/db/TransactionDao.kt`
  - Insert, update, delete transaction
  - Query by account, date range, category
  - Query for statistics (sum, count, average)
- [x] Create `data/db/AccountDao.kt`
- [x] Create `data/db/GoalDao.kt`
- [x] Create `data/db/DebtDao.kt`
- [x] Create `data/db/RepeatExpenseDao.kt`
- [x] Create `data/db/CategoryDao.kt`

### Repositories

- [x] Create `data/repository/TransactionRepository.kt`
  - `fun getAllTransactions(): Flow<List<Transaction>>`
  - `suspend fun addTransaction(transaction: Transaction)`
  - `suspend fun updateTransaction(transaction: Transaction)`
  - `suspend fun deleteTransaction(id: String)`
  - `fun getTransactionsByDateRange(start: Long, end: Long): Flow<List<Transaction>>`
  - `fun getTransactionsByCategory(category: String): Flow<List<Transaction>>`
  - All monetary logic stored as Long, no rounding
- [x] Create `data/repository/AccountRepository.kt`
  - CRUD operations
  - `fun getAccountBalance(accountId: String): Flow<Long>`
  - Cascade or block logic for deletion (document in ADR)
- [x] Create `data/repository/GoalRepository.kt`
- [x] Create `data/repository/DebtRepository.kt`
- [x] Create `data/repository/TransferRepository.kt`
  - `suspend fun createTransfer(fromAccountId, toAccountId, amount)` → creates TWO linked Transaction records
  - Ensures consistency: debit + credit always paired
- [x] Create `data/repository/RepeatExpenseRepository.kt`
  - `fun generateNextOccurrence(id: String): Flow<Transaction?>` → lazy generation
- [x] Create `data/repository/CategoryRepository.kt`

### DataStore Preferences

- [x] Create `data/datastore/PreferenceKeys.kt`
  - Theme preference (light/dark)
  - Font scale multiplier
  - Currency preference
  - Language preference
  - App opening count (for analytics/reviews, if needed)
- [x] Create `data/datastore/PreferencesRepository.kt`
  - Getter/setter functions for all preferences

### Data Model Classes

- [x] Create `data/model/TransactionDisplayItem.kt`
  - Used in UI layer (never expose Entity directly to UI)
  - Fields: id, description, amountFormatted (String), date (String), category, tags
- [x] Create display models for all other entities
  - Separation between database entities and UI models

### Dependency Injection Setup

- [x] Create `di/DatabaseModule.kt` — provides AppDatabase and all DAOs
- [x] Create `di/RepositoryModule.kt` — provides all repository instances
- [x] Create `di/DataStoreModule.kt` — provides DataStore instance

### Testing Setup

- [x] Create sample Room database test: insert and retrieve transaction
- [x] Create TransactionRepository test: verify monetary values stored as Long
- [x] Create TransferRepository test: verify two-transaction linking

### Governance Update

- [x] Update `SCHEMA.md` with complete ER diagram and invariants
- [x] Update `UTILITIES.md` with currency formatter signature
- [x] Create `docs/decisions/ADR-001-Monetary-Storage-Long.md`
  - Decision: All monetary values stored as Long
  - Reasoning: Avoid floating-point precision errors in financial calculations
  - Consequences: Every monetary display requires formatter, calculations never lose precision
- [x] Create `docs/decisions/ADR-002-Transfers-As-Dual-Records.md`
  - Decision: Transfers stored as two linked Transaction records, not one
  - Reasoning: Maintains account-centric transaction log, simplifies queries
  - Consequences: Must always create/delete transfers atomically
- [x] Create `docs/decisions/ADR-003-Repeat-Expenses-Lazy-Generation.md`
  - Decision: Repeat expenses generated one at a time on demand, not eagerly
  - Reasoning: Avoids unbounded expense tables, scales to any frequency
  - Consequences: UI must request next occurrence explicitly
- [x] Update `CHANGELOG_INTERNAL.md`: "Phase 2: Core database schema and repositories complete."
- [x] Commit to git: "Phase 2 complete: Database schema and repositories"

---

## PHASE 3: Core Navigation & Base UI Structure (Week 4)

> Single Activity, bottom navigation, screen routing all set up before feature screens are built.

### Navigation Setup

- [x] Create `ui/navigation/NavDestinations.kt`
  - Sealed class for all routes:
    ```kotlin
    sealed class NavDestination {
      object TransactionList : NavDestination()
      object Analysis : NavDestination()
      object Accounts : NavDestination()
      // Add others as features are built
    }
    ```
- [x] Create `ui/navigation/NavHost.kt`
  - Single NavHost in the main Activity
  - Routes mapped to screen Composables
  - No Fragments — Compose only

### Main Activity & App-level Structure

- [x] Create `MainActivity.kt`
  - Single Activity, full-screen Compose
  - Sets up theme based on DataStore preference
  - Passes NavHost as content
- [x] Create `ui/AppState.kt`
  - Holds navigation state
  - Holds global preferences (theme, language)

### Bottom Navigation

- [x] Create `ui/components/BottomNavBar.kt`
  - Tabs: Transactions, Analysis, Accounts, More (if needed)
  - Navigation between tabs
  - Uses theme colors and spacing

### Scaffold Structure

- [x] Create base scaffold Composable
  - TopAppBar
  - BottomNavBar
  - Floating Action Button (for quick add)
  - All using theme tokens

### Screen Templates

- [x] Create `ui/screens/TransactionList/TransactionListScreen.kt` (empty, structure only)
- [x] Create `ui/screens/Analysis/AnalysisScreen.kt` (empty, structure only)
- [x] Create `ui/screens/Accounts/AccountsScreen.kt` (empty, structure only)
- [x] Each screen has a ViewModel (empty, to be populated)

### Governance Update

- [x] Create `docs/decisions/ADR-004-Single-Activity-Navigation.md`
  - Decision: One Activity, all screens via Jetpack Navigation Compose
  - Reasoning: Simpler state management, no Fragment lifecycle complexity
  - Consequences: No Fragments anywhere, all state in ViewModels
- [x] Update `CHANGELOG_INTERNAL.md`: "Phase 3: Navigation structure and screen templates."
- [x] Commit to git: "Phase 3 complete: Navigation and base UI structure"

---

## PHASE 4: Version 1.0.0 Features (Weeks 5-8)

> Core features that establish the app's foundation.
> Each feature is one Antigravity session bounded by ARCHITECTURE.md + DONE.md.

### Feature 4.1: Account Management

**Planning**
- [x] Create `docs/decisions/ADR-005-Account-Deletion-Cascade.md`
  - Decision: Deleting an account cascades delete all transactions (or block if transfers exist)
  - Specify clearly before implementing

**Implementation**
- [x] Create `ui/screens/Accounts/AccountsViewModel.kt`
  - `data class AccountsUiState(val accounts: List<AccountDisplayItem> = emptyList())`
  - Functions: addAccount, editAccount, deleteAccount
- [x] Create `ui/screens/Accounts/AccountsScreen.kt`
  - List of accounts with balance
  - Button to add new account
  - Edit/delete account buttons
- [x] Create `ui/screens/Accounts/AddAccountDialog.kt`
  - Form: name, type (cash/bank/credit), initial balance
  - Uses theme tokens
- [x] Update `UTILITIES.md` with AccountDisplayItem and account formatter

**Testing**
- [x] Test: Add account and verify it appears in list
- [x] Test: Edit account name and verify update
- [x] Test: Delete account behavior (cascade/block)
- [x] Test: Account balance calculated correctly

**Closure**
- [x] Answer comprehension questions: What files changed? What does each own? What depends on it now?
- [x] Update `CHANGELOG_INTERNAL.md`
- [x] Commit: "Feature 4.1: Account management"

### Feature 4.2: Quick Add Expense

**Planning**
- [x] Fastest path to add a transaction from main screen

**Implementation**
- [x] Create floating action button on main screen
- [x] Create `ui/screens/QuickAddExpense/QuickAddExpenseSheet.kt`
  - Bottom sheet dialog
  - Amount (with currency formatter)
  - Category dropdown
  - Account dropdown
  - Description (optional)
  - Submit button
- [x] Connect to `TransactionRepository.addTransaction()`
- [x] On submit: show success toast, clear form

**Testing**
- [x] Test: Enter amount, select category, submit → transaction saved
- [x] Test: Quick add opens from FAB
- [x] Test: Amount formatting works correctly

**Closure**
- [x] Update `CHANGELOG_INTERNAL.md`
- [x] Commit: "Feature 4.2: Quick add expense"

### Feature 4.3: Transaction List & Display

**Planning**
- [x] Main screen shows list of all transactions
- [x] Sorted by date (newest first)
- [x] Shows: date, description, amount, category, account

**Implementation**
- [x] Create `ui/screens/TransactionList/TransactionListViewModel.kt`
  - Fetches transactions from repository via Flow
  - Exposes as StateFlow<UiState>
- [x] Create `ui/screens/TransactionList/TransactionListScreen.kt`
  - LazyColumn of transactions (not plain Column — performance rule)
  - Each item is `TransactionCard` from components
  - Empty state when no transactions
  - Tap on transaction → detail view
- [x] Create `ui/screens/TransactionDetail/TransactionDetailScreen.kt`
  - Shows full transaction info
  - Edit, delete, duplicate buttons
  - Back navigation

**Testing**
- [x] Test: Add transaction, see it in list
- [x] Test: Sort by date (newest first)
- [x] Test: Empty state when no transactions
- [x] Test: List uses LazyColumn (performance)
- [x] Test: Amount formatting in list

**Closure**
- [x] Update `CHANGELOG_INTERNAL.md`
- [x] Commit: "Feature 4.3: Transaction list and display"

### Feature 4.4: Dual Theme (Light & Dark)

**Planning**
- [x] User can toggle light/dark theme
- [x] Preference saved to DataStore
- [x] Applies to entire app

**Implementation**
- [x] Theme preference UI in settings/more screen
- [x] Toggle button → writes to DataStore
- [x] MainActivity reads DataStore and sets theme
- [x] All colors already use theme tokens (should be automatic)

**Testing**
- [x] Test: Toggle theme, all colors change
- [x] Test: Theme preference persists on restart
- [x] Test: All screens visible in both themes

**Closure**
- [x] Commit: "Feature 4.4: Dual theme"

### Feature 4.5: Basic Analysis (Charts)

**Planning**
- [x] Analysis screen shows high-level spending insights
- [x] Charts: spending by category (pie), spending over time (line)

**Implementation**
- [x] Create `ui/screens/Analysis/AnalysisViewModel.kt`
  - Fetches transactions
  - Aggregates by category (sum of amounts)
  - Aggregates by week/month (sum over time ranges)
  - All aggregation in repository layer, not in composable
- [x] Create `ui/screens/Analysis/AnalysisScreen.kt`
  - Pie chart (Vico): category breakdown
  - Line chart (Vico): spending trend
  - Date range picker (week/month/year)
  - Use theme colors for chart
- [x] Integrations with Vico library

**Testing**
- [x] Test: Chart renders with sample data
- [x] Test: Date range filter works
- [x] Test: Category aggregation correct

**Closure**
- [x] Commit: "Feature 4.5: Basic analysis"

### Feature 4.6: Category Management

**Planning**
- [x] Predefined categories (Food, Transport, Entertainment, etc.)
- [x] User can create custom categories
- [x] Categories used in transactions and analysis

**Implementation**
- [x] Seed database with default categories on first launch
- [x] Create UI to add custom category (name, icon, color)
- [x] Store custom categories in database
- [x] Dropdown in QuickAddExpense uses category list

**Testing**
- [x] Test: Default categories appear
- [x] Test: Add custom category, appears in dropdown
- [x] Test: Custom category persists

**Closure**
- [x] Commit: "Feature 4.6: Category management"

### Feature 4.7: Transfers (Between Accounts)

**Planning**
- [x] User can transfer money between two accounts
- [x] Stored as two linked Transaction records (debit + credit)
- [x] Appears in transaction list for both accounts

**Implementation**
- [x] Create `ui/screens/Transfer/TransferDialog.kt`
  - From account, to account, amount
  - Submit creates transfer via `TransferRepository.createTransfer()`
- [x] Repository creates two transactions atomically (linked by transferId)
- [x] Show transfers in transaction list (with special marker)

**Testing**
- [x] Test: Create transfer, verify two transactions created
- [x] Test: Transfer amount deducted from source, added to destination
- [x] Test: Delete transfer, both transactions deleted atomically

**Closure**
- [x] Commit: "Feature 4.7: Transfers"

### Feature 4.8: Import/Export (CSV)

**Planning**
- [x] Export transactions to CSV file
- [x] Import transactions from CSV file (all-or-nothing)

**Implementation**
- [x] Create `ui/screens/Settings/ImportExportScreen.kt`
- [x] Export button → writes transactions to CSV in app cache/files directory
- [x] Import button → file picker, reads CSV, validates all rows, commits atomically
- [x] Use OpenCSV library
- [x] Handle errors: show error dialog if import fails
  - Rollback: import commits nothing if any row is invalid
- [x] Mapping: CSV columns to Transaction fields

**Testing**
- [x] Test: Export produces valid CSV
- [x] Test: Import from valid CSV
- [x] Test: Invalid row in import → entire import rejected
- [x] Test: Large export/import (performance)

**Closure**
- [x] Create `docs/decisions/ADR-006-Import-All-Or-Nothing.md`
- [x] Commit: "Feature 4.8: Import/Export"

### Feature 4.9: Insights (Basic Statistics)

**Planning**
- [ ] Simple dashboard with key stats:
  - Total spent this month
  - Average daily spend
  - Largest transaction
  - Most used category

**Implementation**
- [ ] Create `ui/screens/Insights/InsightsViewModel.kt`
  - Calculate statistics from transactions
  - All calculation in repository
- [ ] Create `ui/screens/Insights/InsightsScreen.kt`
  - Display stats in card format
  - Use theme tokens
  - Show previous month for comparison

**Testing**
- [ ] Test: Stats calculate correctly
- [ ] Test: Empty state when no data

**Closure**
- [ ] Commit: "Feature 4.9: Insights"

### Phase 4 Closure

- [ ] Run comprehensive test of all v1.0.0 features
- [ ] Verify no architectural drift (review ARCHITECTURE.md adherence)
- [ ] Update all companion files:
  - `UTILITIES.md` — all new formatters, components
  - `CHANGELOG_INTERNAL.md` — full v1.0.0 summary
  - `SCHEMA.md` — current schema
  - `DEPENDENCIES.md` — any new libraries used
- [ ] Perform weekly architecture review
- [ ] Commit: "Phase 4 complete: v1.0.0 features"

---

## PHASE 5: Version 2.0.0 Features (Weeks 9-12)

> Advanced financial tracking features.

### Feature 5.1: Debt & Loans Management

**Planning**
- [x] Track money lent to others and borrowed from others
- [x] Entries include: type (lent/borrowed), counterparty, amount, createdAt, settledAt
- [x] **Debt entries are never deleted** — only marked settled

**Implementation**
- [x] Create `ui/screens/Debt/DebtListScreen.kt`
- [x] Create `ui/screens/Debt/AddDebtDialog.kt`
  - Form: type (radio), counterparty name, amount, notes
- [x] DebtViewModel fetches debts, shows active and settled
- [x] Mark debt as settled (sets settledAt timestamp)
- [x] Create `docs/decisions/ADR-007-Debt-Never-Deleted.md`

**Testing**
- [x] Test: Add debt, see in active list
- [x] Test: Mark as settled, move to settled list
- [x] Test: Delete button blocked (or doesn't exist)
- [x] Test: Settled debts tracked separately

**Closure**
- [x] Commit: "Feature 5.1: Debt & loans"

### Feature 5.2: Repeat Expenses (Lazy Generation)

**Planning**
- [x] User defines recurring expenses (daily, weekly, monthly, yearly)
- [x] On each day, next occurrence is generated automatically
- [x] Stored as separate transactions (one per occurrence)

**Implementation**
- [x] Create `ui/screens/RepeatExpense/RepeatExpenseListScreen.kt`
- [x] Create `ui/screens/RepeatExpense/AddRepeatDialog.kt`
  - Form: description, amount, frequency, category, isActive toggle
- [x] RepeatExpenseRepository implements lazy generation:
  - `fun generateNextOccurrence(repeatExpenseId): Flow<Transaction?>`
  - Called at app startup via ViewModel
  - Creates one transaction if enough time has passed
- [x] Metadata: lastGenerated timestamp on RepeatExpense entity

**Testing**
- [x] Test: Create repeat expense
- [x] Test: Next occurrence generated (simulate time passage)
- [x] Test: Only one occurrence per eligible date
- [x] Test: Toggle inactive stops generation

**Closure**
- [x] Create `docs/decisions/ADR-003-Repeat-Expenses-Lazy-Generation.md` (from phase 2)
- [x] Commit: "Feature 5.2: Repeat expenses"

### Feature 5.3: Goals Tracking

**Planning**
- [x] User sets savings goals (target amount, deadline)
- [x] Track progress toward goal
- [x] Can link goal to specific account or dedicated fund

**Implementation**
- [x] Create `ui/screens/Goals/GoalsListScreen.kt`
  - Shows all goals with progress bars
  - Color coded: on track (green), warning (yellow), overdue (red)
- [x] Create `ui/screens/Goals/AddGoalDialog.kt`
  - Form: name, target amount, deadline, optional linked account, category
- [x] GoalViewModel calculates progress:
  - If linked account: progress = account balance / target
  - If dedicated: progress = dedicated balance / target
- [x] Show timeline: days/weeks/months until deadline

**Testing**
- [x] Test: Create goal
- [x] Test: Progress calculated correctly
- [x] Test: Deadline logic works
- [x] Test: Color coding based on progress

**Closure**
- [x] Commit: "Feature 5.3: Goals"

### Feature 5.4: Enhanced Analysis

**Planning**
- [x] Deeper analytics: trends, forecasts, comparisons
- [x] Breakdown by multiple dimensions (category + account)

**Implementation**
- [x] Add more chart types (bar, stacked)
- [x] Trend analysis: is spending increasing or decreasing?
- [x] Year-over-year comparison
- [x] Category composition over time

**Testing**
- [x] Test: Charts render correctly with multiple dimensions

**Closure**
- [x] Commit: "Feature 5.4: Enhanced analysis"

### Feature 5.5: Notes on Transactions

**Planning**
- [x] Transactions can have rich notes
- [x] Notes searchable later

**Implementation**
- [x] Add notes field to Transaction entity (already in schema)
- [x] Create `ui/components/NotesField.kt` — rich text input
- [x] Edit notes from transaction detail screen

**Testing**
- [x] Test: Add notes, saved with transaction
- [x] Test: Edit notes

**Closure**
- [x] Commit: "Feature 5.5: Notes on transactions"

### Feature 5.6: Multiple Transactions (Batch Add)

**Planning**
- [x] Add multiple transactions at once (not just one at a time)

**Implementation**
- [x] Create multi-transaction add dialog
- [x] Repeatable form: add row, enter data, submit all at once
- [x] All transactions committed atomically

**Testing**
- [x] Test: Add 5 transactions at once
- [x] Test: All saved

**Closure**
- [x] Commit: "Feature 5.6: Multiple transactions"

### Phase 5 Closure

- [ ] Test all v2.0.0 features
- [ ] Verify no architectural drift
- [ ] Update all companion files
- [ ] Weekly architecture review
- [ ] Commit: "Phase 5 complete: v2.0.0 features"

---

## PHASE 6: Version 3.0.0 Features (Weeks 13-15)

> Investment and budgeting — more advanced use cases.

### Feature 6.1: Investment Tracking

**Planning**
- [ ] Track investment accounts (stocks, mutual funds, crypto)
- [ ] Manual entry of holdings and valuation
- [ ] Separate from cash accounts

**Implementation**
- [ ] Add account type: investment
- [ ] Create `ui/screens/Investments/InvestmentListScreen.kt`
- [ ] Create `ui/screens/Investments/AddInvestmentDialog.kt`
  - Form: name, ticker (optional), quantity, value per unit, total value
  - Store as single investment record (or split into separate table?)
- [ ] Display portfolio value and breakdown

**Testing**
- [ ] Test: Add investment
- [ ] Test: Portfolio calculated correctly

**Closure**
- [ ] Create `docs/decisions/ADR-008-Investment-Model.md` (structure decision)
- [ ] Commit: "Feature 6.1: Investment tracking"

### Feature 6.2: Budget Planning & Alerts

**Planning**
- [ ] User sets monthly budgets per category
- [ ] App warns when approaching/exceeding budget
- [ ] Compare actual vs. budgeted spending

**Implementation**
- [ ] Create Budget entity:
  - Fields: category, monthYear, amount (Long), isActive
- [ ] Create `ui/screens/Budget/BudgetListScreen.kt`
- [ ] Create `ui/screens/Budget/SetBudgetDialog.kt`
- [ ] BudgetViewModel:
  - Fetches actual spending per category per month
  - Calculates % of budget used
  - Sets alerts (80%, 100%)
- [ ] Show budget vs. actual in charts

**Testing**
- [ ] Test: Set budget
- [ ] Test: Actual spending compared to budget
- [ ] Test: Alert logic at thresholds

**Closure**
- [ ] Commit: "Feature 6.2: Budget planning"

### Phase 6 Closure

- [ ] Test all v3.0.0 features
- [ ] Verify no architectural drift
- [ ] Update all companion files
- [ ] Weekly architecture review
- [ ] Commit: "Phase 6 complete: v3.0.0 features"

---

## PHASE 7: Version 4.0.0 Features — Polish & Experience (Weeks 16-18)

> Visual and tactile refinements. These depend heavily on Android platform capabilities.

### Feature 7.1: Font Size Increase

**Planning**
- [ ] Global font scale setting accessible from settings
- [ ] All text scales proportionally
- [ ] Applied at theme level (not per-Composable)

**Implementation**
- [ ] Add `fontScaleMultiplier` to DataStore preferences
- [ ] Settings screen: slider from 0.8x to 1.5x (or similar range)
- [ ] `Theme.kt` applies scale:
  ```kotlin
  val scaledFontSize = baseFontSize * fontScaleMultiplier
  ```
- [ ] Update all `Type.kt` style definitions to use scaled size
- [ ] Test on all screens

**Testing**
- [ ] Test: Adjust scale, all text resizes
- [ ] Test: Setting persists on restart
- [ ] Test: UI doesn't break at extreme scales (0.8x, 1.5x)

**Closure**
- [ ] Commit: "Feature 7.1: Font size increase"

### Feature 7.2: Multi-Language Support

**Planning**
- [ ] Support multiple languages (at minimum: English, Hindi, maybe more)
- [ ] All user-visible strings in string resources

**Implementation**
- [ ] Create `res/values/strings.xml` (English)
- [ ] Create `res/values-hi/strings.xml` (Hindi)
  - Translate all strings
- [ ] Add language preference to DataStore
- [ ] Settings screen: language selector (radio buttons or dropdown)
- [ ] App restart with new language (or recomposition if possible)
- [ ] Audit: Verify NO hardcoded strings in Composables

**Testing**
- [ ] Test: Each language loads all strings correctly
- [ ] Test: Language change takes effect
- [ ] Test: All screens display correctly in all languages

**Closure**
- [ ] Create `docs/decisions/ADR-009-Multi-Language-Strategy.md`
- [ ] Commit: "Feature 7.2: Multi-language support"

### Feature 7.3: Cool Animations

**Planning**
- [ ] Smooth transitions between screens
- [ ] List item animations (enter, exit, reorder)
- [ ] Chart animations when data updates
- [ ] Micro-interactions on buttons, touch feedback

**Implementation**
- [ ] Screen transitions: Compose AnimatedContent
- [ ] List animations: animateItemPlacement() in LazyColumn
- [ ] Chart animations: Vico's built-in animation capabilities
- [ ] Button press animations: scale, color transition
- [ ] Add animations to Theme.kt (standardized durations)
- [ ] Use Lottie for complex animations (if needed)

**Testing**
- [ ] Test: Navigate between screens, smooth transition
- [ ] Test: Add item to list, item animates in
- [ ] Test: Charts update with animation

**Closure**
- [ ] Commit: "Feature 7.3: Cool animations"

### Feature 7.4: Haptic Feedback

**Planning**
- [ ] Vibration feedback on:
  - Button press (light tap)
  - Success action (confirmed)
  - Error (denied)

**Implementation**
- [ ] Create `util/HapticFeedbackHelper.kt`:
  ```kotlin
  fun Context.hapticLight()
  fun Context.hapticMedium()
  fun Context.hapticHeavy()
  ```
- [ ] Use Android's `VibrationEffect` (requires VIBRATE permission)
- [ ] Integrate into:
  - Button presses in QuickAddExpense
  - Success toast after save
  - Error dialog on failed action
- [ ] Settings: toggle haptic feedback on/off

**Testing**
- [ ] Test: Perform actions, feel haptic feedback
- [ ] Test: Toggle setting disables feedback

**Closure**
- [ ] Commit: "Feature 7.4: Haptic feedback"

### Feature 7.5: Micro-Interactions

**Planning**
- [ ] Subtle UI refinements that delight:
  - Swipe to delete gesture (with confirmation)
  - Pull-to-refresh on transaction list
  - Empty state illustrations
  - Loading skeleton screens

**Implementation**
- [ ] SwipeToDismiss on transaction cards
- [ ] LazyColumn pull-to-refresh (Compose refresh)
- [ ] Create illustration assets (SVG or Lottie)
- [ ] SkeletonLoader Composable while data loads

**Testing**
- [ ] Test: Swipe to delete works
- [ ] Test: Pull to refresh reloads data
- [ ] Test: Skeleton appears, then replaced by data

**Closure**
- [ ] Commit: "Feature 7.5: Micro-interactions"

### Feature 7.6: Home Screen Widgets

**Planning**
- [ ] Widget displays:
  - Quick balance overview (all accounts)
  - Recent transactions (last 5)
  - Monthly spending summary
- [ ] Tap to open app or specific screen
- [ ] Updates daily (or on app open)

**Implementation**
- [ ] Create `widget/BalanceWidget.kt` (Glance AppWidget)
  - Fetches current account balances from repositories
  - Displays in a simple, card-based layout
  - Uses theme colors
- [ ] Create `widget/RecentTransactionsWidget.kt`
  - Fetches last 5 transactions
  - Shows description, amount, date
- [ ] Create `widget/SpendingSummaryWidget.kt`
  - Current month total spending
  - Previous month for comparison
- [ ] Register all widgets in `AndroidManifest.xml`
- [ ] Create widget layout XMLs in `res/layout/`
- [ ] Widget updates via `WorkManager` background job (daily)
  - Job queries repositories and updates widget data

**Important: Widgets are separate from main UI layer**
- Widgets access repositories directly (no ViewModel)
- Widget data load must be fast (no heavy computation)
- Widget state is read-only, isolated from main app

**Testing**
- [ ] Test: Widget displays on home screen
- [ ] Test: Widget data updates
- [ ] Test: Tap widget opens app (intent)
- [ ] Test: Multiple widgets can be added
- [ ] Test: Widget survives app uninstall and reinstall

**Closure**
- [ ] Create `docs/decisions/ADR-010-Widget-Architecture.md`
- [ ] Commit: "Feature 7.6: Home screen widgets"

### Phase 7 Closure

- [ ] Test all v4.0.0 features across light/dark themes, all languages
- [ ] Verify no architectural drift
- [ ] Update all companion files
- [ ] Full weekly architecture review
- [ ] Commit: "Phase 7 complete: v4.0.0 polish features"

---

## PHASE 8: Testing, Refinement & Polish (Weeks 19-20)

### Comprehensive Testing

- [ ] Unit tests for all repository functions
- [ ] Integration tests for critical flows:
  - Create account → add transaction → see in analysis
  - Create repeat expense → next occurrence generated
  - Transfer between accounts → two transactions created
  - Import CSV → all rows committed atomically
- [ ] Scenario tests (manual checklists in `SCENARIOS.md`):
  - New user install → app startup
  - Add 50 transactions → performance
  - Toggle dark mode → all screens render
  - Change language → all strings updated
  - Delete account with transactions → cascade behavior
  - Use all widgets simultaneously → no crashes

### Data Integrity Tests

- [ ] Database migration tests (if any were added)
- [ ] Export/import round-trip (export then import, compare)
- [ ] Backup recovery (if backup feature added)

### Performance Tests

- [ ] List rendering with 1000+ transactions
- [ ] Search speed
- [ ] Chart rendering time
- [ ] App startup time (target: <2s)

### Accessibility Testing

- [ ] Keyboard navigation on all screens
- [ ] Color contrast ratios (WCAG AA)
- [ ] Font scaling at 1.25x and 1.5x

### Bug Fixes & Refinement

- [ ] Compile list of issues from testing
- [ ] Fix bugs (following root cause protocol from WorkOS_VibeCoding_Fixes.md)
- [ ] Polish UI (spacing, alignment, colors)
- [ ] Final theme review (colors, fonts, shapes match across all screens)

### Governance Review

- [ ] Final review of ARCHITECTURE.md — all rules followed
- [ ] Final review of UTILITIES.md — complete inventory
- [ ] Final review of DEPENDENCIES.md — justified and needed
- [ ] Final review of ADRs — all decisions documented
- [ ] Update CHANGELOG_INTERNAL.md with final summary

### Documentation

- [ ] Create user-facing README.md
- [ ] Create developer README (for future maintainers or forks)
- [ ] Document any known limitations

### Closure

- [ ] Final git commit: "Phase 8 complete: Testing, refinement, polish"
- [ ] Tag version: `v1.0.0-release`

---

## PHASE 9: Release & Post-Launch (Week 21+)

### Pre-Release Checklist

- [ ] Final build, signed APK
- [ ] Test on multiple devices (at minimum: small phone, large phone, tablet)
- [ ] All strings translated and verified
- [ ] Permissions minimal and justified
- [ ] No hardcoded credentials or debug logs
- [ ] Analytics or crash reporting (optional, but if included, must be offline-compatible)

### Release

- [ ] Upload to Google Play Store (if publishing) OR
- [ ] Distribute as APK / sideload link (if staying private)
- [ ] Write release notes summarizing v1.0.0 features

### Post-Launch

- [ ] Monitor for crashes (if telemetry enabled)
- [ ] Gather user feedback (if any users)
- [ ] Plan v2.0.0 features based on feedback
- [ ] Maintain security: run `npm audit` / dependency updates regularly
- [ ] New architecture review cadence: monthly (vs. weekly during development)

### Ongoing Governance

- [ ] ARCHITECTURE.md remains the law
- [ ] All new sessions follow the Session Protocol (Phase 1 of WorkOS_VibeCoding_Fixes.md)
- [ ] Companion files kept up-to-date (UTILITIES.md, CHANGELOG_INTERNAL.md, etc.)
- [ ] ADRs written for all significant decisions
- [ ] Monthly architecture review (half-day, reviewing the four comprehension questions)

---

## Key Governance Documents (Must Maintain)

| Document | Purpose | Updated |
|----------|---------|---------|
| ARCHITECTURE.md | Layer rules, hard constraints | Before breaking any rule |
| SCHEMA.md | Current data model | When entities change |
| UTILITIES.md | Shared abstractions inventory | When new utility/component added |
| DEPENDENCIES.md | Approved/banned packages | When adding dependency |
| CHANGELOG_INTERNAL.md | Feature-by-feature comprehension log | After each Antigravity session |
| DONE.md | Definition of done checklist | Consulted before closing feature |
| docs/decisions/ADR-*.md | Architecture decision records | Before breaking ARCHITECTURE.md |
| [module]/README.md | Module-level context | Created for major modules |

---

## Timeline Summary

| Phase | Duration | Key Deliverables |
|-------|----------|-----------------|
| Phase 0 | Weeks 1-2 | ARCHITECTURE.md, SCHEMA.md, UTILITIES.md, project setup |
| Phase 1 | Weeks 2-3 | Design tokens, theme, components |
| Phase 2 | Weeks 3-4 | Database schema, repositories, DAOs |
| Phase 3 | Week 4 | Navigation, screen templates |
| Phase 4 | Weeks 5-8 | v1.0.0: accounts, quick add, transactions, analysis, transfers, import/export |
| Phase 5 | Weeks 9-12 | v2.0.0: debt, repeat expenses, goals, insights |
| Phase 6 | Weeks 13-15 | v3.0.0: investments, budgets |
| Phase 7 | Weeks 16-18 | v4.0.0: font scale, languages, animations, haptics, widgets |
| Phase 8 | Weeks 19-20 | Testing, refinement, polish |
| Phase 9 | Week 21+ | Release and post-launch |

**Total: ~21 weeks for full feature-complete app, ready for launch.**

---

## Preventing the Vibe Coding Problems

Every task is designed to avoid the 20 vibe coding issues:

1. ✅ **Problem 1 (Features faster than comprehension)** → CHANGELOG_INTERNAL.md after every session
2. ✅ **Problem 2 (Architectural drift)** → ARCHITECTURE.md enforced in every prompt
3. ✅ **Problem 3 (Optimizes for current request)** → Constraints wrapped around every feature
4. ✅ **Problem 4 (Bug fixes create new bugs)** → Root cause protocol in DONE.md
5. ✅ **Problem 5 (Works but edge cases fail)** → DONE.md checklist before closing
6. ✅ **Problem 6 (Combinatorial testing problem)** → Scenario tests in SCENARIOS.md
7. ✅ **Problem 7 (AI tests give false confidence)** → Spec-first testing approach
8. ✅ **Problem 8 (Refactoring becomes hard)** → Small, bounded refactors; REFACTORING_LOG.md
9. ✅ **Problem 9 (Data migrations terrifying)** → Migrations from day one, SCHEMA.md, ADRs
10. ✅ **Problem 10 (AI adds dependencies)** → DEPENDENCIES.md approval gate
11. ✅ **Problem 11 (Duplicate abstractions)** → UTILITIES.md enforces single implementation
12. ✅ **Problem 12 (AI loses the why)** → ADRs provide rationale for all decisions
13. ✅ **Problem 13 (Context bottleneck)** → Module-level README.md + focused prompts
14. ✅ **Problem 14 (Long sessions drift)** → Session protocol: one task, one commit
15. ✅ **Problem 15 (UI consistency)** → Design tokens enforced at theme level
16. ✅ **Problem 16 (Performance becomes architectural)** → PERFORMANCE.md (if needed)
17. ✅ **Problem 17 (Concurrency nightmare)** → Clear operation ordering in ARCHITECTURE.md
18. ✅ **Problem 18 (Security becomes hard)** → Security rules in ARCHITECTURE.md
19. ✅ **Problem 19 (AI optimizes for code quantity)** → Minimum viable implementation rule
20. ✅ **Problem 20 (You become the compiler)** → Weekly architecture review + architect mindset

---

## Session Template for Antigravity (Use for Every Session)

```
# Session Goal
[One sentence: what this session will accomplish]

# Context Files to Use
- ARCHITECTURE.md (section: [X])
- [relevant ADRs]
- [relevant module README.md]

# Task Scope
- Create/modify these files: [list]
- Do not touch: [list of off-limits files]

# Definition of Done for This Session
- [ ] Feature implemented per ARCHITECTURE.md
- [ ] UTILITIES.md updated if applicable
- [ ] At least one test written
- [ ] Antigravity explains what changed and why
- [ ] Code reviewed by human before commit

# Post-Session Checklist
- [ ] Answer comprehension questions (from Phase 0 Problem 1)
- [ ] Update CHANGELOG_INTERNAL.md
- [ ] Verify no architectural drift
- [ ] Git commit with message: "[feature name]: [brief description]"
```

---

**This is your roadmap. Governance first. AI builds. You govern. The codebase stays comprehensible and maintainable at every stage.**
