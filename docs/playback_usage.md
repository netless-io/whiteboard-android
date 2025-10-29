# 白板回放使用文档

## 概述

White SDK Android 支持白板回放功能，允许回放互动白板房间中的所有操作。支持纯白板回放和白板与音视频同步回放两种模式。自 2.16 版本起，回放功能还支持多窗口模式，可以完整回放多窗口场景。

## 基本配置

### 1. 初始化 SDK

```java
// 创建 SDK 配置
WhiteSdkConfiguration configuration = new WhiteSdkConfiguration(appId, true);

// 创建 SDK 实例
WhiteSdk whiteSdk = new WhiteSdk(whiteboardView, context, configuration);
```

### 2. 创建回放配置

```java
// 创建回放配置
PlayerConfiguration playerConfiguration = new PlayerConfiguration(roomUuid, roomToken);

// 可选：设置回放时长（单位：毫秒）
playerConfiguration.setDuration(60000L);

// 可选：设置回放起始时间，需保证回放起始时间在回放房间的开始时间之后
playerConfiguration.setBeginTimestamp(1615370614269L);
```

### 3. 创建回放播放器

```java
whiteSdk.createPlayer(playerConfiguration, playerListener, new Promise<Player>() {
    @Override
    public void then(Player player) {
        // 回放创建成功
        mPlayer = player;
        player.seekToScheduleTime(0);
        player.play();
    }

    @Override
    public void catchEx(SDKError error) {
        // 回放创建失败
        Log.e(TAG, "创建回放失败: " + error.getMessage());
    }
});
```

## 多窗口回放配置

### 1. 启用多窗口回放

要回放多窗口场景，需要在 SDK 配置和回放配置中都启用多窗口支持：

```java
// 1. SDK 配置中启用多窗口
WhiteSdkConfiguration configuration = new WhiteSdkConfiguration(appId, true);
configuration.setUseMultiViews(true);  // 启用多窗口功能

WhiteSdk whiteSdk = new WhiteSdk(whiteboardView, context, configuration);

// 2. 回放配置中设置窗口参数
PlayerConfiguration playerConfiguration = new PlayerConfiguration(roomUuid, roomToken);

// 创建窗口参数
WindowParams windowParams = new WindowParams();
windowParams.setContainerSizeRatio(9f / 16);   // 设置窗口比例
windowParams.setChessboard(false);             // 是否显示棋盘背景
windowParams.setDebug(false);                  // 是否显示调试信息

// 应用窗口参数到回放配置
playerConfiguration.setWindowParams(windowParams);

// 3. 创建回放播放器
whiteSdk.createPlayer(playerConfiguration, playerListener, playerPromise);
```

### 2. WindowParams 详细配置

`WindowParams` 类用于配置多窗口回放的显示参数, 保证配置与房间运行时一致：

```java
WindowParams windowParams = new WindowParams();
windowParams.setContainerSizeRatio(9f / 16);
// 设置自定义样式
HashMap<String, String> collectorStyles = new HashMap<>();
collectorStyles.put("backgroundColor", "#f0f0f0");
collectorStyles.put("borderRadius", "8px");
windowParams.setCollectorStyles(collectorStyles);
playerConfiguration.setWindowParams(windowParams);
```

## 回放控制

### 1. 播放控制

```java
// 开始播放
player.play();

// 暂停播放
player.pause();

// 停止播放（释放资源）
player.stop();

// 跳转到指定位置（单位：毫秒）
player.seekToScheduleTime(5000L);
```

### 2. 播放速度控制

```java
// 设置播放速度（1.0 为原速，2.0 为 2 倍速）
player.setPlaybackSpeed(2.0);

// 获取当前播放速度（同步方法）
double speed = player.getPlaybackSpeed();

// 获取当前播放速度（异步方法）
player.getPlaybackSpeed(new Promise<Double>() {
    @Override
    public void then(Double speed) {
        Log.i(TAG, "当前播放速度: " + speed);
    }
    
    @Override
    public void catchEx(SDKError error) {
        // 获取失败
    }
});
```

### 3. 获取播放状态

```java
// 获取播放阶段（同步方法）
PlayerPhase phase = player.getPlayerPhase();

// 获取播放阶段（异步方法）
player.getPhase(new Promise<PlayerPhase>() {
    @Override
    public void then(PlayerPhase phase) {
        Log.i(TAG, "当前阶段: " + phase);
    }
    
    @Override
    public void catchEx(SDKError error) {
        // 获取失败
    }
});

// 获取播放器状态（同步方法）
PlayerState state = player.getPlayerState();

// 获取播放器状态（异步方法）
player.getPlayerState(new Promise<PlayerState>() {
    @Override
    public void then(PlayerState state) {
        Log.i(TAG, "当前状态: " + state);
    }
    
    @Override
    public void catchEx(SDKError error) {
        // 获取失败
    }
});
```

### 4. 获取时间信息

```java
// 获取时间信息（同步方法）
PlayerTimeInfo timeInfo = player.getPlayerTimeInfo();
long scheduleTime = timeInfo.getScheduleTime();    // 当前播放位置
long timeDuration = timeInfo.getTimeDuration();    // 回放总时长
long beginTimestamp = timeInfo.getBeginTimestamp(); // 回放起始时间
int framesCount = timeInfo.getFramesCount();       // 总帧数

// 获取时间信息（异步方法）
player.getPlayerTimeInfo(new Promise<PlayerTimeInfo>() {
    @Override
    public void then(PlayerTimeInfo timeInfo) {
        Log.i(TAG, "播放进度: " + timeInfo.getScheduleTime());
    }
    
    @Override
    public void catchEx(SDKError error) {
        // 获取失败
    }
});
```

## 音视频同步回放

### 使用 PlayerSyncManager

```java
public class SyncPlaybackActivity extends AppCompatActivity {
    private PlayerSyncManager mPlayerSyncManager;
    private Player mWhitePlayer;
    private NativePlayer mMediaPlayer;
    
    private void setupSyncPlayer() {
        // 1. 创建原生媒体播放器
        mMediaPlayer = new WhiteExoPlayer(this);
        ((WhiteExoPlayer) mMediaPlayer).setVideoPath(videoUrl);
        
        // 2. 创建同步管理器
        mPlayerSyncManager = new PlayerSyncManager(mMediaPlayer, new PlayerSyncManager.Callbacks() {
            @Override
            public void startBuffering() {
                Log.d(TAG, "开始缓冲");
            }
            
            @Override
            public void endBuffering() {
                Log.d(TAG, "缓冲结束");
            }
        });
        
        // 3. 创建白板回放
        whiteSdk.createPlayer(playerConfiguration, this, new Promise<Player>() {
            @Override
            public void then(Player player) {
                mWhitePlayer = player;
                // 关联白板播放器到同步管理器
                mPlayerSyncManager.setWhitePlayer(player);
                
                // 开始同步播放
                player.seekToScheduleTime(0);
                mPlayerSyncManager.play();
            }
            
            @Override
            public void catchEx(SDKError error) {
                Log.e(TAG, "创建回放失败: " + error.getMessage());
            }
        });
    }
    
    @Override
    public void onPhaseChanged(PlayerPhase phase) {
        // 同步白板播放器的阶段变化
        if (mPlayerSyncManager != null) {
            mPlayerSyncManager.updateWhitePlayerPhase(phase);
        }
    }
    
    // 播放控制
    private void play() {
        if (mPlayerSyncManager != null) {
            mPlayerSyncManager.play();
        }
    }
    
    private void pause() {
        if (mPlayerSyncManager != null) {
            mPlayerSyncManager.pause();
        }
    }
}
```

## 高级配置

### 检查房间是否可回放

```java
// 在创建回放前检查房间是否有可用的回放数据
whiteSdk.isPlayable(playerConfiguration, new Promise<Boolean>() {
    @Override
    public void then(Boolean isPlayable) {
        if (isPlayable) {
            // 可以创建回放
            whiteSdk.createPlayer(playerConfiguration, listener, promise);
        } else {
            Log.w(TAG, "该房间没有可用的回放数据");
        }
    }
    
    @Override
    public void catchEx(SDKError error) {
        Log.e(TAG, "检查回放状态失败: " + error.getMessage());
    }
});
```

## 回放事件监听

### 1. PlayerListener 接口

```java
public interface PlayerListener {
    /**
     * 回放阶段变化回调
     * @param phase 当前阶段：
     *   - waitingFirstFrame: 等待第一帧
     *   - playing: 正在播放
     *   - pause: 已暂停
     *   - stopped: 已停止
     *   - ended: 已结束
     *   - buffering: 缓冲中
     */
    void onPhaseChanged(PlayerPhase phase);
    
    /**
     * 首帧加载完成回调
     */
    void onLoadFirstFrame();
    
    /**
     * 播放器状态变化回调
     * @param modifyState 变化的状态
     */
    void onPlayerStateChanged(PlayerState modifyState);
    
    /**
     * 播放错误回调
     * @param error 错误信息
     */
    void onStoppedWithError(SDKError error);
    
    /**
     * 播放进度变化回调（约每 500ms 回调一次）
     * @param time 当前播放时间（毫秒）
     */
    void onScheduleTimeChanged(long time);
    
    /**
     * 切片变化回调（一般不需要实现）
     * @param slice 切片标识
     */
    void onSliceChanged(String slice);
    
    /**
     * 追加帧错误回调
     * @param error 错误信息
     */
    void onCatchErrorWhenAppendFrame(SDKError error);
    
    /**
     * 渲染错误回调
     * @param error 错误信息
     */
    void onCatchErrorWhenRender(SDKError error);
}
```

### 2. 添加和移除监听器

```java
// 添加监听器
player.addPlayerListener(playerListener);

// 移除监听器
player.removePlayerListener(playerListener);
```

## 注意事项

### 通用注意事项

1. **同步与异步方法**：
   - 同步方法（如 `getPlayerPhase()`）返回本地缓存的值，可能不是最新值
   - 异步方法（如 `getPhase(promise)`）返回从 JS 端获取的最新值
   - 建议日常使用同步方法，调试时使用异步方法

2. **资源释放**：
   - 调用 `player.stop()` 后，Player 资源会被释放
   - 需要重新播放时必须重新创建 Player 实例

3. **线程安全**：
   - PlayerListener 的回调在子线程执行
   - UI 操作需要使用 `runOnUiThread()` 切换到主线程

## 常见问题

### Q1: 回放时窗口显示不正确或变形？

**A:** 检查以下配置：
```java
// 1. 确保启用了多窗口
configuration.setUseMultiViews(true);

// 2. 设置正确的窗口比例
WindowParams windowParams = new WindowParams();
windowParams.setContainerSizeRatio(16f / 9);
playerConfiguration.setWindowParams(windowParams);
```

## 相关类和接口

### 核心类

- **WhiteSdk**: SDK 主类，用于创建回放实例
- **Player**: 回放播放器类，控制回放的播放、暂停、跳转等
- **PlayerConfiguration**: 回放配置类，设置回放参数
- **WindowParams**: 多窗口参数类，配置多窗口回放的显示参数

### 监听器接口

- **PlayerListener**: 回放事件监听器

### 数据类

- **PlayerPhase**: 回放阶段枚举
- **PlayerState**: 回放状态类
- **PlayerTimeInfo**: 回放时间信息类

## 参考示例

- **纯白板回放**: `PureReplayActivity.java`
- **音视频同步回放**: `PlayActivity.java`
