// project-level build.gradle.kts (Root of your project)
plugins {
    // Correctly define plugins using version references from libs.versions.toml
    alias(libs.plugins.android.application) apply false
    id("org.jetbrains.kotlin.android") version "2.0.0" apply false
    alias(libs.plugins.kotlin.serialization) apply false // Use alias for serialization plugin as well
    alias(libs.plugins.hilt) apply false
}

// IMPORTANT: Remove any old `buildscript` block if it defines dependencies for plugins
// like 'com.android.tools.build:gradle'. Using `plugins { alias(...) }` means you don't need it.
/*
buildscript {
    repositories {
        google()
        mavenCentral()
    }
    dependencies {
        // IF YOU HAD THIS, DELETE IT:
        // classpath("com.android.tools.build:gradle:8.1.0")
    }
}
*/