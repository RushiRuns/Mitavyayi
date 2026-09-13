# ADR-012: Need vs. Want Classification Field on Transactions

## Status
Accepted

## Context
A core tenet of mindful personal finance (and the 50/30/20 budgeting rule) is distinguishing between non-negotiable living essentials ("Needs") and discretionary lifestyle expenditures ("Wants").
Previously, transactions only tracked category, amount, timestamp, account, and notes. Without a first-class classification for Need vs. Want, users could not analyze what portion of their spending was discretionary versus essential, hindering effective budget discipline and savings optimization.

## Decision
1. **Schema Addition**: Add an `isNeed: Boolean` column to the `transactions` table.
   - Column configuration: `@ColumnInfo(defaultValue = "1") val isNeed: Boolean = true`
   - In SQLite: `INTEGER NOT NULL DEFAULT 1`
   - Bumps `AppDatabase` version from 2 to 3 with `MIGRATION_2_3`.
   - Banned destructive migrations (`fallbackToDestructiveMigration()`) remain strictly forbidden.
2. **Default Behavior**:
   - Default value is `true` ("Need") to maintain conservative, non-disruptive migration semantics for existing transactions.
   - For income transactions and inter-account transfers, `isNeed` defaults to `true` and the UI toggle is hidden.
3. **UI Integration**:
   - **Quick Add Expense**: Presents a two-option toggle row ("Need" / "Want") situated between Category and Description, visible only in Expense mode.
   - **Transaction Detail**: Displays a "Need" or "Want" suggestion chip in the metadata row for expense transactions.
   - **Edit Transaction**: Enables updating `isNeed` when modifying existing expenses.
   - **Batch Add**: Includes a Need/Want toggle per transaction row.
   - **Analysis Screen**: Incorporates a "Spending: Need vs. Want" donut chart card positioned directly above "Spending by Category", reusing `CategoryDonutChart` with teal and amber segments.

## Consequences
- Existing transactions automatically migrate to `isNeed = true` with zero data loss or database reset.
- Users gain instant visibility into essential vs. discretionary spending ratios across weekly, monthly, and yearly time horizons.
- Aggregations remain reactive and performant using date-range indexed queries.
