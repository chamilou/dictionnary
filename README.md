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
- Paged search with a fast first 100 results and explicit load-more
- Favorites and recent searches persisted locally
- A Compose search screen with:
  - left-aligned language direction selector
  - `Search`, `Favorites`, `Recent`, and `Training` tabs
  - larger search card with mic and filter affordances
  - result rows with favorite action, bookmark state, and status chips
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

Room database version: `6`

Tables:

- `entries`
- `translations`
- `favorites`
- `recent_searches`
- `corrections`

Key files:

- [DictionaryDatabase.kt](/Users/shamilidrisov/AndroidStudioProjects/dictionnary/app/src/main/java/com/example/dictionnary/data/local/DictionaryDatabase.kt:1)
- [DictionaryDao.kt](/Users/shamilidrisov/AndroidStudioProjects/dictionnary/app/src/main/java/com/example/dictionnary/data/local/DictionaryDao.kt:1)
- [DictionaryEntities.kt](/Users/shamilidrisov/AndroidStudioProjects/dictionnary/app/src/main/java/com/example/dictionnary/data/local/DictionaryEntities.kt:1)
- [CsvDictionaryImporter.kt](/Users/shamilidrisov/AndroidStudioProjects/dictionnary/app/src/main/java/com/example/dictionnary/data/local/CsvDictionaryImporter.kt:1)
- [DictionaryRepository.kt](/Users/shamilidrisov/AndroidStudioProjects/dictionnary/app/src/main/java/com/example/dictionnary/data/repository/DictionaryRepository.kt:1)

Seed import side effect:

- When the bundled CSV fingerprint or seed import version changes, the app clears all local Room tables and re-imports the seed data.
- That also removes locally stored favorites, recent searches, and saved corrections on the device.

## Search Behavior

- Search is performed against the selected source language
- Imported entries are normalized before search
- Search results are fetched in pages of 100 items
- Recent searches are recorded only when the query is not blank
- Favorites are stored locally in Room
- When a target language is selected, the UI still keeps Avar visible and may also keep Russian visible as bridge content

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

- `:app:compileDebugKotlin` succeeded on July 16, 2026
- `:app:testDebugUnitTest` succeeded on July 8, 2026

## Project Structure

- `app/src/main/assets/`
  - source CSV copied into app assets for offline seed
- `app/src/main/java/com/example/dictionnary/data/`
  - Room entities, DAO, DB, importer, repository
- `app/src/main/java/com/example/dictionnary/domain/`
  - domain models and language enums
- `app/src/main/java/com/example/dictionnary/presentation/`
  - Compose UI and view model
- `app/src/main/java/com/example/dictionnary/ui/theme/`
  - colors, typography, Material theme

## Recommended Next Steps

1. Simplify and document the search model so lookup language, display language, and direction behavior are explicit in code and UI.
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
- The current search implementation matches only in the selected source language, then filters visible translations afterward; this works, but the direction model is not clear enough yet.
- The current training flow is stateless practice over visible dictionary entries; it does not yet track progress, scoring, spaced repetition, or voice pronunciation.
- The Material 3 theme now uses explicit branded light/dark role pairs, including tertiary container roles and stronger outline colors for interactive boundaries.
- Search and detail screens now derive their major cards, bottom sheets, navigation bar, flashcards, and background gradients from `MaterialTheme.colorScheme` instead of hardcoded light palette tokens.
- The settings surface now exists, but privacy/support/legal content is still placeholder-level.
- Lightweight verification guidance lives in [VERIFICATION.md](/Users/shamilidrisov/AndroidStudioProjects/dictionnary/VERIFICATION.md:1).
