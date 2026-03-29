import org.jetbrains.kotlin.gradle.ExperimentalKotlinGradlePluginApi
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidLibrary)
    alias(libs.plugins.kotlinSerialization)
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.composeCompiler)
    alias(libs.plugins.vanniktech.mavenPublish)
}

group = "io.github.hazemafaneh"
version = "1.0.0"

kotlin {
    androidTarget {
        @OptIn(ExperimentalKotlinGradlePluginApi::class)
        compilerOptions { jvmTarget.set(JvmTarget.JVM_11) }
    }
    iosX64()
    iosArm64()
    iosSimulatorArm64()

    sourceSets {
        commonMain.dependencies {
            implementation(compose.runtime)
            implementation(compose.foundation)
            implementation(compose.material3)
            implementation(compose.materialIconsExtended)
            implementation(compose.ui)
            implementation(libs.ktor.client.core)
            implementation(libs.kotlinx.serialization.json)
            implementation(libs.kotlinx.coroutines.core)
        }
        androidMain.dependencies {
            implementation(libs.kotlinx.coroutines.android)
        }
        commonTest.dependencies {
            implementation(libs.kotlin.test)
        }
    }
}

android {
    namespace  = "io.github.hazemafaneh.networkinspectionpro"
    compileSdk = libs.versions.android.compileSdk.get().toInt()
    defaultConfig { minSdk = libs.versions.android.minSdk.get().toInt() }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
}

mavenPublishing {
    publishToMavenCentral()
    if (project.hasProperty("signing.keyId") ||
        System.getenv("ORG_GRADLE_PROJECT_signingInMemoryKey") != null) {
        signAllPublications()
    }
    coordinates(group.toString(), "network-inspection-pro", version.toString())
    pom {
        name = "NetworkInspectionPro"
        description = "A Kotlin Multiplatform Ktor network inspector with a shake-to-open Compose UI."
        inceptionYear = "2025"
        url = "https://github.com/hazemafaneh/ktor-monitor-pro/"
        licenses { license {
            name = "Apache License, Version 2.0"
            url = "https://www.apache.org/licenses/LICENSE-2.0.txt"
            distribution = "repo"
        }}
        developers { developer {
            id = "hazemafaneh"; name = "Hazem Afaneh"
            url = "https://github.com/hazemafaneh/"
        }}
        scm {
            url = "https://github.com/hazemafaneh/ktor-monitor-pro/"
            connection = "scm:git:git://github.com/hazemafaneh/ktor-monitor-pro.git"
            developerConnection = "scm:git:ssh://git@github.com/hazemafaneh/ktor-monitor-pro.git"
        }
    }
}
