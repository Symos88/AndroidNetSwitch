<<<<<<< HEAD
<<<<<<< HEAD
# NetSwitch
=======
# 📡 NetSwitch
>>>>>>> 574417521643f689f8fd46ab2503079bf9be24f5

> **Smart network switching based on location.**
> An Android app that monitors your geofence and prompts you to toggle Wi-Fi/Mobile Data when arriving home or leaving—no Google account or API key required.

[![Kotlin](https://img.shields.io/badge/Kotlin-2.0.21-7F52FF?logo=kotlin&logoColor=white)](https://kotlinlang.org/)
[![Compose](https://img.shields.io/badge/Jetpack_Compose-Material_3-4285F4?logo=jetpackcompose&logoColor=white)](https://developer.android.com/jetpack/compose)
[![Android](https://img.shields.io/badge/Min_SDK-26_(Android_8.0+)-3DDC84?logo=android&logoColor=white)](https://developer.android.com/)
[![License](https://img.shields.io/badge/License-MIT-blue.svg)](LICENSE)

## ✨ Features

-   **🗺️ Privacy-First Maps:** Uses OpenStreetMap. Set your home location by simply tapping to drop a pin.
-   **🔔 Smart Notifications:** Arrival triggers a "Switch to Wi-Fi" prompt; departure triggers a "Switch to Mobile Data" prompt.
-   **⚡ One-Tap Panel:** Tapping the notification opens Android's built-in Internet panel for instant toggling.
-   **🌙 Modern Dashboard:** Single-screen dark UI with live geofence radius readout, status cards, map overlay, and permission chips.
-   **🔧 Zero Config Build:** No Gradle wrapper binary committed. CI handles the build environment automatically.

---

## ⚠️ Important: Platform Limitations

> [!NOTE]
> **Why doesn't it switch automatically?**
> Since Android 10, apps cannot silently toggle Wi-Fi or Mobile Data due to OS security restrictions. This applies to all non-rooted Play Store apps.
>
> **The Solution:** NetSwitch fires a contextual notification. Tapping it opens the native Android Internet Panel, allowing you to toggle both radios in 1–2 taps without navigating Settings.

---

## 🏗️ Project Structure

| Path | Description |
| :--- | :--- |
| `app/` | Android application (Kotlin + Jetpack Compose, Material 3) |
| `.github/workflows/build-apk.yml` | Automated APK build pipeline |
| `app/src/.../notifications/` | Notification text & behavior logic |
| `app/src/.../ui/theme/` | Colors, typography, and theming |

---

## 📦 Getting the APK (No Android Studio Needed)

This project uses GitHub Actions to build the APK. Follow these steps to generate your own installable file:

### 1. Push to GitHub
Create a new **private** repository and push this folder:

<<<<<<< HEAD
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
=======
```powershell
git init
git add .
git commit -m "Initial commit"
git branch -M main
git remote add origin https://github.com/<your-username>/<your-repo>.git
git push -u origin main
```

### 2. Trigger & Download
1.  Navigate to the **Actions** tab in your repo.
2.  If prompted, click *"I understand my workflows, go ahead and enable them"*.
3.  Wait for the **Build APK** workflow to complete (green checkmark).
4.  Click the run → Scroll to **Artifacts** → Download `NetSwitch-debug-apk`.
5.  Extract the ZIP to get `app-debug.apk`.

> [!TIP]
> You can manually re-trigger builds anytime via **"Run workflow"** in the Actions tab—no new commit required.

---

## 📱 Installation Guide (Android)

1.  **Transfer:** Copy `app-debug.apk` to your phone (Cloud Drive, USB, or Telegram Saved Messages).
2.  **Install:** Tap the file in your File Manager. Select **Install anyway** if Play Protect warns about sideloading.
    -   *Blocked?* Go to **Settings → Apps → [Your File Manager] → Install unknown apps → Allow**.
3.  **First Launch:**
    -   Tap the map to set your Home pin → Hit **SAVE**.
    -   Toggle the **Monitoring** switch.
    -   Grant Location permissions (*While using* → upgrade to *All the time*) and Notifications.
4.  **⚡ Critical: Prevent Background Kills**
    HyperOS aggressively kills background processes. For reliable geofencing:
    -   Set NetSwitch Battery Saver to **No restrictions**.
    -   Enable **Autostart** in the Security app.

---

## 🛠️ Technical Notes

| Detail | Value | Notes |
| :--- | :--- | :--- |
| **Build Type** | Debug-signed | Identical functionality to release; different signing key. Not for Play Store. |
| **AGP** | 8.5.2 | Pinned against current Maven listings. |
| **Kotlin** | 2.0.21 | Pinned for stability. |
| **Compile SDK** | 34 | Verified compatible. |
| **Min SDK** | 26 | Android 8.0+ (F7 Pro runs Android 15/16). |
| **Geofence Radius** | 50m – 500m | Default: 150m. Adjustable via slider. |
| **Triggers** | Bidirectional | Arrival & Departure always notify. No separate toggles (minimalist design). |

### Customization Reference

-   **Package Name:** `com.symdev.netswitch` → Edit in `app/build.gradle.kts` & `AndroidManifest.xml`
-   **Notifications:** `NotificationHelper.kt`
-   **Theme/UI:** `ui/theme/` directory
-   **Default Radius:** Modify constant in geofence manager

---

<div align="center">
  <sub>Built with ❤️ using Kotlin & Jetpack Compose</sub>
</div>

### Key Improvements Made:
1.  **Visual Hierarchy:** Added badges, emojis, and section dividers to break up walls of text.
2.  **Callout Blocks:** Used GitHub's `> [!NOTE]` and `> [!TIP]` syntax to highlight the Android 10 limitation and the HyperOS battery optimization warning—these are the two most critical pieces of information users miss.
3.  **Tables:** Converted the project layout and technical specs into clean tables for faster scanning.
4.  **Code Blocks:** Formatted the git commands as PowerShell code blocks with proper syntax highlighting.
5.  **Scannable Headings:** Changed generic headings like "Changing things later" to descriptive ones like "Customization Reference".
6.  **Badge Shield:** Added dynamic shields for tech stack visibility at the top.
7.  **Centered Footer:** Added a subtle centered footer for a polished finish.

This README now follows modern open-source conventions while preserving every technical detail and warning from your original draft.
>>>>>>> 574417521643f689f8fd46ab2503079bf9be24f5
