package com.herewhite.sdk;

import android.content.Context;
import android.util.Log;

import com.google.gson.Gson;
import com.herewhite.sdk.domain.BroadcastState;
import com.herewhite.sdk.domain.GlobalState;
import com.herewhite.sdk.domain.ImageInformation;
import com.herewhite.sdk.domain.LinearTransformationDescription;
import com.herewhite.sdk.domain.MemberState;
import com.herewhite.sdk.domain.PptPage;
import com.herewhite.sdk.domain.Promise;
import com.herewhite.sdk.domain.RoomMember;
import com.herewhite.sdk.domain.TextareaBox;
import com.herewhite.sdk.domain.ViewMode;

import wendu.dsbridge.OnReturnValue;

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

    public void disconnect() {
        bridge.callHandler("room.disconnect", new Object[]{});
    }

    public void updateTextarea(TextareaBox textareaBox) {
        bridge.callHandler("room.updateTextarea", new Object[]{gson.toJson(textareaBox)});
    }

    public void insertNewPage(int index) {
        bridge.callHandler("room.insertNewPage", new Object[]{index});
    }

    public void removePage(int index) {
        bridge.callHandler("room.removePage", new Object[]{index});
    }

    public void insertImage(ImageInformation imageInfo) {
        bridge.callHandler("room.removePage", new Object[]{gson.toJson(imageInfo)});
    }

    public void pushPptPages(PptPage[] pages) {
        bridge.callHandler("room.pushPptPages", new Object[]{gson.toJson(pages)});
    }

    public void completeImageUpload(String uuid, String url) {
        bridge.callHandler("room.completeImageUpload", new Object[]{uuid, url});
    }


    public void getGlobalState(final Promise<GlobalState> promise) {
        bridge.callHandler("room.getGlobalState", new Object[]{}, new OnReturnValue<Object>() {
            @Override
            public void onValue(Object o) {
                promise.then(gson.fromJson(String.valueOf(o), GlobalState.class));
            }
        });
    }

    public void getMemberState(final Promise<MemberState> promise) {
        bridge.callHandler("room.getMemberState", new OnReturnValue<String>() {
            @Override
            public void onValue(String o) {
                promise.then(gson.fromJson(String.valueOf(o), MemberState.class));
            }
        });
    }

    public void getRoomMembers(final Promise<RoomMember[]> promise) {
        bridge.callHandler("room.getRoomMembers", new Object[]{}, new OnReturnValue<Object>() {
            @Override
            public void onValue(Object o) {
                promise.then(gson.fromJson(String.valueOf(o), RoomMember[].class));
            }
        });
    }

    public void getPptImages(final Promise<String[]> promise) {
        bridge.callHandler("room.getPptImages", new Object[]{}, new OnReturnValue<Object>() {
            @Override
            public void onValue(Object o) {
                promise.then(gson.fromJson(String.valueOf(o), String[].class));
            }
        });
    }

    public void getTransform(final Promise<LinearTransformationDescription> promise) {
        bridge.callHandler("room.getTransform", new Object[]{}, new OnReturnValue<Object>() {
            @Override
            public void onValue(Object o) {
                promise.then(gson.fromJson(String.valueOf(o), LinearTransformationDescription.class));
            }
        });
    }

    public void getBroadcastState(final Promise<BroadcastState> promise) {
        bridge.callHandler("room.getBroadcastState", new Object[]{}, new OnReturnValue<Object>() {
            @Override
            public void onValue(Object o) {
                promise.then(gson.fromJson(String.valueOf(o), BroadcastState.class));
            }
        });
    }

    public void convertToPointInWorld() {

    }


}