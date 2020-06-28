package com.herewhite.sdk;

import android.content.Context;
import android.webkit.JavascriptInterface;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.herewhite.sdk.domain.PlayerConfiguration;
import com.herewhite.sdk.domain.PlayerState;
import com.herewhite.sdk.domain.PlayerTimeInfo;
import com.herewhite.sdk.domain.Promise;
import com.herewhite.sdk.domain.RoomState;
import com.herewhite.sdk.domain.SDKError;
import com.herewhite.sdk.domain.UrlInterrupter;

import java.util.Map;

import wendu.dsbridge.OnReturnValue;

public class WhiteSdk {

    private final static Gson gson = new Gson();

    private final WhiteboardView bridge;
    private final Context context;
    private final RoomCallbacksImplement roomCallbacksImplement;
    private final PlayerCallbacksImplement playerCallbacksImplement;
    private final boolean onlyCallbackRemoteStateModify;
    private UrlInterrupter urlInterrupter;

    public static String Version() {
        return "2.9.4";
    }

    public WhiteSdk(WhiteboardView bridge, Context context, WhiteSdkConfiguration whiteSdkConfiguration) {
        this(bridge, context, whiteSdkConfiguration, null);
    }

    public WhiteSdk(WhiteboardView bridge, Context context, WhiteSdkConfiguration whiteSdkConfiguration, UrlInterrupter urlInterrupter) {
        this.bridge = bridge;
        this.context = context;
        this.urlInterrupter = urlInterrupter;
        this.roomCallbacksImplement = new RoomCallbacksImplement(context);
        this.playerCallbacksImplement = new PlayerCallbacksImplement();
        this.onlyCallbackRemoteStateModify = whiteSdkConfiguration.isOnlyCallbackRemoteStateModify();

        bridge.addJavascriptObject(this, "sdk");
        bridge.addJavascriptObject(this.roomCallbacksImplement, "room");
        bridge.addJavascriptObject(this.playerCallbacksImplement, "player");

        if (whiteSdkConfiguration.isOnlyCallbackRemoteStateModify()) {
            // JavaScript 必须将所有 state 变化回调提供给 native。
            // 该属性的实现在 native 代码中体现。
            whiteSdkConfiguration.setOnlyCallbackRemoteStateModify(false);
        }
        bridge.callHandler("sdk.newWhiteSdk", new Object[]{whiteSdkConfiguration});

        whiteSdkConfiguration.setOnlyCallbackRemoteStateModify(this.onlyCallbackRemoteStateModify);
    }

    /**
     * 加入房间，参考 {@link #joinRoom(RoomParams, RoomCallbacks, Promise)}
     *
     * @param roomParams  the room params
     * @param roomPromise the room promise
     */
    public void joinRoom(final RoomParams roomParams, final Promise<Room> roomPromise) {
        this.joinRoom(roomParams, null, roomPromise);
    }

    /**
     * 加入房间，最终调用 API
     *
     * @param roomParams    房间参数，room uuid 与 room token
     * @param roomCallbacks 房间变化回调，在重连时，如果不传 roomCallback 参数，则会回调旧的 roomCallback。如果释放 callback，可以使用 {@link #releaseRoom(String)}
     * @param roomPromise   创建完成回调
     */
    public void joinRoom(final RoomParams roomParams, final RoomCallbacks roomCallbacks, final Promise<Room> roomPromise) {
        try {
            if (roomCallbacks != null) {
                this.roomCallbacksImplement.setListener(roomCallbacks);  // 覆盖
            }
            bridge.callHandler("sdk.joinRoom", new Object[]{roomParams}, new OnReturnValue<String>() {
                @Override
                public void onValue(String roomString) {
                    JsonObject jsonObject = gson.fromJson(roomString, JsonObject.class);
                    SDKError promiseError = SDKError.promiseError(jsonObject);
                    if (promiseError != null) {
                        try {
                            roomPromise.catchEx(promiseError);
                        } catch (AssertionError a) {
                            throw a;
                        } catch (Throwable e) {
                            Logger.error("An exception occurred while catch joinRoom method exception", e);
                        }
                    } else {
                        boolean disableCallbackWhilePutting = onlyCallbackRemoteStateModify;
                        JsonObject jsonState = jsonObject.getAsJsonObject("state");
                        SyncDisplayerState<RoomState> syncRoomState = new SyncDisplayerState<>(RoomState.class, jsonState.toString(), disableCallbackWhilePutting);
                        Room room = new Room(roomParams.getUuid(), bridge, context, WhiteSdk.this, syncRoomState);
                        Long observerId = jsonObject.get("observerId").getAsLong();
                        Boolean isWritable = jsonObject.get("isWritable").getAsBoolean();
                        room.setObserverId(observerId);
                        room.setWritable(isWritable);
                        roomCallbacksImplement.setRoom(room);
                        roomPromise.then(room);
                    }
                }
            });
        } catch (AssertionError a) {
            throw a;
        } catch (Exception e) {
            roomPromise.catchEx(new SDKError(e.getMessage()));
        }
    }

    /**
     * 创建回放房间，参考 {@link #createPlayer(PlayerConfiguration, PlayerEventListener, Promise)}
     *
     * @param playerConfiguration the player configuration
     * @param playerPromise       the player promise
     */
    public void createPlayer(final PlayerConfiguration playerConfiguration, final Promise<Player> playerPromise) {
        createPlayer(playerConfiguration, null, playerPromise);
    }

    /**
     * 创建回放房间
     *
     * @param playerConfiguration 回放参数，具体查看 {@link PlayerConfiguration}
     * @param playerEventListener 回放房间变化回调。当使用同一个 sdk 初始化多个房间时，该参数传入 null，则新回放房间，仍然会回调旧的 playerEventListener
     * @param playerPromise       创建完成回调
     */
    public void createPlayer(final PlayerConfiguration playerConfiguration, PlayerEventListener playerEventListener, final Promise<Player> playerPromise) {
        try {
            if (playerEventListener != null) {
                this.playerCallbacksImplement.setListener(playerEventListener);
            }
            bridge.callHandler("sdk.replayRoom", new Object[]{
                    playerConfiguration
            }, new OnReturnValue<String>() {
                @Override
                public void onValue(String roomString) {
                    JsonObject jsonObject = gson.fromJson(roomString, JsonObject.class);
                    SDKError promiseError = SDKError.promiseError(jsonObject);
                    if (promiseError != null) {
                        try {
                            playerPromise.catchEx(promiseError);
                        } catch (AssertionError a) {
                            throw a;
                        } catch (Throwable e) {
                            Logger.error("An exception occurred while catch createPlayer method exception", e);
                        }
                    } else {
                        JsonObject timeInfo = jsonObject.getAsJsonObject("timeInfo");
                        PlayerTimeInfo playerTimeInfo = gson.fromJson(timeInfo.toString(), PlayerTimeInfo.class);
                        SyncDisplayerState<PlayerState> syncPlayerState = new SyncDisplayerState(PlayerState.class, "{}", true);
                        Player player = new Player(playerConfiguration.getRoom(), bridge, context, WhiteSdk.this, playerTimeInfo, syncPlayerState);

                        playerCallbacksImplement.setPlayer(player);
                        playerPromise.then(player);
                    }
                }
            });
        } catch (AssertionError a) {
            throw a;
        } catch (Exception e) {
            playerPromise.catchEx(new SDKError(e.getMessage()));
        }
    }

    /**
     * 释放实时房间对 RoomCallback 的持有
     *
     * @since 2.4.12
     */
    public void releaseRoom() {
        roomCallbacksImplement.setListener(null);
    }

    /**
     * 释放实时房间对 RoomCallback 的持有
     *
     * @deprecated 一个 WhiteSDK 实例，只能对应一个实时房间，所以不再需要使用 room uuid 进行定位
     * @param uuid 任意参数，不会被使用
     */
    public void releaseRoom(String uuid) {
        releaseRoom();
    }

    /**
     * 释放回放房间对 PlayerEventListener 的持有
     *
     * @since 2.4.12
     */
    public void releasePlayer() {
        playerCallbacksImplement.setListener(null);
    }

    /**
     * 释放回放房间对 PlayerEventListener 的持有
     * @deprecated 由于一个 WhiteSDK 实例，只能对应一个回放房间，所以不再需要使用 player uuid 进行定位
     * @param uuid 任意参数，不会被使用
     */
    public void releasePlayer(String uuid) {
        releasePlayer();
    }


    @JavascriptInterface
    public String urlInterrupter(Object args) {
        if (this.urlInterrupter == null) {
            return String.valueOf(args);
        }
        return this.urlInterrupter.urlInterrupter(String.valueOf(args));
    }

    @JavascriptInterface
    public void throwError(Object args) {
        Logger.info("WhiteSDK JS error: " + gson.fromJson(String.valueOf(args), Map.class));
    }

    @JavascriptInterface
    public void logger(Object args) {
        Logger.info("WhiteSDK logger: " + gson.fromJson(String.valueOf(args), Map.class));
    }

    @JavascriptInterface
    public void postMessage(Object args) {

    }
}