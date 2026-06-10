plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.hilt)
    alias(libs.plugins.ksp)
}

android {
    namespace = "com.mazhuo.airy"
    compileSdk {
        version = release(36) {
            minorApiLevel = 1
        }
    }

    defaultConfig {
        applicationId = "com.mazhuo.airy"
        minSdk = 26
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            optimization {
                enable = false
            }
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    buildFeatures {
        compose = true
    }
    packaging {
        resources {
            excludes.add("META-INF/DEPENDENCIES")
            excludes.add("META-INF/LICENSE")
            excludes.add("META-INF/LICENSE.txt")
            excludes.add("META-INF/license.txt")
            excludes.add("META-INF/NOTICE")
            excludes.add("META-INF/NOTICE.txt")
            excludes.add("META-INF/notice.txt")
            excludes.add("META-INF/LICENSE.md")
            excludes.add("META-INF/NOTICE.md")
            excludes.add("META-INF/ASL2.0")
            excludes.add("META-INF/INDEX.LIST")
            excludes.add("META-INF/gradle/incremental.annotation.processors")
        }
    }
}

dependencies {
    implementation(libs.androidx.compose.foundation)
    implementation(libs.androidx.compose.runtime)
    implementation(libs.androidx.compose.ui.text)
    ksp("org.jetbrains.kotlin:kotlin-metadata-jvm:2.4.0")
    annotationProcessor("org.jetbrains.kotlin:kotlin-metadata-jvm:2.4.0")
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.compose.material.icons.extended)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.viewmodel.ktx)
    implementation(libs.androidx.material3)
    implementation(libs.androidx.navigation.compose)
    implementation(libs.androidx.room.compiler)
    implementation(libs.androidx.room.ktx)
    implementation(libs.androidx.room.runtime)
    ksp(libs.androidx.room.compiler)
    implementation(libs.commons.net)
    implementation(libs.hilt.android)
    ksp(libs.hilt.compiler)
    implementation(libs.hilt.compiler)
    implementation(libs.hilt.navigation.compose)
    implementation(libs.jsch)
    implementation(libs.lifecycle.runtime.ktx)
    implementation(libs.sardine)
    implementation(libs.smbj)
    testImplementation(libs.junit)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(libs.androidx.junit)
    debugImplementation(libs.androidx.compose.ui.test.manifest)
    debugImplementation(libs.androidx.compose.ui.tooling)
}
configurations.all {
    // 全局排除掉引发冲突的老旧 IntelliJ 注解库
    exclude(group = "com.intellij", module = "annotations")
    resolutionStrategy {
        // 强制全局将新版元数据解析库提升到 2.4.0
        force("org.jetbrains.kotlin:kotlin-metadata-jvm:2.4.0")
    }

}