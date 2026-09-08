# ADR-001-B: Monetary Values Stored as Long (Paise/Cents)

## Status
Accepted

## Context
Financial applications require absolute precision in accounting calculations, aggregations, account balances, and reporting. Floating-point numbers (`Float`, `Double`) in IEEE 754 format introduce rounding and representation errors (e.g. `0.1 + 0.2 = 0.30000000000000004`), which over time corrupt transaction ledgers, balance statements, and audit consistency.

## Decision
All monetary values across the entire codebase—database entities, DAO queries, domain repositories, business logic, and calculations—must be stored and manipulated strictly as `Long` integers in the smallest currency unit (paise for INR: 1 INR = 100 paise; cents for USD).

- `10.50` is stored as `1050L`.
- `-15.00` (expense) is stored as `-1500L`.
- `0.05` is stored as `5L`.

Conversion between user-facing decimal strings and `Long` paise occurs exclusively in `com.rushi.mitavyay.util.CurrencyFormatter` without using floating-point intermediaries.

## Consequences
- Floating-point numeric types (`Float`, `Double`, `BigDecimal`) are strictly banned from Room entity columns and repository return signatures.
- Rounding drift is completely eliminated.
- Database queries using `SUM(amount)` operate with integer performance and zero cumulative precision degradation.
- Display in Composables requires explicit formatting via `CurrencyFormatter.format(amountPaise)`.
