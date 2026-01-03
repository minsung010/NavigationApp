plugins {
    id("com.android.application") // Android 플러그인 추가
    id("com.google.gms.google-services") // Firebase Google Services 플러그인
}

android {
    namespace = "com.example.gpss"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.example.gpss"
        minSdk = 24 // 이 부분을 24로 설정하였으나, 두 번째 파일에서는 34로 되어있습니다. 여기에 맞춰 설정이 필요합니다.
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
    // Firebase BOM 사용 (버전 관리를 위해)
    implementation(platform("com.google.firebase:firebase-bom:33.4.0"))

    // Firebase 관련 라이브러리
    implementation("com.google.firebase:firebase-analytics")
    implementation("com.google.firebase:firebase-database")
    implementation("com.google.firebase:firebase-messaging") // 추가된 부분

    // Google Play 서비스 관련 라이브러리
    implementation("com.google.android.gms:play-services-location:21.0.1") // 최신 버전으로 변경
    implementation("com.google.android.gms:play-services-maps:18.1.0")
    implementation("com.squareup.retrofit2:retrofit:2.9.0")
    implementation("com.squareup.retrofit2:converter-gson:2.9.0")
    implementation("com.google.firebase:firebase-messaging:23.1.1")


    // Android UI 관련 라이브러리
    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.activity)
    implementation(libs.constraintlayout)

    // 테스트 라이브러리
    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)
}
