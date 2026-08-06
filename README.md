<<<<<<< HEAD
# NetSwitch

An Android app that watches your location and reminds you to switch networks when
you arrive home or leave: notification on arrival → tap to switch to Wi-Fi and turn
off mobile data; notification on departure → tap to switch back to mobile data.
Home location is set by **tapping the map to drop a pin** on an OpenStreetMap map —
no Google account or API key needed. The UI is a single dark "dashboard" screen:
a live geofence-radius readout, status cards, the map with a radius circle overlay,
a radius slider, and permission chips.

## Important: what this app can and can't do

Since Android 10, apps are **not allowed to silently flip Wi-Fi or mobile data on
your behalf** — that's a platform security restriction, not a limitation of this
app, and no app on the Play Store can get around it without root. So instead of
silently toggling anything, NetSwitch fires a notification when you arrive or leave,
and tapping it opens Android's built-in **Internet panel** — a single popup with
both the Wi-Fi and mobile data switches, so it's one or two taps rather than
digging through Settings.

## Project layout

- `app/` — the Android app (Kotlin + Jetpack Compose, Material 3)
- `.github/workflows/build-apk.yml` — builds the `.apk` automatically on GitHub

No Gradle wrapper binary is committed; the GitHub Actions workflow installs Gradle
itself, so there's nothing to compile locally unless you want to.

## Getting the .apk (no Android Studio needed)

1. Create a new **private** GitHub repository (any name).
2. Push this folder to it. In Windows Terminal (PowerShell), from inside the
   `NetSwitch` folder:
   ```powershell
   git init
   git add .
   git commit -m "Initial commit"
   git branch -M main
   git remote add origin https://github.com/<your-username>/<your-repo>.git
   git push -u origin main
   ```
3. On GitHub, open the **Actions** tab of your repo. A workflow called
   "Build APK" should already be running (it triggers automatically on push).
   If it's not there yet, click "I understand my workflows, go ahead and enable
   them", then re-run it from the Actions tab.
4. When it finishes (green check, a couple of minutes), click into the run, and
   download the **NetSwitch-debug-apk** artifact at the bottom of the page. It's
   a zip containing `app-debug.apk`.
5. If a step fails, open it in the Actions log — that's the fastest way to see
   what broke, and you can paste the error back to me.

You can re-trigger a build any time from the Actions tab with "Run workflow"
(no need to push a new commit).

## Installing on the Poco F7 Pro

1. Copy `app-debug.apk` to the phone (e.g. via a cloud drive, cable, or Telegram
   "Saved Messages").
2. Tap the file in your file manager to install it. HyperOS will likely show a
   Play Protect-style warning since it's not from the Play Store — choose
   **Install anyway**.
3. If installation is blocked outright: Settings → Apps → open the file manager
   or browser app you used → allow **Install unknown apps**.
4. First launch: tap the map to drop a home pin and hit **SAVE**, then flip the
   **Monitoring** switch — this walks you through granting location ("While using
   the app", then upgrade to "All the time"), and notifications.
5. For reliability, also do what the in-app tip on the Monitoring card says:
   set NetSwitch's battery saver to **No restrictions** and enable **Autostart**
   in the Security app. HyperOS kills background apps aggressively, and without
   these two, geofence events can be delayed or missed entirely.

## Notes on the build

- This is a **debug-signed** build, meant for installing on your own phone —
  not for the Play Store. It works exactly like a release build; the only
  difference is the signing key.
- Versions pinned in the Gradle files (AGP 8.5.2, Kotlin 2.0.21, compileSdk 34)
  were checked against current Maven/Google Maven listings to avoid a build
  breaking on a moved or yanked version.
- `minSdk` is 26 (Android 8.0+), comfortably below the F7 Pro's Android 15/16.
- Both arrival and departure always trigger a notification — there's no separate
  on/off toggle for each direction in this design (kept deliberately minimal).

## Changing things later

- Package name: `com.symdev.netswitch` (`app/build.gradle.kts` and the manifest).
- Default geofence radius: 150 m, adjustable 50–500 m from the radius slider.
- Notification text / behavior: `app/src/main/java/com/symdev/netswitch/notifications/NotificationHelper.kt`.
- Colors / fonts: `app/src/main/java/com/symdev/netswitch/ui/theme/`.
=======
# NetSwitch 📶

An Android geofence-based reminder that alerts you when you arrive home to switch to Wi‑Fi and turn off mobile data.

## ✨ Features

- 🗺️ Tap-to-set home location with Google Maps
- 📏 Adjustable geofence radius (50–500 m) with live visualization
- 🔔 High-priority notifications with one-tap action buttons
- 🌙 Background monitoring with foreground service
- 💾 DataStore persistence for home coordinates and settings
- 🎨 Modern Material 3 dark theme UI

## 📱 Requirements

- Android 8.0 (API 26) or higher
- Google Play Services (for Maps & Location)
- Google Maps API key

## 🛠️ Build Instructions

### Prerequisites

| Tool | Version | Notes |
|------|---------|-------|
| **Android Studio** | Hedgehog 2023.1.1+ | [Download](https://developer.android.com/studio) |
| **JDK** | 17 | Bundled with Android Studio |
| **Gradle** | 8.2 | Managed via wrapper (auto-downloaded) |
| **Android SDK** | API 34 | Install via SDK Manager |

## Setup

1. **Clone**
   ```bash
   git clone https://github.com/YOUR_USERNAME/NetSwitch.git
   cd NetSwitch
   ```

2. **Add your Google Maps API key**
   - Open `app/src/main/res/values/strings.xml`
   - Replace `YOUR_API_KEY_HERE` with your [Maps SDK API key](https://developers.google.com/maps/documentation/android-sdk/get-api-key)

3. **Build**
   ```bash
   ./gradlew assembleDebug
   ```

4. **Install**
   ```bash
   adb install app/build/outputs/apk/debug/app-debug.apk
   ```

## Permissions

| Permission | Why |
|------------|-----|
| `ACCESS_FINE_LOCATION` | Precise geofence boundary |
| `ACCESS_BACKGROUND_LOCATION` | Geofence works when app is closed |
| `POST_NOTIFICATIONS` | Arrival alert (Android 13+) |
| `FOREGROUND_SERVICE_LOCATION` | Keep monitoring alive |

## Architecture

```
MainActivity
 └── NetSwitchScreen (Compose)
      ├── HomeViewModel
      │    ├── PreferencesManager (DataStore)
      │    ├── GeofenceManager
      │    └── LocationMonitorService
      ├── GoogleMap (maps-compose)
      └── StatCards / Slider / PermissionChips
```

## License

MIT — see [LICENSE](LICENSE).
>>>>>>> 4796efa6d360366945c93e5b3f35e036dc81a035
