plugins {
    id("com.android.application")
}

android {
    namespace = "com.caspian.betab"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.caspian.betab"
        minSdk = 24
        targetSdk = 34
        versionCode = 6
        versionName = "1.0.5-BetaB"
    }

    buildTypes {
        getByName("release") {
            isMinifyEnabled = false
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    sourceSets {
        getByName("main") {
            assets.srcDirs("../assets")
            res.srcDirs("src/main/res")
            manifest.srcFile("../AndroidManifest.xml")
        }
    }
}

dependencies {
    implementation("androidx.core:core:1.12.0")
    implementation("androidx.appcompat:appcompat:1.6.1")
    implementation("com.google.android.material:material:1.11.0")
    implementation("androidx.browser:browser:1.8.0")
    implementation("com.squareup.okhttp3:okhttp:4.12.0")
    implementation("io.noties.markwon:core:4.6.2")
}
