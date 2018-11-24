package com.herewhite.sdk;

import android.content.Context;
import android.util.Log;

import com.google.gson.Gson;
import com.herewhite.sdk.domain.AkkoEvent;
import com.herewhite.sdk.domain.BroadcastState;
import com.herewhite.sdk.domain.EventEntry;
import com.herewhite.sdk.domain.EventListener;
import com.herewhite.sdk.domain.GlobalState;
import com.herewhite.sdk.domain.ImageInformation;
import com.herewhite.sdk.domain.LinearTransformationDescription;
import com.herewhite.sdk.domain.MemberState;
import com.herewhite.sdk.domain.PptPage;
import com.herewhite.sdk.domain.Promise;
import com.herewhite.sdk.domain.RoomMember;
import com.herewhite.sdk.domain.TextareaBox;
import com.herewhite.sdk.domain.ViewMode;

import org.json.JSONObject;

import java.util.concurrent.ConcurrentHashMap;

import wendu.dsbridge.OnReturnValue;

/**
 * Created by buhe on 2018/8/10.
 */

public class Room {

    private final static Gson gson = new Gson();

    private String uuid;
    private final WhiteBroadView bridge;
    private final Context context;
    private WhiteSdk sdk;
    private ConcurrentHashMap<String, EventListener> eventListenerConcurrentHashMap = new ConcurrentHashMap<>();

    public Room(String uuid, WhiteBroadView bridge, Context context, WhiteSdk sdk) {
        this.uuid = uuid;
        this.bridge = bridge;
        this.context = context;
        this.sdk = sdk;
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
        this.sdk.releaseRoom(this.uuid);
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
                try {
                    promise.then(gson.fromJson(String.valueOf(o), GlobalState.class));
                } catch (Throwable e) {
                    Logger.error("An exception occurred while resolve getGlobalState method promise", e);
                }
            }
        });
    }

    public void getMemberState(final Promise<MemberState> promise) {
        bridge.callHandler("room.getMemberState", new OnReturnValue<String>() {
            @Override
            public void onValue(String o) {
                try {
                    promise.then(gson.fromJson(String.valueOf(o), MemberState.class));
                } catch (Throwable e) {
                    Logger.error("An exception occurred while resolve getMemberState method promise", e);
                }
            }
        });
    }

    public void getRoomMembers(final Promise<RoomMember[]> promise) {
        bridge.callHandler("room.getRoomMembers", new Object[]{}, new OnReturnValue<Object>() {
            @Override
            public void onValue(Object o) {
                try {
                    promise.then(gson.fromJson(String.valueOf(o), RoomMember[].class));
                } catch (Throwable e) {
                    Logger.error("An exception occurred while resolve getRoomMembers method promise", e);
                }
            }
        });
    }

    public void getPptImages(final Promise<String[]> promise) {
        bridge.callHandler("room.getPptImages", new Object[]{}, new OnReturnValue<Object>() {
            @Override
            public void onValue(Object o) {
                try {
                    promise.then(gson.fromJson(String.valueOf(o), String[].class));
                } catch (Throwable e) {
                    Logger.error("An exception occurred while resolve getPptImages method promise", e);
                }
            }
        });
    }

    public void getBroadcastState(final Promise<BroadcastState> promise) {
        bridge.callHandler("room.getBroadcastState", new Object[]{}, new OnReturnValue<Object>() {
            @Override
            public void onValue(Object o) {
                try {
                    promise.then(gson.fromJson(String.valueOf(o), BroadcastState.class));
                } catch (Throwable e) {
                    Logger.error("An exception occurred while resolve getBroadcastState method promise", e);
                }
            }
        });
    }

    public void convertToPointInWorld() {

    }

    public void fireMagixEvent(EventEntry eventEntry) {
        EventListener eventListener = eventListenerConcurrentHashMap.get(eventEntry.getEventName());
        if (eventListener != null) {
            try {
                eventListener.onEvent(eventEntry);
            } catch (Throwable e) {
                Logger.error("An exception occurred while sending the event", e);
            }
        }
    }

    public void dispatchMagixEvent(AkkoEvent eventEntry) {
        bridge.callHandler("room.dispatchMagixEvent", new Object[]{gson.toJson(eventEntry)});
    }

    public void addMagixEventListener(String eventName, EventListener eventListener) {
        this.eventListenerConcurrentHashMap.put(eventName, eventListener);
        bridge.callHandler("room.addMagixEventListener", new Object[]{eventName});
    }

    public void removeMagixEventListener(String eventName) {
        this.eventListenerConcurrentHashMap.remove(eventName);
        bridge.callHandler("room.removeMagixEventListener", new Object[]{eventName});
    }


}
