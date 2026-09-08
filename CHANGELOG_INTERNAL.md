# Internal Changelog

This document tracks progress after each development session. 

## [Unreleased]
- Phase 1: Design system and theme defined. All tokens centralized.
  - Implemented Design Tokens in `ui/theme/` (`Color.kt`, `Type.kt`, `Shape.kt`, `Spacing.kt`, `Theme.kt`).
  - Implemented Design System Components in `ui/components/` (`Buttons.kt`, `Cards.kt`, `Inputs.kt`, `Dialogs.kt`, `Spacing.kt`, `States.kt`, `CurrencyInput.kt`, `DatePicker.kt`).
  - Implemented canonical formatters in `util/Formatters.kt` (`CurrencyFormatter` with Indian Lakhs/Crores grouping without floats, `DateTimeFormatter`).
  - Created `docs/decisions/ADR-001-Color-Palette.md` documenting palette rationale and WCAG AA contrast compliance.
  - Created comprehensive unit test suites (`ThemeTokensTest.kt`, `FormattersTest.kt`).
  - Updated `UTILITIES.md` with complete component and utility index.
- Initialized Phase 0 governance documentation (`SCHEMA.md`, `UTILITIES.md`, `DEPENDENCIES.md`, `DONE.md`, `CHANGELOG_INTERNAL.md`, ADR-000).
