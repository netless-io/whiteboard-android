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
    private Object globalState;
    private RoomMember[] roomMembers;
    private SceneState sceneState;
    private String windowBoxState;
    private PageState pageState;
    private AppState appState;
    private CameraState cameraState;

    /**
     * 设置自定义 `GlobalState`类。
     *
     * @since 2.4.8
     * <p>
     * 设置后，所有 `GlobalState` 都会转换为该类的实例。
     *
     * @param <T>      类型约束，自定义的 `GlobalState` 类必须继承 {@link com.herewhite.sdk.domain.GlobalState GlobalState} 类。
     * @param classOfT 自定义的 `GlobalState` 类。
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
     * 获取房间的用户列表。
     *
     * @note
     * 房间的用户列表仅包含互动模式（具有读写权限）的用户，不包含订阅模式（只读权限）的用户。
     *
     * @return 用户列表，详见 {@link RoomMember RoomMember}。
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

    /**
     * 获取视角状态。
     *
     * @return 视角状态，详见 {@link CameraState}。
     */
    public CameraState getCameraState() {
        return cameraState;
    }

    /**
     * 获取多窗口下窗口展示状态，为一下值：
     * maximized: 最大化
     * minimized: 最小化
     * normal   : 默认展开
     *
     * @experiment
     * @return 窗口展开状态
     */
    public String getWindowBoxState() {
        return windowBoxState;
    }

    /**
     * 获取多窗口下主白板页面状态
     *
     * @return 主白板页面状态
     */
    public PageState getPageState() {
        return pageState;
    }

    /**
     * 获取多窗口下应用状态
     *
     * @return 应用状态
     */
    public AppState getAppState() {
        return appState;
    }
}
