# Decision Log

Record decisions here when they affect future work or would otherwise be easy to forget.

| Date | Decision | Reason |
| --- | --- | --- |
| 2026-08-12 | Start as an Android-first app using Kotlin and Jetpack Compose. | The initial release target is Google Play; native Android keeps the first version focused. |
| 2026-08-12 | Make the MVP local-only with no account. | It reduces scope and supports a privacy-first product. |
| 2026-08-12 | Track calories as user-entered estimates. | Automatic nutrition data would add complexity and reduce the speed of logging. |
| 2026-08-13 | Support Android API 26 and above for the MVP. | It gives modern Android API support while covering a broad range of devices. |
| 2026-08-13 | Store monetary amounts in minor currency units and record the entry currency. | It prevents floating-point rounding issues and preserves historical entries when a user changes currency. |
| 2026-08-13 | Use Room for entries and DataStore Preferences for settings. | Both are Android-supported local persistence solutions suited to their respective data types. |
| 2026-08-16 | Begin the Room schema at version 1, commit its exported JSON schema, and add explicit migrations for every later schema version. | Entries are user data, so upgrades must preserve them; destructive migration is not an acceptable release strategy. |
| 2026-08-16 | Track optional protein in whole grams for the MVP; defer carbohydrates and fats. | Protein adds useful context without making the food form or reports unnecessarily dense. |
| 2026-08-16 | Store water in millilitres while offering cup and bottle shortcuts in the UI. | Millilitres keep totals consistent; familiar shortcuts reduce logging friction. |
