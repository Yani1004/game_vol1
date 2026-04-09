plugins {
    alias(libs.plugins.android.application)
}

android {
    namespace = "com.example.game_vol1"
    compileSdk {
        version = release(36) {
            minorApiLevel = 1
        }
    }

    defaultConfig {
        applicationId = "com.example.game_vol1"
        minSdk = 24
        targetSdk = 36
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
    implementation(libs.core.ktx)
    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.activity)
    implementation(libs.constraintlayout)
    implementation(libs.lifecycle.viewmodel.ktx)
    implementation(libs.lifecycle.livedata.ktx)
    implementation("com.google.ar:core:1.41.0")
    implementation("com.gorisse.thomas.sceneform:sceneform:1.23.0")
    implementation("com.google.android.gms:play-services-location:21.2.0")
    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)
    // 1. MVVM Архитектура (ViewModel и LiveData)

    // 2. Google ARCore (За Добавената реалност - AR Bonus)
    // Sceneform (Улеснява работата с 3D модели в Java)

    // 3. Google Maps и Местоположение (GPS)
    implementation("com.google.android.gms:play-services-maps:18.2.0")
}
