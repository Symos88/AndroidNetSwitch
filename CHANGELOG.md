# Changelog

## 2.0 — 2026-08-09

### Added
- Android 16 (API 36) compile/target configuration.
- Automated JVM unit tests for geofence radius normalization.
- Release signing pipeline with CI signature verification.
- Release build smoke testing on normal CI runs.

### Changed
- Geofence PendingIntent now uses immutable flags.
- Geofence responsiveness is explicitly configured to 30 seconds.
- Reboot and re-registration no longer synthesize an initial ENTER event.
- Geofence radius is consistently clamped to 50–500 meters.
- Arrival and departure notifications now have distinct content and actions.
- Notifications open the Android Internet Panel instead of legacy Wi-Fi/data settings.
- Monitoring is only armed after required permission checks complete.
- Removed the unnecessary permanent foreground location service.
- Release version is now 2.0 (versionCode 20).

### Fixed
- Compose compile failure caused by scoped `AnimatedVisibility` usage.
- Permission callback state race during monitoring activation.
- Reboot-time geofence registration without valid background-location permission.
- Persisted radius values outside the supported range.
- Release workflow previously described/treated an unsigned APK as signed.
