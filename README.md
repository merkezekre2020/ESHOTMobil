# Izmir ESHOT Native Apps

Two native mobile applications (iOS & Android) for tracking Izmir ESHOT buses using Open Data.
Developed with **NO Cross-Platform** tools (SwiftUI & Jetpack Compose).
**Unsigned Builds Only.**

## Directory Structure
- `android-app/`: Native Android project (Kotlin, Compose, OSMDroid).
- `ios-app/`: Native iOS source files (Swift, SwiftUI, MapKit).

## Android Setup
1. Open `android-app` in Android Studio.
2. Sync Project with Gradle.
3. Run `assembleDebug` or launch on Emulator.
   - **Note**: No Release Keystore is configured. Debug signing is automatic.

## iOS Setup
1. **Project Generation**: This project uses `XcodeGen`.
   - Install: `brew install xcodegen`
   - Run: `xcodegen` in the `ios-app` folder.
   - Open `IzmirEshot.xcodeproj`.
2. **Build Settings**:
   - The generated project is configured for Unsigned builds (`CODE_SIGNING_ALLOWED=NO`).
3. Run on **Simulator**.

### CI/CD
The GitHub Actions workflow automatically installs `xcodegen` and generates the project before building.

## Features
- **Maps**:
  - Android: OSMDroid with Custom Grid Clustering.
  - iOS: MapKit with Clustering.
- **Data**: CSV parsing with robust encoding/delimiter detection.
- **Offline**: Caches CSV data for 24h.

## Troubleshooting
- **Maps Empty?** Check Internet connection for Tile download.
- **CSV Error?** The parser supports UTF-8 and Windows-1254. Check logs for "CsvParser".
- **API Errors?** The apps use lenient JSON parsing to handle schema changes.

## CI/CD (GitHub Actions)
- Defined in `.github/workflows`.
- Builds Unsigned artifacts only.
