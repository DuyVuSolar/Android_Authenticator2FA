plugins {
    id(Plugins.ANDROID_LIBRARY)
    kotlin(Plugins.ANDROID)
    kotlin(Plugins.KAPT)
    id(Plugins.NAVIGATION_SAFE_ARGS)
}


android {
    namespace = "com.kuemiin.base"
    compileSdk = AndroidConfig.COMPILE_SDK

    defaultConfig {
        minSdk = AndroidConfig.MIN_SDK
        targetSdk = AndroidConfig.TARGET_SDK
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
        dataBinding = true
        viewBinding = true
    }

    dataBinding{
        enable = true
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_18
        targetCompatibility = JavaVersion.VERSION_18
    }
    kotlinOptions {
        jvmTarget = JavaVersion.VERSION_18.toString()
        freeCompilerArgs = freeCompilerArgs + "-opt-in=kotlinx.coroutines.ExperimentalCoroutinesApi"
    }

    lint {
        disable += "UnusedResources"
        warningsAsErrors = true
        abortOnError = true
    }
}

dependencies {

//    kapt(libs.room.compiler)
//    kapt(libs.glide.compiler)

    implementation(libs.core.ktx)
    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.constraintlayout)
    implementation(libs.sdp)
    implementation(libs.ssp)
    implementation(libs.fragment.ktx)
    implementation(libs.viewmodel)
    implementation(libs.lifecycle.runtime)
    implementation(libs.navigation.fragment)
    implementation(libs.navigation.ui)
    implementation(libs.recyclerview)
    implementation(libs.exo.core)
    implementation(libs.exo.ui)
//    implementation(libs.room.runtime)
//    implementation(libs.room.ktx)
    implementation(libs.viewpager2)
    implementation(libs.coroutines)
    implementation(libs.coroutines.core)
    implementation(libs.glide)
    implementation(libs.datastore)
    implementation(libs.timber)
}

kapt {
    correctErrorTypes = true
}