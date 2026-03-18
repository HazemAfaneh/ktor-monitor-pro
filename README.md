# Ktor Monitor Pro

A Kotlin Multiplatform library for monitoring and inspecting Ktor HTTP client traffic.

## Features

- Intercept and log HTTP requests and responses
- Kotlin Multiplatform support (Android, iOS, JVM, Linux)
- Easy integration with Ktor's plugin system

## Installation

Add the dependency to your `build.gradle.kts`:

```kotlin
dependencies {
    implementation("io.github.hazemafaneh:ktor-monitor-pro:<version>")
}
```

## Usage

```kotlin
val client = HttpClient {
    install(KtorMonitorPro)
}
```

## License

```
Copyright 2024 Hazem Afaneh

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