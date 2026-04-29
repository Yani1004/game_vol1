# Firebase Setup (GeoGuesser)

This project now targets Firebase for cloud data/auth.

## Services to enable

1. Firebase Authentication (Email/Password)
2. Cloud Firestore
3. Firebase Storage

## Android app setup

1. Create a Firebase project in Firebase Console.
2. Add Android app with package name:
   - `com.example.game_vol1`
3. Download `google-services.json`.
4. Put it in:
   - `D:\university\6semestar\PMU\igra\app\google-services.json`
5. Sync Gradle.

## Dependencies already added

- Firebase BoM
- `firebase-auth`
- `firebase-firestore`
- `firebase-storage`

## Data model mapping

- `users` collection
  - profile, role (`ADMIN`/`PLAYER`), score stats
- `geolocations` collection
  - name, country, city, latitude, longitude, difficulty, imageUrl, description
- `game_results` collection
  - playerId, geolocationId, score, isCorrect, distanceKm, playedAt

## Security rules (minimum)

- Only authenticated users can read own player data.
- Only admins can create/update/delete `geolocations`.
- Game results can be written by authenticated players for their own user id.

## Note

The app still contains Room local storage classes.  
Next migration step is to switch repositories/viewmodels to Firebase-backed sources and keep Room as optional offline cache.
