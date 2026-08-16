# Implementation Roadmap

This roadmap turns the MVP specification into a dependency-aware delivery order. Keep one issue in progress at a time. Move an issue to Done only after its acceptance criteria are met, the app is manually checked on a device or emulator, and its changes are committed.

## Completed

| Issue | Outcome |
| --- | --- |
| #8 | MVP screens, flows, data model, and technical decisions documented. |
| #1 | Android project and navigation foundation completed. |
| #10 | Room version-1 persistence, repository, exported schema, and device tests completed. |
| #13 | Optional protein storage, Room version-2 migration, exported schema, and device migration test completed. |
| #6 | Today dashboard, local-day navigation, Room-backed daily totals, empty/loading states, and dashboard unit tests completed. |
| #7 | Food-entry form with optional meal, portion, calories, protein, price, and note fields completed; validation and on-device saving verified. |
| #9 | Water-entry form with 250 ml cup defaults, quick amounts, custom millilitre validation, and Room persistence completed. |
| #4 | Food and water timeline rows open pre-filled editors; updates and confirmed deletions persist through Room and refresh the dashboard. |
| #17 | Food timeline uses contextual meal icons and shows recorded protein while retaining entry correction access. |
| #5 | Preferences DataStore persists currency, daily water goal, and cup size; the dashboard shows water-goal progress, and new food and water entries use the selected preferences. |

## Current Work

### #3 Weekly summary screen

Build the historical weekly view from persisted entries and the daily water goal. The view covers the current day and the preceding six calendar days, including days without entries.

## Delivery Order

| Order | Issue | Why it is next |
| --- | --- | --- |
| 1 | #1 Android and navigation foundation | Establishes the executable app and screen structure. |
| 2 | #10 Room local database | Provides offline persistence and observable data for every core screen. |
| 3 | #13 Optional protein database migration | Finalizes the MVP food schema before screens begin consuming it. |
| 4 | #6 Today screen with daily totals | Establishes the main read experience, empty state, ordering, and derived totals. |
| 5 | #7 Food-entry creation flow | Adds the main food-recording workflow against the established data layer. |
| 6 | #9 Water-entry creation flow | Adds hydration logging using the same date/time and persistence patterns. |
| 7 | #4 Edit and delete actions | Makes real daily tracking practical by correcting mistakes. |
| 8 | #17 Improve food timeline presentation | Adds meal-type icons and visible optional protein after entry correction workflows are complete. |
| 9 | #5 Settings for currency, water goal, and cup size | Adds persisted preferences and makes totals/presentation personal. |
| 10 | #3 Weekly summary screen | Builds on reliable historical entries and settings. |
| 11 | #2 First internal test release | Validates the complete MVP as an installable release build. |

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
