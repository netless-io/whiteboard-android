# 版本更新记录
## [2.16.62] - 2023-08-23
- 新增 `Room.focusApp`，用于设置多窗口下焦点窗口。
- 新增 `Room.queryApp`，用于查询指定 App 信息。
- 新增 `Room.queryAllApps`，用于查询所有 App 信息。
## [2.16.61] - 2023-08-18
- 修复 window-manager 打包错误。
- 下假 FPA 功能，移除 FPA 依赖。
## [2.16.60] - 2023-08-11
- 更新 `@netless/app-slide` 至 0.2.41
## [2.16.59] - 2023-07-06
- 新增 `Room.dispatchDocsEvent`, 用于控制多窗口下文档的切换。
- 新增 `WindowParams.fullscreen`, 用于控制窗口是否全屏。
- 更新 `ProjectorAttributes` 和 `ProjectorOptions`, 方便自定义 SlideApp 插入。
## [2.16.58] - 2023-07-04
- 新增 `Region.eu` 服务欧洲区域，废弃 `Region.gb_lon`。
## [2.16.56] - 2023-06-14
- 更新 `@netless/app-slide` 至 0.2.38
## [2.16.55] - 2023-04-21
- 更新 `white-web-sdk` 至 2.16.42。
- `WhiteSdkConfiguration` 新增 `apiHosts` 配置项, 用于配置白板的 API 服务器域名列表。
- `RoomParams` 新增 `modulesOrigin`, `PlayerConfiguration` 新增 `modulesOrigin` 配置项。用于配置白板请求 modules 数据的地址。
- 修复新版 Android Studio 无法预览布局问题
## [2.16.54] - 2023-03-29
- 更新 `@netless/app-slide` 至 0.2.36
## [2.16.53] - 2023-03-02
- 更新 `@netless/app-slide` 至 0.2.34
- `WhiteSdkConfiguration` 新增 `enableSlideInterrupterAPI`
  配置项。开启后，通过 `WhiteSdk.setSlideListener(SlideListener)`，实现 `SlideListener.slideUrlInterrupter` 对资源 url 进行处理。
- `WhiteSdkConfiguration` 新增 `slideAppOptions` 配置项用于 SlideApp
  显示配置。当前支持配置 `slideAppOptions.showRenderError`，用于配置显示/隐藏渲染错误提示。
## [2.16.52] - 2023-02-17
- 更新 `@netless/app-slide` 至 0.2.33
## [2.16.51] - 2023-02-09
- 更新 `@netless/app-slide` 至 0.2.32
- `WhiteSDK` 新增 `getSlideVolume` 和 `updateSlideVolume` 方法用于获取和更新 ppt 内音频音量
## [2.16.50] - 2023-02-03
- 更新 `@netless/app-slide` 至 0.2.31
## [2.16.48] - 2023-01-13
- 更新 `@netless/app-slide` 至 0.2.30
## [2.16.47] - 2023-01-06
- 更新 `@netless/app-slide` 至 0.2.29
- 更新 `@netless/slide-rtc-plugin` 至 0.10.1
## [2.16.46] - 2022-12-27
- 更新 `@netless/app-slide` 至 0.2.28, 修复部分 WebView 着色器编译问题。
## [2.16.45] - 2022-12-09
- 更新 `@netless/app-slide` 至 0.2.27
- 更新 `@netless/slide-rtc-plugin` 至 0.9.3， 修复 rtc 混音问题
## [2.16.44] - 2022-12-01
- 更新 `@netless/app-slide` 至 0.2.24
- 更新 `@netless/window-manager`至 0.4.54
## [2.16.43] - 2022-11-15
- 更新 `@netless/app-slide` 至 0.2.23, 修复部分切页动画黑屏
## [2.16.42] - 2022-10-27
- 更新 `white-web-sdk` 至 2.16.37。修复 Text 教具导致的弹出键盘异常
## [2.16.41] - 2022-10-24
- 修复部分低版本 WebView 未定义 globalThis 错误
- 更新 `@netless/slide-rtc-plugin` 至 0.8.4， 修复 rtc 混音问题
- 更新 `@netless/app-slide` 至 0.2.22
## [2.16.39] - 2022-10-14
- 更新 `@netless/slide-rtc-plugin` 至 0.7.9
## [2.16.38] - 2022-10-12
- 更新 `@netless/window-manager` 至 0.4.46, 修复 `rectangle` `ellipse` `straight` 等教具 icon 同步显示错误的问题
- 更新 `@netless/app-slide` 至 0.2.17
## [2.16.37] - 2022-10-10
- 更新 `@netless/slide-rtc-plugin` 至 0.7.5，修复混音进度单位错误
- 更新 `@netless/app-slide` 至 0.2.16，修复 PPT 未显示时 `WhiteSdk.requestSlideLog` 无回调问题
- 修复 `RoomParams.floatBar`，默认不开启
## [2.16.36] - 2022-09-23
- 更新 `@netless/window-manager` 至 0.4.44
- 更新 `@netless/app-slide` 至 0.2.12
- 新增 `WhiteSdk.requestSlideLog` 方法，支持客户端获取 PPT 日志，方便问题排查
## [2.16.35] - 2022-09-14
- 更新 `white-web-sdk` 至 2.16.35。
## [2.16.34] - 2022-08-30
- 新增 `RoomParams.floatBar`, 通过 `RoomParams.setUsingFloatBar` 开启或关闭图片、画笔、文本等的浮窗操作工具.
- 新增 `Appliance.PENCIL_ERASER`, 支持线条的局部擦除.
## [2.16.33] - 2022-08-23
- 更新 `white-web-sdk` 至 2.16.34。
- 更新 `@netless/app-slide` 至 0.2.8。修复切页动画显示不正确的问题
## [2.16.32] - 2022-08-19
- 更新 `@netless/window-manager` 至 0.4.42
- 修复多窗口下 PPT 最大化时最小化重新进入再恢复的显示问题
- 更新 `netless/cursor-tool` 至 0.1.1, 修复低版本安卓下头像名字无背景问题
## [2.16.31] - 2022-08-17
- 更新 `@netless/window-manager`至 0.4.40, 修复无法插入动态文档问题.
## [2.16.30] - 2022-08-16
- 更新 `white-web-sdk` 至 2.16.33。
- 更新 `@netless/window-manager`至 0.4.38。
## [2.16.29] - 2022-08-11
- 更新 `@netless/app-slide` 至 0.2.7。
- 更新 `@netless/slide-rtc-plugin` 至 0.6.1。
- `AudioMixerBridge` 添加 `pauseAudioMixing`, `resumeAudioMixing` 方法。用户需要在自定义实现类中调用 RTC 对应的混音方法。
## [2.16.28] - 2022-08-03
- 更新 `white-web-sdk` 至 2.16.31。修复 room.setMemberState() 某些字段无法保存到本地问题。
## [2.16.27] - 2022-08-01
- 更新 `@netless/window-manager`至 0.4.37。
- 修复 Slide 最小化状态下重新进入房间恢复正常无法恢复到正确的大小问题。
- 修复只读用户 Slide 最小化状态下重新进入房间恢复正常无法恢复到正确的大小问题。
## [2.16.26] - 2022-07-19
- 更新 `white-web-sdk` 至 2.16.27
- 画笔支持虚线功能，设置 `room.memberState.dottedLine` 为 `true` 开启该功能。该功能需要开启新铅笔后才能使用
- 文字工具支持直接选择并编辑文字，设置 `room.memberState.textCanSelectText` 为 `true` 开启该功能
- 修改箭头、直线工具的交互方式
## [2.16.25] - 2022-07-07
- 更新 `@netless/window-manager`至 0.4.35。 
  - 降低发送鼠标移动事件的频率 
  - 修复在 `writable` 进行切换的时候 `mainView` 的 `disableCameraTransform` 丢失 
  - 修复重连之后 app 没有正确创建的问题 
  - 修复app 创建之后初始化宽高没有同步的问题
## [2.16.24] - 2022-06-29
- 更新 `@netless/window-manager`至 0.4.33。
  - 修复多人同时调用 WindowManager.mount 有概率出现错误的问题。 
  - 修复只读用户的重连刷新问题。
## [2.16.23] - 2022-06-09
- 更新 `@netless/window-manager`至 0.4.30。
- 添加 `Room.removePage` 接口
## [2.16.22] - 2022-06-02
- 更新 `iframe-bridge`至 2.1.9。
- 修复多窗口模式下 `sceneState` 变化无回调问题。
- 添加 `Player.addPlayerListener`, `Player.removePlayerListener` 接口
## [2.16.21] - 2022-05-31
- 更新 `white-web-sdk` 至 2.16.24
- 更新`@netless/window-manager`至 0.4.26
## [2.16.20] - 2022-05-30
- 更新 `@netless/app-slide`至 0.2.1
- 修复 单窗口模式下 `Room.sceneState` 不回调问题
- 修复 PPT 文字排版显示问题
## [2.16.19] - 2022-05-16
- 更新`@netless/window-manager`至 0.4.25。
- 新增 `Room.setContainerSizeRatio` 方法，用于更新多窗口显示比例。
- 新增 `Room.setPrefersColorScheme` 方法，用于更改多窗口暗色模式。
## [2.16.18] - 2022-05-06
- 更新 `white-web-sdk` 至 2.16.20
- 新增 `WindowParams` 属性值获取方法
## [2.16.17] - 2022-04-24
- 更新 `white-web-sdk` 至 2.16.19
## [2.16.16] - 2022-04-20
- 更新`@netless/window-manager`至 0.4.23, 修复可写进入立即切换成只读造成初始化 camera 失败的问题
## [2.16.15] - 2022-04-14
- 更新`@netless/app-slide`至 0.1.3
## [2.16.14] - 2022-04-12
- 更新`@netless/window-manager`至 0.4.21
## [2.16.13] - 2022-04-08
- 更新 `white-web-sdk` 至 2.16.15
- 新增 `Displayer.getScene` 方法，获取指定路径场景信息
- 新增 `WhiteSdkConfiguration.disableNewPencilStroke` 属性。默认 false 启用新铅笔笔锋, true 禁用新铅笔笔锋。
## [2.16.12] - 2022-04-01
- 更新`@netless/window-manager`至 0.4.20
- 更新`@netless/app-slide`至 0.1.1
- 更新`iframe-bridge`至 2.1.8
- 加了锁定ppt 的功能，小窗口 ppt 里面的白板，禁止拖动。
## [2.16.11] - 2022-03-28
- 更新`@netless/window-manager`至 0.4.18
- 修复单窗口模式下，iframe 不显示的问题## 
## [2.16.10] - 2022-03-28
- 更新`@netless/window-manager`至 0.4.17
- 修复 removeScenes 为 "/" 没有清理完成完成时添加 app 造成的状态错误
- 更新 `WhiteSdk.registerApp` 接口，添加注册结果回调。
- 升级@netless/app-slide至0.1.0
## [2.16.9] - 2022-03-25
- 修复多窗口下，快速修改 RoomState 内容，会丢失部分状态变化的问题
## [2.16.8] - 2022-03-25
- 更新`@netless/window-manager`至 0.4.15
- 修复 removeScenes 为 "/" 时,  切换主白板和 app focus 失效的问题
- 新增 `WhiteSdk.registerApp` 接口，用于添加用户自定义的 App 组件。
## [2.16.7] - 2022-03-22
- 更新`@netless/window-manager`至 0.4.14
- 修复 removeScenes 为 "/" 时， 同步端笔迹依旧存在的问题
## [2.16.6] - 2022-03-16
- 更新`@netless/window-manager`至 0.4.13
- 修复 多窗口模式下，只读状态时 `viewMode` 无法从 `freedom` 切换回 `broadcaster` 问题
## [2.16.5] - 2022-03-10
- 更新`@netless/window-manager`至 0.4.11
- 新增 `RoomState` 新增 `PageState` 属性，用于获取主窗口页面状态。单窗口模式下兼容 `SenceState`
## [2.16.4] - 2022-03-01
- 更新`@netless/window-manager`至 0.4.9
- 修复 多窗口下 `SceneState` 回调异常问题
## [2.16.3] - 2022-02-24
- 更新`@netless/window-manager`至 0.4.7
- 新增 `Room` 接口 `addPage` `nextPage` `prevPage`
## [2.16.2] - 2022-02-22
- 更新`white-web-sdk`至 2.16.10
- 新增 `RoomParams.nativeWebSocket` 属性，支持网络加速。
## [2.16.1] - 2022-02-16
- 更新`@netless/window-manager`至 0.4.5
- 更新`white-web-sdk`至 2.16.9
## [2.16.0] - 2022-02-11
- 更新`@netless/window-manager`至 0.4.1
- 更新`white-web-sdk`至 2.16.7
- 增加调用日志逻辑。开关跟随`WhiteSdkConfiguration`中的 log 参数（默认关闭），上报等级为 info，上报配置跟随`loggerOptions`配置项。
- 回放支持多窗口模式，需要在初始化 SDK 时，配置`WhiteSdkConfiguration`开启 useMultipleView 参数。
- 添加 Room 新增 `insertText` 接口
- 修复多窗口模式下 `redo` `undo` 失效问题。
## [2.15.27] - 2022-03-09
- 更新`@netless/window-manager`至 0.3.27
## [2.15.26] - 2022-03-02
- 更新`@netless/window-manager`至 0.3.26
## [2.15.25] - 2022-01-20
- 更新`@netless/window-manager`至 0.3.25
- 更新`@netless/app-slide`至 0.0.56
- 更新`white-web-sdk`至2.15.17
## [2.15.24] - 2022-01-18
- 更新`white-web-sdk`至 2.15.16
- 更新`@netless/window-manager`至 0.3.23
## [2.15.22] - 2022-01-13
- 更新`@netless/window-manager`至 0.3.19
## [2.15.21] - 2021-01-13
- 更新`white-web-sdk`至 2.15.15
## [2.15.18] - 2021-12-28
- 更新`@netless/window-manager`至 0.3.17
## [2.15.17] - 2021-12-23
- 更新`@netless/window-manager`至 0.3.16
- 更新`@netless/app-slide`至 0.0.52
## [2.15.15] - 2021-12-20
- 更新`@netless/app-slide`至 0.0.51
- 降级`@netless/window-manager`至 0.3.14
## [2.15.14] - 2021-12-17
- 更新`white-web-sdk`至 2.15.13
- 更新`@netless/app-slide`至 0.0.50
- 降级`@netless/window-manager`至 0.3.11
## [2.15.12] - 2021-12-14
- 更新`@netless/window-manager`至 0.3.12
- 更新`@netless/app-slide`至 0.0.44
## [2.15.10] - 2021-12-14
- 优化 native 端截图 API，恢复图片支持，同时支持多窗口模式
- 更新`@netless/window-manager`至 0.3.11
- 更新`@netless/app-slide`至 0.0.42
## [2.15.9] - 2021-12-13
- 修复多窗口模式下，第二次调用加入房间API 失败，并无回调的问题
## [2.15.8] - 2021-12-13
- 更新`@netless/app-slide`至 0.0.40
## [2.15.7] - 2021-12-09
- 更新`@netless/window-manager`至 0.3.9
- 更新`@netless/app-slide`至 0.0.36
## [2.15.6] - 2021-12-07
- 更新`white-web-sdk`至 2.15.11
- 更新`@netless/window-manager`至 0.3.8
- 更新`@netless/app-slide`至 0.0.35
## [2.15.5] - 2021-11-24
- 更新`white-web-sdk`至 2.15.7
- 更新`@netless/window-manager`至 0.3.7
## [2.15.4] - 2021-11-23
- 更新`white-web-sdk`至 2.15.6
- 更新`@netless/window-manager`至 0.3.5
- 更新`@netless/app-slide`至 0.0.27
## [2.15.3] - 2021-11-22
- 更新`white-web-sdk`至 2.15.6
- 更新`@netless/window-manager`至 0.3.2
- 更新`@netless/app-slide`至 0.0.25
## [2.15.2] - 2021-11-16
- 更新`white-web-sdk`至 2.15.4
- 更新`@netless/window-manager`至 0.2.19
- 优化 `useMultiViews` 为 true 时，cameraState 状态回调
## [2.15.1] - 2021-11-09
- 更新`white-web-sdk`至 2.15.3
- 更新`@netless/window-manager`至 0.2.17
## [2.15.0] - 2021-11-01
- 更新`white-web-sdk`至 2.15.1
- 更新`RoomParams`强制依赖 uid
## [2.14.4] - 2021-10-28
- 更新`white-web-sdk`至 2.14.7
- 添加`@netless/app-slide`插件支持
## [2.14.3] - 2021-10-15
- 更新`white-web-sdk`至 2.14.5
- 更新`@netless/window-manager`至 0.2.5，修复`WhiteSdkConfiguration`中`useMultiViews`为 true 时，`WhiteRoomConfig`的 `disableCameraTransform` true 无法生效的问题。
## [2.14.2] - 2021-10-14
- 更新`@netless/window-manager`至 0.2.3
## [2.14.1] - 2021-10-09
- 更新`white-web-sdk`至 2.14.4，支持多窗口模式。具体见 `WhiteSdkConfiguration` 类中的`useMultiViews` 以及`RoomParams`的`windowParams` 属性注释，多窗口暂时不支持回放。
- 修改 Android 最低支持版本为 21
## [2.13.24] - 2021-09-09
- 更新`white-web-sdk`至 2.13.20
## [2.13.23] - 2021-08-20
- 更新`white-web-sdk`至 2.13.18
## [2.13.22] - 2021-08-13
- 更新`white-web-sdk`至 2.13.17
## [2.13.21] - 2021-08-11
- 更新`white-web-sdk`至 2.13.16
## [2.13.20] - 2021-08-06
- 修复 CommonCallback `onLogger` `onMessage` 等接口无回调问题
## [2.13.19] - 2021-08-05
- CommonCallback 添加日志回调接口`onLogger`
- 更新`white-web-sdk`至 2.13.14
## [2.13.18] - 2021-07-26
- 更新`white-web-sdk`至 2.13.12
## [2.13.17] - 2021-07-17
- 更新`white-web-sdk`至 2.13.11
## [2.13.16] - 2021-07-15
- 更新`@netless/video-js-plugin`至 0.3.3
## [2.13.15] - 2021-07-14
- 更新`@netless/video-js-plugin`至 0.3.2
## [2.13.14] - 2021-07-13
- 更新`@netless/video-js-plugin`至 0.3.0
## [2.13.13] - 2021-07-12
- 更新`@netless/video-js-plugin`至 0.3.0.beta.10
## [2.13.12] - 2021-07-12
- 修复 2.13.7 出现的 refreshViewSize 调用无效的问题
## [2.13.11] - 2021-07-10
- 更新`@netless/video-js-plugin`至 0.2.2
## [2.13.10] - 2021-07-09
- 更新`@netless/video-js-plugin`至 0.2.1
## [2.13.9] - 2021-07-08
- 更新`white-web-sdk`至 2.13.10，修复 2.13.x 版本中，第一笔无法正常绘制的问题
## [2.13.8] - 2021-07-07
- 更新`@netless/video-js-plugin`至 0.2.0
## [2.13.7] - 2021-07-06
- 更新`white-web-sdk`至 2.13.9
## [2.13.5] - 2021-06-21
- 更新`white-web-sdk`至 2.13.6
- 优化 room `setTimeDelay`方法
## [2.13.4] - 2021-06-15
- 更新`white-web-sdk`至 2.13.4
## [2.13.2] - 2021-06-11
- 更新`white-web-sdk`至 2.13.3
## [2.13.1] - 2021-06-11
- 更新`white-web-sdk`至 2.13.2
- 修复设置为 disableDeviceInputs 后 iframe 插件，有一定情况仍然能够接受交互的情况
## [2.12.38] - 2021-07-06
- 更新`white-web-sdk`至 2.12.23
## [2.12.37] - 2021-06-30
- 修复两次回调`onPhaseChanged`问题
## [2.12.36] - 2021-06-08
- 修复处于 clicker 教具，进行缩放后，出现的视野异常问题
## [2.12.35] - 2021-06-07
- 更新`@netless/cursor-tool`至 0.1.0
- 更新`@netless/iframe-bridge`至 2.1.2
## [2.12.34] - 2021-06-04
- 更新`white-web-sdk`至 2.12.21
## [2.12.33] - 2021-06-03
- 更新`@netless/video-js-plugin`至 0.1.5
## [2.12.32] - 2021-06-02
- 修复`video-js-plugin`不显示问题
## [2.12.31] - 2021-06-01
- 更新`@netless/video-js-plugin`至 0.1.3, 修复低版本WebView兼容问题
## [2.12.30] - 2021-05-25
- 更新`@netless/cursor-tool`至 0.0.9
## [2.12.29] - 2021-05-24
- 更新`white-web-sdk`至 2.12.20
- 添加`@netless/video-js-plugin`插件支持
## [2.12.28] - 2021-05-20
- 更新`white-web-sdk`至 2.12.19
## [2.12.27] - 2021-05-17
- 更新`white-web-sdk`至 2.12.18
- 默认开启服务器端排版本，同时加载服务器端裁剪字体。具体参考`PptParams`的`useServerWrap`属性注释。
## [2.12.26] - 2021-05-13
- 更新`white-web-sdk`至 2.12.17
- `MemberState`新增`点击``形状`教具，具体可以查看`com.herewhite.sdk.domain.Appliance`文件。形状教具类型查看`com.herewhite.sdk.domain.ShapeType`
- `Room`新增`syncBlockTimestamp`接口
## [2.12.25] - 2021-04-28
- 更新`@netless/white-audio-plugin2`,`@netless/white-video-plugin2`插件，修复显示问题
## [2.12.24] - 2021-04-27
- 修复`getRoomState`异步API，没有回调的问题
- 更新`@netless/white-audio-plugin` 至 1.2.23，修复回放时，音频文件显示问题。
- 支持`@netless/white-audio-plugin2`,`@netless/white-video-plugin2`插件同步支持，需要在 web 端，调用 insertPlugin 时，注册对应的 `audio2`,`video2` 。
## [2.12.22] - 2021-04-22
- 更新`white-web-sdk`至 2.12.14
## [2.12.21] - 2021-04-22
- 接入 V5 版本的 PPT 转换，支持生成 PPT 预览图，静态图片缩放。具体参考 ConverterV5
- 变更 Converter.ConvertType 为独立 ConvertType, 影响 ConvertedFiles `getType`
- 变更 RoomListener 及 PlayerListener 由主线程回调
- 更新`white-web-sdk`至 2.12.13
- 更新`@netless/iframe-bridge`至 2.12.17,优化回放时 iframe 插件逻辑
## [2.12.20] - 2021-04-17
- 更新`white-web-sdk`至 2.12.12
## [2.12.19] - 2021-04-14
- 更新`@netless/iframe-bridge`至 2.0.14，优化 iframe 插件
- pptParams 提供新的构造方法，不需要配置 scheme 参数
## [2.12.18] - 2021-04-12
- 更新`@netless/iframe-bridge`至 2.0.13，优化消息通知
## [2.12.17] - 2021-04-12
- 更新`@netless/iframe-bridge`至 2.0.11，优化 Android 端显示问题
## [2.12.16] - 2021-04-09
- 更新`@netless/iframe-bridge`至 2.0.10，优化 Android 端显示问题
## [2.12.15] - 2021-04-10
- 更新`white-web-sdk`至 2.12.9
## [2.12.14] - 2021-04-09
- 更新`@netless/iframe-bridge`至 2.0.9，修复 h5 课件显示问题
## [2.12.13] - 2021-04-09
- 恢复 Displayer `scaleIframeToFit`API
## [2.12.12] - 2021-04-09
- 更新`@netless/iframe-bridge`至 2.0.8，修复 h5 课件显示问题
## [2.12.11] - 2021-04-06
- 更新`white-web-sdk`至 2.12.8，修复 follower 视角可能无法立即同步的问题
## [2.12.10] - 2021-04-02
- 更新`white-web-sdk`至 2.12.7,优化动态 ppt 显示
## [2.12.9] - 2021-03-30
- 更新`@netless/iframe-bridge`至 2.0.7
## [2.12.8] - 2021-03-30
- 更新`white-web-sdk`至 2.12.6
## [2.12.7] - 2021-03-25
- 更新`@netless/iframe-bridge`至 2.0.5，优化回放时，H5 课件展示
- 修复部分设备下，切换到文字教具无法弹出键盘的问题
- 增加 whiteboardView 自动调用 refreshViewSize 功能
## [2.12.6] - 2021-03-25
- Displayer 新增`scaleIframeToFit`API，可以将 H5 课件进行铺满操作（类似`scalePptToFit`），详情见API注释
## [2.12.5] - 2021-03-25
- 更新`@netless/cursor-tool`至 0.0.7
## [2.12.3] - 2021-03-20
- 默认关闭笔锋功能，开启笔锋后的笔记，需要客户本地 sdk 支持，否则无法显示。如需打开，请参考`RoomParams`中的`disableNewPencil`属性。
## [2.12.2] - 2021-03-15
- 更新`white-web-sdk`至 2.12.4，优化 ppt 显示逻辑
- 优化音视频插件，在回放时候的显示
- 修复 debug 模式下，实时房间异常回调导致的 crash 的问题
## [2.12.1] - 2021-03-15
- 优化使用 iframe 课件时，部分课件存在性能问题
## [2.12.0] - 2021-03-11
- `WhiteSdkConfiguration`新增`enableImgErrorCallback`参数，开启图片加载失败事件的监听，该监听，会回调`CommonCallbacks`增的`onMessage`方法。事件内容格式，见`onMessage`中注释。
## [2.11.21] - 2021-03-08
- 更新`@netless/iframe-bridge`至1.1.2
## [2.11.20] - 2021-03-08
- 解决构建工具缓存，导致部分 Android Studio 用户 2.11.19 版本缓存错误的问题。
## [2.11.19] - 2021-03-05
- 更新`white-web-sdk`至 2.12.2,优化 ppt 显示逻辑
- 更新@netless/white-audio-plugin@1.2.20,@netless/white-video-plugin@1.2.20，优化音视频插件
- 更新`@netless/iframe-bridge`至1.1.1
## [2.11.15] - 2021-02-05
- 更新`@netless/white-audio-plugin@1.2.19`,`@netless/white-video-plugin@1.2.18`，优化音视频插件进度同步
## [2.11.14] - 2021-02-05
- 更新`@netless/white-audio-plugin@1.2.17`,`@netless/white-video-plugin@1.2.16`，优化音视频插件进度同步
## [2.11.13] - 2021-01-29
- 更新`white-web-sdk`至 2.11.11，优化 ppt 中音视频处理
## [2.11.12] - 2021-01-26
- 更新`white-web-sdk`至 2.11.10，兼容部分低版本 ppt 音视频播放
## [2.11.11] - 2021-01-20
- 更新`white-web-sdk`至 2.11.9
- `DisplayerState`新增`cameraState`属性，`RoomState`与`PlayerState`均可使用，具体请看`CameraState`类注释
## [2.11.10] - 2020-12-29
- 更新`white-web-sdk`至 2.11.8
- 更新`@netless/iframe-bridge`至 1.0.6
- 更新`LoggerOptions`日志相关配置类
## [2.11.9] - 2020-12-29
- 更新`white-web-sdk`至 2.11.8
- 更新`@netless/iframe-bridge`至 1.0.6
## [2.11.8] - 2020-12-17
- 更新`@netless/iframe-bridge`至 1.0.5
## [2.11.7] - 2020-12-17
- 更新`@netless/iframe-bridge`至 1.0.4
## [2.11.6] - 2020-12-10
- 同步更新 web sdk 至 2.11.7
- 同步更新`@netless/combine-player`,`@netless/iframe-bridge`插件
- ppt 自定义字体现在支持默认回落字体设置。在自定义设置里，key 设置为'*','*-italic','*-bold','*-bold-italic'后， 当存在不属于自定义字体列表的常规体，斜体，粗体，粗斜体都会使用以上传入的网址字体进行加载。
## [2.11.5] - 2020-12-08
- 修复向 iframe 插件发送消息时，遇到的权限问题
## [2.11.4] - 2020-12-08
- 新增 iframe 插件消息通道 API：
    1. 接受 iframe 消息通道：见 `CommonCallbacks` 中 `onMessage`方法
    2. 向 iframe 发送消息：见 `WhiteDisplayer` 中 `postMessage`方法
- 修复`loadFontFaces:completionHandler:`无法添加多个不同字重的字体的问题
## [2.11.2] - 2020-12-03
- 同步更新 web sdk 至 2.11.6
- 优化弱网连接
- WhiteSDK 新增本地嵌入字体 API `setupFontFaces` `loadFontFaces`，设置本地教具字体 API  `updateTextFont:`。具体使用，可以查看对应 API 代码注释。
## [2.11.1] - 2020-11-27
- 同步更新 web sdk 至 2.11.5
- 更新`@netless/combine-player`，优化插件逻辑
## [2.11.0] - 2020-11-17
- 同步更新 web sdk 至 2.11.3
- iframe 插件的使用，增加开关，并且默认关闭（具体见 WhiteSdkConfiguration setEnableIFramePlugin 方法）。
- WhiteSdk 增加 isPlayable API，可以查询，对应房间，对应时间段是否存在回放数据。
- WhiteSdk 支持多数据中心，枚举可见 com.herewhite.sdk.domain.Region，可以分别在初始化 sdk，加入实时房间，回放房间时，进行设置。默认 Region 为旧数据中心。SDK 初始化 region 参数，将会影响实时房间，回放房间默认 region。具体见`WhiteSdkConfiguration`,`RoomParams`,`PlayerConfiguration`中`setRegion`API。
- 回放时，传入mediaURL，将由开源组件`@netless/combine-player`接管，该组件优化了音视频中有丢帧情况的播放处理。
- 回放 Player 增加 disableCameraTransform API，该功能与实时房间 room 效果一致（具体见 com.herewhite.sdk.Displayer disableCameraTransform方法）。
## [2.10.1] - 2020-10-12
- 修复回放速率为 x.y 带小数点的速率，出现的错误问题
## [2.10.0] - 2020-10-10
- 同步更新 web sdk 至 2.10.1 版本（无断代更新内容）
- 支持显示web 端通过 iframe 插件（`@netless/iframe-bridge`）插入的 iframe 插件，类似音视频插件，native 无需进行修改，只需要更新至 2.10.0 版本即可
## [2.9.21] - 2020-09-23
- 同步 web sdk 至 2.9.17
- RoomCallbacks 移除 `onBeingAbleToCommitChange`，新增 `onCanUndoStepsUpdate` `onCanRedoStepsUpdate`，具体见源码注释
- 更新头像显示组件，修复没有传入 userPayload 时，无法显示的问题
## [2.9.20] - 2020-09-15
- 切换头像显示组件UI，web 端可以切换至`@netless/cursor-tool`即可保持一致，新组件支持`cursorName`，`avatar`字段。
## [2.9.19] - 2020-09-10
- 同步 web sdk 至 2.9.16
## [2.9.18] - 2020-09-03
- 同步 web sdk 至 2.9.15
## [2.9.17] - 2020-08-19
- 同步 web sdk 至 2.9.14
- 支持应用层接管 ppt 音视频播放（具体见 AudioMixerBridge 以及 demo 仓库中 demo-rtc 分支）
## [2.9.16] - 2020-07-31
- 修复 Android 7.1 至 Android 8.1 下，默认渲染模式（Canvas）无法显示内容的问题
- 修复 room.phase 状态错误，必须使用 `room.getPhase` 异步 API 才能获取正确状态的问题
## [2.9.14] - 2020-07-22
- 同步 web SDK 至 2.9.12
- 修复以下情况时，webView 中 SDK 初始化/启动失败，没有任何通知的问题。回调通知在 `CommonCallbacks`新增`sdkSetupFail:`方法中；更多具体内容，见源码注释。
    1. 当传入非法 AppIdentifier
    2. 当获取用户配置信息失败时（例如无网络）
- 修复 webView 中 SDK 初始化失败，导致加入房间，回放房间 API 一直没有回调的问题。
- 弃用`UrlInterrupter`拦截 API，统一迁入`CommonCallbacks`(仍支持)
- `WhiteSdk`新增`WhiteSdk(WhiteboardView bridge, Context context, WhiteSdkConfiguration whiteSdkConfiguration, CommonCallbacks commonCallbacks)`建议使用该初始化方法，直接配置 `CommonCallbacks`，否则可能遗漏部分回调。
## [2.9.13] - 2020-07-16
- 同步 web SDK 至 2.9.11
- 增加动态 ppt 音视频播放暂停通知
- 默认切换至 canvas 渲染引擎，性能兼容性更好
## [2.9.12] - 2020-07-07
- 同步 web SDK 至 2.9.10
- 优化截图 API
## [2.9.11] - 2020-07-07
- 同步 web SDK 至 2.9.9
- 修复 native 端动态 PPT 翻页后媒体仍然在播放的 bug
## [2.9.10] - 2020-07-03
- 优化音视频插件
## [2.9.9] - 2020-07-02
- 优化音视频插件，修复 native 进入房间时，正在播放的音视频进度不一致
## [2.9.8] - 2020-07-01
- 修复动态 PPT 字体重复下载导致的内存占用过多的问题
## [2.9.7] - 2020-06-30
- 修复 CameraBound 初始化时，minScale，maxScale 配置错误问题
## [2.9.6] - 2020-06-30
- 同步更新 white-web-sdk 至 2.9.7
- 提高 canvas 引擎兼容性
## [2.9.5] - 2020-06-30
- 修复在 WebView Debug 模式下时，动态 ppt 播放音视频崩溃的问题
## [2.9.4] - 2020-06-24
- 同步更新 white-web-sdk 至 2.9.4 版本
- 修复`ContentModeConfig`中`scale`为 0 时，实际为 1 的问题
## [2.9.3] - 2020-06-23
- 同步更新 white-web-sdk 至 2.9.3 版本
- 新增`抓手``激光笔`教（见`com.herewhite.sdk.domain.Appliance`）
- 橡皮教具`disableEraseImage`属性，支持中途切换（见Room `disableEraseImage:`API）
- Room 新增`撤销`，`取消撤销`（开启该功能前，请先阅读`disableSerialization`介绍）
- Room 提供`复制`，`粘贴`，`副本`，`删除` API，可以对选中的内容，执行上述操作（见`Room`执行操作 API 部分）
- RoomParams 弃用`disableOperations`，新增`disableCameraTransform` API，与`disableDeviceInputs`搭配，可以起到同样效果。
## [2.9.2] - 2020-06-13
- 修复 userPayload 显示问题，保持与 web 端一致的显示逻辑。
## [2.9.0] - 2020-06-10
- 优化底层渲染系统，画笔教具渲染引擎，默认为`Canvas`，`svg`为兼容模式。
- `MemberState`新增`直线``箭头`教具，具体可以查看`com.herewhite.sdk.domain.Appliance`文件。
- `PlayerConfiguration``audioUrl`属性更改为`mediaURL`，效果不变。
- `WhiteSdkConfiguration`：
    1. 删除`zoomMinScale`,`zoomMaxScale`属性。限制视野需求，请阅读`WhiteRoomConfig`,`WhiterPlayerConfig`以及`WhiteCameraBound`相关类和 API。
    2. 删除`sdkStrategyConfig`属性内容。
    3. `debug`属性更改为`log`属性，效果不变。
    4. `hasUrlInterrupterAPI`字段，更改为`enableInterrupterAPI`。setter 与 getter 更改为`setEnableInterrupterAPI``isEnableInterrupterAPI`。
    5. 新增`disableDeviceInputs`配置。
- 移除`com.herewhite.sdk.Utils.PreFetcher`，SDK 采用更智能的链路选择，`PreFetcher`类的预热结果对 SDK 不再有效果。
- `WhiteCameraBound`增加初始化方法，方便从`zoomMinScale``zoomMaxScale`迁移的用户。
- `ImageInformation`类，预埋`locked`字段。
- 移除 room 的`setViewSize(int width, int height)`方法
## [2.8.1] - 2020-05-22
- 修复`预热器`数据造成的 sdk 连接失败问题。2.8.0 开始，不再需要预热功能。
## [2.8.0] - 2020-05-14
- <span style="color:red;">不兼容改动</span>：SDK 初始化时，新增必须要的 APP identitier 参数（详情见 开发者文档中，查看 APP identifier 一栏）
- 开放画笔渲染引擎选项，新增 canvas 渲染模式（需要主动选择）
- 修复`isWritable=false`用户无法跟随新主播的问题
## [2.7.7] - 2020-04-29
- 加入房间，回放 API，兼容重复调用（房间，回放实例会以最后一次成功回调为准）
## [2.7.6] - 2020-04-21
- 优化 PlayerSyncManger 线程调用
## [2.7.5] - 2020-04-16
- 优化音频插件显示
- 优化动态 ppt 音频播放问题
## [2.7.4] - 2020-04-13
- 优化音频插件显示
## [2.7.3] - 2020-04-12
- 优化音视频
- 增加获取房间内所有场景的 API（见 Displayer getEntireScenes)
## [2.7.2] - 2020-03-25
- 优化预热器连接速度，兼容未接入音视频插件用户
## [2.7.1] - 2020-03-22
- 优化 ppt
## [2.7.0] - 2020-03-18
- 优化底层显示
- 优化动态 ppt
- 注意：2.7.0 版本有一定兼容问题，接入自定义音视频插件系统的用户，可以升级（2020 年开始接入的用户，均为该版本）；未接入音视频插件的用户请勿升级。如不清楚版本，可以询问服务团队。
## [2.6.5] - 2020-03-12
- 部分 promise API 允许传入 null
- 修复加入房间时，可能出现的空指针问题
## [2.6.4] - 2020-03-03
- 优化只读模式
- 优化动态 ppt 音视频
- 新增`getScenePathType`API（见 Displayer `getScenePathType`方法）
- 部分类，增加带参数初始化方法
- 恢复支持 Android 4.4 支持
- 优化只读模式
## [2.6.3] - 2020-02-24
- 优化连接性，以及日志上报逻辑
## [2.6.2] - 2020-02-23
- 优化只读模式
- 修复回放时，后半段时间回调`step`失效的问题
- 修复`throwError`回调丢失信息的问题
## [2.6.1] - 2020-02-20
- 添加回放时间进度回调频率 API（详情见 PlayerConfiguration`step`属性）
- 添加重连等待时长 API（详见 RoomParams`timeout`属性）
- 添加`writable`只读模式（详情见 RoomParams`writable`属性，以及 Room `setWritable:`方法）
- 修复部分情况下，清屏 API 失效的情况
## [2.6.0] - 2020-02-18
- 优化加入房间稳定性
## [2.5.7] - 2020-02-11
- 修复显示用户头像时，教具显示不正确问题
## [2.5.3] - 2020-02-04
- 修复图片拦截 API
- 增加预热器功能，提前选取就进资源
- 增加白板倍率播放 API
## [2.5.1] - 2020-01-13
- 修复支持插件系统用户的连接问题
## [2.5.0] - 2020-01-07
- 优化音视频插件
- 增加向后兼容可能性
- `图片拦截功能暂时不可用，将在下一个版本恢复`
## [2.4.28] - 2019-12-31
- 更新音视频插件
- 修复带参数的 `scalePptToFit` 无效问题
## [2.4.27] - 2019-12-26
- 添加音视频插件支持
## [2.4.26] - 2019-12-26
- 修复 `PlayerSyncManager`
## [2.4.24] - 2019-12-25
- 提供源码以及注释内容
- 修复 `PlayerSyncManager` 问题
## [2.4.23] - 2019-12-23
- 增加 CombinePlayer 模块，提供 `PlayerSyncManager` 同步客户端音视频播放器与白板回放播放状态。
    * 具体使用，见回放文档。
    * 具体见[Android-Demo](./android-open-source)提供的 NativeMediaPlayer 类。
## [2.4.22] - 2019-12-20
- 优化重连逻辑
- 支持动态 ppt 点击动画
- 提供 ppt 铺满屏幕 API `scalePptToFit`
## [2.4.21] - 2019-11-24
- 修复 Android 4.4 支持问题
## [2.4.20] - 2019-11-19
- 修复文字空格宽度问题
## [2.4.19] - 2019-11-13
- 修复 disableCameraTransform 导致的绘制问题
## [2.4.18] - 2019-11-05
- 橡皮擦教具，增加禁止擦除图片选项（初始化房间参数配置）
## [2.4.17] - 2019-11-04
- 修复 SDK 初始化时，部分传入参数不生效的问题
- 提取 Player 与 Room 共有方法，迁移进 Displayer 作为父类实例方法（refreshViewSize, convertToPointInWorld, addMagixEventListener, addHighFrequencyEventListener, removeMagixEventListener）
## [2.4.16] - 2019-10-29
- 回放增加 refreshViewSize API
- 修复了回放时首帧存在快进的问题
- 修复了文字教具在不同端使用不同字体时，造成的文字截断问题
## [2.4.15] - 2019-10-25
- 增加高频自定义事件 API（Room,以及 Player 的 addHighFrequencyEventListener API）
## [2.4.14] - 2019-09-20
- 优化
## [2.4.13] - 2019-09-09
- 恢复图片替换 API
- 切换场景 API，提供成功失败回调
- 移除异步 API Deprecate 警告
## [2.4.12] - 2019-08-30
- 设置场景路径 API，增加成功失败回调
- 优化 Android 4.4 显示
- 优化截图效果
- room 中 ObserverId，表示当前用户在白板内部的 id
## [2.4.10] - 2019-08-25
- 兼容 Android 4.4
- 修复回放时，图片替换 API 失效问题
- 修复带音视频回放时，PlayerPhase 状态变化回调不及时问题
- 优化带音视频回放效果，支持重复初始化
- 优化回放同步获取状态 API
- 修正主播状态信息类型，无主播时，对应信息为空
- 修复主动断连时，无回调问题
- 修复处于最大缩放比例时，双指移动异常的问题
## [2.4.9] - 2019-08-21
- 修复回放 playerState 状态不正确
## [2.4.8] - 2019-08-16
- 支持自定义全局状态
- 房间成员列表功能增强，支持显示用户信息，用户教具信息
- 视角状态 API，增加主播用户信息；修正无主播时，主播id 为 0 的问题
- 修正 Scene 类中，`component` 字段类型错误
- 修正白板类名，并提供向前兼容
- 移除部分无用字段
- 移除部分无效类
## [2.4.6] - 2019-08-06
- 修复部分情况下，用户加入白板，无法立刻看到主播端画面的问题
### [2.4.4] - 2019-08-02
- 优化重连逻辑
### [2.4.3] - 2019-08-01
- 修复视角锁定 API
### [2.4.2] - 2019-08-01
- 增加错误日志上传功能
- 提供关闭日志上传功能接口（默认打开）
### [2.4.1] - 2019-07-25
- 扩大橡皮擦响应范围
- 优化重连逻辑
- 增加白板本地背景色支持
- 优化断线重连功能
### [2.4.0] - 2019-07-18
- 获取状态 API，增加同步接口
### [2.3.5] - 2019-07-17
- 适配服务器端动态 PPT，动态 ppt 客户请升级

### [2.3.4] - 2019-07-12
- 适配服务器端动态转换新 API

### [2.3.2] - 2019-07-09
- 更新视角移动，视觉矩形移动 API参数类型
- 优化动态 PPT

### [2.3.0] - 2019-07-04
- 增加截图 API
- 增加根据 index 切换场景 API

### [2.2.1] - 2019-07-04
- 修复 PPT 转换工具初始化错误

### [2.2.0] - 2019-07-02
- 添加 PPT 转换支持
- 添加动态 PPT 控制API
- 添加视角移动，视角调整 API

### [2.0.4] - 2019-06-24
- 恢复只读 API（后续将拆分为两个 API）
### [2.0.3] - 2019-06-24
- 兼容旧版本静态 ppt 回放

### [2.0.0] - 2019-06-23

#### 兼容性变化
与之前版本 API 兼容，但是无法与低版本互连，进入同一房间。
可以与 iOS 2.1.0，web 2.0.0 正式版互连，无法与 iOS 2.1.0 以下版本，以及 web 2.0.0-beta 开头的版本互连。

>2019.06.24 前接入的客户，在升级至该版本时，请联系 SDK 团队，确认服务器指向版本。  
>更多内容，请查看 [2.0.0正式版发布](/blog/2019/06/22/release-note)