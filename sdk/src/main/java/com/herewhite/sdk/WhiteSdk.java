package com.herewhite.sdk;

import android.content.Context;
import android.webkit.JavascriptInterface;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import com.herewhite.sdk.domain.PlayerConfiguration;
import com.herewhite.sdk.domain.PlayerState;
import com.herewhite.sdk.domain.PlayerTimeInfo;
import com.herewhite.sdk.domain.Promise;
import com.herewhite.sdk.domain.RoomPhase;
import com.herewhite.sdk.domain.RoomState;
import com.herewhite.sdk.domain.SDKError;
import com.herewhite.sdk.domain.UrlInterrupter;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import wendu.dsbridge.OnReturnValue;

public class WhiteSdk {

    private final static Gson gson = new Gson();

    private final WhiteBroadView bridge;
    private final Context context;
    private final RoomCallbacksImplement roomCallbacksImplement;
    private final PlayerCallbacksImplement playerCallbacksImplement;
    private final boolean onlyCallbackRemoteStateModify;
    private UrlInterrupter urlInterrupter;

    private final ConcurrentHashMap<String, Room> roomConcurrentHashMap = new ConcurrentHashMap<>(); // uuid ,Room
    private final ConcurrentHashMap<String, Player> playerConcurrentHashMap = new ConcurrentHashMap<>(); // uuid ,Player

    public static String Version() {
        return "2.3.5";
    }

    public WhiteSdk(WhiteBroadView bridge, Context context, WhiteSdkConfiguration whiteSdkConfiguration) {
        this(bridge, context, whiteSdkConfiguration, null);
    }

    public WhiteSdk(WhiteBroadView bridge, Context context, WhiteSdkConfiguration whiteSdkConfiguration, UrlInterrupter urlInterrupter) {
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
                        initializeRoom(roomParams.getUuid(), roomPromise);
                    }
                }
            });
        } catch (AssertionError a) {
            throw a;
        } catch (Exception e) {
            roomPromise.catchEx(new SDKError(e.getMessage()));
        }
    }

    private void initializeRoom(final String uuid, final Promise<Room> roomPromise) {
        bridge.callHandler("room.state.getRoomState", new OnReturnValue<Object>() {
            @Override
            public void onValue(Object o) {
                boolean disableCallbackWhilePutting = onlyCallbackRemoteStateModify;
                SyncDisplayerState<RoomState> syncRoomState = new SyncDisplayerState<>(RoomState.class, String.valueOf(o), disableCallbackWhilePutting);
                Room room = new Room(uuid, bridge, context, WhiteSdk.this, syncRoomState);

                roomConcurrentHashMap.put(uuid, room);
                roomCallbacksImplement.setRoom(room);

                try {
                    roomPromise.then(room);
                } catch (AssertionError a) {
                    throw a;
                } catch (Throwable e) {
                    Logger.error("An exception occurred while resolve joinRoom method promise", e);
                }
            }
        });
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
                        initializePlayer(playerConfiguration.getRoom(), playerPromise);
                    }
                }
            });
        } catch (AssertionError a) {
            throw a;
        } catch (Exception e) {
            playerPromise.catchEx(new SDKError(e.getMessage()));
        }
    }

    private void initializePlayer(final String uuid, final Promise<Player> playerPromise) {
        bridge.callHandler("player.state.timeInfo", new Object[]{}, new OnReturnValue<Object>() {
            @Override
            public void onValue(Object o) {
                final PlayerTimeInfo playerTimeInfo = gson.fromJson(String.valueOf(o), PlayerTimeInfo.class);
                bridge.callHandler("player.state.playerState", new Object[]{}, new OnReturnValue<Object>() {
                    @Override
                    public void onValue(Object o) {

                        SyncDisplayerState<PlayerState> syncPlayerState = new SyncDisplayerState<>(PlayerState.class, String.valueOf(o), true);
                        Player player = new Player(uuid, bridge, context, WhiteSdk.this, playerTimeInfo, syncPlayerState);

                        playerCallbacksImplement.setPlayer(player);
                        playerConcurrentHashMap.put(uuid, player);

                        try {
                            playerPromise.then(player);

                        } catch (AssertionError a) {
                            throw a;
                        } catch (Throwable e) {
                            Logger.error("An exception occurred while resolve createPlayer method promise", e);
                        }
                    }
                });
            }
        });
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