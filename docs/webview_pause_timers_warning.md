# 关于 WebView.pauseTimers() 的重要警示说明

Whiteboard SDK 在 Android 平台上 **以 WebView 作为核心运行环境**。  
在部分使用场景中，不当调用 `WebView.pauseTimers()` 可能会导致白板功能异常，尤其是在 **多 WebView
或复杂生命周期管理** 的应用中。

本文档用于说明：

- `WebView.pauseTimers()` 的实际影响
- 常见风险场景与问题表现
- joinRoom 无回调时的排查方向
- 何时可以使用 pauseTimers 以及推荐的替代方案

请 **务必在接入 Whiteboard SDK 前完整阅读**。

## 1. Whiteboard SDK 与 WebView 的关系

Whiteboard SDK 的核心功能（包括但不限于）：

- `joinRoom` 房间接入
- 消息与状态同步
- 白板渲染与交互驱动

均运行在 **WebView 的 JavaScript 环境** 中，并依赖以下能力：

- JavaScript 执行线程
- JS → Native 桥接通信
- WebView 内部任务调度与事件循环

因此，**WebView 的运行状态会直接影响白板功能是否正常工作**。

## 2. WebView.pauseTimers() 的行为说明

### 2.1 pauseTimers() 的实际作用

`WebView.pauseTimers()` 是一个 **进程级（Process-wide）API**，其主要行为包括：

- 暂停当前进程内 **所有 WebView** 的 JavaScript 定时器
    - `setTimeout`
    - `setInterval`
    - `requestAnimationFrame`
- 不区分具体 WebView 实例
- 不抛出异常，也不会产生明显错误提示

在部分 Android / Chromium 版本中：

- 同步 JavaScript 代码仍可执行
- JS → Native 桥接调用可能仍然可用
- 但 **基于定时器或异步调度的逻辑会被挂起**

这使得问题 **非常隐蔽，且不易第一时间发现**。

### 2.2 常见误区

以下理解是 **不正确的**：

- `pauseTimers()` 只影响当前 WebView
- 调用后 WebView 会明显报错或崩溃
- 可以安全地在任意页面 onPause 时调用

实际上：

> **对任意一个 WebView 调用 pauseTimers()，都会影响当前进程内的所有 WebView。**

## 3. 多 WebView 场景下的风险说明

如果你的应用满足以下任意条件，请格外注意：

- 同一进程中存在 **多个 WebView**
- 某些页面为了省电或后台优化调用了 `WebView.pauseTimers()`
- Whiteboard WebView 与其他业务 WebView 共存

在这种情况下：

> 即使白板页面本身没有调用 pauseTimers()，  
> 也可能因为其他 WebView 的调用而受到影响。

这类问题通常表现为：

- 白板加载完成但无响应
- joinRoom 长时间无回调
- 无明显异常日志

## 4. joinRoom 无回调的排查建议

如果在 Android 接入过程中出现以下问题：

- 调用 `joinRoom` 后 **长时间没有回调**
- 未收到 error 回调
- WebView 页面可见，但白板功能不可用

请优先检查以下内容：

1. 应用中是否使用了 WebView
2. 是否在任意位置调用过：
    ```java
    WebView.pauseTimers();
    ````

3. 是否存在统一封装逻辑，例如：

    * 页面不可见即调用 pauseTimers
    * Activity / Fragment onPause 中调用 pauseTimers

   **这是 joinRoom 无回调的常见原因之一。**

## 5. 什么时候可以使用 WebView.pauseTimers()

### 相对合理的使用场景

* 应用整体进入后台
* 确认当前进程 **不再需要任何 WebView 的 JS 执行**
* 对应用内所有 WebView 行为有完全控制

### 不推荐使用的场景

* 应用仍在前台
* 存在多个 WebView
* WebView 承载实时或强交互功能，例如：

    * 白板
    * 音视频
    * IM / 实时协作
    * 在线编辑器

在上述场景中使用 pauseTimers，**风险极高**。

## 6. 推荐的替代方案（强烈建议）

在大多数情况下，**不需要使用 `pauseTimers()`**。

### 推荐方案 1：使用 WebView 生命周期方法

```java
webView.onPause();
webView.

onResume();
```

特点：

* 作用范围仅限当前 WebView
* 不影响其他 WebView
* 与页面生命周期语义一致
* 更安全、可控

## 7. Whiteboard SDK 的处理策略说明

Whiteboard SDK 内部：

* **不会主动调用** `WebView.pauseTimers()` 或 `WebView.resumeTimers()`
* 会检测 JS 与 Native 桥接是否可正常通信
* 在检测到异常时给出明确警告，辅助问题定位

SDK **不会自动调用 `resumeTimers()`**，以避免干预宿主应用的全局 WebView 状态。

---

## 8. 总结（请务必阅读）

* `WebView.pauseTimers()` 是一个 **全局影响、风险较高** 的 API
* 在多 WebView 场景中使用需格外谨慎
* 白板异常、joinRoom 无回调时，请优先检查该方法的使用情况
* **推荐使用 `WebView.onPause()` / `onResume()` 作为替代方案**

如有疑问，请联系 Whiteboard SDK 技术支持团队。