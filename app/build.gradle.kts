plugins {
    id("com.android.application")
}

android {
    namespace = "com.zmw.batteryinfo"
    compileSdk {
        version = release(37)
    }

    defaultConfig {
        applicationId = "com.zmw.batteryinfo"
        minSdk = 33
        targetSdk = 37
        versionCode = 1
        versionName = "1.0.0"
    }

    // AGP 9 built-in Kotlin only supports JVM targets up to 24. Upstream pins KGP 2.4.21
    // through extra Kotlin plugins; this minimal module needs none of them, so target 17.
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
}

// No Kotlin sources here. AGP 9 may still put kotlin-stdlib on the runtime classpath;
// excluding it keeps the APK down to a few KB instead of 2.4 MB.
configurations.configureEach {
    exclude(group = "org.jetbrains.kotlin", module = "kotlin-stdlib")
}

dependencies {
    // Provided by the LSPosed framework at runtime, never bundled into the APK.
    compileOnly("io.github.libxposed:api:102.0.0")
}
