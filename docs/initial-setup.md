# Initial Setup Guide

This guide creates a minimal, buildable Android project. It intentionally adds no application features; those are implemented through later issues.

## Prerequisites

Install the current stable Android Studio and, through its SDK Manager, install:

- The latest stable Android SDK Platform.
- Android SDK Build-Tools.
- Android SDK Platform-Tools.
- Android Emulator and one phone emulator image, or enable USB debugging on a physical Android device.

Use the JDK bundled with Android Studio. Do not install a separate JDK unless Android Studio asks for one.

## Create the Project

In Android Studio:

1. Select **New Project** and choose **Empty Activity**.
2. Set Name to `Meal Ledger`.
3. Set Package name to `com.amaljacobs.mealledger`.
4. Set Save location to `/home/xenon/Development/meal-ledger`.
5. Select Kotlin and Jetpack Compose.
6. Set Minimum SDK to API 26.
7. Finish and allow Gradle sync to complete.

If Android Studio refuses to create the project in a non-empty folder, create it in a temporary empty directory, then move only the generated Android project files into this repository. Preserve `README.md`, `.gitignore`, and `docs/`.

## First Verification

Before changing the template:

1. Start an emulator or connect a device.
2. Run the app from Android Studio.
3. Confirm the template launches without a crash.
4. Run the generated unit test suite from the Gradle tool window.
5. Commit the generated project as a standalone baseline.

Suggested commit:

```text
Create Android Compose project (#1)
```

## First Project Commit Contents

Keep the initial setup commit limited to generated build files, the `app` module, Gradle wrapper, and any necessary ignore-file updates. Do not add Room, navigation, screen redesigns, or feature code yet.

## Immediately After Setup

Create these follow-up implementation tasks in this order:

1. Add the package structure and navigation shell.
2. Add Room and the food/water data schema.
3. Add settings storage.
4. Build the Today screen against real local data.

Those map to the existing foundation issues; update their descriptions only if the chosen Android Studio template requires a small version-specific adjustment.
