# ADR-002: Transfers Stored as Dual Linked Transaction Records

## Status
Accepted

## Context
When a user moves funds from one account to another (e.g. from Bank Account A to Cash Account B), the system needs to record both the deduction and the addition. If stored as a single `Transfer` entity, queries for account-specific transaction history, ledger balances (`SUM(amount) WHERE accountId = ...`), and category-based spending breakdown would require complex, slow `UNION` queries and special-casing throughout the application.

## Decision
Every transfer between two accounts is stored as two distinct, linked `Transaction` records sharing a unique `transferId`:
1. **Debit Transaction**: `accountId = fromAccountId`, `amount = -amountPaise` (negative), `transferId = <shared_uuid>`
2. **Credit Transaction**: `accountId = toAccountId`, `amount = +amountPaise` (positive), `transferId = <shared_uuid>`

Both records are committed or deleted atomically within a single database transaction via `TransferRepository` and Room's `@Transaction` / `withTransaction`.

## Consequences
- Account ledger queries (`SELECT * FROM transactions WHERE accountId = :id`) remain standard and uniform with zero transfer-specific joins or conditional branches.
- Account balance calculations (`SELECT SUM(amount) FROM transactions WHERE accountId = :id`) are universally consistent.
- Deleting or editing a transfer must always update or remove both linked transaction records atomically.
