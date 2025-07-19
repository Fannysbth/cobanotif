pluginManagement {
    repositories {
        gradlePluginPortal()
        google() 
        mavenCentral()
    }

    plugins {
        id("com.android.application") version "8.0.2" 
        id("org.jetbrains.kotlin.android") version "1.9.0"
        id("com.google.gms.google-services") version "4.3.15"
    }
}

dependencyResolutionManagement {
    repositories {
        google()
        mavenCentral()
    }
}

rootProject.name = "cobanotif"
include(":app")
