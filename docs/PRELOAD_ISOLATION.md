# 显式预加载与普通入房

本地验证使用 Bridge 的 `file:` SDK 2.16.63（包含 Akko 849e007c）及 npm appliance-plugin 1.1.49。尚未发布新版 Android SDK。

## 普通入房

```java
WhiteSdkConfiguration config = new WhiteSdkConfiguration(appIdentifier);
config.setUseMultiViews(true);
config.setEnableAppliancePlugin(true); // 启用教具功能，默认仍在入房后初始化。
WhiteSdk sdk = new WhiteSdk(view, context, config);
sdk.joinRoom(params, callbacks, promise);
```

不设置 `enableModelPreload`、`dynamicConfig`、非空 `preloadBuiltinApps` 或 `enablePreLoadAppliancePlugin`，就不启动相应准备任务，也不发送 Native `sdk.observePreload`。`[]` 同样关闭 App 类准备。SDK 构造阶段原有 regions 请求继续保留。

普通顺序：基础 JS SDK → SDK 原入房 → Room → WindowManager → 按当前 Room 参数解析 Worker 资源 → 教具原初始化。Bridge 不再调用 SDK 私有 `prepareRoomResources`、`prepareDynamicConfig`，也不加入准备任务总等待。

## 候课页 / 入房页复用

```java
// 候课页：保留已附着的 WhiteboardView 和同一个 WhiteSdk。
WhiteSdkConfiguration config = new WhiteSdkConfiguration(appIdentifier);
config.setUseMultiViews(true);
config.setEnableAppliancePlugin(true);
config.setEnablePreLoadAppliancePlugin(true); // 显式前置教具探针和必要 Worker。
config.setPreloadBuiltinApps(Arrays.asList("Slide", "Presentation")); // 显式选择类。
// config.setEnableModelPreload(true); // 仅后端支持的环境显式开启，默认 false。
// config.setDynamicConfig(regionUrls); // 仅配置时 SDK 才自动预取。
WhiteSdk sdk = new WhiteSdk(view, context, config, new CommonCallback() {
    @Override public void onPreloadStateChange(WhitePreloadResult result) {
        // 可选观察已开启的任务，总结果仅回调一次 success/warn。
        // 不要求宿主等待该通知才能 joinRoom。
    }
});
sdk.prepareCompletion(new Promise<Void>() {
    public void then(Void ignored) {
        // 基础 JS SDK 已准备完成，不代表所有可选预加载或课件像素 ready。
    }
    public void catchEx(SDKError error) { /* 处理本次显式等待失败 */ }
});
// 进入白板页：移动/展示原 View，直接使用上面的 sdk。
sdk.joinRoom(params, callbacks, promise);
```

`prepareCompletion` 是可选接口；构造后立即 `joinRoom` 也支持。默认 15 秒只限制显式 `prepareCompletion` 等待；没有给构造阶段新增准备期限。SDK 在 join 时使用既有模型 120 秒、动态配置 5 秒的等待，失败/超时回退。Native 总入房时限默认仍为 45 秒，不延长。

教具预准备仅由两个教具开关同时为 true 开启，与模型/App 类开关互不隐式联动。配置了动态配置时，Bridge 只观察 SDK 已启动的请求结果；失败或取消则把首次教具配置留给实际 Room 的正常路径。异步 Worker 资源完成后先启动共享探针，再允许实际 getInstance 消费，避免迟到 prepare 与初始化竞争。

教具参数遵循 appliance-plugin 1.1.49 的 JS 文档首份配置生效规则：前置时在 SDK 级配置教具参数；后续 RoomParams 不覆盖已冻结的参数。后置时首次实际初始化采用 RoomParams > SDK 显式值 > 远端 > 默认值。

## Worker 生命周期

- mainThread 不创建内置 Worker Blob；auto 默认 useSimple=true 只准备 Full；useSimple=false 准备 Full+Sub。
- Blob URL 为 JS 文档共享，重入房复用；leaveRoom/releaseRoom 不撤销它们。
- 探针临时 Worker 在探针结束时终止；绘制 Worker 在实际离房销毁插件时终止。
- releaseRoom 只释放 Native 引用，不等于实际离房。页面退出用 leaveRoom；最终销毁原 View 才释放文档资源。

Debug Demo：ReleaseE2EActivity 验证实际 Native 调用；LaunchPreloadActivity 展示同 View/SDK 的候课页复用及点击后普通初始化；WorkerProbeActivity 可显式切换 preload 与 Worker 模式。绘制测试必须等待初始化及输入就绪。
