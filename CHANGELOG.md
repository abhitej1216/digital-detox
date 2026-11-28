# Changelog — Digital Detox

All notable changes to this project will be documented in this file.

## [0.5.0] - 2026-11-28

### Changed & Improved
- **Hardened 60-Second Multi-Step Unblock Barrier (`UnblockChallengeDialog`)**:
  - Increased the mandatory unblock cooldown timer from 30 seconds to **60 seconds** (`01:00`), paused automatically if the app leaves the foreground (`ON_PAUSE` / `ON_RESUME`).
  - Upgraded the single arithmetic prompt to a **3-stage challenge**:
    1. **Equation 1 of 2**: Multi-operation arithmetic with non-multiple-of-10 values.
    2. **Equation 2 of 2 (Harder)**: Compound two-product or square-offset expressions.
    3. **Manual Commitment Pledge**: Typing a rotating mindful phrase with multi-character paste blocked.
  - Added explicit **Verify Answer** submission with a **+10s cooldown penalty** per wrong guess and equation regeneration after 2 wrong attempts.
  - Disabled outside-tap dismissal and enabled scrollable dialog content for smaller screens.
- **Settings Shield Protection (`SettingsScreen`, `SettingsViewModel`)**:
  - Turning off `Strict Unblock Barrier`, `One Sec Breath Intercept`, or `Nighttime Detox` while `Strict Unblock Barrier` is active now requires completing the 60-second multi-step challenge.
- **Blocking Reliability & Persistence (`MindfulInterventionManager`, `AppBlockingService`, `AppRepository`, `SettingsRepository`)**:
  - Persisted active micro-sessions, cooldown timers, and daily open quotas in `SharedPreferences` across app and service restarts.
  - Added `SettingsRepository.getIntFlow` so `AppBlockingService` reacts immediately to changes in daily open limits, micro-session length, and cooldown duration, using millisecond-accurate session expiration tracking.
  - Excluded the default Home launcher from Nighttime Detox and filtered `AppRepository` to launchable user and updated system apps while excluding Digital Detox itself.
- **Intervention, Dashboard, App Guard & Focus Timer Polish**:
  - Added `BackHandler` to `MindfulInterventionScreen` and `CooldownScreen` so pressing Back routes Home instead of returning to the blocked app; excluded Settings preview runs from inflating `Mindful Saves`; and displayed configured `microSessionMinutes` on `CooldownScreen`.
  - Added shared `AccessibilityHelper` and updated `DashboardScreen` / `DashboardViewModel` to reflect live Accessibility Service status (`Protection Active` / `Protection Off`), guarded app count, and daily logged focus minutes.
  - Sorted `BlockedAppsScreen` alphabetically, added app counts to filter tabs (`All Apps` / `Blocked`), and moved app icon decoding off the main thread via `produceState` + `Dispatchers.IO`.
  - Updated `FocusTimerViewModel` to use `SystemClock.elapsedRealtime()` for drift-free countdowns and accurate focus time accumulation across pauses.

## [0.4.0] - 2026-11-22

### Added
- **Micro-Sessions & Mandatory Cooldown Windows (Anti-Doomscroll Engine)**:
  - `MindfulInterventionManager`: Added session timing, mandatory cooldown tracking, and daily open quota limits per package.
  - `CooldownActivity` & `CooldownScreen`: Dedicated cooldown screen with live circular countdown timer, dopamine reset guidance, daily opens counter, and return-to-home action.
  - Active session watchdog in `AppBlockingService`: Automatically pulls the user out of the app and triggers the Cooldown screen when their micro-session expires.
  - `SettingsScreen`: Added configuration controls for:
    - Micro-Session Length (3m, 5m, 10m, 15m)
    - Mandatory Cooldown Duration (15m, 30m, 45m, 60m)
    - Daily Opens Limit (3, 5, 8, 10 opens/day)
  - `DashboardScreen`: Added Anti-Doomscroll Engine status card.

## [0.3.0] - 2026-11-22

### Added
- **"One Sec" Mindful Intervention Engine**:
  - Implemented `MindfulInterventionActivity` and `MindfulInterventionScreen` providing a 10-second guided breathing exercise upon launching blocked apps (Inhale 4s -> Hold 2s -> Exhale 4s).
  - Concentric animated aura rings with phase-synced haptics and live countdown progress.
  - Intentional decision state: "I don't need this right now" (Mindful Save) vs "Open for 5 minutes only" (intentional quota).
  - `SettingsScreen`: Added One Sec toggle & "Preview Mindful Breathing Exercise" button.
  - `DashboardScreen`: Added live "Mindful Saves" counter tracking avoided distractions.

## [0.2.0] - 2026-11-22

### Added / Redesigned (Emil Kowalski Design Engineering Principles)
- **Design System & Motion**:
  - Implemented `tactilePress(scale = 0.96f)` with physical spring dynamics and haptic ticks on button touch down.
  - Symmetrical Apple-grade screen transitions: `scaleIn(0.96f) + fadeIn(220ms)` and `scaleOut(0.96f) + fadeOut(160ms)`. Never scaling from zero.
  - Deep obsidian dark palette (`#090A0D`), soothing emerald detox accents (`#10B981`), and subtle 1dp hairline borders (`#2D323E`).
- **Floating Dock Navigation**:
  - Floating pill navigation bar with spring-animated indicator capsules.
- **Precision Focus Timer**:
  - Dual-layer circular progress canvas ring with gentle ambient breathing motion when running.
  - Monospace tabular timer digits (`24:59`) with quick duration selector chips (`15m`, `25m`, `45m`, `60m`).
  - Fluid segmented control to alternate between Study and Work sessions.
- **Overview Dashboard**:
  - Visual circular progress gauge and goal progress bar.
  - Side-by-side metric cards for Study and Work.
  - Frosted action-required cards with tactile buttons.
- **App Guard & Mindful Challenge**:
  - Rounded search bar and segmented tab filter.
  - Unblock challenge dialog with animated countdown gauge, math challenge card, and intentional friction.
