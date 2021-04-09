package com.herewhite.sdk.domain;

import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;

/**
 * `WhiteDisplayerState` 类，为互动白板实时房间和回放房间共有的状态类。
 *
 * @since 2.4.8
 */
public class WhiteDisplayerState extends WhiteObject {

    static Gson gson = new Gson();
    static Class<?> customClass = GlobalState.class;

    /**
     * 设置自定义 `GlobalState`类。
     *
     * @since 2.4.8
     * <p>
     * 设置后，所有 `GlobalState` 都会转换为该类的实例。
     *
     * @param <T>      类型约束，自定义的 'GlobalState' 类必须继承 {@link GlobalState GlobalState} 类。
     * @param classOfT 自定义的 'GlobalState' 类。
     */
    public static <T extends GlobalState> void setCustomGlobalStateClass(Class<T> classOfT) {
        customClass = classOfT;
    }

    /**
     * 获取房间的全局状态。
     *
     * @return 房间的全局状态。
     */
    public GlobalState getGlobalState() {
        String str = gson.toJson(globalState);
        Object customInstance = null;
        try {
            customInstance = gson.fromJson(str, customClass);
        } catch (JsonSyntaxException e) {
            Log.e("getGlobalState error", e.getMessage());
        }
        if (customClass.isInstance(customInstance)) {
            return ((GlobalState) customInstance);
        } else {
            return null;
        }
    }

    /**
     * 获取房间中所有的互动模式（具有读写权限）的用户。
     *
     * @return 互动模式（具有读写权限）的用户列表，详见 {@link RoomMember}。
     */
    public RoomMember[] getRoomMembers() {
        return roomMembers;
    }

    /**
     * 获取当前场景组下的场景状态。
     *
     * @return 当前场景组下的场景状态，详见 {@link SceneState}。
     */
    public SceneState getSceneState() {
        return sceneState;
    }

    private Object globalState;
    private RoomMember[] roomMembers;
    private SceneState sceneState;

    /**
     * 获取视角状态。
     *
     * @return 视角状态，详见 {@link CameraState}。
     */
    public CameraState getCameraState() {
        return cameraState;
    }

    private CameraState cameraState;

}
