package com.herewhite.sdk;

import androidx.annotation.Nullable;

import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import com.herewhite.sdk.domain.AkkoEvent;
import com.herewhite.sdk.domain.Appliance;
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
import com.herewhite.sdk.domain.SyncedState;
import com.herewhite.sdk.domain.ViewMode;
import com.herewhite.sdk.domain.WindowAppParam;
import com.herewhite.sdk.internal.Logger;
import com.herewhite.sdk.internal.RoomDelegate;

import org.json.JSONObject;

import java.util.UUID;

import wendu.dsbridge.OnReturnValue;

/**
 * `Room` 类，用于操作互动白板实时房间。
 */
public class Room extends Displayer {
    private SyncDisplayerState<RoomState> syncRoomState;
    private RoomPhase roomPhase = RoomPhase.connecting;

    void setDisconnectedBySelf(Boolean disconnectedBySelf) {
        this.disconnectedBySelf = disconnectedBySelf;
    }

    /**
     * 获取用户是否主动断开与白板房间的连接。
     * <p>
     * 该方法可以避免白板 SDK 反复重连，用户不断重新加入房间。
     */
    public Boolean getDisconnectedBySelf() {
        return disconnectedBySelf;
    }

    private Boolean disconnectedBySelf = false;

    /**
     * 获取本地用户在当前互动白板实时房间是否为互动模式。
     *
     * @return 获取本地用户是否为互动模式：
     * - `true`：本地用户在当前互动白板实时房间为互动模式，即可对白板进行读写操作。
     * - `false`: 本地用户在当前互动白板实时房间为订阅模式，即对白板只能进行读取操作。
     */
    public Boolean getWritable() {
        return writable;
    }

    void setWritable(Boolean writable) {
        this.writable = writable;
    }

    private Boolean writable;
    private Double timeDelay;
    private Long observerId;

    /// @cond test

    /**
     * 文档中隐藏，只有 sdk 内部初始化才有意义
     */
    Room(String uuid, JsBridgeInterface bridge, int densityDpi, boolean disableCallbackWhilePutting) {
        super(uuid, bridge, densityDpi);
        this.timeDelay = 0.0;
        this.syncRoomState = new SyncDisplayerState<>(RoomState.class, disableCallbackWhilePutting);
        this.syncRoomState.setListener(localRoomStateListener);
    }
    /// @endcond

    void setSyncRoomState(String stateJSON) {
        syncRoomState.syncDisplayerState(stateJSON);
    }

    void setRoomPhase(RoomPhase roomPhase) {
        this.roomPhase = roomPhase;
        if (roomListener != null) {
            roomListener.onPhaseChanged(roomPhase);
        }
    }

    /**
     * 获取用户在当前房间中的用户 ID。
     *
     * @since 2.4.11
     *
     * @return 用户 ID。
     */
    public Long getObserverId() {
        return observerId;
    }

    void setObserverId(Long observerId) {
        this.observerId = observerId;
    }

    //region Set API

    /**
     * 修改互动白板实时房间的公共全局状态。
     * <p>
     * 实时房间的 `globalState` 属性为公共全局变量，房间内所有用户都可以读取 `globalState`，互动模式用户可以修改 `globalState`。
     * 修改后的 `globalState` 会立即同步给所有用户。
     *
     * @param globalState 房间公共全局状态，详见 {@link com.herewhite.sdk.domain.GlobalState GlobalState}.
     */
    public void setGlobalState(GlobalState globalState) {
        syncRoomState.putProperty("globalState", globalState);
        bridge.callHandler("room.setGlobalState", new Object[]{globalState});
    }

    /**
     * 修改房间内的白板工具状态。
     * <p>
     * 调用该方法会立刻更新房间的 {@link com.herewhite.sdk.domain.MemberState MemberState}。
     * 你可以调用 {@link #getMemberState() getMemberState} 获取最新设置的白板工具状态。
     *
     * @param memberState 需要修改的白板工具状态，详见 {@link com.herewhite.sdk.domain.MemberState MemberState}。
     */
    public void setMemberState(MemberState memberState) {
        syncRoomState.putProperty("memberState", memberState);
        if (Appliance.TEXT.equals(memberState.getCurrentApplianceName())) {
            bridge.callFocusView();
        }
        bridge.callHandler("room.setMemberState", new Object[]{memberState});
    }

    //region operation


    /**
     * 复制选中内容。
     *
     *  @since 2.9.3
     * <p>
     * 该方法会将选中的内容存储到内存中，不会粘贴到白板中。
     *
     * @note 该方法仅当 {@link #disableSerialization disableSerialization} 设为 `false` 时生效。
     */
    public void copy() {
        bridge.callHandler("room.sync.copy", new Object[]{});
    }

    /**
     * 粘贴复制的内容。
     *
     * @since 2.9.3
     * <p>
     * 该方法会将 {@link #copy copy} 方法复制的内容粘贴到白板中（用户当前的视角中间）。
     *
     * @note
     * - 该方法仅当 {@link #disableSerialization disableSerialization} 设为 `false` 时生效。
     * - 多次调用该方法时，不能保证粘贴的内容每次都在用户当前的视角中间，可能会出现随机偏移。
     *
     */
    public void paste() {
        bridge.callHandler("room.sync.paste", new Object[]{});
    }

    /**
     * 复制并粘贴选中的内容。
     *
     * @since 2.9.3
     * <p>
     * 该方法会将选中的内容复制并粘贴到白板中（用户当前的视角中间）。
     *
     * @note
     * - 该方法仅当 {@link #disableSerialization disableSerialization} 设为 `false` 时生效。
     * - 多次调用该方法时，不能保证粘贴的内容每次都在用户当前的视角中间，可能会出现随机偏移。
     */
    public void duplicate() {
        bridge.callHandler("room.sync.duplicate", new Object[]{});
    }

    /**
     * 删除选中的内容。
     *
     * @since 2.9.3
     */
    public void deleteOperation() {
        bridge.callHandler("room.sync.delete", new Object[]{});
    }

    /**
     * 开启/禁止本地序列化。
     *
     * @since 2.9.3
     * <p>
     * 设置 `disableSerialization(true)` 后，以下方法将不生效：
     * - `redo`
     * - `undo`
     * - `duplicate`
     * - `copy`
     * - `paste`
     *
     * @warning
     * 如果要设置 `disableSerialization(false)`，必须确保同一房间内所有用户使用的 SDK 满足以下版本要求，否则会导致 app 客户端崩溃。
     * - Web SDK 2.9.3 或之后版本
     * - Android SDK 2.9.3 或之后版本
     * - iOS SDK 2.9.3 或之后版本
     *
     * @param disable 是否禁止本地序列化：
     *                - `true`：（默认）禁止开启本地序列化；
     *                - `false`： 开启本地序列化，即可以对本地操作进行解析。
     */
    public void disableSerialization(boolean disable) {
        bridge.callHandler("room.sync.disableSerialization", new Object[]{disable});
    }

    /**
     * 重做，即回退撤销操作。
     *
     * @since 2.9.3
     *
     * @note 该方法仅当 {@link #disableSerialization disableSerialization} 设为 `false` 时生效。
     *
     */
    public void redo() {
        bridge.callHandler("room.redo", new Object[]{});
    }

    /**
     * 撤销上一步操作。
     *
     * @since 2.9.3
     *
     * @note 该方法仅当 {@link #disableSerialization disableSerialization} 设为 `false` 时生效。
     */
    public void undo() {
        bridge.callHandler("room.undo", new Object[]{});
    }
    //endregion

    /**
     * 切换视角模式。
     * <p>
     * 互动白板实时房间支持对用户设置以下视角模式：
     * - `Broadcaster`: 主播模式。
     * - `Follower`：跟随模式。
     * - `Freedom`：（默认）自由模式。
     * <p>
     * 该方法的设置会影响房间内所有用户的视角模式：
     * - 当房间内不存在主播模式的用户时，所有用户的视角都默认为自由模式。
     * - 当一个用户的视角设置为主播模式后，房间内其他所有用户（包括新加入房间的用户）的视角会被自动设置为跟随模式。
     * - 当跟随模式的用户进行白板操作时，其视角会自动切换为自由模式。你可以调用 {@link #disableOperations(boolean) disableOperations}(true) 禁止跟随模式的用户操作白板，以保证其保持跟随模式。
     *
     * 该方法是异步操作。调用该方法后，你可以使用 {@link #getBroadcastState(Promise<BroadcastState> promise) getBroadcastState}[2/2] 获取最新设置的视角模式。
     *
     * @param viewMode 视角模式，详见 {@link com.herewhite.sdk.domain.ViewMode ViewMode}。
     */
    public void setViewMode(ViewMode viewMode) {
        bridge.callHandler("room.setViewMode", new Object[]{viewMode.name()});
    }

    //endregion

    /**
     * 主动断开与互动白板实时房间实例的连接。
     * <p>
     * 该方法会把与当前房间实例相关的所有资源释放掉。如果要再次加入房间，需要重新调用 `joinRoom`。
     *
     * @note 调用该方法不会触发回调。如果需要收到断开连接的回调，请使用 {@link #disconnect(@Nullable Promise<Object> promise) disconnect}[2/2]。
     */
    public void disconnect() {
        disconnect(null);
    }

    /**
     * 主动断开与互动白板实时房间实例的连接。
     * <p>
     * 该方法会把与当前房间实例相关的所有资源释放掉。如果要再次加入房间，需要重新调用 `joinRoom`。
     * <p>
     * 你可以在该方法中传入 `Promise<Object>` 接口实例，以获取方法调用结果。
     *
     * @param promise `Promise<Object>` 接口实例，详见 {@link com.herewhite.sdk.domain.Promise Promise}。你可以通过该接口获取 `disconnect` 的调用结果：
     *                - 如果方法调用成功，则返回房间的全局状态。
     *                - 如果方法调用失败，则返回错误信息。
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
    //region image

    /**
     * 插入图片显示区域。
     *
     * SDK 会根据你传入的 `ImageInformation` 在白板上设置并插入图片的显示区域。
     * 调用该方法后，还需要调用 {@link #completeImageUpload(String, String) completeImageUpload} 传入图片的 Url 地址，以在该显示区域插入并展示图片。
     *
     * @note 你也可以调用 {@link #insertImage(ImageInformationWithUrl) insertImage} 方法同时传入图片信息和图片的 Url 地址，在白板中插入并展示图片。
     *
     * @param imageInfo 图片信息，详见 {@link com.herewhite.sdk.domain.ImageInformation ImageInformation}。
     */
    public void insertImage(ImageInformation imageInfo) {
        bridge.callHandler("room.insertImage", new Object[]{imageInfo});
    }

    /**
     * 展示图片。
     * <p>
     * 该方法可以将指定的网络图片展示到指定的图片显示区域。
     *
     * @note 调用该方法前，请确保你已经调用 {@link #insertImage(ImageInformation) insertImage} 方法在白板上插入了图片的显示区域。
     *
     * @param uuid 图片显示区域的 UUID, 即在 {@link #insertImage(ImageInformation) insertImage} 方法的 {@link com.herewhite.sdk.domain.ImageInformation ImageInformation} 中传入的图片 UUID。
     * @param url  图片的 URL 地址。必须确保 app 客户端能访问该 URL，否则无法正常展示图片。
     */
    public void completeImageUpload(String uuid, String url) {
        bridge.callHandler("room.completeImageUpload", new Object[]{uuid, url});
    }

    /**
     * 插入并展示图片。
     * <p>
     * 该方法封装了 {@link #insertImage(ImageInformation) insertImage} 和 {@link #completeImageUpload(String, String) completeImageUpload} 方法。
     * 你可以在该方法中同时传入图片信息和图片的 URL，直接在白板中插入图片的显示区域并展示图片。
     *
     * @param imageInformationWithUrl 图片信息及图片的 URL 地址，详见 {@link com.herewhite.sdk.domain.ImageInformationWithUrl ImageInformationWithUrl}。
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
    //endregion

    /**
     * 插入文字
     * @param x
     * @param y
     * @param text
     */
    public void insertText(int x, int y, String text) {
        bridge.callHandler("room.insertText", new Object[]{x, y, text});
    }

    //region GET API

    /**
     * 获取房间的全局状态。
     *
     * @since 2.4.0
     *
     * @note
     * - 该方法为同步调用。
     * - 对于通过 {@link com.herewhite.sdk.domain.WhiteDisplayerState#setCustomGlobalStateClass(Class<T> classOfT) setCustomGlobalStateClass}
     * 方法设置的自定义 `GlobalState`，在获取后，可以直接进行强转。
     * - 调用 {@link #setGlobalState(GlobalState) setGlobalState} 方法后，可以立刻调用该方法。
     *
     * @return 房间的全局状态，详见 {@link com.herewhite.sdk.domain.GlobalState GlobalState}。
     *
     */
    public GlobalState getGlobalState() {
        return syncRoomState.getDisplayerState().getGlobalState();
    }

    /**
     * 获取房间全局状态。
     *
     * @deprecated 该方法已废弃。请使用 {@link #getGlobalState() getGlobalState}[1/2]。
     *
     * @note
     * - 该方法为异步调用。
     * - 对于通过 {@link com.herewhite.sdk.domain.WhiteDisplayerState#setCustomGlobalStateClass(Class<T> classOfT) setCustomGlobalStateClass} 方法设置的自定义 `GlobalState`，在获取后，可以直接进行强转。
     *
     * @param promise `Promise<GlobalState>` 接口实例，详见 {@link com.herewhite.sdk.domain.Promise Promise}。你可以通过该接口获取 `getGlobalState` 的调用结果：
     *                - 如果方法调用成功，则返回 `GlobalState` 对象，详见 {@link com.herewhite.sdk.domain.GlobalState GlobalState}。
     *                - 如果方法调用失败，则返回错误信息。
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
     * 文档中隐藏
     */
    private <T> void getGlobalState(final Class<T> classOfT, final Promise<T> promise) {
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
     * 获取当前的白板工具状态。
     *
     * @since 2.4.0
     *
     * @note
     * - 该方法为同步调用。
     * - 调用 {@link #setMemberState(MemberState) setMemberState} 方法后，可以立即调用 {@link #getMemberState() getMemberState} 获取最新的白板工具状态。
     *
     * @return 当前的白板工具状态，详见 {@link com.herewhite.sdk.domain.MemberState MemberState}。
     *
     */
    public MemberState getMemberState() {
        return syncRoomState.getDisplayerState().getMemberState();
    }

    /**
     * 获取当前的白板工具状态。
     *
     * @note 该方法为异步调用。
     *
     * @param promise `Promise<MemberState>` 接口实例，详见 {@link com.herewhite.sdk.domain.Promise Promise}。你可以通过该接口获取 `getMemberState` 的调用结果：
     *                - 如果方法调用成功，则返回白板工具状态，详见 {@link com.herewhite.sdk.domain.MemberState MemberState}。
     *                - 如果方法调用失败，则返回错误信息。
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
     * 获取房间的用户列表。
     *
     * @note
     * - 该方法为同步调用。
     * - 房间的用户列表仅包含互动模式（具有读写权限）的用户，不包含订阅模式（只读权限）的用户。
     *
     * @return 用户列表，详见 {@link com.herewhite.sdk.domain.RoomMember RoomMember}。
     *
     */
    public RoomMember[] getRoomMembers() {
        return syncRoomState.getDisplayerState().getRoomMembers();
    }

    /**
     * 获取房间的用户列表。
     *
     * @note
     * - 该方法为异步调用。
     * - 房间的用户列表仅包含互动模式（具有读写权限）的用户，不包含订阅模式（只读权限）的用户。
     *
     * @param promise `Promise<RoomMember[]>` 接口实例，详见 {@link com.herewhite.sdk.domain.Promise Promise}。你可以通过该接口获取 `getRoomMembers` 的调用结果：
     *                - 如果方法调用成功，则返回用户列表，详见 {@link com.herewhite.sdk.domain.RoomMember RoomMember}。
     *                - 如果方法调用失败，则返回错误信息。
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
     * 获取用户的视角状态。
     *
     * @since 2.4.0
     *
     * @note
     * - 该方法为同步调用。
     * - 调用 {@link #setViewMode(ViewMode) setViewMode} 修改用户视角模式后，无法立刻通过 {@link #getBroadcastState() getBroadcastState}[1/2] 获取最新的用户视角状态。
     * 如果需要立即获取最新的用户视角状态，可以调用 {@link #getBroadcastState(Promise<BroadcastState> promise) getBroadcastState}[2/2]。
     *
     * @return 用户视角状态，详见 {@link com.herewhite.sdk.domain.BroadcastState BroadcastState}。
     *
     */
    public BroadcastState getBroadcastState() {
        return syncRoomState.getDisplayerState().getBroadcastState();
    }

    /**
     * 获取用户视角状态。
     *
     * @note
     * - 该方法为异步调用。
     * - 调用 {@link #setViewMode(ViewMode) setViewMode} 修改用户视角模式后，无法立刻通过 {@link #getBroadcastState getBroadcastState}[1/2] 获取最新的用户视角状态。如果需要
     * 立即获取最新的用户视角状态，可以调用 {@link #getBroadcastState(Promise<BroadcastState> promise) getBroadcastState}[2/2]。
     *
     * @param promise `Promise<BroadcastState>` 接口实例，详见 {@link com.herewhite.sdk.domain.Promise Promise}。你可以通过该接口获取 `getBroadcastState` 的调用结果：
     *                - 如果方法调用成功，则返回用户视角状态，详见 {@link com.herewhite.sdk.domain.BroadcastState BroadcastState}。
     *                - 如果方法调用失败，则返回错误信息。
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
     * 获取房间当前场景组下的场景状态。
     *
     * @since 2.4.0
     *
     * @note
     * - 该方法为同步调用。
     * - 调用以下方法修改或新增场景后，无法通过 {@link #getSceneState() getSceneState}[1/2] 立即获取最新的场景状态。此时，如果需要立即获取最新的场景状态，
     * 可以调用 {@link #getSceneState(Promise<SceneState> promise) getSceneState}[2/2]。
     *   - {@link #setScenePath(String path) setScenePath}[1/2]
     *   - {@link #setScenePath(String path, Promise<Boolean> promise) setScenePath}[2/2]
     *   - {@link #putScenes(String, Scene[], int)}
     *
     * @return 当前场景组下的场景状态，详见 {@link com.herewhite.sdk.domain.SceneState SceneState}。
     */
    public SceneState getSceneState() {
        return syncRoomState.getDisplayerState().getSceneState();
    }

    /**
     * 获取房间当前场景组下的场景状态。
     *
     * @note
     * - 该方法为异步调用。
     * - 调用以下方法修改或新增场景后，你可以通过 {@link #getSceneState(Promise<SceneState> promise) getSceneState} 立即获取最新的场景状态。
     *   - {@link #setScenePath(String path) setScenePath}[1/2]
     *   - {@link #setScenePath(String path, Promise<Boolean> promise) setScenePath}[2/2]
     *   - {@link #putScenes(String, Scene[], int)}
     *
     * @param promise `Promise<SceneState>` 接口实例，详见 {@link com.herewhite.sdk.domain.Promise Promise}。你可以通过该接口获取 `getSceneState` 的调用结果：
     *                - 如果方法调用成功，则返回场景状态，详见 {@link com.herewhite.sdk.domain.SceneState SceneState}。
     *                - 如果方法调用失败，则返回错误信息。
     *
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
     * 获取房间当前场景组下的场景列表。
     *
     * @since 2.4.0
     *
     * @note
     * - 该方法为同步调用。
     * - 调用以下方法修改或新增场景后，无法通过 {@link #getScenes() getScenes}[1/2] 立即获取最新的场景列表。
     * 此时，如果需要立即获取最新的场景列表，可以调用 {@link #getScenes(Promise<Scene[]> promise) getScenes}[2/2]。
     *   - {@link #setScenePath(String path) setScenePath}[1/2]
     *   - {@link #setScenePath(String path, Promise<Boolean> promise) setScenePath}[2/2]
     *   - {@link #putScenes(String, Scene[], int) putScenes}
     *
     * @return 当前场景组下的场景列表，详见 {@link com.herewhite.sdk.domain.Scene Scene}。
     */
    public Scene[] getScenes() {
        return this.getSceneState().getScenes();
    }

    /**
     * 获取房间当前场景组下的场景列表。
     *
     * @note
     * - 该方法为异步调用。
     * - 调用以下方法修改或新增场景后，可以调用 {@link #getScenes(Promise<Scene[]> promise) getScenes}，立即获取最新的场景列表。
     *   - {@link #setScenePath(String path) setScenePath}[1/2]
     *   - {@link #setScenePath(String path, Promise<Boolean> promise) setScenePath}[2/2]
     *   - {@link #putScenes(String, Scene[], int) putScenes}
     *
     * @param promise `Promise<Scene[]>` 接口实例，详见 {@link com.herewhite.sdk.domain.Promise Promise}。你可以通过该接口获取 `getScenes` 的调用结果：
     *                - 如果方法调用成功，则返回场景列表，详见 {@link com.herewhite.sdk.domain.Scene Scene}。
     *                - 如果方法调用失败，则返回错误信息。
     *
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
     * 获取用户当前的视角缩放比例。
     *
     * @since 2.4.0
     *
     * @deprecated 该方法已废弃。
     *
     * @note
     * - 该方法为同步调用。
     * - 调用 {@link #zoomChange(double) zoomChange} 或 {@link #moveCamera(CameraConfig) moveCamera} 调整视角缩放比例后，无法通过 {@link #getZoomScale() getZoomScale}[1/2] 立即获取最新的缩放比例。
     * 此时，如果需要立即获取最新的缩放比例，可以调用 {@link #getZoomScale(Promise<Number> promise) getZoomScale}[2/2]。
     *
     * @return 视角缩放比例。
     */
    public double getZoomScale() {
        return syncRoomState.getDisplayerState().getZoomScale();
    }

    /**
     * 获取当前用户的视角缩放比例。
     *
     * @deprecated 该方法已废弃。
     *
     * @note
     * - 该方法为异步调用。
     * - 调用 {@link #zoomChange(double) zoomChange} 或 {@link #moveCamera(CameraConfig) moveCamera} 调整视角缩放比例后，如果需要立即获取最新的缩放比例，可以调用 {@link #getZoomScale(Promise<Number> promise) getZoomScale}[2/2]。
     *
     * @param promise `Promise<Number>` 接口实例，详见 {@link com.herewhite.sdk.domain.Promise Promise}。你可以通过该接口获取 `getZoomScale` 的调用结果：
     *                - 如果方法调用成功，则返回视角缩放比例。
     *                - 如果方法调用失败，则返回错误信息。
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
     * 获取房间的连接状态。
     *
     * @since 2.4.0
     *
     * @note
     * - 该方法为同步调用。
     * - 调用 {@link #disconnect() disconnect}[1/2] 或 {@link #disconnect(@Nullable Promise<Object> promise) disconnect}[2/2] 断开 SDK 与实时房间的连接后，无法立即通过 {@link #getRoomPhase() getRoomPhase}[1/2] 获取最新的房间连接状态。
     * 此时，你可以调用 {@link #getRoomPhase(Promise<RoomPhase> promise) getRoomPhase}[2/2] 立即获取最新的房间连接状态。
     *
     * @return 房间的连接状态，详见 {@link com.herewhite.sdk.domain.RoomPhase RoomPhase}。
     */
    public RoomPhase getRoomPhase() {
        return this.roomPhase;
    }

    /**
     * 获取房间的连接状态。
     *
     * @note
     * - 该方法为异步调用。
     * - 调用 {@link #disconnect() disconnect}[1/2] 或 {@link #disconnect(@Nullable Promise<Object> promise) disconnect}[2/2] 断开 SDK 与实时房间的连接后，无法立即通过 {@link #getRoomPhase() getRoomPhase}[1/2] 获取最新的房间连接状态。
     * 此时，你可以调用 {@link #getRoomPhase(Promise<RoomPhase> promise) getRoomPhase}[2/2] 立即获取最新的房间连接状态。
     *
     * @param promise `Promise<RoomPhase>` 接口实例，详见 {@link com.herewhite.sdk.domain.Promise Promise}。你可以通过该接口获取 `getRoomPhase` 的调用结果：
     *                - 如果方法调用成功，则返回房间连接状态，详见 {@link com.herewhite.sdk.domain.RoomPhase RoomPhase}。
     *                - 如果方法调用失败，则返回错误信息。
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
     * 获取房间的所有状态。
     *
     * @since 2.4.0
     *
     * @note
     * - 该方法为同步调用。
     * - 修改房间的状态属性后，无法立即通过 {@link #getRoomState() getRoomState}[1/2] 获取最新的房间状态。
     * 此时，如果需要立即获取最新的房间状态，可以调用 {@link #getRoomState(Promise<RoomState> promise) getRoomState}[2/2] 获取。
     *
     * @return 房间当前的所有状态，详见 {@link com.herewhite.sdk.domain.RoomState RoomState}。
     *
     */
    public RoomState getRoomState() {
        return syncRoomState.getDisplayerState();
    }

    /**
     * 获取房间的所有状态。
     *
     * @note
     * - 该方法为异步调用。
     * - 修改房间的状态属性后，无法立即通过 {@link #getRoomState() getRoomState} 获取最新的房间状态。此时，如果需要立即获取最新的房间状态，可以调用 {@link #getRoomState(Promise<RoomState> promise) getRoomState}[2/2] 获取。
     *
     * @param promise `Promise<RoomState>` 接口实例，详见 {@link com.herewhite.sdk.domain.Promise Promise}。你可以通过该接口获取 `getRoomState` 的调用结果：
     *                - 如果方法调用成功，则返回房间所有状态，详见 {@link com.herewhite.sdk.domain.RoomState RoomState}。
     *                - 如果方法调用失败，则返回错误信息。
     */
    public void getRoomState(final Promise<RoomState> promise) {
        bridge.callHandler("room.state.getRoomState", new OnReturnValue<Object>() {
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
     * 切换至指定的场景。
     *
     * 方法调用成功后，房间内的所有用户看到的白板都会切换到指定场景。
     *
     * @note
     * - 该方法为同步调用。
     * - 如需获取方法调用回调，请使用 {@link #setScenePath(String path, Promise<Boolean> promise) setScenePath}[2/2]。
     *
     * 场景切换失败可能有以下原因：
     * - 路径不合法，请确保场景路径以 `/` 开头，由场景组和场景名构成。
     * - 场景路径对应的场景不存在。
     * - 传入的路径是场景组的路径，而不是场景路径。
     *
     * @param path 想要切换到的场景的场景路径，请确保场景路径以 `/` 开头，由场景组和场景名构成，例如，`/math/classA`.
     */
    public void setScenePath(String path) {
        bridge.callHandler("room.setScenePath", new Object[]{path});
    }

    /**
     * 切换至指定的场景。
     * <p>
     * 方法调用成功后，房间内的所有用户看到的白板都会切换到指定场景。
     *
     * @note
     * 该方法为异步调用。
     * <p>
     * 场景切换失败可能有以下原因：
     * - 路径不合法，请确保场景路径以 "/"，由场景组和场景名构成。
     * - 场景路径对应的场景不存在。
     * - 传入的路径是场景组的路径，而不是场景路径。
     *
     * @param path    想要切换到的场景的场景路径，请确保场景路径以 "/"，由场景组和场景名构成，例如，`/math/classA`.
     * @param promise `Promise<Boolean>` 接口，详见 {@link com.herewhite.sdk.domain.Promise Promise}。你可以通过该接口获取 `setScenePath` 的调用结果：
     *                - 如果方法调用成功，则返回 `true`.
     *                - 如果方法调用失败，则返回错误信息。
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
     * 切换至当前场景组下的指定场景。
     * <p>
     * 方法调用成功后，房间内的所有用户看到的白板都会切换到指定场景。
     * 指定的场景必须在当前场景组中，否则，方法调用会失败。
     *
     * @param index   目标场景在当前场景组下的索引号。
     * @param promise `Promise<Boolean>` 接口，详见 {@link com.herewhite.sdk.domain.Promise Promise}。你可以通过该接口获取 `setSceneIndex` 的调用结果：
     *                - 如果方法调用成功，则返回 `true`。
     *                - 如果方法调用失败，则返回错误信息。
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
     * 在指定场景组下插入多个场景。
     *
     * @note 调用该方法插入多个场景后不会切换到新插入的场景。如果要切换至新插入的场景，需要调用 `setScenePath`。
     *
     * @param dir    场景组名称，必须以 `/` 开头。不能为场景路径。
     * @param scenes 由多个场景构成的数组。单个场景的字段详见 {@link com.herewhite.sdk.domain.Scene Scene}。
     * @param index  待插入的多个场景中，第一个场景在该场景组的索引号。如果传入的索引号大于该场景组已有场景总数，新插入的场景会排在现有场景的最后。场景的索引号从 0 开始。
     *
     * **Example**
     * <pre>
     * {@code
     * room.putScenes("ppt", new Scene[]{new Scene("page1", new PptPage("https://white-pan.oss-cn-shanghai.aliyuncs.com/101/image/alin-rusu-1239275-unsplash_opt.jpg", 1024d, 768d))}, 0);
     * room.setScenePath("ppt" + "/page1");
     * }
     * </pre>
     */
    public void putScenes(String dir, Scene[] scenes, int index) {
        bridge.callHandler("room.putScenes", new Object[]{dir, scenes, index});
    }

    /**
     * 移动场景。
     * <p>
     * 成功移动场景后，场景路径也会改变。
     *
     * @note
     * - 该方法只能移动场景，不能移动场景组，即 `sourcePath` 只能是场景路径，不能是场景组路径。
     * - 该方法支持改变指定场景在当前所属场景组下的位置，也支持将指定场景移至其他场景组。
     *
     * @param sourcePath      需要移动的场景原路径。必须为场景路径，不能是场景组的路径。
     * @param targetDirOrPath 目标场景组路径或目标场景路径：
     *                        - 当 `targetDirOrPath`设置为目标场景组时，表示将指定场景移至其他场景组中，场景路径会发生改变，但是场景名称不变。
     *                        - 当 `targetDirOrPath`设置为目标场景路径时，表示改变指定场景在当前场景组的位置，场景路径和场景名都会发生改变。
     */
    public void moveScene(String sourcePath, String targetDirOrPath) {
        bridge.callHandler("room.moveScene", new Object[]{sourcePath, targetDirOrPath});
    }

    /**
     * 删除场景或者场景组。
     *
     * @note
     * - 互动白板实时房间内必须至少有一个场景。当删除所有的场景后，SDK 会自动生成一个路径为 `/init` 初始场景（房间初始化时的默认场景）。
     * - 如果删除白板当前所在场景，白板会展示被删除场景所在场景组的最后一个场景
     * - 如果删除的是场景组，则该场景组下的所有场景都会被删除。
     * - 如果删除的是当前场景所在的场景组，例如 `dirA`，SDK 会执行向上递归逻辑选择新的场景作为当前场景，规则如下：
     *    1. 如果当前场景组路径下还有其他场景组，例如 `dirB`，排在被删除的场景组 `dirA` 后面，则将场景切换至
     *    `dirB` 中的第一个场景（index 为 0）。
     *    2. 如果当前场景组路径下 `dirA` 后不存在场景组，则查看当前场景组路径下是否存在场景；
     *    如果存在，则将场景切换至当前场景组路径下的第一个场景（index 为 0）。
     *    3. 如果当前场景组路径下 `dirA` 后没有场景组，也不存在任何场景，则查看 `dirA` 前面是否存在场景组 `dirC`；如果存在，则选择 `dirC` 中的第一个场景（index 为 0）。
     *    4. 以上都不满足，则继续向上递归执行该逻辑。
     *
     * @param dirOrPath 场景组路径或者场景路径。如果传入的是场景组，则会删除该场景组下的所有场景。
     */
    public void removeScenes(String dirOrPath) {
        bridge.callHandler("room.removeScenes", new Object[]{dirOrPath});
    }

    /**
     * 清除当前场景的所有内容。
     *
     * @param retainPpt 是否保留 PPT 内容：
     *                  - `true`：保留 PPT。
     *                  - `false`：连 PPT 一起清空。
     */
    public void cleanScene(boolean retainPpt) {
        bridge.callHandler("room.cleanScene", new Object[]{retainPpt});
    }
    //endregion

    //region PPT

    /**
     * 播放动态 PPT 下一页。
     *
     * @since 2.2.0
     * <p>
     * 当前 PPT 页面的动画已全部执行完成时，SDK 会将场景切换至下一页 PPT。
     */
    public void pptNextStep() {
        bridge.callHandler("ppt.nextStep", new Object[]{});
    }

    /**
     * 返回动态 PPT 上一页。
     *
     * @since 2.2.0
     * <p>
     * 当前 PPT 页面的动画全部回退完成时，SDK 会将场景切回至上一页 PPT。
     */
    public void pptPreviousStep() {
        bridge.callHandler("ppt.previousStep", new Object[]{});
    }
    //endregion

    /**
     * 更新视角的缩放比例。
     *
     * @deprecated 该方法已经废弃。请使用 {@link #moveCamera(CameraConfig)}。
     *
     * @param scale 视角的缩放比例。
     */
    @Deprecated
    public void zoomChange(double scale) {
        CameraConfig config = new CameraConfig();
        config.setScale(scale);
        this.moveCamera(config);
    }

    /**
     * 获取调试日志信息。
     *
     * @since 2.6.2
     *
     * @param promise `Promise<JSONObject>` 接口实例，详见 {@link com.herewhite.sdk.domain.Promise Promise}。你可以通过该接口获取 `debugInfo` 的调用结果：
     *                - 如果方法调用成功，则返回调试日志信息。
     *                - 如果方法调用失败，则返回错误信息。
     */
    public void debugInfo(final Promise<JSONObject> promise) {
        bridge.callHandler("room.state.debugInfo", new OnReturnValue<JSONObject>() {
            @Override
            public void onValue(JSONObject retValue) {
                promise.then(retValue);
            }
        });
    }


    //region Disable

    /**
     * 允许/禁止白板响应用户任何操作。
     * <p>
     * 该方法设置是否禁止白板响应用户的操作，包括：
     * - `CameraTransform`：移动、缩放视角。
     * - `DeviceInputs`：使用白板工具输入。
     *
     * @param disableOperations 允许/禁止白板响应用户任何操作。
     *                          - `true`：不响应用户操作。
     *                          - `false`：（默认）响应用户操作。
     */
    public void disableOperations(final boolean disableOperations) {
        disableCameraTransform(disableOperations);
        disableDeviceInputs(disableOperations);
    }

    /**
     * 设置用户在房间中是否为互动模式。
     *
     * @since 2.6.1
     *
     * @param writable 用户在房间中是否为互动模式：
     *                 - `true`：互动模式，即具有读写权限。
     *                 - `false`：订阅模式，即具有只读权限。
     * @param promise  `Promise<Boolean>` 接口实例，详见 {@link com.herewhite.sdk.domain.Promise Promise}。你可以通过该接口获取 `setWritable` 的调用结果：
     *                 - 如果方法调用成功，则返回用户在房间中的读写状态。
     *                 - 如果方法调用失败，则返回错误信息。
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
     * 关闭/开启橡皮擦擦除图片功能。
     *
     * @since 2.9.3
     *
     * @param disable 是否关闭橡皮擦擦除图片功能：
     *                - `true`：禁止橡皮擦擦除图片。
     *                - `false`：（默认）允许橡皮擦擦除图片。
     */
    public void disableEraseImage(boolean disable) {
        bridge.callHandler("room.sync.disableEraseImage", new Object[]{disable});
    }

    /**
     * 禁止/允许用户调整（移动或缩放）视角。
     *
     * @since 2.2.0
     *
     * @param disableCameraTransform 是否禁止用户调整视角：
     *                               - `true`：禁止用户调整视角。
     *                               - `false`：（默认）允许用户调整视角。
     */
    public void disableCameraTransform(final boolean disableCameraTransform) {
        bridge.callHandler("room.disableCameraTransform", new Object[]{disableCameraTransform});
    }

    /**
     * 禁止/允许用户操作白板工具。
     *
     * @since 2.2.0
     *
     * @param disableOperations 是否禁止用户操作白板工具：
     *                          - `true`：禁止用户操作白板工具操作。
     *                          - `false`：（默认）允许用户操作白板工具输入操作。
     */
    public void disableDeviceInputs(final boolean disableOperations) {
        bridge.callHandler("room.disableDeviceInputs", new Object[]{disableOperations});
    }

    /**
     * 禁止/允许窗口操作。
     *
     * @since 2.2.0
     *
     * @param disableWindowOperation 是否禁止窗口操作：
     *                          - `true`：禁止窗口操作。
     *                          - `false`：（默认）允许窗口操作。
     */
    public void disableWindowOperation(final boolean disableWindowOperation) {
        bridge.callHandler("room.disableWindowOperation", new Object[]{disableWindowOperation});
    }
    //endregion

    //region Delay API

    /**
     * 设置将本地用户的白板内容向远端同步的延时。
     * <p>
     * 调用该方法后，SDK 会根据设置的时长延迟将本地用户的白板内容同步给远端用户。
     * <p>
     * 在 CDN 直播场景，设置白板延时，有助于白板内容和音视频内容同步。
     *
     * @note 该方法不影响本地白板画面的显示，即用户在本地白板上的操作，会立即在本地白板上显示。
     *
     * @param delaySec 延时时长，单位为秒。
     */
    public void setTimeDelay(double delaySec) {
        bridge.callHandler("room.setTimeDelay", new Object[]{(int) (delaySec * 1000)});
        this.timeDelay = delaySec;
    }

    /**
     * 获取设置得远端白板画面同步延时。
     *
     * @return 延时时长，单位为秒。
     */
    public Double getTimeDelay() {
        return this.timeDelay;
    }
    //endregion


    /**
     * 设置白板同步延时。
     *
     * 在同时使用白板和实时音视频的场景中，你可以根据实时音视频的延迟，设置发送白板的延时，以确保音视频与白板内容同步。
     *
     * @param utcMs 白板同步延时，为 UNIX 时间戳，单位为毫秒。
     */
    public void syncBlockTimestamp(long utcMs) {
        bridge.callHandler("room.sync.syncBlockTimestamp", new Object[]{utcMs});
    }

    /**
     * 发送自定义事件。
     *
     * @note 所有注册监听该事件的用户都会收到通知。
     *
     * @param eventEntry 自定义事件内容，详见 {@link com.herewhite.sdk.domain.AkkoEvent AkkoEvent}。
     */
    public void dispatchMagixEvent(AkkoEvent eventEntry) {
        bridge.callHandler("room.dispatchMagixEvent", new Object[]{eventEntry});
    }
    //endregion

    // 添加窗口
    public void addApp(WindowAppParam appParam, Promise<String> promise) {
        String kind = appParam.getKind();
        WindowAppParam.Options options = appParam.getOptions();
        WindowAppParam.Attributes attributes = appParam.getAttributes();
        bridge.callHandler("room.addApp", new Object[]{kind, options, attributes}, new OnReturnValue<String>() {
            @Override
            public void onValue(String value) {
                if (promise != null) {
                    promise.then(value);
                }
            }
        });
    }

    // 获取状态数据
    public <T> void getSyncedState(Class<T> stateClass, Promise<Object> promise) {
        bridge.callHandler("room.getSyncedState", new Object[]{}, (OnReturnValue<String>) value -> {
            try {
                promise.then(gson.fromJson(value, stateClass));
            } catch (Exception e) {
                Logger.error("parse json error", e);
            }
        });
    }

    // 设置状态数据
    public void safeSetAttributes(SyncedState state) {
        bridge.callHandler("room.safeSetAttributes", new Object[]{state});
    }

    // 更新状态数据
    public void safeUpdateAttributes(String[] keys, SyncedState state) {
        bridge.callHandler("room.safeUpdateAttributes", new Object[]{keys, state});
    }

    // region roomListener
    // 关于此处的回调在JsBridge线程，请考虑/讨论确定是否在主执行
    private RoomListener roomListener;

    void setRoomListener(RoomListener roomCallbacks) {
        this.roomListener = roomCallbacks;
    }

    private SyncDisplayerState.Listener<RoomState> localRoomStateListener = modifyState -> {
        post(() -> {
            if (roomListener != null) {
                roomListener.onRoomStateChanged(modifyState);
            }
        });
    };

    //endregion
    private RoomDelegate roomDelegate;

    public RoomDelegate getRoomDelegate() {
        if (roomDelegate == null) {
            roomDelegate = new RoomDelegateImpl();
        }
        return roomDelegate;
    }

    private class RoomDelegateImpl implements RoomDelegate {
        /**
         * 自定义事件回调
         * 文档中隐藏
         *
         * @param eventEntry {@link EventEntry} 自定义事件内容，相对于 {@link AkkoEvent} 多了发送者的 memberId
         */
        @Override
        public void fireMagixEvent(EventEntry eventEntry) {
            EventListener eventListener = eventListenerMap.get(eventEntry.getEventName());
            if (eventListener != null) {
                eventListener.onEvent(eventEntry);
            }
        }

        /// @cond test

        /**
         * 文档中隐藏。
         *
         * @param eventEntries
         */
        @Override
        public void fireHighFrequencyEvent(EventEntry[] eventEntries) {
            FrequencyEventListener eventListener = frequencyEventListenerMap.get(eventEntries[0].getEventName());
            if (eventListener != null) {
                eventListener.onEvent(eventEntries);
            }
        }
        /// @endcond

        @Override
        public void firePhaseChanged(RoomPhase roomPhase) {
            post(() -> setRoomPhase(roomPhase));
        }

        @Override
        public void fireCanUndoStepsUpdate(long canUndoSteps) {
            post(() -> {
                if (roomListener != null) {
                    roomListener.onCanUndoStepsUpdate(canUndoSteps);
                }
            });
        }

        @Override
        public void onCanRedoStepsUpdate(long canRedoSteps) {
            post(() -> {
                if (roomListener != null) {
                    roomListener.onCanRedoStepsUpdate(canRedoSteps);
                }
            });
        }

        @Override
        public void fireKickedWithReason(String reason) {
            post(() -> {
                if (roomListener != null) {
                    roomListener.onKickedWithReason(reason);
                }
            });
        }

        @Override
        public void fireDisconnectWithError(Exception exception) {
            post(() -> {
                if (roomListener != null) {
                    roomListener.onDisconnectWithError(exception);
                }
            });
        }

        @Override
        public void fireCatchErrorWhenAppendFrame(long userId, Exception exception) {
            post(() -> {
                if (roomListener != null) {
                    roomListener.onCatchErrorWhenAppendFrame(userId, exception);
                }
            });
        }

        @Override
        public void fireAttributesUpdate(String valueOf) {
            post(() -> {
                if (roomListener != null) {
                    roomListener.onAttributesUpdate(valueOf);
                }
            });
        }

        @Override
        public void fireRoomStateChanged(String stateJSON) {
            syncRoomState.syncDisplayerState(stateJSON);
        }
    }
}
