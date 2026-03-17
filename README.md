# ZeroMiss 🎯

> Never miss a contest or task again.

A feature-rich Android productivity app built for competitive programmers, students, and educators — wrapped in a beautiful **Liquid Glass UI** inspired by Apple's latest design language.

---

## ✨ Features

### 📋 Task Management
- Add, edit, delete tasks with priorities, categories and color labels
- Smart reminders via AlarmManager (exact alarms)
- Repeat tasks daily or weekly
- Swipe to complete or delete
- Task detail view with notes

### 🏆 Codeforces Integration
- Live upcoming contest feed from Codeforces API
- Countdown timer for each contest
- TODAY / SOON badges with glowing animations
- Set reminders (5 min to 1 day before)
- Add contest directly as a task

### ⏱ Focus Timer
- Pomodoro-style work/break sessions
- Custom durations per mode
- Background audio support (pick your own MP3)
- Session statistics tracking

### 🎓 Tuition Management
- Student profiles with photos, schedule days, fee tracking
- Monthly attendance calendar (Present / Absent / Extra)
- Gradient stat cards per student
- Share attendance reports via WhatsApp/SMS
    - Current month summary
    - Custom date range
    - All time report
- Call guardian directly from app
- Edit student info anytime

### 📅 Calendar
- Monthly calendar with task indicators
- Tap any day to see tasks

### 🎨 Liquid Glass UI
- iOS-inspired Liquid Glass design system
- Animated mesh background
- Glass cards with adaptive transparency
- Floating pill navigation bar
- Directional swipe gestures between tabs
- Haptic feedback on swipe
- Smooth spring animations
- Light / Dark mode support

---

## 🛠 Tech Stack

| Category | Technology |
|---|---|
| Language | Kotlin |
| UI | Jetpack Compose |
| Architecture | MVVM |
| Database | Room (SQLite) |
| Navigation | Navigation Compose |
| Notifications | AlarmManager + BroadcastReceiver |
| Background | WorkManager |
| Network | Retrofit2 + Gson |
| Preferences | DataStore |
| Min SDK | 26 (Android 8.0) |
| Target SDK | 35 |

---

## 📸 Screenshots

| Splash | Dashboard | Tasks |
|---|---|---|
| | | |

| Codeforces | Tuition | Settings |
|---|---|---|
| | | |

> Screenshots coming soon

---

## 🚀 Getting Started

### Prerequisites
- Android Studio Hedgehog or newer
- JDK 17+
- Android device or emulator (API 26+)

### Installation
```bash
git clone https://github.com/Rbn-Rmn/ZeroMiss.git
cd ZeroMiss
```

Open in Android Studio → **Sync Project with Gradle Files** → **Run**

---

## 📁 Project Structure
```
com.example.devflow/
├── data/
│   ├── local/          # Room DB, DAOs
│   ├── model/          # Data classes
│   ├── remote/         # Codeforces API
│   └── repository/     # Repositories
├── navigation/         # NavGraph, BottomNavBar
├── ui/
│   ├── calendar/       # Calendar screen
│   ├── components/     # Shared composables
│   ├── contests/       # Codeforces screen
│   ├── dashboard/      # Home screen
│   ├── focus/          # Focus Timer
│   ├── settings/       # Settings + About
│   ├── splash/         # Splash screen
│   ├── task/           # Task screens
│   ├── theme/          # Liquid Glass theme
│   └── tuition/        # Tuition management
└── worker/             # Background workers
```

---

## 🔔 Permissions
```xml
INTERNET
POST_NOTIFICATIONS
SCHEDULE_EXACT_ALARM
USE_EXACT_ALARM
WAKE_LOCK
VIBRATE
READ_MEDIA_AUDIO
RECEIVE_BOOT_COMPLETED
```

---

## 👨‍💻 Developer

**Dewan Sultan Al Amin**

[![Portfolio](https://img.shields.io/badge/Portfolio-dewansultan.vercel.app-blue?style=flat&logo=vercel)](https://dewansultan.vercel.app)
[![GitHub](https://img.shields.io/badge/GitHub-Rbn--Rmn-black?style=flat&logo=github)](https://github.com/Rbn-Rmn)
[![Codeforces](https://img.shields.io/badge/Codeforces-D_Sultan01-orange?style=flat)](https://codeforces.com/profile/D_Sultan01)
[![Email](https://img.shields.io/badge/Email-dwnsultan@gmail.com-red?style=flat&logo=gmail)](mailto:dwnsultan@gmail.com)

> BSc in Computer Science & Engineering (Data Science) — East West University

---

## 📄 License
```
Copyright (c) 2026 Dewan Sultan Al Amin

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all
copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT.
```

---

<p align="center">Made with ❤️ by Dewan Sultan Al Amin</p>
<p align="center">ZeroMiss v1.0 — Never miss a contest or task again</p>
