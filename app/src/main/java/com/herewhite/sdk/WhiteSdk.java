package com.herewhite.sdk;

import android.content.Context;
import android.util.Log;
import android.webkit.JavascriptInterface;

import com.herewhite.sdk.domain.Promise;
import com.herewhite.sdk.domain.RoomPhase;

import org.json.JSONException;

import java.util.ArrayList;
import java.util.List;

import wendu.dsbridge.OnReturnValue;

/**
 * Created by buhe on 2018/8/10.
 */

public class WhiteSdk {


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
    public Object firePhaseChanged(Object args) throws JSONException {
//         获取事件,反序列化然后发送通知给监听者
        for (RoomCallbacks roomCallbacks : listeners) {
            roomCallbacks.onPhaseChanged(RoomPhase.valueOf(args.toString()));
        }
        return 0;
    }

    @JavascriptInterface
    public Object fireKickedWithReason(Object args) throws JSONException {
        // 获取事件,反序列化然后发送通知给监听者
        for (RoomCallbacks roomCallbacks : listeners) {
            roomCallbacks.onKickedWithReason(args.toString());
        }
        return 0;
    }


}
