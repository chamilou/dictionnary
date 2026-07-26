# Light Verification Flow

This project uses a light verification flow because the Avar-Russian CSV is based on a verified published dictionary.

The goal is not to re-prove lexical correctness entry by entry. The goal is to confirm that the app imports, searches, and displays the trusted data correctly.

## Automated Gate

Run these commands before merging or after a meaningful UI/data change:

```bash
./gradlew :app:compileDebugKotlin
./gradlew :app:testDebugUnitTest
./gradlew :app:lintDebug
```

What these checks cover:

- the app still compiles
- search normalization behavior still works
- the CSV asset still has the expected header shape
- the CSV asset still contains a substantial number of importable rows
- the Android lint gate still passes

For publication work, also use [RELEASE_CHECKLIST.md](/Users/shamilidrisov/AndroidStudioProjects/dictionnary/RELEASE_CHECKLIST.md:1).

## Latest Device Benchmark Snapshot

Latest recorded local macrobenchmark run:

- Date: July 25, 2026
- Device: Pixel 5, Android 14
- Command: `./gradlew :macrobenchmark:connectedBenchmarkAndroidTest`
- Result:
  - `coldStartup`: passed
  - `searchTypingAndScrollFrames`: failed
  - `searchTypingAndScrollPower`: failed
- Failure reason for both search benchmarks:
  `Search results list not found for queries ав, аб, а`

Treat this as a development snapshot, not a release gate. The benchmark module is kept local/optional and is not required for normal app shipping.

## Latest Build Verification

Latest recorded build verification:

- Date: July 26, 2026
- Commands:
  - `./gradlew :app:testDebugUnitTest`
  - `./gradlew :app:lintDebug`
  - `./gradlew help --warning-mode all`
- Result:
  - unit tests passed
  - lint passed
  - Gradle project warnings were cleaned up

## Manual Verification

Use this short checklist when you want to spot-check the trusted dictionary data in the app.

### 1. First-Run Import

- uninstall the app or clear app storage
- launch the app
- confirm the first search is responsive after initial seed

### 2. Avar -> Russian Spot Checks

Check a few known entries from the published dictionary, for example:

- `а`
- `аб`
- `áбадияб`

For each sample:

- search in `AV -> RU`
- open the result detail screen
- confirm the Russian meaning matches the dictionary
- confirm notes/category/source metadata look reasonable

### 3. Russian -> Avar Spot Checks

Check a few known Russian glosses, for example:

- `это`
- `вечный`

For each sample:

- search in `RU -> AV`
- open the result detail screen
- confirm the Avar headword matches the dictionary

### 4. Search Behavior

- verify prefix matches appear before broader substring matches
- verify the first result page loads quickly
- verify the `Load 100 more` action appears when many matches exist
- verify loading more appends results instead of replacing them
- verify `AV -> RU` search does not require Russian input
- verify `RU -> AV` search does not require Avar input

### 5. UI Regression

- result rows open the correct detail entry
- back from detail returns to the previous tab/state
- favorite toggle still works from list and detail views
- recent searches still restore query and direction

## If You Find a Bad Entry

Because the source is trusted, treat most issues like one of these:

- CSV correction needed
- importer/display issue
- search normalization issue

If the lexical content itself needs adjustment, correct the CSV and rerun the same flow.
