import com.android.build.gradle.internal.cxx.configure.gradleLocalProperties

plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("org.jetbrains.kotlin.plugin.compose")
}

android {
    namespace = "com.mandiri.newsapp"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.mandiri.newsapp"
        minSdk = 24
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"
        vectorDrawables { useSupportLibrary = true }

        // inject API key dari local.properties
        val newsKey = gradleLocalProperties(rootDir, providers)
            .getProperty("NEWS_API_KEY") ?: ""
        buildConfigField("String", "NEWS_API_KEY", "\"$newsKey\"")
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    buildFeatures {
        compose = true
        buildConfig = true   // ⬅️ WAJIB: aktifkan fitur BuildConfig
    }
}

kotlin { jvmToolchain(17) }

dependencies {
    val composeBom = platform("androidx.compose:compose-bom:2024.10.00")
    implementation(composeBom)
    androidTestImplementation(composeBom)

    implementation("androidx.activity:activity-compose:1.9.3")
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.material3:material3:1.3.0")
    implementation("androidx.compose.ui:ui-tooling-preview")
    debugImplementation("androidx.compose.ui:ui-tooling")

    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.8.6")
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.8.6")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.9.0")

    implementation("com.squareup.retrofit2:retrofit:2.11.0")
    implementation("com.squareup.retrofit2:converter-gson:2.11.0")
    implementation("com.squareup.okhttp3:logging-interceptor:4.12.0")

    implementation("androidx.paging:paging-runtime:3.3.2")
    implementation("androidx.paging:paging-compose:3.3.2")

    implementation("io.coil-kt:coil-compose:2.7.0")
}
