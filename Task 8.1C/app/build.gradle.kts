plugins {
    alias(libs.plugins.android.application)
}

android {
    namespace = "com.example.chatbotapp"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.example.chatbotapp"
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

    // Retrofit for networking
    implementation("com.squareup.retrofit2:retrofit:2.9.0")

    // Retrofit Scalars Converter (for plain text response)
    implementation("com.squareup.retrofit2:converter-scalars:2.9.0")

    // OkHttp Loggin Interceptor
    implementation("com.squareup.okhttp3:logging-interceptor:4.11.0")


    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)
}