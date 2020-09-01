android {
    compileSdkVersion(29)
    buildToolsVersion("29.0.3")

    defaultConfig {
        applicationId = "me.seebrock3r.elevationtester"
        minSdkVersion(21)
        targetSdkVersion(29)
        versionCode = 6
        versionName = "3.2.1"
    }

    packagingOptions {
        exclude("META-INF/atomicfu.kotlin_module")
    }
}

dependencies {
    implementation("androidx.annotation:annotation:1.1.0")
    implementation("androidx.appcompat:appcompat:1.2.0")
    implementation("androidx.core:core-ktx:1.3.1")
    implementation("androidx.constraintlayout:constraintlayout:2.0.1")
    implementation("com.google.android.material:material:1.2.0")
}
