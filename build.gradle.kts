import com.github.benmanes.gradle.versions.updates.DependencyUpdatesTask
// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {
    id(Plugins.ANDROID_APPLICATION) version (PluginVersion.AGP) apply false
    id(Plugins.ANDROID_LIBRARY) version (PluginVersion.AGP) apply false
    kotlin(Plugins.ANDROID) version (PluginVersion.KGP) apply false
    id(Plugins.AndroidxNavigation) version (PluginVersion.Navigation) apply false
    id(Plugins.Detekt) version (PluginVersion.Detekt)
    id(Plugins.BenManesVersions) version (PluginVersion.BenManesVersions)
    id(Plugins.DAGGER_HILT) apply false
    id(Plugins.GOOGLE_SERVICE) version (PluginVersion.Service) apply false
    id(Plugins.GOOGLE_CRASH) version (PluginVersion.Crash) apply false
}

tasks {
    withType<DependencyUpdatesTask>().configureEach {
        rejectVersionIf {
            candidate.version.isStableVersion().not()
        }
    }
}
