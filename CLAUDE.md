# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

**Heritage Hunt / Наследство+** — An Android location-based game where players discover Bulgarian heritage sites (Rila Monastery, Alexander Nevsky Cathedral, Plovdiv Theatre, etc.) by visiting them within a 250m GPS radius to earn points.

## Build Commands

```bash
# Debug APK
./gradlew.bat assembleDebug

# Release APK
./gradlew.bat assembleRelease

# Unit tests (JVM)
./gradlew test

# Run a single test class
./gradlew test --tests ExampleUnitTest

# Instrumented tests (requires connected device/emulator)
./gradlew connectedAndroidTest

# Clean
./gradlew clean
```

- **Min SDK:** 24 (Android 7.0) | **Target/Compile SDK:** 36
- **AGP:** 9.1.0 | **Kotlin:** 2.2.10 | **Java compatibility:** 11
- Dependency versions are managed via `gradle/libs.versions.toml`

## Architecture

**Pattern:** Activity-centric with a single Repository singleton — not standard MVVM despite importing Lifecycle libraries.

**Data flow:** Activities → `GameRepository` (singleton `object`) → SharedPreferences (JSON-serialized)

There is **no backend or Firebase**. All data is local only. The documentation notes Firebase as the intended next step for a real deployment.

### Key Components

| File | Role |
|---|---|
| `data/GameRepository.kt` | All business logic: scoring, discovery, teams, daily challenges, persistence |
| `ExplorerMapActivity.kt` | Google Maps, FusedLocationProvider, 250m discovery radius, camera |
| `TeamActivity.kt` | Team creation, invite codes, join requests, leaderboard |
| `GeoMenuActivity.kt` | Bottom navigation hub connecting all screens |
| `GeoSplashActivity.kt` | Entry point — redirects to Login or Menu based on session |
| `DailyReminderReceiver.kt` | BroadcastReceiver for AlarmManager-triggered daily notifications |
| `UiLanguageStore.kt` | Toggles between Bulgarian (default) and English at runtime |

### Models (`models/`)

`PlayerProfile`, `HeritagePlace`, `PlaceVisit`, `DailyChallenge`, `TeamInfo` — plain data classes, serialized to JSON in SharedPreferences.

### Activity Flow

```
GeoSplashActivity → GeoLoginActivity / RegisterActivity
                  → GeoMenuActivity (bottom nav)
                       ├── ExplorerMapActivity  (map tab)
                       ├── DiscoveriesActivity  (visit history & stats)
                       ├── GoalsActivity        (daily challenge)
                       └── TeamActivity         (teams & leaderboard)
```

## Game Mechanics

- **Discovery:** First visit to a place within 250m = 100 pts; repeat visit = 35 pts
- **Daily Challenge:** One place selected per day as bonus target = +150 pts on first visit that day
- **Teams:** Create (open/closed, max members), invite by code, approve requests, view leaderboard
- **Heritage places:** 6 hardcoded in `GameRepository` — no external data source
- **Notifications:** AlarmManager schedules a daily reminder to complete the daily challenge

## Localization

Full Bulgarian + English support. `res/values/strings.xml` = English, `res/values-bg/strings.xml` = Bulgarian. Language toggled via `UiLanguageStore` (stored in SharedPreferences).

## Key Dependencies

- **Maps/Location:** `play-services-maps:18.2.0`, `play-services-location:21.2.0`
- **AR (integrated, not heavily used):** `arcore:1.41.0`, `sceneform:1.23.0`
- **UI:** Material 3, ConstraintLayout, AppCompat
- **Lifecycle:** `lifecycle-viewmodel-ktx`, `lifecycle-livedata-ktx` (imported but Architecture Components pattern not fully applied)

## Permissions (AndroidManifest.xml)

`INTERNET`, `ACCESS_FINE_LOCATION`, `ACCESS_COARSE_LOCATION`, `CAMERA`, `POST_NOTIFICATIONS`

The Google Maps API key is embedded directly in `AndroidManifest.xml`.

## Admin Panel

A complete GeoGuesser admin section lives under `admin/`. Entry point: tap **"Admin Access"** on the login screen → `AdminLoginActivity`.

**Default credentials:** `admin@geoguesser.com` / `Admin@2024` (set in `AdminAccessManager`).

**Navigation:** Hamburger drawer with Dashboard → Add Geolocation → Manage Geolocations → Players → Logout.

**Architecture layers:**
- `database/` — Room DB (`AppDatabase`) with three entities: `PlayerEntity`, `GeoLocationEntity`, `GameResultEntity`. Seeds 7 demo locations + 5 players + 9 game results on first launch.
- `admin/repository/AdminRepository.kt` — thin wrapper over DAOs
- `admin/viewmodel/` — one ViewModel per screen, using `Flow → asLiveData()`
- `admin/adapter/` — `GeoLocationAdapter`, `PlayerAdapter`, `GameResultAdapter` (all `ListAdapter` with `DiffUtil`)
- `admin/` activities — `AdminLoginActivity`, `AdminDashboardActivity`, `AddGeoLocationActivity`, `ManageGeoLocationsActivity`, `ViewPlayersActivity`, `PlayerDetailsActivity`

**Build dependencies added:**
- Room 2.7.1 with KSP (`ksp = "2.2.10-1.0.31"`)
- Kotlin Android plugin + `kotlinOptions { jvmTarget = "11" }`

> If the KSP version fails to resolve, update `ksp` in `gradle/libs.versions.toml` to match the exact Kotlin version at https://github.com/google/ksp/releases.

**Security:** `AdminAccessManager` checks a SharedPreferences flag; all admin activities redirect to `GeoLoginActivity` if the flag is absent.

## Tests

Only boilerplate placeholder tests exist (`ExampleUnitTest.java`, `ExampleInstrumentedTest.java`). No business logic is tested. When adding tests, focus on `GameRepository` methods (scoring, distance checks, team operations).
