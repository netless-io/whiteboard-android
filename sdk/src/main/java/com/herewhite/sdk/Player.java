package com.herewhite.sdk;

import com.google.gson.JsonSyntaxException;
import com.herewhite.sdk.domain.EventEntry;
import com.herewhite.sdk.domain.EventListener;
import com.herewhite.sdk.domain.FrequencyEventListener;
import com.herewhite.sdk.domain.PlayerObserverMode;
import com.herewhite.sdk.domain.PlayerPhase;
import com.herewhite.sdk.domain.PlayerState;
import com.herewhite.sdk.domain.PlayerTimeInfo;
import com.herewhite.sdk.domain.Promise;
import com.herewhite.sdk.domain.SDKError;
import com.herewhite.sdk.internal.Logger;
import com.herewhite.sdk.internal.PlayerDelegate;

import wendu.dsbridge.OnReturnValue;

/**
 * 回放房间操作类
 */
public class Player extends Displayer {
    private SyncDisplayerState<PlayerState> syncPlayerState;

    private long scheduleTime = 0;
    private long timeDuration;
    private long beginTimestamp;
    private int framesCount;

    /**
     * 获取白板回放的倍速。该方法为同步调用。
     * <p>
     * 该方法获取的是播放倍速，如 1.0、1.5、2.0 倍速。因此回放暂停时，返回值也不会为 0。
     *
     * @return 白板回放的播放倍速。
     * @since 2.5.2
     */
    public double getPlaybackSpeed() {
        return playbackSpeed;
    }

    /**
     * 设置白板回放的倍速。
     *
     * @param playbackSpeed 白板回放的倍速。取值必须大于 0，设为 1 表示按原速播放。
     * @since 2.5.2
     */
    public void setPlaybackSpeed(double playbackSpeed) {
        this.playbackSpeed = playbackSpeed;
        bridge.callHandler("player.setPlaybackSpeed", new Object[]{playbackSpeed});
    }

    /**
     * 获取白板回放的倍速。该方法为异步调用。
     * <p>
     * 该方法获取的是播放倍速，如 1.0、1.5、2.0 倍速。因此回放暂停时，返回值也不会为 0。
     *
     * @param promise Promise<Double> 接口实例，详见 {@link Promise}。你可以通过该接口了解获取白板回放倍速的结果：
     *                - 如果获取成功，将返回白板回放的倍速。
     *                - 如果获取失败，将返回错误信息。
     * @note 该方位为异步调用。我们推荐你仅在调试或问题排查时使用。一般情况下可以使用同步方法 {@link #getPlaybackSpeed()} 进行获取。
     * @since 2.5.2
     */
    public void getPlaybackSpeed(final Promise<Double> promise) {
        bridge.callHandler("player.state.playbackSpeed", new OnReturnValue<Number>() {
            @Override
            public void onValue(Number value) {
                promise.then(value.doubleValue());
            }
        });
    }

    private double playbackSpeed;

    private PlayerPhase playerPhase = PlayerPhase.waitingFirstFrame;

    /**
     * 文档中隐藏，SDK 内部使用
     * Instantiates a new Player.
     *
     * @param room       回放房间 uuid
     * @param bridge     the bridge
     * @param densityDpi Android屏幕密度值
     */
    Player(String room, JsBridgeInterface bridge, int densityDpi) {
        super(room, bridge, densityDpi);
        syncPlayerState = new SyncDisplayerState(PlayerState.class, "{}", true);
        syncPlayerState.setListener(localPlayStateListener);
    }

    void setPlayerTimeInfo(PlayerTimeInfo playerTimeInfo) {
        this.scheduleTime = playerTimeInfo.getScheduleTime();
        this.timeDuration = playerTimeInfo.getTimeDuration();
        this.framesCount = playerTimeInfo.getFramesCount();
        this.beginTimestamp = playerTimeInfo.getBeginTimestamp();
    }

    /**
     * 开始白板回放。
     * <p>
     * 暂停回放后，也可以调用该方法继续白板回放。
     */
    public void play() {
        bridge.callHandler("player.play", new Object[]{});
    }

    /**
     * 暂停白板回放。
     */
    public void pause() {
        bridge.callHandler("player.pause", new Object[]{});
    }

    /**
     * 停止白板回放。
     * <p>
     * 白板回放停止后，Player 资源会被释放。如果想要重新播放，则需要重新初始化 Player 实例。
     */
    public void stop() {
        bridge.callHandler("player.stop", new Object[]{});
    }

    /**
     * 设置白板回放的开始时间。
     * <p>
     * 由于 SDK 会录制实时房间的全部过程，因此默认情况下，回放会播放从房间构造开始直到最后一次活跃结束的全部过程。
     * 因此在进行回放时，需要调用该方法设置开始回放的时间点。
     *
     * @param seekTime 白板回放的开始时间，单位为毫秒。
     */
    public void seekToScheduleTime(long seekTime) {
        bridge.callHandler("player.seekToScheduleTime", new Object[]{seekTime});
    }

    /**
     * 设置白板回放的查看模式。
     *
     * @param mode 白板回放的查看模式，详见 {@link PlayerObserverMode}。
     */
    public void setObserverMode(PlayerObserverMode mode) {
        bridge.callHandler("player.setObserverMode", new Object[]{mode.name()});
    }

    //region Get API

    /**
     * 获取白板回放房间的阶段。该方法为同步调用。
     * <p>
     * 在 Player 生命周期内，你可以调用该方法获取当前回放房间的阶段。其中初始阶段为 `waitingFirstFrame`，表示正在等待白板回放的第一帧。
     *
     * @return 回放房间的阶段，详见 {@link PlayerPhase}。
     * @note 成功调用 {@link #stop()}、{@link #play()}、{@link #pause()} 等方法均会影响回放房间的阶段，但是该阶段不会立即更新。
     * @since 2.4.0
     */
    public PlayerPhase getPlayerPhase() {
        return this.playerPhase;
    }

    /**
     * 获取白板回放房间的阶段。该方法为异步调用。
     * <p>
     * 在 Player 生命周期内，你可以调用该方法获取当前回放房间的阶段。其中初始状态为 `waitingFirstFrame`，表示正在等待白板回放的第一帧。
     *
     * @param promise `Promise<PlayerPhase>` 接口实例，详见 {@link Promise 类}。你可以通过该接口了解获取白板回放阶段的结果：
     *                - 如果获取成功，将返回白板回放的阶段。
     *                - 如果获取失败，将返回错误信息。
     * @note - 成功调用 {@link #stop()}、{@link #play()}、{@link #pause()} 等方法均会影响回放房间的阶段，但是该阶段不会立即更新。
     * - 该方位为异步调用。我们推荐你仅在调试或问题排查时使用。一般情况下可以使用同步方法 {@link #getPlayerPhase()} 进行获取。
     */
    public void getPhase(final Promise<PlayerPhase> promise) {
        bridge.callHandler("player.getBroadcastState", new Object[]{}, new OnReturnValue<Object>() {
            @Override
            public void onValue(Object o) {
                try {
                    promise.then(PlayerPhase.valueOf(String.valueOf(o)));
                } catch (AssertionError a) {
                    throw a;
                } catch (JsonSyntaxException e) {
                    Logger.error("An JsonSyntaxException occurred while parse json from getPhase", e);
                    promise.catchEx(new SDKError(e.getMessage()));
                } catch (Throwable e) {
                    Logger.error("An exception occurred in getPhase promise then method", e);
                    promise.catchEx(new SDKError(e.getMessage()));
                }
            }
        });
    }

    /**
     * 获取回放房间的状态。该方法为同步调用。
     * <p>
     * 如果回放房间的状态 Player phase 为 `waitingFirstFrame`，则该方法返回 Null。
     *
     * @return 回放房间状态，详见 {@link PlayerState}。
     * @since 2.4.0
     */
    public PlayerState getPlayerState() {
        if (playerPhase == PlayerPhase.waitingFirstFrame) {
            return null;
        }
        return this.syncPlayerState.getDisplayerState();
    }

    /**
     * 获取白板回放房间的状态。该方法为异步调用。
     *
     * @param promise `Promise<PlayerState>` 接口实例，详见 {@link Promise}。你可以通过该接口了解获取白板回放状态的结果：
     *                - 如果获取成功，将返回具体的白板回放状态。
     *                - 如果获取失败，将返回错误信息。
     * @note 该方位为异步调用。我们推荐你仅在调试或问题排查时使用。一般情况下可以使用同步方法 {@link #getPlayerState()} 进行获取。
     */
    public void getPlayerState(final Promise<PlayerState> promise) {
        bridge.callHandler("player.state.playerState", new Object[]{}, new OnReturnValue<Object>() {
            @Override
            public void onValue(Object o) {
                try {
                    PlayerState playerState = gson.fromJson(String.valueOf(o), PlayerState.class);
                    promise.then(playerState);
                } catch (AssertionError a) {
                    throw a;
                } catch (JsonSyntaxException e) {
                    Logger.error("An JsonSyntaxException occurred while parse json from getPlayerState", e);
                    promise.catchEx(new SDKError(e.getMessage()));
                } catch (Throwable e) {
                    Logger.error("An exception occurred in getPlayerState promise then method", e);
                    promise.catchEx(new SDKError(e.getMessage()));
                }
            }
        });
    }

    /**
     * 获取白板回放的时间信息，该方法为同步调用。
     * <p>
     * 该方法获取的时间信息，包含当前的播放时间，回放文件的总时长，以及开始播放的 UTC 时间戳，单位为毫秒。
     *
     * @return 播放时间信息，详见 {@link PlayerTimeInfo}。
     * @note 该方法获取的当前播放时间可能不准确。
     * @since 2.4.0
     */
    public PlayerTimeInfo getPlayerTimeInfo() {
        return new PlayerTimeInfo(this.scheduleTime, this.timeDuration, this.framesCount, this.beginTimestamp);
    }

    /**
     * 获取白板回放的时间信息，该方法为异步调用。
     * <p>
     * 该方法获取的时间信息，包含当前的播放时间，回放文件的总时长，以及开始播放的 UTC 时间戳，单位为毫秒。
     *
     * @param promise `Promise<PlayerTimeInfo>` 接口实例，详见 {@link Promise}。你可以通过该接口了解获取白板回放时间信息的结果：
     *                - 如果获取成功，将返回白板回放的时间信息。
     *                - 如果获取失败，将返回错误信息。
     * @note 该方法为异步调用。我们推荐你仅在调试或问题排查时使用。一般情况下可以使用同步方法 {@link #getPlayerTimeInfo() getPlayerTimeInfo} 进行获取。
     */
    public void getPlayerTimeInfo(final Promise<PlayerTimeInfo> promise) {
        bridge.callHandler("player.state.timeInfo", new Object[]{}, new OnReturnValue<Object>() {
            @Override
            public void onValue(Object o) {
                promise.then(getPlayerTimeInfo());
            }
        });
    }
    //endregion

    // region PlayerListener
    private PlayerListener listener;

    void setPlayerEventListener(PlayerListener playerEventListener) {
        this.listener = playerEventListener;
    }
    // endregion

    private PlayerDelegate playerDelegate;

    PlayerDelegate getDelegate() {
        if (playerDelegate == null) {
            playerDelegate = new PlayerDelegateImpl();
        }
        return playerDelegate;
    }

    private SyncDisplayerState.Listener<PlayerState> localPlayStateListener = modifyState -> {
        post(() -> {
            if (listener != null) {
                listener.onPlayerStateChanged(modifyState);
            }
        });
    };


    private class PlayerDelegateImpl implements PlayerDelegate {
        @Override
        public void fireMagixEvent(EventEntry eventEntry) {
            post(() -> {
                EventListener eventListener = eventListenerMap.get(eventEntry.getEventName());
                if (eventListener != null) {
                    eventListener.onEvent(eventEntry);
                }
            });
        }

        @Override
        public void fireHighFrequencyEvent(EventEntry[] eventEntries) {
            post(() -> {
                FrequencyEventListener eventListener = frequencyEventListenerMap.get(eventEntries[0].getEventName());
                if (eventListener != null) {
                    eventListener.onEvent(eventEntries);
                }
            });
        }

        @Override
        public void setPlayerPhase(PlayerPhase playerPhase) {
            Player.this.playerPhase = playerPhase;
            post(() -> {
                if (listener != null) {
                    listener.onPhaseChanged(playerPhase);
                }
            });
        }

        @Override
        public void onLoadFirstFrame() {
            post(() -> {
                if (listener != null) {
                    listener.onLoadFirstFrame();
                }
            });
        }

        @Override
        public void onSliceChanged(String slice) {
            post(() -> {
                if (listener != null) {
                    listener.onSliceChanged(slice);
                }
            });
        }

        @Override
        public void syncDisplayerState(String stateJSON) {
            if (syncPlayerState != null) {
                syncPlayerState.syncDisplayerState(stateJSON);
            }
        }

        @Override
        public void onStoppedWithError(SDKError error) {
            post(() -> {
                if (listener != null) {
                    listener.onStoppedWithError(error);
                }
            });
        }

        @Override
        public void setScheduleTime(long scheduleTime) {
            Player.this.scheduleTime = scheduleTime;
            post(() -> {
                if (listener != null) {
                    listener.onScheduleTimeChanged(scheduleTime);
                }
            });
        }

        @Override
        public void onCatchErrorWhenAppendFrame(SDKError error) {
            post(() -> {
                if (listener != null) {
                    listener.onCatchErrorWhenAppendFrame(error);
                }
            });
        }

        @Override
        public void onCatchErrorWhenRender(SDKError error) {
            post(() -> {
                if (listener != null) {
                    listener.onCatchErrorWhenRender(error);
                }
            });
        }
    }
}
