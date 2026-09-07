# Finance App — Architecture Rules

> This document is the **law** of the project, not a suggestion.
> Every Antigravity prompt must include or reference the relevant section from this file.
> No architectural pattern may be introduced, and no rule below may be broken,
> without first writing an Architecture Decision Record (ADR) in `docs/decisions/`.

---

## Stack (locked — do not deviate without an ADR)

| Role | Technology |
|------|-----------|
| Language | Kotlin |
| UI Framework | Jetpack Compose |
| Local Database | Room (SQLite) |
| State Management | ViewModel + StateFlow |
| Dependency Injection | Hilt |
| Home Screen Widgets | Jetpack Glance |
| Preferences / Settings | DataStore |
| Charts | Vico |
| Animations | Compose Animations + Lottie |
| Import / Export | OpenCSV |
| Multi-language | Android String Resources |

---

## Layer Order (strict — no skipping layers)

```
UI Layer (Composables / Screens)
  ↓  via UiState / StateFlow only — no direct DB or repository calls from UI
ViewModel Layer
  ↓  via suspend functions or Flow
Repository Layer
  ↓  via Room DAO interfaces only
Data Layer (Room DAO + DataStore)
  ↓
SQLite Database / DataStore Files
```

**Widget layer is separate and does not use ViewModel:**

```
Glance AppWidget
  ↓  direct repository access only
Repository Layer
  ↓
Data Layer
```

---

## Package / Folder Structure

```
app/
├── data/
│   ├── db/                  # Room database class, DAOs, Entity classes
│   ├── datastore/           # DataStore keys and accessor definitions
│   ├── repository/          # Repository interface + implementations
│   └── model/               # Shared domain model data classes (NOT entities)
├── ui/
│   ├── screens/             # One sub-folder per screen (transactions/, analysis/, etc.)
│   ├── components/          # Shared reusable Composables only
│   ├── theme/               # Theme.kt, Color.kt, Type.kt, Shape.kt, Spacing.kt
│   └── navigation/          # NavHost, route definitions, nav graph
├── widget/                  # ALL Glance widget code lives here — nowhere else
├── util/                    # Shared pure utilities (formatters, extensions, constants)
└── di/                      # Hilt modules
```

---

## Hard Rules

### UI / Composable Rules
- Composables **may not** directly call DAOs, repositories, Room, or DataStore.
- All data reaches the UI through `ViewModel` → `StateFlow<UiState>` only.
- No business logic inside Composables. They display state and emit user events — nothing else.
- Shared UI elements (buttons, cards, input fields, dialogs) live in `ui/components/`. Do not create duplicates inside screen folders.
- Every color, spacing, shape, and font size value must reference the theme. **No hardcoded values anywhere.**
- No user-visible string may be hardcoded in a Composable. All text goes through `stringResource()` for multi-language support.
- Haptic feedback is triggered from the ViewModel or a dedicated utility — not ad-hoc inside Composables.

### ViewModel Rules
- One ViewModel per screen or major feature section.
- ViewModels do **not** hold a reference to Context (only Application context via Hilt if absolutely needed).
- ViewModels do **not** directly access DAOs. They go through repositories only.
- All UI state is exposed as a single `data class UiState` via `StateFlow`. No scattered individual `LiveData` or `MutableState` properties at the ViewModel level.

```kotlin
// Correct pattern — always use a single UiState
data class TransactionUiState(
    val transactions: List<TransactionDisplayItem> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null
)
```

### Repository Rules
- One repository per data domain: `TransactionRepository`, `AccountRepository`, `GoalRepository`, etc.
- Repositories are the **only** layer that accesses Room DAOs or DataStore.
- Repositories return `Flow<T>` for observable / live data, and `suspend fun` for one-shot write operations.
- No display formatting, currency symbols, or rounding logic inside repositories. That belongs in ViewModel or a formatter utility.
- Repositories do not know about the UI layer — they have no Compose or Android UI imports.

### Database Rules
- Every table or column change requires a **Room migration file**. `fallbackToDestructiveMigration()` is **banned**.
- All monetary amounts stored as `Long` in the **smallest currency unit** (e.g., paise, cents). Never `Double` or `Float` — floating-point is unsuitable for money.
- All dates and times stored as `Long` (Unix timestamp, milliseconds).
- Room `@Entity` classes live in `data/db/` only. Do not use entity classes as UI models — create separate model classes in `data/model/`.
- Every schema change must update `SCHEMA.md` on the same day it is written.

### Widget Rules
- All Glance widget code lives exclusively in the `widget/` package. No widget logic anywhere else.
- Widgets access data through repositories directly — no ViewModel, no UI layer.
- Widget data must load fast. No heavy computation, sorting of thousands of rows, or blocking calls inside a widget update.
- Widget state is minimal — only the data actually shown on the home screen widget.

### Navigation Rules
- Single Activity architecture. No Fragments.
- All navigation handled through Jetpack Navigation Compose in `ui/navigation/`.
- Screen routes defined as a `sealed class` or `object` in one file — not scattered across screen files.

### Dependency Rules
- No new library may be added without an entry in `DEPENDENCIES.md` including full justification.
- No dependency that requires a network connection for its core function. This app is **fully offline**.
- No dependency for a problem that can be solved in under 50 lines of Kotlin. Write the code instead.

---

## What Antigravity Must NEVER Do

- Add any network call, HTTP client, or internet permission.
- Add authentication, login, or user account logic.
- Store monetary values as `Double` or `Float`.
- Use `SharedPreferences` — use DataStore instead.
- Use `fallbackToDestructiveMigration()`.
- Call a DAO or repository directly from a Composable.
- Create a new shared Composable without checking `ui/components/` first.
- Hardcode any color, spacing, shape, or font size outside the theme.
- Hardcode any user-visible string — use `strings.xml` and `stringResource()`.
- Add sync, cloud storage, server, or remote logic of any kind.
- Introduce a new state management pattern (no Redux, MVI, etc. — MVVM only).
- Add a new dependency without adding it to `DEPENDENCIES.md` first.

---

## Theme and Design Tokens

The theme is defined in `ui/theme/` and contains:

- `Color.kt` — all color definitions for light and dark theme
- `Type.kt` — all text styles (no hardcoded `sp` values in Composables)
- `Shape.kt` — all corner radius values
- `Spacing.kt` — all spacing values as a custom `Spacing` class
- `Theme.kt` — the root `MaterialTheme` wrapper with light/dark switching

**Dual theme rule:** The app must support light and dark mode from day one. All colors must use `MaterialTheme.colorScheme.*` — no hardcoded hex values in Composables.

**Font size rule:** The "font size increase" feature is handled via a font scale setting stored in DataStore and applied at the root theme level — individual Composables never hardcode `sp` sizes.

---

## Monetary Calculation Rule

All monetary arithmetic uses `Long` values in the smallest currency unit.
Conversion to display format (e.g., `1050` → `₹10.50`) happens only in the formatter utility in `util/`.
The formatter utility is the **single place** where currency symbols, decimal formatting, and locale-specific display happen.

**There is only one currency formatter. Do not create a second one.**

---

## Multi-language Rule

All user-visible strings live in `res/values/strings.xml` (and corresponding `res/values-{lang}/strings.xml` for other languages).
No string literal may appear inside a Composable.
New strings are added to the default `strings.xml` immediately when a screen is built — not deferred.

---

## Performance Rules

- Any list that can contain more than 50 items **must** use `LazyColumn` or `LazyRow`. Never a `Column` with a `forEach` loop for unbounded data.
- No database query runs on the main thread. All Room calls are `suspend` or return `Flow`.
- Analysis and chart data must be pre-aggregated at the repository layer before being passed to the UI — Composables do not calculate totals or averages.
- Widget updates must complete within Android's background execution time limits. Heavy pre-processing is not permitted inside a widget updater.

---

## Data Integrity Rules

- Deleting an **account** must define explicit behavior for all related transactions (cascade delete or block delete — document decision in an ADR before implementing).
- **Repeat expenses** are generated lazily (one future occurrence at a time), not all at once on app install.
- **Import** must validate every row before committing any row. A failed import commits nothing — it is all-or-nothing.
- **Export** reads a snapshot of data atomically — no partial reads mid-transaction.
- **Transfers** between accounts are stored as two linked transaction records (one debit, one credit) with a shared transfer ID — never as a single record.
- **Debt and loan** entries track direction (money given vs. money owed), the counterparty, and a settled flag — never deleted when settled, only marked.
- **Goals** track a target amount, a deadline, and a linked account or dedicated balance — the logic for calculating progress lives in the repository, not the UI.

---

## Companion Files to Keep Updated

| File | Updated when |
|------|-------------|
| `SCHEMA.md` | Every database entity or column change |
| `UTILITIES.md` | Every new shared utility, formatter, extension, or component |
| `DEPENDENCIES.md` | Every new library added to `build.gradle` |
| `CHANGELOG_INTERNAL.md` | After every feature session — what changed and why |
| `docs/decisions/ADR-*.md` | Before any rule in this document is broken or changed |
| `DONE.md` | Checklist consulted before closing any feature |

---

## The Core Principle

> Antigravity builds. You govern.
>
> Antigravity's job is to implement what is specified.
> Your job is to specify it correctly, review what was built, and enforce these rules.
> Never let Antigravity invent the architecture while implementing a feature.
