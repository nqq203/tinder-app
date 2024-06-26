plugins {
    id("com.android.application")
    id("com.google.gms.google-services")
}

android {
    namespace = "com.group4.matchmingle"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.group4.matchmingle"
        minSdk = 28
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildFeatures{
        viewBinding = true
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }
    }
    buildFeatures{
        viewBinding = true
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
}

dependencies {

    implementation("androidx.appcompat:appcompat:1.6.1")
    implementation("com.google.android.material:material:1.11.0")
    implementation("androidx.constraintlayout:constraintlayout:2.1.4")


    implementation("com.google.firebase:firebase-database:20.3.1")

    implementation("androidx.activity:activity:1.8.0")

    implementation("androidx.activity:activity:1.8.0")
    implementation("com.google.firebase:firebase-database:20.3.1")


    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")
    implementation("it.xabaras.android:recyclerview-swipedecorator:1.4")

    implementation("com.github.bumptech.glide:glide:4.16.0")
    implementation("de.hdodenhof:circleimageview:3.1.0")
    implementation ("it.xabaras.android:recyclerview-swipedecorator:1.2.3")
    implementation ("androidx.recyclerview:recyclerview:1.2.1")
    implementation ("com.github.shts:StoriesProgressView:3.0.0")
    implementation ("com.squareup.picasso:picasso:2.71828")

//  Firebase config
    implementation(platform("com.google.firebase:firebase-bom:32.8.0"))
    implementation("com.google.firebase:firebase-analytics")
    implementation("com.google.firebase:firebase-database")
    implementation("com.google.firebase:firebase-auth")
    implementation("com.google.firebase:firebase-messaging-ktx:24.0.0")

    //Glide
    implementation ("com.github.bumptech.glide:glide:4.16.0")

    implementation("com.google.firebase:firebase-database")
    implementation("com.google.firebase:firebase-storage:20.0.0")

    
    //Glide
    implementation ("com.github.bumptech.glide:glide:4.16.0")

    //okhttp
    implementation("com.squareup.okhttp3:okhttp:4.12.0")
}
