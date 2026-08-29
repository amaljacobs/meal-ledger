# Meal Ledger

An offline-first Android journal for recording food, water intake, and food spending.

> 🚧 **Actively building** — Meal Ledger is a work in progress. Core daily logging, summaries, and personal settings are in place; the app is still evolving.

## What it does today

- Log food entries with meal type, time, portion notes, estimated calories, and price.
- Log water intake quickly throughout the day.
- See daily food, water, calorie, and spending totals.
- Review food entries and navigate weekly or monthly summaries.
- Set a currency, daily water goal, and optional calorie target.

## Product principles

- **Private by design:** data stays on the device.
- **Offline-first:** no account or cloud sync is required.
- **Simple:** fast daily logging without ads or a complex nutrition database.

## Next up

- Continue polishing the daily logging and summary experience.
- Expand test coverage and prepare an internal test release.
- Keep refining the app from real-world use.

## Project Status

The Android baseline, Room persistence, Today dashboard, food and water logging, entry correction, food timeline presentation, weekly and monthly summaries, and personal settings are in place. The project remains actively under development.

## Documentation

- [Product brief](docs/product-brief.md)
- [MVP specification](docs/mvp-specification.md)
- [Technical plan](docs/technical-plan.md)
- [Initial setup guide](docs/initial-setup.md)
- [Implementation roadmap](docs/implementation-roadmap.md)
- [Milestones](docs/milestones.md)
- [Project workflow](docs/project-workflow.md)
- [Decision log](docs/decisions.md)

## Intended Stack

- Kotlin
- Jetpack Compose
- Room database
