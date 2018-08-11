package com.herewhite.sdk;

import android.content.Context;

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
    public Room joinRoom(RoomParams roomParams) {
        bridge.callHandler("sdk.joinRoom", new Object[]{
                roomParams.getUuid(),
                roomParams.getRoomToken()
        });

        // FIXME
        return new Room(bridge, context);
    }


}
