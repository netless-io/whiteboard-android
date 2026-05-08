# 多窗口使用文档

## 简介

`whiteboard-android` 在多窗口模式下内置了 `window-manager` 能力。Android 接入层不需要直接调用 Web 端的 `WindowManager.mount()`，而是通过 `WhiteSdkConfiguration`、`RoomParams` 和 `Room` 提供的 API 使用对应能力。

这份文档面向 Android Native 接入方，重点说明：

- 如何启用多窗口能力
- 如何配置 `WindowParams`
- 如何插入、关闭、聚焦和查询窗口
- 如何调整窗口样式和恢复窗口状态
- 如何控制当前聚焦的文档窗口

## 接入前提

使用多窗口相关 API 前，需要先在 SDK 配置中开启：

```java
WhiteSdkConfiguration configuration = new WhiteSdkConfiguration(appIdentifier, true);
configuration.setUseMultiViews(true);
```

如果没有开启 `setUseMultiViews(true)`，窗口相关接口不会按多窗口语义工作。

## 初始化配置

### 实时房间

```java
WhiteSdkConfiguration configuration = new WhiteSdkConfiguration(appIdentifier, true);
configuration.setUseMultiViews(true);

WhiteSdk whiteSdk = new WhiteSdk(whiteBoardView, context, configuration);

RoomParams roomParams = new RoomParams(roomUuid, roomToken, userId);

WindowParams windowParams = new WindowParams()
        .setContainerSizeRatio(9f / 16f)
        .setChessboard(true)
        .setFullscreen(false)
        .setDebug(false);
windowParams.setPrefersColorScheme(WindowPrefersColorScheme.Light);

roomParams.setWindowParams(windowParams);

whiteSdk.joinRoom(roomParams, new RoomCallbacks() {
}, new Promise<Room>() {
    @Override
    public void then(Room room) {
        mRoom = room;
    }

    @Override
    public void catchEx(SDKError error) {
    }
});
```

### `WindowParams` 常用字段

`WindowParams` 是多窗口模式下的本地显示参数，只影响当前客户端。

- `containerSizeRatio`：多窗口区域的高宽比，建议多端保持一致。
- `chessboard`：多窗口区域之外是否显示棋盘背景。
- `prefersColorScheme`：窗口主题，可选 `Dark`、`Light`、`Auto`。
- `fullscreen`：是否默认以最大化窗口方式展示。
- `collectorStyles`：最小化图标区域样式，字段为驼峰形式 CSS。
- `overwriteStyles`：覆盖默认窗口样式。
- `debug`：是否输出多窗口调试日志。
- `polling`：是否轮询更新本地视角。

## 核心窗口操作

### 插入窗口

`WindowAppParam` 是 Android 侧对 `window-manager addApp` 的封装。常见内置窗口包括动态 PPT、静态文档和媒体播放器。

#### 插入动态 PPT

如果你拿到的是动态转换任务结果，推荐直接使用 `taskUuid + prefixUrl` 的方式：

```java
String taskUuid = "47f359400ab144498687xxxxxxxxxxxx";
String prefixUrl = "https://convertcdn.netless.link/dynamicConvert";

WindowAppParam appParam = WindowAppParam.createSlideApp(taskUuid, prefixUrl, "Projector App");
mRoom.addApp(appParam, new Promise<String>() {
    @Override
    public void then(String appId) {
    }

    @Override
    public void catchEx(SDKError error) {
    }
});
```

如果你已经有场景数据，也可以使用 `scenePath + scenes` 的方式：

```java
WindowAppParam appParam = WindowAppParam.createSlideApp("/dynamic", scenes, "Dynamic Slide");
mRoom.addApp(appParam, new Promise<String>() {
    @Override
    public void then(String appId) {
    }

    @Override
    public void catchEx(SDKError error) {
    }
});
```

#### 插入静态文档

```java
WindowAppParam appParam = WindowAppParam.createDocsViewerApp("/docs-viewer", scenes, "Static Docs");
mRoom.addApp(appParam, new Promise<String>() {
    @Override
    public void then(String appId) {
    }

    @Override
    public void catchEx(SDKError error) {
    }
});
```

#### 插入媒体播放器

```java
WindowAppParam appParam = WindowAppParam.createMediaPlayerApp(
        "https://example.com/video.mp4",
        "Media Player"
);
mRoom.addApp(appParam, new Promise<String>() {
    @Override
    public void then(String appId) {
    }

    @Override
    public void catchEx(SDKError error) {
    }
});
```

### 关闭窗口

```java
mRoom.closeApp(appId, new Promise<Boolean>() {
    @Override
    public void then(Boolean value) {
    }

    @Override
    public void catchEx(SDKError error) {
    }
});
```

### 聚焦窗口

```java
mRoom.focusApp(appId);
```

### 查询单个窗口

```java
mRoom.queryApp(appId, new Promise<WindowAppSyncAttrs>() {
    @Override
    public void then(WindowAppSyncAttrs attrs) {
    }

    @Override
    public void catchEx(SDKError error) {
    }
});
```

### 查询所有窗口

```java
mRoom.queryAllApps(new Promise<Map<String, WindowAppSyncAttrs>>() {
    @Override
    public void then(Map<String, WindowAppSyncAttrs> apps) {
    }

    @Override
    public void catchEx(SDKError error) {
    }
});
```

## 窗口样式与状态

### 调整多窗口显示比例

```java
mRoom.setContainerSizeRatio(3f / 4f);
```

### 切换窗口主题

```java
mRoom.setPrefersColorScheme(WindowPrefersColorScheme.Dark);
```

### 禁止窗口操作

```java
mRoom.disableWindowOperation(true);
```

### 读取当前 WindowManager attributes

```java
mRoom.getWindowManagerAttributes(new Promise<String>() {
    @Override
    public void then(String attributes) {
    }

    @Override
    public void catchEx(SDKError error) {
    }
});
```

Android 侧 `attributes` 的类型是 `String`，内容本质上是一段 JSON 字符串。

### 恢复当前 WindowManager attributes

```java
mRoom.setWindowManagerAttributes(attributesJson);
```

更推荐的做法是直接保存 `getWindowManagerAttributes()` 返回的原始 JSON，再在需要时整体写回，而不是手动拼装内部字段。

## 文档窗口控制

`dispatchDocsEvent` 用于操作当前聚焦的文档窗口。调用前请确保文档窗口已经创建并完成加载。

### 上一页 / 下一页

```java
mRoom.dispatchDocsEvent(WindowDocsEvent.PrevPage, new Promise<Boolean>() {
    @Override
    public void then(Boolean success) {
    }

    @Override
    public void catchEx(SDKError error) {
    }
});

mRoom.dispatchDocsEvent(WindowDocsEvent.NextPage, new Promise<Boolean>() {
    @Override
    public void then(Boolean success) {
    }

    @Override
    public void catchEx(SDKError error) {
    }
});
```

### 上一步 / 下一步

```java
mRoom.dispatchDocsEvent(WindowDocsEvent.PrevStep, new Promise<Boolean>() {
    @Override
    public void then(Boolean success) {
    }

    @Override
    public void catchEx(SDKError error) {
    }
});

mRoom.dispatchDocsEvent(WindowDocsEvent.NextStep, new Promise<Boolean>() {
    @Override
    public void then(Boolean success) {
    }

    @Override
    public void catchEx(SDKError error) {
    }
});
```

### 跳转到指定页

```java
mRoom.dispatchDocsEvent(WindowDocsEvent.JumpToPage(3), new Promise<Boolean>() {
    @Override
    public void then(Boolean success) {
    }

    @Override
    public void catchEx(SDKError error) {
    }
});
```

## 注意事项

1. `WhiteSdkConfiguration.setUseMultiViews(true)` 是所有窗口能力的前置条件。
2. `WindowParams` 只影响当前客户端的本地显示，不会直接同步到远端。
3. `containerSizeRatio` 建议多端统一配置，否则同房间展示区域可能不一致。
4. `setWindowManagerAttributes(String)` 接收的是 JSON 字符串，推荐只写回通过 `getWindowManagerAttributes()` 得到的快照。
5. 文档事件会作用在当前聚焦的文档窗口上，调用前要确保该窗口已经完成加载。
6. `disableWindowOperation(true)` 是本地交互限制，不等价于修改房间整体读写状态。
- `WindowParams`: 窗口参数类
- `RoomListener`: 房间监听器接口
- `Promise`: 异步操作结果处理接口
