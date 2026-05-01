# Multiplayer Firebase Setup

The app now has Firebase-backed multiplayer code for:

- Email/password cloud accounts
- Cloud player profiles
- Score syncing after monument discovery
- Live global leaderboard
- Cloud team creation and invite-code joining

To enable it on real devices:

1. Create a Firebase project.
2. Add an Android app with package name `com.example.game_vol1`.
3. Download `google-services.json`.
4. Put it here:

   `app/google-services.json`

5. Add the Google Services Gradle plugin only after the JSON file exists:

   Root/version catalog:

   ```toml
   google-services = "4.4.2"
   ```

   ```toml
   google-services = { id = "com.google.gms.google-services", version.ref = "google-services" }
   ```

   App plugins:

   ```kotlin
   alias(libs.plugins.google.services)
   ```

6. In Firebase Console, enable:

- Authentication -> Email/Password
- Firestore Database

7. Suggested demo Firestore rules:

```txt
rules_version = '2';
service cloud.firestore {
  match /databases/{database}/documents {
    match /users/{userId} {
      allow read: if request.auth != null;
      allow create, update: if request.auth != null && request.auth.uid == userId;
      match /visits/{visitId} {
        allow read, write: if request.auth != null && request.auth.uid == userId;
      }
    }

    match /teams/{teamId} {
      allow read: if request.auth != null;
      allow create, update: if request.auth != null;
      match /members/{memberId} {
        allow read, write: if request.auth != null && request.auth.uid == memberId;
      }
    }
  }
}
```

For production, move score awarding to Cloud Functions so players cannot fake points by modifying the app.
