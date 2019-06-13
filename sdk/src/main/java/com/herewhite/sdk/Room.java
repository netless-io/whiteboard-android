package com.herewhite.sdk;

import android.content.Context;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.herewhite.sdk.domain.AkkoEvent;
import com.herewhite.sdk.domain.BroadcastState;
import com.herewhite.sdk.domain.EventEntry;
import com.herewhite.sdk.domain.EventListener;
import com.herewhite.sdk.domain.GlobalState;
import com.herewhite.sdk.domain.ImageInformation;
import com.herewhite.sdk.domain.ImageInformationWithUrl;
import com.herewhite.sdk.domain.MemberState;
import com.herewhite.sdk.domain.Point;
import com.herewhite.sdk.domain.Promise;
import com.herewhite.sdk.domain.RoomMember;
import com.herewhite.sdk.domain.RoomMouseEvent;
import com.herewhite.sdk.domain.RoomPhase;
import com.herewhite.sdk.domain.RoomState;
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
    private Integer timeDelay;
    private String uuid;
    private final Context context;
    private WhiteSdk sdk;
    private ConcurrentHashMap<String, EventListener> eventListenerConcurrentHashMap = new ConcurrentHashMap<>();

    public Room(String uuid, WhiteBroadView bridge, Context context, WhiteSdk sdk) {
        this.uuid = uuid;
        this.bridge = bridge;
        this.context = context;
        this.sdk = sdk;
        this.timeDelay = 0;
    }

    //region Set API
    public void setGlobalState(GlobalState globalState) {
        bridge.callHandler("room.setGlobalState", new Object[]{globalState});
    }

    public void setMemberState(MemberState memberState) {
        bridge.callHandler("room.setMemberState", new Object[]{memberState});
    }

    public void setViewMode(ViewMode viewMode) {
        bridge.callHandler("room.setViewMode", new Object[]{viewMode.name()});
    }

    /**
     * @deprecated use refreshViewSize instead.
     */
    @Deprecated
    public void setViewSize(int width, int height) {
        bridge.callHandler("room.setViewSize", new Object[]{width, height});
    }
    //endregion

    public void refreshViewSize() {
        bridge.callHandler("room.refreshViewSize", new Object[]{});
    }

    public void disconnect() {
        bridge.callHandler("room.disconnect", new Object[]{});
        this.sdk.releaseRoom(this.uuid);
    }

    public void disconnect(final Promise<Object> promise) {
        bridge.callHandler("room.disconnect", new Object[]{}, new OnReturnValue<Object>() {
            @Override
            public void onValue(Object o) {
                try {
                    promise.then(gson.fromJson(String.valueOf(o), GlobalState.class));
                } catch (AssertionError a) {
                    throw a;
                } catch (JsonSyntaxException e) {
                    Logger.error("An JsonSyntaxException occurred while parse json from disconnect", e);
                    promise.catchEx(new SDKError(e.getMessage()));
                } catch (Throwable e) {
                    Logger.error("An exception occurred in disconnect promise then method", e);
                    promise.catchEx(new SDKError(e.getMessage()));
                }
            }
        });
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


    //region Get API
    public void getGlobalState(final Promise<GlobalState> promise) {
        bridge.callHandler("room.getGlobalState", new Object[]{}, new OnReturnValue<Object>() {
            @Override
            public void onValue(Object o) {
                try {
                    promise.then(gson.fromJson(String.valueOf(o), GlobalState.class));
                } catch (AssertionError a) {
                    throw a;
                } catch (JsonSyntaxException e) {
                    Logger.error("An JsonSyntaxException occurred while parse json from getGlobalState", e);
                    promise.catchEx(new SDKError(e.getMessage()));
                } catch (Throwable e) {
                    Logger.error("An exception occurred in getGlobalState promise then method", e);
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
                } catch (AssertionError a) {
                    throw a;
                } catch (JsonSyntaxException e) {
                    Logger.error("An JsonSyntaxException occurred while parse json from getMemberState", e);
                    promise.catchEx(new SDKError(e.getMessage()));
                } catch (Throwable e) {
                    Logger.error("An exception occurred in getMemberState promise then method", e);
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
                } catch (AssertionError a) {
                    throw a;
                } catch (JsonSyntaxException e) {
                    Logger.error("An JsonSyntaxException occurred while parse json from getRoomMembers", e);
                    promise.catchEx(new SDKError(e.getMessage()));
                } catch (Throwable e) {
                    Logger.error("An exception occurred in getRoomMembers promise then method", e);
                    promise.catchEx(new SDKError(e.getMessage()));
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
                } catch (AssertionError a) {
                    throw a;
                } catch (JsonSyntaxException e) {
                    Logger.error("An JsonSyntaxException occurred while parse json from getBroadcastState", e);
                    promise.catchEx(new SDKError(e.getMessage()));
                } catch (Throwable e) {
                    Logger.error("An exception occurred in getBroadcastState promise then method", e);
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
                } catch (AssertionError a) {
                    throw a;
                } catch (JsonSyntaxException e) {
                    Logger.error("An JsonSyntaxException occurred while parse json from getSceneState", e);
                    promise.catchEx(new SDKError(e.getMessage()));
                } catch (Throwable e) {
                    Logger.error("An exception occurred in getSceneState promise then method", e);
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
                } catch (AssertionError a) {
                    throw a;
                } catch (JsonSyntaxException e) {
                    Logger.error("An JsonSyntaxException occurred while parse json from getScenes", e);
                    promise.catchEx(new SDKError(e.getMessage()));
                } catch (Throwable e) {
                    Logger.error("An exception occurred in getScenes promise then method", e);
                    promise.catchEx(new SDKError(e.getMessage()));
                }
            }
        });
    }

    public void getZoomScale(final Promise<Number> promise) {
        bridge.callHandler("room.getZoomScale", new OnReturnValue<Object>() {
            @Override
            public void onValue(Object o) {
                try {
                    promise.then(gson.fromJson(String.valueOf(o), Number.class));
                } catch (AssertionError a) {
                    throw a;
                } catch (JsonSyntaxException e) {
                    Logger.error("An JsonSyntaxException occurred while parse json from getZoomScale", e);
                    promise.catchEx(new SDKError(e.getMessage()));
                } catch (Throwable e) {
                    Logger.error("An exception occurred in getZoomScale promise then method", e);
                    promise.catchEx(new SDKError(e.getMessage()));
                }
            }
        });
    }

    public void getRoomPhase(final Promise<RoomPhase> promise) {
        bridge.callHandler("room.getRoomPhase", new OnReturnValue<Object>() {
            @Override
            public void onValue(Object o) {
                try {
                    //TODO:待测试
                    promise.then(RoomPhase.valueOf(String.valueOf(o)));
                } catch (AssertionError a) {
                    throw a;
                } catch (JsonSyntaxException e) {
                    Logger.error("An JsonSyntaxException occurred while parse json from getRoomPhase", e);
                    promise.catchEx(new SDKError(e.getMessage()));
                } catch (Throwable e) {
                    Logger.error("An exception occurred in getRoomPhase promise then method", e);
                    promise.catchEx(new SDKError(e.getMessage()));
                }
            }
        });
    }

    public void getRoomState(final Promise<RoomState> promise) {
        bridge.callHandler("room.state.getRoomState", new OnReturnValue<Object>() {
            @Override
            public void onValue(Object o) {
                try {
                    //TODO:待测试反序列化是否正确
                    promise.then(gson.fromJson(String.valueOf(o), RoomState.class));
                } catch (AssertionError a) {
                    throw a;
                } catch (JsonSyntaxException e) {
                    Logger.error("An JsonSyntaxException occurred while parse json from getRoomState", e);
                    promise.catchEx(new SDKError(e.getMessage()));
                } catch (Throwable e) {
                    Logger.error("An exception occurred in getRoomState promise then method", e);
                    promise.catchEx(new SDKError(e.getMessage()));
                }
            }
        });
    }
    //endregion

    //region Scene API
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

    public void cleanScene(boolean retainPpt) {
        bridge.callHandler("room.cleanScene", new Object[]{retainPpt});
    }
    //endregion

    public void zoomChange(double scale) {
        bridge.callHandler("room.zoomChange", new Object[]{scale});
    }

    public void disableOperations(boolean disableOperations) {
        bridge.callHandler("room.disableOperations", new Object[]{disableOperations});
    }

    public void convertToPointInWorld(double x, double y, final Promise<Point> promise) {
        bridge.callHandler("room.convertToPointInWorld", new Object[]{x, y}, new OnReturnValue<Object>() {
            @Override
            public void onValue(Object o) {
                try {
                    promise.then(gson.fromJson(String.valueOf(o), Point.class));
                } catch (AssertionError a) {
                    throw a;
                } catch (JsonSyntaxException e) {
                    Logger.error("An JsonSyntaxException occurred while parse json from convertToPointInWorld", e);
                    promise.catchEx(new SDKError(e.getMessage()));
                } catch (Throwable e) {
                    Logger.error("An exception occurred in convertToPointInWorld promise then method", e);
                    promise.catchEx(new SDKError(e.getMessage()));
                }
            }
        });
    }

    //region Delay API
    public void setTimeDelay(Integer timeDelay) {
        bridge.callHandler("room.setTimeDelay", new Object[]{timeDelay * 1000});
        this.timeDelay = timeDelay;
    }

    public Integer getTimeDelay() {
        return this.timeDelay;
    }
    //endregion

    //region Event API
    public void fireMagixEvent(EventEntry eventEntry) {
        EventListener eventListener = eventListenerConcurrentHashMap.get(eventEntry.getEventName());
        if (eventListener != null) {
            try {
                eventListener.onEvent(eventEntry);
            } catch (AssertionError a) {
                throw a;
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
    //endregion
}
