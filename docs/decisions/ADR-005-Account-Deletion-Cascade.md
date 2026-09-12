# ADR-005: Account Deletion and Ledger Integrity Policy

## Status
Accepted

## Context
In Mitavyay, financial transactions and inter-account transfers are immutable ledger entries. An account may have regular expense/income transactions or participate in inter-account transfers (which consist of two linked transactions across two accounts sharing a `transferId` per ADR-002).

Allowing arbitrary hard deletion of an active account with historical transactions risks two critical failure modes:
1. Deleting an account and cascading delete on its transactions would wipe out historical spending data, distort category statistics, and orphan linked transfer legs on the other account.
2. Deleting an account but leaving its transactions orphaned violates foreign key integrity (`transactions.accountId` pointing to a nonexistent account).

## Decision
Mitavyay adopts a **Conditional Deletion & Archiving Policy**:

1. **Clean Accounts (0 Transactions)**:
   - If an account has **zero associated transactions**, it may be **permanently deleted** (hard delete) from the database via `AccountDao.deleteById()`.
2. **Accounts with Transaction History (1+ Transactions)**:
   - If an account has associated transactions or transfers, hard deletion is **blocked**.
   - Instead, the app prompts the user to **Archive** the account (`isActive = false`).
   - Archived accounts retain all ledger history, remain visible in historical reports and analysis, but are hidden from daily transaction entry dropdowns and marked as `Inactive` in account listings.
   - If a user truly wishes to remove an archived account, they can reactivate or keep it archived safely.

## Consequences

### Positive
- **Ledger Invariant Preserved**: Transfer pairs (`ADR-002`) remain completely balanced and verifiable.
- **Historical Reporting Intact**: Spending breakdowns, cash flow totals, and past records remain accurate without retroactive corruption.
- **Accidental Loss Prevention**: Users cannot accidentally destroy their entire transaction history by tapping delete on an account.

### Negative / Trade-offs
- Users cannot instantly purge an account that has transactions with a single click; they must archive it or manually clear transactions if they want a clean state.
