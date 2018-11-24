package com.herewhite.sdk;

import android.content.Context;
import android.webkit.JavascriptInterface;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.herewhite.sdk.domain.EventEntry;
import com.herewhite.sdk.domain.FrameError;
import com.herewhite.sdk.domain.SDKError;
import com.herewhite.sdk.domain.Promise;
import com.herewhite.sdk.domain.RoomPhase;
import com.herewhite.sdk.domain.RoomState;

import org.json.JSONException;

import java.util.ArrayList;
import java.util.List;
import java.util.WeakHashMap;
import java.util.concurrent.ConcurrentHashMap;

import wendu.dsbridge.OnReturnValue;

/**
 *
 */

public class WhiteSdk {

    private final static Gson gson = new Gson();

    private final WhiteBroadView bridge;
    private final Context context;
    private final List<RoomCallbacks> listeners = new ArrayList<>();
    private final ConcurrentHashMap<String, Room> roomConcurrentHashMap = new ConcurrentHashMap<>(); // uuid ,Room

    public WhiteSdk(WhiteBroadView bridge, Context context, WhiteSdkConfiguration whiteSdkConfiguration) {
        this.bridge = bridge;
        this.context = context;
        bridge.addJavascriptObject(this, "sdk");
        bridge.callHandler("sdk.newWhiteSdk", new Object[]{gson.toJson(whiteSdkConfiguration)});
    }

    /**
     * 等待链接成功后才会返回 Room 对象
     *
     * @param roomParams
     */
    public void joinRoom(final RoomParams roomParams, final Promise<Room> roomPromise) {
        try {
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

    public void addRoomCallbacks(RoomCallbacks callback) {
        listeners.add(callback);
    }

    @JavascriptInterface
    public void firePhaseChanged(Object args) throws JSONException {
//         获取事件,反序列化然后发送通知给监听者
        for (RoomCallbacks roomCallbacks : listeners) {
            try {
                roomCallbacks.onPhaseChanged(RoomPhase.valueOf(String.valueOf(args)));
            } catch (Throwable e) {
                Logger.error("An exception occurred while invoke onPhaseChanged method", e);
            }
        }
    }

    @JavascriptInterface
    public void fireKickedWithReason(Object args) throws JSONException {
        // 获取事件,反序列化然后发送通知给监听者
        for (RoomCallbacks roomCallbacks : listeners) {
            try {
                roomCallbacks.onKickedWithReason(String.valueOf(args));
            } catch (Throwable e) {
                Logger.error("An exception occurred while invoke onKickedWithReason method", e);
            }

        }
    }

    @JavascriptInterface
    public void fireDisconnectWithError(Object args) throws JSONException {
        // 获取事件,反序列化然后发送通知给监听者
        for (RoomCallbacks roomCallbacks : listeners) {
            try {
                roomCallbacks.onDisconnectWithError(new Exception(String.valueOf(args)));
            } catch (Throwable e) {
                Logger.error("An exception occurred while invoke onDisconnectWithError method", e);
            }
        }
    }

    @JavascriptInterface
    public void fireRoomStateChanged(Object args) throws JSONException {
        // 获取事件,反序列化然后发送通知给监听者
        RoomState roomState = gson.fromJson(String.valueOf(args), RoomState.class);
        for (RoomCallbacks roomCallbacks : listeners) {
            try {
                roomCallbacks.onRoomStateChanged(roomState);
            } catch (Throwable e) {
                Logger.error("An exception occurred while invoke onRoomStateChanged method", e);
            }

        }
    }

    @JavascriptInterface
    public void fireBeingAbleToCommitChange(Object args) throws JSONException {
        // 获取事件,反序列化然后发送通知给监听者
        for (RoomCallbacks roomCallbacks : listeners) {
            try {
                roomCallbacks.onBeingAbleToCommitChange(Boolean.valueOf(String.valueOf(args)));
            } catch (Throwable e) {
                Logger.error("An exception occurred while invoke onBeingAbleToCommitChange method", e);
            }

        }
    }

    @JavascriptInterface
    public void fireCatchErrorWhenAppendFrame(Object args) throws JSONException {
        // 获取事件,反序列化然后发送通知给监听者
        FrameError frameError = gson.fromJson(String.valueOf(args), FrameError.class);
        for (RoomCallbacks roomCallbacks : listeners) {
            try {
                roomCallbacks.onCatchErrorWhenAppendFrame(frameError.getUserId(), new Exception(frameError.getError()));
            } catch (Throwable e) {
                Logger.error("An exception occurred while invoke onCatchErrorWhenAppendFrame method", e);
            }
        }
    }

    public void releaseRoom(String uuid) {
        this.roomConcurrentHashMap.remove(uuid);
    }

    @JavascriptInterface
    public void fireMagixEvent(Object args) {
        EventEntry eventEntry = gson.fromJson(String.valueOf(args), EventEntry.class);
        Room room = this.roomConcurrentHashMap.get(eventEntry.getUuid());
        if (room != null) {
            room.fireMagixEvent(eventEntry);
        }
    }
}
