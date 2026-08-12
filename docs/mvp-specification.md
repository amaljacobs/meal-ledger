# MVP Specification

This document defines the first release behavior before implementation begins.

## Screens

### Today

The default screen shows the selected day's totals and chronological activity.

- Header: selected date, with previous-day, next-day, and date-picker actions.
- Summary: food calories, food spending, water amount, and water-goal progress.
- Quick actions: add food and add water.
- Timeline: food and water entries ordered by consumed time, newest first when times are equal.
- Empty state: clear actions to add the first food or water entry.

### Add or Edit Food

Required fields:

- Food name
- Consumed time; default to the current time

Optional fields:

- Meal type: breakfast, lunch, dinner, snack, or other
- Portion note, such as `2 chapatis` or `1 bowl`
- Estimated calories
- Price
- Note

Save is disabled until the name is non-blank. Calories and price must be zero or greater when supplied. Editing pre-fills the same form; deleting requires confirmation.

### Add or Edit Water

Required fields:

- Amount in millilitres
- Consumed time; default to the current time

The form includes quick amounts: 250 ml, 500 ml, and 1,000 ml. The user may enter another positive whole-number amount. Editing and deletion follow the food-entry behavior.

### Summary

The initial summary view shows a week at a time:

- Daily calories, food spending, and water totals.
- Number of days that reached the water goal.
- Weekly food-spend total.

Monthly reports, charts, exports, and budgets are out of scope for the MVP.

### Settings

- Currency code, defaulting to the device locale when available.
- Daily water goal in millilitres, default 2,500 ml.
- Optional daily calorie target; disabled by default.
- About section with app version and privacy statement.

## User Flows

### Record Food

1. From Today, select Add Food.
2. Enter a name and any known details.
3. Confirm the consumed time and save.
4. The entry appears in Today's timeline and updates totals immediately.

### Record Water

1. From Today, select Add Water.
2. Pick a quick amount or enter a custom amount.
3. Confirm the time and save.
4. Today's water total and goal progress update immediately.

### Correct an Entry

1. Select an item in the timeline.
2. Choose Edit or Delete.
3. Saving edits recalculates totals; deleting requires confirmation and recalculates totals.

## Data Model

All timestamps are stored as an instant in UTC and rendered in the device's current local time zone. Daily grouping uses the local date at the time the app queries records.

### FoodEntry

| Field | Type | Rules |
| --- | --- | --- |
| id | Long | Local database primary key |
| name | String | Required; trimmed; 1-100 characters |
| consumedAt | Instant | Required |
| mealType | MealType? | Optional enum |
| portionNote | String? | Optional; maximum 200 characters |
| calories | Int? | Optional; 0 or greater |
| priceMinor | Long? | Optional; stored in minor currency units; 0 or greater |
| currencyCode | String | ISO 4217 code captured when a price is saved |
| note | String? | Optional; maximum 500 characters |
| createdAt | Instant | Required |
| updatedAt | Instant | Required |

### WaterEntry

| Field | Type | Rules |
| --- | --- | --- |
| id | Long | Local database primary key |
| amountMl | Int | Required; 1-10,000 |
| consumedAt | Instant | Required |
| createdAt | Instant | Required |
| updatedAt | Instant | Required |

### UserSettings

One locally stored settings record.

| Field | Type | Rules |
| --- | --- | --- |
| currencyCode | String | ISO 4217 code |
| dailyWaterGoalMl | Int | Positive whole number |
| dailyCalorieTarget | Int? | Optional positive whole number |

## Derived Values

- Daily food spending: sum of `priceMinor` for food entries with a price.
- Daily calories: sum of `calories` for food entries with calories.
- Daily water: sum of `amountMl` for water entries.
- Water progress: `daily water / daily water goal`, shown as no more than 100% in the progress indicator while the actual total remains visible.
- Entries with missing calories or price are excluded from that particular total; they are never treated as zero.

## Explicit MVP Boundaries

- A food entry represents a single consumed item or meal. Recipe management is not included.
- Currency changes affect new price entries; existing price entries retain the currency used when saved.
- The app does not give health, hydration, or nutrition advice. Goals are user-controlled reference values.
- Data remains on-device. Uninstalling the app may remove all entries.
