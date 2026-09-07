# CLAUDE.md — Finance App
## AI System Prompt & Convention Rules

> This file is the **law** of this project.
> Paste the relevant section into every AI session before writing a single line of code.
> No rule below may be broken without first writing an ADR in `docs/decisions/`.
> You govern. The AI builds.

---

## 1. Who This App Is and What It Does

A **fully offline, personal finance Android app** built in Kotlin with Jetpack Compose.

Feature set by version:
- **v1.0.0** — Dual theme, Transactions, Analysis, Insights, Import/Export, Transfers, Accounts, Quick-add expense
- **v2.0.0** — Enhanced analysis, Debt & Loans, Repeat expenses, Notes, Multiple transactions, Goals
- **v3.0.0** — Investments, Budget
- **Latest** — Font size scaling, Multiple languages, Animations, Haptic feedback, Micro-interactions, Home screen Widgets

There is **no backend, no server, no sync, no authentication, no cloud**. Everything lives on the device.

---

## 2. Locked Tech Stack

Do not deviate from this stack without an ADR.

| Role                   | Technology                        |
|------------------------|-----------------------------------|
| Language               | Kotlin                            |
| UI Framework           | Jetpack Compose                   |
| Local Database         | Room (SQLite)                     |
| State Management       | ViewModel + StateFlow             |
| Dependency Injection   | Hilt                              |
| Home Screen Widgets    | Jetpack Glance                    |
| Preferences / Settings | DataStore                         |
| Charts                 | Vico                              |
| Animations             | Compose Animations + Lottie       |
| Import / Export        | OpenCSV                           |
| Multi-language         | Android String Resources          |

---

## 3. Layer Order — Strict, No Skipping

```
UI Layer (Composables / Screens)
  ↓  via StateFlow<UiState> only
ViewModel Layer
  ↓  via suspend functions or Flow
Repository Layer
  ↓  via Room DAO interfaces only
Data Layer (Room DAO + DataStore)
  ↓
SQLite / DataStore Files
```

**Widget path is separate — no ViewModel:**
```
Glance AppWidget
  ↓  repository access only
Repository Layer → Data Layer
```

---

## 4. Package Structure — Do Not Reorganize

```
app/
├── data/
│   ├── db/           # Room DB class, DAOs, @Entity classes
│   ├── datastore/    # DataStore keys and accessors
│   ├── repository/   # Repository interfaces + implementations
│   └── model/        # Domain model data classes (NOT entities)
├── ui/
│   ├── screens/      # One sub-folder per screen
│   ├── components/   # Shared reusable Composables ONLY
│   ├── theme/        # Color.kt, Type.kt, Shape.kt, Spacing.kt, Theme.kt
│   └── navigation/   # NavHost, routes, nav graph
├── widget/           # ALL Glance widget code — nowhere else
├── util/             # Pure utilities: formatters, extensions, constants
└── di/               # Hilt modules
```

---

## 5. Naming Conventions

| What                    | Convention                              | Example                              |
|-------------------------|-----------------------------------------|--------------------------------------|
| Screens                 | `PascalCase` + `Screen` suffix          | `TransactionsScreen.kt`              |
| Composables             | `PascalCase`                            | `TransactionCard.kt`                 |
| ViewModels              | `PascalCase` + `ViewModel` suffix       | `TransactionsViewModel.kt`           |
| UI State classes        | `PascalCase` + `UiState` suffix         | `TransactionsUiState`                |
| Repositories (interface)| `PascalCase` + `Repository` suffix      | `TransactionRepository`              |
| Repositories (impl)     | `PascalCase` + `RepositoryImpl` suffix  | `TransactionRepositoryImpl`          |
| DAOs                    | `PascalCase` + `Dao` suffix             | `TransactionDao`                     |
| Room Entities           | `PascalCase` + `Entity` suffix          | `TransactionEntity`                  |
| Domain Models           | `PascalCase`, no suffix                 | `Transaction`                        |
| Hilt Modules            | `PascalCase` + `Module` suffix          | `DatabaseModule`                     |
| Utility files           | `camelCase` or descriptive `PascalCase` | `CurrencyFormatter.kt`               |
| String resource keys    | `snake_case`                            | `label_add_transaction`              |
| Routes                  | `snake_case` string constants           | `"transactions_screen"`              |
| DataStore keys          | `SCREAMING_SNAKE_CASE`                  | `FONT_SCALE_KEY`                     |

---

## 6. UI / Composable Rules

- Composables **may not** call DAOs, repositories, Room, or DataStore directly. Ever.
- All data flows into composables through `ViewModel → StateFlow<UiState>` only.
- Composables display state and emit events. **No business logic inside composables.**
- Before creating any shared UI element, check `ui/components/` first. If it exists, use it. Do not duplicate.
- Every color must use `MaterialTheme.colorScheme.*`. No hardcoded hex values.
- Every spacing and size must use the custom `Spacing` class from `ui/theme/Spacing.kt`. No hardcoded `dp` values.
- Every font size must reference `MaterialTheme.typography.*`. No hardcoded `sp` values.
- Every corner radius must reference `MaterialTheme.shapes.*`. No hardcoded `dp` values.
- Every user-visible string must use `stringResource()`. No string literals in composables.
- Haptic feedback is triggered via a dedicated utility in `util/` or the ViewModel — never inline in a Composable.
- Animations use `Compose Animations` or `Lottie`. No third-party animation library may be introduced.
- Lists with potentially more than 50 items **must** use `LazyColumn` / `LazyRow`. Never `Column { items.forEach { ... } }`.

---

## 7. ViewModel Rules

- One ViewModel per screen or major feature section. No exceptions.
- ViewModels **do not** hold a `Context` reference (Hilt `@ApplicationContext` is the only allowed exception).
- ViewModels **do not** access DAOs directly. All data access goes through repositories.
- All UI state is a **single `data class UiState`** exposed via `StateFlow`. No scattered `LiveData` or individual `MutableState` fields at the ViewModel level.

```kotlin
// Always this pattern — single UiState, single StateFlow
data class TransactionsUiState(
    val transactions: List<Transaction> = emptyList(),
    val totalBalance: Long = 0L,
    val isLoading: Boolean = false,
    val error: String? = null
)

// In ViewModel:
private val _uiState = MutableStateFlow(TransactionsUiState())
val uiState: StateFlow<TransactionsUiState> = _uiState.asStateFlow()
```

- User events are handled via a `sealed class UiEvent` or individual `fun on[Action]()` functions — never by exposing mutable state to the UI.

---

## 8. Repository Rules

- One repository per data domain: `TransactionRepository`, `AccountRepository`, `GoalRepository`, `BudgetRepository`, `InvestmentRepository`, `DebtRepository`.
- Repositories are the **only** layer that touches Room DAOs or DataStore. Nothing else may import from `data/db/`.
- Return `Flow<T>` for observable data. Return `suspend fun` for one-shot writes.
- No display formatting, currency symbols, or locale-specific logic inside repositories. That lives in `util/CurrencyFormatter.kt`.
- Repositories have **zero imports** from `androidx.compose`, any UI class, or any Android UI framework.
- All aggregation for charts and analysis (totals, averages, category breakdowns) is pre-computed at the repository layer before being passed up. Composables do not calculate.

---

## 9. Database Rules

- Every table or column change requires a **Room migration file**. `fallbackToDestructiveMigration()` is permanently banned.
- Monetary amounts are always stored as `Long` in the **smallest currency unit** (paise, cents). Never `Double`, never `Float`.
- Dates and times are stored as `Long` (Unix timestamp, milliseconds). Never as formatted strings.
- `@Entity` classes live in `data/db/` only. They are never used as UI or domain models — create a separate class in `data/model/`.
- Every schema change updates `SCHEMA.md` on the same day.

---

## 10. Widget Rules

- All Glance widget code lives in `widget/`. Zero widget logic anywhere else in the project.
- Widgets access data through repositories — **no ViewModel, no UI layer**.
- Widget data must be fast to load: no heavy sorting, no aggregation of thousands of rows, no blocking calls.
- Widget state carries only what is actually displayed on the home screen.

---

## 11. Navigation Rules

- **Single Activity architecture.** No Fragments. Ever.
- All navigation is handled through Jetpack Navigation Compose in `ui/navigation/`.
- All screen routes are defined as `sealed class` or `object` in one file. Route strings are never scattered across screen files.

---

## 12. Dependency Rules

- No new library without a written entry in `DEPENDENCIES.md` with full justification.
- No library that requires a network connection for its core purpose. This app is **fully offline**.
- If the problem can be solved in under 50 lines of Kotlin, write the code. Do not add a dependency.
- Run `./gradlew dependencies` and review before every release.

---

## 13. Theme & Design Token Rules

Files in `ui/theme/` are the single source of truth for all visual values:

| File          | Owns                                    |
|---------------|-----------------------------------------|
| `Color.kt`    | All color definitions, light + dark     |
| `Type.kt`     | All text styles                         |
| `Shape.kt`    | All corner radius values                |
| `Spacing.kt`  | All spacing values (custom class)       |
| `Theme.kt`    | Root `MaterialTheme` wrapper            |

**Dual theme is non-negotiable from day one.** Every color references `MaterialTheme.colorScheme.*`.

**Font scaling** is a user setting stored in DataStore and applied at the root `Theme.kt` level via font scale multiplier. Individual composables never hardcode `sp` sizes.

---

## 14. Monetary Calculation Rules

- All monetary arithmetic uses `Long` (smallest currency unit). No intermediate `Double` math. Ever.
- Conversion to display format (e.g., `105050` → `₹1,050.50`) happens **only** in `util/CurrencyFormatter.kt`.
- `CurrencyFormatter` is the single place for currency symbols, decimal separators, and locale formatting.
- **There is only one currency formatter. A second one must never be created.**

---

## 15. Multi-language Rules

- All user-visible strings live in `res/values/strings.xml`. Additional languages add `res/values-{lang}/strings.xml`.
- No string literal inside any Composable. Use `stringResource(R.string.key)`.
- New strings are added to `strings.xml` immediately when a screen is built — never deferred.
- String key naming: `snake_case`, prefixed by context: `label_`, `action_`, `error_`, `title_`, `hint_`, `msg_`.

---

## 16. Animation & Haptic Rules

- Animations use `Compose Animations` (for transitions, visibility, size) or `Lottie` (for illustrative/decorative animations).
- Micro-interactions (button press feedback, list item transitions) use built-in Compose animation APIs — no new library.
- Haptic feedback calls live in a single `util/HapticUtil.kt`. Never called ad-hoc from inside composables.
- Animation durations and easing curves are defined as constants in `util/AnimationConstants.kt` — never magic numbers inline.

---

## 17. Feature-Specific Data Rules

### Transactions
- A transaction belongs to exactly one account.
- Type is an enum: `INCOME`, `EXPENSE`.
- Category is a foreign key to the categories table — never a raw string.

### Transfers
- Stored as **two linked transaction records**: one debit, one credit, sharing a `transferId: Long`.
- Never stored as a single record. The link is the transfer.

### Repeat Expenses
- Future occurrences are **generated lazily** — one at a time when due, not all upfront.
- The repeat rule (frequency, end date) is stored separately from the generated transactions.

### Debt & Loans
- Tracks direction: money given (`LENT`) vs. money owed (`BORROWED`).
- Tracks counterparty name.
- Has a `settled: Boolean` flag. Settled entries are **never deleted** — only marked settled.

### Goals
- Has a target amount (`Long`), a deadline (`Long`), and a linked account or dedicated balance.
- Progress calculation lives in `GoalRepository`. The composable receives a pre-computed `progressPercent` float.

### Budget
- Defined per category, per period (monthly/weekly/custom).
- Remaining budget is pre-calculated at the repository layer.

### Investments
- Separate domain from transactions. Has its own entity, repository, and ViewModel.
- Returns/gains calculated at the repository layer — not in the UI.

### Import
- Validates **every row** before committing anything. A failed import is fully rolled back — all or nothing.
- Uses a Room transaction wrapping all inserts.

### Export
- Reads data atomically inside a Room transaction — no partial reads.

### Account Deletion
- Behavior for related transactions (cascade or block) must be documented in an ADR before implementation. It is not a unilateral implementation decision.

---

## 18. Performance Rules

- Any list with potentially more than 50 items: `LazyColumn` / `LazyRow`. No exceptions.
- No Room query on the main thread. All calls are `suspend` or return `Flow`.
- Chart and analysis data is pre-aggregated in the repository. Composables receive display-ready data.
- Widget updates complete within Android's background execution limits. No heavy computation inside a widget updater.

---

## 19. ❌ NEVER DO — Hard Bans

These are permanent. No exceptions without an ADR and explicit owner approval.

```
NEVER — Network & Remote
  ✗ Add any network call, HTTP client, Retrofit, OkHttp, or internet permission
  ✗ Add authentication, login, user accounts, or user identity
  ✗ Add sync, cloud storage, Firebase, or any remote service
  ✗ Add any dependency whose core function requires a network connection

NEVER — Data & Money
  ✗ Store monetary values as Double or Float
  ✗ Do monetary arithmetic in floating-point at any layer
  ✗ Create a second currency formatter (util/CurrencyFormatter.kt is the only one)
  ✗ Modify a migration file that has already been committed
  ✗ Use fallbackToDestructiveMigration()
  ✗ Use SharedPreferences — DataStore only
  ✗ Use @Entity classes as UI or domain models

NEVER — Architecture
  ✗ Call a DAO or repository directly from a Composable
  ✗ Bypass the ViewModel layer for any UI data access
  ✗ Put business logic inside a Composable
  ✗ Use Fragments — Single Activity only
  ✗ Introduce a new state management pattern (no MVI, Redux, etc. — MVVM only)
  ✗ Put widget logic outside the widget/ package

NEVER — UI & Theme
  ✗ Hardcode any color, hex value, or color int in a Composable
  ✗ Hardcode any spacing or dp value — use Spacing.kt
  ✗ Hardcode any sp font size — use Type.kt
  ✗ Hardcode any corner radius — use Shape.kt
  ✗ Hardcode any user-visible string — use stringResource()
  ✗ Create a duplicate shared Composable without checking ui/components/ first
  ✗ Call haptic feedback inline in a Composable — use HapticUtil.kt

NEVER — Dependencies & Code
  ✗ Add a dependency without an entry in DEPENDENCIES.md
  ✗ Add a dependency for something solvable in under 50 lines of Kotlin
  ✗ Add any animation library other than Compose Animations or Lottie
  ✗ Create a second implementation of any utility listed in UTILITIES.md
```

---

## 20. Before Every AI Session — Checklist

```
[ ] I have a one-sentence goal for this session: "This session will implement [X] in [module Y]"
[ ] I have committed the current state (clean checkpoint before starting)
[ ] I have pasted ARCHITECTURE.md (or the relevant section) into the prompt
[ ] I have pasted UTILITIES.md into the prompt so no duplicates are created
[ ] I have pasted the relevant module README.md if this touches an existing module
[ ] I have noted any relevant ADRs for this area of the codebase
[ ] I know which files are in scope and which are NOT to be touched
```

---

## 21. After Every AI Session — Checklist

```
[ ] Review what changed before committing — not for correctness, for pattern conformance
[ ] Can I answer: what files changed, what each owns, what it depends on, what depends on it?
[ ] If a new shared utility was added → update UTILITIES.md
[ ] If a schema changed → update SCHEMA.md
[ ] If a new dependency was added → update DEPENDENCIES.md
[ ] Add an entry to CHANGELOG_INTERNAL.md: what changed and why
[ ] Consult DONE.md — is this feature actually done?
```

---

## 22. Companion Files — Keep Updated

| File                       | Updated when                                      |
|----------------------------|---------------------------------------------------|
| `SCHEMA.md`                | Every DB entity or column change                  |
| `UTILITIES.md`             | Every new shared utility, hook, formatter, component |
| `DEPENDENCIES.md`          | Every new library added to `build.gradle`         |
| `CHANGELOG_INTERNAL.md`    | After every feature session                       |
| `docs/decisions/ADR-*.md`  | Before any rule in this document is broken        |
| `DONE.md`                  | Consulted before closing any feature              |

---

## 23. The Prompt Template — Use This Every Time

Copy this before every feature request:

```
Context files attached: [ARCHITECTURE.md section / UTILITIES.md / relevant module README]

Feature request: [describe what you want]

Constraints:
- This must live in [layer / package]
- It must follow the pattern established in [similar existing feature]
- It must not introduce a new state management pattern
- It must not add a new dependency without justification in DEPENDENCIES.md
- It must not modify [list stable files that must not change]
- It must not touch any file outside [explicit scope]
- Any schema change must include a Room migration file and update SCHEMA.md
- Any new shared component must be placed in ui/components/ not in the screen folder
- All monetary values are Long in smallest currency unit
- All user strings use stringResource()

Do not change anything outside the stated scope.
Before writing code, list the files you will create or modify and why.
```

---

> **The core principle:**
> The AI builds what is specified. You specify it correctly, review what was built, and enforce these rules.
> Never let the AI invent the architecture while implementing a feature.
