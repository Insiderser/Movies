/*
 * Copyright 2019 Oleksandr Bezushko
 *
 * Permission is hereby granted, free of charge, to any person
 * obtaining a copy of this software and associated documentation
 * files (the "Software"), to deal in the Software without restriction,
 * including without limitation the rights to use, copy, modify,
 * merge, publish, distribute, sublicense, and/or sell copies of
 * the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be
 * included in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
 * EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF
 * MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT.
 * IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY
 * CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT,
 * TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE
 * SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

import org.jetbrains.kotlin.config.KotlinCompilerVersion
import java.util.Properties

plugins {
    id("com.android.application")
    kotlin("android")
    kotlin("android.extensions")
    kotlin("kapt")
}

fun getTmdbApiKey(): String {
    val properties = Properties()
    properties.load(project.rootProject.file("keys.properties").inputStream())
    return properties.getProperty("tmdbApiKey") ?: "null"
}

val javaVersion = JavaVersion.VERSION_1_8

android {
    compileSdkVersion(29)
    defaultConfig {
        applicationId = "com.insiderser.android.movies"
        minSdkVersion(19)
        targetSdkVersion(29)
        versionCode = 1
        versionName = "1.0"
        vectorDrawables.useSupportLibrary = true

        buildConfigField("String", "TMDB_API_KEY", getTmdbApiKey())
    }

    dataBinding.isEnabled = true

    compileOptions {
        sourceCompatibility = javaVersion
        targetCompatibility = javaVersion
    }

    kotlinOptions.apply {
        jvmTarget = javaVersion.toString()
    }
}

dependencies {
    implementation(fileTree(mapOf("dir" to "libs", "include" to listOf("*.jar"))))

    implementation(kotlin("stdlib-jdk8", KotlinCompilerVersion.VERSION))
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.3.2")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.3.2")

    implementation("androidx.appcompat:appcompat:1.1.0")
    implementation("androidx.constraintlayout:constraintlayout:1.1.3")
    implementation("androidx.recyclerview:recyclerview:1.0.0")
    implementation("androidx.preference:preference:1.1.0")
    implementation("com.google.android.material:material:1.0.0")
    implementation("androidx.lifecycle:lifecycle-extensions:2.1.0")
    implementation("androidx.room:room-runtime:2.2.1")
    kapt("androidx.room:room-compiler:2.2.1")
    implementation("androidx.legacy:legacy-support-v4:1.0.0")
    implementation("androidx.work:work-runtime-ktx:2.2.0")
    implementation("com.android.support:customtabs:29.0.0")

    implementation("com.github.arimorty:floatingsearchview:2.1.1")

    implementation("com.github.stfalcon:stfalcon-imageviewer:0.1.0")

    implementation("com.github.bumptech.glide:glide:4.10.0") {
        exclude(group = "com.android.support")
    }
    kapt("com.github.bumptech.glide:compiler:4.10.0")

    implementation("com.google.code.gson:gson:2.8.6")
    implementation("com.google.dagger:dagger:2.25.2")
    kapt("com.google.dagger:dagger-compiler:2.25.2")

    implementation("com.squareup.retrofit2:retrofit:2.6.1")
    implementation("com.squareup.retrofit2:converter-gson:2.6.1")
    debugImplementation("com.squareup.leakcanary:leakcanary-android:2.0-beta-3")
}
