# 多窗口使用文档

## 概述

White SDK Android 支持多窗口功能，允许在同一房间中同时显示和管理多个应用程序窗口，如幻灯片、媒体播放器、文档查看器等。

## 基本配置

### 1. 启用多窗口功能

在初始化 WhiteSdkConfiguration 时，必须设置 `setUseMultiViews(true)` 来启用多窗口功能：

```java
WhiteSdkConfiguration configuration = new WhiteSdkConfiguration(demoAPI.getAppId(), true);
configuration.setUseMultiViews(true);  // 启用多窗口功能
```

### 2. 创建 WhiteSdk 实例

```java
mWhiteSdk = new WhiteSdk(mWhiteboardView, this, configuration);
```

## 窗口管理

### 1. 使用新的动态PPT（推荐）

```java
// 使用任务UUID和前缀URL创建幻灯片应用
String prefixUrl = "https://convertcdn.netless.link/dynamicConvert";
String taskUuid = "47f359400ab1444986872db1723bb793";
WindowAppParam param = WindowAppParam.createSlideApp(taskUuid, prefixUrl, "Projector App");
mRoom.addApp(param, insertPromise);
```

### 2. 添加动态幻灯片窗口

使用 `WindowAppParam.createSlideApp()` 方法创建动态幻灯片应用：

```java
// 由转换后信息序列化的幻灯片数据
String ppts = "[{\"name\":\"1\",\"ppt\":{\"src\":\"pptx://convertcdn.netless.link/dynamicConvert/369ac28037d011ec99f08bddeae74404/1.slide\",\"width\":1280,\"height\":720,\"previewURL\":\"https://convertcdn.netless.link/dynamicConvert/369ac28037d011ec99f08bddeae74404/preview/1.png\"}}]";

// 解析幻灯片场景数据
Scene[] scenes = gson.fromJson(ppts, Scene[].class);

// 创建幻灯片应用参数
WindowAppParam param = WindowAppParam.createSlideApp("/dynamic003", scenes, "dynamic");

// 添加应用到房间
mRoom.addApp(param, insertPromise);
```

### 3. 添加媒体播放器窗口

```java
// 创建媒体播放器应用
WindowAppParam appParam = WindowAppParam.createMediaPlayerApp("https://example.com/video.mp4", "player");
mRoom.addApp(appParam, insertPromise);
```

### 4. 添加静态文档窗口

```java
// 创建静态文档查看器
WindowAppParam param = WindowAppParam.createDocsViewerApp("/static", scenes, "static");
mRoom.addApp(param, insertPromise);
```

## 窗口操作

### 1. 关闭窗口

```java
mRoom.closeApp("appId", new Promise<Boolean>() {
    @Override
    public void then(Boolean aBoolean) {
        // 窗口关闭成功
    }

    @Override
    public void catchEx(SDKError t) {
        // 窗口关闭失败
    }
});
```

### 2. 聚焦窗口

```java
mRoom.focusApp("appId");
```

### 3. 查询所有窗口

```java
mRoom.queryAllApps(new Promise<Map<String, WindowAppSyncAttrs>>() {
    @Override
    public void then(Map<String, WindowAppSyncAttrs> apps) {

    }

    @Override
    public void catchEx(SDKError t) {
        // 查询失败
    }
});
```

### 4. 查询单个窗口

```java
mRoom.queryApp(appId, new Promise<WindowAppSyncAttrs>() {
    @Override
    public void then(WindowAppSyncAttrs attrs) {
        // 获取单个应用信息
    }

    @Override
    public void catchEx(SDKError t) {
        // 查询失败
    }
});
```

## 窗口配置

### 1. 设置窗口比例

```java
// 循环切换不同的窗口比例
List<Float> ratios = Arrays.asList(1.0f, 16f/9, 9f/16);
mRoom.setContainerSizeRatio(ratios.get(index++ % ratios.size()));
```

### 2. 设置窗口主题

```java
// 切换窗口主题（暗色/亮色/自动）
List<WindowPrefersColorScheme> colorSchemes = Arrays.asList(
    WindowPrefersColorScheme.Dark,
    WindowPrefersColorScheme.Light,
    WindowPrefersColorScheme.Auto
);
mRoom.setPrefersColorScheme(colorSchemes.get(index++ % colorSchemes.size()));
```

### 3. 禁用窗口操作

```java
// 禁用窗口操作
mRoom.disableWindowOperation(true);
```

## 窗口状态管理

### 1. 保存窗口状态

```java
mRoom.getWindowManagerAttributes(new Promise<String>() {
    @Override
    public void then(String s) {
        // 保存窗口状态到文件
        File file = new File(getCacheDir(), "window_attributes.json");
        try {
            FileUtils.writeStringToFile(file, s);
            // 跳转到窗口恢复页面
            gotoWindowRestore();
        } catch (IOException e) {
            showToast("保存失败");
        }
    }

    @Override
    public void catchEx(SDKError t) {
        // 获取状态失败
    }
});
```

### 2. 监听窗口状态变化

```java
mWhiteSdk.joinRoom(roomParams, new RoomListener() {
    @Override
    public void onRoomStateChanged(RoomState roomState) {
        if (roomState.getWindowBoxState() != null) {
            // 处理窗口状态变化
            Log.i("WindowBoxState", roomState.getWindowBoxState().toString());
        }
    }
    
    // 其他回调方法...
});
```

## 幻灯片自定义链接

### 1. 设置自定义链接

```java
// 创建自定义链接
WhiteSlideCustomLink[] customLinks = new WhiteSlideCustomLink[]{
    new WhiteSlideCustomLink(1, "slide-9", "https://www.example.com?t=1"),
    new WhiteSlideCustomLink(1, "slide-2", "https://www.example.com?t=2"),
};

// 创建带自定义链接的幻灯片应用
WindowAppParam param = WindowAppParam.createSlideApp(taskUuid, prefixUrl, "Projector App", customLinks);
mRoom.addApp(param, insertPromise);
```

### 2. 监听链接点击事件

```java
mWhiteSdk.setSlideListener(new SlideListener() {
    @Override
    public void slideOpenUrl(String url) {
        runOnUiThread(() -> showToast("打开链接: " + url));
    }
    
    @Override
    public void onSlideError(SlideErrorType errorType, String errorMsg, String slideId, int slideIndex) {
        // 处理幻灯片错误
        switch (errorType) {
            case RESOURCE_ERROR:
            case RUNTIME_ERROR:
                // 资源错误或运行时错误
                break;
            case CANVAS_CRASH:
                // 画布崩溃，尝试恢复
                mWhiteSdk.recoverSlide(slideId);
                break;
            case RUNTIME_WARN:
                // 运行时警告
                break;
        }
    }
});
```

## 音量控制

### 1. 设置幻灯片音量

```java
// 循环切换不同的音量设置
List<Float> volumes = Arrays.asList(1.0f, 0f, 0.5f);
mWhiteSdk.updateSlideVolume(volumes.get(index++ % volumes.size()));
```

### 2. 获取当前音量

```java
mWhiteSdk.getSlideVolume(new Promise<Double>() {
    @Override
    public void then(Double volume) {
        showToast("当前音量: " + volume);
    }

    @Override
    public void catchEx(SDKError t) {
        // 获取音量失败
    }
});
```

## 完整示例

```java
// 1. 配置SDK
WhiteSdkConfiguration configuration = new WhiteSdkConfiguration(appId, true);
configuration.setUseMultiViews(true);
configuration.setEnableSlideInterrupterAPI(true);

// 2. 创建SDK实例
mWhiteSdk = new WhiteSdk(mWhiteboardView, this, configuration);

// 3. 设置幻灯片监听器
mWhiteSdk.setSlideListener(new SlideListener() {
    @Override
    public void slideOpenUrl(String url) {
        runOnUiThread(() -> showToast("打开链接: " + url));
    }
    
    @Override
    public void onSlideError(SlideErrorType errorType, String errorMsg, String slideId, int slideIndex) {
        // 处理错误
    }
});

// 4. 加入房间
RoomParams roomParams = new RoomParams(uuid, token, userId);
WindowParams windowParams = new WindowParams()
    .setContainerSizeRatio(3f/4)
    .setChessboard(true)
    .setDebug(true);
roomParams.setWindowParams(windowParams);

mWhiteSdk.joinRoom(roomParams, roomListener, roomPromise);

// 5. 添加应用窗口
String prefixUrl = "https://convertcdn.netless.link/dynamicConvert";
String taskUuid = "47f359400ab1444986872db1723bb793";
WindowAppParam param = WindowAppParam.createSlideApp(taskUuid, prefixUrl, "Projector App");
mRoom.addApp(param, insertPromise);
```

## 注意事项

1. **必须启用多窗口功能**：`configuration.setUseMultiViews(true)` 是使用多窗口功能的前提
2. **应用ID管理**：使用 `insertPromise` 来跟踪成功添加的应用ID，便于后续操作
3. **错误处理**：为所有异步操作提供错误处理回调
4. **内存管理**：在 Activity 销毁时正确清理资源
5. **线程安全**：UI 操作必须在主线程执行

## 相关类和接口

- `WhiteSdkConfiguration`: SDK配置类
- `WindowAppParam`: 窗口应用参数类
- `WindowParams`: 窗口参数类
- `RoomListener`: 房间监听器接口
- `Promise`: 异步操作结果处理接口