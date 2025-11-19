plugins {
    id(Plugins.ANDROID_APPLICATION)
    kotlin(Plugins.ANDROID)
    id(Plugins.DAGGER_HILT)
    id(Plugins.NAVIGATION_SAFE_ARGS)
    id(Plugins.PARCELIZE)
    id(Plugins.GOOGLE_SERVICE)
    id(Plugins.GOOGLE_CRASH)
    kotlin(Plugins.KAPT)
}

android {
    namespace = "com.kuemiin.reversevoice"
    compileSdk = AndroidConfig.COMPILE_SDK

    defaultConfig {
        applicationId = AndroidConfig.APPLICATION_ID
        minSdk = AndroidConfig.MIN_SDK
        targetSdk = AndroidConfig.TARGET_SDK
        multiDexEnabled = AndroidConfig.MULTIDEX

        versionCode = AndroidConfig.VERSION_CODE
        versionName = AndroidConfig.VERSION_NAME

        vectorDrawables.useSupportLibrary = true
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
        debug {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_18
        targetCompatibility = JavaVersion.VERSION_18
    }
    kotlinOptions {
        jvmTarget = JavaVersion.VERSION_18.toString()
    }
    buildFeatures {
        dataBinding = true
        viewBinding = true
    }

    dataBinding { enable = true }
    buildFeatures {
        dataBinding = true
        viewBinding = true
    }
    lint { checkReleaseBuilds = false }
    hilt { enableAggregatingTask = true }
    lint {
        disable += "UnusedResources"
    }

    @Suppress("DEPRECATION")
    packagingOptions {
        pickFirst("**/*.so")
        resources.excludes.add("META-INF/*")
    }
}

dependencies {
    implementation(projects.ezfilter)
    implementation(projects.base)
    implementation(projects.visualizerRecord)

    implementation(libs.camerax.core)
    implementation(libs.camera.lifecycle)

    coreLibraryDesugaring(libs.desugar)
    implementation(libs.hilt)
    kapt(libs.hilt.compiler)
    kapt(libs.glide.compiler)

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
    implementation(libs.viewpager2)
    implementation(libs.coroutines)
    implementation(libs.coroutines.core)
    implementation(libs.glide)
//    implementation(libs.glide.webp)
    implementation(libs.okhttp3.integration)

    implementation(libs.datastore)
    implementation(libs.timber)
    implementation(platform(libs.firebase.bom))
    implementation(libs.crashlytics)
    implementation(libs.analytics)
    implementation(libs.configs)
    implementation(libs.exo.core)
    implementation(libs.exo.ui)
    implementation(libs.gson)

    implementation(libs.room.runtime)
    implementation(libs.room.ktx)
    kapt(libs.room.compiler)

    implementation(libs.carbonview)
    implementation(libs.gifdrawable)
    implementation(libs.calligraphy)
    implementation(libs.androidx.lifecycle.livedata.ktx)
    implementation(libs.androidx.multidex)
    implementation(libs.android.downloader)
    implementation(libs.dexter)

    implementation(libs.lottie)

    implementation(libs.verticalviewpager.library)
    implementation("com.github.skydoves:colorpickerview:2.3.0")

    implementation("com.google.android.gms:play-services-ads:24.3.0")
    implementation("com.facebook.shimmer:shimmer:0.5.0")


    // MediaPipe Library
    implementation("com.google.mediapipe:tasks-vision:0.10.26.1")

//5.4.4
    implementation ("com.adjust.sdk:adjust-android:5.4.4")
    implementation ("com.android.installreferrer:installreferrer:2.2")
    implementation ("com.google.android.gms:play-services-ads-identifier:18.2.0")
}

kapt {
    correctErrorTypes = true
}