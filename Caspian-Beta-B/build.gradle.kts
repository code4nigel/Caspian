buildscript {
    repositories {
        google()
        mavenCentral()
    }
    dependencies {
        classpath("com.android.tools.build:gradle:8.5.2")
    }
}

plugins {
    id("com.android.application") version "8.5.2" apply false
}

task<Delete>("clean") {
    delete(rootProject.buildDir)
}