# ADR-007: Debt Records Are Immutable and Never Deleted

## Status
Accepted

## Context
Debts and loans represent peer-to-peer financial obligations (money lent to colleagues, friends, or family, or money borrowed from them). 
Unlike ordinary transactional expenses which may occasionally be entered erroneously and deleted, debt records constitute an ongoing ledger of commitments.
Allowing users or system operations to delete debt records leads to severe data integrity issues:
1. Historical financial audit trails are broken, making it impossible to reconcile past money flows.
2. Inadvertent or intentional deletion destroys the record of who owes money to whom.
3. Once settled, deleting the record prevents future reference when reviewing personal lending history or resolving disputes.

## Decision
All debt and loan entries in Mitavyay are **strictly immutable and never deleted**:
1. **Mechanical Enforcement**: Neither `DebtDao` nor `DebtRepository` defines a `delete()` or `deleteById()` function. Room schema has no cascading delete triggers on debts.
2. **Settlement Lifecycle**: When a debt is paid back or cleared, it is transitioned to a settled state by assigning a non-null `settledAt` timestamp (`markSettled(id, timestamp)`).
3. **UI Exclusion**: The user interface does not display or expose any "Delete" action or button for debts. Active debts display a "Mark as Settled" action. Settled debts display their settlement date and remain permanently viewable in the "Settled" view.

## Consequences
- Total auditability: Users preserve a complete, indisputable lifetime history of all money lent and borrowed.
- Code clarity: Eliminates all edge cases around dangling references, cascade deletions, or accidental record loss.
- UI simplicity: User interactions are limited to creation and settlement.
