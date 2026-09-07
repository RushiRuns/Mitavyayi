# Definition of Done

Before considering any feature, session, or phase complete, ensure the following checklist is satisfied.

## Feature Checklist

- [ ] **Happy Path**: Core functionality works as expected.
- [ ] **Empty State**: UI gracefully handles scenarios with no data (e.g., empty lists).
- [ ] **Error State**: UI gracefully handles and displays errors without crashing.
- [ ] **Data Persistence**: Data survives app restart and simulated crashes.
- [ ] **No Architectural Drift**: 
    - Code complies entirely with `ARCHITECTURE.md`.
    - No network calls, no SharedPreferences, no floating-point monetary values.
    - ViewModels do not reference UI classes; Composables do not reference Repositories/DAOs.
- [ ] **Documentation Updated**:
    - `SCHEMA.md` is updated if database schema changed.
    - `UTILITIES.md` is updated if new shared logic or components were added.
    - `DEPENDENCIES.md` is updated if `build.gradle` was modified.
    - `CHANGELOG_INTERNAL.md` is populated with the session's work.
- [ ] **Testing**: At least one meaningful automated test (unit or UI) has been written for the new functionality.
