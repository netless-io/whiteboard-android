# whiteboard-android [![Release](https://jitpack.io/v/netless-io/whiteboard-android.svg)](https://jitpack.io/#netless-io/whiteboard-android)

whiteboard-android is an Android package of the Whiteboard SDK, providing interfaces for Android apps to interact with the Whiteboard service. The sdk directory contains interface implementation code for whiteboard functions, while the app directory includes a sample app developed using this SDK.

[Jitpack Release](https://jitpack.io/com/github/netless-io/whiteboard-android/)

English | [简体中文](./README-zh_CN.md)

## Run Demo

1. Get **APP Identifier** and **SDK Token**
    * Read [Projects and permissions](https://developer.netless.link/document-en/home/project-and-authority)
2. Config **app/string_white_sdk_config.xml**
   * sdk_app_id: Required
   * room_uuid: Required
   * room_token: Required
   * sdk_app_token: Optional, used for creating room locally. Please keep the sdk_app_token safe.

## Integrate SDK

### Requirements
1. Android Studio 4.0+
2. API 21+
3. **APP Identifier** and **SDK Token**

### Add jitpack repository

Add the following line in the **build.gradle** file of your project:

```groovy
allprojects {
    repositories {
        // Add jitpack repository
        maven { url 'https://jitpack.io' }
    }
}
```
### Add whiteboard dependency

add dependency of whiteboard in the **app/build.gradle** file

```groovy
dependencies {
    // Get the latest version number through the release notes.
    implementation "com.github.netless-io:whiteboard-android:2.16.71"
}
```

### Config Proguard

Add the following line in the **app/proguard-rules.pro** file to prevent obfuscating the code

```bash
# SDK model
-keep class com.herewhite.** { *; }
# Application classes that will be serialized/deserialized over Gson
```

### Releases

Our [change log](https://developer.netless.link/android-en/home/android-changelog) has release
history.

The latest release is available on [jitpack](https://jitpack.io/v/netless-io/whiteboard-android)