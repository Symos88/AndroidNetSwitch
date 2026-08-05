# NetSwitch

> Geofence-based Wi-Fi reminder for Android. All data stays on device.

NetSwitch monitors your location in the background and sends a high-priority notification when you arrive home, letting you toggle Wi-Fi or mobile data with one tap.

## Screenshots

*(Add screenshots here)*

## Features

- **Privacy-first** — No cloud, no accounts, no tracking. Everything is stored locally in DataStore.
- **Dark dashboard** — Jetpack Compose UI with live map, distance readout, and telemetry-style visuals.
- **Custom geofence radius** — Adjustable from 50 m to 500 m with live map preview.
- **Battery-aware** — Uses a foreground service only while monitoring; respects Doze and App Standby.
- **HyperOS / MIUI guidance** — In-app hints for Chinese OEM battery optimizers.

## Tech Stack

| Layer | Tech |
|-------|------|
| UI | Jetpack Compose (Material 3), `maps-compose` |
| Location | Google Play Services Location (Fused + Geofencing) |
| Storage | DataStore Preferences |
| Architecture | MVVM, StateFlow, Coroutines |
| Min SDK | 26 (Android 8.0) |
| Target SDK | 34 |

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
