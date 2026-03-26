# Personal Digital Assistant

A personal digital assistance application (traditional PDA style) to help with managing the time we have to navigate the things in our lives.

## Features

### 📅 Timeline & Events
- Create, edit, and delete calendar events with title, description, start/end time
- All-day event support
- View events on a chronological timeline sorted by start time

### 🏷️ Tag Management
- Full CRUD (Create, Read, Update, Delete) for tags
- Assign colors to tags using a custom HSV color wheel picker
- Multi-select tags when creating/editing events
- Color-coded tag chips displayed on event cards

### 🔔 Notifications
- Choose per-event whether to receive in-app notifications
- Configurable minutes-before reminder (5–60 minutes)
- Uses Android `AlarmManager` with exact alarms for reliable delivery
- Alternatively, let your external calendar app handle reminders

### 📆 Calendar Sync
- Sync events to the device's default calendar via Android's `CalendarContract` API
- Compatible with **Google Calendar**, **Microsoft Outlook**, and any calendar app registered on the device
- Runtime permission requests for calendar read/write access

### 🔄 Retrospective
- Three timed 5-minute sessions to capture **Positive**, **Neutral**, and **Negative** reflections
- Bullet-point entry during each timed session (press Enter or tap + to add)
- Visual countdown timer with progress indicator
- After all sessions complete, a **Journal** phase opens pre-populated with all session entries
- Write a free-form journal entry summarizing the week
- Save the journal as a **Markdown (`.md`) file** to a user-chosen location on the device (via Android Storage Access Framework)

---

## Tech Stack

| Layer | Technology |
|---|---|
| Language | **Kotlin** |
| UI | **Jetpack Compose** (Material 3) |
| Architecture | **MVVM** (ViewModel + StateFlow/Compose State) |
| Navigation | **Navigation Compose** with bottom nav bar |
| Database | **Room** (SQLite) with KSP annotation processing |
| Dependency Injection | **Hilt** |
| Calendar Integration | Android **CalendarContract** API (ContentProvider) |
| Notifications | **AlarmManager** (exact alarms) + `BroadcastReceiver` |
| File Storage | Android **Storage Access Framework** (SAF) |
| Build System | **Gradle Kotlin DSL** + `libs.versions.toml` version catalog |
| Min SDK | **26** (Android 8.0 Oreo) |
| Target SDK | **35** (Android 15) |

---

## Project Structure

```
app/src/main/java/com/lmen918/pda/
├── data/
│   ├── local/              # Room database, entities, DAOs
│   └── repository/         # Repository implementations
├── di/                     # Hilt dependency injection modules
├── domain/
│   ├── model/              # Domain models (Tag, Event, RetrospectiveEntry)
│   └── repository/         # Repository interfaces
├── notifications/          # AlarmManager scheduling + BroadcastReceiver
└── ui/
    ├── components/         # Reusable Composables (ColorWheelPicker, TagChip, etc.)
    ├── events/             # Timeline screen + Event detail/create/edit screen
    ├── navigation/         # NavGraph + Screen routes
    ├── retrospective/      # Retrospective timed sessions + Journal
    ├── tags/               # Tag management screen
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

> **Note:** Calendar sync requires the `READ_CALENDAR` and `WRITE_CALENDAR` permissions. Exact-alarm notifications require `SCHEDULE_EXACT_ALARM` or `USE_EXACT_ALARM` (granted at runtime on Android 12+).
