# whiteboard-android [![Release](https://jitpack.io/v/netless-io/whiteboard-android.svg)](https://jitpack.io/#netless-io/whiteboard-android)

whiteboard-android 是 Whiteboard SDK 在 Android 平台上的封装,提供了 Android 应用与 Whiteboard
服务端互动的接口。项目中的 sdk 目录包含了白板功能的接口实现代码，app 目录下是一个使用该 SDK 开发的示例
App。

[Jitpack 版本列表](https://jitpack.io/com/github/netless-io/whiteboard-android)

[English](./README.md) | 简体中文

## 示例运行

1. 申请 APP Identifier, 获取 SDK Token
   * 阅读 [项目及权限](https://developer.netless.link/document-zh/home/project-and-authority)
2. 配置 app 目录下 string_white_sdk_config.xml 中相应参数
   * sdk_app_id: 必填
   * room_uuid: 必填
   * room_token: 必填
   * sdk_app_token: 可选，用于本地创建房间。请妥善保管 sdk_app_token


## 项目集成

### 前提条件
1. Android Studio 4.0+
2. Android SDK API level 21+
3. App Identifier 和 SDK Token。

### 添加 Jitpack 仓库

打开根目录下的 build.gradle 进行如下标准配置：

```groovy
allprojects {
    repositories {
        // 添加以下内容
        maven { url 'https://jitpack.io' }
    }
}
```
### 添加 whiteboard 依赖

然后打开 app 目录下的 build.gradle 进行如下配置：

```groovy
dependencies {
    // 数字请根据最新版自行添加
    implementation "com.github.netless-io:whiteboard-android:2.16.92"
}
```

### Proguard 配置

```bash
# SDK model
-keep class com.herewhite.** { *; }
# Application classes that will be serialized/deserialized over Gson
```

### Releases
最新版本请查看 [版本历史](https://developer.netless.link/android-zh/home/android-changelog)
