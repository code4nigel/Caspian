plugins {
    id("com.android.application")
}

android {
    namespace = "com.caspian.betaa"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.caspian.betaa"
        minSdk = 24
        targetSdk = 34
        versionCode = 175
        versionName = "1.0.75-BetaA"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    sourceSets {
        getByName("main") {
            assets.srcDirs("src/main/assets", "../assets")
            manifest.srcFile("../AndroidManifest.xml")
        }
    }
}

dependencies {
    implementation("androidx.core:core:1.12.0")
    implementation("androidx.appcompat:appcompat:1.6.1")
    implementation("com.google.android.material:material:1.11.0")
    implementation("androidx.browser:browser:1.8.0")
}
