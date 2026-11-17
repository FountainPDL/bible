# FountainPDL's Bible — Native Android Edition

A complete, fully offline KJV & NIV Bible app for Android, built entirely in native Java.

## Why Native Java

This version replaces the earlier Capacitor/WebView build entirely. Every screen,
dialog, and interaction is implemented with real Android View, Fragment, and
RecyclerView components — no JavaScript, no WebView, no bridge layer. This directly
fixes the class of bugs that only exist in a hybrid WebView app:

| Bug in the old build | Why it happened | Fixed by |
|---|---|---|
| Text-to-speech stuck "initializing" | Depended on the browser's Web Speech API inside a WebView, which Android only partially implements | TTSManager wraps android.speech.tts.TextToSpeech directly — the OS's real TTS engine, with a reliable onInit() callback |
| Note/Sermon title disappearing when tapping elsewhere | React was recreating the input component on every re-render, resetting its state | Plain native EditText inside a Dialog — nothing ever remounts it while the dialog is open |
| Paste not working reliably | Same remounting issue disrupted the long-press context menu | Native EditText — Cut/Copy/Paste/Select All work exactly as they do system-wide |
| Sermons "locked" to KJV | Verse text was resolved once and baked into the saved sermon | SermonBlock stores only the reference (e.g. "John 3:16-18"); text is resolved live from BibleDataManager using whatever translation is active, every time it's displayed |
| Red-letter text inconsistent | Partial/inconsistent verse-range data | Exact phrase-level ranges generated from the NIV 2011 and KJV+ source markup (`assets/bible/redletter-*.json`), applied by JesusWords the same way for every verse |
| Read page blank on launch / settings slow or ignored / messy theme / TTS banner stuck | Load callbacks lost, settings not pushed to screens, scattered colours, stop() never notified the UI | Multi-callback loader, central `onSettingsChanged()`, one theme engine, TTSManager.stop() notifies the UI |

## Project Structure

```
app/src/main/
├── java/com/fountainpdl/bible/
│   ├── App.java                   - Application: syncs DayNight mode with the theme
│   ├── MainActivity.java          - app shell: toolbar, bottom nav, fragment host, live settings/theme push
│   ├── models/                    - plain data classes (Verse, Note, Sermon, Bookmark, ...)
│   ├── utils/
│   │   ├── BibleDataManager.java  - loads & queries the Bible JSON assets, search
│   │   ├── PrefsManager.java      - SharedPreferences + Gson storage for everything
│   │   ├── TTSManager.java        - native TextToSpeech wrapper
│   │   ├── AppTheme.java          - resolves a full, contrast-safe palette from settings
│   │   ├── ThemePresets.java      - the 19 built-in themes (only place colour literals live)
│   │   ├── ThemeHelper.java       - themed shapes, pills, cards, inputs
│   │   ├── ColorUtils.java        - HSL tinting math
│   │   ├── BooksData.java         - book lists, chapter counts
│   │   ├── VerseCounts.java       - exact verse count for all 1,189 chapters
│   │   ├── JesusWords.java        - loads red-letter ranges, exposes ranges per verse
│   │   ├── ReadingPlans.java      - marathon reading plan definitions
│   │   └── WordAnnotationStore.java - word/phrase-level annotations
│   ├── fragments/
│   │   ├── HomeFragment.java      - dashboard (replaces the old redundant Navigate tab)
│   │   ├── ReadFragment.java      - chapter reading, gestures, TTS, selection actions
│   │   ├── SearchFragment.java    - full-text search
│   │   ├── LibraryFragment.java   - history/bookmarks/highlights/notes/sermons/marathon
│   │   └── MoreFragment.java      - settings hub + all sub-pages
│   ├── adapters/                  - RecyclerView adapters
│   └── dialogs/                   - Note, Sermon, Marathon, Navigate picker, Word annotation
├── assets/bible/                  - kjv-*.json, niv-*.json, redletter-kjv.json, redletter-niv.json
└── res/                           - layouts, drawables, values
```

## Navigation

The bottom nav is Home, Read, Search, Library, More. There is a single Navigate
entry point — tap the book/chapter chip in the top bar — instead of the old
duplicate Navigate tab. Home now shows a continue-reading card, verse of the day,
and quick links.

## Data

Both translations (plus red-letter range files) are bundled as JSON in assets/bible/ and loaded into memory on
a background thread at startup:

| Translation | Verses | Chapters |
|---|---|---|
| KJV (1769, Public Domain) | 31,102 | 1,189 |
| NIV (2011) | from licensed source, empty verses skipped | 1,189 |

No network access is used anywhere in the app.

## Building

Pushing to main triggers .github/workflows/build-apk.yml, which:
1. Sets up JDK 17 and accepts the preinstalled Android SDK licences
2. Runs gradle assembleDebug
3. Uploads the APK as a workflow artifact (30-day retention)

No Node, npm, or Capacitor steps — it's a plain Gradle Android build.

### Updating without uninstalling

Debug and release builds are signed with the committed `app/fountainpdl.jks`, and
`versionCode = 100 + GITHUB_RUN_NUMBER`, so every CI build is a valid in-place
update over the last. Because the keystore is committed, **keep this repository
private**. (Moving from an APK signed with a different key needs one final
uninstall.) The current version is 4.0 and is shown on the About page.

## Themes

19 built-in dark/light themes plus a custom theme (Light, Dark, AMOLED, Sepia with
your own primary and accent colours) and optional Auto Night Mode. All colours
come from `ThemePresets` or neutral tokens in `res/values/colors.xml`; nothing else
in the app hard-codes a colour. Text and icon colours are nudged to stay readable.

## Features

- Read and Focus reading modes
- Swipe left/right for next/previous chapter; long-press a verse to read aloud from there
- Highlight, underline, bookmark, word/phrase annotation, notes — all linked back to their verse
- Sermon/study builder with live-translated scripture blocks
- Reading marathons with 8 built-in plans and per-book progress
- Full-text search across the whole Bible
- 19 themes + custom theme, Auto Night Mode, adjustable red-letter colour
- Red-letter words of Christ in both KJV and NIV
- Built-in Help & Shortcuts page
- Native offline text-to-speech
