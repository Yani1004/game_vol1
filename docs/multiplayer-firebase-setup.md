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

5. The Google Services Gradle plugin is already prepared in the project. It is applied only when `app/google-services.json` exists, so the app keeps building before Firebase is connected.

6. In Firebase Console, enable:

- Authentication -> Email/Password
- Firestore Database

7. Publish the included demo Firestore rules from `firestore.rules`, or paste that file into Firebase Console -> Firestore Database -> Rules.

For production, move score awarding to Cloud Functions so players cannot fake points by modifying the app.
