plugins {
    alias(libs.plugins.android.application)
    id("com.google.gms.google-services")
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
    buildFeatures {
        viewBinding = true
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

    // Gson Converter to Retrofit
    implementation("com.squareup.retrofit2:converter-gson:2.9.0")

    // OkHttp Loggin Interceptor
    implementation("com.squareup.okhttp3:logging-interceptor:4.11.0")

    // Firebase Bill of Materials (BoM)
    implementation(platform("com.google.firebase:firebase-bom:33.14.0"))

    // Firebase Authentication
    implementation(libs.firebase.auth)

    implementation("com.google.android.gms:play-services-auth:21.3.0")
    implementation(libs.navigation.fragment)
    implementation(libs.navigation.ui)

    implementation("androidx.navigation:navigation-fragment-ktx:2.9.0")
    implementation("androidx.navigation:navigation-ui-ktx:2.9.0")
    implementation("com.google.android.material:material:1.12.0")

    implementation("com.android.volley:volley:1.2.1")

    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)
}