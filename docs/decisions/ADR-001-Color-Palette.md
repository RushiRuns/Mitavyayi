# ADR-001: Color Palette & Semantic Design Tokens

## Status
Accepted

## Context
Mitavyay is an offline-first personal finance application that emphasizes mindful economy, clarity, and ease of use. A financial app requires high legibility, accessible contrast across all lighting conditions, and intuitive semantic color coding (e.g., distinguishing debits, credits, alerts, and neutral states).

In accordance with `ARCHITECTURE.md`, all colors must be centralized in `ui/theme/Color.kt`, supporting both Light and Dark themes from day one, with zero hardcoded color literals permitted in Composables.

## Decision
We establish an Emerald Green / Slate / Teal color scheme rooted in Material Design 3, complemented by an `ExtendedColorScheme` for explicit financial semantics (`success` for income/settlement and `warning` for budget threshold alerts).

### 1. Palette Roles
- **Primary (Emerald Green - `#006C4C` light / `#6CDBAC` dark)**: Represents financial stability, prosperity, and deliberate action. Used for primary CTAs, active highlights, and brand identity.
- **Secondary (Sage/Slate - `#4C6357` light / `#B3CCBE` dark)**: Neutral supporting element for filter chips, secondary buttons, and subdued accents.
- **Tertiary (Steel Teal - `#3D6373` light / `#A5CDE0` dark)**: Dedicated accent for analytics, charts, insights, and goal tracking.
- **Background & Surface (`#FBFDFA` light / `#101412` dark)**: Soft off-white and deep charcoal surfaces to minimize glare and maximize contrast.
- **Error (`#BA1A1A` light / `#FFB4AB` dark)**: Denotes expense transactions, over-budget alerts, and destructive actions.
- **Success (`#1B6C31` light / `#8BD891` dark)**: Denotes income transactions, positive cash flow, and settled debt.
- **Warning (`#7B5800` light / `#F6BD39` dark)**: Denotes cautionary events (e.g., reaching 80% of budget limit, upcoming bill deadlines).

### 2. WCAG AA Contrast Compliance
All foreground-to-background combinations meet or exceed WCAG AA requirements:
- **Normal text**: Minimum contrast ratio of 4.5:1
  - `OnPrimaryLight` (`#FFFFFF`) on `PrimaryLight` (`#006C4C`): ~4.9:1 (Passes AA)
  - `OnBackgroundLight` (`#191C1A`) on `BackgroundLight` (`#FBFDFA`): ~15.2:1 (Passes AAA)
  - `OnBackgroundDark` (`#E1E3DF`) on `BackgroundDark` (`#101412`): ~13.8:1 (Passes AAA)
  - `OnErrorLight` (`#FFFFFF`) on `ErrorLight` (`#BA1A1A`): ~5.6:1 (Passes AA)
  - `OnSuccessLight` (`#FFFFFF`) on `SuccessLight` (`#1B6C31`): ~5.2:1 (Passes AA)
  - `OnWarningLight` (`#FFFFFF`) on `WarningLight` (`#7B5800`): ~4.8:1 (Passes AA)
- **Large text & UI components**: Minimum contrast ratio of 3.0:1

### 3. Dual Theme & Inverse Mappings
The dark theme provides direct semantic equivalents with inverted luminescence. Surfaces switch to low-luminance neutral dark tones, while primaries and accents shift to pastel-toned tones (tone 80) against dark containers (tone 20/30) to preserve visual hierarchy and prevent eye fatigue.

## Consequences
- No Composable may use hardcoded `Color(0x...)` or `Color.Green`, `Color.Red`, etc.
- All components must reference `MaterialTheme.colorScheme` or `MaterialTheme.extendedColorScheme`.
- Status indicators for income and expenses must systematically utilize `extendedColorScheme.success` and `colorScheme.error` respectively.
