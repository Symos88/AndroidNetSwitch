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
