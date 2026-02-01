plugins {
    alias(libs.plugins.android.application)
}

android {
    namespace = "ru.mammoth70.totpgenerator"
    compileSdk {
        version = release(36)
    }

    defaultConfig {
        val versionMajor = 1
        val versionMinor = 8
        val versionPatch = 1
        applicationId = "ru.mammoth70.totpgenerator"
        minSdk = 30
        targetSdk = 36
        versionName = "${versionMajor}.${versionMinor}.${versionPatch}"
        versionCode = versionMajor * 10000 + versionMinor * 100 + versionPatch
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        @Suppress("DEPRECATION")
        resourceConfigurations.clear()
        @Suppress("DEPRECATION")
        resourceConfigurations.addAll(listOf("en", "ru"))
    }

    buildTypes {
        release {
           isMinifyEnabled = true
           isShrinkResources = true
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
}

base {
    archivesName = "TOTPgenerator-${android.defaultConfig.versionName}"
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.androidx.activity)
    implementation(libs.androidx.constraintlayout)
    implementation(libs.google.material)
    implementation(libs.androidx.lifecycle.viewmodel.ktx)
    implementation(libs.totp.kt)
    implementation(libs.apache.commons.codec)
    implementation(libs.androidx.security.crypto)
    implementation(libs.gms.play.services.code.scanner)
    implementation(libs.protobuf.javalite)
    implementation(libs.androidx.biometric)

    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
}