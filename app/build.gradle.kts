plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.hilt)
    alias(libs.plugins.ksp)
}

// Build 130/131: androidx.graphics:graphics-path:1.1.0 ships a native binary whose GNU_RELRO
// failed the real 16-KB artifact gate on both arm64-v8a and x86_64. Because this app's
// minSdk is 34, an app-local API-compatible implementation uses the platform PathIterator
// and does not need the pre-34 native fallback. Keep this exclusion until upstream evidence
// proves a released graphics-path artifact is compliant and the local shim is deliberately removed.
configurations.configureEach {
    exclude(group = "androidx.graphics", module = "graphics-path")
}

android {
    namespace   = "now.abfahrt.transit"
    compileSdk  = 37

    defaultConfig {
        applicationId   = "now.abfahrt.transit"
        minSdk          = 34       // Android 14 — three major versions behind Android 17
        targetSdk       = 37       // Android 17 target behavior accepted in Build 132
        versionCode = 1550
        versionName     = "1.1.0"
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
            // RELEASE1 deliberately keeps Gradle signing disabled. The public APK is signed
            // after alignment with apksigner using the old->new signing-certificate lineage.
            // This avoids accidentally producing a new-key-only APK that cannot update the
            // three existing old-key installations.
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    buildFeatures {
        compose = true
        buildConfig = true
    }
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime)
    implementation(libs.androidx.lifecycle.viewmodel)
    implementation(libs.androidx.lifecycle.process)
    implementation(libs.androidx.lifecycle.compose)
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.splashscreen)

    // Compose
    implementation(platform(libs.compose.bom))
    implementation(libs.compose.ui)
    implementation(libs.compose.ui.graphics)
    implementation(libs.compose.ui.tooling.preview)
    implementation(libs.compose.material3)
    implementation(libs.compose.material.icons)
    implementation(libs.navigation.compose)
    debugImplementation(libs.compose.ui.tooling)

    // DI
    implementation(libs.hilt.android)
    ksp(libs.hilt.compiler)
    implementation(libs.hilt.navigation.compose)

    // Network
    implementation(libs.retrofit.core)
    implementation(libs.retrofit.gson)
    implementation(libs.okhttp.logging)
    implementation(libs.gson)

    // Preferences
    implementation(libs.datastore.preferences)

    // Build 130 replaces the non-16-KB-compliant AndroidX graphics-path prebuilt with
    // a minSdk-34 app-local compatibility implementation backed by platform PathIterator.
    // The external module is excluded globally above so no native graphics-path .so is packaged.

    // Location
    implementation(libs.play.services.location)
    implementation(libs.accompanist.permissions)

    // AppCompat (required for Theme.AppCompat.NoActionBar)
    implementation("androidx.appcompat:appcompat:1.7.0")

    // Coroutines
    implementation(libs.kotlinx.coroutines)

    // MapLibre
    implementation("org.maplibre.gl:android-sdk-opengl:13.6.0")

    // Unit tests
    testImplementation(libs.junit)
}
