# Database Schema & Data Model

This document details the complete data model, Room database specifications, constraints, and invariants for Mitavyay in plain language.

---

## Room Database Specifications

- **Database Class**: `com.rushi.mitavyay.data.db.AppDatabase`
- **Database File**: `mitavyay.db`
- **Schema Version**: 3 (Change 17: Need/Want Classification & Donut Chart)
- **Destructive Migrations**: **BANNED** (`fallbackToDestructiveMigration()` is forbidden by `ARCHITECTURE.md`).
- **Migrations**:
  - `MIGRATION_1_2`: Creates table `budgets` with indexes `index_budgets_monthYear` and unique composite index `index_budgets_category_monthYear`.
  - `MIGRATION_2_3`: Adds `isNeed` column (`INTEGER NOT NULL DEFAULT 1`) to `transactions` table.

---

## Entities & Tables

### 1. `transactions` (`Transaction.kt`)
Represents single ledger transactions (both regular expenses/incomes and transfer legs).
- `id`: `String` (Primary Key, UUID)
- `accountId`: `String` (Foreign reference to `accounts.id`, Indexed)
- `amount`: `Long` (Smallest currency unit, e.g. paise. Negative = Debit/Expense, Positive = Credit/Income)
- `description`: `String`
- `timestamp`: `Long` (Unix milliseconds, Indexed)
- `category`: `String` (Indexed)
- `tags`: `String` (JSON array string format, e.g. `["food", "dining"]`)
- `transferId`: `String?` (Indexed, non-null if this transaction is part of an inter-account transfer)
- `notes`: `String?`
- `isNeed`: `Boolean` (Indexed/ColumnInfo default `1`, true = Need, false = Want)

### 2. `accounts` (`Account.kt`)
Represents user financial accounts (cash, bank accounts, credit cards).
- `id`: `String` (Primary Key, UUID)
- `name`: `String`
- `type`: `String` (`cash`, `bank`, `credit`)
- `balance`: `Long` (Current balance in smallest currency unit)
- `currency`: `String` (Default `INR`)
- `createdAt`: `Long` (Unix milliseconds)
- `isActive`: `Boolean` (True if active, false if archived)

### 3. `transfers` (`Transfer.kt`)
Metadata tracking transfer movements between accounts.
- `id`: `String` (Primary Key, UUID, identical to `transactions.transferId`)
- `fromAccountId`: `String` (Source account)
- `toAccountId`: `String` (Destination account)
- `amount`: `Long` (Transfer magnitude in smallest currency unit)
- `timestamp`: `Long` (Unix milliseconds)
- `notes`: `String?`

### 4. `goals` (`Goal.kt`)
Represents user savings goals and financial targets.
- `id`: `String` (Primary Key, UUID)
- `name`: `String`
- `targetAmount`: `Long` (Target amount in paise)
- `deadline`: `Long` (Unix milliseconds)
- `currentAmount`: `Long` (Current saved amount in paise)
- `linkedAccountId`: `String?` (Optional foreign key to `accounts.id`)
- `category`: `String`
- `notes`: `String?`

### 5. `debts` (`Debt.kt`)
Tracks money lent to others or borrowed from others.
- `id`: `String` (Primary Key, UUID)
- `type`: `String` (`lent` or `borrowed`)
- `counterparty`: `String` (Person or entity)
- `amount`: `Long` (Magnitude in smallest currency unit)
- `createdAt`: `Long` (Unix milliseconds)
- `settledAt`: `Long?` (Null while active; timestamp when settled)
- `notes`: `String?`
- **Rule**: Never deleted from the database—only marked settled.

### 6. `repeat_expenses` (`RepeatExpense.kt`)
Configuration for recurring scheduled expenses.
- `id`: `String` (Primary Key, UUID)
- `description`: `String`
- `amount`: `Long` (Amount in smallest currency unit)
- `frequency`: `String` (`DAILY`, `WEEKLY`, `MONTHLY`, `YEARLY`)
- `lastGenerated`: `Long` (Unix milliseconds of the most recently generated occurrence)
- `category`: `String`
- `isActive`: `Boolean`

### 7. `categories` (`Category.kt`)
Predefined and custom user-created transaction categories.
- `id`: `String` (Primary Key)
- `name`: `String`
- `icon`: `String` (Icon identifier token)
- `color`: `String` (Hex color token)
- `isCustom`: `Boolean` (False for default seeded categories, true for user-defined. Note: as of Change 18, both default and custom categories can be freely edited and deleted by the user)

### 8. `budgets` (`Budget.kt`)
Monthly category budget allocations.
- `id`: `String` (Primary Key, UUID)
- `category`: `String` (Category identifier)
- `monthYear`: `String` (Format `yyyy-MM`, e.g. `2026-09`, Indexed)
- `amount`: `Long` (Budget allocation in smallest currency unit / paise)
- `isActive`: `Boolean` (Default `true`)
- **Index**: Indexed on `monthYear`
- **Unique Constraint**: Unique composite index on `(category, monthYear)`

---

## Relationships & Invariants

1. **Monetary Precision Rule**:
   - Every financial value (`amount`, `balance`, `targetAmount`, etc.) is strictly stored as `Long` in paise/cents.
   - Floats and Doubles are prohibited in entities, repositories, and calculation methods.
2. **Atomic Dual-Entry Transfers**:
   - A transfer consists of two linked `Transaction` records: one debit (`-amount`) on the source account, and one credit (`+amount`) on the target account, both sharing the same `transferId`.
   - Creation and deletion of transfers are executed atomically in a database transaction via `TransferRepository`.
3. **Debt Immutability**:
   - `DebtDao` intentionally has no `@Delete` method.
   - Debts are closed exclusively by updating `settledAt` with the resolution timestamp.
4. **Lazy Generation of Recurring Expenses**:
   - Recurring expenses are evaluated on demand via `RepeatExpenseRepository.generateNextOccurrence()`.
   - Occurrences are generated only after the current time meets or passes the next due date.
5. **Composables Isolation**:
   - Room Entities are NEVER directly used as UI state.
   - UI layers observe decoupled display models (`data/model/*DisplayItem.kt`).
6. **Budget Uniqueness per Category and Month**:
   - Budgets are scoped to a specific category and calendar month (`yyyy-MM`).
   - Composite unique index on `(category, monthYear)` guarantees a category has at most one active budget record per month.
   - Actual spending is calculated dynamically from ledger transactions within that month's millisecond timestamp boundaries, ensuring the budget entity remains clean and unpolluted by cached spending amounts.
