package com.herewhite.sdk.domain;

import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;

/**
 * 实时房间，回放房间共有State
 *
 * @since 2.4.8
 */
public class WhiteDisplayerState extends WhiteObject {

    static Gson gson = new Gson();
    static Class<?> customClass = GlobalState.class;

    /**
     * 设置自定义全局变量类型，设置后，所有 GlobalState 都会转换为该类的实例。
     *
     * @param <T>      类型约束
     * @param classOfT 自定义 GlobalState Class
     * @since 2.4.8
     */
    public static <T extends GlobalState> void setCustomGlobalStateClass(Class<T> classOfT) {
        customClass = classOfT;
    }

    /**
     * 全局状态，所有用户可见，实时房间时，可读可写；回放房间只读。返回内容为 sdk 默认全局状态
     *
     * @return 全局状态
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
     * 获取房间中所有用户列表
     *
     * @return 用户成员状态列表 [ ]
     * @see RoomMember
     */
    public RoomMember[] getRoomMembers() {
        return roomMembers;
    }

    /**
     * 获取当前场景目录下的场景状态
     *
     * @return 当前场景目录下的场景状态
     * @see SceneState
     */
    public SceneState getSceneState() {
        return sceneState;
    }

    private Object globalState;
    private RoomMember[] roomMembers;
    private SceneState sceneState;

    public CameraState getCameraState() {
        return cameraState;
    }

    private CameraState cameraState;

}
