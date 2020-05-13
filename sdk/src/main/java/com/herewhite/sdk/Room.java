package com.herewhite.sdk;

import android.content.Context;
import androidx.annotation.Nullable;

import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import com.herewhite.sdk.domain.AkkoEvent;
import com.herewhite.sdk.domain.BroadcastState;
import com.herewhite.sdk.domain.CameraConfig;
import com.herewhite.sdk.domain.EventEntry;
import com.herewhite.sdk.domain.EventListener;
import com.herewhite.sdk.domain.FrequencyEventListener;
import com.herewhite.sdk.domain.GlobalState;
import com.herewhite.sdk.domain.ImageInformation;
import com.herewhite.sdk.domain.ImageInformationWithUrl;
import com.herewhite.sdk.domain.MemberState;
import com.herewhite.sdk.domain.Promise;
import com.herewhite.sdk.domain.RoomMember;
import com.herewhite.sdk.domain.RoomPhase;
import com.herewhite.sdk.domain.RoomState;
import com.herewhite.sdk.domain.SDKError;
import com.herewhite.sdk.domain.Scene;
import com.herewhite.sdk.domain.SceneState;
import com.herewhite.sdk.domain.ViewMode;

import org.json.JSONObject;
import java.util.UUID;

import wendu.dsbridge.OnReturnValue;

/**
 * 实时房间操作类
 */
public class Room extends Displayer {

    private final SyncDisplayerState<RoomState> syncRoomState;
    private RoomPhase roomPhase = RoomPhase.connected;

    void setDisconnectedBySelf(Boolean disconnectedBySelf) {
        this.disconnectedBySelf = disconnectedBySelf;
    }

    public Boolean getDisconnectedBySelf() {
        return disconnectedBySelf;
    }

    private Boolean disconnectedBySelf = false;

    public Boolean getWritable() {
        return writable;
    }

    void setWritable(Boolean writable) {
        this.writable = writable;
    }

    private Boolean writable;
    private Integer timeDelay;
    private Long observerId;

    public Room(String uuid, WhiteboardView bridge, Context context, WhiteSdk sdk, SyncDisplayerState<RoomState> syncRoomState) {
        super(uuid, bridge, context, sdk);
        this.timeDelay = 0;
        this.syncRoomState = syncRoomState;
    }

    SyncDisplayerState<RoomState> getSyncRoomState() {
        return syncRoomState;
    }

    void setRoomPhase(RoomPhase roomPhase) {
        this.roomPhase = roomPhase;
    }

    /**
     * 获取当前用户在白板事实房间中的 memberId，该 id 从 0 开始递增
     * 参考 {@link RoomMember#getMemberId()}
     * @return 用户 memberId
     * @since 2.4.11
     */
    public Long getObserverId() {
        return observerId;
    }

    void setObserverId(Long observerId) {
        this.observerId = observerId;
    }

    //region Set API
    /**
     * 设置全局共享状态，会立刻更新同步 GlobalState。 {@link #getGlobalState()} 可以立刻获取到最新状态。
     *
     * @param globalState 自定义字段，可以传入 {@link GlobalState} 子类
     */
    public void setGlobalState(GlobalState globalState) {
        syncRoomState.putDisplayerStateProperty("globalState", globalState);
        bridge.callHandler("room.setGlobalState", new Object[]{globalState});
    }

    /**
     * 设置当前用户教具，会立刻更新 MemberState。 {@link #getMemberState()} 可以立刻获取到最新设置。
     *
     * @param memberState {@link MemberState} 只需要传入需要修改的部分即可。
     */
    public void setMemberState(MemberState memberState) {
        syncRoomState.putDisplayerStateProperty("memberState", memberState);
        bridge.callHandler("room.setMemberState", new Object[]{memberState});
    }

    /**
     * 切换视角模式
     *
     * 1. 主播模式：房间只存在一个主播。成为主播后，房间中其他用户（包括新加入用户）的视角模式，都会切换为跟随模式。
     * 2. 跟随模式：当用户进行操作时，会从跟随模式，切换成自由模式。
     *            可以通过： 禁止响应用户操作 {@link #disableOperations(boolean)} ，来保证用户保持在跟随模式。
     * 3. 自由模式：当房间中，不存在主播时，所有人默认均为自由模式。
     *
     * 切换视角后，{@link #getBroadcastState()} 方法需要等待服务器更新后，才能更新。
     * 可以使用 {@link #getBroadcastState(Promise)} 强制更新信息
     *
     * @param viewMode {@link ViewMode}
     */
    public void setViewMode(ViewMode viewMode) {
        bridge.callHandler("room.setViewMode", new Object[]{viewMode.name()});
    }

    /**
     * @deprecated 请使用 {@link #refreshViewSize}
     */
    @Deprecated
    public void setViewSize(int width, int height) {
        refreshViewSize();
    }
    //endregion

    /**
     * 主动断连，断开后，当前 room 实例将无法使用。
     * 再次使用，需要使用 {@link WhiteSdk#joinRoom(RoomParams, RoomCallbacks, Promise)} 重新创建实例
     * 如需退出后回调，请使用 {@link #disconnect(Promise)}
     */
    public void disconnect() {
        disconnect(null);
    }

    /**
     * 主动断连，断开后，该 room 实例将无法使用。
     * 再次使用，需要使用 {@link WhiteSdk#joinRoom(RoomParams, RoomCallbacks, Promise)} 重新创建实例
     *
     * @param promise 退出后回调
     */
    public void disconnect(@Nullable final Promise<Object> promise) {
        setDisconnectedBySelf(true);
        bridge.callHandler("room.disconnect", new Object[]{}, new OnReturnValue<Object>() {
            @Override
            public void onValue(Object o) {
                if (promise == null) {
                    return;
                }
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
    }

    /**
     * 插入占位区域，一般配合 {@link #completeImageUpload(String, String)} 完成插入图片功能。
     * 如图片网络地址已知，推荐使用 {@link #insertImage(ImageInformationWithUrl)} API，插入网络图片。
     * @param imageInfo {@link ImageInformation}
     */
    public void insertImage(ImageInformation imageInfo) {
        bridge.callHandler("room.insertImage", new Object[]{imageInfo});
    }

    /**
     * 将特定 uuid 的占位区域，替换成网络图片
     *
     * @param uuid 占位 uuid，需要有唯一性，即 {@link #insertImage(ImageInformation)} 中传入的 uuid
     * @param url  图片网络地址。
     */
    public void completeImageUpload(String uuid, String url) {
        bridge.callHandler("room.completeImageUpload", new Object[]{uuid, url});
    }

    /**
     * 插入网络图片
     *
     * 该 API 封装 {@link #insertImage} 以及 {@link #completeImageUpload(String, String)} API，上传网络图片。
     *
     * @param imageInformationWithUrl 图片信息 {@link ImageInformationWithUrl}
     */
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

    //region GET API
    /**
     * 同步API 获取房间全局状态
     * 如已通过 {@link com.herewhite.sdk.domain.WhiteDisplayerState#setCustomGlobalStateClass(Class)}
     * 设置好自定义 GlobalState。在获取后，可以直接进行强转。
     * 调用 {@link #setGlobalState(GlobalState)} API 后，可以立刻调用该 API
     *
     * @see GlobalState
     * @since 2.4.0
     */
    public GlobalState getGlobalState() {
        return syncRoomState.getDisplayerState().getGlobalState();
    }

    /**
     * 异步API 强制获取房间全局状态。
     * 如已通过 {@link com.herewhite.sdk.domain.WhiteDisplayerState#setCustomGlobalStateClass(Class)}
     * 设置好自定义 GlobalState。在获取后，可以直接进行强转。
     * @deprecated 建议使用 {@link #getGlobalState()} API。
     * @param promise 完成回调
     */
    public void getGlobalState(final Promise<GlobalState> promise) {
        getGlobalState(GlobalState.class, promise);
    }

    /**
     * 异步API 获取房间全局状态，根据传入的 Class 类型，在回调中返回对应的实例
     *
     * @param <T>      globalState 反序列化的类
     * @param classOfT 泛型 T 的 class 类型
     * @param promise  完成回调，其中返回值传入的 class 的实例
     * @since 2.4.8
     */
    private  <T>void getGlobalState(final Class<T> classOfT, final Promise<T> promise) {
        bridge.callHandler("room.getGlobalState", new Object[]{}, new OnReturnValue<Object>() {
            @Override
            public void onValue(Object o) {
            T customState = null;
            try {
                customState = gson.fromJson(String.valueOf(o), classOfT);
            } catch (AssertionError a) {
                throw a;
            } catch (Throwable e) {
                Logger.error("An exception occurred while parse json from getGlobalState for customState", e);
                promise.catchEx(new SDKError((e.getMessage())));
            }
            if (customState == null) {
                return;
            }
            try {
                promise.then(customState);
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

    /**
     * 同步API 获取当前用户教具状态,使用 {@link #setMemberState(MemberState)} 该 API 内容会立刻更新
     *
     * @return 用户教具状态 {@link MemberState}
     * @since 2.4.0
     */
    public MemberState getMemberState() {
        return syncRoomState.getDisplayerState().getMemberState();
    }

    /**
     * 异步API 获取当前用户教具状态
     *
     * @deprecated 请使用 {@link #getMemberState()} 同步 API，进行获取。
     * @param promise 完成回调
     * @see MemberState
     */
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

    /**
     * 同步API 获取房间中用户列表
     * 当有用户加入时，会在回调中自动更新该属性。由于本地用户没有任何操作可以更新该 API，所以可以在所有代码中都直接使用
     * 同步 API
     *
     * @return 用户列表
     * @see RoomMember
     * @since 2.4.0
     */
    public RoomMember[] getRoomMembers() {
        return syncRoomState.getDisplayerState().getRoomMembers();
    }

    /**
     * 异步API 获取房间中用户列表
     *
     * @deprecated 请使用 {@link #getRoomMembers()} 同步 API 进行获取。
     * @param promise 完成回调
     */
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

    /**
     * 同步缓存API 获取用户视角状态
     * 当调用 {@link #setViewMode(ViewMode)} 时，{@link BroadcastState} 无法立刻更新，
     * 此时可以调用 {@link #getBroadcastState(Promise)} 异步API 获取状态。
     *
     * @see BroadcastState
     * @since 2.4.0
     */
    public BroadcastState getBroadcastState() {
        return syncRoomState.getDisplayerState().getBroadcastState();
    }

    /**
     * 异步API 获取用户视角状态
     *
     * @deprecated 请使用 {@link #getBroadcastState()} 同步 API 进行获取。
     * @param promise 完成回调
     */
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
     * 同步缓存API 获取房间当前场景目录下场景状态。
     *
     * 当调用 {@link #setScenePath(String, Promise)}、{@link #setScenePath(String)}、{@link #putScenes(String, Scene[], int)}
     * 等 API 时，该 API 不会立即更新，此时如需立即获取 SceneState，请使用 {@link #getSceneState(Promise)} 异步API。
     *
     * @see SceneState
     * @since 2.4.0
     */
    public SceneState getSceneState() {
        return syncRoomState.getDisplayerState().getSceneState();
    }

    /**
     * 异步API 获取房间当前场景目录下场景状态
     * @param promise 完成回调
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
     * 同步缓存API 获取房间当前场景目录下场景列表。
     *
     * 当调用 {@link #setScenePath(String, Promise)}、{@link #setScenePath(String)}、{@link #putScenes(String, Scene[], int)}
     * 等 API 后，该 API 不会立即更新，此时如需立即获取 SceneState，请使用 {@link #getScenes(Promise)} 异步API。
     *
     * @since 2.4.0
     */
    public Scene[] getScenes() {
        return this.getSceneState().getScenes();
    }

    /**
     * 异步API 获取房间当前场景目录下场景列表。
     *
     * @param promise 完成回调
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


    /**
     * 同步缓存API 获取当前用户缩放比例。
     *
     * 当调用 {@link #zoomChange(double)}、{@link #moveCamera(CameraConfig)} API 进行调整缩放比例后，该 API 不会立刻更新
     * 此时请调用 {@link #getZoomScale()} 异步API
     *
     * @return 房间缩放比例
     * @since 2.4.0
     */
    public double getZoomScale() {
        return syncRoomState.getDisplayerState().getZoomScale();
    }

    /**
     * 异步API 获取房间缩放比例
     *
     * 一般情况下，请使用 {@link #getZoomScale()} 同步 API 进行获取。
     * @param promise 获取完成后回调
     */
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

    /**
     * 同步缓存API 获取房间连接状态
     *
     * 当主动调用 {@link #disconnect()} {@link #disconnect(Promise)} API 时，该 API 无法立即更新，此时可以使用
     * {@link #getRoomPhase()} 异步 API
     * @see RoomPhase
     * @since 2.4.0
     */
    public RoomPhase getRoomPhase() {
        return this.roomPhase;
    }

    /**
     * 异步 获取房间连接状态
     *
     * 普通情况下，请使用 {@link #getRoomPhase()} 同步 API 进行获取。
     * @param promise 获取所有状态后，完成回调
     */
    public void getRoomPhase(final Promise<RoomPhase> promise) {
        bridge.callHandler("room.getRoomPhase", new OnReturnValue<Object>() {
            @Override
            public void onValue(Object o) {
                try {
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

    /**
     * 同步缓存API 获取实时房间内所有状态。
     * 当调用场景 API 后，想要立即获取 sceneState 相关内容时，请使用异步 {@link #getRoomState(Promise)}
     *
     * @return RoomState
     * @see RoomState
     * @since 2.4.0
     */
    public RoomState getRoomState() {
        return syncRoomState.getDisplayerState();
    }

    /**
     * 异步API 获取实时房间内所有状态
     *
     * 如果只是简单获取房间状态，请使用 {@link #getRoomState()} 同步 API，进行获取。
     * @param promise 获取所有状态后，完成回调
     */
    public void getRoomState(final Promise<RoomState> promise) {
        bridge.callHandler("room.state.getDisplayerState", new OnReturnValue<Object>() {
            @Override
            public void onValue(Object o) {
                try {
                    promise.then(gson.fromJson(String.valueOf(o), RoomState.class));
                } catch (AssertionError a) {
                    throw a;
                } catch (JsonSyntaxException e) {
                    Logger.error("An JsonSyntaxException occurred while parse json from getDisplayerState", e);
                    promise.catchEx(new SDKError(e.getMessage()));
                } catch (Throwable e) {
                    Logger.error("An exception occurred in getDisplayerState promise then method", e);
                    promise.catchEx(new SDKError(e.getMessage()));
                }
            }
        });
    }
    //endregion

    //region Scene API
    /**
     * 切换至特定的场景,如需同时获取报错，或完成回调，请使用 {@link #setScenePath(String, Promise)}
     *
     * 所有人都会同时切换到对应场景中
     *
     * 切换失败的几种原因：
     *  1. 路径不合法，请确定场景路径的定义。（以 "/" 开头）
     *  2. 场景路径，对应的场景不存在。
     *  3. 传入的地址，是场景目录，而不是场景路径。
     *
     * @param path 想要切换的场景 的场景路径(场景目录+场景名）
     */
    public void setScenePath(String path) {
        bridge.callHandler("room.setScenePath", new Object[]{path});
    }

    /**
     * 切换至特定的场景
     *
     * 所有人都会同时切换到对应场景中
     *
     * 切换失败的几种原因：
     *  1. 路径不合法，请确定场景路径的定义。（以 "/" 开头）
     *  2. 场景路径，对应的场景不存在。
     *  3. 传入的地址，是场景目录，而不是场景路径。
     *
     * @param path 想要切换的场景 的场景目录
     * @param promise 完成回调，如果出错会进入 catchEx
     */
    public void setScenePath(String path, final Promise<Boolean> promise) {
        bridge.callHandler("room.setScenePath", new Object[]{path}, new OnReturnValue<String>() {
            @Override
            public void onValue(String result) {
                SDKError sdkError = SDKError.promiseError(result);
                if (sdkError != null) {
                    promise.catchEx(sdkError);
                } else {
                    promise.then(true);
                }
            }
        });
    }

    /**
     * 在当前场景目录中，切换当前场景。
     *
     * 当 index 超出当前目录的场景数，会报错，进入 promise 错误回调
     *
     * @param index 目标场景在当前场景目录下的 index。
     * @param promise 设置完后回调
     */
    public void setSceneIndex(Integer index, @Nullable final Promise<Boolean> promise) {
        bridge.callHandler("room.setSceneIndex", new Object[]{index}, new OnReturnValue<String>() {
            @Override
            public void onValue(String result) {
                if (promise == null) {
                    return;
                }
                SDKError sdkError = SDKError.promiseError(result);
                if (sdkError != null) {
                    promise.catchEx(sdkError);
                } else {
                    promise.then(true);
                }
            }
        });
    }

    /**
     * 插入场景API，该 API 并不会自动切换到对应场景
     *
     * 向特定场景目录中，插入多个场景。
     * 插入场景后，如果要将显示插入的场景，需要调用 {@link #setScenePath(String)} API，设置当前插入场景。
     *
     * <pre>
     * {@code
     * room.putScenes("ppt", new Scene[]{new Scene("page1", new PptPage("https://white-pan.oss-cn-shanghai.aliyuncs.com/101/image/alin-rusu-1239275-unsplash_opt.jpg", 1024d, 768d))}, 0);
     * room.setScenePath("ppt" + "/page1");
     * }
     * </pre>
     *
     * @param dir    场景目录，不能为场景路径（不能向文件中插入文件）
     * @param scenes 插入的场景数组，单个场景为 {@link Scene}
     * @param index  插入的场景数组中，第一个场景在该目录中的索引位置；填写的数字，超出该场景目录中已有场景的个数时，
     *               会排在最后。
     */
    public void putScenes(String dir, Scene[] scenes, int index) {
        bridge.callHandler("room.putScenes", new Object[]{dir, scenes, index});
    }

    /**
     * 移动/重命名场景
     *
     * 当移动的当前场景目录时，当前场景路径也会自动改变。
     * targetDirOrPath 情况：
     *  1. 目录：将 sourcePath 场景 移动至该目录中，场景名称不变。
     *  2. 场景路径：将 sourcePath 场景，移动到该场景路径对应的目录中，并将 sourcePath 场景改名。
     *
     * @param sourcePath 需要移动的场景路径(只接受场景路径，无法移动目录)
     * @param targetDirOrPath 场景目录或场景路径
     */
    public void moveScene(String sourcePath, String targetDirOrPath) {
        bridge.callHandler("room.moveScene", new Object[]{sourcePath, targetDirOrPath});
    }

    /**
     * 移除场景或者场景组。房间中至少会存在一个场景。删除时，会自动清理不存在任何场景的场景目录。
     *
     * 1. 删光房间内的场景：自动生成一个名为 init，场景路径为 "/init" 的初始场景（房间初始化时的默认场景）
     * 2. 删除当前场景：场景会指向被删除场景同级目录中后一个场景（即 index 不发生改变）。
     * 3. 删除包含当前场景的场景目录 dirA：向上递归，寻找场景目录同级的场景目录。
     *      3.1 如果上一级目录中，还有其他场景目录 dirB（可映射文件夹概念），排在被删除的场景目录 dirA 后面，则当前场景会变成
     *      dirB 中的第一个场景（index 为 0）；
     *      3.2 如果上一级目录中，在 dirA 后不存在场景目录，则查看当前目录是否存在场景；
     *          如果存在，则该场景成为当前目录（index 为 0 的场景目录）。
     *      3.3 如果上一级目录中，dirA 后没有场景目录，当前上一级目录，也不存在任何场景；
     *          则查看是否 dirA 前面是否存在场景目录 dirC，选择 dir C 中的第一顺位场景
     *      3.4 以上都不满足，则继续向上递归执行该逻辑。
     *
     * @param dirOrPath 场景目录，或者场景路径。传入目录会删除目录下所有场景。
     */
    public void removeScenes(String dirOrPath) {
        bridge.callHandler("room.removeScenes", new Object[]{dirOrPath});
    }

    /**
     * 清屏 API，清理当前场景的所有内容
     *
     * @param retainPpt 是否保留 ppt 内容。true:保留 ppt；false：连 ppt 一起清空。
     */
    public void cleanScene(boolean retainPpt) {
        bridge.callHandler("room.cleanScene", new Object[]{retainPpt});
    }
    //endregion

    /**
     * 动态 PPT 下一步操作。当前 ppt 页面的动画已全部执行完成时，会进入下一页 ppt 页面（场景）
     * @since 2.2.0
     */
    public void pptNextStep() {
        bridge.callHandler("ppt.nextStep", new Object[]{});
    }

    /**
     * 动态 PPT 上一步操作。当前 ppt 页面的动画全部回退完成时，会回滚至上一页 ppt 页面（场景）
     * @since 2.2.0
     */
    public void pptPreviousStep() {
        bridge.callHandler("ppt.previousStep", new Object[]{});
    }

    /**
     * 改变房间缩放比例
     * @deprecated 使用 {@link #moveCamera(CameraConfig)} 调整缩放比例，新 API 同时支持动画选项
     * @param scale 缩放比例，2x 表示内容放大两倍。
     */
    @Deprecated
    public void zoomChange(double scale) {
        bridge.callHandler("room.zoomChange", new Object[]{scale});
    }

    /**
     * 返回 debug 用信息
     * @param promise
     * @since 2.6.2
     */
    public void debugInfo(final Promise<JSONObject> promise) {
        bridge.callHandler("room.state.debugInfo", new OnReturnValue<JSONObject>() {
            @Override
            public void onValue(JSONObject retValue) {
                promise.then(retValue);
            }
        });
    }

    /**
     * 禁止操作，不响应用户任何操作。
     *
     * @param disableOperations true:不响应用户操作；false:响应用户操作。默认:false。
     */
    public void disableOperations(final boolean disableOperations) {
        bridge.callHandler("room.disableOperations", new Object[]{disableOperations});
    }

    /**
     * 设置读写模式
     * @param writable 是否可写
     * @param promise 完成回调，并同时返回房间的读写状态
     * @since 2.6.1
     */
    public void setWritable(final boolean writable, @Nullable final Promise<Boolean> promise) {
        bridge.callHandler("room.setWritable", new Object[]{writable}, new OnReturnValue<String>() {
            @Override
            public void onValue(String result) {
                SDKError sdkError = SDKError.promiseError(result);
                if (promise == null) {
                    return;
                }

                if (sdkError != null) {
                    promise.catchEx(sdkError);
                } else {
                    JsonObject jsonObject = gson.fromJson(result, JsonObject.class);
                    Boolean isWritable = jsonObject.get("isWritable").getAsBoolean();
                    Long ObserverId = jsonObject.get("observerId").getAsLong();
                    setWritable(isWritable);
                    setObserverId(ObserverId);
                    promise.then(isWritable);
                }
            }
        });
    }

    /**
     * 禁止用户视角变化（缩放，移动）。禁止后，开发者仍然可以通过 SDK API 移动视角。
     *
     * @param disableCameraTransform true:禁止用户主动改变视角；false:允许用户主动改变视角。默认:false。
     * @since 2.2.0
     */
    public void disableCameraTransform(final boolean disableCameraTransform) {
        bridge.callHandler("room.disableCameraTransform", new Object[]{disableCameraTransform});
    }

    /**
     * 禁止用户教具操作
     *
     * @param disableOperations true:禁止用户教具操作；false:响应用户教具输入操作。默认:false。
     * @since 2.2.0
     */
    public void disableDeviceInputs(final boolean disableOperations) {
        bridge.callHandler("room.disableDeviceInputs", new Object[]{disableOperations});
    }

    /**
     * 主动延时 API，延迟播放远端白板同步画面（自己画的内容，会立即显示。防止用户感知错位）
     *
     * @param delaySec 延时秒数
     */
    //region Delay API
    public void setTimeDelay(Integer delaySec) {
        bridge.callHandler("room.setTimeDelay", new Object[]{delaySec * 1000});
        this.timeDelay = delaySec;
    }

    /**
     * 获取当前主动延时秒数
     *
     * @return 延时秒数
     */
    public Integer getTimeDelay() {
        return this.timeDelay;
    }
    //endregion

    /**
     * 自定义事件回调
     *
     * @param eventEntry {@link EventEntry} 自定义事件内容，相对于 {@link AkkoEvent} 多了发送者的 memberId
     */
    //region Event API
    void fireMagixEvent(EventEntry eventEntry) {
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

    void fireHighFrequencyEvent(EventEntry[] eventEntries) {
        FrequencyEventListener eventListener = frequencyEventListenerConcurrentHashMap.get(eventEntries[0].getEventName());
        if (eventListener != null) {
            try {
                eventListener.onEvent(eventEntries);
            } catch (AssertionError a) {
                throw a;
            } catch (Throwable e) {
                Logger.error("An exception occurred while sending the event", e);
            }
        }
    }

    /**
     * 发送自定义事件，所有注册监听事件的客户端，都会收到通知。
     *
     * @param eventEntry 自定义事件内容，{@link AkkoEvent}
     */
    public void dispatchMagixEvent(AkkoEvent eventEntry) {
        bridge.callHandler("room.dispatchMagixEvent", new Object[]{eventEntry});
    }

    //endregion
}
