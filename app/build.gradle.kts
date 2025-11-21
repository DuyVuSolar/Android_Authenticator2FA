
plugins {
    id(Plugins.ANDROID_APPLICATION)
    kotlin(Plugins.ANDROID)
    id(Plugins.DAGGER_HILT)
    id(Plugins.protobuf)
    id(Plugins.NAVIGATION_SAFE_ARGS)
    id(Plugins.PARCELIZE)
    id(Plugins.GOOGLE_SERVICE)
    id(Plugins.GOOGLE_CRASH)
    kotlin(Plugins.KAPT)
    id("com.mikepenz.aboutlibraries.plugin")
}

android {
    namespace = "com.beemdevelopment.aegis"
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
            resValue("bool", "pref_secure_screen_default", "true")

        }
        debug {
            isMinifyEnabled = false
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            manifestPlaceholders["title"] = "DUYVD"
            manifestPlaceholders["iconName"] = "ic_launcher"
            manifestPlaceholders["fileProviderAuthority"] = "DUYVD"
            resValue("bool", "pref_secure_screen_default", "false")
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_18
        targetCompatibility = JavaVersion.VERSION_18
        isCoreLibraryDesugaringEnabled = true
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
    lint {
        abortOnError = true
        checkDependencies = true
        checkReleaseBuilds = false
    }
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

protobuf {
    protoc {
        artifact = "com.google.protobuf:protoc:3.25.1"
    }
    generateProtoTasks {
        ofSourceSet("main").forEach { task ->

            task.builtins {
                getByName("java") {
                    option("lite")
                }
            }
            task.builtins {
                getByName("kotlin") {
                    option("lite")
                }
            }
        }
    }
}



aboutLibraries {
    // Tasks for aboutLibraries are not run automatically to keep the build reproducible
    // To update manually: ./gradlew app:exportLibraryDefinitions -PaboutLibraries.exportPath=src/main/res/raw
    prettyPrint = true
    configPath = "app/config"
    fetchRemoteFunding = false
    registerAndroidTasks = false
    duplicationMode = com.mikepenz.aboutlibraries.plugin.DuplicateMode.MERGE
}



dependencies {
    implementation(libs.camerax.core)
    implementation(libs.camera.lifecycle)
    implementation(libs.camerax.view)

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

    implementation("com.github.topjohnwu.libsu:core:6.0.0")
    implementation("com.github.topjohnwu.libsu:io:6.0.0")
    implementation("androidx.documentfile:documentfile:1.1.0")
    implementation("com.caverock:androidsvg-aar:1.4")
    implementation("com.github.avito-tech:krop:0.52")
    implementation("com.mikepenz:aboutlibraries-core-android:11.2.3")
    implementation("com.nulab-inc:zxcvbn:1.9.0")
    implementation("net.lingala.zip4j:zip4j:2.11.5")
    implementation("org.bouncycastle:bcprov-jdk18on:1.80")
    implementation("org.simpleflatmapper:sfm-csv:8.2.3")
    implementation("androidx.preference:preference:1.2.1")
    implementation ("androidx.biometric:biometric:1.1.0")
    implementation("com.mikepenz:aboutlibraries:11.2.3")
    implementation("com.google.protobuf:protobuf-javalite:4.31.0")
    implementation("com.google.zxing:core:3.5.3")
    implementation("com.github.bumptech.glide:recyclerview-integration:4.16.0")

//5.4.4
    implementation ("com.adjust.sdk:adjust-android:5.4.4")
    implementation ("com.android.installreferrer:installreferrer:2.2")
    implementation ("com.google.android.gms:play-services-ads-identifier:18.2.0")
}

kapt {
    correctErrorTypes = true
}