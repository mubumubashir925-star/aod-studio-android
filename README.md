# AOD Studio

A native Android Always-On Display customization project.

## Current foundation
- Kotlin + Jetpack Compose
- Dark AMOLED-inspired interface
- Ready-made clock style gallery
- Live clock preview
- Starter custom editor (colour and 12/24-hour selection)
- Settings UI placeholders for burn-in protection, brightness and notifications

## Build
Open this project in Android Studio (Ladybug or newer), allow Gradle sync, then run the `app` configuration on an Android device or emulator.

## Important platform note
Android does not provide one universal public API that replaces every manufacturer's system AOD. A third-party AOD experience must account for overlay/lock-screen restrictions, foreground-service rules, device-specific battery management and user-granted permissions. The current commit is the UI foundation; real screen-off activation is not claimed as implemented.

## Roadmap
1. Expand the editor: fonts, sizes, alignment, position, date/battery widgets and backgrounds.
2. Add design save/load using local storage.
3. Implement permission onboarding and an opt-in AOD foreground/overlay mode.
4. Add burn-in-safe movement, proximity/pocket detection and brightness controls.
5. Test on multiple Android manufacturers before release.

## Minimum SDK
Android 8.0 (API 26), subject to final feature/device compatibility testing.
