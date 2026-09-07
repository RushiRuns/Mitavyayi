# Approved Dependencies Stack

> **RULE:** No new dependency may be added without an entry in this file including full justification.

## Approved Stack

- **Kotlin**: Primary language (v1.9.22)
- **Jetpack Compose**: UI framework (BOM 2024.02.00, Compiler 1.5.10)
- **Room (SQLite)**: Local database (v2.6.1)
- **DataStore**: Preferences & Settings (v1.0.0) (No SharedPreferences)
- **Hilt**: Dependency Injection (v2.50, hilt-navigation-compose v1.2.0)
- **Jetpack Glance**: Home Screen Widgets (v1.0.0)
- **Vico**: Charts & Analytics Visualization (v1.13.1)
- **OpenCSV**: Import / Export functionality (v5.9)
- **Compose Animations & Lottie**: Animations (v6.4.0)

## Banned Alternatives (DO NOT USE)

- **SharedPreferences**: Banned. Use DataStore instead.
- **Firebase / Supabase**: Banned. The app is strictly offline only.
- **Retrofit / Ktor / OkHttp**: Banned. No network calls are permitted.

## Banned Completely (DO NOT USE)

- **Authentication Libraries**: No login or user accounts.
- **Cloud Sync Libraries**: Data remains local to the device.
- **Analytics / Crashlytics**: No tracking, privacy-first.
