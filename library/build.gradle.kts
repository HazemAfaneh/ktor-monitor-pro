import com.android.build.api.dsl.androidLibrary
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.android.kotlin.multiplatform.library)
    alias(libs.plugins.vanniktech.mavenPublish)
}

group = "io.github.hazemafaneh"
version = "1.0.0"

kotlin {
    jvm()
    androidLibrary {
        namespace = "io.github.hazemafaneh.ktormonitorpro"
        compileSdk = libs.versions.android.compileSdk.get().toInt()
        minSdk = libs.versions.android.minSdk.get().toInt()

        withJava() // enable java compilation support
        withHostTestBuilder {}.configure {}
        withDeviceTestBuilder {
            sourceSetTreeName = "test"
        }

        compilations.configureEach {
            compileTaskProvider.configure {
                compilerOptions {
                    jvmTarget.set(JvmTarget.JVM_11)
                }
            }
        }
    }
    iosX64()
    iosArm64()
    iosSimulatorArm64()
    linuxX64()

    sourceSets {
        commonMain.dependencies {
            //put your multiplatform dependencies here
        }

        commonTest.dependencies {
            implementation(libs.kotlin.test)
        }
    }
}

mavenPublishing {
    publishToMavenCentral()

    signAllPublications()

    coordinates(group.toString(), "ktor-monitor-pro", version.toString())

    pom {
        name = "Ktor Monitor Pro"
        description = "A Kotlin Multiplatform library for monitoring Ktor HTTP client traffic."
        inceptionYear = "2024"
        url = "https://github.com/hazemafaneh/ktor-monitor-pro/"
        licenses {
            license {
                name = "Apache License, Version 2.0"
                url = "https://www.apache.org/licenses/LICENSE-2.0.txt"
                distribution = "repo"
            }
        }
        developers {
            developer {
                id = "hazemafaneh"
                name = "Hazem Afaneh"
                url = "https://github.com/hazemafaneh/"
            }
        }
        scm {
            url = "https://github.com/hazemafaneh/ktor-monitor-pro/"
            connection = "scm:git:git://github.com/hazemafaneh/ktor-monitor-pro.git"
            developerConnection = "scm:git:ssh://git@github.com/hazemafaneh/ktor-monitor-pro.git"
        }
    }
}
