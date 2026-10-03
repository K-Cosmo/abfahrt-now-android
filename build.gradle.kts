// AGP 9 built-in Kotlin uses the KGP runtime on the build classpath.
// Pin 2.3.21 to match the Compose Compiler plugin used by this project.
buildscript {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
    dependencies {
        classpath("org.jetbrains.kotlin:kotlin-gradle-plugin:2.3.21")
    }
}

plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.kotlin.compose)      apply false
    alias(libs.plugins.hilt)               apply false
    alias(libs.plugins.ksp)                apply false
}
