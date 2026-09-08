# ADR-003: Lazy Occurrence Generation for Recurring Expenses

## Status
Accepted

## Context
Personal finance management includes recurring commitments such as daily transit, weekly allowances, monthly subscriptions, and annual premiums. Eagerly pre-generating hundreds of future occurrences bloats the database, distorts current expense metrics and cash balances, and complicates cancellation or adjustments of future recurrences.

## Decision
Recurring expenses (`RepeatExpense`) generate concrete `Transaction` records **lazily**—strictly one occurrence at a time when the due date has arrived.

The `RepeatExpense` entity stores `frequency` (DAILY, WEEKLY, MONTHLY, YEARLY) and `lastGenerated` timestamp. At application startup or during repository synchronization, `RepeatExpenseRepository.generateNextOccurrence()` evaluates whether `currentTime >= nextDueDate`. If due:
1. It creates and inserts exactly one corresponding `Transaction` into the ledger.
2. It advances `lastGenerated` on the `RepeatExpense` to the newly generated occurrence timestamp.
3. If not yet due, it returns `null` and performs no database write.

## Consequences
- Database size scales strictly with real historical activity, avoiding unbounded pre-populated tables.
- Deactivating an expense (`isActive = false`) immediately stops future generation without needing to purge speculative entries.
- UI layer requests or triggers synchronization upon startup or view refresh.
