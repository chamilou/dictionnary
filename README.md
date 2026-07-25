# Avar Dictionary Android App

Offline-first Android dictionary app for Avar with Room-backed local storage and a Compose UI.

Project map:

- [Project Index](/Users/shamilidrisov/AndroidStudioProjects/dictionnary/docs/PROJECT_INDEX.md:1)
- [Project Snapshot](/Users/shamilidrisov/AndroidStudioProjects/dictionnary/docs/PROJECT_SNAPSHOT.md:1)

## Current State

This project is no longer an empty prototype. It now has:

- A local Room database stored as `dictionary.db`
- First-run CSV import from `app/src/main/assets/avar_russian_english.csv`
- Search over imported Avar, Russian, and English entries
- A staged first-launch database build flow with visible progress instead of a blank wait
- Paged search with a fast first 100 results and explicit load-more
- Indexed alphabet browse with an explicit 46-letter Avar letter set
- Favorites and recent searches persisted locally
- A Compose search screen with:
  - left-aligned language direction selector
  - `Search`, `Favorites`, `Recent`, and `Training` tabs
  - compact search card with mic and filter affordances
  - result rows with favorite action, bookmark state, and status chips
  - row taps that open the detail screen from search, browse, favorites, recent, and training-backed selections
  - training flashcards with chosen-word and random-word modes
  - training roadmap split into flashcards now and voice pronunciation later
  - entry detail screen with notes, metadata, and Russian bridge content
  - a settings surface for theme, UI language, app info, references, direction counts, and contact placeholder

## Important Reality Check

The product direction is multilingual, and the current imported dataset is now Avar + Russian + draft English.

That means:

- `AV -> RU`, `RU -> AV`, `AV -> EN`, and `EN -> AV` now have seeded data
- English entries come from a Russian-English pass and are still marked `needs_review`
- German/Spanish/French options are still UI-level placeholders without seeded data

## Tech Stack

- Kotlin
- Jetpack Compose Material 3
- Room
- KSP
- Min SDK 24
- Target / Compile SDK 35

## Data Model

Room database version: `10`

Tables:

- `entries`
- `translations`
- `favorites`
- `recent_searches`
- `corrections`

Key files:

- [DictionaryDatabase.kt](/Users/shamilidrisov/AndroidStudioProjects/dictionnary/app/src/main/java/com/avardiction/app/data/local/DictionaryDatabase.kt:1)
- [DictionaryDao.kt](/Users/shamilidrisov/AndroidStudioProjects/dictionnary/app/src/main/java/com/avardiction/app/data/local/DictionaryDao.kt:1)
- [DictionaryEntities.kt](/Users/shamilidrisov/AndroidStudioProjects/dictionnary/app/src/main/java/com/avardiction/app/data/local/DictionaryEntities.kt:1)
- [CsvDictionaryImporter.kt](/Users/shamilidrisov/AndroidStudioProjects/dictionnary/app/src/main/java/com/avardiction/app/data/local/CsvDictionaryImporter.kt:1)
- [DictionaryRepository.kt](/Users/shamilidrisov/AndroidStudioProjects/dictionnary/app/src/main/java/com/avardiction/app/data/repository/DictionaryRepository.kt:1)

Seed import side effect:

- When the bundled CSV fingerprint or seed import version changes, the app clears all local Room tables and re-imports the seed data.
- That also removes locally stored favorites, recent searches, and saved corrections on the device.

Seed import behavior:

- First-launch seeding is guarded by a single-flight mutex so parallel repository calls do not start duplicate imports.
- Import progress is reported in stages: preparing, parsing rows, writing entries, writing translations, finalizing.

## Search Behavior

- Search is performed against the selected source language
- Imported entries are normalized before search and get precomputed `browseKey` values during import
- DAO ranking prefers exact match, then prefix, then word-start, then general substring matches
- Search results are fetched in pages of 100 items
- Recent searches are recorded only when the query is not blank
- Favorites are stored locally in Room
- When a target language is selected, the UI still keeps Avar visible and may also keep Russian visible as bridge content
- Tapping a found word opens the detail screen consistently across result sources

Browse and indexing rules:

- Avar browse uses an explicit 46-letter alphabet and treats variants like `Г`, `Гь`, `Гъ`, `ГI` and `К`, `Кь`, `Къ`, `КI` as different letters.
- Digits are excluded from browse indexing.
- Morphology separators such as `¦`, `:`, and `-` are not indexed as starting characters for Avar headwords.
- Russian reverse-search indexing also excludes entries whose visible source starts with `(` and falls back to the normalized Russian key when needed.

Current limitation:

- DAO search is source-language driven
- Searching from `de`, `es`, or `fr` still returns nothing because those translations have not been seeded
- Users must explicitly load additional pages when more than 100 ranked matches exist

## UI Status

Implemented:

- search home screen
- entry detail screen
- language direction dropdown
- more-actions dropdown for translation filters
- settings sheet with:
  - theme mode selection
  - branded light/dark palettes with stronger dark contrast and accessible secondary/outline pairings
  - theme-aware cards, sheets, and screen gradients so dark/system mode no longer reuses light-only `Paper` surfaces
  - UI language override independent from system language
  - about / references / direction word counts
  - contact placeholder
- favorites tab
- recent searches tab
- bottom navigation shell
- training flashcards with:
  - manual word selection from the current source language
  - random word selection for the active direction
  - flip-to-reveal translation check

Not implemented yet:

- add / suggest entry flow
- voice pronunciation training mode
- real bookmark persistence
- correction submission UI
- finalized privacy / support / license disclosures

## Build

Use the Android Studio bundled JDK, not the system JDK.

Working compile command:

```bash
/bin/zsh -lc 'export JAVA_HOME="/Applications/Android Studio.app/Contents/jbr/Contents/Home"; export PATH="$JAVA_HOME/bin:$PATH"; export GRADLE_USER_HOME="$PWD/.gradle-user-home"; ./gradlew :app:compileDebugKotlin'
```

APK build command:

```bash
/bin/zsh -lc 'export JAVA_HOME="/Applications/Android Studio.app/Contents/jbr/Contents/Home"; export PATH="$JAVA_HOME/bin:$PATH"; export GRADLE_USER_HOME="$PWD/.gradle-user-home"; ./gradlew :app:assembleDebug'
```

Last verified:

- `:app:assembleDebug` succeeded on July 25, 2026
- `:app:testDebugUnitTest --tests com.avardiction.app.data.local.CsvDictionaryImporterNoteNormalizationTest` succeeded on July 25, 2026
- Local macrobenchmark snapshot on a Pixel 5 (Android 14) ran on July 25, 2026:
  `coldStartup` passed; `searchTypingAndScrollFrames` and `searchTypingAndScrollPower`
  failed with `Search results list not found for queries ав, аб, а`

## Project Structure

- `app/src/main/assets/`
  - source CSV copied into app assets for offline seed
- `app/src/main/java/com/avardiction/app/data/`
  - Room entities, DAO, DB, importer, repository
- `app/src/main/java/com/avardiction/app/domain/`
  - domain models and language enums
- `app/src/main/java/com/avardiction/app/presentation/`
  - Compose UI and view model
- `app/src/main/java/com/avardiction/app/ui/theme/`
  - colors, typography, Material theme

## Recommended Next Steps

1. Stabilize the search macrobenchmark path before treating frame/power numbers as publishable; the latest Pixel 5 run passed `coldStartup` but not the two search benchmarks.
2. Decide whether unsupported language directions should be hidden, disabled, or labeled more aggressively until data exists.
3. Add real bookmark storage instead of a note-state icon.
4. Add correction submission flow backed by the existing `corrections` table.
5. Review and refine the draft English dataset so `AV -> EN` is not just a machine-assisted bridge.
6. Decide whether training should use favorites, bookmarks, or a dedicated study list instead of the full live dictionary.
7. Define the second training mode around voice pronunciation, including audio source, playback, and answer-check flow.
8. Replace placeholder settings/legal text with real privacy, license, and support details before publishing.

## Notes

- The database currently uses `fallbackToDestructiveMigration`, which is acceptable for early iteration but not for production data safety.
- There is an older reusable `LanguageSelector` composable in the codebase that is not part of the current main screen flow.
- The current search implementation matches only in the selected source language, then filters visible translations afterward; this works, but the direction model is still more complex than the UI suggests.
- Alphabet browse is now SQL/index driven through `browseKey` values instead of scanning and deriving letters on the main path of first launch.
- First-launch waiting is now user-visible through import progress UI, but the import still rebuilds the full local dataset synchronously before search becomes usable.
- The current training flow is stateless practice over visible dictionary entries; it does not yet track progress, scoring, spaced repetition, or voice pronunciation.
- The Material 3 theme now uses explicit branded light/dark role pairs, including tertiary container roles and stronger outline colors for interactive boundaries.
- Search and detail screens now derive their major cards, bottom sheets, navigation bar, flashcards, and background gradients from `MaterialTheme.colorScheme` instead of hardcoded light palette tokens.
- The settings surface now exists, but privacy/support/legal content is still placeholder-level.
- Lightweight verification guidance lives in [VERIFICATION.md](/Users/shamilidrisov/AndroidStudioProjects/dictionnary/VERIFICATION.md:1).
