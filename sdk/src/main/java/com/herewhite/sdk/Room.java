package com.herewhite.sdk;

import android.content.Context;

import com.google.gson.Gson;
import com.herewhite.sdk.domain.AkkoEvent;
import com.herewhite.sdk.domain.BroadcastState;
import com.herewhite.sdk.domain.EventEntry;
import com.herewhite.sdk.domain.EventListener;
import com.herewhite.sdk.domain.GlobalState;
import com.herewhite.sdk.domain.ImageInformation;
import com.herewhite.sdk.domain.ImageInformationWithUrl;
import com.herewhite.sdk.domain.MemberState;
import com.herewhite.sdk.domain.PptPage;
import com.herewhite.sdk.domain.Promise;
import com.herewhite.sdk.domain.RoomMember;
import com.herewhite.sdk.domain.RoomMouseEvent;
import com.herewhite.sdk.domain.SDKError;
import com.herewhite.sdk.domain.Scene;
import com.herewhite.sdk.domain.SceneState;
import com.herewhite.sdk.domain.TextareaBox;
import com.herewhite.sdk.domain.ViewMode;

import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import wendu.dsbridge.OnReturnValue;

/**
 * Created by buhe on 2018/8/10.
 */

public class Room {

    private final static Gson gson = new Gson();
    private final WhiteBroadView bridge;

    private String uuid;
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
        bridge.callHandler("room.setGlobalState", new Object[]{globalState});
    }

    public void setMemberState(MemberState memberState) {
        bridge.callHandler("room.setMemberState", new Object[]{memberState});
    }

    public void setViewMode(ViewMode viewMode) {
        bridge.callHandler("room.setViewMode", new Object[]{viewMode.name()});
    }

    public void setViewSize(int width, int height) {
        bridge.callHandler("room.setViewSize", new Object[]{width, height});
    }

    public void disconnect() {
        bridge.callHandler("room.disconnect", new Object[]{});
        this.sdk.releaseRoom(this.uuid);
    }

    public void updateTextarea(TextareaBox textareaBox) {
        bridge.callHandler("room.updateTextarea", new Object[]{textareaBox});
    }

    public void insertImage(ImageInformation imageInfo) {
        bridge.callHandler("room.insertImage", new Object[]{imageInfo});
    }

    public void completeImageUpload(String uuid, String url) {
        bridge.callHandler("room.completeImageUpload", new Object[]{uuid, url});
    }

    public void insertImage(ImageInformationWithUrl imageInformationWithUrl) {
        ImageInformation imageInformation = new ImageInformation();
        String uuid = UUID.randomUUID().toString();
        imageInformation.setUuid(uuid);
        imageInformation.setCenterX(imageInformationWithUrl.getCenterX());
        imageInformation.setCenterY(imageInformationWithUrl.getCenterY());
        imageInformation.setHeight(imageInformationWithUrl.getHeight());
        imageInformation.setWidth(imageInformationWithUrl.getWidth());
        this.insertImage(imageInformation);
        this.completeImageUpload(uuid, imageInformationWithUrl.getUrl());
    }


    public void getGlobalState(final Promise<GlobalState> promise) {
        bridge.callHandler("room.getGlobalState", new Object[]{}, new OnReturnValue<Object>() {
            @Override
            public void onValue(Object o) {
                try {
                    promise.then(gson.fromJson(String.valueOf(o), GlobalState.class));
                } catch (Throwable e) {
                    Logger.error("An exception occurred while resolve getGlobalState method promise", e);
                    promise.catchEx(new SDKError(e.getMessage()));
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
                    promise.catchEx(new SDKError(e.getMessage()));
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
                    promise.catchEx(new SDKError(e.getMessage()));
                }
            }
        });
    }

    /**
     * 获取当前场景状态
     *
     * @param promise
     */
    public void getSceneState(final Promise<SceneState> promise) {
        bridge.callHandler("room.getSceneState", new Object[]{}, new OnReturnValue<Object>() {
            @Override
            public void onValue(Object o) {
                try {
                    promise.then(gson.fromJson(String.valueOf(o), SceneState.class));
                } catch (Throwable e) {
                    Logger.error("An exception occurred while resolve getRoomMembers method promise", e);
                    promise.catchEx(new SDKError(e.getMessage()));
                }
            }
        });
    }

    /**
     * 获取当前目录下，所有页面的信息
     *
     * @param promise
     */
    public void getScenes(final Promise<Scene[]> promise) {
        bridge.callHandler("room.getScenes", new Object[]{}, new OnReturnValue<Object>() {
            @Override
            public void onValue(Object o) {
                try {
                    promise.then(gson.fromJson(String.valueOf(o), Scene[].class));
                } catch (Throwable e) {
                    Logger.error("An exception occurred while resolve getRoomMembers method promise", e);
                    promise.catchEx(new SDKError(e.getMessage()));
                }
            }
        });
    }

    /**
     * 切换到某个 Scene
     *
     * @param path
     */
    public void setScenePath(String path) {
        bridge.callHandler("room.setScenePath", new Object[]{path});
    }

    public void putScenes(String dir, Scene[] scenes, int index) {
        bridge.callHandler("room.putScenes", new Object[]{dir, scenes, index});
    }

    public void moveScene(String source, String target) {
        bridge.callHandler("room.moveScene", new Object[]{source, target});
    }

    public void removeScenes(String dirOrPath) {
        bridge.callHandler("room.removeScenes", new Object[]{dirOrPath});
    }

    public void getBroadcastState(final Promise<BroadcastState> promise) {
        bridge.callHandler("room.getBroadcastState", new Object[]{}, new OnReturnValue<Object>() {
            @Override
            public void onValue(Object o) {
                try {
                    promise.then(gson.fromJson(String.valueOf(o), BroadcastState.class));
                } catch (Throwable e) {
                    Logger.error("An exception occurred while resolve getBroadcastState method promise", e);
                    promise.catchEx(new SDKError(e.getMessage()));
                }
            }
        });
    }

    public void zoomChange(double scale) {
        bridge.callHandler("room.zoomChange", new Object[]{scale});
    }

    public void disableOperations(boolean disableOperations) {
        bridge.callHandler("room.disableOperations", new Object[]{disableOperations});
    }

//    public void convertToPointInWorld(double x, double y, final Promise<Point> promise) {
//        bridge.callHandler("room.convertToPointInWorld", new Object[]{}, new OnReturnValue<Object>() {
//            @Override
//            public void onValue(Object o) {
//                try {
//                    promise.then(gson.fromJson(String.valueOf(o), Point.class));
//                } catch (Throwable e) {
//                    Logger.error("An exception occurred while resolve convertToPointInWorld method promise", e);
//                    promise.catchEx(new SDKError(e.getMessage()));
//                }
//            }
//        });
//    }


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
        bridge.callHandler("room.dispatchMagixEvent", new Object[]{eventEntry});
    }

    public void addMagixEventListener(String eventName, EventListener eventListener) {
        this.eventListenerConcurrentHashMap.put(eventName, eventListener);
        bridge.callHandler("room.addMagixEventListener", new Object[]{eventName});
    }

    public void removeMagixEventListener(String eventName) {
        this.eventListenerConcurrentHashMap.remove(eventName);
        bridge.callHandler("room.removeMagixEventListener", new Object[]{eventName});
    }


    public void externalDeviceEventDown(RoomMouseEvent mouseEvent) {
        bridge.callHandler("room.externalDeviceEventDown", new Object[]{mouseEvent});
    }

    public void externalDeviceEventMove(RoomMouseEvent mouseEvent) {
        bridge.callHandler("room.externalDeviceEventMove", new Object[]{mouseEvent});
    }

    public void externalDeviceEventUp(RoomMouseEvent mouseEvent) {
        bridge.callHandler("room.externalDeviceEventUp", new Object[]{mouseEvent});
    }

    public void externalDeviceEventLeave(RoomMouseEvent mouseEvent) {
        bridge.callHandler("room.externalDeviceEventLeave", new Object[]{mouseEvent});
    }

}
