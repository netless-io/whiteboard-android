package com.herewhite.sdk;

import android.content.Context;

import com.google.gson.Gson;
import com.herewhite.sdk.domain.GlobalState;
import com.herewhite.sdk.domain.MemberState;
import com.herewhite.sdk.domain.ViewMode;

/**
 * Created by buhe on 2018/8/10.
 */

public class Room {

    private final static Gson gson = new Gson();

    private final WhiteBroadView bridge;
    private final Context context;

    public Room(WhiteBroadView bridge, Context context) {
        this.bridge = bridge;
        this.context = context;
    }

    public void setGlobalState(GlobalState globalState) {
        bridge.callHandler("room.setGlobalState", new Object[]{gson.toJson(globalState)});
    }

    public void setMemberState(MemberState memberState) {
        bridge.callHandler("room.setMemberState", new Object[]{gson.toJson(memberState)});
    }

    public void setViewMode(ViewMode viewMode) {
        bridge.callHandler("room.setViewMode", new Object[]{gson.toJson(viewMode)});
    }

    public void setViewSize(int width, int height) {
        bridge.callHandler("room.setViewSize", new Object[]{width, height});
    }
}
