package com.herewhite.sdk;

import android.content.Context;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import com.herewhite.sdk.domain.AkkoEvent;
import com.herewhite.sdk.domain.BroadcastState;
import com.herewhite.sdk.domain.CameraConfig;
import com.herewhite.sdk.domain.EventEntry;
import com.herewhite.sdk.domain.EventListener;
import com.herewhite.sdk.domain.GlobalState;
import com.herewhite.sdk.domain.ImageInformation;
import com.herewhite.sdk.domain.ImageInformationWithUrl;
import com.herewhite.sdk.domain.MemberState;
import com.herewhite.sdk.domain.Point;
import com.herewhite.sdk.domain.Promise;
import com.herewhite.sdk.domain.RoomMember;
import com.herewhite.sdk.domain.RoomPhase;
import com.herewhite.sdk.domain.RoomState;
import com.herewhite.sdk.domain.SDKError;
import com.herewhite.sdk.domain.Scene;
import com.herewhite.sdk.domain.SceneState;
import com.herewhite.sdk.domain.TextareaBox;
import com.herewhite.sdk.domain.ViewMode;

import org.json.JSONObject;

import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import wendu.dsbridge.OnReturnValue;

/**
 * 实时房间操作类
 */
public class Room extends Displayer {

    private final SyncDisplayerState<RoomState> syncRoomState;
    private RoomPhase roomPhase = RoomPhase.connected;

    private Integer timeDelay;
    private ConcurrentHashMap<String, EventListener> eventListenerConcurrentHashMap = new ConcurrentHashMap<>();

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

    //region Set API
    /**
     * 设置全局共享状态
     *
     * @param globalState 自定义字段，可以传入 {@link GlobalState} 子类
     */
    public void setGlobalState(GlobalState globalState) {
        syncRoomState.putDisplayerStateProperty("globalState", globalState);
        bridge.callHandler("room.setGlobalState", new Object[]{globalState});
    }

    /**
     * 设置当前用户教具
     *
     * @param memberState {@link MemberState} 只需要传入需要修改的部分即可。
     */
    public void setMemberState(MemberState memberState) {
        syncRoomState.putDisplayerStateProperty("memberState", memberState);
        bridge.callHandler("room.setMemberState", new Object[]{memberState});
    }

    /**
     *
     * 切换视角状态
     *
     * 1. 主播模式：房间只存在一个主播。成为主播后，房间中其他用户（包括新加入用户）的视角模式，都会切换为跟随模式。
     * 2. 跟随模式：当用户进行操作时，会从跟随模式，切换成自由模式。
     *            可以通过： 禁止响应用户操作 {@link #disableOperations(boolean)} ，来保证用户保持在跟随模式。
     * 3. 自由模式：当房间中，不存在主播时，所有人默认均为自由模式。
     *
     * @param viewMode 视角选项
     * @see ViewMode
     */
    public void setViewMode(ViewMode viewMode) {
        bridge.callHandler("room.setViewMode", new Object[]{viewMode.name()});
    }

    /**
     * @deprecated use {@link #refreshViewSize} instead.
     */
    @Deprecated
    public void setViewSize(int width, int height) {
        bridge.callHandler("room.setViewSize", new Object[]{width, height});
    }
    //endregion

    /**
     * 刷新当前白板的视觉矩形。
     * 当 WhiteboardView 大小出现改变时，需要手动调用该方法。
     */
    public void refreshViewSize() {
        bridge.callHandler("room.refreshViewSize", new Object[]{});
    }

    /**
     * 主动断连，断开后，该 room 实例将无法使用。
     */
    public void disconnect() {
        bridge.callHandler("room.disconnect", new Object[]{});
        this.sdk.releaseRoom(this.uuid);
    }

    /**
     * 主动断连，断开后，该 room 实例将无法使用。
     *
     * @param promise 退出后回调
     */
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

    /**
     * 插入图片占位区域
     *
     * 可以使用 {@link #insertImage(ImageInformationWithUrl)} 封装 API，插入网络图片。
     * @param imageInfo {@link ImageInformation}
     */
    public void insertImage(ImageInformation imageInfo) {
        bridge.callHandler("room.insertImage", new Object[]{imageInfo});
    }

    /**
     * 将特定 uuid 的占位区域，替换成网络图片
     *
     * @param uuid 占位 uuid，需要有唯一性。
     * @param url  图片网络地址。
     */
    public void completeImageUpload(String uuid, String url) {
        bridge.callHandler("room.completeImageUpload", new Object[]{uuid, url});
    }

    /**
     * 插入网络图片。
     *
     * 该 API 封装 {@link #insertImage} 以及 {@link #completeImageUpload(String, String)} API，传入图片
     * 信息 {@link ImageInformationWithUrl} 即可上传网络图片。
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

    /**
     * 同步 获取房间全局状态，只支持 sdk 已有的字段。
     *
     * @see GlobalState
     */
    public GlobalState getGlobalState() {
        return syncRoomState.getDisplayerState().getGlobalState();
    }

    /**
     * 异步 获取房间全局状态，只返回 sdk 的 GlobalState 类。
     * @deprecated 获取 sdk 的全局状态，请使用 {@link #getGlobalState()} API。如果需要获取自定义的全局状态，请使用 {@link #getGlobalState(Class, Promise)}
     * @param promise 完成回调
     */
    public void getGlobalState(final Promise<GlobalState> promise) {
        getGlobalState(GlobalState.class, promise);
    }

    public <T>void getGlobalState(final Class<T> classOfT, final Promise<T> promise) {
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
     * 同步 获取当前用户教具状态
     *
     * @return 用户教具状态
     * @see MemberState
     */
    public MemberState getMemberState() {
        return syncRoomState.getDisplayerState().getMemberState();
    }

    /**
     * 异步 获取当前用户教具状态
     *
     * @deprecated 请使用 {@link #getMemberState()} 同步 API，进行获取。
     * @param promise 完成回调
     */
    @Deprecated
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
     * 同步 获取房间中用户列表
     *
     * @return 用户列表
     * @see RoomMember
     */
    public RoomMember[] getRoomMembers() {
        return syncRoomState.getDisplayerState().getRoomMembers();
    }

    /**
     * 异步 获取房间中用户列表
     *
     * @deprecated 请使用 {@link #getRoomMembers()} 同步 API 进行获取。
     * @param promise 完成回调
     */
    @Deprecated
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
     * 同步 获取用户视角状态
     *
     * @see BroadcastState
     */
    public BroadcastState getBroadcastState() {
        return syncRoomState.getDisplayerState().getBroadcastState();
    }

    /**
     * 异步 获取用户视角状态
     *
     * @deprecated 请使用 {@link #getBroadcastState()} 同步 API 进行获取。
     * @param promise 完成回调
     */
    @Deprecated
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
     * 同步 获取房间当前场景目录下场景状态。
     * @see SceneState
     */
    public SceneState getSceneState() {
        return syncRoomState.getDisplayerState().getSceneState();
    }

    /**
     * 异步 获取房间当前场景目录下场景状态
     *
     * @deprecated 请使用 {@link #getSceneState()} 同步 API 进行获取。
     * @param promise 完成回调
     */
    @Deprecated
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
     * 同步 获取房间当前场景目录下场景列表
     */
    public Scene[] getScenes() {
        return this.getSceneState().getScenes();
    }

    /**
     * 异步 获取房间当前场景目录下场景列表
     *
     * @deprecated 请使用 {@link #getScenes()} 同步 API 进行获取。
     * @param promise 完成回调
     */
    @Deprecated
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

    public double getZoomScale() {
        return syncRoomState.getDisplayerState().getZoomScale();
    }


    /**
     * 异步 获取房间缩放比例
     *
     * @deprecated 请使用 {@link #getZoomScale()} 同步 API 进行获取。
     * @param promise 获取完成后回调
     */
    @Deprecated
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
     * 同步 获取房间连接状态
     *
     * @see RoomPhase
     */
    public RoomPhase getRoomPhase() {
        return this.roomPhase;
    }

    /**
     * 异步 获取房间连接状态
     *
     * @deprecated 请使用 {@link #getRoomPhase()} 同步 API 进行获取。
     * @param promise 获取所有状态后，完成回调
     */
    @Deprecated
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
     * 同步 获取实时房间内所有状态
     *
     * @return RoomState
     * @see RoomState
     */
    public RoomState getRoomState() {
        return syncRoomState.getDisplayerState();
    }

    /**
     * 异步 获取实时房间内所有状态
     *
     * @deprecated 请使用 {@link #getRoomState()} 同步 API，进行获取。
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
     */
    public void setScenePath(String path) {
        bridge.callHandler("room.setScenePath", new Object[]{path});
    }

    /**
     * 在当前场景目录中，切换当前场景。
     *
     * 当 index 超出当前目录的场景数，会报错，进入 promise 错误回调
     *
     * @param index 目标场景在当前场景目录下的 index。
     * @param promise 设置完后回调
     */
    public void setSceneIndex(Integer index, final Promise<Boolean> promise) {
        bridge.callHandler("room.setSceneIndex", new Object[]{index}, new OnReturnValue<String>() {
            @Override
            public void onValue(String result) {
                JsonObject jsonObject = gson.fromJson(result, JsonObject.class);
                if (jsonObject.has("__error")) {
                    String msg = "Unknow exception";
                    String jsStack = "Unknow stack";
                    if (jsonObject.getAsJsonObject("__error").has("message")) {
                        msg = jsonObject.getAsJsonObject("__error").get("message").getAsString();
                    }
                    if (jsonObject.getAsJsonObject("__error").has("jsStack")) {
                        jsStack = jsonObject.getAsJsonObject("__error").get("jsStack").getAsString();
                    }
                    promise.catchEx(new SDKError(msg, jsStack));
                } else {
                    promise.then(true);
                }
            }
        });
    }

    /**
     * 插入场景
     *
     * 向特定场景目录中，插入多个场景。
     * 插入场景后，如果要将显示插入的场景，需要调用 setScenePath API，设置当前插入场景。
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
     */
    public void pptNextStep() {
        bridge.callHandler("ppt.nextStep", new Object[]{});
    }

    /**
     * 动态 PPT 上一步操作。当前 ppt 页面的动画全部回退完成时，会回滚至上一页 ppt 页面（场景）
     */
    public void pptPreviousStep() {
        bridge.callHandler("ppt.previousStep", new Object[]{});
    }

    /**
     * 改变房间缩放比例
     * @deprecated 使用 {@link #moveCamera(CameraConfig)} 调整缩放比例。同时支持动画选项
     * @param scale 缩放比例，2x 表示内容放大两倍。
     */
    @Deprecated
    public void zoomChange(double scale) {
        bridge.callHandler("room.zoomChange", new Object[]{scale});
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
     * 禁止用户视角变化（缩放，移动）。禁止后，开发者仍然可以通过 SDK API 移动视角。
     *
     * @param disableOperations true:禁止用户主动改变视角；false:允许用户主动改变视角。默认:false。
     */
    public void disableCameraTransform(final boolean disableOperations) {
        bridge.callHandler("room.disableCameraTransform", new Object[]{disableOperations});
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
     * 将以白板左上角为原点的 Android 坐标系坐标，转换为白板内部坐标系（坐标原点为白板初始化时中点位置，坐标轴方向相同）的坐标
     *
     * @param x       the Android 端 x 坐标
     * @param y       the Android 端 y 坐标
     * @param promise the promise
     */
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

    //region Event API
    //TODO: 支持同一个自定义事件，多个回调。（看需求）
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

    /**
     * 发送自定义事件，所有注册监听事件的客户端，都会收到通知。
     *
     * @param eventEntry 自定义事件内容，{@link AkkoEvent}
     */
    public void dispatchMagixEvent(AkkoEvent eventEntry) {
        bridge.callHandler("room.dispatchMagixEvent", new Object[]{eventEntry});
    }

    /**
     * 注册自定义事件监听，接受对应名称的自定义事件通知（包括自己发送的）。
     * TODO:目前 Android 端，同一个自定义事件（名），只支持单个回调。只有 Web 端支持一个自定义事件，调用多个回调。
     * @param eventName     需要监听自定义事件名称
     * @param eventListener 自定义事件回调，重复调用时，后者会覆盖前者。
     */
    public void addMagixEventListener(String eventName, EventListener eventListener) {
        this.eventListenerConcurrentHashMap.put(eventName, eventListener);
        bridge.callHandler("room.addMagixEventListener", new Object[]{eventName});
    }

    /**
     * 移除自定义事件监听
     * TODO:目前 Android 端同一个自定义事件（名），只支持单个回调。移除时，只需要传入自定义事件名称即可。
     * @param eventName 需要移除监听的自定义事件名称
     */
    public void removeMagixEventListener(String eventName) {
        this.eventListenerConcurrentHashMap.remove(eventName);
        bridge.callHandler("room.removeMagixEventListener", new Object[]{eventName});
    }
    //endregion
}
