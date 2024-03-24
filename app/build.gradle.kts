plugins {
    id("com.android.application")
    id("com.google.gms.google-services")
    id("com.google.devtools.ksp")
}

android {
    namespace = "com.fyp.musclefatigue"
    compileSdk = 33

    defaultConfig {
        applicationId = "com.fyp.musclefatigue"
        minSdk = 24
        targetSdk = 33
        versionCode = 4
        versionName = "1.3"
        multiDexEnabled = true
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildFeatures {
        viewBinding = true
    }

    signingConfigs {
        getByName("debug") {
            storeFile = file("keystore.jks")
            storePassword = "Money123@"
            keyAlias = "fyp"
            keyPassword = "Money123@"
        }
        create("release") {
            storeFile = file("keystore.jks")
            storePassword = "Money123@"
            keyAlias = "fyp"
            keyPassword = "Money123@"
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            signingConfig = signingConfigs.getByName("release")
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

    packagingOptions {
        resources.excludes.add("META-INF/DEPENDENCIES")
        resources.excludes.add("META-INF/LICENSE")
        resources.excludes.add("META-INF/LICENSE.txt")
        resources.excludes.add("META-INF/license.txt")
        resources.excludes.add("META-INF/NOTICE")
        resources.excludes.add("META-INF/NOTICE.txt")
        resources.excludes.add("META-INF/notice.txt")
        resources.excludes.add("META-INF/ASL2.0")
    }

}

dependencies {

    implementation("androidx.appcompat:appcompat:1.6.1")
    implementation("com.google.android.material:material:1.9.0")
    implementation("androidx.constraintlayout:constraintlayout:2.1.4")


    implementation(platform("com.google.firebase:firebase-bom:32.3.1"))
    implementation("com.google.android.gms:play-services-auth:20.7.0")
    implementation("com.google.firebase:firebase-analytics")
    implementation("com.google.firebase:firebase-firestore")
    implementation("com.google.firebase:firebase-auth")

    //glide
    implementation ("com.github.bumptech.glide:glide:4.12.0")
    ksp ("com.github.bumptech.glide:compiler:4.12.0")
    implementation ("com.github.bumptech.glide:okhttp3-integration:4.12.0")

    // charts
    implementation ("com.github.PhilJay:MPAndroidChart:v3.1.0")

    // Shimmer sensor
    implementation ("androidx.multidex:multidex:2.0.1")
    implementation ("com.shimmerresearch:shimmerandroidinstrumentdriver:3.0.74_beta")
    implementation ("com.shimmerresearch:shimmerbluetoothmanager:0.9.47_alpha"){
        // excluding org.json which is provided by Android
        exclude (group = "io.grpc")
        exclude (group = "io.netty")
        exclude (group = "com.google.protobuf")
        exclude (group = "org.apache.commons.math")
    }
    implementation ("com.shimmerresearch:shimmerdriver:0.9.143_alpha"){
        // excluding org.json which is provided by Android
        exclude (group = "io.grpc")
        exclude (group = "io.netty")
        exclude (group = "com.google.protobuf")
        exclude (group = "org.apache.commons.math")
    }

}