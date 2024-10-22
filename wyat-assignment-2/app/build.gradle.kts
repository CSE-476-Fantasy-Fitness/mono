plugins {
    alias(libs.plugins.android.application)
}

android {
    namespace = "com.example.cse476app"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.example.cse476app"
        minSdk = 24
        targetSdk = 34
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
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
}

dependencies {
    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.activity)
    implementation(libs.constraintlayout)
    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)

    // CameraX core library
    implementation(libs.camera.core)

    // CameraX Camera2 interoperability
    implementation(libs.camera.camera2)

    // CameraX lifecycle library
    implementation(libs.camera.lifecycle)

    // CameraX view library for PreviewView
    implementation(libs.camera.view)

    // CameraX Extensions for advanced features like Bokeh, HDR, etc.
    implementation(libs.camera.extensions)

    implementation(libs.exifinterface)

}