# tal-day

Android study tracker — one persistent daily schedule, per-topic countdown timers that run in the background, hierarchical topic notes, and a stats dashboard.

## Features

- **My Schedule** — one list of topics + planned minutes, same every day, with a per-row checkbox that auto-resets at midnight.
- **Background timer** — tap Start on any row to launch a foreground-service countdown. A persistent mini bar at the bottom of every screen shows the remaining time; you can keep using the rest of the app while it runs. Notification survives backgrounding.
- **Topic folders** — tap a topic name to open a hierarchical notes tree (subfolders + notes with DONE / NOT_DONE / NONE status toggles).
- **Stats** — live-updating dashboard:
  - Today / This week / Streak headline cards
  - Lifetime total study time + session count
  - "This week" bar chart (7 bars, Sun → Sat, day-of-week labels)
  - "Last 14 days" bar chart (day-of-month labels)
  - Previous 6 weeks with total + per-day average

## Tech

Kotlin · Jetpack Compose (Material 3) · MVVM · Room · Hilt · Navigation Compose · Vico charts · Foreground Service.

- `minSdk 26`, `targetSdk 34`, `compileSdk 34`
- Package root: `com.maayan.studytracker`

## Build

```sh
./gradlew assembleDebug
./gradlew installDebug
```

On first run Android Studio will also generate the Gradle wrapper JAR and a `local.properties` with your SDK path.
