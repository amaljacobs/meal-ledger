# Implementation Roadmap

This roadmap turns the MVP specification into a dependency-aware delivery order. Keep one issue in progress at a time. Move an issue to Done only after its acceptance criteria are met, the app is manually checked on a device or emulator, and its changes are committed.

## Completed

| Issue | Outcome |
| --- | --- |
| #8 | MVP screens, flows, data model, and technical decisions documented. |

## Current Work

### #1 Create Android project with Kotlin and Jetpack Compose

The Android Studio project now opens and launches on a physical device. Finish this issue by replacing the generated greeting with a small navigation host and stable routes for `today` and `settings`. Commit the generated Android project and the navigation baseline together.

## Delivery Order

| Order | Issue | Why it is next |
| --- | --- | --- |
| 1 | #1 Android and navigation foundation | Establishes the executable app and screen structure. |
| 2 | #10 Room local database | Provides offline persistence and observable data for every core screen. |
| 3 | #6 Today screen with daily totals | Establishes the main read experience, empty state, ordering, and derived totals. |
| 4 | #7 Food-entry creation flow | Adds the main food-recording workflow against the established data layer. |
| 5 | #9 Water-entry creation flow | Adds hydration logging using the same date/time and persistence patterns. |
| 6 | #4 Edit and delete actions | Makes real daily tracking practical by correcting mistakes. |
| 7 | #5 Settings for currency and water goal | Adds persisted preferences and makes totals/presentation personal. |
| 8 | #3 Weekly summary screen | Builds on reliable historical entries and settings. |
| 9 | #2 First internal test release | Validates the complete MVP as an installable release build. |

## Completion Checklist

For each issue:

1. Move it from Backlog to In Progress.
2. Create a focused branch, for example `feature/10-room-database`.
3. Implement only the issue's acceptance criteria and necessary tests.
4. Run the relevant unit tests and install/run the app on a device or emulator.
5. Commit with the issue number, for example `Add Room persistence (#10)`.
6. Push the branch, open a pull request if using them, then move the issue to Done and close it.

## MVP Guardrails

- Keep all data local; do not introduce accounts, cloud sync, or external nutrition APIs.
- Store timestamps in UTC; group them by the device's local day for display and totals.
- Store monetary values in minor units and water in milliliters.
- Add tests for validation, totals, date grouping, and persistence before release work.
- Treat privacy policy, release signing, and manual test notes as #2 work.
