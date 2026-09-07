# ADR-000: Offline-Only Constraint

## Status
Accepted

## Context
A primary requirement for the Finance App is absolute privacy and data ownership by the user. Relying on cloud services, external databases, or third-party APIs introduces privacy risks, potential subscription costs, and single points of failure.

## Decision
The app will be strictly **offline-only**. 
There will be absolutely no network access, no backend connectivity, no cloud synchronization, and no remote analytics. 

## Reasoning
- **User Privacy**: Financial data is highly sensitive. Keeping data on-device ensures complete privacy.
- **Independence**: The app will function fully without an internet connection and will never rely on external service availability.
- **Simplicity**: Removes the need for complex sync logic, conflict resolution, authentication, and token management.

## Consequences
- **Data Portability**: Users must rely entirely on local export/import mechanisms (e.g., CSV) to back up or move their data between devices.
- **Schema Migrations**: Database migrations using Room must be meticulously written and tested, as there is no backend to correct corrupted data. `fallbackToDestructiveMigration()` is strictly banned.
- **Feature Limitations**: Features that inherently require a network (e.g., live currency exchange rates, bank API integrations) cannot be implemented.
