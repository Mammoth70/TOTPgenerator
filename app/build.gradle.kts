import org.gradle.kotlin.dsl.kotlin
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
}

val versionMajor = 1
val versionMinor = 3
val versionPatch = 0

android {
    namespace = "ru.mammoth70.totpgenerator"
    compileSdk {
        version = release(36)
    }

    defaultConfig {
        applicationId = "ru.mammoth70.totpgenerator"
        minSdk = 30
        targetSdk = 36
        versionName = "${versionMajor}.${versionMinor}.${versionPatch}"
        versionCode = versionMajor * 10000 + versionMinor * 100 + versionPatch
        setProperty("archivesBaseName", "TOTPgenerator-${versionName}")
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }

    buildFeatures {
        buildConfig = true
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }

    kotlin {
        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_11)
            freeCompilerArgs.add("-opt-in=kotlin.RequiresOptIn")
        }
    }
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.androidx.activity)
    implementation(libs.androidx.constraintlayout)
    //noinspection UseTomlInstead
    implementation("com.google.android.material:material:1.14.0-alpha08")
    //noinspection UseTomlInstead
    implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:2.10.0")
    //noinspection UseTomlInstead
    implementation("dev.robinohs:totp-kt:1.0.1")
    //noinspection UseTomlInstead
    implementation("commons-codec:commons-codec:1.20.0")
    //noinspection UseTomlInstead
    implementation("androidx.security:security-crypto:1.1.0")
    //noinspection UseTomlInstead
    implementation("com.google.android.gms:play-services-code-scanner:16.1.0")
    //noinspection UseTomlInstead
    implementation("com.google.protobuf:protobuf-javalite:4.33.2")
    implementation("androidx.biometric:biometric:1.4.0-alpha02")
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
}