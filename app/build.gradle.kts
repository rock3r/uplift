android {
    compileSdkVersion(30)

    defaultConfig {
        applicationId = "me.seebrock3r.elevationtester"
        minSdkVersion(21)
        targetSdkVersion(30)
        versionCode = 7
        versionName = "3.2.2"
    }

    packagingOptions {
        exclude("META-INF/atomicfu.kotlin_module")
    }

    buildTypes {
        named("release") {
            isMinifyEnabled = false
        }
    }
}

repositories {
    maven { setUrl("https://jitpack.io") }
}

dependencies {
    implementation("androidx.annotation:annotation:1.1.0")
    implementation("androidx.appcompat:appcompat:1.2.0")
    implementation("androidx.core:core-ktx:1.3.1")
    implementation("androidx.constraintlayout:constraintlayout:2.0.1")
    implementation("com.google.android.material:material:1.2.0")
    implementation("com.github.sephiroth74:android-target-tooltip:2.0.4")
}
