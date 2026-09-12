# ADR-004: Single Activity Navigation Architecture

## Status
Accepted

## Context
Mitavyay is an offline-first personal finance application developed with Jetpack Compose and Modern Android Architecture. Traditional Android architectures often relied on multiple Activities or a Single Activity hosting multiple Android `Fragment`s. Fragments introduce complicated lifecycles, fragment manager backstack synchronization issues, and XML/Compose interoperability overhead.

## Decision
Mitavyay uses a **Single Activity (`MainActivity`)** with all screen transitions, backstack management, and top-level tab switching implemented purely using **Jetpack Navigation Compose (`androidx.navigation.compose`)**. 

- Android `Fragment`s are **strictly banned** throughout the entire codebase.
- The root `MitavyayApp` scaffold orchestrates the top-level app bar, bottom navigation bar, floating action button, and hosts `MitavyayNavHost`.
- Navigation state and route parsing are managed by `MitavyayAppState`.
- Each screen composable is paired with a scoped `@HiltViewModel` (`hiltViewModel()`) that observes domain repositories and exposes a single unified `StateFlow<UiState>`.

## Consequences

### Positive
- **Lifecycle Simplicity**: Eliminates Fragment lifecycle quirks (e.g. `onCreateView` vs `onViewCreated` vs `onDestroyView`). All screen lifecycles map naturally to standard Compose composition lifecycles and ViewModel lifecycles.
- **Predictable State Flow**: Screen UI state flows unidirectionally from ViewModels as immutable `StateFlow`s.
- **Seamless Theming & Transition**: The app-level `MitavyayTheme` remains persistent across screen transitions, preventing theme flashing or window attachment overhead.
- **Testability**: Screen composables are decoupled from navigation controller instances, making them easily previewable and unit-testable in isolation.

### Negative / Trade-offs
- Screen parameters and backstack arguments must be serialized via route arguments rather than Intent extras or Fragment bundles.
- Deep linking or widget routing must dispatch through the single `MainActivity` navigation intent handler.
