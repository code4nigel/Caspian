plugins {
    id("com.android.application")
}

android {
    namespace = "com.caspian.betac"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.caspian.betac"
        minSdk = 24
        targetSdk = 34
        versionCode = 107
        versionName = "1.1.96-BetaC"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }
    }
    lint {
        abortOnError = false
        checkReleaseBuilds = false
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    sourceSets {
        getByName("main") {
            assets.srcDirs("src/main/assets")
        }
    }
}

dependencies {
    implementation("androidx.core:core:1.12.0")
    implementation("androidx.appcompat:appcompat:1.6.1")
    implementation("com.google.android.material:material:1.11.0")
    implementation("androidx.browser:browser:1.8.0")
    implementation("androidx.webkit:webkit:1.13.0")
    implementation("androidx.cardview:cardview:1.0.0")
    implementation("androidx.media:media:1.7.0")
    implementation("com.google.android.gms:play-services-mlkit-text-recognition:19.0.0")
}
