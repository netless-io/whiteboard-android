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
        .setUseBoxesStatus(false)
        .setOriginSize(new WindowOriginSize(1280, 900))
        .setPageScaleRange(new PageScaleRange().setMinScale(0.5).setMaxScale(4.0))
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

`WindowParams` 主要是多窗口模式下的本地显示参数；`originSize` 是例外，可写端首次设置或改值时会重置并同步 MainView camera-size contract。

- `containerSizeRatio`：多窗口区域的高宽比，建议多端保持一致。
- `chessboard`：多窗口区域之外是否显示棋盘背景。
- `prefersColorScheme`：窗口主题，可选 `Dark`、`Light`、`Auto`。
- `fullscreen`：是否默认以最大化窗口方式展示。
- `collectorStyles`：最小化图标区域样式，字段为驼峰形式 CSS。
- `overwriteStyles`：覆盖默认窗口样式。
- `debug`：是否输出多窗口调试日志。
- `polling`：是否轮询更新本地视角。
- `useBoxesStatus`：是否使用每个窗口独立的状态管理。开启后窗口最大化、最小化状态会按窗口分别同步；同一房间内多端建议保持一致。回放带窗口房间时也需要在 `PlayerConfiguration.windowParams` 中设置同样的值。
- `originSize`：MainView 的归一化参考尺寸。可写端首次设置或传入不同尺寸时，WindowManager 会重置并同步 MainView 的 origin/active camera-size contract；不会隐式改写 Slide/Presentation App 参数。
- `pageScaleRange`：`scalePage` 的可选相对倍率范围。`minScale`、`maxScale` 均可省略；未配置时不施加业务范围限制。

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

`dispatchDocsEvent` 统一控制 MainView、DocsViewer、Slide 和 Presentation。`target` 可传 `"mainView"` 或具体 appId；省略时使用当前聚焦 App，没有聚焦 App 时回退 MainView。返回对象只表示命令是否被接受，实际状态以 `UnifiedPageStateListener` 为准。

### 上一页 / 下一页

```java
mRoom.dispatchDocsEvent(WindowDocsEvent.PrevPage, new Promise<DispatchDocsEventResult>() {
    @Override
    public void then(DispatchDocsEventResult result) {
        Log.d("page", "prev accepted=" + result.isAccepted());
    }

    @Override
    public void catchEx(SDKError error) {
    }
});

mRoom.dispatchDocsEvent(WindowDocsEvent.NextPage.withTarget("mainView"),
        new Promise<DispatchDocsEventResult>() {
    @Override
    public void then(DispatchDocsEventResult result) {
        Log.d("page", "next accepted=" + result.isAccepted());
    }

    @Override
    public void catchEx(SDKError error) {
    }
});
```

### 上一步 / 下一步

```java
mRoom.dispatchDocsEvent(WindowDocsEvent.PrevStep, new Promise<DispatchDocsEventResult>() {
    @Override
    public void then(DispatchDocsEventResult result) {
    }

    @Override
    public void catchEx(SDKError error) {
    }
});

mRoom.dispatchDocsEvent(WindowDocsEvent.NextStep, new Promise<DispatchDocsEventResult>() {
    @Override
    public void then(DispatchDocsEventResult result) {
    }

    @Override
    public void catchEx(SDKError error) {
    }
});
```

### 跳转到指定页

```java
mRoom.dispatchDocsEvent(WindowDocsEvent.JumpToPage(3).withTarget(appId),
        new Promise<DispatchDocsEventResult>() {
    @Override
    public void then(DispatchDocsEventResult result) {
    }

    @Override
    public void catchEx(SDKError error) {
    }
});
```

`page` 为 1-based。`prevStep/nextStep` 在 Slide 中表示动画步骤，在 DocsViewer 中沿用翻页 alias；Presentation 和 MainView 返回 `eventNotSupported`。

### 缩放页面

```java
mRoom.dispatchDocsEvent(WindowDocsEvent.ScalePage(1.5).withTarget("mainView"),
        new Promise<DispatchDocsEventResult>() {
    @Override
    public void then(DispatchDocsEventResult result) {
        if (!result.isAccepted()) {
            Log.w("page", result.getReason() + ": " + result.getMessage());
        }
    }

    @Override
    public void catchEx(SDKError error) {
    }
});
```

`scale` 是相对于适配尺寸的倍率，`1` 表示适配尺寸，不是底层 `view.camera.scale`。DocsViewer 不支持缩放，固定返回 `eventNotSupported` 和原因 `DocsViewer does not support scalePage`。

### 查询与监听状态

```java
mRoom.getPageState(new WindowPageStateOptions().withTarget(appId),
        new Promise<UnifiedPageState>() {
    @Override
    public void then(UnifiedPageState state) {
        Log.d("page", state.getPage() + "/" + state.getPageCount());
    }

    @Override
    public void catchEx(SDKError error) {
    }
});

whiteSdk.setUnifiedPageStateListener(new UnifiedPageStateListener() {
    @Override
    public void onUnifiedPageStateChange(UnifiedPageStateChange state) {
        Log.d("page", state.getTarget() + ": " + state.getStatus());
    }
});
```

## 注意事项

1. `WhiteSdkConfiguration.setUseMultiViews(true)` 是所有窗口能力的前置条件。
2. `WindowParams` 主要影响当前客户端的本地显示；实时房间可写端的 `originSize` 会按约定重置并同步 MainView camera-size contract。
3. `containerSizeRatio` 建议多端统一配置，否则同房间展示区域可能不一致。
4. `setWindowManagerAttributes(String)` 接收的是 JSON 字符串，推荐只写回通过 `getWindowManagerAttributes()` 得到的快照。
5. `dispatchDocsEvent` 可通过 `target` 控制 MainView 或指定文档 App；省略时才跟随焦点，调用前要确保目标 App 已完成加载。
6. `disableWindowOperation(true)` 是本地交互限制，不等价于修改房间整体读写状态。
- `WindowParams`: 窗口参数类
- `RoomListener`: 房间监听器接口
- `Promise`: 异步操作结果处理接口
