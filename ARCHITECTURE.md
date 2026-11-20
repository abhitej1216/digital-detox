# Architecture — Digital Detox

## Overview

The application follows a **MVVM + Clean Architecture** pattern, separating concerns into
three main concentric layers:

```
+--------------------------------------------------+
|                Presentation Layer                |
|  — Screen composables                            |
|  — ViewModels (state holders)                   |
|  — Navigation (Compose Destinations)            |
|  — Theme (Typography, Colors, Shapes)           |
+--------------------------------------------------+
|                  Domain Layer                    |
|  — Use cases (business logic)                    |
|  — Domain models (immutable data)                |
|  — Repository interfaces                         |
+--------------------------------------------------+
|                   Data Layer                     |
|  — Room (AppDatabase, DAOs, Entities)           |
|  — DataStore (Proto/DataStore preferences)       |
|  — Repository implementations                    |
+--------------------------------------------------+
|                  Android Framework               |
|  — DevicePolicyManager (device owner blocking)  |
|  — AccessibilityService (app override blocking) |
|  — UsageStatsManager (usage tracking)           |
+--------------------------------------------------+
```

## Layer responsibilities

### Presentation layer (`presentation`)

* **Screens**: Composables for Home, Dashboard, Blocked, Settings, Focus timer.
* **ViewModels**: Hold UI state, expose flows, invoke use cases.
* **Navigation**: `NavHost` with composable destinations; single-activity setup.
* **Theme**: Material 3 `Theme.kt` with dynamic color (Android 12+) and fallback palette.

### Domain layer (`domain`)

* **Models**: `App`, `BlockedSession`, `FocusGoal`, `FocusMode`, `RestrictionStatus`.
* **Use Cases**: `ToggleAppBlocking`, `StartFocusSession`, `GetDailySummary`,
  `UpdateSettings`, `QueryUsageStats`.
* **Repositories**: Abstract interfaces consumed by ViewModels.

### Data layer (`data`)

* **Room entities**: `AppEntity`, `BlockedSessionEntity`, `FocusSessionEntity`,
  `DailyUsageStatEntity`, `SettingsEntity`.
* **DAOs**: Accessors for Room tables with queries for stats and blocking rules.
* **DataStore**: Proto preferences for user settings and app flags.
* **Implementation**: Concrete repositories that delegate to Room + DataStore + framework APIs.

## Blocking approach

Android app blocking has two viable paths, both requiring user opt-in:

1. **Device Owner / Profile Owner** via `DevicePolicyManager`
   * Required: user enables via `adb shell dpm set-active-admin` or via Play
     Managed Google Play (enterprise provisioning).
   * Pros: reliable blocking that survives reboots, cannot be bypassed by
     force-stopping.
   * Cons: one-time setup that the user must understand; cannot be undone by
     the app itself.

2. **AccessibilityService**
   * Required: user enables in Settings > Accessibility.
   * Pros: no USB debugging, works on consumer devices.
   * Cons: unreliable on recent Android versions; some launchers clear
     notifications; can be manually disabled.

**MVP strategy**: The app detects which method is available and prompts
accordingly. Device-owner mode is preferred; AccessibilityService is the
fallback. The blocking logic must still be tested on physical devices.

## Database design

The schema lives in a single `AppDatabase`:

```
TABLE apps
  - packageName TEXT PRIMARY KEY
  - label TEXT
  - blockEnabled BOOLEAN
  - blockType INTEGER (SYSTEM, USER_SELECTED)

TABLE blocked_sessions
  - id INTEGER PRIMARY KEY AUTOINCREMENT
  - packageName TEXT REFERENCES apps(packageName)
  - startTime INTEGER (epoch millis)
  - endTime INTEGER (epoch millis)
  - timestamp INTEGER

TABLE focus_sessions
  - id INTEGER PRIMARY KEY AUTOINCREMENT
  - mode INTEGER (STUDY, WORK)
  - startTime INTEGER
  - endTime INTEGER
  - goalMinutes INTEGER
  - actualMinutes INTEGER (computed)

TABLE daily_usage_stats
  - date TEXT PRIMARY KEY
  - totalFocusMinutes INTEGER
  - studyMinutes INTEGER
  - workMinutes INTEGER
  - totalAppUsageMinutes INTEGER

TABLE settings
  - key TEXT PRIMARY KEY
  - value TEXT
```

## Permissions required

```xml
<!-- AndroidManifest.xml -->
<uses-permission android:name="android.permission.REQUEST_IGNORE_BATTERY_OPTIMIZATIONS"/>
<uses-permission android:name="android.permission.PACKAGE_USAGE_STATS"/>
<uses-permission android:name="android.permission.ACCESSIBILITY_SERVICE"/>

<uses-feature
    android:name="android.software.device_admin"
    android:required="false"/>
```

| Permission | Why | Required at |
| --- | --- | --- |
| `REQUEST_IGNORE_BATTERY_OPTIMIZATIONS` | Prevent OS from killing app/service | Runtime (user prompt) |
| `PACKAGE_USAGE_STATS` | Read app usage for daily stats | Settings (manual user grant) |
| `DEVICE_ADMIN` | Device-owner mode for strict blocking | Setup flow |
| `ACCESSIBILITY_SERVICE` | Fallback blocking method | Settings (manual user grant) |

## Key decisions

1. **Local-first**: No network calls, no analytics, no crash reporting.
2. **Strict blocking**: We attempt to prevent app launches, not just show UI.
3. **Data privacy**: All data encrypted or plain-text stored; no cloud sync.
4. **User control**: Settings UI to disable, adjust targets, or whitelist apps.