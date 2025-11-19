plugins {
    `kotlin-dsl`
}
repositories {
    google()
    mavenCentral()
    maven(url= "https://jitpack.io")
    maven(url= "https://maven.google.com")
}

tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile>().configureEach {
    kotlinOptions {
        jvmTarget = JavaVersion.VERSION_18.toString()
    }
}