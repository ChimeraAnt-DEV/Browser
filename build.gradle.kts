// Top-level build file for the Bastion browser.
plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.kotlin.compose) apply false
    alias(libs.plugins.kotlin.serialization) apply false
}

tasks.register<Delete>("cleanAll") {
    delete(layout.buildDirectory)
}