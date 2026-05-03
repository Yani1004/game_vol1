# Heritage Hunt

Heritage Hunt is an Android location-based game for discovering cultural and historical places in Bulgaria. Players explore a Google Map, visit real-world coordinates, open an AR camera view, scan a 3D location pin, earn points, complete a daily challenge, and compete through team leaderboards.

The project is built as a mobile programming coursework app, but the current codebase already includes a working game loop, local offline mode, optional Firebase multiplayer support, an admin panel, Google Maps, GPS checks, camera preview, and a packaged 3D AR pin model.

## Features

- Account registration and login
- Local profile storage with `SharedPreferences`
- Optional Firebase Authentication and Firestore integration
- Google Maps screen with Bulgaria's 100 National Tourist Sites as markers
- GPS distance checks before a place can be discovered
- Place detail cards with online Wikimedia/Wikipedia thumbnail images
- AR camera screen with a 3D location pin model
- Discovery radius of 30 meters
- Compass direction check before the AR pin appears
- Points for first and repeated discoveries
- Daily challenge bonus points
- Discovery history with visit time and earned points
- Team creation, invite codes, join requests, team scores, and leaderboards
- Admin login and dashboard
- Admin management for players, geolocations, and game results through Room
- Daily notification reminder
- Bulgarian/English language toggle in the main player flow

## How Discovery Works

1. The player opens the Explore map.
2. They select a place marker.
3. The app checks the player's GPS location.
4. The player must be within 30 meters of the selected place.
5. The AR camera opens and uses the phone compass to guide the player.
6. When the player is close enough and facing the place, the 3D AR pin appears.
7. Pressing `Scan location pin` saves the discovery and awards points.

Important: the current AR flow does not use computer-vision image recognition of a printed marker. It uses GPS, compass direction, the live camera preview, and a 3D glTF pin rendered as the location target.

## Tech Stack

- Kotlin
- Android SDK
- XML layouts
- Google Maps SDK for Android
- Google Play Services Location
- ARCore dependency
- Sceneform for 3D model rendering
- Firebase Auth, Firestore, and Storage
- Room database for admin/local structured data
- SharedPreferences for local offline game state
- Gradle Kotlin DSL

## Requirements

- Android Studio
- JDK 11 or compatible Android Studio bundled JDK
- Android SDK with compile SDK 36
- Google Maps API key
- Android device or emulator

For the AR/camera flow, a physical Android device is recommended because GPS, camera, and compass behavior are limited on many emulators.

## Setup

Clone the project and open it in Android Studio.

Create or update `local.properties` in the project root:

```properties
MAPS_API_KEY=your_google_maps_api_key
```

Optional admin credentials can also be provided through Gradle properties or `local.properties`:

```properties
ADMIN_EMAIL=your_admin_email
ADMIN_PASSWORD=your_admin_password
```

If these are not provided, admin login is disabled for that build.

## Firebase Setup

Firebase is optional. Without Firebase, the app stores player progress locally on the device.

To enable cloud login and multiplayer sync:

1. Create a Firebase project.
2. Add an Android app with package name `com.example.game_vol1`.
3. Enable Email/Password authentication.
4. Enable Cloud Firestore.
5. Enable Firebase Storage if you plan to use uploaded assets.
6. Download `google-services.json`.
7. Place it at:

```text
app/google-services.json
```

The Gradle script applies the Google Services plugin only when this file exists.

More notes are available in:

- `docs/firebase-setup.md`
- `docs/multiplayer-firebase-setup.md`

## Build

On Windows:

```powershell
./gradlew.bat :app:assembleDebug
```

On macOS/Linux:

```bash
./gradlew :app:assembleDebug
```

The debug APK is generated at:

```text
app/build/outputs/apk/debug/app-debug.apk
```

## Run

You can run the app from Android Studio or install the debug APK:

```powershell
./gradlew.bat :app:installDebug
```

Grant the requested permissions when prompted:

- Location
- Camera
- Notifications, on Android versions that require runtime notification permission

## Login

For a normal player account in local mode, register a new account from the app.

For admin access, configure `ADMIN_EMAIL` and `ADMIN_PASSWORD` in Gradle properties or `local.properties`.

The admin entry point is available from the login screen.

## Included Places

The local repository includes 100 entries based on Bulgaria's `100 National Tourist Sites` movement.

Examples include:

- Alexander Nevsky Cathedral
- Rila Monastery
- Plovdiv Roman Theatre
- Tsarevets Archaeological Reserve
- Nessebar Archaeological Museum
- Belogradchik Rocks
- Seven Rila Lakes
- Madara Rider
- Thracian Tomb of Kazanlak
- Pliska Archaeological Reserve

The current place catalog lives in:

```text
app/src/main/java/com/example/game_vol1/data/TouristSitesData.kt
```

The scoring and discovery logic lives in:

```text
app/src/main/java/com/example/game_vol1/data/GameRepository.kt
```

Place photos are loaded online from the direct `imageUrl` field when present, or from the Wikipedia summary thumbnail for each place's `wikipediaTitle`.

## AR Pin Asset

The 3D location pin is packaged as app assets:

```text
app/src/main/assets/models/location_pin/location_tag.gltf
app/src/main/assets/models/location_pin/location_tag.bin
```

The source asset folder is:

```text
map-pin-location-pin/
```

The AR pin renderer is implemented in:

```text
app/src/main/java/com/example/game_vol1/ArPinModelView.kt
```

The camera and discovery scan flow is implemented in:

```text
app/src/main/java/com/example/game_vol1/ArDemoActivity.kt
```

## Project Structure

```text
app/src/main/java/com/example/game_vol1/
  ArDemoActivity.kt                 AR camera and scan flow
  ArPinModelView.kt                 Sceneform 3D pin renderer
  ExplorerMapActivity.kt            Google Maps exploration screen
  DiscoveriesActivity.kt            Visit history
  GoalsActivity.kt                  Daily challenge screen
  TeamActivity.kt                   Teams and leaderboards
  GeoLoginActivity.kt               Login flow
  RegisterActivity.kt               Registration flow
  data/
    GameRepository.kt               Local game logic and scoring
    MultiplayerRepository.kt        Firebase-backed multiplayer sync
  admin/
    AdminLoginActivity.kt           Admin authentication screen
    AdminDashboardActivity.kt       Admin dashboard
    repository/AdminRepository.kt   Room-backed admin data access
  database/
    AppDatabase.kt                  Room database
    dao/                            Room DAO interfaces
    entity/                         Room entities
  models/                           App domain models

app/src/main/res/
  layout/                           XML screens
  drawable/                         UI backgrounds and icons
  values/                           strings, colors, themes

docs/
  PROJECT_DOCUMENTATION.md
  DEFENSE_NOTES.md
  firebase-setup.md
  multiplayer-firebase-setup.md
```

## Scoring

- First discovery of a place: 100 points
- Repeated discovery: 35 points
- Daily challenge bonus: 150 points

If the player belongs to a team, discovery points are also reflected in team scoring.

## Permissions

The app declares:

- `INTERNET`
- `ACCESS_FINE_LOCATION`
- `ACCESS_COARSE_LOCATION`
- `CAMERA`
- `POST_NOTIFICATIONS`

The camera feature is marked as not strictly required so the app can still install on devices without a camera, although the AR scan experience needs one.

## Documentation

Additional coursework and setup documentation:

- `docs/PROJECT_DOCUMENTATION.md`
- `docs/DEFENSE_NOTES.md`
- `docs/firebase-setup.md`
- `docs/multiplayer-firebase-setup.md`

## Current Limitations

- The heritage place list is mostly static in `GameRepository`.
- The AR pin is location and compass gated, not detected through image recognition.
- Firebase support is optional and depends on providing `google-services.json`.
- Room is currently used mainly by the admin/local data layer, while the player flow uses local preferences and optional Firebase sync.
- Accurate discovery requires real device GPS and compass data.

## License

No license has been added yet. Add one before publishing the project as reusable open-source software.
