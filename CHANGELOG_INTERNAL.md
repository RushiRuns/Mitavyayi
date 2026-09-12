# Internal Changelog

This document tracks progress after each development session. 

## [Unreleased]
- Phase 3: Navigation structure and screen templates.
  - Implemented sealed navigation routing in `ui/navigation/NavDestinations.kt` (`TransactionList`, `Analysis`, `Accounts`).
  - Implemented single `MitavyayNavHost` in `ui/navigation/NavHost.kt` without Fragments.
  - Implemented `MitavyayAppState` state holder in `ui/AppState.kt` managing route tracking, top-level tab state saving, and restoration.
  - Implemented `MitavyayBottomBar` in `ui/components/BottomNavBar.kt` using theme tokens.
  - Implemented `MitavyayApp` in `ui/MitavyayApp.kt` root scaffold with TopAppBar, BottomNavBar, and FloatingActionButton.
  - Implemented screen templates and `@HiltViewModel`s for `TransactionList`, `Analysis`, and `Accounts` in `ui/screens/`.
  - Implemented `MainViewModel` and integrated dynamic theme/font scaling with `MitavyayApp` in `MainActivity.kt`.
  - Locked `androidx-navigation-compose` v2.7.7 in `libs.versions.toml` and documented in `DEPENDENCIES.md`.
  - Created architectural decision record `docs/decisions/ADR-004-Single-Activity-Navigation.md`.
  - Added unit tests for navigation destinations and ViewModels in `ui/navigation/NavigationTest.kt` and `ui/screens/ViewModelsTest.kt`.
  - Updated `UTILITIES.md`.
- Phase 2: Core database schema and repositories complete.
  - Implemented Room entities in `data/db/` (`Transaction.kt`, `Account.kt`, `Transfer.kt`, `Goal.kt`, `Debt.kt`, `RepeatExpense.kt`, `Category.kt`).
  - Implemented Room DAOs in `data/db/` (`TransactionDao.kt`, `AccountDao.kt`, `GoalDao.kt`, `DebtDao.kt`, `RepeatExpenseDao.kt`, `CategoryDao.kt`).
  - Created `AppDatabase.kt` (v1, no destructive migrations allowed) and `DatabaseTransactionRunner.kt`.
  - Implemented repositories in `data/repository/` (`TransactionRepository.kt`, `AccountRepository.kt`, `TransferRepository.kt`, `GoalRepository.kt`, `DebtRepository.kt`, `RepeatExpenseRepository.kt`, `CategoryRepository.kt`).
  - Implemented DataStore preferences in `data/datastore/` (`PreferenceKeys.kt`, `PreferencesRepository.kt`).
  - Implemented UI display models in `data/model/` with pure mapping functions ensuring Room entities are never exposed directly to UI.
  - Implemented Hilt dependency injection in `di/` (`DatabaseModule.kt`, `RepositoryModule.kt`, `DataStoreModule.kt`), `MitavyayApplication.kt`, and `MainActivity.kt`.
  - Documented architectural decisions in `docs/decisions/` (`ADR-001-Monetary-Storage-Long.md`, `ADR-002-Transfers-As-Dual-Records.md`, `ADR-003-Repeat-Expenses-Lazy-Generation.md`).
  - Created unit tests verifying Long monetary values, dual-record atomic transfers, and lazy recurring expense generation.
  - Updated `SCHEMA.md` and `UTILITIES.md`.
- Phase 1: Design system and theme defined. All tokens centralized.
  - Implemented Design Tokens in `ui/theme/` (`Color.kt`, `Type.kt`, `Shape.kt`, `Spacing.kt`, `Theme.kt`).
  - Implemented Design System Components in `ui/components/` (`Buttons.kt`, `Cards.kt`, `Inputs.kt`, `Dialogs.kt`, `Spacing.kt`, `States.kt`, `CurrencyInput.kt`, `DatePicker.kt`).
  - Implemented canonical formatters in `util/Formatters.kt` (`CurrencyFormatter` with Indian Lakhs/Crores grouping without floats, `DateTimeFormatter`).
  - Created `docs/decisions/ADR-001-Color-Palette.md` documenting palette rationale and WCAG AA contrast compliance.
  - Created comprehensive unit test suites (`ThemeTokensTest.kt`, `FormattersTest.kt`).
  - Updated `UTILITIES.md` with complete component and utility index.
- Initialized Phase 0 governance documentation (`SCHEMA.md`, `UTILITIES.md`, `DEPENDENCIES.md`, `DONE.md`, `CHANGELOG_INTERNAL.md`, ADR-000).
