package com.herewhite.sdk;

import android.content.Context;

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

import java.util.concurrent.ConcurrentHashMap;

import wendu.dsbridge.OnReturnValue;

/**
 * 回放房间操作类
 */
public class Player extends Displayer {
    private final ConcurrentHashMap<String, EventListener> eventListenerConcurrentHashMap = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, FrequencyEventListener> frequencyEventListenerConcurrentHashMap = new ConcurrentHashMap<>();

    private final SyncDisplayerState<PlayerState> syncPlayerState;

    private final long timeDuration;
    private final long beginTimestamp;
    private final int framesCount;

    /**
     * 获取播放时的播放速率
     *
     * @return 播放速率
     * @since 2.5.2
     */
    public double getPlaybackSpeed() {
        return playbackSpeed;
    }

    /**
     * 设置播放时的播放速率
     *
     * @param playbackSpeed
     * @since 2.5.2
     */
    public void setPlaybackSpeed(double playbackSpeed) {
        this.playbackSpeed = playbackSpeed;
        bridge.callHandler("player.setPlaybackSpeed", new Object[]{playbackSpeed});
    }

    /**
     * 异步从 player 中获取播放时的播放速率，暂停时不会变为 0。
     * 正常情况下，不需要使用该 API，仅做 Debug 与测试用
     *
     * @param promise
     * @since 2.5.2
     */
    public void getPlaybackSpeed(final Promise<Double> promise) {
        bridge.callHandler("player.state.playbackSpeed", new OnReturnValue<Double>() {
            @Override
            public void onValue(Double retValue) {
                promise.then(retValue);
            }
        });
    }

    private double playbackSpeed;

    private long scheduleTime = 0;

    private PlayerPhase playerPhase = PlayerPhase.waitingFirstFrame;

    /**
     * Instantiates a new Player.
     *
     * @param room            回放房间 uuid
     * @param bridge          the bridge
     * @param context         the context
     * @param whiteSdk        the white sdk
     * @param playerTimeInfo  the player time info
     * @param syncPlayerState the sync player state
     */
    public Player(String room, JsBridgeInterface bridge, Context context, WhiteSdk whiteSdk, PlayerTimeInfo playerTimeInfo, SyncDisplayerState<PlayerState> syncPlayerState) {
        super(room, bridge, context, whiteSdk);
        this.syncPlayerState = syncPlayerState;
        this.timeDuration = playerTimeInfo.getTimeDuration();
        this.framesCount = playerTimeInfo.getFramesCount();
        this.beginTimestamp = playerTimeInfo.getBeginTimestamp();
    }

    SyncDisplayerState<PlayerState> getSyncPlayerState() {
        return syncPlayerState;
    }

    void setPlayerPhase(PlayerPhase playerPhase) {
        this.playerPhase = playerPhase;
    }

    void setScheduleTime(long scheduleTime) {
        this.scheduleTime = scheduleTime;
    }

    public void play() {
        bridge.callHandler("player.play", new Object[]{});
    }

    public void pause() {
        bridge.callHandler("player.pause", new Object[]{});
    }

    /**
     * stop 后，player 资源会被释放。需要重新创建WhitePlayer实例，才可以重新播放
     */
    public void stop() {
        bridge.callHandler("player.stop", new Object[]{});
    }

    /**
     * 跳转至特定时间，开始时间为 0，单位毫秒。
     *
     * @param seekTime 跳转时间
     */
    public void seekToScheduleTime(long seekTime) {
        bridge.callHandler("player.seekToScheduleTime", new Object[]{seekTime});
    }

    //region Event API
    void fireMagixEvent(EventEntry eventEntry) {
        EventListener eventListener = eventListenerConcurrentHashMap.get(eventEntry.getEventName());
        if (eventListener != null) {
            try {
                eventListener.onEvent(eventEntry);
            } catch (Throwable e) {
                Logger.error("An exception occurred while sending the event", e);
            }
        }
    }

    void fireHighFrequencyEvent(EventEntry[] eventEntries) {
        FrequencyEventListener eventListener = frequencyEventListenerConcurrentHashMap.get(eventEntries[0].getEventName());
        if (eventListener != null) {
            try {
                eventListener.onEvent(eventEntries);
            } catch (AssertionError a) {
                throw a;
            } catch (Throwable e) {
                Logger.error("An exception occurred while sending the event", e);
            }
        }
    }

    //endregion

    /**
     * 设置查看模式
     *
     * @param mode {@link PlayerObserverMode}
     */
    public void setObserverMode(PlayerObserverMode mode) {
        bridge.callHandler("player.setObserverMode", new Object[]{mode.name()});
    }

    //region Get API

    /**
     * 同步缓存API 获取回放房间播放状态
     * 初始状态为 WhitePlayerPhaseWaitingFirstFrame.
     * 如果操作 {@link #stop()} {@link #play()} {@link #stop()} 等影响 playerPhase API，该 API 不会立即更新
     *
     * @return {@link PlayerPhase} 回放房间状态
     * @since 2.4.0
     */
    public PlayerPhase getPlayerPhase() {
        return this.playerPhase;
    }

    /**
     * 异步API 获取回放房间播放状态
     * <p>
     * 一般情况，可以使用 {@link #getPlayerPhase()} 同步API，进行获取。
     * 如果操作 {@link #stop()} {@link #play()} {@link #stop()} 等影响 playerPhase API，可以使用该 API
     *
     * @param promise the promise
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
     * 同步API 获取回放房间中所有状态
     * <p>
     * 当 phase 状态为 WhitePlayerPhaseWaitingFirstFrame
     * 回调得到的数据为 null
     *
     * @return 回放房间状态 {@link PlayerState}
     * @since 2.4.0
     */
    public PlayerState getPlayerState() {
        if (playerPhase == PlayerPhase.waitingFirstFrame) {
            return null;
        }
        return this.syncPlayerState.getDisplayerState();
    }

    /**
     * 异步API 获取回放房间中状态
     * <p>
     * 一般情况，请使用 {@link #getPlayerState()} 同步 API，进行获取。
     *
     * @param promise 完成回调
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
     * 同步API 获取播放文件信息
     * <p>
     * 当前时间，总时长，开始 UTC 时间戳。单位：毫秒
     * 当前时间不准确
     *
     * @return {@link PlayerTimeInfo}
     * @since 2.4.0
     */
    public PlayerTimeInfo getPlayerTimeInfo() {
        return new PlayerTimeInfo(this.scheduleTime, this.timeDuration, this.framesCount, this.beginTimestamp);
    }

    /**
     * 异步API 获取播放文件信息
     * <p>
     * 一般情况，请使用 {@link #getPlayerTimeInfo()} 同步 API，进行获取。
     *
     * @param promise 完成回调
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
}
