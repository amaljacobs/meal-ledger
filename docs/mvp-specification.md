# MVP Specification

This document defines the first release behavior before implementation begins.

## Screens

### Today

The default screen shows the selected day's totals and chronological activity.

- Header: selected date, with previous-day, next-day, and date-picker actions.
- Summary: food calories, protein, food spending, water amount, and configured-goal progress.
- Quick actions: add food and add water.
- Timeline: food and water entries ordered by consumed time, newest first when times are equal. Food rows show a contextual meal-type icon when set and show optional protein when recorded.
- Empty state: clear actions to add the first food or water entry.

### Add or Edit Food

Required fields:

- Food name
- Consumed time; default to the current time

Optional fields:

- Meal type: breakfast, lunch, dinner, snack, or other
- Portion note, such as `2 chapatis` or `1 bowl`
- Estimated calories
- Protein in grams
- Price
- Note

Save is disabled until the name is non-blank. Calories and price must be zero or greater when supplied. Editing pre-fills the same form; deleting requires confirmation.

### Add or Edit Water

Required fields:

- Amount in millilitres
- Consumed time; default to the current time

The form includes shortcuts for one and two configured cups plus 500 ml and 1,000 ml. The default cup size is 250 ml. The user may enter another positive whole-number amount. Editing and deletion follow the food-entry behavior.

### Summary

The summary supports calendar-aligned week and month views:

- Daily calories, food spending, and water totals.
- Number of days that reached configured water, calorie, and protein goals when those goals are set.
- Total food spending, protein, and average daily water for the selected period.
- Week mode runs Monday through Sunday and can navigate through past weeks.
- Month mode uses a Monday-first calendar grid. Empty days remain visible without activity totals.
- Forward navigation is disabled for the current period, so future weeks and months cannot be selected.

Charts, exports, and budgets are out of scope for the MVP.

### Settings

- Currency code, defaulting to the device locale when available.
- Daily water goal in millilitres, default 2,500 ml.
- Optional daily calorie goal in kilocalories.
- Optional daily protein goal in grams.
- Default cup size in millilitres, default 250 ml, used by water shortcuts.

Goal changes take effect on the local date they are saved and do not change the targets shown for earlier dates.

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
| proteinGrams | Int? | Optional whole grams; 0 or greater |
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
| dailyCalorieGoal | Int? | Optional positive whole number |
| dailyProteinGoalGrams | Int? | Optional positive whole number |
| cupSizeMl | Int | Positive whole number; default 250 |

### DailyGoal

| Field | Type | Rules |
| --- | --- | --- |
| effectiveDate | LocalDate | Local calendar date at which this snapshot applies; primary key |
| dailyWaterGoalMl | Int | Required positive whole number |
| dailyCalorieGoal | Int? | Optional positive whole number |
| dailyProteinGoalGrams | Int? | Optional positive whole number |

## Derived Values

- Daily food spending: sum of `priceMinor` for food entries with a price.
- Daily calories: sum of `calories` for food entries with calories.
- Daily protein: sum of `proteinGrams` for food entries with protein recorded.
- Daily water: sum of `amountMl` for water entries.
- Goal progress uses the goal snapshot effective on the local date being viewed. Water, calorie, and protein progress indicators are capped at 100% while actual totals remain visible.
- Entries with missing calories or price are excluded from that particular total; they are never treated as zero.

## Explicit MVP Boundaries

- A food entry represents a single consumed item or meal. Recipe management is not included.
- Protein is the only macronutrient tracked in the MVP. Carbohydrates and fats are deferred until they have a clear reporting use case.
- Currency changes affect new price entries; existing price entries retain the currency used when saved.
- Water is stored in millilitres. The logging UI offers cup and bottle shortcuts that convert to millilitres, while a custom millilitre amount remains available.
- The app does not give health, hydration, or nutrition advice. Goals are user-controlled reference values.
- Goal snapshots are retained locally so changing a goal today does not rewrite historical progress.
- Data remains on-device. Uninstalling the app may remove all entries.
