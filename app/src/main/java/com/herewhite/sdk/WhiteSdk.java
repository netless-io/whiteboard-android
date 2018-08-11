package com.herewhite.sdk;

import android.content.Context;
import android.support.annotation.NonNull;
import android.util.Log;

import com.herewhite.sdk.domain.Promise;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import wendu.dsbridge.OnReturnValue;

/**
 * Created by buhe on 2018/8/10.
 */

public class WhiteSdk {


    private final WhiteBroadView bridge;
    private final Context context;

    public WhiteSdk(WhiteBroadView bridge, Context context, WhiteSdkConfiguration whiteSdkConfiguration) {
        this.bridge = bridge;
        this.context = context;
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


}
