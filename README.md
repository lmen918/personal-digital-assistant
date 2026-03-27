# Retrospective

A focused retrospective app to help you reflect on your week — capturing what went well, what was neutral, and what could improve, then consolidating everything into a Markdown journal.

> Scope note: the previous timeline/events/tags modules were removed. The app is now intentionally retrospective-first.

## Features

### 🔄 Guided Retrospective
- Three timed 5-minute sessions to capture **Positive**, **Neutral**, and **Negative** reflections
- Bullet-point entry during each timed session (press Enter or tap + to add)
- Visual countdown timer with progress indicator
- After all sessions complete, a **Journal** phase opens pre-populated with all session entries
- Write a free-form journal entry summarising the week
- Save the journal as a **Markdown (`.md`) file** in `Documents/retrospective`
- Show a completion confirmation with the saved file location

### 💾 Journal Storage
- The app initializes a default journal folder under `Documents/retrospective` on startup
- Journal filenames use a timestamp format: `retrospective_yyyyMMdd_HHmm.md`

### ⚙️ Settings & Reminders
- Open Settings from the top-right cog icon
- Configure recurring reminders for retrospectives:
  - **Weekly** on a selected weekday and time (e.g., Saturday at 8:00 AM)
  - **Monthly** on a selected day of month and time (e.g., the 28th)
- Reminder configuration persists across app restarts and device reboots
- Saving settings shows a snackbar confirmation, then returns to the previous screen

---

## Migration Notes

- The app was refactored from a broad PDA concept to a focused retrospective workflow.
- Removed modules:
  - Timeline / Events UI and view models
  - Tag management UI and repositories
  - Calendar/event notification scheduling
  - Room database entities, DAOs, and repository layer
- Navigation was simplified to two screens: `Retrospective` and `Settings`.
- Journal saving moved from user-picked SAF location to a default `Documents/retrospective` path.
- Reminder scheduling is now settings-driven with persisted weekly/monthly rules.

---

## Tech Stack

| Layer | Technology |
|---|---|
| Language | **Kotlin** |
| UI | **Jetpack Compose** (Material 3) |
| Architecture | **MVVM** (ViewModel + Compose State) |
| Dependency Injection | **Hilt** |
| Persistence | **DataStore Preferences** (reminder settings) |
| Scheduling | **AlarmManager** + `BroadcastReceiver` |
| File Storage | `Documents/retrospective` via **MediaStore** (Android 10+) |
| Build System | **Gradle Kotlin DSL** + `libs.versions.toml` version catalog |
| Min SDK | **26** (Android 8.0 Oreo) |
| Target SDK | **35** (Android 15) |

---

## Project Structure

```
app/src/main/java/com/lmen918/pda/
├── journal/                # Default folder setup + markdown save helpers
├── reminder/               # Reminder settings model, scheduler, receivers
└── ui/
    ├── retrospective/      # RetrospectiveScreen + RetrospectiveViewModel
    ├── settings/           # SettingsScreen + SettingsViewModel
    └── theme/              # Material 3 theme, colors, typography
```

---

## Getting Started

1. Clone the repository
2. Use **JDK 21** (recommended for this Gradle/AGP setup)
3. Open in **Android Studio Ladybug** (or newer)
4. Sync Gradle
5. Run on a device or emulator with **API 26+**

### Build From Terminal

```bash
./gradlew :app:assembleDebug
```

If your system default JDK is newer (for example Java 25), point Gradle to JDK 21:

```bash
export JAVA_HOME="/path/to/jdk-21"
export PATH="$JAVA_HOME/bin:$PATH"
./gradlew :app:assembleDebug
```

### Enable Repository Hooks (Recommended)

This repo includes hooks that block accidental commits and pushes of local tooling artifacts (like `.tools/`) and files larger than 50MB.

| Hook | When it runs | What it blocks |
|---|---|---|
| `pre-commit` | Every `git commit` | Staged `.tools/*` files and blobs >50MB |
| `pre-push` | Every `git push` | Same policy applied across all new commits in the push |

```bash
git config core.hooksPath .githooks
chmod +x .githooks/pre-commit .githooks/pre-push
```


