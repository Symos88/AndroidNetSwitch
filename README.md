# 📡 NetSwitch 2.0

> **Smart network switching based on location.**
> An Android app that monitors your geofence and prompts you to toggle Wi-Fi/Mobile Data when arriving home or leaving—no Google account or API key required.

[![Kotlin](https://img.shields.io/badge/Kotlin-2.0.21-7F52FF?logo=kotlin&logoColor=white)](https://kotlinlang.org/)
[![Jetpack Compose](https://img.shields.io/badge/Jetpack_Compose-Material_3-4285F4?logo=jetpackcompose)](https://developer.android.com/jetpack/compose)
[![Android](https://img.shields.io/badge/Target_SDK-36-3DDC84?logo=android&logoColor=white)](https://developer.android.com/)
[![License](https://img.shields.io/badge/License-MIT-blue.svg)](LICENSE)

## ✨ NetSwitch 2.0

- **🗺️ Privacy-First Maps:** Uses OpenStreetMap. Set your home location by tapping to drop a pin.
- **🔔 Smart Notifications:** Arrival prompts Wi-Fi; departure prompts mobile data.
- **⚡ Internet Panel:** Tapping a notification opens Android's built-in Internet panel.
- **🌙 Modern Dashboard:** Dark UI with live geofence radius, status cards, map overlay, and permission chips.
- **🧪 Automated Tests:** Geofence radius/state behavior is covered by JVM unit tests.
- **🔧 CI Builds:** GitHub Actions builds, tests, and smoke-tests both debug and release variants.

---

## ⚠️ Platform limitation

Since Android 10, regular apps cannot silently toggle Wi-Fi or Mobile Data because of OS security restrictions.

NetSwitch therefore fires a contextual notification. Tapping it opens the native Android Internet Panel, where the requested network can be changed.

---

## 🏗️ Project Structure

| Path | Description |
| :--- | :--- |
| `app/` | Android application (Kotlin + Jetpack Compose, Material 3) |
| `.github/workflows/build-apk.yml` | Debug/test, release smoke-test, and signed release pipeline |
| `app/src/.../notification/` | Notification text and behavior |
| `app/src/.../geofence/` | Geofence registration and transition handling |
| `app/src/test/` | JVM unit tests |

---

## 📦 Build and release

For normal pushes, pull requests, and manual workflow runs, GitHub Actions:

1. Builds the debug APK.
2. Runs JVM unit tests.
3. Builds an unsigned release APK as a release-configuration smoke test.
4. Publishes the debug and release-smoke APKs as artifacts.

Release tags (`v*`) additionally require the release signing secrets and produce a signed `NetSwitch 2.0` APK.

### Release signing secrets

Configure these repository secrets before creating a release tag:

- `SIGNING_KEY` — base64-encoded JKS/keystore file
- `KEY_ALIAS`
- `KEY_PASSWORD`
- `STORE_PASSWORD`

The CI decodes the keystore only inside the ephemeral runner. The keystore is never committed to the repository. The resulting APK is verified with `apksigner` before the GitHub Release is published.

### Version 2.0

- `versionName`: **2.0**
- `versionCode`: **20**
- `minSdk`: 26
- `targetSdk`: 36
- `compileSdk`: 36

---

## 📱 Installation

1. Transfer `app-debug.apk` to the phone.
2. Install it through the file manager.
3. Tap the map to set the Home location and press **SAVE**.
4. Enable **Monitoring**.
5. Grant foreground location, background location, and notification permissions as requested.
6. On OEMs with aggressive background management, allow NetSwitch to run without battery restrictions and enable autostart where applicable.

> Android 11+ requires background location to be enabled through the system settings page rather than the normal permission dialog.

---

## 🛠️ Technical Notes

| Detail | Value |
| :--- | :--- |
| **Version** | 2.0 (20) |
| **AGP** | 8.9.1 |
| **Gradle** | 8.11.1 |
| **Kotlin** | 2.0.21 |
| **Compile / Target SDK** | 36 / 36 |
| **Min SDK** | 26 |
| **Geofence Radius** | 50m – 500m; default 150m |
| **Triggers** | ENTER + EXIT |
| **Background monitoring** | Google Play Services Geofencing API |
| **Permanent foreground service** | Not required |

### Customization Reference

- **Package Name:** `com.symdev.netswitch`
- **Notifications:** `NotificationHelper.kt`
- **Geofencing:** `GeofenceManager.kt`
- **Theme/UI:** `ui/theme/`
- **Default Radius:** `PreferencesManager.DEFAULT_RADIUS`

---

<div align="center">
  <sub>NetSwitch 2.0 · Kotlin + Jetpack Compose + Google Play Services Geofencing + OpenStreetMap</sub>
</div>
