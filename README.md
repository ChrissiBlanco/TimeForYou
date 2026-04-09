# Time for You

**Time for You** is a mindfulness Android app for logging short moments, maintaining low-pressure streaks, and reviewing the last seven days in a bar chart. All logs and profile-related state live in a local database; nothing is synced to a backend by default. An optional **Coach** screen enriches copy with the OpenAI API when a key is provided; otherwise the app uses deterministic local text. The UI is Compose + Material 3 with a restrained dark green palette.

## Screenshots

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

## Architecture

The app follows **MVVM** with a thin **domain** layer between UI and persistence.

- **View (Compose)** — Screens observe `StateFlow`-backed UI state via `collectAsStateWithLifecycle()`. User actions call methods on the `ViewModel`; navigation is handled with Navigation Compose.
- **ViewModel** — One per feature screen (Home, Insights, Coach, Profile). ViewModels are `@HiltViewModel`-annotated, inject repositories and **use cases**, and combine `Flow`s from `TimeRepository` (and profile prefs where needed) into a single `UiState`. Side effects (logging a moment, refreshing coach advice) run in `viewModelScope` with coroutines.
- **Domain** — `TimeRepository` and `CoachAdviceRepository` are interfaces; **use cases** encapsulate rules for dashboard snapshots, streak semantics, coach summaries, and local-vs-AI coach flows. Domain models are separate from Room entities; **mappers** convert at the data boundary.
- **Data** — **Room** exposes DAOs that return `Flow` for reactive reads. `TimeRepositoryImpl` maps entities to domain models and implements streak and 7-day aggregation. **Hilt** wires singletons (`AppDatabase`, `OkHttpClient`, repository implementations). If `OPENAI_API_KEY` is empty at build time, `CoachAdviceRepository` is a no-op implementation; otherwise it uses **OkHttp** against the OpenAI API.

End-to-end flow for logging a moment: UI → ViewModel → repository `suspend`/`Flow` → Room insert → DAO `Flow` emits → ViewModel recomputes state → Compose recomposes. Coach text follows the same pattern, with an optional one-shot network call inside the domain/data coach path when configured.

## Tech stack

| Technology | Purpose |
|------------|---------|
| Kotlin | Language; shared conventions across UI, domain, and data layers |
| Jetpack Compose | Declarative UI; state driven by ViewModels |
| Material 3 | Theming and components (calm dark green product styling) |
| Room | On-device persistence; reactive reads via `Flow` |
| Hilt | Compile-time DI for ViewModels, DB, repositories, OkHttp |
| Kotlin Coroutines & Flow | Async work, stream composition in repositories and ViewModels |
| Navigation Compose | Typed routes between Home, Insights, Coach, Profile |
| MVVM | Separation of UI state and Android lifecycle from business logic |
| OkHttp | HTTP client for optional OpenAI coach requests |
| OpenAI API (optional) | Richer coach wording when `OPENAI_API_KEY` is set at build time |

## Getting started

**Requirements**

- Android Studio (or compatible IDE) with **Android SDK 35**
- **JDK 17**

**Build**

From the repo root:

```bash
./gradlew :app:assembleDebug
```

Install the debug APK from `app/build/outputs/apk/debug/`.

**Optional: OpenAI coach**

1. Create or edit **`local.properties`** in the project root (same level as `settings.gradle.kts`). This file is gitignored.
2. Add:

   ```properties
   OPENAI_API_KEY=sk-...
   ```

   Optionally override the model:

   ```properties
   OPENAI_MODEL=gpt-4o-mini
   ```

3. Sync Gradle / rebuild. Keys are read in `app/build.gradle.kts` and exposed as `BuildConfig` fields; the app selects a real `CoachAdviceRepository` only when the key is non-blank.

## Design decisions

- **Room instead of a remote backend** — The product goal is private, offline-capable journaling. SQLite via Room gives durable local storage, `Flow`-based UI updates, and no account or sync surface to secure.
- **Hilt over manual service locators** — Constructor injection scales as screens and repositories grow; `@HiltViewModel` integrates with the Jetpack lifecycle and keeps graph configuration in one module.
- **Offline-first coach** — Coach copy has a local fallback path so the screen stays usable without network or API keys; OpenAI is an enhancement, not a hard dependency at runtime for core logging.
- **Use cases on top of repositories** — Streak rules, “today” windows, and coach summaries stay testable and readable instead of growing inside ViewModels or Composables.
