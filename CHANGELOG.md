# Changelog — Digital Detox

All notable changes to this project will be documented in this file.

## [0.3.0] - 2026-11-22

### Added
- **"One Sec" Mindful Intervention Engine**:
  - Implemented `MindfulInterventionActivity` and `MindfulInterventionScreen` providing a 10-second guided breathing exercise upon launching blocked apps (Inhale 4s -> Hold 2s -> Exhale 4s).
  - Concentric animated aura rings with phase-synced haptics and live countdown progress.
  - Intentional decision state: "I don't need this right now" (Mindful Save) vs "Open for 5 minutes only" (intentional quota).
  - `MindfulInterventionManager`: Thread-safe temporary access manager for short 5-minute passes.
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
