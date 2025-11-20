plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    id("kotlin-kapt")
    id("com.google.gms.google-services")
}

android {
    namespace = "com.fahad.i210394"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.fahad.i210394"
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
    kotlinOptions {
        jvmTarget = "11"
    }
}
dependencies {

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.activity)
    implementation(libs.androidx.constraintlayout)
    implementation("androidx.fragment:fragment-ktx:1.6.2")
    implementation("androidx.navigation:navigation-fragment-ktx:2.7.5")
    implementation("androidx.navigation:navigation-ui-ktx:2.7.5")
    implementation ("androidx.camera:camera-core:1.3.0")
    implementation ("androidx.camera:camera-camera2:1.3.0")
    implementation ("androidx.camera:camera-lifecycle:1.3.0")
    implementation ("androidx.camera:camera-view:1.3.0")
    implementation ("androidx.room:room-runtime:2.6.1")
    implementation(libs.androidx.lifecycle.process)
    implementation(libs.firebase.messaging)
    implementation("com.google.firebase:firebase-messaging:23.4.0")

    implementation("com.google.firebase:firebase-core:21.1.1")

    kapt("androidx.room:room-compiler:2.6.1")
    implementation( "androidx.room:room-ktx:2.6.1")

    implementation ("com.github.bumptech.glide:glide:4.16.0")
    kapt("com.github.bumptech.glide:compiler:4.16.0")

    implementation("io.agora.rtc:full-sdk:4.2.3")
    implementation("com.android.volley:volley:1.2.1")
    implementation ("de.hdodenhof:circleimageview:3.1.0")

    implementation(libs.core.ktx)
    implementation(libs.androidx.junit.ktx)
    implementation(libs.androidx.espresso.intents)
    testImplementation(libs.junit)
    testImplementation(libs.androidx.rules)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")

    // JUnit for testing
    androidTestImplementation("androidx.test.ext:junit:1.1.5")

    // AndroidX Test Runner & Rules
    androidTestImplementation("androidx.test:runner:1.5.2")
    androidTestImplementation("androidx.test:rules:1.5.0")
}