plugins {
    alias(libs.plugins.android.application)
}

android {
    namespace = "com.example.weather"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.example.weather"
        minSdk = 30
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        // 添加百度地图配置
        ndk {
            abiFilters.addAll(listOf("armeabi", "armeabi-v7a", "arm64-v8a", "x86", "x86_64"))
        }
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
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }

    // 配置百度SDK路径
    sourceSets {
        named("main") {
            jniLibs.srcDirs("libs")
        }
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

    // 网络请求
    implementation("com.squareup.okhttp3:okhttp:4.9.3")
    implementation("com.google.code.gson:gson:2.10.1")

    // 权限处理 - 推荐添加
    implementation("com.guolindev.permissionx:permissionx:1.7.1")

    // RecyclerView
    implementation("androidx.recyclerview:recyclerview:1.3.2")

    // 百度地图SDK
    implementation(fileTree(mapOf("dir" to "libs", "include" to listOf("*.jar"))))

    // 百度地图SDK
    implementation(files("libs/BaiduLBS_Android.jar"))

    // 添加 Material Design 图标库
    implementation("androidx.compose.material:material-icons-extended:1.6.0")

    // 添加 Glide 图片加载库用于背景切换
    implementation("com.github.bumptech.glide:glide:4.16.0")
    annotationProcessor("com.github.bumptech.glide:compiler:4.16.0")

    // 添加音乐播放相关依赖
    implementation("androidx.media:media:1.6.0")
    implementation("com.google.android.exoplayer:exoplayer:2.18.1")

    // 添加 ExoPlayer 用于更好的网络音频播放
    implementation("com.google.android.exoplayer:exoplayer-core:2.18.1")
    implementation("com.google.android.exoplayer:exoplayer-ui:2.18.1")

    // 添加权限处理
    implementation("com.guolindev.permissionx:permissionx:1.7.1")
}