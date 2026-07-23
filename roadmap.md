# Avar Dictionary App Roadmap

## 1. Product Goal

Build an offline-first Android dictionary app where:

- Avar is the anchor language
- Russian is the reliable source and bridge layer
- English is the main future user-facing target language

This is a dictionary app first, not a sentence translator. Word and phrase lookup are in scope. Sentence translation is a separate future track.

## 2. Current Codebase Status

The repository is no longer a prototype. Today it already includes:

- a working Android app built with Kotlin, Compose, Room, and MVVM
- a first-run CSV importer that seeds the local Room database from app assets
- local persistence for entries, translations, favorites, recent searches, and corrections
- search over imported dictionary rows with paged loading
- a main Compose screen with:
  - left-side language direction selector
  - Search / Favorites / Recent / Training tabs
  - search card and result list
  - favorite toggle
  - recent search history
  - entry detail screen

## 3. Current Product Truth

The app infrastructure is multilingual, but the seeded data is not.

What is real today:

- `AV -> RU`
- `RU -> AV`

What exists only as draft UX today:

- `AV -> EN`
- `EN -> AV`
- `AV -> DE`
- `DE -> AV`
- `AV -> ES`
- `ES -> AV`
- `AV -> FR`
- `FR -> AV`

Important constraint:

- the importer seeds Avar and Russian rows from the bundled CSV and imports English when a row includes it
- searching from `en`, `de`, `es`, or `fr` returns no real matches because those translations are not in the database yet

## 4. Dataset Status

Current baseline dataset:

- bundled Avar-Russian-English CSV
- imported from `app/src/main/assets/avar_russian_english.csv`
- used as the first-launch seed source for the Room database

Current dataset is good for:

- offline word lookup
- Avar-Russian dictionary search
- building a multilingual data model around a reliable Avar-Russian base with partial English coverage

Current dataset is not yet enough for:

- fully reviewed English lookup
- reviewed multilingual output
- sentence translation

## 5. Actual Technical Direction

Current stack in the repository:

- Kotlin
- Jetpack Compose Material 3
- Room
- KSP
- MVVM

Current import strategy:

- the app does not ship a prebuilt SQLite database from an external tools pipeline
- instead, it imports the CSV into Room on first launch if the local database is empty
- when the bundled seed fingerprint or import version changes, the app clears local Room tables and re-imports the CSV

This matters because earlier planning assumed a separate CSV-to-SQLite build pipeline. That is not how the current app works.

Current side effect of seed refresh:

- local favorites are lost
- local recent searches are lost
- local saved corrections are lost

## 6. Current Data Model

Current Room tables:

- `entries`
- `translations`
- `favorites`
- `recent_searches`
- `corrections`

The multilingual shape is already correct for future expansion:

- `entries` stores shared entry metadata such as category, type, notes, and source fields
- `translations` stores language-specific text and quality/source metadata
- `corrections` exists for a future suggestion workflow

Current supported language codes in app logic:

- `av`
- `ru`
- `en`
- `de`
- `es`
- `fr`
- `all`

## 7. Current Search Behavior

Search currently works like this:

1. match in the selected source language
2. rank prefix matches before broader substring matches
3. fetch the first 100 ranked matches quickly
4. let the user explicitly load more result pages
5. load all translations for matched entries
6. filter visible translations for the selected target language or "all"

This works for the real seeded data, but the model is still muddy in naming and UX:

- `sourceLanguageCode` is really the lookup language
- `targetLanguageCode` is really the display preference
- the UI presents this as one "direction", which implies more completeness than the data actually supports

## 8. Current UI Status

Implemented:

- main search screen
- entry detail screen
- language direction menu
- favorites tab
- recent searches tab
- initial training flashcards with chosen-word and random-word modes
- training should evolve into two modes:
  - flashcards
  - voice pronunciation practice
- search options menu for showing all translations and draft translations
- result rows with favorite action and translation status chips
- lightweight verification doc and first structural unit tests

Not implemented yet:

- copy/share actions
- correction submission UI
- supported-data gating for empty language directions

## 9.5 Next Session Focus

Next session should focus on release hardening, not feature expansion.

Priority order:

1. Replace placeholder privacy / support / legal content.
2. Remove, hide, or hard-disable unsupported language directions.
3. Decide whether draft English quality is acceptable for first public release.
4. Produce a release build and run a manual device QA pass.
5. Finalize release metadata and publishing details.

## 9. Immediate Roadmap

### Phase 1 - Clarify Search Model

Goals:

- make lookup vs display behavior explicit in code and UI
- reduce confusion around unsupported directions
- document the search contract clearly
- make paged search obvious to users

Tasks:

- decide whether to rename `sourceLanguageCode` and `targetLanguageCode`
- document exact ranking and filtering behavior
- decide whether to show a result counter like `100 shown, more available`
- disable, relabel, or hide directions that have no seeded data
- decide whether "Show all translations" is a direction or a display mode

### Phase 2 - Bookmark, Correction, and Utility Flows

Goals:

- replace the current bookmark icon shell with real persistence
- expose the existing corrections table through UI
- add practical utility actions

Tasks:

- add a real bookmarks table or redefine bookmark behavior if favorites already cover the need
- add correction submission flow
- add copy actions for words/translations
- add a small settings surface if the filters outgrow the overflow menu
- add support / helper / contact-admin links without introducing a backend-first admin system

### Phase 3 - English Layer

Goals:

- make `AV -> EN` and `EN -> AV` real instead of draft UI

Tasks:

- import or generate English translation rows
- mark them with draft/bridge metadata
- keep Russian visible as a reference layer where helpful

Required metadata shape:

- `languageCode = en`
- `sourceLanguageCode = ru`
- `translationSource = russian_bridge_auto`
- `checkedStatus = draft`

Important rule:

- do not present English as a stable direction until real rows exist in the database

### Phase 4 - Data Safety and Versioning

Goals:

- make the dataset lifecycle safer
- reduce destructive iteration risk

Current risk:

- Room uses `fallbackToDestructiveMigration`, which wipes local data on schema change

Tasks:

- define dataset version metadata
- decide how dataset refreshes should preserve user favorites and corrections
- replace destructive-only migration behavior when the schema settles
- add tests for importer, repository, and UI state behavior

### Phase 5 - Search and Learning Polish

Goals:

- improve discoverability and retention after the dictionary core is stable

Possible work:

- FTS search
- fuzzy search
- better suggestions
- export/share actions
- larger text / accessibility options
- training deck model built on favorites, bookmarks, or saved study items
- voice pronunciation mode with playback and pronunciation-check flow

## 10. Suggested Version Plan

### v0.1

- reliable offline Avar-Russian search
- first-run CSV seed into Room

### v0.2

- entry detail screen
- paged search / load-more
- clearer search model
- unsupported direction gating

### v0.3

- real bookmark or saved-entry behavior
- correction submission UI
- copy/share utilities

### v0.4

- English draft translation import
- real `AV -> EN` and `EN -> AV`
- explicit Russian bridge presentation

### v0.5

- safer migration/versioning strategy
- initial tests for importer and repository behavior

### v0.6

- better search ranking and suggestions
- training mode beyond simple flashcards, including voice pronunciation

## 11. Development Principle

Keep the base honest:

- Avar-Russian is the current reliable dictionary layer
- multilingual support is the architectural direction
- English and other languages should become real only when seeded data exists

The next work should improve correctness and clarity before expanding the language surface further.
