# NetworkInspectionPro

A zero-setup Kotlin Multiplatform Ktor HTTP traffic inspector with a Compose Multiplatform UI. Shake the device to open the inspector overlay. Completely passive in release builds.

## What is it?

NetworkInspectionPro is a KMP library that plugs into the Ktor HTTP client as a plugin. It captures every request and response, stores them in memory, and presents them in a beautiful Compose UI that slides in when you shake your device — no buttons to add, no deep links to configure.

## Features

- Intercepts all Ktor HTTP traffic (method, URL, headers, body, status, duration)
- Shake-to-open inspector overlay (accelerometer on Android, CoreMotion on iOS)
- Searchable list of requests with color-coded method and status badges
- Detail view with Overview / Request / Response tabs and pretty-printed JSON
- Zero-overhead in release builds — just don't call `init()` / `enable()`
- Kotlin Multiplatform: Android + iOS

## Installation

```kotlin
// build.gradle.kts
dependencies {
    debugImplementation("io.github.hazemafaneh:network-inspection-pro:1.0.0")
}
```

## Setup

### Android

In your `Application.onCreate()` (or debug-only initializer):

```kotlin
import io.github.hazemafaneh.networkinspectionpro.NetworkInspectionPro

class MyApp : Application() {
    override fun onCreate() {
        super.onCreate()
        if (BuildConfig.DEBUG) {
            NetworkInspectionPro.init(this)
        }
    }
}
```

### iOS

In your `AppDelegate` or SwiftUI `App` init (debug only):

```kotlin
// Kotlin side — call from Swift/ObjC
NetworkInspectionPro.shared.enable()
```

```swift
// Swift (AppDelegate or @main App)
#if DEBUG
NetworkInspectionPro.shared.enable()
#endif
```

### Ktor Client

```kotlin
val client = HttpClient {
    install(NetworkInspectorPlugin)
}
```

### Wrap Your Root Composable

```kotlin
// In your root Composable (e.g., MainActivity setContent)
NetworkInspectorOverlay {
    MyAppContent()
}
```

Shake the device to open the inspector. Shake again to dismiss.

## Release Builds

Simply do **not** call `NetworkInspectionPro.init(context)` (Android) or `NetworkInspectionPro.shared.enable()` (iOS) in release builds. The Ktor plugin checks `isEnabled` before doing any work, and the overlay renders only your content when disabled.

## Requirements

| Platform | Minimum |
|---|---|
| Android | API 24 |
| iOS | iOS 14+ |
| Kotlin | 2.1.21 |
| Ktor | 3.1.3 |

## License

```
Copyright 2025 Hazem Afaneh

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    https://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
```
