# Time for you

Android app for logging small mindful moments, tracking gentle streaks, and reading lightweight insights—styled with a calm, dark green theme. Data stays on device unless you configure optional coach features.

## Features

- **Home** — Log moments, see today’s entries, and a streak card (safe vs at risk).
- **Insights** — Last seven days as a simple bar chart (volume as insight, not “worth”).
- **Coach** — Activity-aware summary and suggestions (local fallbacks; optional richer wording with API key).
- **Profile** — Display name and clearing local data.

## Screenshots

Full UI notes and tables live in **[docs/README.md](docs/README.md)**. Previews:

**Home (streak at risk)**  
<img src="docs/screenshots/home-streak-at-risk.png" alt="Home streak at risk" width="260" />

**Home (streak + today)**  
<img src="docs/screenshots/home-with-streak.png" alt="Home with streak" width="260" />

**Coach**  
<img src="docs/screenshots/coach.png" alt="Coach" width="260" />

**Insights**  
<img src="docs/screenshots/insights.png" alt="Insights" width="260" />

**Profile**  
<img src="docs/screenshots/profile.png" alt="Profile" width="260" />

## Requirements

- Android Studio with Android SDK **35** (see `app/build.gradle.kts`).
- **JDK 17**

## Build

From the project root:

```bash
./gradlew :app:assembleDebug
```

Install the debug APK from `app/build/outputs/apk/debug/`.

## Documentation

- **[docs/README.md](docs/README.md)** — UI tour with screenshots and main components.

## Privacy

Profile copy in the app states that this space stays on your device; clearing data removes local logs and related state from the app’s storage.
