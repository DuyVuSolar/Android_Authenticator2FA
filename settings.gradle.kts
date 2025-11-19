import org.gradle.api.initialization.resolve.RepositoriesMode.FAIL_ON_PROJECT_REPOS

include(":ezfilter")


include(":visualizer_record")



pluginManagement {
    resolutionStrategy {
        eachPlugin {
            when (requested.id.id) {
                "androidx.navigation" -> {
                    useModule("androidx.navigation:navigation-safe-args-gradle-plugin:2.7.4")
                }
                "dagger.hilt.android.plugin" -> {
                    useModule("com.google.dagger:hilt-android-gradle-plugin:2.56.2")
                }
            }
        }
    }



    repositories {
        google {
            content {
                includeGroupByRegex("com\\.android.*")
                includeGroupByRegex("com\\.google.*")
                includeGroupByRegex("androidx.*")
            }
        }
        mavenCentral()
        gradlePluginPortal()
        maven(url= "https://jitpack.io")
        maven(url = "https://jcenter.bintray.com")
    }
}
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
        maven(url= "https://jitpack.io")
        maven(url = "https://jcenter.bintray.com")
        maven(url = "https://repository.liferay.com/nexus/content/repositories/public")//android-pdf-viewer

        maven (url ="https://android-sdk.is.com/")
        maven (url ="https://dl-maven-android.mintegral.com/repository/mbridge_android_sdk_oversea")
        maven (url ="https://artifact.bytedance.com/repository/pangle/")
    }
}

rootProject.name = "Android_ReverseVoice"
enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")
include(
    "app",
    "base")
