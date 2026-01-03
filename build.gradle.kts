// Top-level build file where you can add configuration options common to all sub-projects/modules.

buildscript {
    repositories {
        google()
        mavenCentral()
    }
    dependencies {
        // Firebase 또는 Google 서비스를 사용할 때 필요한 클래스패스
        classpath("com.google.gms:google-services:4.4.2")  // 최신 버전으로 대체 가능
    }
}

plugins {
    // Android 애플리케이션 플러그인 설정 (공통으로 사용)
    alias(libs.plugins.android.application) apply false

    // Firebase Google Services 플러그인 설정
    id("com.google.gms.google-services") version "4.4.2" apply false
}
