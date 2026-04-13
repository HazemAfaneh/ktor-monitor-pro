# NetworkInspectionPro

[![Maven Central](https://img.shields.io/maven-central/v/io.github.hazemafaneh/network-inspection-pro.svg?label=Maven%20Central)](https://central.sonatype.com/artifact/io.github.hazemafaneh/network-inspection-pro)
[![License](https://img.shields.io/badge/License-Apache%202.0-blue.svg)](https://www.apache.org/licenses/LICENSE-2.0)

A Kotlin Multiplatform Ktor HTTP traffic inspector with a Compose Multiplatform UI.
Shake the device to open the inspector overlay. Zero overhead in release builds.

## Project Structure

This is a Kotlin Multiplatform library targeting Android and iOS.

* [/library](./library/src) contains all library source code.
  - [commonMain](./library/src/commonMain/kotlin) — shared logic for all platforms: Ktor plugin, overlay UI, view model, and shared utilities.
  - [androidMain](./library/src/androidMain/kotlin) — Android-specific implementations: accelerometer-based shake detection and context initialization.
  - [iosMain](./library/src/iosMain/kotlin) — iOS-specific implementations: CoreMotion-based shake detection and platform utilities.
  - [commonTest](./library/src/commonTest/kotlin) — shared unit tests.

---

## Build

### Publish to Maven Local (for local testing)

```shell
./gradlew :library:publishToMavenLocal
```

### Run tests

```shell
./gradlew :library:allTests
```

---

## Features

- Intercepts all Ktor HTTP traffic — method, URL, headers, body, status code, duration
- Shake-to-open inspector overlay (accelerometer on Android, CoreMotion on iOS)
- Searchable request list with color-coded method and status badges
- Detail view with Overview / Request / Response tabs and pretty-printed JSON
- Zero cost in release builds — nothing runs until you call `enable()`
- Kotlin Multiplatform: Android + iOS

## Requirements

| | Minimum |
|---|---|
| Android | API 24 |
| iOS | iOS 14 |
| Kotlin | 2.1.21 |
| Ktor | 3.1.3 |
| Compose Multiplatform | 1.8.1 |

---

## Installation

### Android-only project

```kotlin
// app/build.gradle.kts
dependencies {
    debugImplementation("io.github.hazemafaneh:network-inspection-pro:0.1.0")
}
```

### Kotlin Multiplatform project

```kotlin
// shared/build.gradle.kts
kotlin {
    listOf(
        iosArm64(),
        iosSimulatorArm64()
    ).forEach { iosTarget ->
        iosTarget.binaries.framework {
            baseName = "shared"
            isStatic = true
            export("io.github.hazemafaneh:network-inspection-pro:0.1.0") // (1)
        }
    }

    sourceSets {
        commonMain.dependencies {
            api("io.github.hazemafaneh:network-inspection-pro:0.1.0") // (2)
        }
        androidMain.dependencies {
            implementation("io.ktor:ktor-client-okhttp:3.1.3")
        }
        iosMain.dependencies {
            implementation("io.ktor:ktor-client-darwin:3.1.3")
        }
    }
}
```

> **(1)** `export()` includes the library symbols in the generated `.framework` so Swift can import `NetworkInspectionPro` directly.
> **(2)** `api()` instead of `implementation()` is required for the `export()` to take effect.

---

## How to Use

### 1. Install the Ktor plugin

Add `NetworkInspectorPlugin` when building your `HttpClient`. This is what captures every request and response.

```kotlin
import io.github.hazemafaneh.networkinspectionpro.NetworkInspectorPlugin
import io.ktor.client.HttpClient
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.serialization.kotlinx.json.json

val client = HttpClient {
    install(NetworkInspectorPlugin)
    install(ContentNegotiation) {
        json()
    }
}
```

### 2. Wrap your root composable

`NetworkInspectorOverlay` renders your UI untouched. When enabled and the device is shaken, the inspector panel slides in over your content.

**Android**

```kotlin
import io.github.hazemafaneh.networkinspectionpro.internal.ui.NetworkInspectorOverlay

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            NetworkInspectorOverlay {
                App()
            }
        }
    }
}
```

**iOS (KMP shared module)**

```kotlin
import androidx.compose.ui.window.ComposeUIViewController
import io.github.hazemafaneh.networkinspectionpro.internal.ui.NetworkInspectorOverlay

fun MainViewController() = ComposeUIViewController {
    NetworkInspectorOverlay {
        App()
    }
}
```

### 3. Enable the library

The library is **opt-in** — it does nothing until you call `enable()`. This lets you include it in all build types and only turn it on in debug.

**Android — call `init(context)` in your `Application`**

`init()` stores the application context (needed for shake detection) and calls `enable()` internally.

```kotlin
import io.github.hazemafaneh.networkinspectionpro.NetworkInspectionPro
import io.github.hazemafaneh.networkinspectionpro.init

class MyApp : Application() {
    override fun onCreate() {
        super.onCreate()
        if (BuildConfig.DEBUG) {
            NetworkInspectionPro.init(this)
        }
    }
}
```

**iOS — call `enable()` from your SwiftUI `App`**

```swift
import SwiftUI
import shared // your KMP framework name

@main
struct iOSApp: App {
    init() {
        #if DEBUG
        NetworkInspectionPro.shared.enable()
        #endif
    }

    var body: some Scene {
        WindowGroup {
            ContentView()
        }
    }
}
```

---

## Usage

Once set up, make HTTP requests normally through your Ktor client. Then **shake the device** to open the inspector. You will see a list of all captured requests. Tap any entry to view full request/response details including headers, body, and timing.

Shake again (or press the back button) to dismiss the overlay.

---

## Release builds

Do **not** call `init(context)` (Android) or `enable()` (iOS) in release builds.

When disabled:
- `NetworkInspectorPlugin` skips all work on every request — no allocations, no storage.
- `NetworkInspectorOverlay` renders only your content, adding no overhead.

---

---

Learn more about [Kotlin Multiplatform](https://www.jetbrains.com/help/kotlin-multiplatform-dev/get-started.html)

---

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