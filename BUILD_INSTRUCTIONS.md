# Build Instructions — Digital Detox

## Prerequisites

1. **JDK 17** (Adoptium Temurin recommended)
2. **Android SDK Platform 34** and **Build Tools 34.0.0**
3. **Android Studio** (recommended) or command-line Gradle

## Option A: Build with Android Studio (Recommended)

1. Open Android Studio
2. File → Open → Select the `digital-detox` folder
3. Android Studio will automatically sync Gradle and download dependencies
4. Click "Run" to build and deploy

## Option B: Command-line build

```bash
cd /path/to/digital-detox
./gradlew assembleDebug
```

Or on Windows:
```cmd
cd digital-detox
gradlew.bat assembleDebug
```

## Setting up Android SDK

If you don't have Android Studio, install the SDK:

```bash
# Install SDKMAN (if not installed)
curl -s "https://get.sdkman.io" | bash

# Install Android SDK
sdk install android 34.0.0

# Or use sdkmanager
sdkmanager "platforms;android-34" "build-tools;34.0.0"
```

Set the environment variable:
```bash
export ANDROID_HOME=/path/to/android/sdk
```

## Common Build Commands

```bash
# Clean build
./gradlew clean

# Assemble debug build
./gradlew assembleDebug

# Assemble release build (requires keystore)
./gradlew assembleRelease

# Run tests
./gradlew test

# Get dependency info
./gradlew dependencies
```

## Troubleshooting

### "Plugin not found" errors
Make sure you have internet access to download plugins from:
- Google's Maven repository (for Android Gradle Plugin)
- Gradle Plugin Portal
- Maven Central

### "Could not find NDK"
The NDK is not required for this MVP. If prompted, skip NDK installation.

### Build cache issues
```bash
./gradlew --stop
./gradlew cleanBuildCache
./gradlew clean
```

## Expected Output

After a successful build, you'll find the APK at:
`app/build/outputs/apk/debug/app-debug.apk`
