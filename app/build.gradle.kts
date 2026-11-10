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

dependencies {
    // Provided by the LSPosed framework at runtime, never bundled into the APK.
    compileOnly("io.github.libxposed:api:102.0.0")
}
