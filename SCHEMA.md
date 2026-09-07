# Database Schema

This document details the complete data model for the Finance App in plain language.

## Entities (Tables)

### 1. Transaction
- `id`: String (Primary Key)
- `accountId`: String (Foreign Key to Account)
- `amount`: Long (Smallest currency unit, e.g., paise/cents)
- `description`: String
- `timestamp`: Long (Unix milliseconds)
- `category`: String
- `tags`: String (JSON array)
- `transferId`: String? (Optional, links two transactions forming a transfer)
- `notes`: String?

### 2. Account
- `id`: String (Primary Key)
- `name`: String
- `type`: String (cash/bank/credit)
- `balance`: Long (Smallest currency unit)
- `currency`: String
- `createdAt`: Long (Unix milliseconds)
- `isActive`: Boolean

### 3. Transfer
- Tracks transfers between accounts. Represented as two linked `Transaction` records sharing the same `transferId`.
- `id`: String (Primary Key)
- `fromAccountId`: String
- `toAccountId`: String
- `amount`: Long (Smallest currency unit)
- `timestamp`: Long (Unix milliseconds)
- `notes`: String?

### 4. Goal
- `id`: String (Primary Key)
- `name`: String
- `targetAmount`: Long (Smallest currency unit)
- `deadline`: Long (Unix milliseconds)
- `currentAmount`: Long (Smallest currency unit)
- `linkedAccountId`: String? (Optional Foreign Key to Account)
- `category`: String
- `notes`: String?

### 5. Debt
- `id`: String (Primary Key)
- `type`: String (lent/borrowed)
- `counterparty`: String
- `amount`: Long (Smallest currency unit)
- `createdAt`: Long (Unix milliseconds)
- `settledAt`: Long? (Unix milliseconds, null if active)
- `notes`: String?

### 6. RepeatExpense
- `id`: String (Primary Key)
- `description`: String
- `amount`: Long (Smallest currency unit)
- `frequency`: String (DAILY/WEEKLY/MONTHLY/YEARLY)
- `lastGenerated`: Long (Unix milliseconds)
- `category`: String
- `isActive`: Boolean

### 7. Category
- `id`: String (Primary Key)
- `name`: String
- `icon`: String
- `color`: String
- `isCustom`: Boolean

### 8. Label
- `id`: String (Primary Key)
- `name`: String
- `color`: String

## Relationships & Constraints
- A `Transaction` always belongs to one `Account`.
- A `Transfer` is represented by exactly two `Transaction` records (one debit from `fromAccountId`, one credit to `toAccountId`).
- `Goal` can optionally be linked to a specific `Account`.

## Invariants (Must Never Break)
1. **Offline Only**: Data is strictly local. No cloud synchronization, no backend tables.
2. **Monetary Precision**: All monetary values are stored as `Long` in the smallest denomination (no floating-point types).
3. **Immutability of Debt**: Debt records are never deleted; they are only marked as settled by updating `settledAt`.
4. **Double-Entry Transfers**: Deleting or creating a transfer must atomically affect both associated `Transaction` records.
5. **No Destructive Migrations**: Room migrations must be meticulously written (`fallbackToDestructiveMigration` is banned).
