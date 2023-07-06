# white sdk android [![Release](https://jitpack.io/v/netless-io/whiteboard-android.svg)](https://jitpack.io/#netless-io/whiteboard-android)

[外部版本列表](https://jitpack.io/com/github/netless-io/whiteboard-android)

# 示例运行

1. 阅读[项目及权限](https://developer.netless.link/document-zh/home/project-and-authority)，申请 APP Identifier 及 获取 SDK Token
2. 配置 app 目录下 string_white_sdk_config.xml 中相应参数

# 项目集成

## 前提条件
1. Android Studio 
2. Android SDK API level 21+
3. Agora 开发者账号。
4. 已在 Agora 控制台开启互动白板服务并获取项目的 App Identifier 和 SDK Token。

## 集成 SDK 到项目中

### 配置 build.gradle
打开根目录下的 build.gradle 进行如下标准配置：
```groovy
allprojects {
    repositories {
        // 添加以下内容
        maven { url 'https://jitpack.io' }
    }
}
```

然后打开 app 目录下的 build.gradle 进行如下配置：
```groovy
dependencies {
    // 数字请根据最新版自行添加
    implementation "com.github.netless-io:whiteboard-android:2.16.59"
}
```

最新版本请查看[版本历史](https://developer.netless.link/android-zh/home/android-changelog)

### Proguard 配置
```bash
# SDK model
-keep class com.herewhite.** { *; }
# Application classes that will be serialized/deserialized over Gson
```
