package com.herewhite.sdk.domain;

import com.google.gson.Gson;

/**
 * 实时房间，回放房间共有State
 * @since 2.4.7
 */
public class WhiteDisplayerState extends WhiteObject {

    static Gson gson = new Gson();
    static Class customClass = GlobalState.class;

    /**
     * 设置自定义全局变量类型，设置后，所有 GlobalState 都会转换为该类的实例。
     *
     * @param <T> 类型约束
     * @param classOfT 自定义 GlobalState Class
     * @since 2.4.7
     */
    public static <T extends GlobalState> void setCustomGlobalStateClass(Class<T> classOfT) {
        customClass = classOfT;
    }

    /**
     * 获取当前的自定义 GlobalState Class
     *
     * @param <T> 泛型约束
     * @return 自定义 GlobalState Class 类
     * @since 2.4.7
     */
    public static <T extends GlobalState>Class<T> getCustomGlobalStateClass() {
        return customClass;
    }
    /**
     * 全局状态，所有用户可见，实时房间时，可读可写；回放房间只读。返回内容为 sdk 默认全局状态
     *
     * @return 全局状态
     */
    public GlobalState getGlobalState() {
        String str = gson.toJson(globalState);
        Object customInstance = gson.fromJson(str, customClass);
        return ((GlobalState) customInstance);
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

}
