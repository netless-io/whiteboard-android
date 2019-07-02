package com.herewhite.sdk;

import android.content.Context;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.herewhite.sdk.domain.EventEntry;
import com.herewhite.sdk.domain.EventListener;
import com.herewhite.sdk.domain.PlayerObserverMode;
import com.herewhite.sdk.domain.PlayerPhase;
import com.herewhite.sdk.domain.PlayerState;
import com.herewhite.sdk.domain.PlayerTimeInfo;
import com.herewhite.sdk.domain.Promise;
import com.herewhite.sdk.domain.SDKError;

import java.util.concurrent.ConcurrentHashMap;

import wendu.dsbridge.OnReturnValue;

public class Player extends Displayer {

    private ConcurrentHashMap<String, EventListener> eventListenerConcurrentHashMap = new ConcurrentHashMap<>();

    public Player(String room, WhiteBroadView bridge, Context context, WhiteSdk whiteSdk) {
        super(room, bridge, context, whiteSdk);
    }

    public void play() {
        bridge.callHandler("player.play", new Object[]{});
    }

    public void pause() {
        bridge.callHandler("player.pause", new Object[]{});
    }

    //stop 后，player 资源会被释放。需要重新创建WhitePlayer实例，才可以重新播放
    public void stop() {
        bridge.callHandler("player.stop", new Object[]{});
        this.sdk.releasePlayer(uuid);
    }

    //跳转至特定时间，开始时间为 0，单位毫秒
    public void seekToScheduleTime(long beginTime) {
        bridge.callHandler("player.seekToScheduleTime", new Object[]{beginTime});
    }

    //region Event API
    public void fireMagixEvent(EventEntry eventEntry) {
        EventListener eventListener = eventListenerConcurrentHashMap.get(eventEntry.getEventName());
        if (eventListener != null) {
            try {
                eventListener.onEvent(eventEntry);
            } catch (Throwable e) {
                Logger.error("An exception occurred while sending the event", e);
            }
        }
    }

    public void removeMagixEventListener(String eventName) {
        this.eventListenerConcurrentHashMap.remove(eventName);
        bridge.callHandler("player.removeMagixEventListener", new Object[]{eventName});
    }

    public void addMagixEventListener(String eventName, EventListener eventListener) {
        this.eventListenerConcurrentHashMap.put(eventName, eventListener);
        bridge.callHandler("player.addMagixEventListener", new Object[]{eventName});
    }
    //endregion

    //设置查看模式
    public void setObserverMode(PlayerObserverMode mode) {
        bridge.callHandler("player.setObserverMode", new Object[]{mode.name()});
    }

    //region Get API
    /**
     * 获取房间状态
     * 目前：初始状态为 WhitePlayerPhaseWaitingFirstFrame
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
     * 当 phase 状态为 WhitePlayerPhaseWaitingFirstFrame
     * 回调得到的数据是空的
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

    /** 获取播放器信息（当前时长，总时长，开始 UTC 时间戳）单位：毫秒 */
    public void getPlayerTimeInfo(final Promise<PlayerTimeInfo> promise) {
        bridge.callHandler("player.state.timeInfo", new Object[]{}, new OnReturnValue<Object>() {
            @Override
            public void onValue(Object o) {
                try {
                    PlayerTimeInfo playerState = gson.fromJson(String.valueOf(o), PlayerTimeInfo.class);
                    promise.then(playerState);
                } catch (AssertionError a) {
                    throw a;
                } catch (JsonSyntaxException e) {
                    Logger.error("An JsonSyntaxException occurred while parse json from getPlayerTimeInfo", e);
                    promise.catchEx(new SDKError(e.getMessage()));
                } catch (Throwable e) {
                    Logger.error("An exception occurred in getPlayerTimeInfo promise then method", e);
                    promise.catchEx(new SDKError(e.getMessage()));
                }
            }
        });
    }
    //endregion
}
