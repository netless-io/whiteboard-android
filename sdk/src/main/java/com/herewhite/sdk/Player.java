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
import com.herewhite.sdk.domain.WindowParams;
import com.herewhite.sdk.internal.Logger;
import com.herewhite.sdk.internal.PlayerDelegate;

import wendu.dsbridge.OnReturnValue;

/**
 * `Player` 类，用于操作白板的回放。
 */
public class Player extends Displayer {
    private SyncDisplayerState<PlayerState> syncPlayerState;

    private long scheduleTime = 0;
    private long timeDuration;
    private long beginTimestamp;
    private int framesCount;

    /**
     * 获取白板回放的倍速。
     *
     * @since 2.5.2
     *
     * @note
     * - 该方法为同步调用。
     * - 该方法获取的是播放倍速。例如，当返回值为 `2.0` 时，表示当前得播放速度是原速的 2 倍。
     * - 回放暂停时，返回值也不会为 0。
     *
     * @return 白板回放的播放倍速。
     */
    public double getPlaybackSpeed() {
        return playbackSpeed;
    }

    /**
     * 设置白板回放的倍速。
     *
     * @since 2.5.2
     *
     * @param playbackSpeed 白板回放的倍速。取值必须大于 0，设为 1 表示按原速播放。
     *
     */
    public void setPlaybackSpeed(double playbackSpeed) {
        this.playbackSpeed = playbackSpeed;
        bridge.callHandler("player.setPlaybackSpeed", new Object[]{playbackSpeed});
    }

    /**
     * 获取白板回放的倍速。
     *
     * @since 2.5.2
     *
     * @note
     * - 该方位为异步调用。我们推荐你仅在调试或问题排查时使用。一般情况下可以使用同步方法 {@link #getPlaybackSpeed() getPlaybackSpeed}[1/2] 获取播放速度。
     * - 该方法获取的是播放倍速。例如，当返回值为 `2.0` 时，表示当前得播放速度是原速的 2 倍。
     * - 回放暂停时，返回值也不会为 0。
     *
     * @param promise Promise<Double> 接口实例，详见 {@link com.herewhite.sdk.domain.Promise Promise}。你可以通过该接口获取 `getPlaybackSpeed` 的调用结果：
     * - 如果获取成功，将返回白板回放的倍速。
     * - 如果获取失败，将返回错误信息。
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

    /// @cond test
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
        syncPlayerState = new SyncDisplayerState(PlayerState.class, true);
        syncPlayerState.setListener(localPlayStateListener);
    }
    /// @endcond

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
     * 白板回放停止后，`Player` 资源会被释放。如果想要重新播放，需要重新初始化 `Player` 实例。
     */
    public void stop() {
        bridge.callHandler("player.stop", new Object[]{});
    }

    /**
     * 设置白板回放的播放位置。
     *
     * 白板回放的起始时间点为 0，成功调用该方法后，白板回放会在指定位置开始播放。
     *
     * @param seekTime 播放进度，单位为毫秒。
     */
    public void seekToScheduleTime(long seekTime) {
        bridge.callHandler("player.seekToScheduleTime", new Object[]{seekTime});
    }

    /**
     * 设置白板回放的查看模式。
     *
     * @param mode 白板回放的查看模式，详见 {@link com.herewhite.sdk.domain.PlayerObserverMode PlayerObserverMode}。
     */
    public void setObserverMode(PlayerObserverMode mode) {
        bridge.callHandler("player.setObserverMode", new Object[]{mode.name()});
    }

    //region Get API

    /**
     * 获取白板回放的阶段。
     *
     * @since 2.4.0
     *
     * 在 `Player` 生命周期内，你可以调用该方法获取白板回放当前所处的阶段。
     *
     * @note
     * - 该方法为同步调用。
     * - 成功调用 {@link #stop() stop}、{@link #play() play} 或 {@link #pause() pause} 等方法均会影响白板回放的阶段，但是通过该方法无法立即获取最新的白板回放阶段。
     * 此时，你可以调用 {@link getPhase(final Promise<PlayerPhase> promise) getPhase} 获取最新的回放阶段。
     *
     * @return 白板回放的阶段，详见 {@link com.herewhite.sdk.domain.PlayerPhase PlayerPhase}。
     *
     */
    public PlayerPhase getPlayerPhase() {
        return this.playerPhase;
    }

    /**
     * 获取白板回放的阶段。
     *
     * 在 `Player` 生命周期内，你可以调用该方法获取白板回放当前所处的阶段。
     *
     * @note
     * - 该方法为异步调用。我们推荐你仅在调试或问题排查时使用。一般情况下可以使用同步方法 {@link #getPlayerPhase() getPlayerPhase} 获取回放阶段。
     * - 成功调用 {@link #stop() stop}、{@link #play() play} 或 {@link #pause() pause} 等方法后，你无法通过 {@link #getPlayerPhase() getPlayerPhase} 立即获取最新的白板回放阶段。
     * 此时，你可以调用 {@link getPhase(final Promise<PlayerPhase> promise) getPhase}。
     *
     * @param promise `Promise<PlayerPhase>` 接口实例，详见 {@link com.herewhite.sdk.domain.Promise Promise}。你可以通过该接口获取 `getPhase` 方法的调用结果：
     *                - 如果方法调用成功，将返回白板回放的阶段。
     *                - 如果方法调用失败，将返回错误信息。
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
     * 获取白板回放的状态。
     *
     * @since 2.4.0
     *
     * @note
     * - 该方法为同步调用。
     * - 如果白板回放处于 `waitingFirstFrame` 阶段，则该方法返回 `null`。
     *
     * @return 白板回放的状态，详见 {@link com.herewhite.sdk.domain.PlayerState PlayerState}。
     *
     */
    public PlayerState getPlayerState() {
        if (playerPhase == PlayerPhase.waitingFirstFrame) {
            return null;
        }
        return this.syncPlayerState.getDisplayerState();
    }

    /**
     * 获取白板回放的状态。
     *
     * @note
     * - 该方法为异步调用。我们推荐你仅在调试或问题排查时使用。一般情况下可以使用同步方法 {@link #getPlayerState() getPlayerState}[1/2] 获取。
     * - 如果白板回放处于 `waitingFirstFrame` 阶段，则该方法返回 `null`。
     *
     * @param promise `Promise<PlayerState>` 接口实例，详见 {@link com.herewhite.sdk.domain.Promise Promise}。你可以通过该接口获取 `getPlayerState` 方法调用的结果：
     *                - 如果方法调用成功，将返回白板回放状态，详见 {@link com.herewhite.sdk.domain.PlayerState PlayerState}。
     *                - 如果方法调用失败，将返回错误信息。
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
     * 获取白板回放的时间信息。
     *
     * @since 2.4.0
     *
     * 该方法获取的时间信息，包含当前的播放进度，回放的总时长，以及回放的起始时间，单位为毫秒。
     *
     * @note
     * - 该方法为同步调用。
     * - 该方法获取的当前播放进度可能不准确。
     *
     * @return 白板回放的时间信息，详见 {@link com.herewhite.sdk.domain.PlayerTimeInfo PlayerTimeInfo}。
     */
    public PlayerTimeInfo getPlayerTimeInfo() {
        return new PlayerTimeInfo(this.scheduleTime, this.timeDuration, this.framesCount, this.beginTimestamp);
    }

    /**
     * 获取白板回放的时间信息，该方法为异步调用。
     * <p>
     * 该方法获取的时间信息，包含当前的播放进度，回放的总时长，以及回放的起始时间，单位为毫秒。
     *
     * @note 该方法为异步调用。我们推荐你仅在调试或问题排查时使用。一般情况下可以使用同步方法 {@link #getPlayerTimeInfo() getPlayerTimeInfo} 进行获取。
     *
     * @param promise `Promise<PlayerTimeInfo>` 接口实例，详见 {@link com.herewhite.sdk.domain.Promise Promise}。你可以通过该接口获取 `getPlayerTimeInfo` 方法的调用结果：
     *                - 如果方法调用成功，将返回白板回放的时间信息，详见 {@link com.herewhite.sdk.domain.PlayerTimeInfo PlayerTimeInfo}。
     *                - 如果方法调用失败，将返回错误信息。
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
