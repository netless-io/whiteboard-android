package com.herewhite.sdk;

import android.content.Context;

import com.google.gson.Gson;
import com.herewhite.sdk.domain.PlayerObserverMode;
import com.herewhite.sdk.domain.PlayerPhase;
import com.herewhite.sdk.domain.PlayerState;
import com.herewhite.sdk.domain.PlayerTimeInfo;
import com.herewhite.sdk.domain.Promise;
import com.herewhite.sdk.domain.SDKError;
import com.herewhite.sdk.implement.BridgeWrapper;

import wendu.dsbridge.OnReturnValue;

public class Player {
    private final static Gson gson = new Gson();

    private final String room;
    private final BridgeWrapper bridge;
    private final Context context;
    private final WhiteSdk whiteSdk;

    public Player(String room, WhiteBroadView bridge, Context context, WhiteSdk whiteSdk) {

        this.room = room;
        this.bridge = new BridgeWrapper(bridge);
        this.context = context;
        this.whiteSdk = whiteSdk;
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
        this.whiteSdk.releasePlayer(room);
    }

    //跳转至特定时间
    public void seekToScheduleTime(long beginTime) {
        bridge.callHandler("player.seekToScheduleTime", new Object[]{beginTime});
    }

    //设置查看模式
    public void setObserverMode(PlayerObserverMode mode) {
        bridge.callHandler("player.setObserverMode", new Object[]{mode.name()});
    }

    //设置跟随的用户
    public void setFollowUserId(int userId) {
        bridge.callHandler("player.setFollowUserId", new Object[]{userId});
    }

    public void getPhase(final Promise<PlayerPhase> promise) {
        bridge.callHandler("player.getBroadcastState", new Object[]{}, new OnReturnValue<Object>() {
            @Override
            public void onValue(Object o) {
                try {
                    promise.then(PlayerPhase.valueOf(String.valueOf(o)));
                } catch (Throwable e) {
                    Logger.error("An exception occurred while resolve getPhase method promise", e);
                    promise.catchEx(new SDKError(e.getMessage()));
                }
            }
        });
    }

    public void getPlayerState(final Promise<PlayerState> promise) {
        bridge.callHandler("player.getPlayerState", new Object[]{}, new OnReturnValue<Object>() {
            @Override
            public void onValue(Object o) {
                try {
                    PlayerState playerState = gson.fromJson(String.valueOf(o), PlayerState.class);
                    promise.then(playerState);
                } catch (Throwable e) {
                    Logger.error("An exception occurred while resolve getPlayerState method promise", e);
                    promise.catchEx(new SDKError(e.getMessage()));
                }
            }
        });
    }

    public void getPlayerTimeInfo(final Promise<PlayerTimeInfo> promise) {
        bridge.callHandler("player.getPlayerTimeInfo", new Object[]{}, new OnReturnValue<Object>() {
            @Override
            public void onValue(Object o) {
                try {
                    PlayerTimeInfo playerState = gson.fromJson(String.valueOf(o), PlayerTimeInfo.class);
                    promise.then(playerState);
                } catch (Throwable e) {
                    Logger.error("An exception occurred while resolve getPlayerTimeInfo method promise", e);
                    promise.catchEx(new SDKError(e.getMessage()));
                }
            }
        });
    }


}
