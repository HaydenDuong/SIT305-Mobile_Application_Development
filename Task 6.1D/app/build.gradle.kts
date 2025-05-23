plugins {
    alias(libs.plugins.android.application)
}

android {
    namespace = "com.example.personalizedlearningexperienceapp"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.example.personalizedlearningexperienceapp"
        minSdk = 24
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
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
}

dependencies {

    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.activity)
    implementation(libs.constraintlayout)
    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)

    // Necessary dependencies for calling Backend API LLM
    implementation(libs.retrofit)
    implementation(libs.retrofit2.converter.gson)
    implementation(libs.okhttp)
    implementation(libs.navigation.fragment)
    implementation(libs.navigation.ui)

    // Dependencies for Room Database
    implementation(libs.room.runtime)
    annotationProcessor(libs.androidx.room.compiler)

    implementation(libs.gson)
    implementation(libs.androidx.cardview)

    // QR code integration
    implementation("com.google.zxing:core:3.5.1")
    implementation("com.journeyapps:zxing-android-embedded:4.3.0")

    // Google Pay integration
    implementation("com.google.android.gms:play-services-wallet:19.4.0")

    implementation(libs.material)
}