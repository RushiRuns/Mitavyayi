# ADR-006: All-or-Nothing CSV Import Policy

## Status
Accepted

## Context
When users migrate data between devices, restore backups, or import transactions from external spreadsheets, the data file may contain dozens or thousands of transaction rows. 
If an import process allows partial success (e.g. committing 50 rows, failing on row 51, and abandoning the remaining 49), the user's ledger is left in a corrupted, indeterminate state:
1. Account balances will be partially updated and inaccurate.
2. Dual linked transfer records may have only one leg committed, breaking double-entry accounting.
3. If the user attempts to correct the file and re-import, the first 50 rows will be duplicated unless complex, fragile deduplication heuristics are executed.

## Decision
All CSV imports in Mitavyay operate under a strict **All-or-Nothing (Atomic)** policy:
1. **Pre-commit Complete Validation**: Every row in the CSV file must be parsed and validated prior to database writes. Any formatting error (e.g. unparseable date, malformed numeric amount, missing category/account) terminates the import immediately.
2. **Detailed Error Reporting**: When validation fails, the user is provided with the exact row number and descriptive cause (e.g. `Row 14: Invalid amount format 'abc'`) so they can fix the source file.
3. **Atomic Transaction Scope**: All database writes (inserting transactions and updating the respective account balances) are wrapped inside a single database transaction via `DatabaseTransactionRunner` (`RoomDatabase.withTransaction`). If an unexpected database or system failure occurs midway, all changes are rolled back automatically. Zero rows are persisted.
4. **Account Auto-Provisioning**: If an imported row references an account that does not yet exist in the database (e.g. initial setup on a new device), the account is automatically created with initial balance `0` and adjusted according to the imported ledger rows.

## Consequences
- The ledger remains 100% consistent: either the entire dataset is imported and account balances accurately reconciled, or the database remains untouched.
- Users can safely fix the identified line in their CSV file and re-run the import without risk of duplicate transactions or inconsistent balances.
- Memory consumption during pre-validation is kept lightweight by streaming and mapping rows before batch insertion.
