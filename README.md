# Digital Detox — Android

A local-first Android application that helps users reduce time spent on
distracting social media and entertainment apps, and that nudges them away
from their phone during study and work blocks.

## Configuration summary

| Setting | Value |
| --- | --- |
| Platform | Android |
| UI toolkit | Jetpack Compose |
| Architecture | MVVM + Clean Architecture |
| Database | Room |
| Preferences | DataStore |
| Blocking policy | Strict |
| Restricted apps | Instagram, WhatsApp, YouTube, Facebook, Reddit, Snapchat, X, plus user-selected |
| Study target | 4 h / day |
| Work / coding target | 4 h / day |
| Nighttime detox | Enabled, 22:00–06:00 |
| Backend | Not required for the MVP |
| Privacy | Local-first |

## Getting started

The project is built with the Android Gradle Plugin (AGP) and Gradle 8.5.
The Android SDK must be installed and `ANDROID_HOME` / `ANDROID_SDK_ROOT`
must point at it before any Gradle task runs.

```bash
# From the project root
./gradlew :app:assembleDebug
```

> **Note:** App blocking is not a compile-time guarantee. It is enforced at
> runtime through `DevicePolicyManager` (device-owner / profile-owner mode)
> and/or an `AccessibilityService`. The blocking behaviour must be verified
> on a physical device before it is treated as reliable.

## Project layout

```
app/
├── src/main/
│   ├── java/com/example/digitaldetox/
│   │   ├── data/        # Room entities, DAOs, repositories, DataStore
│   │   ├── domain/      # Use cases and domain models
│   │   ├── presentation/# ViewModels, screens, navigation, theme
│   │   └── service/     # Blocking service and accessibility service
│   └── res/             # Drawables, strings, XML configs
└── src/test/            # Unit tests
```

See `ARCHITECTURE.md` for the full layered description.

## Documentation

- `ARCHITECTURE.md` — layers, data flow, and module boundaries.
- `PROJECT_STATUS.md` — current milestone, progress, and known issues.
- `TODO.md` — prioritised backlog.
- `CHANGELOG.md` — version history.

## License

This is a personal project. Do not distribute without permission.