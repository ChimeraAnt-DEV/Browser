plugins {
    // AGP 9 ships built-in Kotlin support; the separate kotlin-android plugin
    // is intentionally NOT applied (it is rejected by AGP 9).
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.kotlin.serialization)
}

android {
    namespace = "com.browseglass"
    compileSdk = 37
    // GeckoView 159 requires Android API 37.2.
    compileSdkMinor = 2

    defaultConfig {
        applicationId = "com.browseglass"
        minSdk = 26
        targetSdk = 37
        versionCode = 1
        versionName = "0.1.0"

        // Universal APK: 64-bit (arm64-v8a) + 32-bit (armeabi-v7a).
        ndk {
            abiFilters += listOf("arm64-v8a", "armeabi-v7a")
        }
        vectorDrawables.useSupportLibrary = true
    }

    signingConfigs {
        create("release") {
            storeFile = rootProject.file("keystore/bastion.jks")
            storePassword = providers.gradleProperty("BASTION_STORE_PASSWORD").orElse("android").get()
            keyAlias = providers.gradleProperty("BASTION_KEY_ALIAS").orElse("bastion").get()
            keyPassword = providers.gradleProperty("BASTION_KEY_PASSWORD").orElse("android").get()
        }
    }

    buildTypes {
        debug {
            applicationIdSuffix = ".debug"
            versionNameSuffix = "-debug"
            isMinifyEnabled = false
            isDebuggable = true
            enableUnitTestCoverage = false
        }
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            // R8 + ProGuard rules for GeckoView and the app.
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro",
                "geckoview-proguard.pro",
            )
            signingConfig = signingConfigs.getByName("release")
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    buildFeatures {
        compose = true
        buildConfig = true
        viewBinding = true
    }
    lint {
        // Lint runs as its own CI step; do not block APK assembly on it.
        abortOnError = false
        checkReleaseBuilds = false
    }
    packaging {
        resources.excludes += "/META-INF/{AL2.0,LGPL2.1}"
        resources.excludes += "META-INF/DEPENDENCIES"
        resources.excludes += "META-INF/LICENSE*"
        resources.excludes += "META-INF/NOTICE*"
    }
}

dependencies {
    // Jetpack/Compose
    implementation(platform(libs.compose.bom))
    implementation(libs.compose.ui)
    implementation(libs.compose.ui.tooling.preview)
    implementation(libs.compose.material3)
    implementation(libs.compose.material.icons)
    implementation(libs.compose.animation)
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.lifecycle.runtime.compose)
    implementation(libs.androidx.lifecycle.process)
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.fragment.ktx)
    implementation(libs.androidx.navigation.compose)
    implementation(libs.androidx.datastore.preferences)
    implementation(libs.androidx.palette.ktx)
    implementation(libs.androidx.biometric)
    implementation(libs.haze)
    implementation(libs.androidx.profileinstaller)
    implementation(libs.kotlinx.coroutines.android)
    implementation(libs.kotlinx.serialization.json)

    // GeckoView engine (sole rendering engine).
    implementation(libs.geckoview)

    debugImplementation(libs.compose.ui.tooling)

    // Testing
    testImplementation(libs.junit)
}

// The Rust JNI library is produced by cargo-ndk into
// app/src/main/jniLibs/<abi>/libbastion.so (standard AGP location, picked up
// automatically). `build-android-rust.sh` drives the cross-compile for both
// the 64-bit (arm64-v8a) and 32-bit (armeabi-v7a) targets.
android.sourceSets.getByName("main") {
    jniLibs.srcDirs("src/main/jniLibs")
}