# Meal Ledger

An Android app for recording food, water intake, and food spending.

## Project Status

The Android baseline, Room persistence, Today dashboard, food and water logging, entry correction, food timeline presentation, and personal settings are complete. The weekly summary and internal test release remain.

## Documentation

- [Product brief](docs/product-brief.md)
- [MVP specification](docs/mvp-specification.md)
- [Technical plan](docs/technical-plan.md)
- [Initial setup guide](docs/initial-setup.md)
- [Implementation roadmap](docs/implementation-roadmap.md)
- [Milestones](docs/milestones.md)
- [Project workflow](docs/project-workflow.md)
- [Decision log](docs/decisions.md)

## Data handling for internal testing

Meal Ledger keeps food entries, water entries, and goal history in its on-device Room database. Preferences such as currency and daily goals are stored locally in Android DataStore. The app does not send this data to a server or provide cloud backup or account sync. Removing the app or clearing its storage removes that local data; use test data only during internal releases.

## Intended Stack

- Kotlin
- Jetpack Compose
- Room database
