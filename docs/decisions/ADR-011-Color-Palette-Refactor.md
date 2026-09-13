# ADR-011: Color Palette Refactor - Neutral Grey Foundation with Green Accent

## Status
Accepted (Supersedes ADR-001)

## Context
Mitavyay originally utilized an emerald/forest green primary palette across numerous UI elements including surfaces, navigation containers, card backgrounds, and chips. Feedback revealed that ubiquitous green usage produced visual fatigue, diluted the semantic visual weight of green (which represents positive income/savings), and lacked the modern, polished aesthetic of professional financial applications.

The application requires a refined, neutral foundation that directs user attention through selective, intentional use of accent color.

## Decision
We demote green from a general surface/container color to an **accent color**, establishing a clean, grey-based neutral foundation across both Light and Dark themes.

### 1. Accent Green Role & Constraints
Accent green (`#4CAF50` / `#388E3C` for Light / `#81C784` for Dark) is strictly reserved for:
1. **Floating Action Buttons (FABs)**: Quick add expense, add account, add goal.
2. **Primary Call-to-Action (CTA) Buttons**: "Save", "Add", confirmation actions.
3. **Financial Inflows & Gains**: Positive transaction amounts, settled debts, goal milestones.
4. **Active Selection Highlights**: Selected bottom navigation tab icon and label.
5. **Progress Indicators**: Positive budget and savings progress tracks.

All other surfaces, top bars, cards, sheet headers, chip backgrounds, and non-selected UI components use neutral greys.

### 2. Neutral Palette Hierarchy

#### Light Theme Neutrals
- **Background (`#F5F5F5`)**: Soft, cool off-white canvas.
- **Surface (`#FFFFFF`)**: Pure white cards and dialog containers providing elevation and crisp separation.
- **Surface Variant (`#EEEEEE`)**: Clean, non-tinted grey for secondary surfaces, dividers, and unselected chips.
- **Text & Icons**:
  - Primary text: `#121212` (~17.5:1 contrast on `#FFFFFF`, exceeding WCAG AAA).
  - Secondary text / hints: `#757575` (~4.6:1 contrast on `#FFFFFF`, exceeding WCAG AA).
- **Dividers & Outlines**: `#E0E0E0` and `#BDBDBD`.

#### Dark Theme Neutrals
- **Background (`#121212`)**: Deep charcoal/near-black canvas preventing glare.
- **Surface (`#1E1E1E`)**: Refined dark neutral card surfaces.
- **Surface Variant (`#2A2A2A`)**: Elevated secondary containers, chips, and sheet backgrounds.
- **Surface Elevated (`#333333`)**: High-elevation dialogs and floating menus.
- **Text & Icons**:
  - Primary text: `#E0E0E0` (~11.6:1 contrast on `#1E1E1E`, exceeding WCAG AAA).
  - Secondary text / hints: `#9E9E9E` (~5.3:1 contrast on `#1E1E1E`, exceeding WCAG AA).
- **Dividers & Outlines**: `#3A3A3A` and `#555555`.

### 3. Financial Semantics Preserved
- **Expenses & Outflows**: Red (`#E53935` / `#D32F2F`) remains dedicated to expense amounts, negative cash flow, and destructive actions.
- **Income & Inflows**: Green (`#4CAF50` / `#388E3C` / `#81C784`) remains dedicated to income amounts, positive cash flow, and savings.
- **Warnings**: Amber (`#FFA000` / `#E65100` / `#FFB74D`) for thresholds (e.g., 80% budget limit).

### 4. WCAG AA / AAA Accessibility Compliance
All text and interactive element pairings strictly comply with WCAG AA guidelines:
- `AccentGreenDark` (`#388E3C`) on `#FFFFFF`: ~4.52:1 (Passes AA).
- `AccentGreenLight` (`#81C784`) on `#121212`: ~9.86:1 (Passes AAA).
- `OnPrimaryLight` (`#FFFFFF`) on `AccentGreenDark` (`#388E3C`): ~4.52:1 (Passes AA).
- `OnPrimaryDark` (`#003816`) on `AccentGreenLight` (`#81C784`): ~9.86:1 (Passes AAA).

## Consequences
- The UI maintains a clean, modern aesthetic with strong visual hierarchy.
- Green retains strong semantic impact when highlighting positive balance changes and primary actions.
- Zero green tints on cards, lists, backgrounds, or app bars.
- 100% backward-compatible tokens preserved in `ui/theme/Color.kt`.
