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

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_25
        targetCompatibility = JavaVersion.VERSION_25
    }
}

dependencies {
    // Provided by the LSPosed framework at runtime, never bundled into the APK.
    compileOnly("io.github.libxposed:api:102.0.0")
}
