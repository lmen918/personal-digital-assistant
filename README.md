
---

## Project Structure

```
app/src/main/java/com/lmen918/retro/
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
