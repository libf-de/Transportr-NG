/*
 *    Transportr
 *
 *    Copyright (c) 2013 - 2024 Torsten Grote
 *
 *    This program is Free Software: you can redistribute it and/or modify
 *    it under the terms of the GNU General Public License as
 *    published by the Free Software Foundation, either version 3 of the
 *    License, or (at your option) any later version.
 *
 *    This program is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *    GNU General Public License for more details.
 *
 *    You should have received a copy of the GNU General Public License
 *    along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

// build.gradle.kts
plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.parcelize)
    alias(libs.plugins.compose.compiler)
    alias(libs.plugins.kotlin.serialization)
    //kotlin("kapt")
    //id("witness")
}

//apply(from = "witness.gradle")

android {
    buildFeatures {
        viewBinding = true
        compose = true
    }

    defaultConfig {
        versionCode = 125
        versionName = "2.2.1"

        applicationId = "de.grobox.liberario"
        minSdk = 21
        compileSdk = 34
        targetSdk = 34

        testInstrumentationRunner = "de.grobox.transportr.MockTestRunner"
        javaCompileOptions {
            annotationProcessorOptions {
                arguments += mapOf("room.schemaLocation" to "$projectDir/schemas")
            }
        }
    }

    buildTypes {
        release {
            resValue("string", "app_name", "Transportr")
            isShrinkResources = true
            isMinifyEnabled = true
            proguardFiles(getDefaultProguardFile("proguard-android.txt"), "proguard-rules.txt")
        }

        debug {
            applicationIdSuffix = ".debug"
            versionNameSuffix = "-debug"
            resValue("string", "app_name", "Transportr Devel")
            isShrinkResources = false
            isMinifyEnabled = false
            proguardFiles(getDefaultProguardFile("proguard-android.txt"), "proguard-rules.txt")
            testProguardFiles(getDefaultProguardFile("proguard-android.txt"), "proguard-rules.txt", "proguard-test.txt")

            lint {
                disable += "ProtectedPermissions"
            }
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    dexOptions {
        preDexLibraries = !project.hasProperty("buildServer")
        javaMaxHeapSize = "1g"
    }

    sourceSets {
        getByName("androidTest") {
            assets.srcDirs(files("$projectDir/schemas"))
        }
    }

    namespace = "de.grobox.transportr"

    lint {
        checkReleaseBuilds = false
        disable += listOf("MissingTranslation", "InvalidPackage", "VectorDrawableCompat", "TrustAllX509TrustManager", "GradleDependency", "IconDensities", "PrivateResource")
    }

    if (!project.hasProperty("buildServer") &&
        project.hasProperty("signingStoreLocation") &&
        project.hasProperty("signingStorePassword") &&
        project.hasProperty("signingKeyAlias") &&
        project.hasProperty("signingKeyPassword")
    ) {
        signingConfigs {
            create("release") {
                storeFile = file(project.property("signingStoreLocation") as String)
                storePassword = project.property("signingStorePassword") as String
                keyAlias = project.property("signingKeyAlias") as String
                keyPassword = project.property("signingKeyPassword") as String
            }
        }
        buildTypes.getByName("release").signingConfig = signingConfigs.getByName("release")
        buildTypes.getByName("debug").signingConfig = signingConfigs.getByName("release")
    } else {
        buildTypes.getByName("release").signingConfig = null
    }
}

dependencies {
    implementation(libs.kotlin.stdlib)

    implementation(platform(libs.koin.bom))
    implementation(libs.koin.core)
    implementation(libs.koin.android)
    implementation(libs.koin.compose)

    // Java Compatibility
    implementation("io.insert-koin:koin-android-compat")

    implementation(libs.material)
    implementation(libs.androidx.appcompat)
    implementation(libs.androidx.activity)
    implementation(libs.androidx.fragment) {
        because("issues with MapBox SDK onDestroy after upgrading to 1.2.0-alpha02 or newer versions")
    }
    implementation(libs.androidx.preference) {
        because("newer version requires fragment v1.2.4")
    }
    implementation(libs.androidx.cardview)
    implementation(libs.androidx.recyclerview)
    implementation(libs.androidx.legacy.preference)
    implementation(libs.androidx.constraintlayout)
    implementation(libs.androidx.lifecycle.viewmodel)
//    implementation(libs.androidx.lifecycle.livedata)
    //kapt(libs.androidx.lifecycle.compiler)
    implementation(libs.androidx.room.runtime)
    implementation(libs.androidx.room.ktx)
    //kapt(libs.androidx.room.compiler)
    implementation(libs.androidx.localbroadcastmanager)

    implementation(libs.okhttp)
    implementation(libs.swipy)
//    implementation(libs.ckchangelog)
    implementation(libs.flexbox)
    implementation(libs.aboutlibraries)
    implementation(libs.fastadapter)
    implementation(libs.fastadapter.commons)
    implementation(libs.fastadapter.expandable)
    implementation(libs.material.tap.target.prompt)
    implementation(libs.maplibre)
    implementation(libs.maplibre.annotation)
    implementation(libs.timber)

    val composeBom = platform(libs.androidx.compose)
    implementation(composeBom)
    androidTestImplementation(composeBom)
    implementation(libs.compose.material3)
    implementation(libs.compose.preview)
    implementation(libs.compose.navigation)
    //debugImplementation(libs.compose.preview.debug)

    implementation(libs.compose.activities)
    implementation(libs.compose.viewmodel)
//    implementation(libs.compose.livedata)
    implementation(libs.compose.ui)
    implementation(libs.compose.icons)

    implementation(libs.composeSettings.ui)
    implementation(libs.composeSettings.ui.extended)

    //implementation(libs.dagger)
    //kapt(libs.dagger.compiler)

    implementation(libs.guava) {
        exclude(module = "failureaccess")
        exclude(group = "com.google.j2objc")
    }
//    implementation(libs.public.transport.enabler) {
//        exclude(group = "com.google.guava")
//        exclude(group = "org.json", module = "json")
//        exclude(group = "net.sf.kxml", module = "kxml2")
//    }
    implementation("de.schildbach.pte:public-transport-enabler-ktx-jvm:unspecified")
    debugImplementation(libs.slf4j)
    debugImplementation(libs.logback.android)

    testImplementation(libs.junit)
    testImplementation(libs.mockito)
    testImplementation(libs.androidx.arch.core.testing)

    androidTestImplementation(libs.androidx.test.core)
    androidTestImplementation(libs.androidx.arch.core.testing)
    androidTestImplementation(libs.androidx.test.rules)
    androidTestImplementation(libs.androidx.test.ext.junit)
    androidTestImplementation(libs.androidx.test.espresso.core)
    androidTestImplementation(libs.androidx.test.espresso.contrib)
    androidTestImplementation(libs.screengrab)
    //kaptAndroidTest(libs.dagger.compiler)

    implementation(libs.androidx.core.splashscreen)
    implementation(libs.kotlinx.serialization.json)
    implementation(libs.kotlinx.datetime)

    implementation(libs.settings)
    implementation(libs.settings.coroutines)
    //api(libs.moko.geo)
}
