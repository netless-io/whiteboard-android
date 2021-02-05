package com.herewhite.sdk;

import android.content.Context;
import android.webkit.JavascriptInterface;

import androidx.annotation.Nullable;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.herewhite.sdk.domain.FontFace;
import com.herewhite.sdk.domain.PlayerConfiguration;
import com.herewhite.sdk.domain.PlayerState;
import com.herewhite.sdk.domain.PlayerTimeInfo;
import com.herewhite.sdk.domain.Promise;
import com.herewhite.sdk.domain.RoomPhase;
import com.herewhite.sdk.domain.RoomState;
import com.herewhite.sdk.domain.SDKError;
import com.herewhite.sdk.domain.UrlInterrupter;

import org.json.JSONObject;

import java.util.Map;

import wendu.dsbridge.OnReturnValue;

public class WhiteSdk {

    private final static Gson gson = new Gson();

    private final WhiteboardView bridge;
    private final Context context;
    private final RoomCallbacksImplement roomCallbacksImplement;
    private final PlayerCallbacksImplement playerCallbacksImplement;

    public CommonCallbacks getCommonCallbacks() {
        return commonCallbacks;
    }

    public void setCommonCallbacks(CommonCallbacks commonCallbacks) {
        this.commonCallbacks = commonCallbacks;
    }

    @Nullable
    private CommonCallbacks commonCallbacks;
    private final boolean onlyCallbackRemoteStateModify;
    @Nullable
    private UrlInterrupter urlInterrupter;

    public AudioMixerImplement getAudioMixerImplement() {
        return audioMixerImplement;
    }

    @Nullable
    private AudioMixerImplement audioMixerImplement;

    public static String Version() {
        return "2.11.15";
    }

    public WhiteSdk(WhiteboardView bridge, Context context, WhiteSdkConfiguration whiteSdkConfiguration, @Nullable CommonCallbacks commonCallbacks) {
        this(bridge, context, whiteSdkConfiguration, commonCallbacks, null);
    }

    public WhiteSdk(WhiteboardView bridge, Context context, WhiteSdkConfiguration whiteSdkConfiguration, @Nullable CommonCallbacks commonCallbacks, @Nullable AudioMixerBridge audioMixerBridge) {
        this.bridge = bridge;
        this.context = context;
        this.roomCallbacksImplement = new RoomCallbacksImplement(context);
        this.playerCallbacksImplement = new PlayerCallbacksImplement();
        this.onlyCallbackRemoteStateModify = whiteSdkConfiguration.isOnlyCallbackRemoteStateModify();
        this.commonCallbacks = commonCallbacks;
        if (audioMixerBridge != null) {
            this.audioMixerImplement = new AudioMixerImplement(bridge, audioMixerBridge);
            bridge.addJavascriptObject(this.audioMixerImplement, "rtc");
            whiteSdkConfiguration.setEnableRtcIntercept(true);
        }

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

    public WhiteSdk(WhiteboardView bridge, Context context, WhiteSdkConfiguration whiteSdkConfiguration) {
        this(bridge, context, whiteSdkConfiguration, (CommonCallbacks) null);
    }

    public WhiteSdk(WhiteboardView bridge, Context context, WhiteSdkConfiguration whiteSdkConfiguration, UrlInterrupter urlInterrupter) {
        this(bridge, context, whiteSdkConfiguration);
        this.urlInterrupter = urlInterrupter;
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
                        room.setRoomPhase(RoomPhase.connected);
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
     * 查看房间是否有回放数据
     *
     * @param playerConfiguration 回放房间和时间段信息
     * @param playablePromise 返回是否能够播放
     * @since 2.11.0
     */
    public void isPlayable(final PlayerConfiguration playerConfiguration, final Promise<Boolean> playablePromise) {
        bridge.callHandler("sdk.isPlayable", new Object[]{playerConfiguration}, new OnReturnValue<Boolean>() {
            @Override
            public void onValue(Boolean retValue) {
                playablePromise.then(retValue);
            }
        });
    }

    /**
     * @param fontFaces 需要增加的字体，当名字可以提供给 ppt 和文字教具使用。
     * 注意：1. 该修改只在本地有效，不会对远端造成影响。
     *      2. 以这种方式插入的 FontFace，只有当该字体被使用时，才会触发下载。
     *      3. FontFace，可能会影响部分设备的渲染逻辑，部分设备，可能会在完成字体加载后，才渲染文字。
     *      4. 该 API 插入的字体，为一个整体，重复调用该 API，会覆盖之前的字体内容。
     *      5. 该 API 与 loadFontFaces 重复使用，无法预期行为，请尽量避免。
     * @since 2.11.2
     */
    public void setupFontFaces(FontFace[] fontFaces) {
        bridge.callHandler("sdk.updateNativeFontFaceCSS", new Object[]{fontFaces});
    }

    /**
     * @param fontFaces 需要增加的字体，可以提供给 ppt 和文字教具使用。
     * @param loadPromise 如果有报错，会在此处错误回调。该回调会在每一个字体加载成功或者失败后，单独回调。FontFace 填写正确的话，有多少个字体，就会有多少个回调。
     * 注意：1. 该修改只在本地有效，不会对远端造成影响。
     *      2. FontFace，可能会影响部分设备的渲染逻辑，部分设备，可能会在完成字体加载后，才渲染文字。
     *      3. 该 API 插入的字体，无法删除；每次都是增加新字体。
     *      4. 该 API 与 setupFontFaces 重复使用，无法预期行为，请尽量避免。
     * @since 2.11.2
     */
    public void loadFontFaces(FontFace[] fontFaces, final Promise<JSONObject>loadPromise) {
        bridge.callHandler("sdk.asyncInsertFontFaces", new Object[]{fontFaces}, new OnReturnValue<JSONObject>() {
            @Override
            public void onValue(JSONObject retValue) {
                loadPromise.then(retValue);
            }
        });
    }

    /**
     * @param names 定义文字教具，在本地使用的字体。
     * 注意：该修改只在本地有效，不会对远端造成影响。
     * @since 2.11.2
     */
    public void updateTextFont(String[] names) {
        bridge.callHandler("sdk.updateNativeTextareaFont", new Object[]{names});
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

    //region SDK Callbacks

    @JavascriptInterface
    public String urlInterrupter(Object args) {
        if (this.commonCallbacks != null) {
            return this.commonCallbacks.urlInterrupter(String.valueOf(args));
        } else if (this.urlInterrupter == null) {
            return String.valueOf(args);
        }
        return this.urlInterrupter.urlInterrupter(String.valueOf(args));
    }

    @JavascriptInterface
    public void throwError(Object args) {
        Logger.info("WhiteSDK JS error: " + gson.fromJson(String.valueOf(args), Map.class));
        if (this.commonCallbacks != null) {
            this.commonCallbacks.throwError(args);
        }
    }

    @JavascriptInterface
    public void logger(Object args) {
        Logger.info("WhiteSDK logger: " + gson.fromJson(String.valueOf(args), Map.class));
    }

    @JavascriptInterface
    public void postMessage(Object args) {
        if (this.commonCallbacks != null) {
            try {

                JSONObject object = new JSONObject((String) args);
                this.commonCallbacks.onMessage(object);
            } catch (Throwable throwable) {

            }
        }
    }

    @JavascriptInterface
    public void onPPTMediaPlay(Object args) {
        if (this.commonCallbacks != null) {
            this.commonCallbacks.onPPTMediaPlay();
        }
    }

    @JavascriptInterface
    public void onPPTMediaPause(Object args) {
        if (this.commonCallbacks != null) {
            this.commonCallbacks.onPPTMediaPause();
        }
    }

    @JavascriptInterface
    public void setupFail(Object object) {
        if (this.commonCallbacks != null && object instanceof JSONObject) {
            SDKError sdkError = SDKError.parseError((JSONObject) object);
            this.commonCallbacks.sdkSetupFail(sdkError);
        }
    }
    //endregion
}