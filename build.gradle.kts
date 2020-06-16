import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

buildscript {
    repositories {
        google()
        mavenCentral()
        jcenter()
    }

    dependencies {
        classpath("com.android.tools.build:gradle:4.0.0")
    }
}

plugins {
    kotlin("jvm") version "1.3.72"
}

allprojects {
    repositories {
        google()
        mavenCentral()
        jcenter()
    }

    tasks {
        withType<JavaCompile> {
            sourceCompatibility = "1.8"
            targetCompatibility = "1.8"
        }

        withType<KotlinCompile> {
            kotlinOptions {
                jvmTarget = "1.8"
            }
        }
    }
}

val kotlinVersion = "1.3.72"
val coroutinesVersion = "1.3.7"

subprojects {
    apply(plugin = "com.android.application")
    apply(plugin = "kotlin-android")
    apply(plugin = "kotlin-android-extensions")

    dependencies {
        implementation(kotlin("stdlib-jdk7"))
        implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:$coroutinesVersion")
        implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:$coroutinesVersion")
    }
}

tasks {
    clean {
        delete(rootProject.buildDir)
    }
}
