package com.herewhite.sdk;

import com.herewhite.sdk.domain.SDKError;

public interface OnRoomJoinCallback {
    void onRoomJoinSuccess(Room room);

    void onRoomJoinFail(SDKError error);
}
