plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.google.gms.google.services)
}

android {
    namespace = "com.example.facialflex"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.example.facialflex"
        minSdk = 24
        targetSdk = 34
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
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }

    buildFeatures{
        viewBinding = true
        mlModelBinding = true
    }
}

dependencies {

    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.activity)
    implementation(libs.constraintlayout)
    implementation(libs.firebase.database)
    implementation(libs.tensorflow.lite.support)
    implementation(libs.tensorflow.lite.metadata)
    implementation(libs.tensorflow.lite.gpu)
    implementation(libs.firebase.storage)
    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)

    // Add Glide dependency
    implementation("com.github.bumptech.glide:glide:4.15.1")
    annotationProcessor("com.github.bumptech.glide:compiler:4.12.0")

    // recycler view
    implementation("androidx.recyclerview:recyclerview:1.2.1")
    // exoplayer
    implementation("com.google.android.exoplayer:exoplayer:2.18.5")
    // for older font to access new fonts
    implementation("androidx.core:core:1.7.0")
    // viewpager
    implementation("androidx.viewpager2:viewpager2:1.0.0")
    implementation("com.google.firebase:firebase-database:20.0.5")
    implementation("com.google.android.material:material:1.8.0")
    //img
    implementation("com.github.denzcoskun:ImageSlideshow:0.1.0")
    //pie
    implementation("com.github.blackfizz:eazegraph:1.2.2")
    implementation("com.google.firebase:firebase-auth:21.3.0")

    implementation("nl.dionsegijn:konfetti:1.3.2")

    implementation("com.sun.mail:android-mail:1.6.0")
    implementation("com.sun.mail:android-activation:1.6.0")


    implementation("androidx.swiperefreshlayout:swiperefreshlayout:1.1.0")
    
}

