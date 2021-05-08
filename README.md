# white sdk android [![Release](https://jitpack.io/v/netless-io/whiteboard-android.svg)](https://jitpack.io/#netless-io/whiteboard-android)

[last release](https://github.com/netless-io/whiteboard-android/releases/latest)

[jitpack release](https://jitpack.io/com/github/netless-io/whiteboard-android/)

## Run Demo
1. Read [Projects and permissions](https://developer.netless.link/document-en/home/project-and-authority)，Gain **APP Identifier** and **SDK Token**
2. Config **app/string_white_sdk_config.xml**

## Integrate the SDK
### Prerequisites
1. Android Studio 
2. API 19+
3. A valid **APP Identifier** and **SDK Token**

### Config build.gradle
Add the following line in the **build.gradle** file of your project:
```groovy
allprojects {
    repositories {
        // Add jitpack repository
        maven { url 'https://jitpack.io' }
    }
}
```

add dependency of whiteboard in the **app/build.gradle** file

```groovy
dependencies {
    // Get the latest version number through the release notes.
    implementation 'com.github.netless-io:whiteboard-android:$last-version'
}
```
Our [change log](https://developer.netless.link/android-en/home/android-changelog) has release history.

The latest release is available on [jitpack](https://jitpack.io/v/netless-io/whiteboard-android)

### Config Proguard
Add the following line in the **app/proguard-rules.pro** file to prevent obfuscating the code
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