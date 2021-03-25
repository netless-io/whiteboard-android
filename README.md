# white sdk android [![Release](https://jitpack.io/v/netless-io/white-sdk-android.svg)](http://developer.herewhite.com)

[最新release](https://github.com/netless-io/whiteboard-android/releases/latest)
[外部版本列表](https://jitpack.io/com/github/netless-io/whiteboard-android/)

# Demo运行
1. 阅读[项目及权限](https://developer.netless.link/document-zh/home/project-and-authority)，申请 APP Identifier 及 获取 SDK Token
2. 配置 app 目录下 string_white_sdk_config.xml 中相应参数


# 项目集成

## 前提条件
1. Android Studio 
2. API 19+

## 获取所需要的信息
1. 阅读 应用与权限，获取 SDK Token
2. 阅读 APP Identifier，获取 App Identifier

## 集成 SDK 到项目中

### 配置 build.gradle
打开根目录下的 build.gradle 进行如下标准配置：
```groovy
allprojects {
    repositories {
        jcenter()
        // 添加以下内容
        maven { url 'https://jitpack.io' }
    }
}
```

然后打开 app 目录下的 build.gradle 进行如下配置：
```groovy
dependencies {
    // 数字请根据最新版自行添加
    implementation 'com.github.netless-io:whiteboard-android:2.12.6'
}
```

> 最新版本请查看[版本历史](https://developer.netless.link/android-zh/home/android-changelog)

这时你会看到 Android Studio 在编辑器的顶部有一行提示
```sh
gradle files have changed since last project sync. a project sync may be necessary for the IDE to work properly
```
点击 Sync Now 按钮后提示变为 Gradle project sync in process... ，稍等一段时间（依你的网络环境而定）后提示消失，则集成完毕。

### Proguard 配置
```bash
# SDK model
-keep class com.herewhite.** { *; }
-keepattributes  *JavascriptInterface*
-keepattributes Signature
# Gson specific classes
-keep class sun.misc.Unsafe { *; }
-keep class com.google.gson.stream.** { *; }
# Application classes that will be serialized/deserialized over Gson
-keep class com.google.gson.examples.android.model.** { *; }
-keep class com.google.gson.** { *;}
```