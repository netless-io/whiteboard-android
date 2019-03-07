package com.herewhite.sdk;

import android.content.Context;
import android.webkit.JavascriptInterface;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.herewhite.sdk.domain.EventEntry;
import com.herewhite.sdk.domain.Promise;
import com.herewhite.sdk.domain.SDKError;
import com.herewhite.sdk.domain.UrlInterrupter;

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
    private UrlInterrupter urlInterrupter;

    private final ConcurrentHashMap<String, Room> roomConcurrentHashMap = new ConcurrentHashMap<>(); // uuid ,Room

    public WhiteSdk(WhiteBroadView bridge, Context context, WhiteSdkConfiguration whiteSdkConfiguration) {
        this.bridge = bridge;
        this.context = context;
        this.roomCallbacksImplement = new RoomCallbacksImplement();
        bridge.addJavascriptObject(new RoomCallbacksImplement(), "room");
        bridge.callHandler("sdk.newWhiteSdk", new Object[]{gson.toJson(whiteSdkConfiguration)});
    }


    public void addRoomCallbacks(RoomCallbacks callback) {
        roomCallbacksImplement.addRoomCallbacks(callback);
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

    @JavascriptInterface
    public String urlInterrupter(Object args) {
        return this.urlInterrupter.urlInterrupter(String.valueOf(args));
    }

    @JavascriptInterface
    public void logger(Object args) {
        Logger.info("From JS: " + String.valueOf(args));
    }
}
