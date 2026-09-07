# Shared Utilities & Components

This file serves as the index for all shared utilities, formatters, extensions, and reusable UI components. 

> **DO NOT CREATE** duplicate utilities. Check this list before writing new shared logic.

## Utilities & Formatters

- **Monetary Formatter**: Converts `Long` values (smallest currency unit) into readable `String` format (e.g., ₹ display). *Status: Pending*
- **Date/Time Formatter**: Converts `Long` (Unix timestamp) into human-readable date/time strings. *Status: Pending*
- **Currency Conversion Utilities**: Helper functions for currency processing (if applicable, entirely offline). *Status: Pending*
- **Transaction Categorization Logic**: Centralized logic for determining default or custom categories. *Status: Pending*

## Reusable UI Components
*(To be populated as components are created in `ui/components/`)*
- `TransactionCard`: *Pending*
- `AccountCard`: *Pending*
- `GoalCard`: *Pending*
- `TextField` wrappers: *Pending*
- `AlertDialog` wrappers: *Pending*

## DO NOT CREATE
- Do not create a second currency formatter. Use the centralized one.
- Do not create ad-hoc date formatting in UI components. Use the central Date/Time Formatter.
- Do not create custom spacer components per screen. Use theme spacing scales.
