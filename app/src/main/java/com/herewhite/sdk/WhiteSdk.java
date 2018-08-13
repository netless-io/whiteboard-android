package com.herewhite.sdk;

import android.content.Context;
import android.util.Log;
import android.webkit.JavascriptInterface;

import com.google.gson.Gson;
import com.herewhite.sdk.domain.FrameError;
import com.herewhite.sdk.domain.GlobalState;
import com.herewhite.sdk.domain.Promise;
import com.herewhite.sdk.domain.RoomPhase;
import com.herewhite.sdk.domain.RoomState;

import org.json.JSONException;

import java.util.ArrayList;
import java.util.List;

import wendu.dsbridge.OnReturnValue;

/**
 *
 */

public class WhiteSdk {

    private final static Gson gson = new Gson();

    private final WhiteBroadView bridge;
    private final Context context;
    private final List<RoomCallbacks> listeners = new ArrayList<>();

    public WhiteSdk(WhiteBroadView bridge, Context context, WhiteSdkConfiguration whiteSdkConfiguration) {
        this.bridge = bridge;
        this.context = context;
        bridge.addJavascriptObject(this, "sdk");
        bridge.callHandler("sdk.newWhiteSdk", new Object[]{
                whiteSdkConfiguration.getDeviceType().name(),
                whiteSdkConfiguration.getZoomMaxScale(),
                whiteSdkConfiguration.getZoomMinScale()
        });
    }

    /**
     * 等待链接成功后才会返回 Room 对象
     *
     * @param roomParams
     */
    public void joinRoom(RoomParams roomParams, final Promise<Room> roomPromise) {
        try {
            bridge.callHandler("sdk.joinRoom", new Object[]{
                    roomParams.getUuid(),
                    roomParams.getRoomToken()
            }, new OnReturnValue<String>() {
                @Override
                public void onValue(String retValue) {
                    Log.d("jsbridge", "call succeed,return value is " + retValue);
                    roomPromise.then(new Room(bridge, context));
                }
            });
        } catch (Exception e) {
            roomPromise.catchEx(e);
        }

    }

    public void addRoomCallbacks(RoomCallbacks callback) {
        listeners.add(callback);
    }

    @JavascriptInterface
    public void firePhaseChanged(Object args) throws JSONException {
//         获取事件,反序列化然后发送通知给监听者
        for (RoomCallbacks roomCallbacks : listeners) {
            roomCallbacks.onPhaseChanged(RoomPhase.valueOf(String.valueOf(args)));
        }
    }

    @JavascriptInterface
    public void fireKickedWithReason(Object args) throws JSONException {
        // 获取事件,反序列化然后发送通知给监听者
        for (RoomCallbacks roomCallbacks : listeners) {
            roomCallbacks.onKickedWithReason(String.valueOf(args));
        }
    }

    @JavascriptInterface
    public void fireDisconnectWithError(Object args) throws JSONException {
        // 获取事件,反序列化然后发送通知给监听者
        for (RoomCallbacks roomCallbacks : listeners) {
            roomCallbacks.onDisconnectWithError(new Exception(String.valueOf(args)));
        }
    }

    @JavascriptInterface
    public void fireRoomStateChanged(Object args) throws JSONException {
        // 获取事件,反序列化然后发送通知给监听者
        RoomState roomState = gson.fromJson(String.valueOf(args), RoomState.class);
        for (RoomCallbacks roomCallbacks : listeners) {
            roomCallbacks.onRoomStateChanged(roomState);
        }
    }

    @JavascriptInterface
    public void fireBeingAbleToCommitChange(Object args) throws JSONException {
        // 获取事件,反序列化然后发送通知给监听者
        for (RoomCallbacks roomCallbacks : listeners) {
            roomCallbacks.onBeingAbleToCommitChange(Boolean.valueOf(String.valueOf(args)));
        }
    }

    @JavascriptInterface
    public void fireCatchErrorWhenAppendFrame(Object args) throws JSONException {
        // 获取事件,反序列化然后发送通知给监听者
        FrameError frameError = gson.fromJson(String.valueOf(args), FrameError.class);
        for (RoomCallbacks roomCallbacks : listeners) {
            roomCallbacks.onCatchErrorWhenAppendFrame(frameError.getUserId(), new Exception(frameError.getError()));
        }
    }

}
