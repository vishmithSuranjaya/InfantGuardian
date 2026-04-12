# InfantGuardian_v3

> InfantGuardian_v3 — Android app for infant monitoring and alerts.

## Overview

InfantGuardian_v3 is an Android application that provides monitoring, alarm, and notification features to help caregivers monitor infants. This repository contains the Android Studio / Gradle project for the app (module: `app`).

## Key Features

- Background monitoring and alarm playback
- Custom sound support (see `CUSTOM_SOUND_TESTING.md`)
- Lock screen behavior and quick actions (see `LOCK_SCREEN_COMPLETE.md`)
- Google services integration (analytics / notifications) via `google-services.json`

## Requirements

- Java JDK 11+ (or the project's configured JDK)
- Android Studio Arctic Fox or newer
- Gradle (wrapper included)
- A connected Android device or emulator

## Quick Start

1. Open the project in Android Studio (select the repository root).
2. Let Android Studio import Gradle settings and sync dependencies.
3. Build and run on a device or emulator.

From the command line (Windows):

```powershell
.\gradlew.bat assembleDebug
.\gradlew.bat installDebug
```

Run instrumentation tests (if any):

```powershell
.\gradlew.bat connectedAndroidTest
```

## Build (release)

Prepare signing config in `app/build.gradle.kts` or the Gradle properties, then:

```powershell
.\gradlew.bat assembleRelease
```

## Project Structure

- `app/` — Android application module (source, resources, manifest)
- `gradle/` — Gradle wrapper and version catalog
- Docs and notes: `QUICK_START.md`, `QUICK_TEST.md`, `ALARM_SOUND_GUIDE.md`, etc.

