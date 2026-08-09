# 📡 NetSwitch

> **Smart network switching based on location.**
> An Android app that monitors your geofence and prompts you to toggle Wi-Fi/Mobile Data when arriving home or leaving—no Google account or API key required.

[![Kotlin](https://img.shields.io/badge/Kotlin-2.0.21-7F52FF?logo=kotlin&logoColor=white)](https://kotlinlang.org/)
[![Compose](https://img.shields.io/badge/Jetpack_Compose-Material_3-4285F4?logo=jetpackcompose)](https://developer.android.com/jetpack/compose)
[![Android](https://img.shields.io/badge/Min_SDK-26_(Android_8.0+)-3DDC84?logo=android&logoColor=white)](https://developer.android.com/)
[![License](https://img.shields.io/badge/License-MIT-blue.svg)](LICENSE)

## ✨ Features

- **🗺️ Privacy-First Maps:** Uses OpenStreetMap. Set your home location by tapping to drop a pin.
- **🔔 Smart Notifications:** Arrival prompts Wi-Fi; departure prompts mobile data.
- **⚡ One-Tap Panel:** Tapping the notification opens Android's built-in Internet panel.
- **🌙 Modern Dashboard:** Dark UI with live geofence radius, status cards, map overlay, and permission chips.
- **🔧 CI Builds:** GitHub Actions builds and tests the app automatically.

---

## ⚠️ Important: Platform Limitations

> [!NOTE]
> **Why doesn't it switch automatically?**
> Since Android 10, regular apps cannot silently toggle Wi-Fi or Mobile Data because of OS security restrictions.
>
> **The solution:** NetSwitch fires a contextual notification. Tapping it opens the native Android Internet Panel, where the requested network can be changed.

---

## 🏗️ Project Structure

| Path | Description |
| :--- | :--- |
| `app/` | Android application (Kotlin + Jetpack Compose, Material 3) |
| `.github/workflows/build-apk.yml` | Debug/test and signed release pipeline |
| `app/src/.../notification/` | Notification text and behavior |
| `app/src/.../geofence/` | Geofence registration and transition handling |
| `app/src/.../ui/theme/` | Colors, typography, and theming |
| `app/src/test/` | JVM unit tests |

---

## 📦 Getting the APK

The repository uses GitHub Actions. For normal pushes, pull requests, and manual workflow runs:

1. Open the **Actions** tab.
2. Run **Build APK** if needed.
3. Wait for the green build.
4. Download the `NetSwitch-debug-apk` artifact.
5. Extract `app-debug.apk`.

Release tags (`v*`) use the release signing secrets configured in GitHub Actions. The release keystore is never committed to the repository.

### Release signing secrets

Configure these repository secrets before creating a release tag:

- `SIGNING_KEY` — base64-encoded JKS/keystore file
- `KEY_ALIAS`
- `KEY_PASSWORD`
- `STORE_PASSWORD`

The CI decodes the keystore only inside the ephemeral runner and verifies the resulting APK signature before publishing the GitHub Release.

---

## 📱 Installation Guide

1. Transfer `app-debug.apk` to the phone.
2. Install it through the file manager.
3. Tap the map to set the Home location and press **SAVE**.
4. Enable **Monitoring**.
5. Grant foreground location, background location, and notification permissions as requested.
6. On OEMs with aggressive background management, allow NetSwitch to run without battery restrictions and enable its autostart option where applicable.

> Android 11+ requires background location to be enabled through the system settings page rather than the normal permission dialog.

---

## 🛠️ Technical Notes

| Detail | Value | Notes |
| :--- | :--- | :--- |
| **Build Type** | Debug-signed / optionally release-signed | Release signing is supplied only through CI secrets. |
| **AGP** | 8.5.2 | Kept stable for the current migration; not upgraded blindly. |
| **Kotlin** | 2.0.21 | Pinned for the current project stack. |
| **Compile SDK** | 36 | Android 16. |
| **Target SDK** | 36 | Android 16. |
| **Min SDK** | 26 | Android 8.0+. |
| **Geofence Radius** | 50m – 500m | Default: 150m; persisted values are clamped. |
| **Triggers** | ENTER + EXIT | Arrival and departure notifications. |
| **Background service** | Geofencing API | No unnecessary permanent foreground location service. |

### Customization Reference

- **Package Name:** `com.symdev.netswitch`
- **Notifications:** `NotificationHelper.kt`
- **Geofencing:** `GeofenceManager.kt`
- **Theme/UI:** `ui/theme/`
- **Default Radius:** `PreferencesManager.DEFAULT_RADIUS`

---

<div align="center">
  <sub>Built with Kotlin + Jetpack Compose + Google Play Services Geofencing + OpenStreetMap</sub>
</div>
