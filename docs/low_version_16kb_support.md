# 低版本 Whiteboard 支持 16KB 页说明文档

用户可以尝试一下三种方案之一

## 方案 1：升级依赖版本（推荐）

将 **whiteboard-android** 升级到 **2.16.61 及以后版本**：

```gradle
implementation 'com.github.netless-io:whiteboard-android:2.16.61'
```

> 建议优先采用此方案，以获得最新功能和修复。

## 方案 2：包中移除旧版本依赖

如果项目中仍存在旧版本 **2.16.41**，请在 **模块 build.gradle** 中移除并排除 `fpa`：

```gradle
implementation('com.github.netless-io:whiteboard-android:2.16.41') {
    exclude group: 'com.github.agorabuilder', module: 'fpa'
}
```

> 该操作避免旧版本依赖引入不必要的 `fpa` 模块。

## 方案 3：应用中全局排除 fpa

在 **app 模块 build.gradle** 中添加全局排除，确保整个应用中不再引入 `fpa`：

```gradle
configurations.all {
    exclude group: "com.github.agorabuilder", module: "fpa"
}
```

> 该方式可以防止任何传递依赖将 `fpa` 模块引入应用。

**备注：**

* 建议在升级 whiteboard-android 版本后，同时检查依赖树，确认 `fpa` 已完全移除。
* 可使用命令查看依赖树：

```bash
./gradlew app:dependencies | grep com.github.agorabuilder:fpa
```