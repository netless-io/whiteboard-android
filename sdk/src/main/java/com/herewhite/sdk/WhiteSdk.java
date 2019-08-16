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
        return "2.4.7";
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

    public void joinRoom(final RoomParams roomParams, final Promise<Room> roomPromise) {
        this.joinRoom(roomParams, null, roomPromise);
    }

    public void joinRoom(final RoomParams roomParams, final RoomCallbacks roomCallbacks, final Promise<Room> roomPromise) {
        try {
            if (roomCallbacks != null) {
                this.roomCallbacksImplement.setListener(roomCallbacks);  // 覆盖
            }
            bridge.callHandler("sdk.joinRoom", new Object[]{
                    roomParams.getUuid(),
                    roomParams.getRoomToken(),
                    roomParams.getUserPayload() != null ? roomParams.getUserPayload() : roomParams.getMemberInfo()
            }, new OnReturnValue<String>() {
                @Override
                public void onValue(String roomString) {
                    JsonObject jsonObject = gson.fromJson(roomString, JsonObject.class);
                    if (jsonObject.has("__error")) {
                        String msg = "Unknow exception";
                        String jsStack = "Unknow stack";
                        if (jsonObject.getAsJsonObject("__error").has("message")) {
                            msg = jsonObject.getAsJsonObject("__error").get("message").getAsString();
                        }
                        if (jsonObject.getAsJsonObject("__error").has("jsStack")) {
                            jsStack = jsonObject.getAsJsonObject("__error").get("jsStack").getAsString();
                        }
                        try {
                            roomPromise.catchEx(new SDKError(msg, jsStack));
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

    public void createPlayer(final PlayerConfiguration playerConfiguration, final Promise<Player> playerPromise) {
        createPlayer(playerConfiguration, null, playerPromise);
    }

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
                    if (jsonObject.has("__error")) {
                        String msg = "Unknow exception";
                        String jsStack = "Unknow stack";
                        if (jsonObject.getAsJsonObject("__error").has("message")) {
                            msg = jsonObject.getAsJsonObject("__error").get("message").getAsString();
                        }
                        if (jsonObject.getAsJsonObject("__error").has("jsStack")) {
                            jsStack = jsonObject.getAsJsonObject("__error").get("jsStack").getAsString();
                        }
                        try {
                            playerPromise.catchEx(new SDKError(msg, jsStack));
                        } catch (AssertionError a) {
                            throw a;
                        } catch (Throwable e) {
                            Logger.error("An exception occurred while catch createPlayer method exception", e);
                        }
                    } else {
                        JsonObject timeInfo = jsonObject.getAsJsonObject("timeInfo");
                        PlayerTimeInfo playerTimeInfo = gson.fromJson(timeInfo.toString(), PlayerTimeInfo.class);
                        SyncDisplayerState<PlayerState> syncPlayerState = new SyncDisplayerState<>(PlayerState.class, "{}", true);
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

    public void releaseRoom(String uuid) {
        roomCallbacksImplement.setListener(null);
    }

    public void releasePlayer(String uuid) {
        playerCallbacksImplement.setListener(null);
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
}