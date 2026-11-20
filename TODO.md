# TODO — Digital Detox

Prioritised backlog for the MVP.

## Phase 1: Foundation (current)

- [x] Project folder structure
- [x] Architecture documentation
- [x] Gradle build files (settings.gradle, build.gradle)
- [x] AndroidManifest.xml
- [x] Initial Navigation (NavHost)
- [x] Basic Compose UI (Home screen)

## Phase 2: Core features

- [x] Room database implementation (entities, DAOs, Database)
- [x] DataStore preferences implementation
- [x] Theme (Colors, Typography, Shapes)
- [x] Settings screen (blocking method, targets, night mode)
- [x] Dashboard screen (daily goals, app usage)
- [x] Blocked apps screen (list, toggle, add)

## Phase 3: Blocking mechanics

- [ ] DevicePolicyManager setup flow
- [x] AccessibilityService for app blocking
- [ ] Foreground service for continuous blocking
- [x] UsageStatsManager integration for daily stats
- [x] Notification for blocked app attempts

## Phase 4: Focus sessions

- [x] Focus timer screen
- [x] Study/Work mode switching
- [x] Session tracking in Room
- [x] Goal progress display

## Phase 5: Polish & Enhancements

- [x] Battery optimization prompt
- [x] Nighttime detox enforcement
- [x] Adaptive layout / scrollable screens
- [x] Real App Icons display in Blocked Apps list
- [x] Search & Filter (All / Blocked Only) in Blocked Apps
- [x] Daily Goal Progress Bar on Dashboard
- [x] Strict Unblock Protection (30s Mandatory Delay + Advanced Math Challenge)