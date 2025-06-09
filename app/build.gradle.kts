plugins {
    // id("com.android.application")
    // id("org.jetbrains.kotlin.android")
    id("com.google.dagger.hilt.android")

    alias(libs.plugins.android.application)
}

android {
    namespace = "com.example.mad_project"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.example.mad_project"
        minSdk = 31
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"

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
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
}

dependencies {

    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.activity)
    implementation(libs.constraintlayout)
    implementation(libs.navigation.fragment)
    implementation(libs.navigation.ui)
    implementation(libs.work.runtime)
    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)

    // AndroidX
    implementation(libs.androidx.appcompat.v161)
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.viewmodel)
    implementation(libs.androidx.lifecycle.livedata)

    // Room Database
    implementation(libs.androidx.room.runtime)
    implementation(libs.androidx.room.ktx)
    annotationProcessor(libs.androidx.room.compiler)

    // Google Play Services
    implementation(libs.play.services.maps)
    implementation(libs.play.services.location)

    // Retrofit for API calls
    implementation(libs.retrofit)
    implementation(libs.converter.gson)
    implementation(libs.logging.interceptor)

    // Dependency Injection
    implementation(libs.hilt.android)
    annotationProcessor(libs.hilt.android.compiler)

    // Sensor management
    implementation(libs.sensey)

    // Logging
    implementation(libs.timber)

    implementation(libs.jsoup)

    implementation(libs.androidx.navigation.fragment)

    implementation(libs.androidx.work.runtime)
}