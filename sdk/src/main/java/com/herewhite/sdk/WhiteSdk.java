package com.herewhite.sdk;

import android.content.Context;
import android.webkit.JavascriptInterface;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.herewhite.sdk.domain.PlayerConfiguration;
import com.herewhite.sdk.domain.Promise;
import com.herewhite.sdk.domain.SDKError;
import com.herewhite.sdk.domain.UrlInterrupter;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import wendu.dsbridge.OnReturnValue;

/**
 *
 */

public class WhiteSdk {

    private final static Gson gson = new Gson();

    private final WhiteBroadView bridge;
    private final Context context;
    private final RoomCallbacksImplement roomCallbacksImplement;
    private final PlayerCallbacksImplement playerCallbacksImplement;
    private UrlInterrupter urlInterrupter;

    private final ConcurrentHashMap<String, Room> roomConcurrentHashMap = new ConcurrentHashMap<>(); // uuid ,Room
    private final ConcurrentHashMap<String, Player> playerConcurrentHashMap = new ConcurrentHashMap<>(); // uuid ,Player

    public WhiteSdk(WhiteBroadView bridge, Context context, WhiteSdkConfiguration whiteSdkConfiguration) {
        this(bridge, context, whiteSdkConfiguration, null);
    }

    public WhiteSdk(WhiteBroadView bridge, Context context, WhiteSdkConfiguration whiteSdkConfiguration, UrlInterrupter urlInterrupter) {
        this.bridge = bridge;
        this.context = context;
        this.urlInterrupter = urlInterrupter;
        this.roomCallbacksImplement = new RoomCallbacksImplement();
        this.playerCallbacksImplement = new PlayerCallbacksImplement();
        bridge.addJavascriptObject(this, "sdk");
        bridge.addJavascriptObject(this.roomCallbacksImplement, "room");
        bridge.addJavascriptObject(this.playerCallbacksImplement, "player");
        bridge.callHandler("sdk.newWhiteSdk", new Object[]{whiteSdkConfiguration});
    }

    public void joinRoom(final RoomParams roomParams, final Promise<Room> roomPromise) {
        this.joinRoom(roomParams, null, roomPromise);
    }

    /**
     * 等待链接成功后才会返回 Room 对象
     *
     * @param roomParams
     */
    public void joinRoom(final RoomParams roomParams, final RoomCallbacks roomCallbacks, final Promise<Room> roomPromise) {
        try {
            if (roomCallbacks != null) {
                this.roomCallbacksImplement.setListener(roomCallbacks);  // 覆盖
            }
            bridge.callHandler("sdk.joinRoom", new Object[]{
                    roomParams.getUuid(),
                    roomParams.getRoomToken()
            }, new OnReturnValue<String>() {
                @Override
                public void onValue(String roomString) {
//                    Log.i("white", "call succeed,return value is " + retValue);
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
                        } catch (Throwable e) {
                            Logger.error("An exception occurred while catch joinRoom method exception", e);
                        }
                    } else {
                        Room room = new Room(roomParams.getUuid(), bridge, context, WhiteSdk.this);
                        roomConcurrentHashMap.put(roomParams.getUuid(), room);
                        roomCallbacksImplement.setRoom(room);
                        try {
                            roomPromise.then(room);
                        } catch (Throwable e) {
                            Logger.error("An exception occurred while resolve joinRoom method promise", e);
                        }
                    }

                }
            });
        } catch (Exception e) {
            roomPromise.catchEx(new SDKError(e.getMessage()));
        }

    }


    public void releaseRoom(String uuid) {
        this.roomConcurrentHashMap.remove(uuid);
    }

    public void releasePlayer(String uuid) {
        this.playerConcurrentHashMap.remove(uuid);
    }

    public void createPlayer(final PlayerConfiguration playerConfiguration, final Promise<Player> playerPromise) {
        this.createPlayer(playerConfiguration, null, playerPromise);
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
//                    Log.i("white", "call succeed,return value is " + retValue);
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
                        } catch (Throwable e) {
                            Logger.error("An exception occurred while catch joinRoom method exception", e);
                        }
                    } else {
                        Player room = new Player(playerConfiguration.getRoom(), bridge, context, WhiteSdk.this);
                        playerConcurrentHashMap.put(playerConfiguration.getRoom(), room);
                        try {
                            playerPromise.then(room);
                        } catch (Throwable e) {
                            Logger.error("An exception occurred while resolve joinRoom method promise", e);
                        }
                    }

                }
            });
        } catch (Exception e) {
            playerPromise.catchEx(new SDKError(e.getMessage()));
        }
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
        Logger.info("Error From JS: " + gson.fromJson(String.valueOf(args), Map.class));
    }

    @JavascriptInterface
    public void logger(Object args) {
        Logger.info("From JS: " + gson.fromJson(String.valueOf(args), Map.class));
    }
}
