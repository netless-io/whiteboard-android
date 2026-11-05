# 本地 Worker 分包验证

Debug Example 的 `com.herewhite.demo.test.window.WorkerProbeActivity` 通过真实 Native SDK 只读入房。
Release Example 不包含该 Activity；SDK 仍使用 `file:///android_asset/whiteboard/index.html`。

启动时传 `workerMode`：`simple`（默认）、`dual` 或 `mainThread`。房间信息可通过 `roomUuid` / `roomToken`
启动参数传入，也可使用被 Git 忽略的 `string_white_sdk_config.xml`，不要将实际 token 写入源码或命令历史。

初始化后点击“加入（只读）”与“退出”，也可向同一个 Activity 发送 `workerAction=join` / `leave`。
切换模式需要先结束 Activity，再以新参数启动，确保测试的是新页面，避免模块和 Blob 缓存影响数量。

| 模式 | Native extras | 首次入房需要的教具源码分包 / Blob |
| --- | --- | --- |
| simple | useWorker=auto, useSimple=true | Full / 1 |
| dual | useWorker=auto, useSimple=false | Full + Sub / 2 |
| mainThread | useWorker=mainThread, useSimple=true | 无 / 0 |

三个模式均显式开启 allowImageBitmapFallback。auto 的探针或回退可能终止 Worker，
因此要分别观察“已加载分包/已创建 Blob”和“稳定后的活跃 Worker”；不能用活跃数量反推资源加载量。
初始化时不应加载两个教具分包。退房会终止 Worker，但已加载的模块和 Blob URL 按页面缓存。

`window.__androidWorkerProbe` 和 `WhiteWorkerProbe` 日志只提供模式与 Native 事件；
具体资源请求、Blob 大小及 Worker 消息可在 WebView 调试通道观察。该 Example 不向 SDK 内嵌 JS 注入探针。
